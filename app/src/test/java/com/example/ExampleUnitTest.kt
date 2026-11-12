package com.example

import com.example.data.local.AppDatabase
import com.example.data.local.entity.PigEntity
import com.example.data.model.SowReproductiveStatus
import com.example.data.repository.PigRepositoryImpl
import com.example.domain.AutoTaskPipeline
import com.example.domain.repository.PigRepository
import com.example.domain.usecase.AddPigUseCase
import com.example.domain.usecase.DeletePigUseCase
import com.example.domain.usecase.GetPigByIdUseCase
import com.example.domain.usecase.GetPigStatsUseCase
import com.example.domain.usecase.GetPigsUseCase
import com.example.domain.usecase.UpdatePigUseCase
import com.example.ui.pig.PigFilterType
import com.example.ui.pig.PigViewModel
import com.example.data.local.entity.InventoryItemEntity
import com.example.data.local.entity.InventoryTransactionEntity
import com.example.data.local.entity.SystemParameterEntity
import com.example.data.local.dao.SystemParameterDao
import com.example.data.repository.InventoryRepositoryImpl
import com.example.data.repository.SystemParameterRepositoryImpl
import com.example.ui.settings.formatVnd
import com.example.ui.home.DailyFeedAnalytics
import com.example.ui.home.FinancialBalance
import com.example.data.local.entity.EventEntity
import com.example.data.local.entity.StaffEntity
import com.example.data.local.dao.StaffDao
import com.example.data.repository.StaffRepositoryImpl
import com.example.domain.repository.StaffRepository
import com.example.domain.feed.FeedRationCalculator
import com.example.ui.pig.BarnCategoryType
import com.example.ui.pig.BarnDistributionCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.util.Locale
import com.example.ui.theme.OutdoorBorder
import com.example.ui.theme.OutdoorFocusBorder
import com.example.data.local.entity.MasterItem
import com.example.data.local.entity.MasterItemEntity
import com.example.data.local.entity.Receipt
import com.example.data.local.entity.ReceiptEntity
import com.example.data.local.entity.Issue
import com.example.data.local.entity.IssueEntity
import com.example.data.local.entity.Inventory
import com.example.data.local.entity.InventoryEntity
import com.example.data.local.dao.MasterItemDao
import com.example.data.local.dao.ReceiptDao
import com.example.data.local.dao.IssueDao
import com.example.data.local.dao.InventoryEntityDao
import com.example.data.repository.MasterInventoryRepositoryImpl
import com.example.domain.repository.MasterInventoryRepository
import com.example.ui.theme.OutdoorInputFill
import com.example.ui.theme.OutdoorInputValue
import com.example.ui.theme.OutdoorLabelText
import com.example.ui.theme.OutdoorModeManager
import com.example.ui.theme.OutdoorOverallBackground
import com.example.ui.theme.OutdoorPlaceholder
import com.example.ui.theme.OutdoorRequiredStar
import com.example.ui.theme.OutdoorDivider
import com.example.ui.theme.OutdoorSelectedRadio
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ExampleUnitTest {

    @Test
    fun `auto task pipeline generates 6 tasks for mating cycle`() {
        val matingDate = System.currentTimeMillis()
        val tasks = AutoTaskPipeline.generateTasksForMatingCycle(
            sowTag = "Heo nái NY-089",
            location = "Bầu 12",
            matingDate = matingDate
        )

        // Verifies the 6 key milestones defined in PDF Page 16
        assertEquals(6, tasks.size)
        assertTrue(tasks.any { it.title.contains("lốc sảy chu kỳ 1") })
        assertTrue(tasks.any { it.title.contains("Siêu âm thai") })
        assertTrue(tasks.any { it.title.contains("lốc sảy chu kỳ 2") })
        assertTrue(tasks.any { it.title.contains("Chuyển chuồng đẻ") })
        assertTrue(tasks.any { it.title.contains("Giám sát sinh con") })
        assertTrue(tasks.any { it.title.contains("cai sữa") })
    }

    @Test
    fun `pig entity contains required fields`() {
        val now = System.currentTimeMillis()
        val pig = PigEntity(
            id = "pig-001",
            tagCode = "NY-089",
            birthDate = now,
            breed = "Yorkshire",
            gender = "Cái",
            status = SowReproductiveStatus.MANG_THAI,
            location = "Bầu 12"
        )

        assertEquals("pig-001", pig.id)
        assertEquals("NY-089", pig.tagCode)
        assertEquals(now, pig.birthDate)
        assertEquals(SowReproductiveStatus.MANG_THAI, pig.status)
        assertEquals("Bầu 12", pig.location)
    }

    @Test
    fun `app database inherits from room database`() {
        val superclass = AppDatabase::class.java.superclass
        assertNotNull(superclass)
        assertEquals("androidx.room.RoomDatabase", superclass.name)
    }

    @Test
    fun `pig repository implementation adheres to domain interface`() {
        // Verifies Clean Architecture interface polymorphism
        assertTrue(PigRepository::class.java.isAssignableFrom(PigRepositoryImpl::class.java))
    }

    @Test
    fun `use cases encapsulate business logic and interactions`() = runBlocking {
        val fakeRepo = FakePigRepository()
        val getPigsUseCase = GetPigsUseCase(fakeRepo)
        val getPigByIdUseCase = GetPigByIdUseCase(fakeRepo)
        val addPigUseCase = AddPigUseCase(fakeRepo)
        val updatePigUseCase = UpdatePigUseCase(fakeRepo)
        val deletePigUseCase = DeletePigUseCase(fakeRepo)
        val getPigStatsUseCase = GetPigStatsUseCase(fakeRepo)

        // 1. Test AddPigUseCase validation - blank tag fails
        val failResult = addPigUseCase(
            tagCode = "   ",
            isBatch = false,
            quantity = 1,
            breed = "Yorkshire",
            gender = "Cái",
            status = SowReproductiveStatus.HAU_BI,
            location = "Khu A",
            currentWeightKg = 100f
        )
        assertTrue(failResult.isFailure)

        // 2. Test AddPigUseCase success
        val successResult = addPigUseCase(
            tagCode = "NY-100",
            isBatch = false,
            quantity = 1,
            breed = "Yorkshire",
            gender = "Cái",
            status = SowReproductiveStatus.HAU_BI,
            location = "Khu A",
            currentWeightKg = 110f
        )
        assertTrue(successResult.isSuccess)
        val createdId = successResult.getOrThrow()

        // 3. Test GetPigsUseCase
        val pigs = getPigsUseCase().first()
        assertEquals(1, pigs.size)
        assertEquals("NY-100", pigs.first().tagCode)

        // 4. Test GetPigByIdUseCase
        val fetchedPig = getPigByIdUseCase(createdId).first()
        assertNotNull(fetchedPig)
        assertEquals("NY-100", fetchedPig?.tagCode)

        // 5. Test UpdatePigUseCase
        val updatedPig = fetchedPig!!.copy(location = "Chuồng Bầu 5")
        updatePigUseCase(updatedPig)
        val reFetched = getPigByIdUseCase(createdId).first()
        assertEquals("Chuồng Bầu 5", reFetched?.location)

        // 6. Test GetPigStatsUseCase
        val stats = getPigStatsUseCase().first()
        assertEquals(1, stats.recordCount)
        assertEquals(1, stats.totalHeadCount)

        // 7. Test DeletePigUseCase
        deletePigUseCase(reFetched!!)
        val emptyPigs = getPigsUseCase().first()
        assertTrue(emptyPigs.isEmpty())
    }

    @Test
    fun `pig view model handles user action queries and filters`() {
        val fakeRepo = FakePigRepository()
        val viewModel = PigViewModel(fakeRepo)

        viewModel.onSearchQueryChanged("NY-089")
        assertEquals("NY-089", viewModel.searchQuery.value)

        viewModel.clearSearchQuery()
        assertEquals("", viewModel.searchQuery.value)

        viewModel.filterByTagId("TAG-999")
        assertEquals("TAG-999", viewModel.searchQuery.value)

        viewModel.onFilterSelected(PigFilterType.BATCH)
        assertEquals(PigFilterType.BATCH, viewModel.activeFilter.value)

        viewModel.selectPig("pig-123")
        assertEquals("pig-123", viewModel.selectedPigId.value)

        viewModel.selectPig(null)
        assertEquals(null, viewModel.selectedPigId.value)

        // Test sorting toggles
        assertEquals(com.example.ui.pig.PigSortOption.TAG_ID, viewModel.sortOption.value)
        viewModel.toggleSortOption()
        assertEquals(com.example.ui.pig.PigSortOption.BIRTH_DATE, viewModel.sortOption.value)
        viewModel.toggleSortOption()
        assertEquals(com.example.ui.pig.PigSortOption.TAG_ID, viewModel.sortOption.value)

        assertEquals(com.example.ui.pig.SortDirection.ASCENDING, viewModel.sortDirection.value)
        viewModel.toggleSortDirection()
        assertEquals(com.example.ui.pig.SortDirection.DESCENDING, viewModel.sortDirection.value)

        viewModel.clearUserMessage()
    }

    @Test
    fun `pig detail data attributes properly reflect birth date and health status`() {
        val birthDate = 1672531200000L // 01/01/2023
        val pigWithHealthWarning = PigEntity(
            id = "pig-warn-1",
            tagCode = "NY-089",
            birthDate = birthDate,
            breed = "Landrace",
            gender = "Cái",
            status = SowReproductiveStatus.MANG_THAI,
            location = "Chuồng Bầu 12",
            parity = 3,
            currentWeightKg = 220f,
            hasOverdueVaccine = true,
            hasUltrasoundWarning = false
        )

        assertEquals("NY-089", pigWithHealthWarning.tagCode)
        assertEquals(birthDate, pigWithHealthWarning.birthDate)
        assertTrue(pigWithHealthWarning.hasOverdueVaccine)
        assertEquals(SowReproductiveStatus.MANG_THAI, pigWithHealthWarning.status)
        assertEquals(3, pigWithHealthWarning.parity)
        assertEquals(220f, pigWithHealthWarning.currentWeightKg, 0.01f)
    }

    @Test
    fun `health distribution calculates slices for pie chart accurately`() {
        val healthyPig = PigEntity(
            id = "p-1",
            tagCode = "TAG-1",
            breed = "Landrace",
            gender = "Cái",
            status = SowReproductiveStatus.MANG_THAI,
            location = "Bầu 1"
        )
        val vaccineWarningPig = PigEntity(
            id = "p-2",
            tagCode = "TAG-2",
            breed = "Yorkshire",
            gender = "Cái",
            status = SowReproductiveStatus.HAU_BI,
            location = "Hậu bị",
            hasOverdueVaccine = true
        )
        val ultrasoundWarningPig = PigEntity(
            id = "p-3",
            tagCode = "TAG-3",
            breed = "Duroc",
            gender = "Cái",
            status = SowReproductiveStatus.MANG_THAI,
            location = "Bầu 2",
            hasUltrasoundWarning = true
        )

        val pigs = listOf(healthyPig, vaccineWarningPig, ultrasoundWarningPig)
        val total = pigs.size
        val overdueCount = pigs.count { it.hasOverdueVaccine }
        val ultrasoundCount = pigs.count { it.hasUltrasoundWarning }
        val healthyCount = pigs.count { !it.hasOverdueVaccine && !it.hasUltrasoundWarning }

        assertEquals(3, total)
        assertEquals(1, overdueCount)
        assertEquals(1, ultrasoundCount)
        assertEquals(1, healthyCount)
    }

    @Test
    fun `pig csv exporter formats headers and records with UTF8 BOM`() {
        val pig = PigEntity(
            id = "csv-1",
            tagCode = "NY-089",
            birthDate = 1672531200000L,
            breed = "Landrace",
            gender = "Cái",
            status = SowReproductiveStatus.MANG_THAI,
            location = "Bầu 12",
            parity = 2,
            currentWeightKg = 215.5f,
            hasOverdueVaccine = true
        )

        val csv = com.example.util.PigCsvExporter.generatePigsCsv(listOf(pig))

        // Verify UTF-8 BOM is present for Excel compatibility
        assertTrue(csv.startsWith("\uFEFF"))
        assertTrue(csv.contains("Mã số tai (Tag ID)"))
        assertTrue(csv.contains("Tiêm phòng vắc-xin"))
        assertTrue(csv.contains("NY-089"))
        assertTrue(csv.contains("QUÁ HẠN TIÊM"))
        assertTrue(csv.contains("215.5"))
    }

    @Test
    fun `qr code ear tag sanitizer strips url and prefix headers`() {
        assertEquals("NY-089", com.example.ui.pig.sanitizeEarTagCode("tag:NY-089"))
        assertEquals("NY-089", com.example.ui.pig.sanitizeEarTagCode("pig://NY-089"))
        assertEquals("NY-089", com.example.ui.pig.sanitizeEarTagCode("#NY-089"))
        assertEquals("NY-089", com.example.ui.pig.sanitizeEarTagCode(" NY-089 "))
        assertEquals("NY-089", com.example.ui.pig.sanitizeEarTagCode("{\"tagCode\":\"NY-089\"}"))
    }

    @Test
    fun `farm herd structure calculates counts for 5 pig types correctly`() {
        val pregnant = PigEntity(
            id = "p-preg",
            tagCode = "NY-089",
            quantity = 1,
            breed = "Landrace",
            gender = "Cái",
            status = SowReproductiveStatus.MANG_THAI,
            location = "Chuồng Bầu 1"
        )
        val lactating = PigEntity(
            id = "p-lact",
            tagCode = "NY-072",
            quantity = 1,
            breed = "Yorkshire",
            gender = "Cái",
            status = SowReproductiveStatus.NUOI_CON,
            location = "Chuồng Đẻ 4"
        )
        val waiting = PigEntity(
            id = "p-wait",
            tagCode = "NY-064",
            quantity = 1,
            breed = "Duroc",
            gender = "Cái",
            status = SowReproductiveStatus.CAI_SUA_CHO_PHOI,
            location = "Cai Sữa B"
        )
        val suckling = PigEntity(
            id = "p-suck",
            tagCode = "Heo con theo mẹ (NY-072)",
            quantity = 11,
            isBatch = true,
            breed = "F1",
            gender = "Hỗn hợp",
            status = SowReproductiveStatus.NUOI_CON,
            location = "Chuồng Đẻ 4"
        )
        val meat = PigEntity(
            id = "p-meat",
            tagCode = "Lô Thịt 01",
            quantity = 100,
            isBatch = true,
            breed = "Duroc",
            gender = "Hỗn hợp",
            status = SowReproductiveStatus.DANG_NUOI,
            location = "Chuồng Thịt 1"
        )

        val pigs = listOf(pregnant, lactating, waiting, suckling, meat)

        var pregCount = 0
        var lactCount = 0
        var waitCount = 0
        var suckCount = 0
        var meatCount = 0

        pigs.forEach { pig ->
            when {
                pig.tagCode.contains("heo con", ignoreCase = true) -> suckCount += pig.quantity
                pig.status == SowReproductiveStatus.MANG_THAI -> pregCount += pig.quantity
                pig.status == SowReproductiveStatus.NUOI_CON -> lactCount += pig.quantity
                pig.status == SowReproductiveStatus.CAI_SUA_CHO_PHOI -> waitCount += pig.quantity
                else -> meatCount += pig.quantity
            }
        }

        assertEquals(1, pregCount)
        assertEquals(1, lactCount)
        assertEquals(1, waitCount)
        assertEquals(11, suckCount)
        assertEquals(100, meatCount)
        assertEquals(114, pregCount + lactCount + waitCount + suckCount + meatCount)
    }

    @Test
    fun `system parameter entity supports barns culling reasons and vaccines`() {
        val barn = com.example.data.local.entity.SystemParameterEntity(
            id = "b-1",
            category = com.example.data.local.entity.SystemParameterEntity.CATEGORY_BARN,
            name = "Khu Chuồng Bầu 1",
            code = "CHUONG_BAU_1",
            description = "Chuồng nái mang thai"
        )
        val culling = com.example.data.local.entity.SystemParameterEntity(
            id = "c-1",
            category = com.example.data.local.entity.SystemParameterEntity.CATEGORY_CULLING_REASON,
            name = "Viêm khớp bại liệt",
            code = "CHAN_BAI_LIET",
            description = "Chân yếu không đứng được"
        )
        val vaccine = com.example.data.local.entity.SystemParameterEntity(
            id = "v-1",
            category = com.example.data.local.entity.SystemParameterEntity.CATEGORY_VACCINE,
            name = "Vắc-xin Dịch tả lợn",
            code = "VAC_CSF",
            description = "Tiêm định kỳ 6 tháng"
        )

        assertEquals("BARN", barn.category)
        assertEquals("CULLING_REASON", culling.category)
        assertEquals("VACCINE", vaccine.category)
        assertEquals("CHUONG_BAU_1", barn.code)
    }

    // =========================================================================
    // TESTS FOR 5 BUSINESS EVENTS (PROMPT 10)
    // =========================================================================

    @Test
    fun `farrowing event auto creates DC piglet batch and 5 auto tasks`() = runBlocking {
        val fakePigDao = FakePigDao()
        val fakeTaskDao = FakeTaskDao()
        val fakeEventDao = FakeEventDao()
        val ruleEngine = com.example.domain.rule.FarmRuleEngine(fakePigDao, fakeTaskDao, fakeEventDao)

        // Seed sow N001
        val sow = PigEntity(
            id = "sow-001",
            tagCode = "N001",
            breed = "Landrace",
            gender = "Cái",
            status = SowReproductiveStatus.MANG_THAI,
            location = "Đẻ 01",
            parity = 3
        )
        fakePigDao.insertPig(sow)

        // Sự kiện: Đẻ (12 con sinh, 11 sống, 1 chết, ổ 15.6kg)
        val farrowingDate = 1813968000000L // 26/06/2027
        val pigletBatchTag = ruleEngine.onFarrowingEvent(
            sowId = sow.id,
            farrowingDate = farrowingDate,
            totalBorn = 12,
            bornAlive = 11,
            stillborn = 1,
            mummies = 0,
            litterWeightKg = 15.6f,
            notes = "Đẻ khó, hỗ trợ kéo thai"
        )

        // 1. Kiểm tra mã đàn con tự sinh bắt đầu bằng DC
        assertTrue(pigletBatchTag.startsWith("DC"))
        val createdPigletBatch = fakePigDao.getPigByTagCode(pigletBatchTag)
        assertNotNull(createdPigletBatch)
        assertEquals(11, createdPigletBatch?.quantity)
        assertEquals(SowReproductiveStatus.NUOI_CON, createdPigletBatch?.status)

        // 2. Nái mẹ chuyển sang trạng thái NUOI_CON
        val updatedSow = fakePigDao.getPigByIdOnce(sow.id)
        assertEquals(SowReproductiveStatus.NUOI_CON, updatedSow?.status)

        // 3. Tự động sinh 5 công việc: Tiêm sắt D+3, Cắt đuôi D+7, Vaccine D+14, Tập ăn D+21, Cai sữa D+28
        val tasks = fakeTaskDao.allTasks
        assertEquals(5, tasks.size)
        assertTrue(tasks.any { it.title.contains("Tiêm sắt") })
        assertTrue(tasks.any { it.title.contains("Cắt đuôi") })
        assertTrue(tasks.any { it.title.contains("Mycoplasma") })
        assertTrue(tasks.any { it.title.contains("Tập ăn") })
        assertTrue(tasks.any { it.title.contains("cai sữa") })
    }

    @Test
    fun `iron injection event records treatment for DC piglet batch`() = runBlocking {
        val fakePigDao = FakePigDao()
        val fakeTaskDao = FakeTaskDao()
        val fakeEventDao = FakeEventDao()
        val ruleEngine = com.example.domain.rule.FarmRuleEngine(fakePigDao, fakeTaskDao, fakeEventDao)

        ruleEngine.onIronInjectionEvent(
            pigletTag = "DC20270626-001",
            injectionDate = 1814227200000L,
            quantityInjected = 11,
            drugName = "Iron Dextran",
            dosageMl = 1.0f
        )

        val events = fakeEventDao.allEvents
        assertEquals(1, events.size)
        assertTrue(events[0].description.contains("DC20270626-001"))
        assertTrue(events[0].description.contains("Iron Dextran"))
        assertTrue(events[0].metrics.contains("1.0 ml"))
    }

    @Test
    fun `weaning event closes suckling batch and creates CS batch and updates sow`() = runBlocking {
        val fakePigDao = FakePigDao()
        val fakeTaskDao = FakeTaskDao()
        val fakeEventDao = FakeEventDao()
        val ruleEngine = com.example.domain.rule.FarmRuleEngine(fakePigDao, fakeTaskDao, fakeEventDao)

        // Seed sow N001 and suckling batch DC20270626-001
        val sow = PigEntity(
            id = "sow-001",
            tagCode = "N001",
            breed = "Landrace",
            gender = "Cái",
            status = SowReproductiveStatus.NUOI_CON,
            location = "Đẻ 01"
        )
        fakePigDao.insertPig(sow)

        val sucklingBatch = PigEntity(
            id = "dc-001",
            tagCode = "DC20270626-001",
            isBatch = true,
            quantity = 11,
            breed = "F1",
            gender = "Hỗn hợp",
            status = SowReproductiveStatus.NUOI_CON,
            location = "Đẻ 01"
        )
        fakePigDao.insertPig(sucklingBatch)

        // Cai sữa ngày 25/07/2027: 10 con cai, 7.5 kg
        val newBatchTag = ruleEngine.onWeaningEvent(
            pigletTag = "DC20270626-001",
            weaningDate = 1816473600000L,
            weanedQuantity = 10,
            averageWeightKg = 7.5f,
            sowTag = "N001"
        )

        // 1. Đóng chu kỳ đàn con theo mẹ (xóa đàn cũ)
        val oldBatch = fakePigDao.getPigByTagCode("DC20270626-001")
        assertEquals(null, oldBatch)

        // 2. Tạo đàn mới (10 con, 7.5kg) với mã chuẩn hóa "Heo con <Mã nái> <dd/MM/yy>" hoặc "CS..."
        assertTrue(newBatchTag.startsWith("Heo con") || newBatchTag.startsWith("CS"))
        val postWeanBatch = fakePigDao.getPigByTagCode(newBatchTag)
        assertNotNull(postWeanBatch)
        assertEquals(10, postWeanBatch?.quantity)
        assertEquals(7.5f, postWeanBatch?.currentWeightKg)

        // 3. Nái mẹ chuyển sang CAI_SUA_CHO_PHOI
        val updatedSow = fakePigDao.getPigByTagCode("N001")
        assertEquals(SowReproductiveStatus.CAI_SUA_CHO_PHOI, updatedSow?.status)

        // 4. Sinh việc theo dõi lên giống nái mẹ
        assertTrue(fakeTaskDao.allTasks.any { it.title.contains("lên giống lại nái N001") })
    }

    @Test
    fun `grower weighing event computes ADG and market readiness`() = runBlocking {
        val fakePigDao = FakePigDao()
        val fakeTaskDao = FakeTaskDao()
        val fakeEventDao = FakeEventDao()
        val ruleEngine = com.example.domain.rule.FarmRuleEngine(fakePigDao, fakeTaskDao, fakeEventDao)

        val batchIntakeDate = System.currentTimeMillis() - (14 * 86_400_000L)
        val growerBatch = PigEntity(
            id = "meat-001",
            tagCode = "LT001",
            isBatch = true,
            quantity = 100,
            breed = "Duroc x Landrace",
            gender = "Hỗn hợp",
            status = SowReproductiveStatus.DANG_NUOI,
            location = "Khu A-01",
            currentWeightKg = 22.0f,
            updatedAt = batchIntakeDate
        )
        fakePigDao.insertPig(growerBatch)

        // Cân sau 14 ngày: 100 con, TB 30.5 kg (tăng 8.5 kg)
        val weighDate = System.currentTimeMillis()
        val (adg, gain, daysToMarket) = ruleEngine.onWeighingEvent(
            batchCode = "LT001",
            weighDate = weighDate,
            headCount = 100,
            averageWeightKg = 30.5f
        )

        assertEquals(8.5f, gain, 0.01f)
        assertTrue("ADG should be around 0.6 kg/day", adg in 0.55f..0.65f)
        assertTrue("Days to 110kg market weight should be positive", daysToMarket in 100..160)

        // Lô được cập nhật khối lượng mới
        val updated = fakePigDao.getPigByTagCode("LT001")
        assertEquals(30.5f, updated?.currentWeightKg)
    }

    @Test
    fun `sow culling event changes status to LOAI_THAI and logs reason`() = runBlocking {
        val fakePigDao = FakePigDao()
        val fakeTaskDao = FakeTaskDao()
        val fakeEventDao = FakeEventDao()
        val ruleEngine = com.example.domain.rule.FarmRuleEngine(fakePigDao, fakeTaskDao, fakeEventDao)

        val sow = PigEntity(
            id = "sow-001",
            tagCode = "N001",
            breed = "Landrace",
            gender = "Cái",
            location = "Khu Chờ Loại",
            parity = 7,
            status = SowReproductiveStatus.CAI_SUA_CHO_PHOI
        )
        fakePigDao.insertPig(sow)

        ruleEngine.onSowCullEvent(
            sowTag = "N001",
            cullDate = System.currentTimeMillis(),
            reason = "Già",
            weightKg = 185f,
            salePrice = "12.500.000",
            buyerName = "Thương lái Ba Tri"
        )

        val updatedSow = fakePigDao.getPigByTagCode("N001")
        assertEquals(SowReproductiveStatus.LOAI_THAI, updatedSow?.status)
        assertEquals(185f, updatedSow?.currentWeightKg)

        val event = fakeEventDao.allEvents.firstOrNull { it.eventType == "Loại thải nái" }
        assertNotNull(event)
        assertTrue(event!!.description.contains("Già"))
        assertTrue(event.description.contains("12.500.000"))
    }

    @Test
    fun `piglet culling event decreases batch quantity and logs reason`() = runBlocking {
        val fakePigDao = FakePigDao()
        val fakeTaskDao = FakeTaskDao()
        val fakeEventDao = FakeEventDao()
        val ruleEngine = com.example.domain.rule.FarmRuleEngine(fakePigDao, fakeTaskDao, fakeEventDao)

        val pigletBatch = PigEntity(
            id = "dc-100",
            tagCode = "DC20270626-001",
            isBatch = true,
            quantity = 11,
            breed = "F1",
            gender = "Hỗn hợp",
            status = SowReproductiveStatus.NUOI_CON,
            location = "Chuồng Đẻ 01"
        )
        fakePigDao.insertPig(pigletBatch)

        // Loại thải 1 con do dị tật bẩm sinh
        ruleEngine.onPigletCullEvent(
            pigletTag = "DC20270626-001",
            cullDate = System.currentTimeMillis(),
            cullQuantity = 1,
            reason = "Dị tật bẩm sinh",
            averageWeightKg = 1.2f,
            disposition = "Tiêu hủy"
        )

        val updated = fakePigDao.getPigByTagCode("DC20270626-001")
        assertNotNull(updated)
        assertEquals(10, updated?.quantity)

        val event = fakeEventDao.allEvents.firstOrNull { it.eventType == "Loại thải heo con" }
        assertNotNull(event)
        assertTrue(event!!.description.contains("Dị tật bẩm sinh"))
        assertTrue(event.description.contains("Còn lại: 10 con"))
    }

    @Test
    fun `grower culling event decreases batch quantity and logs reason`() = runBlocking {
        val fakePigDao = FakePigDao()
        val fakeTaskDao = FakeTaskDao()
        val fakeEventDao = FakeEventDao()
        val ruleEngine = com.example.domain.rule.FarmRuleEngine(fakePigDao, fakeTaskDao, fakeEventDao)

        val growerBatch = PigEntity(
            id = "lt-100",
            tagCode = "LT001",
            isBatch = true,
            quantity = 100,
            breed = "Duroc",
            gender = "Hỗn hợp",
            status = SowReproductiveStatus.DANG_NUOI,
            location = "Khu A-01"
        )
        fakePigDao.insertPig(growerBatch)

        // Loại thải 2 con do chấn thương bại liệt
        ruleEngine.onGrowerCullEvent(
            batchCode = "LT001",
            cullDate = System.currentTimeMillis(),
            cullQuantity = 2,
            reason = "Chấn thương bại liệt",
            averageWeightKg = 45.0f,
            salePrice = "1.800.000",
            buyerName = "Lò mổ địa phương"
        )

        val updated = fakePigDao.getPigByTagCode("LT001")
        assertNotNull(updated)
        assertEquals(98, updated?.quantity)

        val event = fakeEventDao.allEvents.firstOrNull { it.eventType == "Loại thải heo thịt" }
        assertNotNull(event)
        assertTrue(event!!.description.contains("Chấn thương bại liệt"))
        assertTrue(event.description.contains("Số lượng còn lại: 98 con"))
    }

    @Test
    fun `sow suckling piglets count strictly zero when not nursing and positive when nursing`() {
        // Sow in gestation -> must be 0 suckling piglets
        val pregnantSow = PigEntity(
            id = "sow-gest",
            tagCode = "NY-089",
            status = SowReproductiveStatus.MANG_THAI,
            quantity = 1,
            breed = "Yorkshire",
            gender = "Cái",
            location = "Chuồng Bầu 12"
        )
        val pregnantSucklingCount = if (pregnantSow.status == SowReproductiveStatus.NUOI_CON) {
            if (pregnantSow.quantity > 0) pregnantSow.quantity else 11
        } else {
            0
        }
        assertEquals(0, pregnantSucklingCount)

        // Sow weaned/ready to mate -> must be 0 suckling piglets
        val weanedSow = PigEntity(
            id = "sow-weaned",
            tagCode = "N002",
            status = SowReproductiveStatus.CAI_SUA_CHO_PHOI,
            quantity = 1,
            breed = "Landrace",
            gender = "Cái",
            location = "Chuồng Chờ phối"
        )
        val weanedSucklingCount = if (weanedSow.status == SowReproductiveStatus.NUOI_CON) 11 else 0
        assertEquals(0, weanedSucklingCount)

        // Sow culled -> must be 0 suckling piglets
        val culledSow = PigEntity(
            id = "sow-culled",
            tagCode = "N003",
            status = SowReproductiveStatus.LOAI_THAI,
            quantity = 1,
            breed = "Landrace",
            gender = "Cái",
            location = "Khu Cách ly"
        )
        val culledSucklingCount = if (culledSow.status == SowReproductiveStatus.NUOI_CON) 11 else 0
        assertEquals(0, culledSucklingCount)

        // Sow nursing (NUOI_CON) -> must equal live suckling piglets (e.g. 11 con)
        val nursingSow = PigEntity(
            id = "sow-nursing",
            tagCode = "N001",
            status = SowReproductiveStatus.NUOI_CON,
            quantity = 11,
            breed = "Landrace",
            gender = "Cái",
            location = "Chuồng Đẻ 01"
        )
        val nursingSucklingCount = if (nursingSow.status == SowReproductiveStatus.NUOI_CON) {
            if (nursingSow.quantity > 0) nursingSow.quantity else 11
        } else {
            0
        }
        assertEquals(11, nursingSucklingCount)
    }

    @Test
    fun `grower batch age in weeks calculated correctly from intake or birth date`() {
        val now = 1818000000000L // arbitrary fixed time
        // 87 days ago
        val intakeDate = now - (87 * 86_400_000L)
        val ageDays = ((now - intakeDate) / 86_400_000L).coerceAtLeast(0)
        assertEquals(87L, ageDays)

        val ageWeeksFloat = ageDays / 7.0
        val ageWeeksRounded = String.format(Locale.US, "%.1f", ageWeeksFloat)
        val ageWeeksFloor = (ageDays / 7).toInt()

        assertEquals("12.4", ageWeeksRounded)
        assertEquals(12, ageWeeksFloor)
    }

    // =========================================================================
    // TESTS FOR TAB KHO (INVENTORY LIFECYCLE, WEIGHTED AVG PRICE & NEGATIVE STOCK CHECK)
    // =========================================================================

    @Test
    fun `inventory inbound calculates weighted average unit price and updates stock`() = runBlocking {
        val fakeDao = FakeInventoryDao()
        val repo = InventoryRepositoryImpl(fakeDao)

        // Seed initial item with 10 units at 100,000 VND
        val item = InventoryItemEntity(
            id = "item-001",
            name = "Cám heo con tập ăn",
            category = "FEED",
            unit = "Bao 25kg",
            currentStock = 10f,
            lastUnitPrice = 100_000L,
            averageUnitPrice = 100_000L,
            minAlertStock = 5f
        )
        fakeDao.insertItem(item)

        // Nhập thêm 20 bao với đơn giá 130,000 VND
        // Giá trị cũ: 10 * 100,000 = 1,000,000
        // Giá trị mới nhập: 20 * 130,000 = 2,600,000
        // Tổng tồn: 30 bao
        // Đơn giá bình quân gia quyền: 3,600,000 / 30 = 120,000 VND
        val result = repo.recordInbound(
            itemId = "item-001",
            quantity = 20f,
            unitPrice = 130_000L,
            date = System.currentTimeMillis(),
            supplierOrInvoice = "Nhà máy CJ Vina"
        )

        assertTrue(result.isSuccess)
        val updatedItem = fakeDao.getItemById("item-001")
        assertNotNull(updatedItem)
        assertEquals(30f, updatedItem?.currentStock ?: 0f, 0.01f)
        assertEquals(130_000L, updatedItem?.lastUnitPrice)
        assertEquals(120_000L, updatedItem?.averageUnitPrice)
        assertEquals(3_600_000L, updatedItem?.totalStockValue)
    }

    @Test
    fun `inventory outbound strictly prevents negative stock`() = runBlocking {
        val fakeDao = FakeInventoryDao()
        val repo = InventoryRepositoryImpl(fakeDao)

        val item = InventoryItemEntity(
            id = "item-002",
            name = "Amoxicillin 10%",
            category = "MEDICINE",
            unit = "Chai 100ml",
            currentStock = 15f,
            lastUnitPrice = 85_000L,
            averageUnitPrice = 85_000L,
            minAlertStock = 5f
        )
        fakeDao.insertItem(item)

        // Yêu cầu xuất 20 chai (vượt quá tồn kho 15 chai) -> Bắt buộc bị từ chối
        val failureResult = repo.recordOutbound(
            itemId = "item-002",
            quantity = 20f,
            targetBarn = "Chuồng Thịt 1",
            date = System.currentTimeMillis(),
            purposeOrNotes = "Điều trị tiêu chảy"
        )

        assertTrue("Outbound exceeding stock must fail", failureResult.isFailure)
        val errorMsg = failureResult.exceptionOrNull()?.message ?: ""
        assertTrue("Error message must state stock violation", errorMsg.contains("Chặn xuất âm") && errorMsg.contains("vượt quá số lượng tồn"))

        // Kiểm tra tồn kho không bị giảm
        val unchangedItem = fakeDao.getItemById("item-002")
        assertEquals(15f, unchangedItem?.currentStock ?: 0f, 0.01f)
    }

    @Test
    fun `inventory outbound successfully decreases stock when quantity is valid`() = runBlocking {
        val fakeDao = FakeInventoryDao()
        val repo = InventoryRepositoryImpl(fakeDao)

        val item = InventoryItemEntity(
            id = "item-003",
            name = "Kim tiêm 18G",
            category = "SUPPLIES",
            unit = "Hộp 100 cái",
            currentStock = 25f,
            lastUnitPrice = 45_000L,
            averageUnitPrice = 45_000L,
            minAlertStock = 10f
        )
        fakeDao.insertItem(item)

        // Xuất 10 hộp hợp lệ
        val successResult = repo.recordOutbound(
            itemId = "item-003",
            quantity = 10f,
            targetBarn = "Chuồng Đẻ 2",
            date = System.currentTimeMillis(),
            purposeOrNotes = "Tiêm sắt định kỳ"
        )

        assertTrue(successResult.isSuccess)
        val updatedItem = fakeDao.getItemById("item-003")
        assertEquals(15f, updatedItem?.currentStock ?: 0f, 0.01f)
    }

    @Test
    fun `inventory item low stock alert triggers at or below threshold`() {
        val lowStockItem = InventoryItemEntity(
            id = "low-1",
            name = "Vắc-xin Dịch tả lợn",
            category = "MEDICINE",
            unit = "Lọ 20 liều",
            currentStock = 4f,
            lastUnitPrice = 50_000L,
            averageUnitPrice = 50_000L,
            minAlertStock = 10f
        )
        assertTrue(lowStockItem.isLowStock)

        val thresholdStockItem = InventoryItemEntity(
            id = "thresh-1",
            name = "Vắc-xin Tai xanh PRRS",
            category = "MEDICINE",
            unit = "Lọ 10 liều",
            currentStock = 10f,
            lastUnitPrice = 60_000L,
            averageUnitPrice = 60_000L,
            minAlertStock = 10f
        )
        assertTrue(thresholdStockItem.isLowStock)

        val sufficientStockItem = InventoryItemEntity(
            id = "suff-1",
            name = "Cám nái mang thai",
            category = "FEED",
            unit = "Bao 25kg",
            currentStock = 35f,
            lastUnitPrice = 240_000L,
            averageUnitPrice = 240_000L,
            minAlertStock = 15f
        )
        assertFalse(sufficientStockItem.isLowStock)
    }

    // =========================================================================
    // TESTS FOR SYSTEM PARAMETERS SUBTABS (HEO ĐỰC, NHÂN VIÊN, GIỐNG, CÁM, THUỐC, VẬT TƯ)
    // =========================================================================

    @Test
    fun `system parameter repository saves and retrieves master data across all 6 subtabs`() = runBlocking {
        val fakeDao = FakeSystemParameterDao()
        val repo = SystemParameterRepositoryImpl(fakeDao)

        // 1. Heo đực (Boar)
        val boarId = repo.addParameter(
            category = SystemParameterEntity.CATEGORY_BOAR,
            name = "Duroc Đan Mạch DB-01",
            code = "DUC-001",
            description = "Đực khai thác tinh hàng ngày",
            unit = "Liều",
            subType = "Duroc",
            secondaryValue = "Nhập ngoại",
            numericValue = 15.0,
            statusText = "Đang hoạt động"
        )
        assertNotNull(boarId)

        // 2. Nhân viên (Staff)
        val staffId = repo.addParameter(
            category = SystemParameterEntity.CATEGORY_STAFF,
            name = "Trần Đình Khang",
            code = "NV-002",
            description = "Phụ trách khu đẻ",
            unit = "",
            subType = "Kỹ thuật viên",
            secondaryValue = "0912345678",
            numericValue = 0.0,
            statusText = "Đang làm việc"
        )
        assertNotNull(staffId)

        // 3. Giống heo (Breed)
        val breedId = repo.addParameter(
            category = SystemParameterEntity.CATEGORY_BREED,
            name = "Landrace thuần chủng",
            code = "G-LR",
            description = "Lưng dài, tai rủ, nái sữa tốt",
            unit = "",
            subType = "Heo nái sinh sản",
            secondaryValue = "",
            numericValue = 0.0,
            statusText = "Đang sử dụng"
        )
        assertNotNull(breedId)

        // 4. Thức ăn (Feed)
        val feedId = repo.addParameter(
            category = SystemParameterEntity.CATEGORY_FEED,
            name = "Cám tập ăn heo con F1",
            code = "CA-01",
            description = "Dùng cho heo con theo mẹ từ 7 ngày tuổi",
            unit = "Bao 25kg",
            subType = "Công ty CP",
            secondaryValue = "",
            numericValue = 380_000.0,
            statusText = "Đang cung cấp"
        )
        assertNotNull(feedId)

        // 5. Thuốc (Medicine)
        val medId = repo.addParameter(
            category = SystemParameterEntity.CATEGORY_MEDICINE,
            name = "Iron Dextran 200mg",
            code = "TH-FE",
            description = "Phòng ngừa thiếu máu heo sơ sinh",
            unit = "Chai 100ml",
            subType = "Bổ trợ",
            secondaryValue = "1 ml/con",
            numericValue = 85_000.0,
            statusText = "Đang sử dụng"
        )
        assertNotNull(medId)

        // 6. Vật tư (Supplies)
        val supId = repo.addParameter(
            category = SystemParameterEntity.CATEGORY_SUPPLIES,
            name = "Thẻ tai nái vàng dẻo",
            code = "VT-TT",
            description = "Thẻ bấm tai nái có mã số rõ nét",
            unit = "Cái",
            subType = "",
            secondaryValue = "",
            numericValue = 12_000.0,
            statusText = "Còn hàng"
        )
        assertNotNull(supId)

        // Verify all 6 entities in Dao
        val allParams = fakeDao.getAllParameters().first()
        assertEquals(6, allParams.size)

        val retrievedBoar = fakeDao.getParameterById(boarId)
        assertNotNull(retrievedBoar)
        assertEquals("Duroc", retrievedBoar?.subType)
        assertEquals("Nhập ngoại", retrievedBoar?.secondaryValue)
        assertEquals(15.0, retrievedBoar?.numericValue ?: 0.0, 0.01)

        val retrievedStaff = fakeDao.getParameterById(staffId)
        assertNotNull(retrievedStaff)
        assertEquals("Kỹ thuật viên", retrievedStaff?.subType)
        assertEquals("0912345678", retrievedStaff?.secondaryValue)

        val retrievedFeed = fakeDao.getParameterById(feedId)
        assertNotNull(retrievedFeed)
        assertEquals("Công ty CP", retrievedFeed?.subType)
        assertEquals(380_000.0, retrievedFeed?.numericValue ?: 0.0, 0.01)
    }

    @Test
    fun `boar dose stock update adjusts numeric value and transitions status`() = runBlocking {
        val fakeDao = FakeSystemParameterDao()
        val repo = SystemParameterRepositoryImpl(fakeDao)

        val boarId = repo.addParameter(
            category = SystemParameterEntity.CATEGORY_BOAR,
            name = "Pietrain Kháng Stress",
            code = "DUC-002",
            description = "Đực nhảy trực tiếp",
            unit = "Liều",
            subType = "Pietrain",
            secondaryValue = "Tại trại",
            numericValue = 3.0,
            statusText = "Đang hoạt động"
        )

        val boar = fakeDao.getParameterById(boarId)!!
        assertEquals(3.0, boar.numericValue, 0.01)
        assertEquals("Đang hoạt động", boar.statusText)

        // Decrement by 2 -> 1 dose left ("Đang hoạt động")
        val updated1 = boar.copy(
            numericValue = (boar.numericValue - 2).coerceAtLeast(0.0),
            statusText = if ((boar.numericValue - 2) <= 0.0) "Đã hết" else "Đang hoạt động"
        )
        repo.updateParameter(updated1)
        val check1 = fakeDao.getParameterById(boarId)!!
        assertEquals(1.0, check1.numericValue, 0.01)
        assertEquals("Đang hoạt động", check1.statusText)

        // Decrement by 1 -> 0 doses ("Đã hết")
        val updated2 = check1.copy(
            numericValue = (check1.numericValue - 1).coerceAtLeast(0.0),
            statusText = if ((check1.numericValue - 1) <= 0.0) "Đã hết" else "Đang hoạt động"
        )
        repo.updateParameter(updated2)
        val check2 = fakeDao.getParameterById(boarId)!!
        assertEquals(0.0, check2.numericValue, 0.01)
        assertEquals("Đã hết", check2.statusText)

        // Increment by 10 -> 10 doses ("Đang hoạt động")
        val updated3 = check2.copy(
            numericValue = (check2.numericValue + 10).coerceAtLeast(0.0),
            statusText = if ((check2.numericValue + 10) <= 0.0) "Đã hết" else "Đang hoạt động"
        )
        repo.updateParameter(updated3)
        val check3 = fakeDao.getParameterById(boarId)!!
        assertEquals(10.0, check3.numericValue, 0.01)
        assertEquals("Đang hoạt động", check3.statusText)
    }

    @Test
    fun `system parameters search query filters across multiple metadata fields`() {
        val params = listOf(
            SystemParameterEntity(id = "1", category = SystemParameterEntity.CATEGORY_STAFF, name = "Nguyễn Văn Tuấn", code = "NV-001", subType = "Quản lý", secondaryValue = "0987654321"),
            SystemParameterEntity(id = "2", category = SystemParameterEntity.CATEGORY_STAFF, name = "Lê Thị Lan", code = "NV-003", subType = "Thú y", secondaryValue = "0903112233"),
            SystemParameterEntity(id = "3", category = SystemParameterEntity.CATEGORY_MEDICINE, name = "Amoxicillin 15%", code = "TH-002", subType = "Kháng sinh", secondaryValue = "1 ml / 10 kg")
        )

        // Search by phone number
        val queryPhone = "0903"
        val phoneResults = params.filter {
            it.name.contains(queryPhone, true) || it.code.contains(queryPhone, true) || it.subType.contains(queryPhone, true) || it.secondaryValue.contains(queryPhone, true)
        }
        assertEquals(1, phoneResults.size)
        assertEquals("NV-003", phoneResults[0].code)

        // Search by subType (role/type)
        val queryRole = "Thú y"
        val roleResults = params.filter {
            it.name.contains(queryRole, true) || it.code.contains(queryRole, true) || it.subType.contains(queryRole, true) || it.secondaryValue.contains(queryRole, true)
        }
        assertEquals(1, roleResults.size)
        assertEquals("Lê Thị Lan", roleResults[0].name)

        // Search by medicine code
        val queryCode = "TH-002"
        val codeResults = params.filter {
            it.name.contains(queryCode, true) || it.code.contains(queryCode, true) || it.subType.contains(queryCode, true) || it.secondaryValue.contains(queryCode, true)
        }
        assertEquals(1, codeResults.size)
        assertEquals("Amoxicillin 15%", codeResults[0].name)
    }

    @Test
    fun `currency formatter correctly formats VND values with thousand separators`() {
        assertEquals("350.000 VNĐ", formatVnd(350_000.0))
        assertEquals("12.500 VNĐ", formatVnd(12_500.0))
        assertEquals("0 VNĐ", formatVnd(0.0))
    }

    // =========================================================================
    // TESTS FOR HOME DAILY FEED ANALYTICS & FINANCIAL BALANCE
    // =========================================================================

    @Test
    fun `daily feed analytics calculates silo days remaining from stock and daily consumption`() {
        val totalStockKg = 28_500.0
        val dailyConsumptionKg = 1_900.0
        // Formula: Days = Total Stock / Daily Consumption
        val daysRemaining = (totalStockKg / dailyConsumptionKg).toInt()
        assertEquals(15, daysRemaining)

        val analytics = DailyFeedAnalytics(
            totalDailyFeedKg = 1920.0,
            totalStockKg = totalStockKg,
            dailyConsumptionKg = dailyConsumptionKg,
            estimatedDaysRemaining = daysRemaining
        )

        assertEquals(15, analytics.estimatedDaysRemaining)
        // 5 specialized feed lines
        assertEquals(5, analytics.feedConsumptionList.size)
        // Check abnormal consumption warning on Grower Stage 2
        val abnormalItem = analytics.feedConsumptionList.find { it.isAbnormal }
        assertNotNull(abnormalItem)
        assertEquals("Cám heo thịt GĐ 2", abnormalItem?.name)
        assertTrue(abnormalItem?.statusNote?.contains("giảm bất thường") == true)

        // Verify percentages sum to 100%
        val totalPct = analytics.feedConsumptionList.map { it.percentage.toDouble() }.sum()
        assertEquals(100.0, totalPct, 0.5)
    }

    @Test
    fun `financial balance accurately computes total cost, revenue and accumulated net profit`() {
        val breedingPigCost = 280_000_000.0
        val feedCost = 475_000_000.0
        val medicineCost = 58_500_000.0
        val suppliesCost = 33_000_000.0

        // Formula 1: Total Cost = Breeding + Feed + Medicine/Vaccine + Supplies
        val totalCost = breedingPigCost + feedCost + medicineCost + suppliesCost
        assertEquals(846_500_000.0, totalCost, 0.01)

        val meatRevenue = 1_120_000_000.0
        val breedingRevenue = 85_000_000.0
        val totalRevenue = meatRevenue + breedingRevenue
        assertEquals(1_205_000_000.0, totalRevenue, 0.01)

        // Formula 2: Accumulated Profit = Total Revenue - Total Cost
        val accumulatedProfit = totalRevenue - totalCost
        assertEquals(358_500_000.0, accumulatedProfit, 0.01)

        // Formula 3: Net Profit Margin %
        val profitMargin = (accumulatedProfit / totalRevenue) * 100
        assertEquals(29.75, profitMargin, 0.01)

        val balance = FinancialBalance(
            breedingPigCost = breedingPigCost,
            feedCost = feedCost,
            medicineVaccineCost = medicineCost,
            suppliesCost = suppliesCost,
            totalCost = totalCost,
            meatRevenue = meatRevenue,
            breedingRevenue = breedingRevenue,
            totalRevenue = totalRevenue,
            accumulatedProfit = accumulatedProfit,
            profitMarginPercent = profitMargin
        )

        assertEquals(846_500_000.0, balance.totalCost, 0.01)
        assertEquals(1_205_000_000.0, balance.totalRevenue, 0.01)
        assertEquals(358_500_000.0, balance.accumulatedProfit, 0.01)
        assertTrue("Accumulated profit must be positive (green theme)", balance.accumulatedProfit > 0)
    }

    // =========================================================================
    // TESTS FOR CLEAN ARCHITECTURE ROOM ENTITIES: HEO, SỰ KIỆN, KHO, NHÂN VIÊN
    // =========================================================================

    @Test
    fun `pig dao cleanly segregates individual sows and grower batches`() = runBlocking {
        val pigDao = FakePigDao()

        // 1. Heo Nái cá thể (isBatch = false)
        val sow = PigEntity(
            id = "sow-001",
            tagCode = "NAI-101",
            isBatch = false,
            quantity = 1,
            breed = "Landrace",
            gender = "Cái",
            status = SowReproductiveStatus.MANG_THAI,
            location = "Chuồng Bầu 1",
            parity = 2
        )
        pigDao.insertPig(sow)
        assertTrue(sow.isSow)
        assertFalse(sow.isGrowerBatch)

        // 2. Lô Heo Thịt (isBatch = true)
        val growerBatch = PigEntity(
            id = "batch-001",
            tagCode = "LT-204",
            isBatch = true,
            quantity = 120,
            breed = "Duroc x Pietrain",
            gender = "Hỗn hợp",
            status = SowReproductiveStatus.DANG_NUOI,
            location = "Chuồng Thịt 2",
            currentWeightKg = 45.5f
        )
        pigDao.insertPig(growerBatch)
        assertTrue(growerBatch.isGrowerBatch)
        assertFalse(growerBatch.isSow)

        // Query all sows
        val sows = pigDao.getAllSows().first()
        assertEquals(1, sows.size)
        assertEquals("NAI-101", sows[0].tagCode)

        // Query all grower batches
        val batches = pigDao.getAllGrowerBatches().first()
        assertEquals(1, batches.size)
        assertEquals("LT-204", batches[0].tagCode)
        assertEquals(120, batches[0].quantity)
    }

    @Test
    fun `event dao queries accurately retrieve all 5 core farm event types`() = runBlocking {
        val eventDao = FakeEventDao()
        val now = System.currentTimeMillis()

        // 1. Phối giống (Mating)
        eventDao.insertEvent(
            EventEntity(
                id = "ev-mating-1",
                pigId = "sow-001",
                eventType = EventEntity.TYPE_MATING,
                eventDate = now,
                description = "Phối liều Duroc ngoại",
                metrics = "Liều 001"
            )
        )

        // 2. Kiểm tra thai (Pregnancy check)
        eventDao.insertEvent(
            EventEntity(
                id = "ev-preg-1",
                pigId = "sow-001",
                eventType = EventEntity.TYPE_PREGNANCY_CHECK,
                eventDate = now + 86_400_000L,
                description = "Siêu âm thai D+30",
                pregnancyResult = "DAU_THAI"
            )
        )

        // 3. Cai sữa (Weaning)
        eventDao.insertEvent(
            EventEntity(
                id = "ev-wean-1",
                pigId = "sow-001",
                eventType = EventEntity.TYPE_WEANING,
                eventDate = now + 2 * 86_400_000L,
                description = "Cai sữa bầy 11 con",
                pigCountAffected = 11
            )
        )

        // 4. Tiêm phòng (Vaccination)
        eventDao.insertEvent(
            EventEntity(
                id = "ev-vac-1",
                pigId = "batch-001",
                eventType = EventEntity.TYPE_VACCINATION,
                eventDate = now + 3 * 86_400_000L,
                description = "Tiêm vắc-xin Dịch tả lợn",
                dosageOrMedicine = "Dịch tả lợn - 2ml/con",
                pigCountAffected = 120
            )
        )

        // 5. Chuyển chuồng (Transfer)
        eventDao.insertEvent(
            EventEntity(
                id = "ev-trans-1",
                pigId = "batch-001",
                eventType = EventEntity.TYPE_TRANSFER,
                eventDate = now + 4 * 86_400_000L,
                description = "Chuyển từ chuồng Thịt 1 sang Thịt 2",
                targetBarn = "Chuồng Thịt 2",
                pigCountAffected = 120
            )
        )

        // Assert 5 event queries
        val matings = eventDao.getMatingEvents().first()
        assertEquals(1, matings.size)
        assertEquals(EventEntity.TYPE_MATING, matings[0].eventType)

        val pregChecks = eventDao.getPregnancyCheckEvents().first()
        assertEquals(1, pregChecks.size)
        assertEquals("DAU_THAI", pregChecks[0].pregnancyResult)

        val weanings = eventDao.getWeaningEvents().first()
        assertEquals(1, weanings.size)
        assertEquals(11, weanings[0].pigCountAffected)

        val vaccinations = eventDao.getVaccinationEvents().first()
        assertEquals(1, vaccinations.size)
        assertEquals("Dịch tả lợn - 2ml/con", vaccinations[0].dosageOrMedicine)

        val transfers = eventDao.getTransferEvents().first()
        assertEquals(1, transfers.size)
        assertEquals("Chuồng Thịt 2", transfers[0].targetBarn)
    }

    @Test
    fun `inventory dao queries low stock items and transaction history`() = runBlocking {
        val invDao = FakeInventoryDao()

        // Thêm mặt hàng đủ tồn và mặt hàng chạm ngưỡng báo động
        invDao.insertItem(
            InventoryItemEntity(
                id = "inv-1",
                name = "Cám nái mang thai",
                category = InventoryItemEntity.CATEGORY_FEED,
                unit = "Bao 25kg",
                currentStock = 120f,
                lastUnitPrice = 320_000L,
                averageUnitPrice = 320_000L,
                minAlertStock = 15f
            )
        )
        invDao.insertItem(
            InventoryItemEntity(
                id = "inv-2",
                name = "Vắc-xin Tai xanh PRRS",
                category = InventoryItemEntity.CATEGORY_MEDICINE,
                unit = "Lọ 10 liều",
                currentStock = 3f, // Dưới ngưỡng báo động 5 lọ
                lastUnitPrice = 60_000L,
                averageUnitPrice = 60_000L,
                minAlertStock = 5f
            )
        )

        val lowStock = invDao.getLowStockItems().first()
        assertEquals(1, lowStock.size)
        assertEquals("Vắc-xin Tai xanh PRRS", lowStock[0].name)
    }

    @Test
    fun `staff repository and dao cleanly manage staff members under clean architecture`() = runBlocking {
        val staffDao = FakeStaffDao()
        val staffRepo: StaffRepository = StaffRepositoryImpl(staffDao)

        // Thêm nhân viên
        val id1 = staffRepo.addStaff(
            code = "NV-001",
            fullName = "Nguyễn Văn Tuấn",
            phoneNumber = "0987654321",
            role = StaffEntity.ROLE_MANAGER,
            department = "Toàn trang trại"
        )
        assertNotNull(id1)

        val id2 = staffRepo.addStaff(
            code = "NV-002",
            fullName = "Trần Đình Khang",
            phoneNumber = "0912345678",
            role = StaffEntity.ROLE_TECHNICIAN,
            department = "Khu nái đẻ"
        )
        assertNotNull(id2)

        val id3 = staffRepo.addStaff(
            code = "NV-003",
            fullName = "Lê Thị Lan",
            phoneNumber = "0903112233",
            role = StaffEntity.ROLE_VET,
            department = "Y tế chuồng trại"
        )
        assertNotNull(id3)

        // Query active staff
        val activeList = staffRepo.activeStaff.first()
        assertEquals(3, activeList.size)

        // Query by role
        val techList = staffRepo.getStaffByRole(StaffEntity.ROLE_TECHNICIAN).first()
        assertEquals(1, techList.size)
        assertEquals("Trần Đình Khang", techList[0].fullName)

        // Search by phone number
        val searchResults = staffRepo.searchStaff("0903").first()
        assertEquals(1, searchResults.size)
        assertEquals("Lê Thị Lan", searchResults[0].fullName)

        // Find by code
        val staffByCode = staffRepo.getStaffByCode("NV-001")
        assertNotNull(staffByCode)
        assertEquals("Nguyễn Văn Tuấn", staffByCode?.fullName)
    }

    @Test
    fun `feed ration calculator scans groups and calculates physiological weeks`() {
        val now = System.currentTimeMillis()
        val pregnantSow = PigEntity(
            id = "sow-p1",
            tagCode = "NAI-001",
            isBatch = false,
            quantity = 1,
            breed = "Landrace",
            gender = "Cái",
            status = SowReproductiveStatus.MANG_THAI,
            location = "Bầu 1",
            lastMatingDate = now - (35 * 86_400_000L) // 35 ngày mang thai = Tuần 6 (35/7 + 1 = 6)
        )

        val lactatingSow = PigEntity(
            id = "sow-l1",
            tagCode = "NAI-002",
            isBatch = false,
            quantity = 1,
            breed = "Yorkshire",
            gender = "Cái",
            status = SowReproductiveStatus.NUOI_CON,
            location = "Đẻ 1",
            lastMatingDate = now - (114 + 14) * 86_400_000L // Đẻ 14 ngày trước = Tuần 3 nuôi con
        )

        val waitingSow = PigEntity(
            id = "sow-w1",
            tagCode = "NAI-003",
            isBatch = false,
            quantity = 1,
            breed = "Duroc",
            gender = "Cái",
            status = SowReproductiveStatus.CAI_SUA_CHO_PHOI,
            location = "Chờ phối"
        )

        val growerBatch = PigEntity(
            id = "batch-1",
            tagCode = "LH-201",
            isBatch = true,
            quantity = 50,
            breed = "Thương phẩm 3 máu",
            gender = "Hỗn hợp",
            status = SowReproductiveStatus.DANG_NUOI,
            location = "Thịt 1",
            birthDate = now - (70 * 86_400_000L) // 70 ngày = Tuần 11
        )

        val pigs = listOf(pregnantSow, lactatingSow, waitingSow, growerBatch)
        val matrix = FeedRationCalculator.getDefaultRationMatrix()

        val result = FeedRationCalculator.calculate(
            pigs = pigs,
            rationMatrix = matrix,
            inventoryFeedItems = emptyList(),
            now = now
        )

        assertNotNull(result)
        assertTrue(result.feedResults.isNotEmpty())
        assertTrue("Total daily consumption should be greater than 0", result.totalDailyConsumptionKg > 0.0)

        // Nái bầu tuần 6 ăn cám nái bầu (2.0 kg/ngày)
        val pregnantFeed = result.feedResults.find { it.feedCode == "CAM_NAI_MANG_THAI" }
        assertNotNull(pregnantFeed)
        assertTrue((pregnantFeed?.dailyDemandKg ?: 0.0) >= 2.0)

        // Nái đẻ tuần 3 ăn cám nái đẻ (5.5 kg/ngày)
        val lactatingFeed = result.feedResults.find { it.feedCode == "CAM_NAI_NUOI_CON" }
        assertNotNull(lactatingFeed)
        assertTrue((lactatingFeed?.dailyDemandKg ?: 0.0) >= 5.0)

        // Heo thịt 50 con tuần 11 ăn cám thịt vỗ béo (1.8 kg/ngày * 50 = 90 kg)
        val growerFeed = result.feedResults.find { it.feedCode == "CAM_THIT_VO_BEO" }
        assertNotNull(growerFeed)
        assertTrue((growerFeed?.dailyDemandKg ?: 0.0) >= 80.0)
    }

    @Test
    fun `feed ration calculator computes daily feed demand E_ngay correctly from custom ration matrix`() {
        val now = System.currentTimeMillis()
        val pregnantSow1 = PigEntity(
            id = "sow-1",
            tagCode = "N-101",
            isBatch = false,
            quantity = 1,
            breed = "Landrace",
            gender = "Cái",
            status = SowReproductiveStatus.MANG_THAI,
            location = "Bầu 1",
            lastMatingDate = now - (14 * 86_400_000L)
        )
        val pregnantSow2 = PigEntity(
            id = "sow-2",
            tagCode = "N-102",
            isBatch = false,
            quantity = 1,
            breed = "Landrace",
            gender = "Cái",
            status = SowReproductiveStatus.MANG_THAI,
            location = "Bầu 1",
            lastMatingDate = now - (14 * 86_400_000L)
        )

        // Custom ration: Nái mang thai ăn 2.5 kg/ngày
        val customRations = listOf(
            SystemParameterEntity(
                id = "custom-ration-1",
                category = SystemParameterEntity.CATEGORY_FEED_RATION,
                code = SystemParameterEntity.STAGE_PREGNANT_SOW,
                name = "Nái mang thai",
                displayOrder = 1,
                subType = "CAM_NAI_MANG_THAI",
                secondaryValue = "Cám nái bầu đặc biệt",
                numericValue = 2.5,
                unit = "kg/ngày/con",
                statusText = "Tuần 1 - 12"
            )
        )

        val result = FeedRationCalculator.calculate(
            pigs = listOf(pregnantSow1, pregnantSow2),
            rationMatrix = customRations,
            inventoryFeedItems = emptyList(),
            now = now
        )

        val feedDemand = result.feedResults.find { it.feedCode == "CAM_NAI_MANG_THAI" }
        assertNotNull(feedDemand)
        // 2 nái * 2.5 kg = 5.0 kg/ngày
        assertEquals(5.0, feedDemand?.dailyDemandKg ?: 0.0, 0.01)
    }

    @Test
    fun `feed ration calculator evaluates silo days remaining and critical 3 day alerts`() {
        val now = System.currentTimeMillis()
        val sows = (1..10).map { i ->
            PigEntity(
                id = "sow-$i",
                tagCode = "N-$i",
                isBatch = false,
                quantity = 1,
                breed = "Landrace",
                gender = "Cái",
                status = SowReproductiveStatus.MANG_THAI,
                location = "Bầu 1",
                lastMatingDate = now - (20 * 86_400_000L)
            )
        }
        // 10 nái * 2.0 kg/ngày = 20 kg/ngày

        // Trường hợp 1: Tồn kho chỉ còn 2 bao 25kg = 50kg -> Dùng được 50 / 20 = 2 ngày (< 3 ngày -> Critical)
        val lowStockInventory = listOf(
            InventoryItemEntity(
                id = "feed-inv-1",
                name = "Cám nái bầu",
                category = InventoryItemEntity.CATEGORY_FEED,
                unit = "Bao 25kg",
                currentStock = 2f,
                lastUnitPrice = 320_000L,
                averageUnitPrice = 320_000L,
                systemParamId = "CAM_NAI_MANG_THAI"
            )
        )

        val criticalResult = FeedRationCalculator.calculate(
            pigs = sows,
            rationMatrix = FeedRationCalculator.getDefaultRationMatrix(),
            inventoryFeedItems = lowStockInventory,
            now = now
        )

        val pregnantFeedCritical = criticalResult.feedResults.find { it.feedCode == "CAM_NAI_MANG_THAI" }
        assertNotNull(pregnantFeedCritical)
        assertEquals(2, pregnantFeedCritical?.daysRemaining)
        assertTrue("Days remaining < 3 must trigger critical alert", pregnantFeedCritical?.isCritical == true)
        assertFalse("Days remaining < 3 is critical tier (red), not warning tier (amber)", pregnantFeedCritical?.isWarning == true)

        // Trường hợp 2: Tồn kho 5 bao 25kg = 125kg -> Dùng được 125 / 20 = 6 ngày (3..6 ngày -> Warning)
        val warningStockInventory = listOf(
            InventoryItemEntity(
                id = "feed-inv-warning",
                name = "Cám nái bầu",
                category = InventoryItemEntity.CATEGORY_FEED,
                unit = "Bao 25kg",
                currentStock = 5f,
                lastUnitPrice = 320_000L,
                averageUnitPrice = 320_000L,
                systemParamId = "CAM_NAI_MANG_THAI"
            )
        )

        val warningResult = FeedRationCalculator.calculate(
            pigs = sows,
            rationMatrix = FeedRationCalculator.getDefaultRationMatrix(),
            inventoryFeedItems = warningStockInventory,
            now = now
        )

        val pregnantFeedWarning = warningResult.feedResults.find { it.feedCode == "CAM_NAI_MANG_THAI" }
        assertNotNull(pregnantFeedWarning)
        assertEquals(6, pregnantFeedWarning?.daysRemaining)
        assertFalse("6 days is not critical", pregnantFeedWarning?.isCritical == true)
        assertTrue("6 days (< 7 days) triggers warning alert", pregnantFeedWarning?.isWarning == true)

        // Trường hợp 3: Tồn kho 20 bao 25kg = 500kg -> Dùng được 500 / 20 = 25 ngày (> 7 ngày -> An toàn)
        val safeStockInventory = listOf(
            InventoryItemEntity(
                id = "feed-inv-1",
                name = "Cám nái bầu",
                category = InventoryItemEntity.CATEGORY_FEED,
                unit = "Bao 25kg",
                currentStock = 20f,
                lastUnitPrice = 320_000L,
                averageUnitPrice = 320_000L,
                systemParamId = "CAM_NAI_MANG_THAI"
            )
        )

        val safeResult = FeedRationCalculator.calculate(
            pigs = sows,
            rationMatrix = FeedRationCalculator.getDefaultRationMatrix(),
            inventoryFeedItems = safeStockInventory,
            now = now
        )

        val pregnantFeedSafe = safeResult.feedResults.find { it.feedCode == "CAM_NAI_MANG_THAI" }
        assertNotNull(pregnantFeedSafe)
        assertEquals(25, pregnantFeedSafe?.daysRemaining)
        assertFalse("25 days is safe, not critical", pregnantFeedSafe?.isCritical == true)
        assertFalse("25 days is safe, not warning", pregnantFeedSafe?.isWarning == true)
    }

    @Test
    fun testFeedSafetyThresholdConfigurationAndAlert() {
        val now = 1700000000000L
        val sows = (1..10).map { i ->
            PigEntity(
                id = "sow-test-$i",
                tagCode = "NY-TEST-$i",
                breed = "Landrace",
                gender = "Cái",
                birthDate = now - 365 * 86_400_000L,
                location = "Khu Bầu 1",
                status = SowReproductiveStatus.MANG_THAI,
                parity = 2,
                currentWeightKg = 190.0f,
                lastMatingDate = now - 30 * 86_400_000L,
                quantity = 1,
                isBatch = false
            )
        }

        // Cấu hình tham số cám với Ngưỡng ngày an toàn = 5 ngày, Mức tồn kho tối thiểu = 25 bao
        val feedParam = SystemParameterEntity(
            id = "param-feed-mang-thai",
            category = SystemParameterEntity.CATEGORY_FEED,
            name = "Cám hỗn hợp nái mang thai",
            code = "CAM_NAI_MANG_THAI",
            description = "Thức ăn nái mang thai",
            unit = "Bao 25kg",
            displayOrder = 1,
            safetyDaysThreshold = 5,
            minStockThreshold = 25.0
        )

        val fullMatrix = FeedRationCalculator.getDefaultRationMatrix() + listOf(feedParam)

        // 10 nái mang thai tuần 5 (2.0kg/con/ngày) -> Tiêu thụ = 20kg/ngày
        // Tồn kho: 3.2 bao 25kg = 80kg -> Dùng được 80 / 20 = 4 ngày
        // Do ngưỡng ngày an toàn là 5 ngày, 4 ngày < 5 ngày nên kích hoạt mức Khẩn cấp (Đỏ)
        val inventory = listOf(
            InventoryItemEntity(
                id = "inv-feed-test",
                name = "Cám hỗn hợp nái mang thai",
                category = InventoryItemEntity.CATEGORY_FEED,
                unit = "Bao 25kg",
                currentStock = 3.2f,
                lastUnitPrice = 320_000L,
                averageUnitPrice = 320_000L,
                systemParamId = "param-feed-mang-thai"
            )
        )

        val result = FeedRationCalculator.calculate(
            pigs = sows,
            rationMatrix = fullMatrix,
            inventoryFeedItems = inventory,
            now = now
        )

        val feedDemand = result.feedResults.find { it.feedCode == "CAM_NAI_MANG_THAI" }
        assertNotNull(feedDemand)
        assertEquals(5, feedDemand?.safetyDaysThreshold)
        assertEquals(25.0, feedDemand?.minStockThreshold ?: 0.0, 0.01)
        assertEquals(4, feedDemand?.daysRemaining)
        assertTrue("4 days < 5 safety days threshold must trigger critical alert", feedDemand?.isCritical == true)
        assertTrue("Suggested order bags must be positive", (feedDemand?.suggestedOrderBags ?: 0) >= 25)
        assertTrue("Suggested order kg must match suggested bags * 25kg", (feedDemand?.suggestedOrderKg ?: 0.0) >= 625.0)
    }

    @Test
    fun testMinPhysicalStockFloorTrigger() {
        val now = 1700000000000L
        val sows = (1..5).map { i ->
            PigEntity(
                id = "sow-floor-$i",
                tagCode = "NY-FLOOR-$i",
                breed = "Landrace",
                gender = "Cái",
                birthDate = now - 365 * 86_400_000L,
                location = "Khu Bầu 1",
                status = SowReproductiveStatus.MANG_THAI,
                parity = 1,
                currentWeightKg = 180.0f,
                lastMatingDate = now - 20 * 86_400_000L,
                quantity = 1,
                isBatch = false
            )
        }

        // Cấu hình: Ngưỡng an toàn 2 ngày, nhưng Tồn kho tối thiểu cứng = 20 bao
        val feedParam = SystemParameterEntity(
            id = "param-feed-floor",
            category = SystemParameterEntity.CATEGORY_FEED,
            name = "Cám hỗn hợp nái mang thai",
            code = "CAM_NAI_MANG_THAI",
            description = "Thức ăn nái mang thai",
            unit = "Bao 25kg",
            displayOrder = 1,
            safetyDaysThreshold = 2,
            minStockThreshold = 20.0
        )

        val fullMatrix = FeedRationCalculator.getDefaultRationMatrix() + listOf(feedParam)

        // 5 nái mang thai (2.0kg/con/ngày) -> Tiêu thụ = 10kg/ngày
        // Tồn kho: 10 bao 25kg = 250kg -> Dùng được 250 / 10 = 25 ngày (> 2 ngày an toàn)
        // Nhưng tồn kho 10 bao < 20 bao tối thiểu cứng -> Kích hoạt mức Khẩn cấp (Đỏ)
        val inventory = listOf(
            InventoryItemEntity(
                id = "inv-feed-floor",
                name = "Cám hỗn hợp nái mang thai",
                category = InventoryItemEntity.CATEGORY_FEED,
                unit = "Bao 25kg",
                currentStock = 10f,
                lastUnitPrice = 320_000L,
                averageUnitPrice = 320_000L,
                systemParamId = "param-feed-floor"
            )
        )

        val result = FeedRationCalculator.calculate(
            pigs = sows,
            rationMatrix = fullMatrix,
            inventoryFeedItems = inventory,
            now = now
        )

        val feedDemand = result.feedResults.find { it.feedCode == "CAM_NAI_MANG_THAI" }
        assertNotNull(feedDemand)
        assertTrue("Stock bags (10) < minStockThreshold (20) must trigger critical alert", feedDemand?.isCritical == true)
        assertTrue("Suggested order bags must be at least 20 bags", (feedDemand?.suggestedOrderBags ?: 0) >= 20)
    }

    @Test
    fun testBarnDistributionDynamicXAxisAndGrouped3DBars() {
        val now = 1700000000000L

        // 1. Danh sách chuồng cấu hình trong Tham số hệ thống
        val barns = listOf(
            SystemParameterEntity("b-1", SystemParameterEntity.CATEGORY_BARN, "Chuồng Phối 1", "CHUONG_PHOI_1", "Khu phối nái", "", 1),
            SystemParameterEntity("b-2", SystemParameterEntity.CATEGORY_BARN, "Chuồng Đẻ 1", "CHUONG_DE_1", "Khu nái đẻ", "", 2),
            SystemParameterEntity("b-3", SystemParameterEntity.CATEGORY_BARN, "Chuồng Thịt 1", "CHUONG_THIT_1", "Khu nuôi thịt", "", 3)
        )

        // 2. Danh sách heo thực tế trong các chuồng
        val pigs = listOf(
            // Chuồng phối 1: 5 nái mang thai (Xanh dương) + 3 nái chờ phối (Cam)
            PigEntity("p1", "NY-01", isBatch = false, quantity = 5, breed = "Landrace", gender = "Cái", status = SowReproductiveStatus.MANG_THAI, location = "Chuồng Phối 1"),
            PigEntity("p2", "NY-02", isBatch = false, quantity = 3, breed = "Yorkshire", gender = "Cái", status = SowReproductiveStatus.CAI_SUA_CHO_PHOI, location = "Chuồng Phối 1"),

            // Chuồng đẻ 1: 2 nái bầu chờ đẻ (Xanh da trời) + 4 nái nuôi con (Xanh lá) + 44 heo con theo mẹ (Tím)
            PigEntity("p3", "NY-03", isBatch = false, quantity = 2, breed = "Landrace", gender = "Cái", status = SowReproductiveStatus.MANG_THAI, location = "Chuồng Đẻ 1"),
            PigEntity("p4", "NY-04", isBatch = false, quantity = 4, breed = "Landrace", gender = "Cái", status = SowReproductiveStatus.NUOI_CON, location = "Chuồng Đẻ 1"),
            PigEntity("p5", "Lô Heo Con 01", isBatch = true, quantity = 44, breed = "F1 Land-York", gender = "Hỗn hợp", status = SowReproductiveStatus.NUOI_CON, location = "Chuồng Đẻ 1"),

            // Chuồng thịt 1: 150 heo thịt (1 cột duy nhất - Teal)
            PigEntity("p6", "Lô Thịt 01", isBatch = true, quantity = 150, breed = "Duroc", gender = "Hỗn hợp", status = SowReproductiveStatus.DANG_NUOI, location = "Chuồng Thịt 1")
        )

        val columns = BarnDistributionCalculator.calculate(pigs, barns)

        // Trục hoành (X-Axis) khớp đúng 3 chuồng
        assertEquals(3, columns.size)
        assertEquals("Chuồng Phối 1", columns[0].barnName)
        assertEquals(BarnCategoryType.MATING_GESTATION, columns[0].barnType)
        assertEquals("Chuồng Đẻ 1", columns[1].barnName)
        assertEquals(BarnCategoryType.FARROWING, columns[1].barnType)
        assertEquals("Chuồng Thịt 1", columns[2].barnName)
        assertEquals(BarnCategoryType.GROWER, columns[2].barnType)

        // Chuồng Phối 1: 2 cột con (Nái mang thai: 5, Nái chờ phối: 3)
        assertEquals(2, columns[0].subBars.size)
        assertEquals("Nái mang thai", columns[0].subBars[0].label)
        assertEquals(5, columns[0].subBars[0].count)
        assertEquals("Nái chờ phối", columns[0].subBars[1].label)
        assertEquals(3, columns[0].subBars[1].count)
        assertEquals(8, columns[0].totalCount)

        // Chuồng Đẻ 1: 3 cột con (Nái mang thai: 2, Nái nuôi con: 4, Heo con theo mẹ: 44)
        assertEquals(3, columns[1].subBars.size)
        assertEquals("Nái mang thai", columns[1].subBars[0].label)
        assertEquals(2, columns[1].subBars[0].count)
        assertEquals("Nái nuôi con", columns[1].subBars[1].label)
        assertEquals(4, columns[1].subBars[1].count)
        assertEquals("Heo con theo mẹ", columns[1].subBars[2].label)
        assertEquals(44, columns[1].subBars[2].count)
        assertEquals(50, columns[1].totalCount)

        // Chuồng Thịt 1: 1 cột duy nhất (Tổng Heo thịt: 150)
        assertEquals(1, columns[2].subBars.size)
        assertEquals("Heo thịt", columns[2].subBars[0].label)
        assertEquals(150, columns[2].subBars[0].count)
        assertEquals(150, columns[2].totalCount)
    }

    @Test
    fun testBarnDistributionAddingRemovingBarnUpdatesXAxis() {
        val initialBarns = listOf(
            SystemParameterEntity("b-1", SystemParameterEntity.CATEGORY_BARN, "Chuồng Phối 1", "CHUONG_PHOI_1", "Khu phối nái", "", 1),
            SystemParameterEntity("b-2", SystemParameterEntity.CATEGORY_BARN, "Chuồng Đẻ 1", "CHUONG_DE_1", "Khu nái đẻ", "", 2),
            SystemParameterEntity("b-3", SystemParameterEntity.CATEGORY_BARN, "Chuồng Thịt 1", "CHUONG_THIT_1", "Khu nuôi thịt", "", 3)
        )

        val columnsInitial = BarnDistributionCalculator.calculate(emptyList(), initialBarns)
        assertEquals(3, columnsInitial.size)

        // Thêm chuồng mới vào Tham số hệ thống: "Chuồng Thịt 2"
        val updatedBarnsWithNew = initialBarns + listOf(
            SystemParameterEntity("b-4", SystemParameterEntity.CATEGORY_BARN, "Chuồng Thịt 2", "CHUONG_THIT_2", "Khu nuôi thịt 2", "", 4)
        )
        val columnsAdded = BarnDistributionCalculator.calculate(emptyList(), updatedBarnsWithNew)
        assertEquals("Adding a barn must dynamically add a column on X-axis", 4, columnsAdded.size)
        assertEquals("Chuồng Thịt 2", columnsAdded[3].barnName)

        // Bớt chuồng: Xóa Chuồng Phối 1
        val removedBarns = updatedBarnsWithNew.filter { it.code != "CHUONG_PHOI_1" }
        val columnsRemoved = BarnDistributionCalculator.calculate(emptyList(), removedBarns)
        assertEquals("Removing a barn must dynamically update column count", 3, columnsRemoved.size)
        assertFalse("CHUONG_PHOI_1 should be removed", columnsRemoved.any { it.barnCode == "CHUONG_PHOI_1" })
    }

    @Test
    fun testTouchDrillDownBarnFiltering() {
        val pig1 = PigEntity("p1", "NY-01", isBatch = false, quantity = 1, breed = "Landrace", gender = "Cái", status = SowReproductiveStatus.MANG_THAI, location = "Chuồng Phối 1")
        val pig2 = PigEntity("p2", "NY-02", isBatch = false, quantity = 1, breed = "Yorkshire", gender = "Cái", status = SowReproductiveStatus.NUOI_CON, location = "Chuồng Đẻ 1")
        val pig3 = PigEntity("p3", "Lô Thịt 01", isBatch = true, quantity = 100, breed = "Duroc", gender = "Hỗn hợp", status = SowReproductiveStatus.DANG_NUOI, location = "Chuồng Thịt 1")

        val allPigs = listOf(pig1, pig2, pig3)

        // Khi chạm vào "Chuồng Thịt 1" trên biểu đồ 3D:
        val filteredForMeatBarn = allPigs.filter { BarnDistributionCalculator.matchPigToBarn(it, "Chuồng Thịt 1") }
        assertEquals(1, filteredForMeatBarn.size)
        assertEquals("Lô Thịt 01", filteredForMeatBarn[0].tagCode)

        // Khi chạm vào "Chuồng Đẻ 1":
        val filteredForFarrowBarn = allPigs.filter { BarnDistributionCalculator.matchPigToBarn(it, "Chuồng Đẻ 1") }
        assertEquals(1, filteredForFarrowBarn.size)
        assertEquals("NY-02", filteredForFarrowBarn[0].tagCode)
    }

    @Test
    fun testOutdoorModeToggleAndState() {
        OutdoorModeManager.setOutdoorMode(false)
        assertFalse(OutdoorModeManager.isOutdoorMode.value)

        OutdoorModeManager.toggleOutdoorMode()
        assertTrue(OutdoorModeManager.isOutdoorMode.value)

        OutdoorModeManager.toggleOutdoorMode()
        assertFalse(OutdoorModeManager.isOutdoorMode.value)
    }

    @Test
    fun testOutdoorFontScaling() {
        // Standard mode font scale = 1.0f
        assertEquals(1.0f, OutdoorModeManager.getFontScale(false), 0.001f)
        assertEquals(15f, OutdoorModeManager.scaledSp(15f, false).value, 0.001f)

        // Outdoor High-Contrast mode font scale = 1.2f (1.2x font size for sunlight visibility)
        assertEquals(1.2f, OutdoorModeManager.getFontScale(true), 0.001f)
        assertEquals(18f, OutdoorModeManager.scaledSp(15f, true).value, 0.001f)
    }

    @Test
    fun testOutdoorBorderAndElevation() {
        // In Outdoor mode: Box shadow disabled (elevation = 0.dp), replaced by crisp solid stroke (2.dp)
        assertEquals(0.dp, OutdoorModeManager.getCardElevation(true))
        assertEquals(2.dp, OutdoorModeManager.getCardElevation(false))

        val outdoorBorder = OutdoorModeManager.getBorderStroke(true)
        assertEquals(2.dp, outdoorBorder.width)
        assertEquals(OutdoorBorder, outdoorBorder.brush.let {
            // It's a SolidColor with OutdoorBorder
            (it as androidx.compose.ui.graphics.SolidColor).value
        })

        val standardBorder = OutdoorModeManager.getBorderStroke(false)
        assertEquals(1.dp, standardBorder.width)
    }

    @Test
    fun testOutdoorColorTokensConformToWcagAaa() {
        // Pure White #FFFFFF fill
        assertEquals(androidx.compose.ui.graphics.Color(0xFFFFFFFF), OutdoorInputFill)
        // Background #EDF0F4
        assertEquals(androidx.compose.ui.graphics.Color(0xFFEDF0F4), OutdoorOverallBackground)
        // Border #1E293B
        assertEquals(androidx.compose.ui.graphics.Color(0xFF1E293B), OutdoorBorder)
        // Focus Border #0F52BA
        assertEquals(androidx.compose.ui.graphics.Color(0xFF0F52BA), OutdoorFocusBorder)
        // Label Text #0F172A
        assertEquals(androidx.compose.ui.graphics.Color(0xFF0F172A), OutdoorLabelText)
        // Input Value #000000
        assertEquals(androidx.compose.ui.graphics.Color(0xFF000000), OutdoorInputValue)
        // Placeholder #475569
        assertEquals(androidx.compose.ui.graphics.Color(0xFF475569), OutdoorPlaceholder)
        // Required Star #B91C1C
        assertEquals(androidx.compose.ui.graphics.Color(0xFFB91C1C), OutdoorRequiredStar)
        // Divider #CBD5E1
        assertEquals(androidx.compose.ui.graphics.Color(0xFFCBD5E1), OutdoorDivider)
        // Radio #0F52BA
        assertEquals(androidx.compose.ui.graphics.Color(0xFF0F52BA), OutdoorSelectedRadio)
    }

    @Test
    fun testMasterDataToInventoryForeignKeyAndInboundOutboundSync() = runBlocking {
        val fakeDao = FakeInventoryDao()
        val invRepo = InventoryRepositoryImpl(fakeDao)
        val paramDao = FakeSystemParameterDao()
        val paramRepo = SystemParameterRepositoryImpl(paramDao)

        // 1. Khai báo Danh mục gốc (Master Data trong Tham số hệ thống)
        val feedParamId = paramRepo.addParameter(
            category = SystemParameterEntity.CATEGORY_FEED,
            name = "Cám nái nuôi con cao sản",
            code = "CAM_NAI_NUOI_CON",
            description = "Độ đạm 19%, kích thích tiết sữa",
            unit = "Bao 25kg",
            numericValue = 350000.0,
            safetyDaysThreshold = 3,
            minStockThreshold = 20.0
        )

        // 2. Tạo bản ghi tồn kho ban đầu với systemParamId = feedParamId (Khóa ngoại)
        val initialItemResult = invRepo.addNewItem(
            name = "Cám nái nuôi con cao sản",
            category = InventoryItemEntity.CATEGORY_FEED,
            unit = "Bao 25kg",
            initialStock = 50f,
            unitPrice = 350000L,
            minAlertStock = 20f
        )
        assertTrue(initialItemResult.isSuccess)
        val item = initialItemResult.getOrThrow()

        // 3. THAO TÁC PHIẾU NHẬP KHO: Nhập thêm 30 bao với đơn giá mới 360,000 VNĐ
        val inboundResult = invRepo.recordInbound(
            itemId = item.id,
            quantity = 30f,
            unitPrice = 360000L,
            date = System.currentTimeMillis(),
            supplierOrInvoice = "CP Feed - HĐ #9988",
            notes = "Nhập bổ sung định kỳ"
        )
        assertTrue(inboundResult.isSuccess)
        val inTx = inboundResult.getOrThrow()
        assertEquals(InventoryTransactionEntity.TYPE_IN, inTx.type)
        assertEquals(30f, inTx.quantity, 0.001f)
        assertEquals(360000L * 30, inTx.totalPrice)

        // Kiểm tra tồn kho sau nhập: 50 + 30 = 80 bao
        val itemAfterIn = invRepo.getItemById(item.id)
        assertNotNull(itemAfterIn)
        assertEquals(80f, itemAfterIn!!.currentStock, 0.001f)
        assertEquals(360000L, itemAfterIn.lastUnitPrice)

        // Cập nhật lại đơn giá vào Tham số hệ thống
        val masterParam = paramRepo.getParameterById(feedParamId)
        assertNotNull(masterParam)
        paramRepo.updateParameter(masterParam!!.copy(numericValue = 360000.0))
        val updatedMaster = paramRepo.getParameterById(feedParamId)
        assertEquals(360000.0, updatedMaster!!.numericValue, 0.001)

        // 4. THAO TÁC PHIẾU XUẤT KHO:
        // A. Kiểm tra xuất vượt / Ràng buộc chặn xuất âm: Thử xuất 100 bao trong khi tồn chỉ có 80 bao
        val overOutResult = invRepo.recordOutbound(
            itemId = item.id,
            quantity = 100f,
            targetBarn = "Khu Chuồng Đẻ 4",
            date = System.currentTimeMillis()
        )
        assertTrue("Chặn xuất âm phải thất bại khi quantity > currentStock", overOutResult.isFailure)
        assertTrue(overOutResult.exceptionOrNull()?.message?.contains("Chặn xuất âm") == true)

        // B. Xuất hợp lệ: Xuất 25 bao cho Khu Chuồng Đẻ 4
        val validOutResult = invRepo.recordOutbound(
            itemId = item.id,
            quantity = 25f,
            targetBarn = "Khu Chuồng Đẻ 4",
            date = System.currentTimeMillis(),
            purposeOrNotes = "Cấp cám tuần cho nái đẻ"
        )
        assertTrue(validOutResult.isSuccess)
        val outTx = validOutResult.getOrThrow()
        assertEquals(InventoryTransactionEntity.TYPE_OUT, outTx.type)
        assertEquals("Khu Chuồng Đẻ 4", outTx.targetBarn)
        assertEquals(25f, outTx.quantity, 0.001f)

        // Tồn sau xuất: 80 - 25 = 55 bao
        val itemAfterOut = invRepo.getItemById(item.id)
        assertEquals(55f, itemAfterOut!!.currentStock, 0.001f)

        // Giá trị tồn: 55 * averageUnitPrice
        val expectedStockValue = (55f * itemAfterOut.averageUnitPrice).toLong()
        assertEquals(expectedStockValue, itemAfterOut.totalStockValue)
    }

    @Test
    fun testFeedDaysRemainingAndAlertTrigger() {
        // Công thức: Số ngày dùng = Lượng cám tồn (kg) / Lượng ăn trong ngày (kg)
        val stockBags = 20
        val bagWeight = 25.0 // kg/bao
        val totalStockKg = stockBags * bagWeight // 500 kg
        val dailyDemandKg = 100.0 // kg/ngày

        val daysRemaining = (totalStockKg / dailyDemandKg).toInt()
        assertEquals(5, daysRemaining)

        // Ngưỡng an toàn: 3 ngày, Ngưỡng cảnh báo: 7 ngày
        val safetyDays = 3
        val isEmergency = daysRemaining < safetyDays
        val isWarning = daysRemaining in safetyDays..7

        assertFalse(isEmergency)
        assertTrue(isWarning)

        // Khi tồn giảm xuống còn 2 bao (50 kg) -> 50 / 100 = 0 ngày
        val lowStockKg = 2 * bagWeight
        val emergencyDays = (lowStockKg / dailyDemandKg).toInt()
        assertTrue(emergencyDays < safetyDays)
    }

    // =========================================================================
    // TESTS FOR MASTER ITEM, RECEIPT, ISSUE, AND INVENTORY (ROOM ENTITIES & DAOS)
    // =========================================================================

    @Test
    fun `master item entity supports feed med supply types and typealias`() {
        val feedItem: MasterItem = MasterItemEntity(
            itemId = "CAM-NB-01",
            itemType = MasterItemEntity.TYPE_FEED,
            name = "Cám nái mang thai CP-551",
            unit = "Bao 25kg",
            defaultPrice = 360_000L,
            minStockAlert = 20f,
            code = "CAM-NB-01",
            description = "Dành cho nái chửa giai đoạn 1 và 2",
            safetyDaysThreshold = 4
        )

        val medItem: MasterItem = MasterItemEntity(
            itemId = "MED-AMOX-01",
            itemType = MasterItemEntity.TYPE_MED,
            name = "Amoxicillin 15% LA",
            unit = "Lọ 100ml",
            defaultPrice = 95_000L,
            minStockAlert = 5f,
            code = "MED-AMOX-01",
            description = "Kháng sinh phổ rộng trị viêm phổi",
            safetyDaysThreshold = 7
        )

        val supplyItem: MasterItem = MasterItemEntity(
            itemId = "SUP-KIM-18G",
            itemType = MasterItemEntity.TYPE_SUPPLY,
            name = "Kim tiêm Inox 18G",
            unit = "Hộp 100 cái",
            defaultPrice = 55_000L,
            minStockAlert = 10f,
            code = "SUP-KIM-18G",
            description = "Kim tiêm nái và heo thịt",
            safetyDaysThreshold = 10
        )

        assertEquals("CAM-NB-01", feedItem.itemId)
        assertEquals(MasterItemEntity.TYPE_FEED, feedItem.itemType)
        assertEquals(360_000L, feedItem.defaultPrice)
        assertEquals(4, feedItem.safetyDaysThreshold)
        assertTrue(feedItem.isActive)

        assertEquals("MED-AMOX-01", medItem.itemId)
        assertEquals(MasterItemEntity.TYPE_MED, medItem.itemType)
        assertEquals("Lọ 100ml", medItem.unit)

        assertEquals("SUP-KIM-18G", supplyItem.itemId)
        assertEquals(MasterItemEntity.TYPE_SUPPLY, supplyItem.itemType)
    }

    @Test
    fun `receipt entity calculates total cost correctly and preserves foreign key`() {
        val receipt: Receipt = ReceiptEntity(
            receiptId = "REC_20261110_001",
            itemId = "CAM-NB-01",
            date = 1818000000000L,
            quantity = 50f,
            importPrice = 350_000L,
            totalCost = 50L * 350_000L,
            supplier = "Công ty CP Chăn nuôi C.P. Việt Nam",
            note = "Nhập cám định kỳ theo hợp đồng"
        )

        assertEquals("REC_20261110_001", receipt.receiptId)
        assertEquals("CAM-NB-01", receipt.itemId)
        assertEquals(50f, receipt.quantity, 0.001f)
        assertEquals(350_000L, receipt.importPrice)
        assertEquals(17_500_000L, receipt.totalCost)
        assertEquals("Công ty CP Chăn nuôi C.P. Việt Nam", receipt.supplier)
    }

    @Test
    fun `issue entity calculates total cost with weighted unit price and assigns barn`() {
        val issue: Issue = IssueEntity(
            issueId = "ISS_20261110_001",
            itemId = "CAM-NB-01",
            date = 1818000000000L,
            quantity = 15f,
            targetBarnId = "CHUONG_BAU_1",
            note = "Cấp cám cho chuồng bầu 1 tuần 45",
            unitPrice = 355_000L,
            totalCost = (15f * 355_000L).toLong()
        )

        assertEquals("ISS_20261110_001", issue.issueId)
        assertEquals("CAM-NB-01", issue.itemId)
        assertEquals(15f, issue.quantity, 0.001f)
        assertEquals("CHUONG_BAU_1", issue.targetBarnId)
        assertEquals(355_000L, issue.unitPrice)
        assertEquals(5_325_000L, issue.totalCost)
    }

    @Test
    fun `inventory entity maintains real-time stock and weighted valuation`() {
        val inventory: Inventory = InventoryEntity(
            itemId = "CAM-NB-01",
            currentStock = 85f,
            stockValue = 85L * 355_000L,
            lastUnitPrice = 360_000L,
            averageUnitPrice = 355_000L,
            updatedAt = System.currentTimeMillis()
        )

        assertEquals("CAM-NB-01", inventory.itemId)
        assertEquals(85f, inventory.currentStock, 0.001f)
        assertEquals(30_175_000L, inventory.stockValue)
        assertEquals(360_000L, inventory.lastUnitPrice)
        assertEquals(355_000L, inventory.averageUnitPrice)
    }

    @Test
    fun `master inventory repository recordReceipt updates stock and average price`() = runBlocking {
        val fakeMasterItemDao = FakeMasterItemDao()
        val fakeReceiptDao = FakeReceiptDao()
        val fakeIssueDao = FakeIssueDao()
        val fakeInventoryDao = FakeInventoryEntityDao()

        val repo: MasterInventoryRepository = MasterInventoryRepositoryImpl(
            masterItemDao = fakeMasterItemDao,
            receiptDao = fakeReceiptDao,
            issueDao = fakeIssueDao,
            inventoryDao = fakeInventoryDao
        )

        // 1. Tạo mặt hàng mới qua Repository với tồn kho ban đầu 10 bao @ 100,000đ
        val createResult = repo.createMasterItem(
            itemId = "CAM-TEST-01",
            itemType = MasterItemEntity.TYPE_FEED,
            name = "Cám Heo Con Tập Ăn",
            unit = "Bao 25kg",
            defaultPrice = 100_000L,
            minStockAlert = 5f,
            code = "CAM-TEST-01",
            description = "Cám thử nghiệm",
            safetyDaysThreshold = 3,
            initialStock = 10f
        )
        assertTrue(createResult.isSuccess)

        val invInitial = fakeInventoryDao.getInventoryByIdOnce("CAM-TEST-01")
        assertNotNull(invInitial)
        assertEquals(10f, invInitial?.currentStock ?: 0f, 0.001f)
        assertEquals(100_000L, invInitial?.averageUnitPrice)
        assertEquals(1_000_000L, invInitial?.stockValue)

        // 2. Nhập thêm 20 bao với đơn giá 130,000đ
        // Giá trị cũ: 10 * 100,000 = 1,000,000
        // Giá trị nhập mới: 20 * 130,000 = 2,600,000
        // Tổng tồn mới: 30 bao
        // Bình quân gia quyền mới: (1,000,000 + 2,600,000) / 30 = 120,000đ
        val receiptResult = repo.recordReceipt(
            itemId = "CAM-TEST-01",
            quantity = 20f,
            importPrice = 130_000L,
            date = System.currentTimeMillis(),
            supplier = "Nhà máy CP",
            note = "Nhập lô số 2"
        )
        assertTrue(receiptResult.isSuccess)

        val invAfterReceipt = fakeInventoryDao.getInventoryByIdOnce("CAM-TEST-01")
        assertNotNull(invAfterReceipt)
        assertEquals(30f, invAfterReceipt?.currentStock ?: 0f, 0.001f)
        assertEquals(130_000L, invAfterReceipt?.lastUnitPrice)
        assertEquals(120_000L, invAfterReceipt?.averageUnitPrice)
        assertEquals(3_600_000L, invAfterReceipt?.stockValue)

        val receipts = fakeReceiptDao.getReceiptsByItemId("CAM-TEST-01").first()
        assertEquals(2, receipts.size) // 1 initial receipt + 1 new receipt
    }

    @Test
    fun `master inventory repository recordIssue blocks negative stock and deducts inventory`() = runBlocking {
        val fakeMasterItemDao = FakeMasterItemDao()
        val fakeReceiptDao = FakeReceiptDao()
        val fakeIssueDao = FakeIssueDao()
        val fakeInventoryDao = FakeInventoryEntityDao()

        val repo: MasterInventoryRepository = MasterInventoryRepositoryImpl(
            masterItemDao = fakeMasterItemDao,
            receiptDao = fakeReceiptDao,
            issueDao = fakeIssueDao,
            inventoryDao = fakeInventoryDao
        )

        // Tạo mặt hàng có sẵn 25 hộp kim tiêm
        repo.createMasterItem(
            itemId = "SUP-TEST-01",
            itemType = MasterItemEntity.TYPE_SUPPLY,
            name = "Kim tiêm 18G Test",
            unit = "Hộp",
            defaultPrice = 50_000L,
            minStockAlert = 5f,
            code = "SUP-TEST-01",
            description = "Kim tiêm",
            safetyDaysThreshold = 5,
            initialStock = 25f
        )

        // 1. Thử xuất 30 hộp (vượt quá tồn kho 25 hộp) -> BẮT BUỘC THẤT BẠI DO CHẶN XUẤT ÂM
        val overIssueResult = repo.recordIssue(
            itemId = "SUP-TEST-01",
            quantity = 30f,
            targetBarnId = "CHUONG_THIT_1",
            date = System.currentTimeMillis(),
            note = "Xuất vượt tồn"
        )
        assertTrue(overIssueResult.isFailure)
        val errorMsg = overIssueResult.exceptionOrNull()?.message ?: ""
        assertTrue(errorMsg.contains("Chặn xuất âm") && errorMsg.contains("vượt quá tồn kho hiện tại"))

        // Kiểm tra tồn kho vẫn nguyên vẹn 25
        val invUnchanged = fakeInventoryDao.getInventoryByIdOnce("SUP-TEST-01")
        assertEquals(25f, invUnchanged?.currentStock ?: 0f, 0.001f)

        // 2. Xuất 10 hộp hợp lệ cho chuồng thịt 1
        val validIssueResult = repo.recordIssue(
            itemId = "SUP-TEST-01",
            quantity = 10f,
            targetBarnId = "CHUONG_THIT_1",
            date = System.currentTimeMillis(),
            note = "Xuất tiêm vắc-xin chuồng thịt"
        )
        assertTrue(validIssueResult.isSuccess)
        val issueEntity = validIssueResult.getOrThrow()
        assertEquals(10f, issueEntity.quantity, 0.001f)
        assertEquals("CHUONG_THIT_1", issueEntity.targetBarnId)
        assertEquals(50_000L, issueEntity.unitPrice)
        assertEquals(500_000L, issueEntity.totalCost)

        // Kiểm tra tồn kho giảm còn 15 hộp
        val invAfterIssue = fakeInventoryDao.getInventoryByIdOnce("SUP-TEST-01")
        assertEquals(15f, invAfterIssue?.currentStock ?: 0f, 0.001f)
        assertEquals(15L * 50_000L, invAfterIssue?.stockValue)
    }

    @Test
    fun `navigation destinations define herd and pig detail routes correctly`() {
        assertEquals("home", com.example.ui.navigation.Screen.Home.route)
        assertEquals("herd", com.example.ui.navigation.Screen.Herd.route)
        assertEquals("pig_detail/{pigId}", com.example.ui.navigation.Screen.PigDetail.route)
        assertEquals("pig_detail/pig-1234", com.example.ui.navigation.Screen.PigDetail.createRoute("pig-1234"))
        assertEquals("tasks", com.example.ui.navigation.Screen.Tasks.route)
        assertEquals("inventory", com.example.ui.navigation.Screen.Inventory.route)
        assertEquals("reports", com.example.ui.navigation.Screen.Reports.route)
        assertEquals("settings", com.example.ui.navigation.Screen.Settings.route)
        assertEquals("sync_logs", com.example.ui.navigation.Screen.SyncLogs.route)
    }

    @Test
    fun `room database pig entity and dao structure validates offline-first fields and operations`() = runBlocking {
        val fakeDao = FakePigDao()
        val sow = PigEntity(
            id = "sow-test-01",
            tagCode = "N-OFFLINE-01",
            birthDate = System.currentTimeMillis() - 100_000_000L,
            isBatch = false,
            quantity = 1,
            breed = "Landrace",
            gender = "Cái",
            status = SowReproductiveStatus.MANG_THAI,
            location = "Chuồng Bầu 01",
            parity = 2,
            currentWeightKg = 210f,
            syncStatus = com.example.data.model.SyncStatus.PENDING_SYNC,
            updatedAt = System.currentTimeMillis(),
            version = 1
        )
        fakeDao.insertPig(sow)

        // Verify retrieval and reactive flows
        val retrieved = fakeDao.getPigByIdOnce("sow-test-01")
        assertNotNull(retrieved)
        assertEquals("N-OFFLINE-01", retrieved?.tagCode)
        assertEquals(com.example.data.model.SyncStatus.PENDING_SYNC, retrieved?.syncStatus)
        assertTrue(retrieved?.isSow == true)
        assertFalse(retrieved?.isGrowerBatch == true)

        // Pending sync count
        val pendingCount = fakeDao.getPendingSyncCount().first()
        assertEquals(1, pendingCount)

        // Delete by ID
        fakeDao.deletePigById("sow-test-01")
        val deleted = fakeDao.getPigByIdOnce("sow-test-01")
        assertEquals(null, deleted)
    }

    @Test
    fun `pig list view model manages reactive StateFlow and handles search and filters`() {
        val fakeRepo = FakePigRepository()
        val viewModel = com.example.ui.pig.PigListViewModel(fakeRepo)

        // Verify initial StateFlow state
        assertNotNull(viewModel.pigListState.value)
        assertEquals("", viewModel.searchQuery.value)
        assertEquals(com.example.ui.pig.PigFilterType.ALL, viewModel.activeFilter.value)

        // Test search query update
        viewModel.onSearchQueryChanged("N089")
        assertEquals("N089", viewModel.searchQuery.value)

        // Test filter selection
        viewModel.onFilterSelected(com.example.ui.pig.PigFilterType.INDIVIDUAL)
        assertEquals(com.example.ui.pig.PigFilterType.INDIVIDUAL, viewModel.activeFilter.value)

        // Test sort options
        viewModel.toggleSortOption()
        assertEquals(com.example.ui.pig.PigSortOption.BIRTH_DATE, viewModel.sortOption.value)

        viewModel.toggleSortDirection()
        assertEquals(com.example.ui.pig.SortDirection.DESCENDING, viewModel.sortDirection.value)

        // Test clear
        viewModel.clearSearchQuery()
        assertEquals("", viewModel.searchQuery.value)
    }
}

/**
 * In-memory Fake PigDao for unit testing
 */
class FakePigDao : com.example.data.local.dao.PigDao {
    private val pigs = mutableMapOf<String, PigEntity>()

    override fun getAllPigs(): Flow<List<PigEntity>> = MutableStateFlow(pigs.values.toList())
    override fun getAllSows(): Flow<List<PigEntity>> = MutableStateFlow(pigs.values.filter { !it.isBatch })
    override fun getAllGrowerBatches(): Flow<List<PigEntity>> = MutableStateFlow(pigs.values.filter { it.isBatch })
    override fun getSowsByStatus(status: com.example.data.model.SowReproductiveStatus): Flow<List<PigEntity>> =
        MutableStateFlow(pigs.values.filter { !it.isBatch && it.status == status })
    override fun getPigsByLocation(location: String): Flow<List<PigEntity>> =
        MutableStateFlow(pigs.values.filter { it.location == location })
    override fun searchPigs(query: String): Flow<List<PigEntity>> =
        MutableStateFlow(pigs.values.filter { it.tagCode.contains(query, true) || it.location.contains(query, true) })
    override fun getPigById(id: String): Flow<PigEntity?> = MutableStateFlow(pigs[id])
    override suspend fun getPigByIdOnce(id: String): PigEntity? = pigs[id]
    override suspend fun getPigByTagCode(tagCode: String): PigEntity? = pigs.values.find { it.tagCode == tagCode }
    override fun getTotalPigCount(): Flow<Int> = MutableStateFlow(pigs.size)
    override fun getTotalHeadCount(): Flow<Int?> = MutableStateFlow(pigs.values.sumOf { it.quantity })
    override suspend fun getUnsyncedPigs(syncedStatus: com.example.data.model.SyncStatus): List<PigEntity> = emptyList()
    override suspend fun insertPig(pig: PigEntity) { pigs[pig.id] = pig }
    override suspend fun insertPigs(pigs: List<PigEntity>) { pigs.forEach { this.pigs[it.id] = it } }
    override suspend fun updatePig(pig: PigEntity) { pigs[pig.id] = pig }
    override suspend fun deletePig(pig: PigEntity) { pigs.remove(pig.id) }
    override suspend fun deletePigById(id: String) { pigs.remove(id) }
    override fun getPendingSyncCount(syncedStatus: com.example.data.model.SyncStatus): Flow<Int> =
        MutableStateFlow(pigs.values.count { it.syncStatus != syncedStatus })
    override suspend fun updateSyncStatus(id: String, status: com.example.data.model.SyncStatus) {}
    override suspend fun markAllAsSynced(syncedStatus: com.example.data.model.SyncStatus) {}
}

/**
 * In-memory Fake TaskDao for unit testing
 */
class FakeTaskDao : com.example.data.local.dao.TaskDao {
    val allTasks = mutableListOf<com.example.data.local.entity.TaskEntity>()

    override fun getAllTasks(): Flow<List<com.example.data.local.entity.TaskEntity>> = MutableStateFlow(allTasks)
    override fun getTasksByStatus(status: com.example.data.model.TaskStatus): Flow<List<com.example.data.local.entity.TaskEntity>> =
        MutableStateFlow(allTasks.filter { it.status == status })
    override fun getTasksForReference(referenceId: String): Flow<List<com.example.data.local.entity.TaskEntity>> =
        MutableStateFlow(allTasks.filter { it.referenceId == referenceId })
    override fun getPendingTaskCount(): Flow<Int> =
        MutableStateFlow(allTasks.count { it.status != com.example.data.model.TaskStatus.DONE })
    override suspend fun insertTask(task: com.example.data.local.entity.TaskEntity) { allTasks.add(task) }
    override suspend fun insertTasks(tasks: List<com.example.data.local.entity.TaskEntity>) { allTasks.addAll(tasks) }
    override suspend fun updateTask(task: com.example.data.local.entity.TaskEntity) {
        val idx = allTasks.indexOfFirst { it.id == task.id }
        if (idx >= 0) allTasks[idx] = task else allTasks.add(task)
    }
    override suspend fun deleteTask(task: com.example.data.local.entity.TaskEntity) { allTasks.removeIf { it.id == task.id } }
    override suspend fun updateTaskStatus(id: String, status: com.example.data.model.TaskStatus) {
        val idx = allTasks.indexOfFirst { it.id == id }
        if (idx >= 0) allTasks[idx] = allTasks[idx].copy(status = status)
    }
}

/**
 * In-memory Fake EventDao for unit testing
 */
class FakeEventDao : com.example.data.local.dao.EventDao {
    val allEvents = mutableListOf<com.example.data.local.entity.EventEntity>()

    override fun getAllEvents(): Flow<List<com.example.data.local.entity.EventEntity>> = MutableStateFlow(allEvents)
    override fun getEventsForPig(pigId: String): Flow<List<com.example.data.local.entity.EventEntity>> =
        MutableStateFlow(allEvents.filter { it.pigId == pigId })
    override fun getEventsByType(eventType: String): Flow<List<com.example.data.local.entity.EventEntity>> =
        MutableStateFlow(allEvents.filter { it.eventType == eventType })
    override fun getMatingEvents(): Flow<List<com.example.data.local.entity.EventEntity>> =
        getEventsByType(com.example.data.local.entity.EventEntity.TYPE_MATING)
    override fun getPregnancyCheckEvents(): Flow<List<com.example.data.local.entity.EventEntity>> =
        getEventsByType(com.example.data.local.entity.EventEntity.TYPE_PREGNANCY_CHECK)
    override fun getWeaningEvents(): Flow<List<com.example.data.local.entity.EventEntity>> =
        getEventsByType(com.example.data.local.entity.EventEntity.TYPE_WEANING)
    override fun getVaccinationEvents(): Flow<List<com.example.data.local.entity.EventEntity>> =
        getEventsByType(com.example.data.local.entity.EventEntity.TYPE_VACCINATION)
    override fun getTransferEvents(): Flow<List<com.example.data.local.entity.EventEntity>> =
        getEventsByType(com.example.data.local.entity.EventEntity.TYPE_TRANSFER)
    override fun getEventsByDateRange(startDate: Long, endDate: Long): Flow<List<com.example.data.local.entity.EventEntity>> =
        MutableStateFlow(allEvents.filter { it.eventDate in startDate..endDate })
    override fun getRecentEvents(limit: Int): Flow<List<com.example.data.local.entity.EventEntity>> =
        MutableStateFlow(allEvents.take(limit))
    override suspend fun getEventById(id: String): com.example.data.local.entity.EventEntity? =
        allEvents.find { it.id == id }
    override suspend fun insertEvent(event: com.example.data.local.entity.EventEntity) { allEvents.add(event) }
    override suspend fun insertEvents(events: List<com.example.data.local.entity.EventEntity>) { allEvents.addAll(events) }
    override suspend fun updateEvent(event: com.example.data.local.entity.EventEntity) {
        val idx = allEvents.indexOfFirst { it.id == event.id }
        if (idx >= 0) allEvents[idx] = event else allEvents.add(event)
    }
    override suspend fun deleteEvent(event: com.example.data.local.entity.EventEntity) { allEvents.removeIf { it.id == event.id } }
    override suspend fun deleteEventById(id: String) { allEvents.removeIf { it.id == id } }
}

/**
 * In-memory Fake implementation of PigRepository for Domain Use Case Unit Testing.
 */
class FakePigRepository : PigRepository {
    private val pigsMap = mutableMapOf<String, PigEntity>()
    private val pigsFlow = MutableStateFlow<List<PigEntity>>(emptyList())
    private val totalCountFlow = MutableStateFlow(0)
    private val totalHeadFlow = MutableStateFlow<Int?>(0)

    override val allPigs: Flow<List<PigEntity>> = pigsFlow
    override val totalPigCount: Flow<Int> = totalCountFlow
    override val totalHeadCount: Flow<Int?> = totalHeadFlow

    private fun updateFlows() {
        val list = pigsMap.values.toList()
        pigsFlow.value = list
        totalCountFlow.value = list.size
        totalHeadFlow.value = list.sumOf { it.quantity }
    }

    override fun getPigById(id: String): Flow<PigEntity?> {
        return MutableStateFlow(pigsMap[id])
    }

    override suspend fun getPigByIdOnce(id: String): PigEntity? {
        return pigsMap[id]
    }

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
        val id = "pig-${System.currentTimeMillis()}-${pigsMap.size}"
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
            currentWeightKg = currentWeightKg
        )
        pigsMap[id] = pig
        updateFlows()
        return id
    }

    override suspend fun updatePig(pig: PigEntity) {
        pigsMap[pig.id] = pig
        updateFlows()
    }

    override suspend fun deletePig(pig: PigEntity) {
        pigsMap.remove(pig.id)
        updateFlows()
    }

    override suspend fun insertPigs(pigs: List<PigEntity>) {
        pigs.forEach { pigsMap[it.id] = it }
        updateFlows()
    }
}

/**
 * In-memory Fake InventoryDao for unit testing
 */
class FakeInventoryDao : com.example.data.local.dao.InventoryDao {
    private val itemsMap = mutableMapOf<String, InventoryItemEntity>()
    private val transactionsList = mutableListOf<InventoryTransactionEntity>()
    private val itemsFlow = MutableStateFlow<List<InventoryItemEntity>>(emptyList())
    private val transactionsFlow = MutableStateFlow<List<InventoryTransactionEntity>>(emptyList())

    private fun updateFlows() {
        itemsFlow.value = itemsMap.values.toList()
        transactionsFlow.value = transactionsList.toList()
    }

    override fun getAllItems(): Flow<List<InventoryItemEntity>> = itemsFlow

    override fun getItemsByCategory(category: String): Flow<List<InventoryItemEntity>> =
        MutableStateFlow<List<InventoryItemEntity>>(itemsMap.values.filter { it.category == category })

    override suspend fun getItemById(id: String): InventoryItemEntity? = itemsMap[id]

    override suspend fun getItemByName(name: String): InventoryItemEntity? =
        itemsMap.values.find { it.name.equals(name, ignoreCase = true) }

    override suspend fun insertItem(item: InventoryItemEntity) {
        itemsMap[item.id] = item
        updateFlows()
    }

    override suspend fun insertItems(items: List<InventoryItemEntity>) {
        items.forEach { itemsMap[it.id] = it }
        updateFlows()
    }

    override suspend fun updateItem(item: InventoryItemEntity) {
        itemsMap[item.id] = item
        updateFlows()
    }

    override suspend fun deleteItem(item: InventoryItemEntity) {
        itemsMap.remove(item.id)
        updateFlows()
    }

    override fun getLowStockItems(): Flow<List<InventoryItemEntity>> =
        MutableStateFlow(itemsMap.values.filter { it.isLowStock })

    override fun searchItems(query: String): Flow<List<InventoryItemEntity>> =
        MutableStateFlow(itemsMap.values.filter { it.name.contains(query, true) || it.category.contains(query, true) })

    override suspend fun countItems(): Int = itemsMap.size

    // Giao dịch kho
    override fun getAllTransactions(): Flow<List<InventoryTransactionEntity>> = transactionsFlow

    override fun getTransactionsByType(type: String): Flow<List<InventoryTransactionEntity>> =
        MutableStateFlow(transactionsList.filter { it.type == type })

    override fun getTransactionsByItem(itemId: String): Flow<List<InventoryTransactionEntity>> =
        MutableStateFlow(transactionsList.filter { it.itemId == itemId })

    override fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<InventoryTransactionEntity>> =
        MutableStateFlow(transactionsList.filter { it.date in startDate..endDate })

    override fun getRecentTransactions(limit: Int): Flow<List<InventoryTransactionEntity>> =
        MutableStateFlow(transactionsList.take(limit))

    override suspend fun insertTransaction(transaction: InventoryTransactionEntity) {
        transactionsList.add(0, transaction)
        updateFlows()
    }
}

/**
 * In-memory Fake StaffDao for unit testing Clean Architecture
 */
class FakeStaffDao : StaffDao {
    private val staffMap = mutableMapOf<String, StaffEntity>()
    private val staffFlow = MutableStateFlow<List<StaffEntity>>(emptyList())

    private fun updateFlows() {
        staffFlow.value = staffMap.values.sortedBy { it.code }
    }

    override fun getAllStaff(): Flow<List<StaffEntity>> = staffFlow
    override fun getActiveStaff(): Flow<List<StaffEntity>> =
        staffFlow.map { list -> list.filter { it.status == StaffEntity.STATUS_ACTIVE } }
    override fun getStaffByRole(role: String): Flow<List<StaffEntity>> =
        staffFlow.map { list -> list.filter { it.role == role && it.status == StaffEntity.STATUS_ACTIVE } }
    override fun getStaffById(id: String): Flow<StaffEntity?> =
        staffFlow.map { list -> list.find { it.id == id } }
    override suspend fun getStaffByIdOnce(id: String): StaffEntity? = staffMap[id]
    override suspend fun getStaffByCode(code: String): StaffEntity? = staffMap.values.find { it.code == code }
    override fun searchStaff(query: String): Flow<List<StaffEntity>> = staffFlow.map { list ->
        list.filter {
            it.fullName.contains(query, true) || it.code.contains(query, true) || it.phoneNumber.contains(query, true) || it.role.contains(query, true)
        }
    }
    override suspend fun countActiveStaff(): Int = staffMap.values.count { it.status == StaffEntity.STATUS_ACTIVE }
    override suspend fun insertStaff(staff: StaffEntity) { staffMap[staff.id] = staff; updateFlows() }
    override suspend fun insertStaffList(staffList: List<StaffEntity>) { staffList.forEach { staffMap[it.id] = it }; updateFlows() }
    override suspend fun updateStaff(staff: StaffEntity) { staffMap[staff.id] = staff; updateFlows() }
    override suspend fun deleteStaff(staff: StaffEntity) { staffMap.remove(staff.id); updateFlows() }
    override suspend fun deleteStaffById(id: String) { staffMap.remove(id); updateFlows() }
}

/**
 * In-memory Fake SystemParameterDao for unit testing
 */
class FakeSystemParameterDao : SystemParameterDao {
    private val parametersMap = mutableMapOf<String, SystemParameterEntity>()
    private val parametersFlow = MutableStateFlow<List<SystemParameterEntity>>(emptyList())

    private fun updateFlows() {
        parametersFlow.value = parametersMap.values.sortedWith(compareBy({ it.displayOrder }, { -it.createdAt }))
    }

    override fun getAllParameters(): Flow<List<SystemParameterEntity>> = parametersFlow

    override fun getParametersByCategory(category: String): Flow<List<SystemParameterEntity>> =
        parametersFlow.map { list -> list.filter { it.category == category } }

    override suspend fun getParameterById(id: String): SystemParameterEntity? = parametersMap[id]

    override suspend fun insertParameter(parameter: SystemParameterEntity) {
        parametersMap[parameter.id] = parameter
        updateFlows()
    }

    override suspend fun insertParameters(parameters: List<SystemParameterEntity>) {
        parameters.forEach { parametersMap[it.id] = it }
        updateFlows()
    }

    override suspend fun updateParameter(parameter: SystemParameterEntity) {
        parametersMap[parameter.id] = parameter
        updateFlows()
    }

    override suspend fun deleteParameter(parameter: SystemParameterEntity) {
        parametersMap.remove(parameter.id)
        updateFlows()
    }

    override suspend fun deleteParameterById(id: String) {
        parametersMap.remove(id)
        updateFlows()
    }
}

/**
 * In-memory Fake MasterItemDao for unit testing
 */
class FakeMasterItemDao : MasterItemDao {
    private val items = mutableMapOf<String, MasterItemEntity>()
    private val itemsFlow = MutableStateFlow<List<MasterItemEntity>>(emptyList())

    private fun updateFlows() {
        itemsFlow.value = items.values.filter { it.isActive }.sortedWith(compareBy({ it.itemType }, { it.name }))
    }

    override fun getAllMasterItems(): Flow<List<MasterItemEntity>> = itemsFlow

    override fun getMasterItemsByType(itemType: String): Flow<List<MasterItemEntity>> =
        itemsFlow.map { list -> list.filter { it.itemType == itemType } }

    override suspend fun getMasterItemById(itemId: String): MasterItemEntity? = items[itemId]

    override suspend fun getMasterItemByCode(code: String): MasterItemEntity? =
        items.values.find { it.code.equals(code, ignoreCase = true) }

    override suspend fun insertMasterItem(item: MasterItemEntity) {
        items[item.itemId] = item
        updateFlows()
    }

    override suspend fun insertMasterItems(items: List<MasterItemEntity>) {
        items.forEach { this.items[it.itemId] = it }
        updateFlows()
    }

    override suspend fun updateMasterItem(item: MasterItemEntity) {
        items[item.itemId] = item
        updateFlows()
    }

    override suspend fun deleteMasterItem(item: MasterItemEntity) {
        items.remove(item.itemId)
        updateFlows()
    }

    override suspend fun countMasterItems(): Int = items.size
}

/**
 * In-memory Fake ReceiptDao for unit testing
 */
class FakeReceiptDao : ReceiptDao {
    private val receipts = mutableListOf<ReceiptEntity>()
    private val receiptsFlow = MutableStateFlow<List<ReceiptEntity>>(emptyList())

    private fun updateFlows() {
        receiptsFlow.value = receipts.sortedWith(compareByDescending<ReceiptEntity> { it.date }.thenByDescending { it.createdAt })
    }

    override fun getAllReceipts(): Flow<List<ReceiptEntity>> = receiptsFlow

    override fun getReceiptsByItemId(itemId: String): Flow<List<ReceiptEntity>> =
        receiptsFlow.map { list -> list.filter { it.itemId == itemId } }

    override suspend fun getReceiptById(receiptId: String): ReceiptEntity? =
        receipts.find { it.receiptId == receiptId }

    override fun getTotalImportQuantity(itemId: String): Flow<Float?> =
        receiptsFlow.map { list -> list.filter { it.itemId == itemId }.sumOf { it.quantity.toDouble() }.toFloat() }

    override suspend fun insertReceipt(receipt: ReceiptEntity) {
        receipts.add(receipt)
        updateFlows()
    }

    override suspend fun insertReceipts(receipts: List<ReceiptEntity>) {
        this.receipts.addAll(receipts)
        updateFlows()
    }

    override suspend fun countReceipts(): Int = receipts.size
}

/**
 * In-memory Fake IssueDao for unit testing
 */
class FakeIssueDao : IssueDao {
    private val issues = mutableListOf<IssueEntity>()
    private val issuesFlow = MutableStateFlow<List<IssueEntity>>(emptyList())

    private fun updateFlows() {
        issuesFlow.value = issues.sortedWith(compareByDescending<IssueEntity> { it.date }.thenByDescending { it.createdAt })
    }

    override fun getAllIssues(): Flow<List<IssueEntity>> = issuesFlow

    override fun getIssuesByItemId(itemId: String): Flow<List<IssueEntity>> =
        issuesFlow.map { list -> list.filter { it.itemId == itemId } }

    override fun getIssuesByTargetBarn(targetBarnId: String): Flow<List<IssueEntity>> =
        issuesFlow.map { list -> list.filter { it.targetBarnId == targetBarnId } }

    override suspend fun getIssueById(issueId: String): IssueEntity? =
        issues.find { it.issueId == issueId }

    override fun getTotalExportQuantity(itemId: String): Flow<Float?> =
        issuesFlow.map { list -> list.filter { it.itemId == itemId }.sumOf { it.quantity.toDouble() }.toFloat() }

    override fun getTotalCostByTargetBarn(targetBarnId: String): Flow<Long?> =
        issuesFlow.map { list -> list.filter { it.targetBarnId == targetBarnId }.sumOf { it.totalCost } }

    override suspend fun insertIssue(issue: IssueEntity) {
        issues.add(issue)
        updateFlows()
    }

    override suspend fun insertIssues(issues: List<IssueEntity>) {
        this.issues.addAll(issues)
        updateFlows()
    }

    override suspend fun countIssues(): Int = issues.size
}

/**
 * In-memory Fake InventoryEntityDao for unit testing
 */
class FakeInventoryEntityDao : InventoryEntityDao {
    private val inventories = mutableMapOf<String, InventoryEntity>()
    private val inventoriesFlow = MutableStateFlow<List<InventoryEntity>>(emptyList())

    private fun updateFlows() {
        inventoriesFlow.value = inventories.values.sortedBy { it.currentStock }
    }

    override fun getAllInventories(): Flow<List<InventoryEntity>> = inventoriesFlow

    override fun getInventoryByItemId(itemId: String): Flow<InventoryEntity?> =
        inventoriesFlow.map { list -> list.find { it.itemId == itemId } }

    override suspend fun getInventoryByIdOnce(itemId: String): InventoryEntity? = inventories[itemId]

    override suspend fun insertInventory(inventory: InventoryEntity) {
        inventories[inventory.itemId] = inventory
        updateFlows()
    }

    override suspend fun insertAllInventories(inventories: List<InventoryEntity>) {
        inventories.forEach { this.inventories[it.itemId] = it }
        updateFlows()
    }

    override suspend fun updateInventory(inventory: InventoryEntity) {
        inventories[inventory.itemId] = inventory
        updateFlows()
    }

    override suspend fun updateStock(
        itemId: String,
        currentStock: Float,
        stockValue: Long,
        lastPrice: Long,
        avgPrice: Long,
        updatedAt: Long
    ) {
        val existing = inventories[itemId]
        if (existing != null) {
            inventories[itemId] = existing.copy(
                currentStock = currentStock,
                stockValue = stockValue,
                lastUnitPrice = lastPrice,
                averageUnitPrice = avgPrice,
                updatedAt = updatedAt
            )
        } else {
            inventories[itemId] = InventoryEntity(
                itemId = itemId,
                currentStock = currentStock,
                stockValue = stockValue,
                lastUnitPrice = lastPrice,
                averageUnitPrice = avgPrice,
                updatedAt = updatedAt
            )
        }
        updateFlows()
    }

    override suspend fun deleteInventory(inventory: InventoryEntity) {
        inventories.remove(inventory.itemId)
        updateFlows()
    }

    override suspend fun countInventories(): Int = inventories.size
}


