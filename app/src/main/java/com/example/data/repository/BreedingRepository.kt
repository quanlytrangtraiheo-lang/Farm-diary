package com.example.data.repository

import com.example.data.local.dao.BreedingCycleDao
import com.example.data.local.dao.EventDao
import com.example.data.local.dao.PigDao
import com.example.data.local.dao.TaskDao
import com.example.data.local.entity.BreedingCycleEntity
import com.example.data.local.entity.EventEntity
import com.example.data.model.SowReproductiveStatus
import com.example.data.model.SyncStatus
import com.example.domain.AutoTaskPipeline
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class BreedingRepository(
    private val breedingCycleDao: BreedingCycleDao,
    private val pigDao: PigDao,
    private val eventDao: EventDao,
    private val taskDao: TaskDao
) {
    fun getCyclesForSow(sowId: String): Flow<List<BreedingCycleEntity>> =
        breedingCycleDao.getCyclesForSow(sowId)

    suspend fun getActiveCycle(sowId: String): BreedingCycleEntity? =
        breedingCycleDao.getActiveCycleForSow(sowId)

    /**
     * Start a new breeding cycle (Mating Event) (PDF Page 15-16)
     * Auto-inserts 6 tasks into TaskEntity via AutoTaskPipeline.
     */
    suspend fun addMatingCycle(
        sowId: String,
        parity: Int,
        matingDate: Long,
        boarCode: String,
        recordedBy: String = "Nguyễn Văn Tuấn"
    ): String {
        val cycleId = UUID.randomUUID().toString()
        val dayMs = 86_400_000L
        val expectedFarrowingDate = matingDate + (114 * dayMs)

        val cycle = BreedingCycleEntity(
            cycleId = cycleId,
            sowId = sowId,
            parity = parity,
            matingDate = matingDate,
            boarCode = boarCode,
            expectedFarrowingDate = expectedFarrowingDate,
            cycleStatus = "IN_PROGRESS",
            syncStatus = SyncStatus.PENDING_SYNC,
            updatedAt = System.currentTimeMillis()
        )
        breedingCycleDao.insertCycle(cycle)

        // Update sow status to MANG_THAI
        val pig = pigDao.getPigByIdOnce(sowId)
        if (pig != null) {
            pigDao.updatePig(
                pig.copy(
                    status = SowReproductiveStatus.MANG_THAI,
                    currentCycleId = cycleId,
                    lastMatingDate = matingDate,
                    expectedFarrowingDate = expectedFarrowingDate,
                    parity = parity,
                    syncStatus = SyncStatus.PENDING_SYNC,
                    updatedAt = System.currentTimeMillis()
                )
            )

            // Auto-Task Pipeline execution
            val autoTasks = AutoTaskPipeline.generateTasksForMatingCycle(
                sowTag = pig.tagCode,
                location = pig.location,
                matingDate = matingDate,
                assignedTo = recordedBy
            )
            taskDao.insertTasks(autoTasks)
        }

        // Add event
        eventDao.insertEvent(
            EventEntity(
                id = UUID.randomUUID().toString(),
                pigId = sowId,
                eventType = "Phối giống",
                eventDate = matingDate,
                description = "Phối giống lứa $parity với $boarCode",
                recordedByUserId = recordedBy,
                syncStatus = SyncStatus.PENDING_SYNC
            )
        )

        return cycleId
    }

    /**
     * Record farrowing event (PDF Page 17 - Bottom Sheet "Ghi nhận đẻ")
     */
    suspend fun recordFarrowing(
        sowId: String,
        cycleId: String,
        actualDate: Long,
        totalBorn: Int,
        bornAlive: Int,
        stillborn: Int,
        mummies: Int,
        averageBirthWeightKg: Float,
        recordedBy: String = "Nguyễn Văn Tuấn"
    ) {
        val cycle = breedingCycleDao.getCycleById(cycleId)
        if (cycle != null) {
            breedingCycleDao.updateCycle(
                cycle.copy(
                    actualFarrowingDate = actualDate,
                    totalBorn = totalBorn,
                    bornAlive = bornAlive,
                    stillborn = stillborn,
                    mummies = mummies,
                    averageBirthWeightKg = averageBirthWeightKg,
                    cycleStatus = "IN_PROGRESS",
                    syncStatus = SyncStatus.PENDING_SYNC,
                    updatedAt = System.currentTimeMillis(),
                    version = cycle.version + 1
                )
            )
        }

        val pig = pigDao.getPigByIdOnce(sowId)
        if (pig != null) {
            pigDao.updatePig(
                pig.copy(
                    status = SowReproductiveStatus.NUOI_CON,
                    syncStatus = SyncStatus.PENDING_SYNC,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }

        eventDao.insertEvent(
            EventEntity(
                id = UUID.randomUUID().toString(),
                pigId = sowId,
                eventType = "Đẻ",
                eventDate = actualDate,
                description = "Ghi nhận đẻ: Tổng $totalBorn con (Sống: $bornAlive, Chết lưu: $stillborn, Gỗ: $mummies, TL bình quân: $averageBirthWeightKg kg)",
                metrics = "$bornAlive con sống",
                recordedByUserId = recordedBy,
                syncStatus = SyncStatus.PENDING_SYNC
            )
        )
    }

    /**
     * Record ultrasound result (Đạt + / Không đạt -)
     */
    suspend fun recordUltrasound(
        sowId: String,
        cycleId: String,
        isPositive: Boolean,
        date: Long = System.currentTimeMillis(),
        recordedBy: String = "Nguyễn Văn Tuấn"
    ) {
        val resultStr = if (isPositive) "Đạt (+)" else "Không đạt (-)"
        val cycle = breedingCycleDao.getCycleById(cycleId)
        if (cycle != null) {
            breedingCycleDao.updateCycle(
                cycle.copy(
                    ultrasoundResult = resultStr,
                    ultrasoundDate = date,
                    cycleStatus = if (isPositive) "IN_PROGRESS" else "FAILED",
                    syncStatus = SyncStatus.PENDING_SYNC,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }

        val pig = pigDao.getPigByIdOnce(sowId)
        if (pig != null) {
            val nextStatus = if (isPositive) SowReproductiveStatus.MANG_THAI else SowReproductiveStatus.HAU_BI
            pigDao.updatePig(
                pig.copy(
                    status = nextStatus,
                    hasUltrasoundWarning = false,
                    syncStatus = SyncStatus.PENDING_SYNC,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }

        eventDao.insertEvent(
            EventEntity(
                id = UUID.randomUUID().toString(),
                pigId = sowId,
                eventType = "Siêu âm thai",
                eventDate = date,
                description = "Siêu âm thai kết quả: $resultStr",
                recordedByUserId = recordedBy,
                syncStatus = SyncStatus.PENDING_SYNC
            )
        )
    }

    fun getEventsForPig(pigId: String): Flow<List<EventEntity>> =
        eventDao.getEventsForPig(pigId)

    fun getAllEvents(): Flow<List<EventEntity>> =
        eventDao.getAllEvents()
}
