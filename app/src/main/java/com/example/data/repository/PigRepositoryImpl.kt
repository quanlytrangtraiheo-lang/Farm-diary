package com.example.data.repository

import com.example.data.local.dao.PigDao
import com.example.data.local.entity.PigEntity
import com.example.data.model.SowReproductiveStatus
import com.example.data.model.SyncStatus
import com.example.domain.repository.PigRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Lớp triển khai (Implementation) của PigRepository thuộc Data Layer.
 * Tương tác trực tiếp với Room DAO (PigDao) để xử lý lưu trữ dữ liệu cục bộ,
 * gắn nhãn trạng thái đồng bộ (SyncStatus.PENDING_SYNC) và quản lý timestamp cập nhật.
 */
class PigRepositoryImpl(private val pigDao: PigDao) : PigRepository {

    override val allPigs: Flow<List<PigEntity>> = pigDao.getAllPigs()
    override val totalPigCount: Flow<Int> = pigDao.getTotalPigCount()
    override val totalHeadCount: Flow<Int?> = pigDao.getTotalHeadCount()

    override fun getPigById(id: String): Flow<PigEntity?> = pigDao.getPigById(id)

    override suspend fun getPigByIdOnce(id: String): PigEntity? = pigDao.getPigByIdOnce(id)

    override suspend fun addPig(
        tagCode: String,
        isBatch: Boolean,
        quantity: Int,
        breed: String,
        gender: String,
        status: SowReproductiveStatus,
        location: String,
        currentWeightKg: Float,
        parity: Int,
        birthDate: Long
    ): String {
        val id = UUID.randomUUID().toString()
        val pig = PigEntity(
            id = id,
            tagCode = tagCode,
            birthDate = birthDate,
            isBatch = isBatch,
            quantity = quantity,
            breed = breed,
            gender = gender,
            status = status,
            location = location,
            parity = parity,
            currentWeightKg = currentWeightKg,
            syncStatus = SyncStatus.PENDING_SYNC,
            updatedAt = System.currentTimeMillis()
        )
        pigDao.insertPig(pig)
        return id
    }

    override suspend fun updatePig(pig: PigEntity) {
        pigDao.updatePig(
            pig.copy(
                syncStatus = SyncStatus.PENDING_SYNC,
                updatedAt = System.currentTimeMillis(),
                version = pig.version + 1
            )
        )
    }

    override suspend fun deletePig(pig: PigEntity) {
        pigDao.deletePig(pig)
    }

    override suspend fun insertPigs(pigs: List<PigEntity>) {
        pigDao.insertPigs(pigs)
    }
}
