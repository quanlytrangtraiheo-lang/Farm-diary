package com.example.domain.rule

import com.example.data.local.dao.BreedingCycleDao
import com.example.data.local.dao.EventDao
import com.example.data.local.dao.PigDao
import com.example.data.local.dao.TaskDao
import com.example.data.local.entity.BreedingCycleEntity
import com.example.data.local.entity.EventEntity
import com.example.data.local.entity.PigEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.model.SowReproductiveStatus
import com.example.data.model.SyncStatus
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus
import com.example.data.model.TaskType
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Bộ quy tắc nghiệp vụ trang trại thông minh (Farm Rule Engine).
 * Triết lý kiến trúc:
 * SỰ KIỆN (Event) -> QUY TẮC (Rules) -> CÔNG VIỆC TỰ ĐỘNG + TRẠNG THÁI + CẢNH BÁO + BÁO CÁO.
 * Nhân viên chỉ cần 3-5 thao tác nhập sự kiện, Rule Engine tự động xử lý toàn bộ quy trình tiếp theo.
 */
class FarmRuleEngine(
    private val pigDao: PigDao,
    private val taskDao: TaskDao,
    private val eventDao: EventDao,
    private val breedingCycleDao: BreedingCycleDao? = null
) {
    private val dayMs = 86_400_000L
    private val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

    // =========================================================================
    // 1. SỰ KIỆN: ĐẺ (HEO CON THEO MẸ)
    // =========================================================================
    suspend fun onFarrowingEvent(
        sowId: String,
        farrowingDate: Long,
        totalBorn: Int,
        bornAlive: Int,
        stillborn: Int,
        mummies: Int,
        litterWeightKg: Float,
        notes: String = "",
        recordedBy: String = "Nguyễn Văn Tuấn"
    ): String {
        val sow = pigDao.getPigByIdOnce(sowId)
        val sowTag = sow?.tagCode ?: "Nái"

        // 1.1 Cập nhật nái mẹ sang trạng thái NUOI_CON
        if (sow != null) {
            pigDao.updatePig(
                sow.copy(
                    status = SowReproductiveStatus.NUOI_CON,
                    hasUltrasoundWarning = false,
                    hasProlongedGestationWarning = false,
                    syncStatus = SyncStatus.PENDING_SYNC,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }

        // 1.2 Tự động tạo đàn con mới: DC<yyyyMMdd>-001 (Đang theo mẹ)
        val datePrefix = dateFormat.format(Date(farrowingDate))
        val randomSuffix = (100..999).random()
        val pigletBatchTag = "DC$datePrefix-$randomSuffix"
        val pigletBatchId = "piglet-${UUID.randomUUID()}"
        val avgPigletWeight = if (bornAlive > 0) litterWeightKg / bornAlive else 1.4f

        val pigletBatch = PigEntity(
            id = pigletBatchId,
            tagCode = pigletBatchTag,
            birthDate = farrowingDate,
            isBatch = true,
            quantity = bornAlive,
            breed = sow?.breed ?: "F1 Land-York",
            gender = "Hỗn hợp",
            status = SowReproductiveStatus.NUOI_CON,
            location = sow?.location ?: "Chuồng đẻ",
            currentWeightKg = avgPigletWeight,
            syncStatus = SyncStatus.PENDING_SYNC,
            updatedAt = System.currentTimeMillis()
        )
        pigDao.insertPig(pigletBatch)

        // 1.3 Rule Engine: Tự động sinh chuỗi 5 công việc nuôi heo con
        val autoTasks = listOf(
            // Ngày 3 (D+3): Tiêm sắt
            TaskEntity(
                id = UUID.randomUUID().toString(),
                title = "Tiêm sắt đàn con $pigletBatchTag (1ml Iron Dextran)",
                taskType = TaskType.VACCINE,
                dueDate = farrowingDate + (3 * dayMs),
                timeLabel = "08:00",
                assignedTo = recordedBy,
                status = TaskStatus.TODO,
                priority = TaskPriority.HIGH,
                referenceId = pigletBatchTag,
                locationInfo = pigletBatch.location,
                note = "Tiêm phòng thiếu máu sắt sơ sinh cho $bornAlive con theo mẹ",
                syncStatus = SyncStatus.PENDING_SYNC
            ),
            // Ngày 7 (D+7): Cắt đuôi & Thiến
            TaskEntity(
                id = UUID.randomUUID().toString(),
                title = "Cắt đuôi & Kiểm tra rốn $pigletBatchTag",
                taskType = TaskType.GENERAL,
                dueDate = farrowingDate + (7 * dayMs),
                timeLabel = "09:00",
                assignedTo = recordedBy,
                status = TaskStatus.TODO,
                priority = TaskPriority.NORMAL,
                referenceId = pigletBatchTag,
                locationInfo = pigletBatch.location,
                note = "Vệ sinh sát trùng chuồng và cắt đuôi phòng cắn đuôi",
                syncStatus = SyncStatus.PENDING_SYNC
            ),
            // Ngày 14 (D+14): Tiêm vaccine Mycoplasma
            TaskEntity(
                id = UUID.randomUUID().toString(),
                title = "Tiêm vaccine Mycoplasma lần 1 - $pigletBatchTag",
                taskType = TaskType.VACCINE,
                dueDate = farrowingDate + (14 * dayMs),
                timeLabel = "08:30",
                assignedTo = recordedBy,
                status = TaskStatus.TODO,
                priority = TaskPriority.HIGH,
                referenceId = pigletBatchTag,
                locationInfo = pigletBatch.location,
                note = "Tiêm phòng suyễn lợn lần 1 cho bầy $bornAlive con",
                syncStatus = SyncStatus.PENDING_SYNC
            ),
            // Ngày 21 (D+21): Tập ăn
            TaskEntity(
                id = UUID.randomUUID().toString(),
                title = "Tập ăn cho đàn con $pigletBatchTag",
                taskType = TaskType.GENERAL,
                dueDate = farrowingDate + (21 * dayMs),
                timeLabel = "10:00",
                assignedTo = recordedBy,
                status = TaskStatus.TODO,
                priority = TaskPriority.NORMAL,
                referenceId = pigletBatchTag,
                locationInfo = pigletBatch.location,
                note = "Rải cám viên tập ăn chuyên dụng kích thích đường ruột",
                syncStatus = SyncStatus.PENDING_SYNC
            ),
            // Ngày 28 (D+28): Cai sữa
            TaskEntity(
                id = UUID.randomUUID().toString(),
                title = "Dự kiến cai sữa đàn con $pigletBatchTag",
                taskType = TaskType.WEANING_PLAN,
                dueDate = farrowingDate + (28 * dayMs),
                timeLabel = "08:00",
                assignedTo = recordedBy,
                status = TaskStatus.TODO,
                priority = TaskPriority.HIGH,
                referenceId = pigletBatchTag,
                locationInfo = pigletBatch.location,
                note = "Mốc cai sữa chuẩn 28 ngày. Tách mẹ chuyển sang heo cai sữa",
                syncStatus = SyncStatus.PENDING_SYNC
            )
        )
        taskDao.insertTasks(autoTasks)

        // 1.4 Lưu sự kiện Đẻ
        val noteDetail = if (notes.isNotBlank()) " | Ghi chú: $notes" else ""
        eventDao.insertEvent(
            EventEntity(
                id = UUID.randomUUID().toString(),
                pigId = sowId,
                eventType = "Đẻ (Heo con theo mẹ)",
                eventDate = farrowingDate,
                description = "Nái $sowTag đẻ tổng $totalBorn con (Sống: $bornAlive, Chết: $stillborn, Gỗ: $mummies, Ổ: $litterWeightKg kg) -> Sinh đàn con $pigletBatchTag$noteDetail",
                metrics = "$bornAlive con sống",
                recordedByUserId = recordedBy,
                syncStatus = SyncStatus.PENDING_SYNC
            )
        )

        return pigletBatchTag
    }

    // =========================================================================
    // 2. SỰ KIỆN: TIÊM SẮT HEO CON
    // =========================================================================
    suspend fun onIronInjectionEvent(
        pigletTag: String,
        injectionDate: Long,
        quantityInjected: Int,
        drugName: String = "Iron Dextran",
        dosageMl: Float = 1.0f,
        recordedBy: String = "Nguyễn Văn Tuấn"
    ) {
        val piglet = pigDao.getPigByTagCode(pigletTag)
        val pigId = piglet?.id ?: pigletTag

        // Ghi nhận sự kiện thú y
        eventDao.insertEvent(
            EventEntity(
                id = UUID.randomUUID().toString(),
                pigId = pigId,
                eventType = "Tiêm sắt heo con",
                eventDate = injectionDate,
                description = "Đã tiêm sắt cho $quantityInjected con đàn $pigletTag bằng $drugName liều $dosageMl ml/con",
                metrics = "$drugName $dosageMl ml",
                recordedByUserId = recordedBy,
                syncStatus = SyncStatus.PENDING_SYNC
            )
        )
    }

    // =========================================================================
    // 3. SỰ KIỆN: CAI SỮA (RẤT QUAN TRỌNG)
    // =========================================================================
    suspend fun onWeaningEvent(
        pigletTag: String,
        weaningDate: Long,
        weanedQuantity: Int,
        averageWeightKg: Float,
        sowTag: String? = null,
        newLocation: String = "Khu Cai Sữa A-01",
        recordedBy: String = "Nguyễn Văn Tuấn"
    ): String {
        // 3.1 Đóng chu kỳ đàn con theo mẹ cũ
        val piglet = pigDao.getPigByTagCode(pigletTag)
        if (piglet != null) {
            pigDao.deletePig(piglet)
        }

        // 3.2 Tự động sinh mã đàn con theo công thức: "Heo con " + Mã Nái mẹ + " " + dd/mm/yy
        val weanDateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        val dateStr = weanDateFormat.format(Date(weaningDate))
        val postWeanTag = if (!sowTag.isNullOrBlank()) "Heo con $sowTag $dateStr" else "CS${dateFormat.format(Date(weaningDate))}-${(100..999).random()}"
        val postWeanId = "weaned-${UUID.randomUUID()}"

        val weanedBatch = PigEntity(
            id = postWeanId,
            tagCode = postWeanTag,
            birthDate = piglet?.birthDate ?: (weaningDate - 28 * dayMs),
            isBatch = true,
            quantity = weanedQuantity,
            breed = piglet?.breed ?: "F1 Land-York",
            gender = "Hỗn hợp",
            status = SowReproductiveStatus.DANG_NUOI,
            location = newLocation,
            currentWeightKg = averageWeightKg,
            syncStatus = SyncStatus.PENDING_SYNC,
            updatedAt = System.currentTimeMillis()
        )
        pigDao.insertPig(weanedBatch)

        // 3.3 Cập nhật Nái mẹ: chuyển sang CAI_SUA_CHO_PHOI
        if (!sowTag.isNullOrBlank()) {
            val sow = pigDao.getPigByTagCode(sowTag)
            if (sow != null) {
                pigDao.updatePig(
                    sow.copy(
                        status = SowReproductiveStatus.CAI_SUA_CHO_PHOI,
                        hasDelayedHeatWarning = false,
                        syncStatus = SyncStatus.PENDING_SYNC,
                        updatedAt = System.currentTimeMillis()
                    )
                )

                // Rule: Sinh công việc theo dõi lên giống nái mẹ (D+4 đến D+7)
                taskDao.insertTask(
                    TaskEntity(
                        id = UUID.randomUUID().toString(),
                        title = "Theo dõi lên giống lại nái $sowTag",
                        taskType = TaskType.CHECK_HEAT_1,
                        dueDate = weaningDate + (5 * dayMs),
                        timeLabel = "07:30",
                        assignedTo = recordedBy,
                        status = TaskStatus.TODO,
                        priority = TaskPriority.HIGH,
                        referenceId = sowTag,
                        locationInfo = sow.location,
                        note = "Nái sau cai sữa 4-7 ngày bắt đầu chu kỳ động dục mới",
                        syncStatus = SyncStatus.PENDING_SYNC
                    )
                )
            }
        }

        // 3.4 Rule: Sinh công việc cho đàn heo sau cai sữa
        taskDao.insertTask(
            TaskEntity(
                id = UUID.randomUUID().toString(),
                title = "Vắc-xin phòng tiêu chảy & sưng phù đầu - Đàn $postWeanTag",
                taskType = TaskType.VACCINE,
                dueDate = weaningDate + (4 * dayMs),
                timeLabel = "08:30",
                assignedTo = recordedBy,
                status = TaskStatus.TODO,
                priority = TaskPriority.NORMAL,
                referenceId = postWeanTag,
                locationInfo = newLocation,
                note = "Ổn định tiêu hóa sau tách mẹ cho $weanedQuantity con",
                syncStatus = SyncStatus.PENDING_SYNC
            )
        )

        // 3.5 Lưu sự kiện
        eventDao.insertEvent(
            EventEntity(
                id = UUID.randomUUID().toString(),
                pigId = postWeanId,
                eventType = "Cai sữa",
                eventDate = weaningDate,
                description = "Cai sữa đàn $pigletTag -> Tạo đàn mới $postWeanTag ($weanedQuantity con, TL bình quân: $averageWeightKg kg)",
                metrics = "$weanedQuantity con cai sữa",
                recordedByUserId = recordedBy,
                syncStatus = SyncStatus.PENDING_SYNC
            )
        )

        return postWeanTag
    }

    // =========================================================================
    // 4. SỰ KIỆN: HEO THỊT
    // =========================================================================

    // 4.1 Nhập lô heo thịt
    suspend fun onBatchIntakeEvent(
        batchCode: String,
        intakeDate: Long,
        quantity: Int,
        averageWeightKg: Float,
        location: String,
        breed: String = "Duroc x Landrace",
        recordedBy: String = "Nguyễn Văn Tuấn"
    ): String {
        val batchId = "meat-${UUID.randomUUID()}"
        val pigBatch = PigEntity(
            id = batchId,
            tagCode = batchCode.trim().uppercase(),
            birthDate = intakeDate - (60 * dayMs),
            isBatch = true,
            quantity = quantity,
            breed = breed,
            gender = "Hỗn hợp",
            status = SowReproductiveStatus.DANG_NUOI,
            location = location,
            currentWeightKg = averageWeightKg,
            syncStatus = SyncStatus.PENDING_SYNC,
            updatedAt = System.currentTimeMillis()
        )
        pigDao.insertPig(pigBatch)

        // Rule Engine tự sinh chuỗi 5 việc heo thịt
        val meatTasks = listOf(
            TaskEntity(
                id = UUID.randomUUID().toString(),
                title = "Cân lần 1 - Lô $batchCode",
                taskType = TaskType.WEIGHT_CHECK,
                dueDate = intakeDate + (15 * dayMs),
                timeLabel = "08:00",
                assignedTo = recordedBy,
                status = TaskStatus.TODO,
                priority = TaskPriority.NORMAL,
                referenceId = batchCode,
                locationInfo = location,
                note = "Đánh giá tăng trọng 15 ngày đầu nhập trại",
                syncStatus = SyncStatus.PENDING_SYNC
            ),
            TaskEntity(
                id = UUID.randomUUID().toString(),
                title = "Tiêm vắc-xin Dịch tả nhắc lại - Lô $batchCode",
                taskType = TaskType.VACCINE,
                dueDate = intakeDate + (7 * dayMs),
                timeLabel = "08:30",
                assignedTo = recordedBy,
                status = TaskStatus.TODO,
                priority = TaskPriority.HIGH,
                referenceId = batchCode,
                locationInfo = location,
                note = "Tiêm phòng dịch tả bắt buộc cho toàn lô",
                syncStatus = SyncStatus.PENDING_SYNC
            ),
            TaskEntity(
                id = UUID.randomUUID().toString(),
                title = "Tiêm vắc-xin Tai xanh (PRRS) - Lô $batchCode",
                taskType = TaskType.VACCINE,
                dueDate = intakeDate + (21 * dayMs),
                timeLabel = "08:30",
                assignedTo = recordedBy,
                status = TaskStatus.TODO,
                priority = TaskPriority.HIGH,
                referenceId = batchCode,
                locationInfo = location,
                note = "Phòng hội chứng hô hấp",
                syncStatus = SyncStatus.PENDING_SYNC
            ),
            TaskEntity(
                id = UUID.randomUUID().toString(),
                title = "Tẩy giun định kỳ - Lô $batchCode",
                taskType = TaskType.GENERAL,
                dueDate = intakeDate + (30 * dayMs),
                timeLabel = "09:00",
                assignedTo = recordedBy,
                status = TaskStatus.TODO,
                priority = TaskPriority.NORMAL,
                referenceId = batchCode,
                locationInfo = location,
                note = "Trộn thuốc tẩy giun vào thức ăn",
                syncStatus = SyncStatus.PENDING_SYNC
            ),
            TaskEntity(
                id = UUID.randomUUID().toString(),
                title = "Dự kiến xuất bán Lô $batchCode (Mục tiêu 110kg)",
                taskType = TaskType.WEANING_PLAN,
                dueDate = intakeDate + (100 * dayMs),
                timeLabel = "08:00",
                assignedTo = recordedBy,
                status = TaskStatus.TODO,
                priority = TaskPriority.HIGH,
                referenceId = batchCode,
                locationInfo = location,
                note = "Liên hệ thương lái chuẩn bị xuất chuồng",
                syncStatus = SyncStatus.PENDING_SYNC
            )
        )
        taskDao.insertTasks(meatTasks)

        eventDao.insertEvent(
            EventEntity(
                id = UUID.randomUUID().toString(),
                pigId = batchId,
                eventType = "Nhập đàn",
                eventDate = intakeDate,
                description = "Nhập mới lô heo thịt $batchCode ($quantity con, TL bình quân: $averageWeightKg kg) tại $location",
                metrics = "$quantity con",
                recordedByUserId = recordedBy,
                syncStatus = SyncStatus.PENDING_SYNC
            )
        )

        return batchCode
    }

    // 4.2 Cân trọng lượng & Tính ADG, dự báo xuất bán
    suspend fun onWeighingEvent(
        batchCode: String,
        weighDate: Long,
        headCount: Int,
        averageWeightKg: Float,
        recordedBy: String = "Nguyễn Văn Tuấn"
    ): Triple<Float, Float, Int> { // Returns: Triple(adgKgPerDay, totalGainKg, estimatedDaysToMarket)
        val batch = pigDao.getPigByTagCode(batchCode)
        val oldWeight = batch?.currentWeightKg ?: 25.0f
        val daysElapsed = if (batch != null) {
            ((weighDate - batch.updatedAt) / dayMs).coerceAtLeast(1)
        } else 14L

        val weightGain = (averageWeightKg - oldWeight).coerceAtLeast(0f)
        val adg = (weightGain / daysElapsed.toFloat()) // kg / con / ngày
        val daysToMarket = if (adg > 0.1f) {
            ((110f - averageWeightKg) / adg).toInt().coerceAtLeast(0)
        } else 45

        if (batch != null) {
            pigDao.updatePig(
                batch.copy(
                    currentWeightKg = averageWeightKg,
                    quantity = headCount,
                    syncStatus = SyncStatus.PENDING_SYNC,
                    updatedAt = weighDate
                )
            )
        }

        eventDao.insertEvent(
            EventEntity(
                id = UUID.randomUUID().toString(),
                pigId = batch?.id ?: batchCode,
                eventType = "Cân trọng lượng",
                eventDate = weighDate,
                description = "Cân lô $batchCode ($headCount con): TL bình quân $averageWeightKg kg. ADG: ${String.format(Locale.US, "%.2f", adg)} kg/ngày, Tăng trọng: $weightGain kg. Dự báo xuất bán sau $daysToMarket ngày.",
                metrics = "ADG ${String.format(Locale.US, "%.2f", adg)} kg/ngày",
                recordedByUserId = recordedBy,
                syncStatus = SyncStatus.PENDING_SYNC
            )
        )

        return Triple(adg, weightGain, daysToMarket)
    }

    // 4.3 Điều trị bệnh
    suspend fun onTreatmentEvent(
        batchCode: String,
        treatmentDate: Long,
        sickCount: Int,
        diseaseName: String,
        medicineName: String,
        notes: String = "",
        recordedBy: String = "Nguyễn Văn Tuấn"
    ) {
        val batch = pigDao.getPigByTagCode(batchCode)
        val batchId = batch?.id ?: batchCode

        eventDao.insertEvent(
            EventEntity(
                id = UUID.randomUUID().toString(),
                pigId = batchId,
                eventType = "Điều trị bệnh",
                eventDate = treatmentDate,
                description = "Điều trị cho $sickCount con bị $diseaseName bằng thuốc $medicineName. $notes",
                metrics = "$sickCount con bệnh",
                recordedByUserId = recordedBy,
                syncStatus = SyncStatus.PENDING_SYNC
            )
        )
    }

    // 4.4 Xuất bán lô heo
    suspend fun onBatchSaleEvent(
        batchCode: String,
        saleDate: Long,
        soldQuantity: Int,
        averageWeightKg: Float,
        customerName: String,
        salePriceVnd: String = "",
        recordedBy: String = "Nguyễn Văn Tuấn"
    ) {
        val batch = pigDao.getPigByTagCode(batchCode)
        if (batch != null) {
            // Chuyển trạng thái sang LOAI_THAI (Đã xuất bán)
            pigDao.updatePig(
                batch.copy(
                    status = SowReproductiveStatus.LOAI_THAI,
                    currentWeightKg = averageWeightKg,
                    quantity = soldQuantity,
                    syncStatus = SyncStatus.PENDING_SYNC,
                    updatedAt = saleDate
                )
            )
        }

        val priceDesc = if (salePriceVnd.isNotBlank()) " • Giá bán: $salePriceVnd" else ""
        eventDao.insertEvent(
            EventEntity(
                id = UUID.randomUUID().toString(),
                pigId = batch?.id ?: batchCode,
                eventType = "Xuất bán",
                eventDate = saleDate,
                description = "Đã xuất bán lô $batchCode: $soldQuantity con (TL bình quân: $averageWeightKg kg) cho khách hàng: $customerName$priceDesc.",
                metrics = "$soldQuantity con xuất bán",
                recordedByUserId = recordedBy,
                syncStatus = SyncStatus.PENDING_SYNC
            )
        )
    }

    // =========================================================================
    // 5. SỰ KIỆN: LOẠI THẢI (HEO NÁI, HEO CON, HEO THỊT)
    // =========================================================================
    suspend fun onSowCullEvent(
        sowTag: String,
        cullDate: Long,
        reason: String,
        weightKg: Float,
        salePrice: String,
        buyerName: String,
        recordedBy: String = "Nguyễn Văn Tuấn"
    ) {
        val sow = pigDao.getPigByTagCode(sowTag)
        if (sow != null) {
            pigDao.updatePig(
                sow.copy(
                    status = SowReproductiveStatus.LOAI_THAI,
                    currentWeightKg = weightKg,
                    hasOverdueVaccine = false,
                    hasUltrasoundWarning = false,
                    hasDelayedHeatWarning = false,
                    hasProlongedGestationWarning = false,
                    syncStatus = SyncStatus.PENDING_SYNC,
                    updatedAt = cullDate
                )
            )
        }

        eventDao.insertEvent(
            EventEntity(
                id = UUID.randomUUID().toString(),
                pigId = sow?.id ?: sowTag,
                eventType = "Loại thải nái",
                eventDate = cullDate,
                description = "Loại thải nái $sowTag (Lứa ${sow?.parity ?: 0}) - Lý do: $reason. TL: $weightKg kg, Giá bán: $salePrice đ, Người mua: $buyerName.",
                metrics = "Loại thải ($reason)",
                recordedByUserId = recordedBy,
                syncStatus = SyncStatus.PENDING_SYNC
            )
        )
    }

    suspend fun onPigletCullEvent(
        pigletTag: String,
        cullDate: Long,
        cullQuantity: Int,
        reason: String,
        averageWeightKg: Float = 0f,
        disposition: String = "Tiêu hủy",
        recordedBy: String = "Nguyễn Văn Tuấn"
    ) {
        val piglet = pigDao.getPigByTagCode(pigletTag)
        var remaining = 0
        if (piglet != null) {
            remaining = (piglet.quantity - cullQuantity).coerceAtLeast(0)
            if (remaining == 0) {
                pigDao.deletePig(piglet)
            } else {
                pigDao.updatePig(
                    piglet.copy(
                        quantity = remaining,
                        syncStatus = SyncStatus.PENDING_SYNC,
                        updatedAt = cullDate
                    )
                )
            }
        }

        eventDao.insertEvent(
            EventEntity(
                id = UUID.randomUUID().toString(),
                pigId = piglet?.id ?: pigletTag,
                eventType = "Loại thải heo con",
                eventDate = cullDate,
                description = "Loại thải $cullQuantity con đàn $pigletTag - Lý do: $reason ($disposition). TL: $averageWeightKg kg/con. Còn lại: $remaining con.",
                metrics = "$cullQuantity con ($reason)",
                recordedByUserId = recordedBy,
                syncStatus = SyncStatus.PENDING_SYNC
            )
        )
    }

    suspend fun onGrowerCullEvent(
        batchCode: String,
        cullDate: Long,
        cullQuantity: Int,
        reason: String,
        averageWeightKg: Float,
        salePrice: String = "",
        buyerName: String = "",
        recordedBy: String = "Nguyễn Văn Tuấn"
    ) {
        val batch = pigDao.getPigByTagCode(batchCode)
        var remaining = 0
        if (batch != null) {
            remaining = (batch.quantity - cullQuantity).coerceAtLeast(0)
            if (remaining == 0) {
                pigDao.updatePig(
                    batch.copy(
                        status = SowReproductiveStatus.LOAI_THAI,
                        quantity = 0,
                        syncStatus = SyncStatus.PENDING_SYNC,
                        updatedAt = cullDate
                    )
                )
            } else {
                pigDao.updatePig(
                    batch.copy(
                        quantity = remaining,
                        syncStatus = SyncStatus.PENDING_SYNC,
                        updatedAt = cullDate
                    )
                )
            }
        }

        val saleInfo = if (salePrice.isNotBlank()) " | Giá: $salePrice đ ($buyerName)" else " | Xử lý tiêu hủy"
        eventDao.insertEvent(
            EventEntity(
                id = UUID.randomUUID().toString(),
                pigId = batch?.id ?: batchCode,
                eventType = "Loại thải heo thịt",
                eventDate = cullDate,
                description = "Loại thải $cullQuantity con lô $batchCode - Lý do: $reason. TL bình quân: $averageWeightKg kg$saleInfo. Số lượng còn lại: $remaining con.",
                metrics = "$cullQuantity con ($reason)",
                recordedByUserId = recordedBy,
                syncStatus = SyncStatus.PENDING_SYNC
            )
        )
    }

    // =========================================================================
    // 6. SỰ KIỆN: PHỐI GIỐNG (HEO NÁI)
    // =========================================================================
    suspend fun onMatingEvent(
        sowTag: String,
        matingDate: Long,
        boarTag: String,
        technician: String = "Nguyễn Văn Tuấn",
        notes: String = ""
    ) {
        val sow = pigDao.getPigByTagCode(sowTag)
        if (sow != null) {
            pigDao.updatePig(
                sow.copy(
                    status = SowReproductiveStatus.MANG_THAI,
                    lastMatingDate = matingDate,
                    hasDelayedHeatWarning = false,
                    hasUltrasoundWarning = false,
                    syncStatus = SyncStatus.PENDING_SYNC,
                    updatedAt = matingDate
                )
            )
        }

        // Tự động sinh chuỗi công việc theo dõi thai kỳ
        val matingTasks = listOf(
            TaskEntity(
                id = UUID.randomUUID().toString(),
                title = "Kiểm tra lên giống lốc 1 (D+18) - Nái $sowTag",
                taskType = TaskType.CHECK_HEAT_1,
                dueDate = matingDate + (18 * dayMs),
                timeLabel = "07:30",
                assignedTo = technician,
                status = TaskStatus.TODO,
                priority = TaskPriority.HIGH,
                referenceId = sowTag,
                locationInfo = sow?.location ?: "Chuồng mang thai",
                note = "Quan sát âm hộ, phản xạ đứng im khi nọc đi qua",
                syncStatus = SyncStatus.PENDING_SYNC
            ),
            TaskEntity(
                id = UUID.randomUUID().toString(),
                title = "Kiểm tra lên giống lốc 2 (D+21) - Nái $sowTag",
                taskType = TaskType.CHECK_HEAT_2,
                dueDate = matingDate + (21 * dayMs),
                timeLabel = "07:30",
                assignedTo = technician,
                status = TaskStatus.TODO,
                priority = TaskPriority.HIGH,
                referenceId = sowTag,
                locationInfo = sow?.location ?: "Chuồng mang thai",
                note = "Xác nhận không lốc sảy chu kỳ 21 ngày",
                syncStatus = SyncStatus.PENDING_SYNC
            ),
            TaskEntity(
                id = UUID.randomUUID().toString(),
                title = "Siêu âm thai D+28 đến D+30 - Nái $sowTag",
                taskType = TaskType.ULTRASOUND,
                dueDate = matingDate + (28 * dayMs),
                timeLabel = "08:30",
                assignedTo = technician,
                status = TaskStatus.TODO,
                priority = TaskPriority.HIGH,
                referenceId = sowTag,
                locationInfo = sow?.location ?: "Chuồng mang thai",
                note = "Khám thai bằng máy siêu âm, xác định túi phôi",
                syncStatus = SyncStatus.PENDING_SYNC
            ),
            TaskEntity(
                id = UUID.randomUUID().toString(),
                title = "Chuyển chuồng đẻ (D+107) - Nái $sowTag",
                taskType = TaskType.FARROWING_TRANSFER,
                dueDate = matingDate + (107 * dayMs),
                timeLabel = "09:00",
                assignedTo = technician,
                status = TaskStatus.TODO,
                priority = TaskPriority.HIGH,
                referenceId = sowTag,
                locationInfo = sow?.location ?: "Chuồng mang thai",
                note = "Tắm sát trùng nái và đưa lên chuồng đẻ sạch",
                syncStatus = SyncStatus.PENDING_SYNC
            ),
            TaskEntity(
                id = UUID.randomUUID().toString(),
                title = "Dự kiến đẻ (D+114) - Nái $sowTag",
                taskType = TaskType.FARROWING_MONITOR,
                dueDate = matingDate + (114 * dayMs),
                timeLabel = "00:00",
                assignedTo = technician,
                status = TaskStatus.TODO,
                priority = TaskPriority.HIGH,
                referenceId = sowTag,
                locationInfo = "Chuồng đẻ",
                note = "Mốc 114 ngày chuẩn. Bật đèn úm, chuẩn bị dụng cụ đỡ đẻ",
                syncStatus = SyncStatus.PENDING_SYNC
            )
        )
        taskDao.insertTasks(matingTasks)

        val noteDetail = if (notes.isNotBlank()) " | Ghi chú: $notes" else ""
        eventDao.insertEvent(
            EventEntity(
                id = UUID.randomUUID().toString(),
                pigId = sow?.id ?: sowTag,
                eventType = "Phối giống",
                eventDate = matingDate,
                description = "Phối giống nái $sowTag với nọc $boarTag bởi $technician$noteDetail",
                metrics = "Nọc: $boarTag",
                recordedByUserId = technician,
                syncStatus = SyncStatus.PENDING_SYNC
            )
        )
    }

    // =========================================================================
    // 7. SỰ KIỆN: VACCINE & CẮT ĐUÔI HEO CON
    // =========================================================================
    suspend fun onPigletVaccineEvent(
        pigletTag: String,
        vaccineDate: Long,
        quantity: Int,
        vaccineName: String,
        dosage: String = "2ml/con",
        recordedBy: String = "Nguyễn Văn Tuấn"
    ) {
        val piglet = pigDao.getPigByTagCode(pigletTag)
        eventDao.insertEvent(
            EventEntity(
                id = UUID.randomUUID().toString(),
                pigId = piglet?.id ?: pigletTag,
                eventType = "Tiêm vắc-xin heo con",
                eventDate = vaccineDate,
                description = "Tiêm vắc-xin $vaccineName cho $quantity con đàn $pigletTag (Liều: $dosage)",
                metrics = "$vaccineName $dosage",
                recordedByUserId = recordedBy,
                syncStatus = SyncStatus.PENDING_SYNC
            )
        )
    }

    suspend fun onTailDockingEvent(
        pigletTag: String,
        dockDate: Long,
        quantity: Int,
        notes: String = "",
        recordedBy: String = "Nguyễn Văn Tuấn"
    ) {
        val piglet = pigDao.getPigByTagCode(pigletTag)
        eventDao.insertEvent(
            EventEntity(
                id = UUID.randomUUID().toString(),
                pigId = piglet?.id ?: pigletTag,
                eventType = "Cắt đuôi & bấm nanh",
                eventDate = dockDate,
                description = "Cắt đuôi, bấm nanh cho $quantity con đàn $pigletTag. $notes",
                metrics = "$quantity con",
                recordedByUserId = recordedBy,
                syncStatus = SyncStatus.PENDING_SYNC
            )
        )
    }

    // =========================================================================
    // 8. SỰ KIỆN: CHUYỂN CHUỒNG HEO THỊT
    // =========================================================================
    suspend fun onRelocationEvent(
        batchCode: String,
        moveDate: Long,
        newLocation: String,
        notes: String = "",
        recordedBy: String = "Nguyễn Văn Tuấn"
    ) {
        val batch = pigDao.getPigByTagCode(batchCode)
        if (batch != null) {
            pigDao.updatePig(
                batch.copy(
                    location = newLocation,
                    syncStatus = SyncStatus.PENDING_SYNC,
                    updatedAt = moveDate
                )
            )
        }
        eventDao.insertEvent(
            EventEntity(
                id = UUID.randomUUID().toString(),
                pigId = batch?.id ?: batchCode,
                eventType = "Chuyển chuồng",
                eventDate = moveDate,
                description = "Chuyển lô $batchCode sang $newLocation. $notes",
                metrics = newLocation,
                recordedByUserId = recordedBy,
                syncStatus = SyncStatus.PENDING_SYNC
            )
        )
    }

    // =========================================================================
    // 9. SỰ KIỆN: KHÁM THAI (LUỒNG 2 XÁC NHẬN ĐẬU THAI)
    // =========================================================================
    suspend fun onPregnancyCheckEvent(
        sowTag: String,
        checkDate: Long,
        isPregnant: Boolean,
        recordedBy: String = "Nguyễn Văn Tuấn"
    ) {
        onPregnancyCheckResultEvent(
            sowTag = sowTag,
            checkDate = checkDate,
            result = if (isPregnant) "PREGNANT" else "NOT_PREGNANT",
            recordedBy = recordedBy
        )
    }

    suspend fun onPregnancyCheckResultEvent(
        sowTag: String,
        checkDate: Long,
        result: String, // "PREGNANT" (Đậu thai +), "NOT_PREGNANT" (Không đậu -), "DOUBTFUL" (Nghi ngờ ?)
        recordedBy: String = "Nguyễn Văn Tuấn"
    ) {
        val sow = pigDao.getPigByTagCode(sowTag) ?: return

        when (result) {
            "PREGNANT" -> {
                // Nái đậu thai (+) -> Giữ nguyên/Cập nhật MANG_THAI
                pigDao.updatePig(
                    sow.copy(
                        status = SowReproductiveStatus.MANG_THAI,
                        hasUltrasoundWarning = false,
                        syncStatus = SyncStatus.PENDING_SYNC,
                        updatedAt = checkDate
                    )
                )

                // Tự động kích hoạt mốc công việc chuẩn bị chuồng đẻ (D+107) và theo dõi đẻ (D+114)
                val matingMs = sow.lastMatingDate ?: (checkDate - (30 * dayMs))
                taskDao.insertTasks(
                    listOf(
                        TaskEntity(
                            id = UUID.randomUUID().toString(),
                            title = "Chuẩn bị chuồng đẻ & Vệ sinh nái $sowTag (Mốc D+107)",
                            taskType = TaskType.FARROWING_TRANSFER,
                            dueDate = matingMs + (107 * dayMs),
                            timeLabel = "14:00",
                            assignedTo = recordedBy,
                            status = TaskStatus.TODO,
                            priority = TaskPriority.HIGH,
                            referenceId = sowTag,
                            locationInfo = sow.location,
                            note = "Tắm sát trùng nái, quét vôi tiêu độc chuồng đẻ trước khi nái vào",
                            syncStatus = SyncStatus.PENDING_SYNC
                        ),
                        TaskEntity(
                            id = UUID.randomUUID().toString(),
                            title = "Theo dõi trực đẻ nái $sowTag (Dự sinh D+114)",
                            taskType = TaskType.FARROWING_MONITOR,
                            dueDate = matingMs + (114 * dayMs),
                            timeLabel = "00:00",
                            assignedTo = recordedBy,
                            status = TaskStatus.TODO,
                            priority = TaskPriority.HIGH,
                            referenceId = sowTag,
                            locationInfo = sow.location,
                            note = "Trực đẻ, chuẩn bị lồng úm nhiệt độ 32-34 độ C, kẹp rốn, lau nhớt",
                            syncStatus = SyncStatus.PENDING_SYNC
                        )
                    )
                )

                eventDao.insertEvent(
                    EventEntity(
                        id = UUID.randomUUID().toString(),
                        pigId = sow.id,
                        eventType = "Khám thai",
                        eventDate = checkDate,
                        description = "Khám thai nái $sowTag: Đậu thai (+) • Xác nhận thai phát triển bình thường",
                        metrics = "Đậu thai (+)",
                        recordedByUserId = recordedBy,
                        syncStatus = SyncStatus.PENDING_SYNC
                    )
                )
            }
            "NOT_PREGNANT" -> {
                // Không đậu thai (-) -> Tự động chuyển nái về CAI_SUA_CHO_PHOI (chờ phối lại)
                pigDao.updatePig(
                    sow.copy(
                        status = SowReproductiveStatus.CAI_SUA_CHO_PHOI,
                        hasUltrasoundWarning = false,
                        hasDelayedHeatWarning = true,
                        syncStatus = SyncStatus.PENDING_SYNC,
                        updatedAt = checkDate
                    )
                )

                // Cảnh báo lên bảng điều khiển và sinh việc kiểm tra lên giống lại
                taskDao.insertTask(
                    TaskEntity(
                        id = UUID.randomUUID().toString(),
                        title = "Theo dõi lên giống lại nái $sowTag (Lốc sảy / Không đậu)",
                        taskType = TaskType.CHECK_HEAT_1,
                        dueDate = checkDate + (3 * dayMs),
                        timeLabel = "08:00",
                        assignedTo = recordedBy,
                        status = TaskStatus.TODO,
                        priority = TaskPriority.HIGH,
                        referenceId = sowTag,
                        locationInfo = sow.location,
                        note = "Nái không đậu thai, kiểm tra âm hộ chảy dịch, kích dục bằng đực nọc để phối lại",
                        syncStatus = SyncStatus.PENDING_SYNC
                    )
                )

                eventDao.insertEvent(
                    EventEntity(
                        id = UUID.randomUUID().toString(),
                        pigId = sow.id,
                        eventType = "Khám thai",
                        eventDate = checkDate,
                        description = "Khám thai nái $sowTag: Không đậu (-) • Chuyển trạng thái chờ phối lại",
                        metrics = "Không đậu (-)",
                        recordedByUserId = recordedBy,
                        syncStatus = SyncStatus.PENDING_SYNC
                    )
                )
            }
            "DOUBTFUL" -> {
                // Nghi ngờ (?) -> Đặt lịch siêu âm/kiểm tra lại sau 7 ngày
                taskDao.insertTask(
                    TaskEntity(
                        id = UUID.randomUUID().toString(),
                        title = "Siêu âm thai kiểm tra lại nái $sowTag (Nghi ngờ)",
                        taskType = TaskType.ULTRASOUND,
                        dueDate = checkDate + (7 * dayMs),
                        timeLabel = "09:00",
                        assignedTo = recordedBy,
                        status = TaskStatus.TODO,
                        priority = TaskPriority.NORMAL,
                        referenceId = sowTag,
                        locationInfo = sow.location,
                        note = "Kết quả siêu âm trước chưa rõ túi thai, kiểm tra lại sau 7 ngày",
                        syncStatus = SyncStatus.PENDING_SYNC
                    )
                )

                eventDao.insertEvent(
                    EventEntity(
                        id = UUID.randomUUID().toString(),
                        pigId = sow.id,
                        eventType = "Khám thai",
                        eventDate = checkDate,
                        description = "Khám thai nái $sowTag: Nghi ngờ (?) • Đã lên lịch siêu âm lại sau 7 ngày",
                        metrics = "Nghi ngờ (?)",
                        recordedByUserId = recordedBy,
                        syncStatus = SyncStatus.PENDING_SYNC
                    )
                )
            }
        }
    }

    // =========================================================================
    // 10. SỰ KIỆN: CHUYỂN CHUỒNG HEO NÁI
    // =========================================================================
    suspend fun onSowRelocationEvent(
        sowTag: String,
        moveDate: Long,
        fromLocation: String,
        toLocation: String,
        notes: String = "",
        recordedBy: String = "Nguyễn Văn Tuấn"
    ) {
        val sow = pigDao.getPigByTagCode(sowTag) ?: return

        pigDao.updatePig(
            sow.copy(
                location = toLocation,
                syncStatus = SyncStatus.PENDING_SYNC,
                updatedAt = moveDate
            )
        )

        eventDao.insertEvent(
            EventEntity(
                id = UUID.randomUUID().toString(),
                pigId = sow.id,
                eventType = "Chuyển chuồng nái",
                eventDate = moveDate,
                description = "Chuyển nái $sowTag từ $fromLocation sang $toLocation. ${notes.ifBlank { "Di chuyển chuồng an toàn" }}",
                metrics = "$fromLocation -> $toLocation",
                recordedByUserId = recordedBy,
                syncStatus = SyncStatus.PENDING_SYNC
            )
        )
    }

    // =========================================================================
    // 11. SỰ KIỆN: CHUYỂN CHUỒNG HEO THỊT
    // =========================================================================
    suspend fun onGrowerRelocationEvent(
        fromPen: String,
        toPen: String,
        moveDate: Long,
        quantity: Int,
        totalWeightKg: Float,
        avgAgeDays: Int,
        batchCode: String? = null,
        notes: String = "",
        recordedBy: String = "Nguyễn Văn Tuấn"
    ) {
        val batch = if (!batchCode.isNullOrBlank()) {
            pigDao.getPigByTagCode(batchCode)
        } else {
            pigDao.getAllPigs().first().firstOrNull { it.isBatch && it.location == fromPen }
        }

        if (batch != null) {
            val avgWeight = if (quantity > 0) totalWeightKg / quantity else batch.currentWeightKg
            pigDao.updatePig(
                batch.copy(
                    location = toPen,
                    quantity = quantity,
                    currentWeightKg = avgWeight,
                    syncStatus = SyncStatus.PENDING_SYNC,
                    updatedAt = moveDate
                )
            )
        }

        eventDao.insertEvent(
            EventEntity(
                id = UUID.randomUUID().toString(),
                pigId = batch?.id ?: (batchCode ?: fromPen),
                eventType = "Chuyển chuồng thịt",
                eventDate = moveDate,
                description = "Chuyển $quantity con heo thịt từ $fromPen sang $toPen (TL tổng: $totalWeightKg kg, Tuổi TB: $avgAgeDays ngày). $notes",
                metrics = "$fromPen -> $toPen ($quantity con)",
                recordedByUserId = recordedBy,
                syncStatus = SyncStatus.PENDING_SYNC
            )
        )
    }

    // =========================================================================
    // 12. SỰ KIỆN: "+ NHẬP ĐÀN" HEO NÁI
    // =========================================================================
    suspend fun onNewSowIntakeEvent(
        tagCode: String,
        breed: String,
        birthDate: Long,
        parity: Int,
        location: String,
        notes: String = "",
        recordedBy: String = "Nguyễn Văn Tuấn"
    ): String {
        // Kiểm tra trùng lặp mã tai
        val existing = pigDao.getPigByTagCode(tagCode)
        if (existing != null) {
            throw IllegalArgumentException("Mã tai nái $tagCode đã tồn tại trong hệ thống!")
        }

        val newId = "sow-${UUID.randomUUID()}"
        val initialStatus = if (parity == 0) SowReproductiveStatus.HAU_BI else SowReproductiveStatus.CAI_SUA_CHO_PHOI

        val newSow = PigEntity(
            id = newId,
            tagCode = tagCode,
            birthDate = birthDate,
            isBatch = false,
            quantity = 1,
            breed = breed,
            gender = "Cái",
            status = initialStatus,
            location = location,
            parity = parity,
            currentWeightKg = if (parity == 0) 115f else 185f,
            syncStatus = SyncStatus.PENDING_SYNC,
            updatedAt = System.currentTimeMillis()
        )
        pigDao.insertPig(newSow)

        // Nếu là hậu bị (lứa 0), tự động sinh công việc theo dõi lên giống lứa đầu (khoảng 210-240 ngày tuổi)
        if (parity == 0) {
            val firstMatingDue = birthDate + (220 * dayMs)
            taskDao.insertTask(
                TaskEntity(
                    id = UUID.randomUUID().toString(),
                    title = "Kiểm tra động dục lứa đầu nái hậu bị $tagCode",
                    taskType = TaskType.CHECK_HEAT_1,
                    dueDate = if (firstMatingDue > System.currentTimeMillis()) firstMatingDue else (System.currentTimeMillis() + 7 * dayMs),
                    timeLabel = "08:00",
                    assignedTo = recordedBy,
                    status = TaskStatus.TODO,
                    priority = TaskPriority.NORMAL,
                    referenceId = tagCode,
                    locationInfo = location,
                    note = "Hậu bị đạt 7-8 tháng tuổi, thể trọng > 120kg đủ tiêu chuẩn phối lứa đầu",
                    syncStatus = SyncStatus.PENDING_SYNC
                )
            )
        }

        eventDao.insertEvent(
            EventEntity(
                id = UUID.randomUUID().toString(),
                pigId = newId,
                eventType = "Nhập đàn nái",
                eventDate = System.currentTimeMillis(),
                description = "Nhập mới nái $tagCode (Giống: $breed, Lứa: $parity, Vị trí: $location). $notes",
                metrics = "Nhập mới $tagCode",
                recordedByUserId = recordedBy,
                syncStatus = SyncStatus.PENDING_SYNC
            )
        )

        return newId
    }
}
