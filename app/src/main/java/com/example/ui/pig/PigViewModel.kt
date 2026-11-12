package com.example.ui.pig

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.PigEntity
import com.example.data.local.entity.SystemParameterEntity
import com.example.data.model.SowReproductiveStatus
import com.example.domain.repository.PigRepository
import com.example.domain.repository.SystemParameterRepository
import com.example.domain.usecase.AddPigUseCase
import com.example.domain.usecase.DeletePigUseCase
import com.example.domain.usecase.GetPigByIdUseCase
import com.example.domain.usecase.GetPigStatsUseCase
import com.example.domain.usecase.GetPigsUseCase
import com.example.domain.usecase.UpdatePigUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class PigFilterType(val label: String) {
    ALL("Tất cả"),
    BATCH("Theo Lô"),
    INDIVIDUAL("Cá thể"),
    NEEDS_CHECK("Cần kiểm tra")
}

enum class PigSortOption(val label: String) {
    TAG_ID("Mã số tai (Tag ID)"),
    BIRTH_DATE("Ngày sinh")
}

enum class SortDirection(val label: String) {
    ASCENDING("Tăng dần"),
    DESCENDING("Giảm dần")
}

private data class FilterSortSelectionBundle(
    val query: String,
    val filter: PigFilterType,
    val sortOption: PigSortOption,
    val sortDirection: SortDirection,
    val selectedId: String?,
    val selectedBarn: String?
)

data class PigUiState(
    val pigs: List<PigEntity> = emptyList(),
    val totalRecordsCount: Int = 0,
    val totalHeadCount: Int = 0,
    val searchQuery: String = "",
    val activeFilter: PigFilterType = PigFilterType.ALL,
    val sortOption: PigSortOption = PigSortOption.TAG_ID,
    val sortDirection: SortDirection = SortDirection.ASCENDING,
    val healthDistribution: HealthDistribution = HealthDistribution(),
    val herdStructure: FarmHerdStructure = FarmHerdStructure(),
    val barnDistribution: List<Barn3DColumnData> = emptyList(),
    val selectedBarnFilter: String? = null,
    val selectedPig: PigEntity? = null,
    val isLoading: Boolean = false,
    val userMessage: String? = null
)

/**
 * ViewModel managing UI state and user actions for pig management.
 * Leverages the Domain Use Cases to encapsulate business logic,
 * supporting tag ID search, sorting by tag ID/birth date, health distribution computation,
 * and 3D Barn distribution drill-down.
 */
open class PigViewModel(
    private val getPigsUseCase: GetPigsUseCase,
    private val getPigByIdUseCase: GetPigByIdUseCase,
    private val addPigUseCase: AddPigUseCase,
    private val updatePigUseCase: UpdatePigUseCase,
    private val deletePigUseCase: DeletePigUseCase,
    private val getPigStatsUseCase: GetPigStatsUseCase,
    private val farmRuleEngine: com.example.domain.rule.FarmRuleEngine? = null,
    private val systemParameterRepository: SystemParameterRepository? = null
) : ViewModel() {

    // Secondary constructor accepting repository directly for convenience
    constructor(
        pigRepository: PigRepository,
        farmRuleEngine: com.example.domain.rule.FarmRuleEngine? = null,
        systemParameterRepository: SystemParameterRepository? = null
    ) : this(
        getPigsUseCase = GetPigsUseCase(pigRepository),
        getPigByIdUseCase = GetPigByIdUseCase(pigRepository),
        addPigUseCase = AddPigUseCase(pigRepository),
        updatePigUseCase = UpdatePigUseCase(pigRepository),
        deletePigUseCase = DeletePigUseCase(pigRepository),
        getPigStatsUseCase = GetPigStatsUseCase(pigRepository),
        farmRuleEngine = farmRuleEngine,
        systemParameterRepository = systemParameterRepository
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _activeFilter = MutableStateFlow(PigFilterType.ALL)
    val activeFilter: StateFlow<PigFilterType> = _activeFilter.asStateFlow()

    private val _sortOption = MutableStateFlow(PigSortOption.TAG_ID)
    val sortOption: StateFlow<PigSortOption> = _sortOption.asStateFlow()

    private val _sortDirection = MutableStateFlow(SortDirection.ASCENDING)
    val sortDirection: StateFlow<SortDirection> = _sortDirection.asStateFlow()

    private val _selectedPigId = MutableStateFlow<String?>(null)
    val selectedPigId: StateFlow<String?> = _selectedPigId.asStateFlow()

    private val _selectedBarnFilter = MutableStateFlow<String?>(null)
    val selectedBarnFilter: StateFlow<String?> = _selectedBarnFilter.asStateFlow()

    fun onBarnSelected(barnName: String?) {
        _selectedBarnFilter.value = if (_selectedBarnFilter.value == barnName) null else barnName
    }

    fun clearBarnFilter() {
        _selectedBarnFilter.value = null
    }

    private val _userMessage = MutableStateFlow<String?>(null)

    private val barnParametersFlow = systemParameterRepository?.getParametersByCategory(SystemParameterEntity.CATEGORY_BARN)
        ?: MutableStateFlow<List<SystemParameterEntity>>(emptyList())

    private val filterSortSelectionFlow = combine(
        combine(_searchQuery, _activeFilter, _sortOption) { q, f, s -> Triple(q, f, s) },
        combine(_sortDirection, _selectedPigId, _selectedBarnFilter) { d, id, barn -> Triple(d, id, barn) }
    ) { first, second ->
        FilterSortSelectionBundle(
            query = first.first,
            filter = first.second,
            sortOption = first.third,
            sortDirection = second.first,
            selectedId = second.second,
            selectedBarn = second.third
        )
    }

    val uiState: StateFlow<PigUiState> = combine(
        getPigsUseCase(),
        getPigStatsUseCase(),
        barnParametersFlow,
        filterSortSelectionFlow
    ) { pigs, stats, barns, bundle ->
        // 1. Tính toán phân bổ sức khỏe đàn heo (Health Distribution) cho biểu đồ tròn (Pie Chart)
        val totalCount = pigs.size
        val overdueVaccineCount = pigs.count { it.hasOverdueVaccine }
        val ultrasoundWarningCount = pigs.count { it.hasUltrasoundWarning }
        val reproductiveWarningCount = pigs.count { it.hasDelayedHeatWarning || it.hasProlongedGestationWarning }
        val healthyCount = pigs.count {
            !it.hasOverdueVaccine && !it.hasUltrasoundWarning && !it.hasDelayedHeatWarning && !it.hasProlongedGestationWarning
        }

        val slices = if (totalCount > 0) {
            listOf(
                HealthStatusSlice(
                    id = "healthy",
                    label = "Khỏe mạnh",
                    count = healthyCount,
                    percentage = (healthyCount.toFloat() / totalCount) * 100f,
                    color = Color(0xFF15803D), // Healthy Green
                    description = "Thể trạng chuẩn, không có cảnh báo"
                ),
                HealthStatusSlice(
                    id = "overdue_vaccine",
                    label = "Quá hạn vắc-xin",
                    count = overdueVaccineCount,
                    percentage = (overdueVaccineCount.toFloat() / totalCount) * 100f,
                    color = Color(0xFFDC2626), // Bold Warning Red
                    description = "Cần tiêm phòng bổ sung ngay"
                ),
                HealthStatusSlice(
                    id = "ultrasound",
                    label = "Cần siêu âm",
                    count = ultrasoundWarningCount,
                    percentage = (ultrasoundWarningCount.toFloat() / totalCount) * 100f,
                    color = Color(0xFFD97706), // Bold Warning Amber
                    description = "Mốc khám thai D+25 đến D+30"
                ),
                HealthStatusSlice(
                    id = "reproductive",
                    label = "Cảnh báo sinh sản",
                    count = reproductiveWarningCount,
                    percentage = (reproductiveWarningCount.toFloat() / totalCount) * 100f,
                    color = Color(0xFFBE123C), // Alert Rose / Crimson
                    description = "Chậm lên giống hoặc mang thai quá hạn"
                )
            )
        } else emptyList()

        val healthDistribution = HealthDistribution(
            totalCount = totalCount,
            healthyCount = healthyCount,
            overdueVaccineCount = overdueVaccineCount,
            ultrasoundWarningCount = ultrasoundWarningCount,
            reproductiveWarningCount = reproductiveWarningCount,
            slices = slices
        )

        // 2. Tính toán tổng hợp số lượng 5 loại heo trong trại (Cơ cấu đàn heo)
        var pregnantCount = 0
        var lactatingCount = 0
        var waitingCount = 0
        var sucklingCount = 0
        var meatCount = 0

        pigs.forEach { pig ->
            when {
                // Heo con theo mẹ
                pig.tagCode.contains("heo con", ignoreCase = true) ||
                pig.breed.contains("heo con", ignoreCase = true) ||
                (pig.location.contains("chuồng đẻ", ignoreCase = true) && pig.isBatch) -> {
                    sucklingCount += pig.quantity
                }
                // Nái bầu
                pig.status == SowReproductiveStatus.MANG_THAI -> {
                    pregnantCount += pig.quantity
                }
                // Nái nuôi con
                pig.status == SowReproductiveStatus.NUOI_CON -> {
                    lactatingCount += pig.quantity
                }
                // Nái chờ phối
                pig.status == SowReproductiveStatus.CAI_SUA_CHO_PHOI ||
                pig.status == SowReproductiveStatus.HAU_BI -> {
                    waitingCount += pig.quantity
                }
                // Heo thịt
                else -> {
                    meatCount += pig.quantity
                }
            }
        }

        // Tự động tính số heo con theo mẹ nếu chưa có lô ghi nhận riêng
        if (sucklingCount == 0 && lactatingCount > 0) {
            sucklingCount = lactatingCount * 11
        }

        val herdStructure = FarmHerdStructure(
            pregnantSows = pregnantCount,
            lactatingSows = lactatingCount,
            waitingMatingSows = waitingCount,
            sucklingPiglets = sucklingCount,
            meatPigs = meatCount,
            totalHeads = pregnantCount + lactatingCount + waitingCount + sucklingCount + meatCount
        )

        // 3. Tính toán Phân bố chuồng với biểu đồ 3D Chart theo trục hoành động từ Tham số hệ thống
        val barnDistribution = BarnDistributionCalculator.calculate(pigs, barns)

        // 4. Lọc theo từ khóa tìm kiếm (ưu tiên Tag ID), bộ lọc tab và tương tác chạm Drill-down theo Chuồng
        val trimmedQuery = bundle.query.trim()
        val filtered = pigs.filter { pig ->
            val matchesQuery = trimmedQuery.isBlank() ||
                pig.tagCode.contains(trimmedQuery, ignoreCase = true) ||
                pig.location.contains(trimmedQuery, ignoreCase = true) ||
                pig.breed.contains(trimmedQuery, ignoreCase = true)

            val matchesFilter = when (bundle.filter) {
                PigFilterType.ALL -> true
                PigFilterType.BATCH -> pig.isBatch
                PigFilterType.INDIVIDUAL -> !pig.isBatch
                PigFilterType.NEEDS_CHECK -> pig.hasOverdueVaccine ||
                    pig.hasUltrasoundWarning ||
                    pig.hasDelayedHeatWarning ||
                    pig.hasProlongedGestationWarning
            }

            // Tương tác chạm (Touch Drill-down): Lọc ngay danh sách heo trong chuồng được chọn trên biểu đồ 3D
            val matchesBarn = bundle.selectedBarn == null ||
                BarnDistributionCalculator.matchPigToBarn(pig, bundle.selectedBarn)

            matchesQuery && matchesFilter && matchesBarn
        }

        // 5. Sắp xếp danh sách heo theo Ngày sinh hoặc Mã tai (Tag ID)
        val sorted = when (bundle.sortOption) {
            PigSortOption.TAG_ID -> {
                if (bundle.sortDirection == SortDirection.ASCENDING) {
                    filtered.sortedBy { it.tagCode.lowercase() }
                } else {
                    filtered.sortedByDescending { it.tagCode.lowercase() }
                }
            }
            PigSortOption.BIRTH_DATE -> {
                if (bundle.sortDirection == SortDirection.ASCENDING) {
                    filtered.sortedBy { it.birthDate }
                } else {
                    filtered.sortedByDescending { it.birthDate }
                }
            }
        }

        val selected = if (bundle.selectedId != null) pigs.find { it.id == bundle.selectedId } else null

        PigUiState(
            pigs = sorted,
            totalRecordsCount = stats.recordCount,
            totalHeadCount = stats.totalHeadCount,
            searchQuery = bundle.query,
            activeFilter = bundle.filter,
            sortOption = bundle.sortOption,
            sortDirection = bundle.sortDirection,
            healthDistribution = healthDistribution,
            herdStructure = herdStructure,
            barnDistribution = barnDistribution,
            selectedBarnFilter = bundle.selectedBarn,
            selectedPig = selected,
            userMessage = _userMessage.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PigUiState()
    )

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun clearSearchQuery() {
        _searchQuery.value = ""
    }

    fun filterByTagId(tagId: String) {
        _searchQuery.value = tagId
    }

    fun onFilterSelected(filter: PigFilterType) {
        _activeFilter.value = filter
    }

    fun setSortOption(option: PigSortOption) {
        _sortOption.value = option
    }

    fun setSortDirection(direction: SortDirection) {
        _sortDirection.value = direction
    }

    /**
     * Chuyển đổi giữa tiêu chí sắp xếp theo Mã số tai (Tag ID) và Ngày sinh.
     */
    fun toggleSortOption() {
        _sortOption.value = if (_sortOption.value == PigSortOption.TAG_ID) {
            PigSortOption.BIRTH_DATE
        } else {
            PigSortOption.TAG_ID
        }
    }

    /**
     * Chuyển đổi giữa chiều sắp xếp Tăng dần (ASC) và Giảm dần (DESC).
     */
    fun toggleSortDirection() {
        _sortDirection.value = if (_sortDirection.value == SortDirection.ASCENDING) {
            SortDirection.DESCENDING
        } else {
            SortDirection.ASCENDING
        }
    }

    fun selectPig(pigId: String?) {
        _selectedPigId.value = pigId
    }

    fun addPig(
        tagCode: String,
        isBatch: Boolean,
        quantity: Int,
        breed: String,
        gender: String,
        status: SowReproductiveStatus,
        location: String,
        currentWeightKg: Float,
        parity: Int = 0,
        birthDate: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            val result = addPigUseCase(
                tagCode = tagCode,
                isBatch = isBatch,
                quantity = quantity,
                breed = breed,
                gender = gender,
                status = status,
                location = location,
                currentWeightKg = currentWeightKg,
                parity = parity,
                birthDate = birthDate
            )
            if (result.isSuccess) {
                _userMessage.value = "Đã thêm thành công: $tagCode"
            } else {
                _userMessage.value = result.exceptionOrNull()?.message ?: "Lỗi thêm heo mới"
            }
        }
    }

    fun updatePig(pig: PigEntity) {
        viewModelScope.launch {
            val result = updatePigUseCase(pig)
            if (result.isSuccess) {
                _userMessage.value = "Đã cập nhật: ${pig.tagCode}"
            } else {
                _userMessage.value = result.exceptionOrNull()?.message ?: "Lỗi cập nhật heo"
            }
        }
    }

    fun deletePig(pig: PigEntity) {
        viewModelScope.launch {
            deletePigUseCase(pig)
            _userMessage.value = "Đã xóa: ${pig.tagCode}"
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    // =========================================================================
    // EVENT PROCESSING METHODS (BUSINESS EVENTS -> RULE ENGINE)
    // =========================================================================

    fun recordFarrowing(
        sowId: String,
        farrowingDate: Long,
        totalBorn: Int,
        bornAlive: Int,
        stillborn: Int,
        mummies: Int,
        litterWeightKg: Float,
        notes: String = "",
        onSuccess: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            if (farmRuleEngine != null) {
                val pigletBatchTag = farmRuleEngine.onFarrowingEvent(
                    sowId = sowId,
                    farrowingDate = farrowingDate,
                    totalBorn = totalBorn,
                    bornAlive = bornAlive,
                    stillborn = stillborn,
                    mummies = mummies,
                    litterWeightKg = litterWeightKg,
                    notes = notes
                )
                _userMessage.value = "Đã lưu sự kiện đẻ: Đàn $pigletBatchTag ($bornAlive con) & 5 việc tự động"
                onSuccess(pigletBatchTag)
            } else {
                _userMessage.value = "Chưa cấu hình FarmRuleEngine"
            }
        }
    }

    fun recordIronInjection(
        pigletTag: String,
        injectionDate: Long,
        quantity: Int,
        drugName: String = "Iron Dextran",
        dosageMl: Float = 1.0f,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            farmRuleEngine?.onIronInjectionEvent(
                pigletTag = pigletTag,
                injectionDate = injectionDate,
                quantityInjected = quantity,
                drugName = drugName,
                dosageMl = dosageMl
            )
            _userMessage.value = "Đã tiêm sắt cho $quantity con đàn $pigletTag ($drugName $dosageMl ml)"
            onSuccess()
        }
    }

    fun recordWeaning(
        pigletTag: String,
        weaningDate: Long,
        weanedQuantity: Int,
        averageWeightKg: Float,
        sowTag: String? = null,
        newLocation: String = "Khu Cai Sữa A-01",
        onSuccess: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            if (farmRuleEngine != null) {
                val newBatch = farmRuleEngine.onWeaningEvent(
                    pigletTag = pigletTag,
                    weaningDate = weaningDate,
                    weanedQuantity = weanedQuantity,
                    averageWeightKg = averageWeightKg,
                    sowTag = sowTag,
                    newLocation = newLocation
                )
                _userMessage.value = "Đã cai sữa đàn $pigletTag -> Tạo đàn $newBatch ($weanedQuantity con, $averageWeightKg kg)"
                onSuccess(newBatch)
            }
        }
    }

    fun recordBatchIntake(
        batchCode: String,
        intakeDate: Long,
        quantity: Int,
        averageWeightKg: Float,
        location: String,
        breed: String = "Duroc x Landrace",
        onSuccess: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            if (farmRuleEngine != null) {
                val code = farmRuleEngine.onBatchIntakeEvent(
                    batchCode = batchCode,
                    intakeDate = intakeDate,
                    quantity = quantity,
                    averageWeightKg = averageWeightKg,
                    location = location,
                    breed = breed
                )
                _userMessage.value = "Đã nhập lô heo thịt $code ($quantity con) & kích hoạt 5 việc tự động"
                onSuccess(code)
            }
        }
    }

    fun recordWeighing(
        batchCode: String,
        weighDate: Long,
        headCount: Int,
        averageWeightKg: Float,
        onResult: (adg: Float, gain: Float, daysToMarket: Int) -> Unit = { _, _, _ -> }
    ) {
        viewModelScope.launch {
            if (farmRuleEngine != null) {
                val (adg, gain, daysToMarket) = farmRuleEngine.onWeighingEvent(
                    batchCode = batchCode,
                    weighDate = weighDate,
                    headCount = headCount,
                    averageWeightKg = averageWeightKg
                )
                _userMessage.value = "Lô $batchCode: ADG ${String.format(java.util.Locale.US, "%.2f", adg)} kg/ngày, dự kiến xuất bán sau $daysToMarket ngày"
                onResult(adg, gain, daysToMarket)
            }
        }
    }

    fun recordTreatment(
        batchOrTag: String,
        treatmentDate: Long,
        sickCount: Int,
        diseaseName: String,
        medicineName: String,
        notes: String = "",
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            farmRuleEngine?.onTreatmentEvent(
                batchCode = batchOrTag,
                treatmentDate = treatmentDate,
                sickCount = sickCount,
                diseaseName = diseaseName,
                medicineName = medicineName,
                notes = notes
            )
            _userMessage.value = "Đã ghi nhận điều trị $diseaseName cho $sickCount con ($batchOrTag)"
            onSuccess()
        }
    }

    fun recordBatchSale(
        batchCode: String,
        saleDate: Long,
        soldQuantity: Int,
        averageWeightKg: Float,
        customerName: String,
        salePriceVnd: String = "",
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            farmRuleEngine?.onBatchSaleEvent(
                batchCode = batchCode,
                saleDate = saleDate,
                soldQuantity = soldQuantity,
                averageWeightKg = averageWeightKg,
                customerName = customerName,
                salePriceVnd = salePriceVnd
            )
            val priceMsg = if (salePriceVnd.isNotBlank()) " • Giá: $salePriceVnd" else ""
            _userMessage.value = "Đã xuất bán lô $batchCode ($soldQuantity con, $averageWeightKg kg) -> Khách: $customerName$priceMsg"
            onSuccess()
        }
    }

    fun recordSowCull(
        sowTag: String,
        cullDate: Long,
        reason: String,
        weightKg: Float,
        salePrice: String,
        buyerName: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            farmRuleEngine?.onSowCullEvent(
                sowTag = sowTag,
                cullDate = cullDate,
                reason = reason,
                weightKg = weightKg,
                salePrice = salePrice,
                buyerName = buyerName
            )
            _userMessage.value = "Đã loại thải nái $sowTag (Lý do: $reason) -> Chuyển trạng thái Loại thải"
            onSuccess()
        }
    }

    fun recordPigletCull(
        pigletTag: String,
        cullDate: Long,
        cullQuantity: Int,
        reason: String,
        averageWeightKg: Float = 0f,
        disposition: String = "Tiêu hủy",
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            farmRuleEngine?.onPigletCullEvent(
                pigletTag = pigletTag,
                cullDate = cullDate,
                cullQuantity = cullQuantity,
                reason = reason,
                averageWeightKg = averageWeightKg,
                disposition = disposition
            )
            _userMessage.value = "Đã loại thải $cullQuantity con đàn $pigletTag (Lý do: $reason)"
            onSuccess()
        }
    }

    fun recordGrowerCull(
        batchCode: String,
        cullDate: Long,
        cullQuantity: Int,
        reason: String,
        averageWeightKg: Float,
        salePrice: String = "",
        buyerName: String = "",
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            farmRuleEngine?.onGrowerCullEvent(
                batchCode = batchCode,
                cullDate = cullDate,
                cullQuantity = cullQuantity,
                reason = reason,
                averageWeightKg = averageWeightKg,
                salePrice = salePrice,
                buyerName = buyerName
            )
            _userMessage.value = "Đã loại thải $cullQuantity con lô thịt $batchCode (Lý do: $reason)"
            onSuccess()
        }
    }

    fun recordMating(
        sowTag: String,
        matingDate: Long,
        boarTag: String,
        technician: String = "Nguyễn Văn Tuấn",
        notes: String = "",
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            farmRuleEngine?.onMatingEvent(
                sowTag = sowTag,
                matingDate = matingDate,
                boarTag = boarTag,
                technician = technician,
                notes = notes
            )
            _userMessage.value = "Đã ghi nhận phối giống nái $sowTag (Nọc: $boarTag) & sinh 5 việc thai kỳ"
            onSuccess()
        }
    }

    fun recordPregnancyCheck(
        sowTag: String,
        checkDate: Long,
        isPregnant: Boolean,
        onSuccess: () -> Unit = {}
    ) {
        recordPregnancyCheckResult(
            sowTag = sowTag,
            checkDate = checkDate,
            result = if (isPregnant) "PREGNANT" else "NOT_PREGNANT",
            onSuccess = onSuccess
        )
    }

    fun recordPregnancyCheckResult(
        sowTag: String,
        checkDate: Long,
        result: String, // "PREGNANT", "NOT_PREGNANT", "DOUBTFUL"
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            farmRuleEngine?.onPregnancyCheckResultEvent(
                sowTag = sowTag,
                checkDate = checkDate,
                result = result
            )
            _userMessage.value = when (result) {
                "PREGNANT" -> "Nái $sowTag: Đậu thai (+) -> Đã kích hoạt lịch chuẩn bị đẻ D+107"
                "NOT_PREGNANT" -> "Nái $sowTag: Không đậu (-) -> Đã chuyển chờ phối lại"
                else -> "Nái $sowTag: Nghi ngờ (?) -> Đã hẹn lịch siêu âm lại sau 7 ngày"
            }
            onSuccess()
        }
    }

    fun recordPigletVaccine(
        pigletTag: String,
        vaccineDate: Long,
        quantity: Int,
        vaccineName: String,
        dosage: String = "2ml/con",
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            farmRuleEngine?.onPigletVaccineEvent(
                pigletTag = pigletTag,
                vaccineDate = vaccineDate,
                quantity = quantity,
                vaccineName = vaccineName,
                dosage = dosage
            )
            _userMessage.value = "Đã tiêm vắc-xin $vaccineName cho đàn $pigletTag"
            onSuccess()
        }
    }

    fun recordTailDocking(
        pigletTag: String,
        dockDate: Long,
        quantity: Int,
        notes: String = "",
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            farmRuleEngine?.onTailDockingEvent(
                pigletTag = pigletTag,
                dockDate = dockDate,
                quantity = quantity,
                notes = notes
            )
            _userMessage.value = "Đã cắt đuôi & bấm nanh cho đàn $pigletTag ($quantity con)"
            onSuccess()
        }
    }

    fun recordPigletCare(
        pigletTag: String,
        date: Long,
        procedureType: String,
        vaccineName: String,
        tech: String,
        dosage: String,
        notes: String = "",
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            when (procedureType) {
                "Tiêm sắt" -> farmRuleEngine?.onIronInjectionEvent(pigletTag, date, 11, "Iron Dextran", 1.0f, tech)
                "Cắt đuôi" -> farmRuleEngine?.onTailDockingEvent(pigletTag, date, 11, notes, tech)
                else -> farmRuleEngine?.onPigletVaccineEvent(pigletTag, date, 11, vaccineName.ifBlank { "Vaccine phòng bệnh" }, dosage, tech)
            }
            _userMessage.value = "Đã ghi nhận $procedureType cho đàn $pigletTag"
            onSuccess()
        }
    }

    fun recordRelocation(
        batchCode: String,
        moveDate: Long,
        newLocation: String,
        notes: String = "",
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            farmRuleEngine?.onRelocationEvent(
                batchCode = batchCode,
                moveDate = moveDate,
                newLocation = newLocation,
                notes = notes
            )
            _userMessage.value = "Đã chuyển lô $batchCode sang $newLocation"
            onSuccess()
        }
    }

    fun recordSowRelocation(
        sowTag: String,
        moveDate: Long,
        fromLocation: String,
        toLocation: String,
        notes: String = "",
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            farmRuleEngine?.onSowRelocationEvent(
                sowTag = sowTag,
                moveDate = moveDate,
                fromLocation = fromLocation,
                toLocation = toLocation,
                notes = notes
            )
            _userMessage.value = "Đã chuyển nái $sowTag: $fromLocation -> $toLocation"
            onSuccess()
        }
    }

    fun recordGrowerRelocation(
        fromPen: String,
        toPen: String,
        moveDate: Long,
        quantity: Int,
        totalWeightKg: Float,
        avgAgeDays: Int,
        batchCode: String? = null,
        notes: String = "",
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            farmRuleEngine?.onGrowerRelocationEvent(
                fromPen = fromPen,
                toPen = toPen,
                moveDate = moveDate,
                quantity = quantity,
                totalWeightKg = totalWeightKg,
                avgAgeDays = avgAgeDays,
                batchCode = batchCode,
                notes = notes
            )
            _userMessage.value = "Đã chuyển chuồng thịt: $fromPen -> $toPen ($quantity con)"
            onSuccess()
        }
    }

    fun recordNewSowIntake(
        tagCode: String,
        breed: String,
        birthDate: Long,
        parity: Int,
        location: String,
        notes: String = "",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                farmRuleEngine?.onNewSowIntakeEvent(
                    tagCode = tagCode,
                    breed = breed,
                    birthDate = birthDate,
                    parity = parity,
                    location = location,
                    notes = notes
                )
                _userMessage.value = "Đã nhập mới nái $tagCode (Lứa $parity, Chuồng $location)"
                onSuccess()
            } catch (e: Exception) {
                _userMessage.value = e.message ?: "Lỗi nhập nái"
                onError(e.message ?: "Lỗi nhập nái")
            }
        }
    }
}
