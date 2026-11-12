package com.example.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.SyncLogEntity
import com.example.data.local.entity.SystemParameterEntity
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.repository.SyncRepository
import com.example.domain.repository.SystemParameterRepository
import com.example.sync.SyncState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val userProfile: UserProfile = UserProfile(),
    val isAutoSync: Boolean = true,
    val isOnline: Boolean = true,
    val lastSyncTimeMs: Long = System.currentTimeMillis() - 900_000L,
    val syncState: SyncState = SyncState.Idle,
    val syncLogs: List<SyncLogEntity> = emptyList(),
    // Danh mục tham số dùng chung theo 6 tiểu mục + chuồng & loại thải
    val boarParameters: List<SystemParameterEntity> = emptyList(),
    val staffParameters: List<SystemParameterEntity> = emptyList(),
    val breedParameters: List<SystemParameterEntity> = emptyList(),
    val feedParameters: List<SystemParameterEntity> = emptyList(),
    val medicineParameters: List<SystemParameterEntity> = emptyList(),
    val suppliesParameters: List<SystemParameterEntity> = emptyList(),
    val barnParameters: List<SystemParameterEntity> = emptyList(),
    val cullingParameters: List<SystemParameterEntity> = emptyList(),
    val vaccineParameters: List<SystemParameterEntity> = emptyList(),
    val feedRationParameters: List<SystemParameterEntity> = emptyList(),
    val selectedParameterTab: String = SystemParameterEntity.CATEGORY_BOAR,
    val searchQuery: String = "",
    val userMessage: String? = null
)

private data class SyncBundle(
    val isAutoSync: Boolean,
    val isOnline: Boolean,
    val lastSyncTime: Long,
    val syncState: SyncState
)

private data class TabFilterBundle(
    val selectedTab: String,
    val searchQuery: String
)

class SettingsViewModel(
    private val syncRepository: SyncRepository,
    private val systemParameterRepository: SystemParameterRepository? = null
) : ViewModel() {

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _selectedParameterTab = MutableStateFlow(SystemParameterEntity.CATEGORY_BOAR)
    val selectedParameterTab: StateFlow<String> = _selectedParameterTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)

    private val syncBundleFlow = combine(
        syncRepository.isAutoSync,
        syncRepository.isOnline,
        syncRepository.lastSyncTime,
        syncRepository.syncState
    ) { isAutoSync, isOnline, lastSync, syncState ->
        SyncBundle(isAutoSync, isOnline, lastSync, syncState)
    }

    private val tabFilterFlow = combine(
        _selectedParameterTab,
        _searchQuery
    ) { tab, query ->
        TabFilterBundle(tab, query)
    }

    private val parametersFlow = systemParameterRepository?.getAllParameters()
        ?: MutableStateFlow(emptyList())

    val uiState: StateFlow<SettingsUiState> = combine(
        _userProfile,
        syncBundleFlow,
        syncRepository.syncLogs,
        parametersFlow,
        tabFilterFlow
    ) { profile, bundle, logs, parameters, tabFilter ->
        val boars = parameters.filter { it.category == SystemParameterEntity.CATEGORY_BOAR }
        val staff = parameters.filter { it.category == SystemParameterEntity.CATEGORY_STAFF }
        val breeds = parameters.filter { it.category == SystemParameterEntity.CATEGORY_BREED }
        val feeds = parameters.filter { it.category == SystemParameterEntity.CATEGORY_FEED }
        val medicines = parameters.filter { it.category == SystemParameterEntity.CATEGORY_MEDICINE }
        val supplies = parameters.filter { it.category == SystemParameterEntity.CATEGORY_SUPPLIES }
        val barns = parameters.filter { it.category == SystemParameterEntity.CATEGORY_BARN }
        val cullingReasons = parameters.filter { it.category == SystemParameterEntity.CATEGORY_CULLING_REASON }
        val vaccines = parameters.filter { it.category == SystemParameterEntity.CATEGORY_VACCINE }
        val feedRations = parameters.filter { it.category == SystemParameterEntity.CATEGORY_FEED_RATION }

        SettingsUiState(
            userProfile = profile,
            isAutoSync = bundle.isAutoSync,
            isOnline = bundle.isOnline,
            lastSyncTimeMs = bundle.lastSyncTime,
            syncState = bundle.syncState,
            syncLogs = logs,
            boarParameters = boars,
            staffParameters = staff,
            breedParameters = breeds,
            feedParameters = feeds,
            medicineParameters = medicines,
            suppliesParameters = supplies,
            barnParameters = barns,
            cullingParameters = cullingReasons,
            vaccineParameters = vaccines,
            feedRationParameters = feedRations,
            selectedParameterTab = tabFilter.selectedTab,
            searchQuery = tabFilter.searchQuery,
            userMessage = _userMessage.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun toggleAutoSync(enabled: Boolean) {
        syncRepository.toggleAutoSync(enabled)
    }

    fun toggleOnlineMode(online: Boolean) {
        syncRepository.toggleOnline(online)
    }

    fun switchRole() {
        val currentRole = _userProfile.value.role
        val nextRole = if (currentRole == UserRole.MANAGER) UserRole.TECHNICIAN else UserRole.MANAGER
        _userProfile.value = _userProfile.value.copy(
            role = nextRole,
            name = if (nextRole == UserRole.MANAGER) "Nguyễn Văn Tuấn" else "Trần Đình Khang"
        )
    }

    fun triggerSync(onComplete: ((Boolean) -> Unit)? = null) {
        syncRepository.triggerSync(onComplete)
    }

    fun resolveConflict(logId: String) {
        viewModelScope.launch {
            syncRepository.resolveConflictManually(logId)
        }
    }

    fun selectParameterTab(tab: String) {
        _selectedParameterTab.value = tab
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun addSystemParameter(
        category: String,
        name: String,
        code: String,
        description: String = "",
        unit: String = "",
        subType: String = "",
        secondaryValue: String = "",
        numericValue: Double = 0.0,
        statusText: String = "Đang hoạt động",
        safetyDaysThreshold: Int = 3,
        minStockThreshold: Double = 20.0
    ) {
        viewModelScope.launch {
            systemParameterRepository?.addParameter(
                category = category,
                name = name,
                code = code,
                description = description,
                unit = unit,
                subType = subType,
                secondaryValue = secondaryValue,
                numericValue = numericValue,
                statusText = statusText,
                safetyDaysThreshold = safetyDaysThreshold,
                minStockThreshold = minStockThreshold
            )
            _userMessage.value = "Đã lưu tham số: $name"
        }
    }

    fun updateBoarDoseStock(parameter: SystemParameterEntity, delta: Int) {
        val current = parameter.numericValue
        val updated = (current + delta).coerceAtLeast(0.0)
        val updatedStatus = if (updated <= 0.0) "Đã hết" else "Đang hoạt động"
        viewModelScope.launch {
            systemParameterRepository?.updateParameter(
                parameter.copy(
                    numericValue = updated,
                    statusText = updatedStatus
                )
            )
            _userMessage.value = "Đã cập nhật liều tinh cho ${parameter.name}: ${updated.toInt()} liều"
        }
    }

    fun updateSystemParameter(parameter: SystemParameterEntity) {
        viewModelScope.launch {
            systemParameterRepository?.updateParameter(parameter)
            _userMessage.value = "Đã cập nhật: ${parameter.name}"
        }
    }

    fun deleteSystemParameter(parameter: SystemParameterEntity) {
        viewModelScope.launch {
            systemParameterRepository?.deleteParameter(parameter)
            _userMessage.value = "Đã xóa: ${parameter.name}"
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
