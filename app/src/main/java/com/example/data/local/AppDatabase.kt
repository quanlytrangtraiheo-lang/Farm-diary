package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.BreedingCycleDao
import com.example.data.local.dao.EventDao
import com.example.data.local.dao.InventoryDao
import com.example.data.local.dao.InventoryEntityDao
import com.example.data.local.dao.IssueDao
import com.example.data.local.dao.MasterItemDao
import com.example.data.local.dao.PigDao
import com.example.data.local.dao.ReceiptDao
import com.example.data.local.dao.StaffDao
import com.example.data.local.dao.SyncLogDao
import com.example.data.local.dao.SystemParameterDao
import com.example.data.local.dao.TaskDao
import com.example.data.local.entity.BreedingCycleEntity
import com.example.data.local.entity.EventEntity
import com.example.data.local.entity.InventoryEntity
import com.example.data.local.entity.InventoryItemEntity
import com.example.data.local.entity.InventoryTransactionEntity
import com.example.data.local.entity.IssueEntity
import com.example.data.local.entity.MasterItemEntity
import com.example.data.local.entity.PigEntity
import com.example.data.local.entity.ReceiptEntity
import com.example.data.local.entity.StaffEntity
import com.example.data.local.entity.SyncLogEntity
import com.example.data.local.entity.SystemParameterEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.model.SowReproductiveStatus
import com.example.data.model.SyncStatus
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus
import com.example.data.model.TaskType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        PigEntity::class,
        BreedingCycleEntity::class,
        EventEntity::class,
        TaskEntity::class,
        SyncLogEntity::class,
        SystemParameterEntity::class,
        InventoryItemEntity::class,
        InventoryTransactionEntity::class,
        StaffEntity::class,
        MasterItemEntity::class,
        ReceiptEntity::class,
        IssueEntity::class,
        InventoryEntity::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pigDao(): PigDao
    abstract fun breedingCycleDao(): BreedingCycleDao
    abstract fun eventDao(): EventDao
    abstract fun taskDao(): TaskDao
    abstract fun syncLogDao(): SyncLogDao
    abstract fun systemParameterDao(): SystemParameterDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun staffDao(): StaffDao
    abstract fun masterItemDao(): MasterItemDao
    abstract fun receiptDao(): ReceiptDao
    abstract fun issueDao(): IssueDao
    abstract fun inventoryEntityDao(): InventoryEntityDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pig_farm_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialFarmData(database)
                    }
                }
            }
        }

        suspend fun populateInitialFarmData(database: AppDatabase) {
            val now = System.currentTimeMillis()
            val dayMs = 86_400_000L

            // 1. Core Pigs / Batches
            val sowNy089Id = "sow-ny-089"
            val lotLh204Id = "lot-lh-204"
            val lotL105Id = "lot-l-105"
            val sowNy085Id = "sow-ny-085"
            val sowNy072Id = "sow-ny-072"
            val sowNy064Id = "sow-ny-064"

            // Mating date for NY-089 was 85 days ago
            val ny089MatingDate = now - (85 * dayMs)
            val ny089ExpectedFarrow = ny089MatingDate + (114 * dayMs)

            val pigs = listOf(
                PigEntity(
                    id = sowNy089Id,
                    tagCode = "Heo nái NY-089",
                    isBatch = false,
                    quantity = 1,
                    breed = "York-Land",
                    gender = "Cái",
                    status = SowReproductiveStatus.MANG_THAI,
                    location = "Chuồng: Phối 1 (Bầu 12)",
                    parity = 3,
                    currentCycleId = "cycle-ny-089-3",
                    currentWeightKg = 215f,
                    lastMatingDate = ny089MatingDate,
                    expectedFarrowingDate = ny089ExpectedFarrow,
                    hasOverdueVaccine = false,
                    syncStatus = SyncStatus.SYNCED,
                    updatedAt = now - 3600_000L
                ),
                PigEntity(
                    id = lotLh204Id,
                    tagCode = "Lô LH-204",
                    isBatch = true,
                    quantity = 120,
                    breed = "Landrace",
                    gender = "Hỗn hợp",
                    status = SowReproductiveStatus.DANG_NUOI,
                    location = "Cấp dưỡng 3",
                    currentWeightKg = 75.2f,
                    hasOverdueVaccine = true, // Quá hạn tiêm alert
                    syncStatus = SyncStatus.PENDING_SYNC, // Chờ đồng bộ
                    updatedAt = now - 1800_000L
                ),
                PigEntity(
                    id = lotL105Id,
                    tagCode = "Lô L-105",
                    isBatch = true,
                    quantity = 80,
                    breed = "Duroc",
                    gender = "Hỗn hợp",
                    status = SowReproductiveStatus.DANG_NUOI,
                    location = "Chuồng Thịt 2",
                    currentWeightKg = 92.4f,
                    hasOverdueVaccine = false,
                    syncStatus = SyncStatus.PENDING_SYNC, // Chờ đồng bộ
                    updatedAt = now - 900_000L
                ),
                PigEntity(
                    id = sowNy085Id,
                    tagCode = "Heo nái NY-085",
                    isBatch = false,
                    quantity = 1,
                    breed = "Landrace thuần",
                    gender = "Cái",
                    status = SowReproductiveStatus.MANG_THAI,
                    location = "Khu phối (Chuồng 2)",
                    parity = 2,
                    currentCycleId = "cycle-ny-085-2",
                    currentWeightKg = 198f,
                    lastMatingDate = now - (36 * dayMs),
                    expectedFarrowingDate = now + (78 * dayMs),
                    hasUltrasoundWarning = true, // Quá ngày siêu âm thai
                    syncStatus = SyncStatus.SYNC_FAILED, // Xung đột cần xử lý
                    conflictLog = "Xung đột lịch siêu âm thai giữa Quản lý và Kỹ thuật viên (Ngày 28 so với Ngày 35)",
                    updatedAt = now - 7200_000L
                ),
                PigEntity(
                    id = sowNy072Id,
                    tagCode = "Heo nái NY-072",
                    isBatch = false,
                    quantity = 1,
                    breed = "F1 Land-York",
                    gender = "Cái",
                    status = SowReproductiveStatus.NUOI_CON,
                    location = "Chuồng đẻ 4 (Ô 08)",
                    parity = 4,
                    currentWeightKg = 185f,
                    syncStatus = SyncStatus.SYNCED,
                    updatedAt = now - 86400_000L
                ),
                PigEntity(
                    id = sowNy064Id,
                    tagCode = "Heo nái NY-064",
                    isBatch = false,
                    quantity = 1,
                    breed = "Yorkshire",
                    gender = "Cái",
                    status = SowReproductiveStatus.CAI_SUA_CHO_PHOI,
                    location = "Cai sữa B",
                    parity = 2,
                    currentWeightKg = 175f,
                    hasDelayedHeatWarning = true, // Cai sữa quá 7 ngày chưa lên giống
                    syncStatus = SyncStatus.SYNCED,
                    updatedAt = now - 43200_000L
                ),
                // Batch for total count matching 1,250
                PigEntity(
                    id = "batch-meat-01",
                    tagCode = "Lô Thịt 01 - Trại B",
                    isBatch = true,
                    quantity = 520,
                    breed = "Duroc x Landrace",
                    gender = "Hỗn hợp",
                    status = SowReproductiveStatus.DANG_NUOI,
                    location = "Chuồng Thịt 1",
                    currentWeightKg = 68.5f,
                    syncStatus = SyncStatus.SYNCED
                ),
                PigEntity(
                    id = "batch-meat-02",
                    tagCode = "Lô Thịt 02 - Trại B",
                    isBatch = true,
                    quantity = 527,
                    breed = "Duroc x Yorkshire",
                    gender = "Hỗn hợp",
                    status = SowReproductiveStatus.DANG_NUOI,
                    location = "Chuồng Thịt 3",
                    currentWeightKg = 65.0f,
                    syncStatus = SyncStatus.SYNCED
                ),
                PigEntity(
                    id = "batch-piglets-ny072",
                    tagCode = "Heo con theo mẹ (Lô NY-072)",
                    isBatch = true,
                    quantity = 11,
                    breed = "F1 Land-York",
                    gender = "Hỗn hợp",
                    status = SowReproductiveStatus.NUOI_CON,
                    location = "Chuồng đẻ 4 (Ô 08)",
                    birthDate = now - (14 * dayMs),
                    currentWeightKg = 4.2f,
                    syncStatus = SyncStatus.SYNCED
                )
            )
            database.pigDao().insertPigs(pigs)

            // 2. Breeding cycles for NY-089 (Lứa 2 hoàn thành, Lứa 3 đang mang thai)
            val cycles = listOf(
                BreedingCycleEntity(
                    cycleId = "cycle-ny-089-2",
                    sowId = sowNy089Id,
                    parity = 2,
                    matingDate = now - (200 * dayMs),
                    boarCode = "Đực Duroc D-01",
                    ultrasoundResult = "Đạt (+)",
                    ultrasoundDate = now - (170 * dayMs),
                    expectedFarrowingDate = now - (86 * dayMs),
                    actualFarrowingDate = now - (86 * dayMs),
                    totalBorn = 13,
                    bornAlive = 12,
                    stillborn = 1,
                    mummies = 0,
                    averageBirthWeightKg = 1.45f,
                    weaningDate = now - (65 * dayMs),
                    pigletsWeaned = 12,
                    cycleStatus = "COMPLETED"
                ),
                BreedingCycleEntity(
                    cycleId = "cycle-ny-089-3",
                    sowId = sowNy089Id,
                    parity = 3,
                    matingDate = ny089MatingDate,
                    boarCode = "Đực Duroc (05/08/2026)",
                    ultrasoundResult = "Đạt (+)",
                    ultrasoundDate = ny089MatingDate + (30 * dayMs),
                    expectedFarrowingDate = ny089ExpectedFarrow,
                    actualFarrowingDate = null,
                    totalBorn = 0,
                    bornAlive = 0,
                    cycleStatus = "IN_PROGRESS"
                )
            )
            database.breedingCycleDao().insertCycles(cycles)

            // 3. Events
            val events = listOf(
                EventEntity(
                    id = UUID.randomUUID().toString(),
                    pigId = sowNy089Id,
                    eventType = "Phối giống",
                    eventDate = ny089MatingDate,
                    description = "Phối giống với Đực Duroc tuyển chọn",
                    recordedByUserId = "Nguyễn Văn Tuấn"
                ),
                EventEntity(
                    id = UUID.randomUUID().toString(),
                    pigId = sowNy089Id,
                    eventType = "Siêu âm thai",
                    eventDate = ny089MatingDate + (30 * dayMs),
                    description = "Kết quả siêu âm thai Đạt (+), phôi thai phát triển tốt",
                    recordedByUserId = "Trần Đình Khang"
                ),
                EventEntity(
                    id = UUID.randomUUID().toString(),
                    pigId = lotLh204Id,
                    eventType = "Cân trọng lượng",
                    eventDate = now - 7200_000L,
                    description = "Cân định kỳ Lô LH-204: Đạt 75.2 kg (+4.5 kg)",
                    metrics = "75.2"
                ),
                EventEntity(
                    id = UUID.randomUUID().toString(),
                    pigId = lotL105Id,
                    eventType = "Cân trọng lượng",
                    eventDate = now - 14400_000L,
                    description = "Cân định kỳ Lô L-105: Đạt 92.4 kg",
                    metrics = "92.4"
                )
            )
            database.eventDao().insertEvents(events)

            // 4. Tasks (Matching Mockup pages 6, 8, 10)
            val tasks = listOf(
                TaskEntity(
                    id = "task-1",
                    title = "Tiêm vắc-xin Dịch tả",
                    taskType = TaskType.VACCINE,
                    dueDate = now + 1800_000L,
                    timeLabel = "08:30",
                    assignedTo = "Kỹ thuật viên chuồng",
                    status = TaskStatus.TODO,
                    priority = TaskPriority.HIGH,
                    referenceId = "Lô LH-204",
                    locationInfo = "Chuồng 3",
                    note = "Tiêm mũi nhắc lại vắc-xin Dịch tả cổ điển"
                ),
                TaskEntity(
                    id = "task-2",
                    title = "Siêu âm thai nái",
                    taskType = TaskType.ULTRASOUND,
                    dueDate = now + 7200_000L,
                    timeLabel = "10:00",
                    assignedTo = "Nguyễn Văn Tuấn",
                    status = TaskStatus.IN_PROGRESS,
                    priority = TaskPriority.NORMAL,
                    referenceId = "Nái NY-089",
                    locationInfo = "Khu phối",
                    note = "Dùng máy siêu âm xách tay kiểm tra thai kỳ"
                ),
                TaskEntity(
                    id = "task-3",
                    title = "Vệ sinh sát trùng hành lang",
                    taskType = TaskType.CLEAN_DISINFECT,
                    dueDate = now + 21600_000L,
                    timeLabel = "14:00",
                    assignedTo = "Tổ vệ sinh trại",
                    status = TaskStatus.DONE,
                    priority = TaskPriority.NORMAL,
                    referenceId = "Toàn khu trại thịt",
                    locationInfo = "Chuồng Thịt 2",
                    note = "Phun Cloramin B sát trùng toàn bộ lối đi"
                ),
                TaskEntity(
                    id = "task-4",
                    title = "Kiểm tra lốc sảy chu kỳ 1",
                    taskType = TaskType.CHECK_HEAT_1,
                    dueDate = now + 86400_000L,
                    timeLabel = "09:00",
                    assignedTo = "Kỹ thuật viên chuồng",
                    status = TaskStatus.TODO,
                    priority = TaskPriority.NORMAL,
                    referenceId = "Nái NY-092",
                    locationInfo = "Chuồng Bầu 8",
                    note = "Quan sát dịch tiết âm hộ và kích thích lưng"
                ),
                TaskEntity(
                    id = "task-5",
                    title = "Chuyển chuồng đẻ & Vệ sinh nái",
                    taskType = TaskType.FARROWING_TRANSFER,
                    dueDate = now + (2 * dayMs),
                    timeLabel = "15:30",
                    assignedTo = "Nguyễn Văn Tuấn",
                    status = TaskStatus.TODO,
                    priority = TaskPriority.HIGH,
                    referenceId = "Nái NY-078",
                    locationInfo = "Chuồng Bầu -> Chuồng Đẻ 2",
                    note = "Tắm sát trùng nái bằng xà phòng sát khuẩn, bật đèn úm trước 4 tiếng"
                ),
                TaskEntity(
                    id = "task-6",
                    title = "Cân định kỳ Lô LH-204",
                    taskType = TaskType.WEIGHT_CHECK,
                    dueDate = now - 3600_000L,
                    timeLabel = "10:30",
                    assignedTo = "Kỹ thuật viên cân",
                    status = TaskStatus.DONE,
                    priority = TaskPriority.NORMAL,
                    referenceId = "Lô LH-204",
                    locationInfo = "Chuồng Cấp dưỡng 3",
                    note = "Đạt 75.2 kg (+4.5 kg so với kỳ trước)"
                ),
                TaskEntity(
                    id = "task-7",
                    title = "Cân định kỳ Lô L-105",
                    taskType = TaskType.WEIGHT_CHECK,
                    dueDate = now - 7200_000L,
                    timeLabel = "07:30",
                    assignedTo = "Kỹ thuật viên cân",
                    status = TaskStatus.DONE,
                    priority = TaskPriority.NORMAL,
                    referenceId = "Lô L-105",
                    locationInfo = "Chuồng Thịt 2",
                    note = "Đạt 92.4 kg"
                ),
                TaskEntity(
                    id = "task-8",
                    title = "Giám sát sinh con (Trực đẻ)",
                    taskType = TaskType.FARROWING_MONITOR,
                    dueDate = now + (3 * dayMs),
                    timeLabel = "00:00",
                    assignedTo = "Trực ca đêm",
                    status = TaskStatus.TODO,
                    priority = TaskPriority.HIGH,
                    referenceId = "Nái NY-069",
                    locationInfo = "Chuồng Đẻ 3 (Ô 05)",
                    note = "Chuẩn bị giỏ úm, kẹp rốn, oxy và canxi bồi dưỡng"
                )
            )
            database.taskDao().insertTasks(tasks)

            // 5. Sync Logs (Matching Page 22)
            val logs = listOf(
                SyncLogEntity(
                    id = "log-1",
                    timestamp = now - 600_000L,
                    timeFormatted = "10:38",
                    title = "Cập nhật Heo Nái NY-089",
                    subDetail = "Phối giống",
                    syncStatus = SyncStatus.SYNCED
                ),
                SyncLogEntity(
                    id = "log-2",
                    timestamp = now - 3600_000L,
                    timeFormatted = "09:42",
                    title = "Thêm Lô mới LH-204",
                    subDetail = "Nhập đàn",
                    syncStatus = SyncStatus.SYNCED
                ),
                SyncLogEntity(
                    id = "log-3",
                    timestamp = now - 7200_000L,
                    timeFormatted = "08:15",
                    title = "Kiểm tra Siêu âm - Nái NY-085",
                    subDetail = "Xung đột ngày khám",
                    syncStatus = SyncStatus.SYNC_FAILED,
                    hasConflict = true,
                    conflictDetail = "Client gửi D+28 (28 ngày), Server có D+35 do Quản lý phê duyệt"
                ),
                SyncLogEntity(
                    id = "log-4",
                    timestamp = now - 10800_000L,
                    timeFormatted = "07:30",
                    title = "Cập nhật Cân nặng Lô L-105",
                    subDetail = "Đo trọng lượng",
                    syncStatus = SyncStatus.PENDING_SYNC
                )
            )
            database.syncLogDao().insertLogs(logs)

            // 6. System Parameters (Mở rộng đầy đủ 6 tiểu mục danh mục dùng chung + Chuồng & Loại thải)
            val parameters = listOf(
                // A. Heo đực & Tinh phối (Boars & Semen batches)
                SystemParameterEntity("boar-1", SystemParameterEntity.CATEGORY_BOAR, "Duroc D-01 (Đực giống)", "D01", "Đực giống nhảy trực tiếp • Tinh lực 92% • Tăng trọng nhanh", "Liều", 1, subType = "Tại trại", secondaryValue = "Duroc", numericValue = 1.0, statusText = "Đang hoạt động"),
                SystemParameterEntity("boar-2", SystemParameterEntity.CATEGORY_BOAR, "Duroc D-02 (Đực giống)", "D02", "Đực giống nhảy trực tiếp • Chuẩn ngoại hình 2 máu", "Liều", 2, subType = "Tại trại", secondaryValue = "Duroc", numericValue = 1.0, statusText = "Đang hoạt động"),
                SystemParameterEntity("boar-3", SystemParameterEntity.CATEGORY_BOAR, "Pietrain P-01 (Đực giống)", "P01", "Đực giống nhảy trực tiếp • Siêu nạc Bỉ 4 chân chắc khỏe", "Liều", 3, subType = "Nhập ngoại", secondaryValue = "Pietrain", numericValue = 1.0, statusText = "Đang hoạt động"),
                SystemParameterEntity("boar-4", SystemParameterEntity.CATEGORY_BOAR, "Liều tinh Duroc 301", "LT301", "Kho liều tinh mát • Tinh hoạt lực >85% • Bảo quản 17°C", "Liều", 4, subType = "Tại trại", secondaryValue = "Duroc", numericValue = 25.0, statusText = "Đang hoạt động"),
                SystemParameterEntity("boar-5", SystemParameterEntity.CATEGORY_BOAR, "Liều tinh Pietrain 502", "LT502", "Kho liều tinh mát nhập ngoại • Dòng siêu nạc cao cấp", "Liều", 5, subType = "Nhập ngoại", secondaryValue = "Pietrain", numericValue = 18.0, statusText = "Đang hoạt động"),
                SystemParameterEntity("boar-6", SystemParameterEntity.CATEGORY_BOAR, "Liều tinh Landrace L-05", "LT105", "Kho liều tinh mát • Khai thác từ nọc đầu đàn Landrace", "Liều", 6, subType = "Tại trại", secondaryValue = "Landrace", numericValue = 12.0, statusText = "Đang hoạt động"),

                // B. Nhân viên & Kỹ thuật viên (Staff & Technicians)
                SystemParameterEntity("staff-1", SystemParameterEntity.CATEGORY_STAFF, "Nguyễn Văn Tuấn", "NV01", "Quản lý điều hành chung & phụ trách hồ sơ dịch tễ toàn trại", "", 1, subType = "Quản lý", secondaryValue = "0912 345 678", statusText = "Đang làm việc"),
                SystemParameterEntity("staff-2", SystemParameterEntity.CATEGORY_STAFF, "Trần Văn Bình", "NV02", "Kỹ thuật viên phối giống & thụ tinh nhân tạo nái", "", 2, subType = "Kỹ thuật viên", secondaryValue = "0983 214 567", statusText = "Đang làm việc"),
                SystemParameterEntity("staff-3", SystemParameterEntity.CATEGORY_STAFF, "Lê Thị Mai", "NV03", "Bác sĩ thú y trại • Phụ trách phác đồ tiêm vaccine & điều trị", "", 3, subType = "Thú y", secondaryValue = "0976 543 210", statusText = "Đang làm việc"),
                SystemParameterEntity("staff-4", SystemParameterEntity.CATEGORY_STAFF, "Phạm Hoàng Nam", "NV04", "Công nhân chăm sóc, vệ sinh chuồng nuôi và hỗ trợ nái đẻ", "", 4, subType = "Công nhân", secondaryValue = "0905 888 999", statusText = "Đang làm việc"),

                // C. Giống heo (Pig Breeds)
                SystemParameterEntity("breed-1", SystemParameterEntity.CATEGORY_BREED, "Landrace", "B_LANDRACE", "Giống Đan Mạch siêu dài đòn, mắn đẻ, trung bình 12-14 con/lứa, nuôi con khéo", "", 1, subType = "Nái nền sinh sản", statusText = "Đang hoạt động"),
                SystemParameterEntity("breed-2", SystemParameterEntity.CATEGORY_BREED, "Yorkshire", "B_YORKSHIRE", "Đại Bạch, khả năng tiết sữa dồi dào, kháng bệnh tốt, tỷ lệ thụ thai cao", "", 2, subType = "Nái nền sinh sản", statusText = "Đang hoạt động"),
                SystemParameterEntity("breed-3", SystemParameterEntity.CATEGORY_BREED, "Duroc", "B_DUROC", "Dòng đực cuối, cơ bắp cuồn cuộn, tăng trọng nhanh (ADG >850g/ngày), thịt đỏ", "", 3, subType = "Đực giống", statusText = "Đang hoạt động"),
                SystemParameterEntity("breed-4", SystemParameterEntity.CATEGORY_BREED, "Pietrain", "B_PIETRAIN", "Giống Bỉ siêu nạc, mỡ lưng mỏng (<10mm), tỷ lệ thịt xẻ >65%", "", 4, subType = "Đực giống", statusText = "Đang hoạt động"),
                SystemParameterEntity("breed-5", SystemParameterEntity.CATEGORY_BREED, "F1 (Landrace x Yorkshire)", "B_F1_LY", "Nái nền sinh sản phổ biến nhất trang trại, phát huy tối đa ưu thế lai", "", 5, subType = "Nái thương phẩm", statusText = "Đang hoạt động"),
                SystemParameterEntity("breed-6", SystemParameterEntity.CATEGORY_BREED, "F2 (Duroc x F1)", "B_F2_DLY", "Heo thương phẩm 3 máu nuôi thịt, tốc độ tăng trưởng vượt trội", "", 6, subType = "Heo thịt thương phẩm", statusText = "Đang hoạt động"),

                // D. Thức ăn (Feed / Cám)
                SystemParameterEntity("feed-1", SystemParameterEntity.CATEGORY_FEED, "Cám hỗn hợp nái mang thai", "CAM_NAI_MANG_THAI", "Dinh dưỡng chuẩn giai đoạn chửa kỳ 1 & 2 • Đạm thô 14%", "Bao 25kg", 1, secondaryValue = "CP Feed", numericValue = 320000.0, statusText = "Đang sử dụng", safetyDaysThreshold = 3, minStockThreshold = 20.0),
                SystemParameterEntity("feed-2", SystemParameterEntity.CATEGORY_FEED, "Cám nái nuôi con cao sản", "CAM_NAI_NUOI_CON", "Kích thích tiết sữa, chống suy kiệt nái mẹ • Đạm thô 19%", "Bao 25kg", 2, secondaryValue = "Cargill", numericValue = 350000.0, statusText = "Đang sử dụng", safetyDaysThreshold = 3, minStockThreshold = 20.0),
                SystemParameterEntity("feed-3", SystemParameterEntity.CATEGORY_FEED, "Cám tập ăn heo con sơ sinh", "CAM_HEO_CON_TAP_AN", "Dễ tiêu hóa, thơm ngon, tập ăn từ D+14 • Men tiêu hóa", "Bao 10kg", 3, secondaryValue = "De Heus", numericValue = 280000.0, statusText = "Đang sử dụng", safetyDaysThreshold = 3, minStockThreshold = 15.0),
                SystemParameterEntity("feed-4", SystemParameterEntity.CATEGORY_FEED, "Cám heo con sau cai sữa", "CAM_SAU_CAI_SUA", "Ngăn ngừa tiêu chảy sau cai sữa • Kháng thể lòng đỏ", "Bao 25kg", 4, secondaryValue = "GreenFeed", numericValue = 340000.0, statusText = "Đang sử dụng", safetyDaysThreshold = 3, minStockThreshold = 20.0),
                SystemParameterEntity("feed-5", SystemParameterEntity.CATEGORY_FEED, "Cám vỗ béo heo thịt", "CAM_THIT_VO_BEO", "Tăng trọng nhanh, tỷ lệ nạc cao >62% • Giảm FCR xuống 2.4", "Bao 40kg", 5, secondaryValue = "CJ Vina", numericValue = 410000.0, statusText = "Đang sử dụng", safetyDaysThreshold = 3, minStockThreshold = 25.0),

                // E. Thuốc & Thú y (Medicine & Drugs)
                SystemParameterEntity("med-1", SystemParameterEntity.CATEGORY_MEDICINE, "Iron Dextran 100ml (Tiêm sắt)", "THUOC_IRON_DEXTRAN", "Liều khuyên dùng: 1ml/con tiêm bắp ngày D+3", "Lọ 100ml", 1, subType = "Bổ trợ", numericValue = 85000.0, statusText = "Đang sử dụng"),
                SystemParameterEntity("med-2", SystemParameterEntity.CATEGORY_MEDICINE, "Kháng sinh Amoxicillin 100ml", "THUOC_AMOXICILLIN", "Liều khuyên dùng: 1ml/10kg P điều trị tiêu chảy, hô hấp, MMA", "Lọ 100ml", 2, subType = "Kháng sinh", numericValue = 125000.0, statusText = "Đang sử dụng"),
                SystemParameterEntity("med-3", SystemParameterEntity.CATEGORY_MEDICINE, "Oxytocin kích đẻ 50ml", "THUOC_OXYTOCIN", "Liều khuyên dùng: 2-3ml/nái kích thích co bóp tử cung hỗ trợ đẻ", "Lọ 50ml", 3, subType = "Bổ trợ", numericValue = 65000.0, statusText = "Đang sử dụng"),
                SystemParameterEntity("med-4", SystemParameterEntity.CATEGORY_MEDICINE, "Dung dịch sát trùng Iodine 10%", "THUOC_IODINE_10", "Liều khuyên dùng: Pha loãng tỷ lệ 1:200 sát trùng rốn & chuồng", "Chai 1 Lít", 4, subType = "Sát trùng", numericValue = 95000.0, statusText = "Đang sử dụng"),
                SystemParameterEntity("med-5", SystemParameterEntity.CATEGORY_MEDICINE, "Vitamin ADE + B-Complex", "THUOC_VITAMIN_ADE", "Liều khuyên dùng: Tiêm 2ml/con bồi bổ cơ thể chống còi", "Lọ 100ml", 5, subType = "Bổ trợ", numericValue = 110000.0, statusText = "Đang sử dụng"),
                SystemParameterEntity("med-6", SystemParameterEntity.CATEGORY_MEDICINE, "Vắc-xin Dịch tả lợn cổ điển (CSF)", "VAC_CSF", "Liều khuyên dùng: 1 liều/nái định kỳ 6 tháng/lần", "Lọ 20 liều", 6, subType = "Vaccine", numericValue = 160000.0, statusText = "Đang sử dụng"),

                // F. Vật tư chuồng trại (Supplies & Equipment)
                SystemParameterEntity("sup-1", SystemParameterEntity.CATEGORY_SUPPLIES, "Xilanh tự động 2ml (Inox)", "VT_XILANH_2ML", "Tiêm vaccine và thuốc phòng liên tục có định liều chính xác", "Cái", 1, numericValue = 320000.0, statusText = "Còn hàng"),
                SystemParameterEntity("sup-2", SystemParameterEntity.CATEGORY_SUPPLIES, "Kim tiêm Inox 18G (Hộp 100)", "VT_KIM_TIEM_18G", "Kim tiêm bắp nái và heo thịt • Chống gãy, tái sử dụng", "Hộp 100 kim", 2, numericValue = 45000.0, statusText = "Còn hàng"),
                SystemParameterEntity("sup-3", SystemParameterEntity.CATEGORY_SUPPLIES, "Bóng đèn sưởi hồng ngoại 175W", "VT_DEN_UOM_175W", "Đèn úm giữ ấm ổ heo con trong lồng đẻ", "Bóng", 3, numericValue = 68000.0, statusText = "Còn hàng"),
                SystemParameterEntity("sup-4", SystemParameterEntity.CATEGORY_SUPPLIES, "Thẻ tai nhựa vàng bấm số (100)", "VT_THE_TAI_VANG", "Thẻ tai định danh cá thể heo nái và đực giống", "Hộp 100 cái", 4, numericValue = 120000.0, statusText = "Còn hàng"),
                SystemParameterEntity("sup-5", SystemParameterEntity.CATEGORY_SUPPLIES, "Kìm bấm thẻ tai chuyên dụng", "VT_KIM_BAM_THE_TAI", "Kìm bấm thẻ tai thép hợp kim không rỉ, độ bấm nhạy", "Cái", 5, numericValue = 185000.0, statusText = "Còn hàng"),

                // Chuồng nuôi (Barns / Housing)
                SystemParameterEntity("barn-1", SystemParameterEntity.CATEGORY_BARN, "Khu Chuồng Bầu 1", "CHUONG_BAU_1", "Dành cho nái mang thai kỳ đầu (D+0 đến D+80)", "", 1),
                SystemParameterEntity("barn-2", SystemParameterEntity.CATEGORY_BARN, "Khu Chuồng Bầu 2", "CHUONG_BAU_2", "Dành cho nái mang thai kỳ cuối (D+80 đến D+107)", "", 2),
                SystemParameterEntity("barn-3", SystemParameterEntity.CATEGORY_BARN, "Khu Chuồng Đẻ 4", "CHUONG_DE_4", "Nái đẻ và nuôi heo con theo mẹ (D+107 đến cai sữa)", "", 3),
                SystemParameterEntity("barn-4", SystemParameterEntity.CATEGORY_BARN, "Khu Cai Sữa & Chờ Phối B", "CAI_SUA_PHOI_B", "Nái sau cai sữa chờ phối giống lại và lợn hậu bị", "", 4),
                SystemParameterEntity("barn-5", SystemParameterEntity.CATEGORY_BARN, "Khu Chuồng Thịt 1", "CHUONG_THIT_1", "Nuôi heo thịt thương phẩm giai đoạn 1 (30-70kg)", "", 5),
                SystemParameterEntity("barn-6", SystemParameterEntity.CATEGORY_BARN, "Khu Chuồng Thịt 2 & 3", "CHUONG_THIT_2_3", "Nuôi heo thịt xuất bán giai đoạn 2 (70-110kg)", "", 6),

                // Nguyên nhân loại thải (Culling Reasons)
                SystemParameterEntity("cull-1", SystemParameterEntity.CATEGORY_CULLING_REASON, "Viêm khớp / Bại liệt chân", "CHAN_BAI_LIET", "Chân yếu, đau móng, không đứng vững để phối giống", "", 1),
                SystemParameterEntity("cull-2", SystemParameterEntity.CATEGORY_CULLING_REASON, "Mất sữa / Viêm vú mãn tính", "VIEM_VU_MAT_SUA", "Viêm tuyến vú, teo núm vú, không đủ sữa nuôi con", "", 2),
                SystemParameterEntity("cull-3", SystemParameterEntity.CATEGORY_CULLING_REASON, "Không đậu thai sau 3 lần phối", "VO_SINH_3_LAN", "Lốc sảy, phối giống quá 3 chu kỳ liên tiếp không có thai", "", 3),
                SystemParameterEntity("cull-4", SystemParameterEntity.CATEGORY_CULLING_REASON, "Năng suất con sơ sinh thấp (<7 con)", "NANG_SUAT_KEM", "Số con đẻ ra còn sống thấp trong 2 lứa liên tiếp", "", 4),
                SystemParameterEntity("cull-5", SystemParameterEntity.CATEGORY_CULLING_REASON, "Nái già quá tuổi kinh tế (>7 lứa)", "NAI_GIA_7_LUA", "Đã khai thác hết tuổi sinh sản kinh tế", "", 5),
                SystemParameterEntity("cull-6", SystemParameterEntity.CATEGORY_CULLING_REASON, "Sảy thai bệnh lý / MMA", "SAY_THAI_MMA", "Hội chứng viêm tử cung - viêm vú - mất sữa", "", 6)
            ) + com.example.domain.feed.FeedRationCalculator.getDefaultRationMatrix()
            database.systemParameterDao().insertParameters(parameters)

            // 7. Khởi tạo Dữ liệu Tồn kho Thực tế (Inventory Items)
            val inventoryItems = listOf(
                // Cám
                InventoryItemEntity("inv-cam-1", "Cám hỗn hợp nái mang thai", InventoryItemEntity.CATEGORY_FEED, "Bao 25kg", 120f, 320000L, 320000L, 15f, "Đơn vị cung ứng: CP Feed", "feed-1"),
                InventoryItemEntity("inv-cam-2", "Cám nái nuôi con cao sản", InventoryItemEntity.CATEGORY_FEED, "Bao 25kg", 85f, 350000L, 350000L, 10f, "Độ đạm 19%, kích thích tiết sữa", "feed-2"),
                InventoryItemEntity("inv-cam-3", "Cám tập ăn heo con sơ sinh", InventoryItemEntity.CATEGORY_FEED, "Bao 10kg", 50f, 280000L, 280000L, 10f, "Tập ăn từ 14 ngày tuổi", "feed-3"),
                InventoryItemEntity("inv-cam-4", "Cám heo con sau cai sữa", InventoryItemEntity.CATEGORY_FEED, "Bao 25kg", 95f, 340000L, 340000L, 12f, "Giai đoạn 1 sau cai sữa", "feed-4"),
                InventoryItemEntity("inv-cam-5", "Cám vỗ béo heo thịt", InventoryItemEntity.CATEGORY_FEED, "Bao 40kg", 200f, 410000L, 410000L, 25f, "Giai đoạn xuất chuồng", "feed-5"),

                // Thuốc
                InventoryItemEntity("inv-thuoc-1", "Iron Dextran 100ml (Tiêm sắt)", InventoryItemEntity.CATEGORY_MEDICINE, "Lọ 100ml", 45f, 85000L, 85000L, 8f, "Tiêm D+3 cho heo con", "med-1"),
                InventoryItemEntity("inv-thuoc-2", "Kháng sinh Amoxicillin 100ml", InventoryItemEntity.CATEGORY_MEDICINE, "Lọ 100ml", 30f, 125000L, 125000L, 6f, "Đặc trị tiêu chảy, hô hấp", "med-2"),
                InventoryItemEntity("inv-thuoc-3", "Oxytocin kích đẻ 50ml", InventoryItemEntity.CATEGORY_MEDICINE, "Lọ 50ml", 20f, 65000L, 65000L, 5f, "Hỗ trợ nái đẻ khó", "med-3"),
                InventoryItemEntity("inv-thuoc-4", "Dung dịch sát trùng Iodine 10%", InventoryItemEntity.CATEGORY_MEDICINE, "Chai 1 Lít", 15f, 95000L, 95000L, 4f, "Sát trùng rốn & chuồng", "med-4"),
                InventoryItemEntity("inv-thuoc-5", "Vitamin ADE + B-Complex", InventoryItemEntity.CATEGORY_MEDICINE, "Lọ 100ml", 40f, 110000L, 110000L, 8f, "Hồi phục sức khỏe nái", "med-5"),

                // Vật tư
                InventoryItemEntity("inv-vattu-1", "Xilanh tự động 2ml (Inox)", InventoryItemEntity.CATEGORY_SUPPLIES, "Cái", 12f, 250000L, 250000L, 2f, "Dùng tiêm vaccine", "sup-1"),
                InventoryItemEntity("inv-vattu-2", "Kim tiêm Inox 18G (Hộp 100)", InventoryItemEntity.CATEGORY_SUPPLIES, "Hộp 100 kim", 8f, 120000L, 120000L, 2f, "Kim tiêm bắp", "sup-2"),
                InventoryItemEntity("inv-vattu-3", "Bóng đèn sưởi hồng ngoại 175W", InventoryItemEntity.CATEGORY_SUPPLIES, "Bóng", 25f, 75000L, 75000L, 5f, "Úm ấm heo con", "sup-3"),
                InventoryItemEntity("inv-vattu-4", "Thẻ tai nhựa vàng (Hộp 100)", InventoryItemEntity.CATEGORY_SUPPLIES, "Hộp 100 cái", 10f, 220000L, 220000L, 2f, "Đánh số định danh", "sup-4"),
                InventoryItemEntity("inv-vattu-5", "Kìm bấm thẻ tai chuyên dụng", InventoryItemEntity.CATEGORY_SUPPLIES, "Cái", 5f, 180000L, 180000L, 1f, "Kìm bấm thẻ tai", "sup-5")
            )
            database.inventoryDao().insertItems(inventoryItems)

            // Giao dịch mẫu
            val sampleTransactions = listOf(
                InventoryTransactionEntity(
                    id = "tx-in-1",
                    type = InventoryTransactionEntity.TYPE_IN,
                    itemId = "inv-cam-1",
                    itemName = "Cám hỗn hợp nái mang thai",
                    category = InventoryItemEntity.CATEGORY_FEED,
                    unit = "Bao 25kg",
                    quantity = 150f,
                    unitPrice = 320000L,
                    totalPrice = 48000000L,
                    supplierOrNotes = "Công ty CP Chăn Nuôi VN - HĐ #HD-202708-01",
                    date = System.currentTimeMillis() - 7 * 86_400_000L
                ),
                InventoryTransactionEntity(
                    id = "tx-out-1",
                    type = InventoryTransactionEntity.TYPE_OUT,
                    itemId = "inv-cam-1",
                    itemName = "Cám hỗn hợp nái mang thai",
                    category = InventoryItemEntity.CATEGORY_FEED,
                    unit = "Bao 25kg",
                    quantity = 30f,
                    unitPrice = 320000L,
                    totalPrice = 9600000L,
                    targetBarn = "Khu Chuồng Bầu 1",
                    supplierOrNotes = "Cấp cám tuần cho 30 nái mang thai",
                    date = System.currentTimeMillis() - 3 * 86_400_000L
                )
            )
            database.inventoryDao().insertTransaction(sampleTransactions[0])
            database.inventoryDao().insertTransaction(sampleTransactions[1])

            // Nhân viên trang trại (Staff Members)
            val defaultStaff = listOf(
                StaffEntity(
                    id = "staff-001",
                    code = "NV-001",
                    fullName = "Nguyễn Văn Tuấn",
                    phoneNumber = "0987654321",
                    role = StaffEntity.ROLE_MANAGER,
                    department = "Toàn trang trại",
                    status = StaffEntity.STATUS_ACTIVE
                ),
                StaffEntity(
                    id = "staff-002",
                    code = "NV-002",
                    fullName = "Trần Đình Khang",
                    phoneNumber = "0912345678",
                    role = StaffEntity.ROLE_TECHNICIAN,
                    department = "Khu nái đẻ & thụ tinh",
                    status = StaffEntity.STATUS_ACTIVE
                ),
                StaffEntity(
                    id = "staff-003",
                    code = "NV-003",
                    fullName = "Lê Thị Lan",
                    phoneNumber = "0903112233",
                    role = StaffEntity.ROLE_VET,
                    department = "Y tế chuồng trại & Vaccine",
                    status = StaffEntity.STATUS_ACTIVE
                ),
                StaffEntity(
                    id = "staff-004",
                    code = "NV-004",
                    fullName = "Phạm Văn Hùng",
                    phoneNumber = "0938445566",
                    role = StaffEntity.ROLE_WORKER,
                    department = "Khu nuôi heo thịt",
                    status = StaffEntity.STATUS_ACTIVE
                )
            )
            database.staffDao().insertStaffList(defaultStaff)

            // Khởi tạo Danh mục gốc MasterItem, Giao dịch Kho và Tồn kho tức thời
            val masterItems = listOf(
                MasterItemEntity("CAM-NB-01", MasterItemEntity.TYPE_FEED, "Cám nái bầu", "Bao 25kg", 320000L, 20f, "CAM_NAI_MANG_THAI", "Dinh dưỡng chuẩn giai đoạn chửa kỳ 1 & 2"),
                MasterItemEntity("CAM-ND-02", MasterItemEntity.TYPE_FEED, "Cám nái nuôi con", "Bao 25kg", 350000L, 15f, "CAM_NAI_NUOI_CON", "Kích thích tiết sữa, chống suy kiệt nái mẹ"),
                MasterItemEntity("CAM-HT-03", MasterItemEntity.TYPE_FEED, "Cám vỗ béo heo thịt", "Bao 40kg", 410000L, 25f, "CAM_THIT_VO_BEO", "Tăng trọng nhanh giai đoạn xuất chuồng"),
                MasterItemEntity("VTM-01", MasterItemEntity.TYPE_MED, "Vắc-xin Dịch tả lợn cổ điển", "Lọ 20 liều", 160000L, 5f, "VAC_CSF", "Phòng dịch tả định kỳ 6 tháng/lần"),
                MasterItemEntity("VTM-02", MasterItemEntity.TYPE_MED, "Kháng sinh Amoxicillin 100ml", "Lọ 100ml", 125000L, 8f, "THUOC_AMOXICILLIN", "Đặc trị tiêu chảy, hô hấp, MMA"),
                MasterItemEntity("SUP-01", MasterItemEntity.TYPE_SUPPLY, "Xilanh tự động 2ml (Inox)", "Cái", 320000L, 2f, "VT_XILANH_2ML", "Tiêm vaccine có định liều chính xác"),
                MasterItemEntity("SUP-02", MasterItemEntity.TYPE_SUPPLY, "Thẻ tai nhựa vàng (Hộp 100)", "Hộp 100 cái", 120000L, 2f, "VT_THE_TAI_VANG", "Định danh cá thể nái và đực giống")
            )
            database.masterItemDao().insertMasterItems(masterItems)

            val receipts = listOf(
                ReceiptEntity("rec-001", "CAM-NB-01", System.currentTimeMillis() - 7 * 86_400_000L, 150f, 320000L, 48000000L, "CP Feed", "Nhập cám đầu tuần"),
                ReceiptEntity("rec-002", "VTM-02", System.currentTimeMillis() - 5 * 86_400_000L, 50f, 125000L, 6250000L, "Thú y Trung Ương", "Nhập kháng sinh dự phòng")
            )
            database.receiptDao().insertReceipts(receipts)

            val issues = listOf(
                IssueEntity("iss-001", "CAM-NB-01", System.currentTimeMillis() - 3 * 86_400_000L, 30f, "CHUONG_BAU_1", "Cấp cám tuần nái bầu", 320000L, 9600000L)
            )
            database.issueDao().insertIssues(issues)

            val inventories = listOf(
                InventoryEntity("CAM-NB-01", 120f, 38400000L, 320000L),
                InventoryEntity("CAM-ND-02", 85f, 29750000L, 350000L),
                InventoryEntity("CAM-HT-03", 200f, 82000000L, 410000L),
                InventoryEntity("VTM-01", 20f, 3200000L, 160000L),
                InventoryEntity("VTM-02", 50f, 6250000L, 125000L),
                InventoryEntity("SUP-01", 12f, 3840000L, 320000L),
                InventoryEntity("SUP-02", 10f, 1200000L, 120000L)
            )
            database.inventoryEntityDao().insertAllInventories(inventories)
        }
    }
}
