package com.example.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.InventoryItemEntity
import com.example.data.local.entity.InventoryTransactionEntity
import com.example.data.local.entity.PigEntity
import com.example.data.local.entity.SystemParameterEntity
import com.example.domain.feed.FarmFeedAnalyticsResult
import com.example.domain.feed.FeedRationCalculator
import com.example.domain.repository.InventoryRepository
import com.example.domain.repository.PigRepository
import com.example.domain.repository.SystemParameterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.util.UUID

enum class InventorySubTab(val label: String) {
    STOCK("Tồn kho"),
    INBOUND("Nhập kho"),
    OUTBOUND("Xuất kho")
}

enum class StockAlertLevel {
    NORMAL,    // Đủ tồn kho an toàn (Xanh)
    WARNING,   // Sắp hết (Cam)
    EMERGENCY  // Khẩn cấp (Đỏ)
}

/**
 * Model kết nối giữa Danh mục gốc (Master Data trong Tham số hệ thống)
 * và Bảng Tồn kho hiện thời (Real-time Inventory Data).
 */
data class MasterInventoryItemUi(
    val masterId: String,               // ID trong Tham số hệ thống
    val masterCode: String,             // Mã hàng hóa (vd: CAM-NB-01, VTM-02)
    val name: String,                   // Tên hàng hóa
    val category: String,               // "FEED", "MEDICINE", "SUPPLIES"
    val unit: String,                   // Đơn vị tính (Bao 25kg, Liều, Lọ 100ml, Cái...)
    val defaultPrice: Long,             // Đơn giá tham chiếu (VNĐ)
    val minStockThreshold: Float,       // Mức tồn an toàn / Tồn tối thiểu
    val safetyDaysThreshold: Int,       // Ngưỡng ngày dùng an toàn
    val currentStock: Float,            // Số lượng tồn thực tế (Sum Nhập - Sum Xuất)
    val lastUnitPrice: Long,            // Đơn giá nhập gần nhất
    val averageUnitPrice: Long,         // Đơn giá bình quân gia quyền
    val totalStockValue: Long,          // Giá trị tồn = currentStock * averageUnitPrice
    val daysRemaining: Int?,            // Số ngày dùng còn lại (đối với Cám)
    val alertLevel: StockAlertLevel,    // NORMAL, WARNING, EMERGENCY
    val inventoryItemId: String,        // ID trong inventory_items
    val supplierOrNote: String = ""     // Nhà cung cấp / ghi chú
) {
    val isEmergency: Boolean get() = alertLevel == StockAlertLevel.EMERGENCY
    val isWarning: Boolean get() = alertLevel == StockAlertLevel.WARNING
    val isNormal: Boolean get() = alertLevel == StockAlertLevel.NORMAL
}

data class InventoryUiState(
    val items: List<InventoryItemEntity> = emptyList(),
    val transactions: List<InventoryTransactionEntity> = emptyList(),
    val masterParameters: List<SystemParameterEntity> = emptyList(),
    val masterLinkedItems: List<MasterInventoryItemUi> = emptyList(),
    val filteredMasterItems: List<MasterInventoryItemUi> = emptyList(),
    val barns: List<SystemParameterEntity> = emptyList(),
    val selectedSubTab: InventorySubTab = InventorySubTab.STOCK,
    val selectedCategoryFilter: String = "ALL", // "ALL", "FEED", "MEDICINE", "SUPPLIES"
    val searchQuery: String = "",
    val totalStockValue: Long = 0L,
    val totalItemsCount: Int = 0,
    val emergencyCount: Int = 0,
    val warningCount: Int = 0,
    val userMessage: String? = null,
    val feedAnalytics: FarmFeedAnalyticsResult? = null
)

class InventoryViewModel(
    private val inventoryRepository: InventoryRepository,
    private val systemParameterRepository: SystemParameterRepository? = null,
    private val pigRepository: PigRepository? = null
) : ViewModel() {

    private val _selectedSubTab = MutableStateFlow(InventorySubTab.STOCK)
    val selectedSubTab: StateFlow<InventorySubTab> = _selectedSubTab.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow("ALL")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private data class InventoryFilterBundle(
        val subTab: InventorySubTab,
        val catFilter: String,
        val query: String,
        val msg: String?
    )

    private val filterBundleFlow: Flow<InventoryFilterBundle> = combine(
        _selectedSubTab,
        _selectedCategoryFilter,
        _searchQuery,
        _userMessage
    ) { subTab, catFilter, query, msg ->
        InventoryFilterBundle(subTab, catFilter, query, msg)
    }

    private val masterParamsFlow: Flow<List<SystemParameterEntity>> =
        systemParameterRepository?.getAllParameters() ?: flowOf(emptyList())

    private val pigsFlow: Flow<List<PigEntity>> =
        pigRepository?.allPigs ?: flowOf(emptyList())

    // Gộp flow để tránh vượt quá 5 tham số của hàm combine
    private val dataFlowA = combine(
        inventoryRepository.allItems,
        inventoryRepository.allTransactions
    ) { items, transactions ->
        Pair(items, transactions)
    }

    private val dataFlowB = combine(
        masterParamsFlow,
        pigsFlow
    ) { params, pigs ->
        Pair(params, pigs)
    }

    val uiState: StateFlow<InventoryUiState> = combine(
        dataFlowA,
        dataFlowB,
        filterBundleFlow
    ) { pairA, pairB, filters ->
        val (inventoryItems, transactions) = pairA
        val (allParams, pigs) = pairB

        // Danh sách Master Data thuốc, cám, vật tư
        val masterCatalogParams = allParams.filter {
            it.category == SystemParameterEntity.CATEGORY_FEED ||
                it.category == SystemParameterEntity.CATEGORY_MEDICINE ||
                it.category == SystemParameterEntity.CATEGORY_SUPPLIES
        }

        // Danh sách chuồng nuôi từ Tham số hệ thống
        val barnParams = allParams.filter { it.category == SystemParameterEntity.CATEGORY_BARN }

        // Tính toán dự báo thức ăn và số ngày sử dụng còn lại theo đàn thực tế
        val feedAnalytics = try {
            val feedInStock = inventoryItems.filter { it.category == InventoryItemEntity.CATEGORY_FEED }
            FeedRationCalculator.calculate(
                pigs = pigs,
                rationMatrix = allParams,
                inventoryFeedItems = feedInStock
            )
        } catch (e: Exception) {
            null
        }

        // Kết nối Bảng danh mục gốc (Tham số hệ thống) với Tồn kho thực tế
        val masterLinked = mutableListOf<MasterInventoryItemUi>()

        // 1. Duyệt qua từng bản ghi Master Data để kế thừa thuộc tính
        masterCatalogParams.forEach { param ->
            val matchedInv = inventoryItems.find { inv ->
                inv.systemParamId == param.id ||
                    inv.id == param.id ||
                    inv.name.equals(param.name, ignoreCase = true) ||
                    (param.code.isNotBlank() && inv.name.contains(param.code, ignoreCase = true))
            }

            val currentStock = matchedInv?.currentStock ?: 0f
            val lastPrice = matchedInv?.lastUnitPrice ?: param.numericValue.toLong()
            val avgPrice = matchedInv?.averageUnitPrice ?: param.numericValue.toLong()
            val totalValue = (currentStock * avgPrice).toLong()
            val minStock = param.minStockThreshold.toFloat()
            val safetyDays = param.safetyDaysThreshold

            // Tính số ngày dùng còn lại nếu là cám
            val feedForecast = feedAnalytics?.feedResults?.find {
                it.feedCode == param.code ||
                    it.feedName.equals(param.name, ignoreCase = true) ||
                    it.feedName.contains(param.name, ignoreCase = true)
            }
            val daysRem = feedForecast?.daysRemaining

            // Xác định mức độ cảnh báo an toàn
            val alertLevel = when {
                currentStock <= 0f -> StockAlertLevel.EMERGENCY
                currentStock <= minStock -> StockAlertLevel.EMERGENCY
                daysRem != null && daysRem <= 1 -> StockAlertLevel.EMERGENCY
                daysRem != null && daysRem <= safetyDays -> StockAlertLevel.WARNING
                currentStock <= minStock * 1.5f -> StockAlertLevel.WARNING
                else -> StockAlertLevel.NORMAL
            }

            masterLinked.add(
                MasterInventoryItemUi(
                    masterId = param.id,
                    masterCode = param.code,
                    name = param.name,
                    category = param.category,
                    unit = param.unit.ifBlank { matchedInv?.unit ?: "Đơn vị" },
                    defaultPrice = param.numericValue.toLong(),
                    minStockThreshold = minStock,
                    safetyDaysThreshold = safetyDays,
                    currentStock = currentStock,
                    lastUnitPrice = lastPrice,
                    averageUnitPrice = avgPrice,
                    totalStockValue = totalValue,
                    daysRemaining = daysRem,
                    alertLevel = alertLevel,
                    inventoryItemId = matchedInv?.id ?: param.id,
                    supplierOrNote = param.secondaryValue.ifBlank { param.description }
                )
            )
        }

        // 2. Bổ sung các mặt hàng tồn kho chưa có trong Master Data (nếu có)
        inventoryItems.forEach { inv ->
            val alreadyMapped = masterLinked.any { it.inventoryItemId == inv.id || it.masterId == inv.id }
            if (!alreadyMapped) {
                val alertLevel = when {
                    inv.currentStock <= 0f -> StockAlertLevel.EMERGENCY
                    inv.currentStock <= inv.minAlertStock -> StockAlertLevel.EMERGENCY
                    inv.currentStock <= inv.minAlertStock * 1.5f -> StockAlertLevel.WARNING
                    else -> StockAlertLevel.NORMAL
                }
                masterLinked.add(
                    MasterInventoryItemUi(
                        masterId = inv.systemParamId ?: inv.id,
                        masterCode = inv.id.take(8).uppercase(),
                        name = inv.name,
                        category = inv.category,
                        unit = inv.unit,
                        defaultPrice = inv.lastUnitPrice,
                        minStockThreshold = inv.minAlertStock,
                        safetyDaysThreshold = 3,
                        currentStock = inv.currentStock,
                        lastUnitPrice = inv.lastUnitPrice,
                        averageUnitPrice = inv.averageUnitPrice,
                        totalStockValue = inv.totalStockValue,
                        daysRemaining = null,
                        alertLevel = alertLevel,
                        inventoryItemId = inv.id,
                        supplierOrNote = inv.notes
                    )
                )
            }
        }

        // Lọc theo Category và Tìm kiếm
        val filtered = masterLinked.filter { itm ->
            val matchCategory = filters.catFilter == "ALL" || itm.category == filters.catFilter
            val matchQuery = filters.query.isBlank() ||
                itm.name.contains(filters.query.trim(), ignoreCase = true) ||
                itm.masterCode.contains(filters.query.trim(), ignoreCase = true) ||
                itm.unit.contains(filters.query.trim(), ignoreCase = true)
            matchCategory && matchQuery
        }

        val totalValue = masterLinked.sumOf { it.totalStockValue }
        val emergencies = masterLinked.count { it.isEmergency }
        val warnings = masterLinked.count { it.isWarning }

        InventoryUiState(
            items = inventoryItems,
            transactions = transactions,
            masterParameters = masterCatalogParams,
            masterLinkedItems = masterLinked,
            filteredMasterItems = filtered,
            barns = barnParams,
            selectedSubTab = filters.subTab,
            selectedCategoryFilter = filters.catFilter,
            searchQuery = filters.query,
            totalStockValue = totalValue,
            totalItemsCount = filtered.size,
            emergencyCount = emergencies,
            warningCount = warnings,
            userMessage = filters.msg,
            feedAnalytics = feedAnalytics
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        InventoryUiState()
    )

    fun selectSubTab(tab: InventorySubTab) {
        _selectedSubTab.value = tab
    }

    fun selectCategoryFilter(category: String) {
        _selectedCategoryFilter.value = category
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    /**
     * Ghi nhận Phiếu Nhập kho (Subtab A)
     * - Tự động liên kết khóa ngoại với Master Data (Tham số hệ thống)
     * - Tăng ngay current_stock trong Bảng Tồn kho
     * - Cập nhật lại đơn giá tham chiếu / giá nhập mới nhất cho mặt hàng tại Tham số hệ thống
     * - Cập nhật đơn giá bình quân gia quyền
     * - Lưu phiếu nhập vào lịch sử giao dịch
     */
    fun recordInbound(
        itemId: String,
        quantity: Float,
        unitPrice: Long,
        date: Long = System.currentTimeMillis(),
        supplierOrInvoice: String = "",
        notes: String = "",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            if (quantity <= 0f) {
                _userMessage.value = "⚠️ Số lượng nhập phải lớn hơn 0"
                onError("Số lượng nhập phải lớn hơn 0")
                return@launch
            }

            // Kiểm tra xem item đã có trong kho chưa, nếu chưa có tạo bản ghi tồn kho liên kết với Tham số
            val existingItem = inventoryRepository.getItemById(itemId)
            val finalItemId = if (existingItem == null) {
                val param = systemParameterRepository?.getParameterById(itemId)
                if (param != null) {
                    val newEntity = inventoryRepository.addNewItem(
                        name = param.name,
                        category = param.category,
                        unit = param.unit.ifBlank { "Đơn vị" },
                        initialStock = 0f,
                        unitPrice = unitPrice,
                        minAlertStock = param.minStockThreshold.toFloat(),
                        notes = "Liên kết từ Tham số ${param.code}"
                    ).getOrNull()
                    newEntity?.id ?: itemId
                } else itemId
            } else itemId

            val result = inventoryRepository.recordInbound(
                itemId = finalItemId,
                quantity = quantity,
                unitPrice = unitPrice,
                date = date,
                supplierOrInvoice = supplierOrInvoice,
                notes = notes
            )

            result.onSuccess { tx ->
                // Đồng bộ ngược lại Tham số hệ thống: Cập nhật đơn giá tham chiếu mới nhất
                val matchedParam = systemParameterRepository?.getParameterById(itemId)
                    ?: systemParameterRepository?.getAllParameters()?.let {
                        // find param by name
                        null
                    }

                if (matchedParam != null) {
                    systemParameterRepository?.updateParameter(
                        matchedParam.copy(
                            numericValue = unitPrice.toDouble(),
                            secondaryValue = if (supplierOrInvoice.isNotBlank()) supplierOrInvoice else matchedParam.secondaryValue
                        )
                    )
                }

                val formattedAmount = formatCurrency(tx.totalPrice)
                _userMessage.value = "✓ Đã nhập kho: +${tx.quantity} ${tx.unit} ${tx.itemName} (${formattedAmount})"
                onSuccess()
            }.onFailure { err ->
                _userMessage.value = "⚠️ Lỗi nhập kho: ${err.message}"
                onError(err.message ?: "Lỗi nhập kho")
            }
        }
    }

    /**
     * Ghi nhận Phiếu Xuất kho (Subtab B)
     * - RÀNG BUỘC TỒN KHO: Kiểm tra tồn kho khả dụng
     * - KIỂM TRA XUẤT VƯỢT: Chặn xuất âm nếu quantity > current_stock
     * - Giảm tồn kho tức thời (Trừ trực tiếp current_stock)
     * - Phân bổ chi phí: Ghi nhận chuồng nhận và thành tiền vào chi phí hoạt động chuồng
     */
    fun recordOutbound(
        itemId: String,
        quantity: Float,
        targetBarn: String,
        date: Long = System.currentTimeMillis(),
        purposeOrNotes: String = "",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            if (quantity <= 0f) {
                _userMessage.value = "⚠️ Số lượng xuất phải lớn hơn 0"
                onError("Số lượng xuất phải lớn hơn 0")
                return@launch
            }

            val item = inventoryRepository.getItemById(itemId)
            if (item == null) {
                _userMessage.value = "⚠️ Không tìm thấy mặt hàng trong kho"
                onError("Không tìm thấy mặt hàng trong kho")
                return@launch
            }

            // Chặn xuất âm
            if (quantity > item.currentStock) {
                val err = "Chặn xuất âm: Số lượng xuất ($quantity ${item.unit}) vượt quá số lượng tồn hiện tại (${item.currentStock} ${item.unit})!"
                _userMessage.value = "⚠️ $err"
                onError(err)
                return@launch
            }

            val result = inventoryRepository.recordOutbound(
                itemId = itemId,
                quantity = quantity,
                targetBarn = targetBarn,
                date = date,
                purposeOrNotes = purposeOrNotes
            )

            result.onSuccess { tx ->
                val target = if (tx.targetBarn.isNotBlank()) " -> ${tx.targetBarn}" else ""
                val costStr = formatCurrency(tx.totalPrice)
                _userMessage.value = "✓ Đã xuất kho: -${tx.quantity} ${tx.unit} ${tx.itemName}$target (Phân bổ chi phí: $costStr)"
                onSuccess()
            }.onFailure { err ->
                _userMessage.value = "⚠️ ${err.message}"
                onError(err.message ?: "Lỗi xuất kho")
            }
        }
    }

    /**
     * Thêm mặt hàng mới vào danh mục dùng chung (Đồng bộ cả Tham số hệ thống và Kho)
     */
    fun addNewItem(
        name: String,
        category: String,
        unit: String,
        initialStock: Float,
        unitPrice: Long,
        minAlertStock: Float = 5f,
        notes: String = "",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            // 1. Thêm vào Tham số hệ thống (Master Data)
            val codePrefix = when (category) {
                InventoryItemEntity.CATEGORY_FEED -> "CAM"
                InventoryItemEntity.CATEGORY_MEDICINE -> "THUOC"
                else -> "VT"
            }
            val generatedCode = "${codePrefix}_${System.currentTimeMillis() % 10000}"

            val paramId = systemParameterRepository?.addParameter(
                category = category,
                name = name.trim(),
                code = generatedCode,
                description = notes.trim(),
                unit = unit.trim(),
                numericValue = unitPrice.toDouble(),
                minStockThreshold = minAlertStock.toDouble(),
                safetyDaysThreshold = 3
            )

            // 2. Thêm vào Bảng Tồn kho
            val result = inventoryRepository.addNewItem(
                name = name.trim(),
                category = category,
                unit = unit.trim(),
                initialStock = initialStock,
                unitPrice = unitPrice,
                minAlertStock = minAlertStock,
                notes = notes.trim()
            )

            result.onSuccess { item ->
                _userMessage.value = "✓ Đã thêm mặt hàng '${item.name}' vào Danh mục & Kho hàng"
                onSuccess()
            }.onFailure { err ->
                _userMessage.value = "⚠️ ${err.message}"
                onError(err.message ?: "Lỗi thêm mặt hàng")
            }
        }
    }

    companion object {
        fun formatCurrency(amount: Long): String {
            return try {
                val formatter = DecimalFormat("#,###")
                "${formatter.format(amount).replace(',', '.')} VNĐ"
            } catch (e: Exception) {
                "$amount VNĐ"
            }
        }
    }
}
