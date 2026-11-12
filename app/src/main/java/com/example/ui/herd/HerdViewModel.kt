package com.example.ui.herd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.BreedingCycleEntity
import com.example.data.local.entity.EventEntity
import com.example.data.local.entity.PigEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.model.SowReproductiveStatus
import com.example.data.repository.BreedingRepository
import com.example.data.repository.TaskRepository
import com.example.domain.repository.PigRepository
import com.example.domain.usecase.AddPigUseCase
import com.example.domain.usecase.GetPigsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class HerdFilterType(val label: String) {
    ALL("Tất cả"),
    BATCH("Theo Lô"),
    INDIVIDUAL("Cá thể"),
    NEEDS_CHECK("Cần kiểm tra")
}

data class HerdUiState(
    val pigs: List<PigEntity> = emptyList(),
    val searchQuery: String = "",
    val activeFilter: HerdFilterType = HerdFilterType.ALL,
    val selectedPig: PigEntity? = null,
    val selectedPigCycles: List<BreedingCycleEntity> = emptyList(),
    val selectedPigEvents: List<EventEntity> = emptyList(),
    val selectedPigTasks: List<TaskEntity> = emptyList()
)

class HerdViewModel(
    private val getPigsUseCase: GetPigsUseCase,
    private val addPigUseCase: AddPigUseCase,
    private val breedingRepository: BreedingRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    // Secondary constructor accepting PigRepository directly
    constructor(
        pigRepository: PigRepository,
        breedingRepository: BreedingRepository,
        taskRepository: TaskRepository
    ) : this(
        getPigsUseCase = GetPigsUseCase(pigRepository),
        addPigUseCase = AddPigUseCase(pigRepository),
        breedingRepository = breedingRepository,
        taskRepository = taskRepository
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _activeFilter = MutableStateFlow(HerdFilterType.ALL)
    val activeFilter: StateFlow<HerdFilterType> = _activeFilter.asStateFlow()

    private val _selectedPigId = MutableStateFlow<String?>(null)
    val selectedPigId: StateFlow<String?> = _selectedPigId.asStateFlow()

    val uiState: StateFlow<HerdUiState> = combine(
        getPigsUseCase(),
        _searchQuery,
        _activeFilter,
        _selectedPigId
    ) { pigs, query, filter, selectedId ->
        val filtered = pigs.filter { pig ->
            val matchesQuery = query.isBlank() ||
                pig.tagCode.contains(query, ignoreCase = true) ||
                pig.location.contains(query, ignoreCase = true) ||
                pig.breed.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                HerdFilterType.ALL -> true
                HerdFilterType.BATCH -> pig.isBatch
                HerdFilterType.INDIVIDUAL -> !pig.isBatch
                HerdFilterType.NEEDS_CHECK -> pig.hasOverdueVaccine ||
                    pig.hasUltrasoundWarning ||
                    pig.hasDelayedHeatWarning ||
                    pig.hasProlongedGestationWarning
            }

            matchesQuery && matchesFilter
        }

        val selected = if (selectedId != null) pigs.find { it.id == selectedId } else null

        HerdUiState(
            pigs = filtered,
            searchQuery = query,
            activeFilter = filter,
            selectedPig = selected
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HerdUiState()
    )

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onFilterSelected(filter: HerdFilterType) {
        _activeFilter.value = filter
    }

    fun selectPig(pigId: String?) {
        _selectedPigId.value = pigId
    }

    fun getCyclesForSow(sowId: String) = breedingRepository.getCyclesForSow(sowId)
    fun getEventsForPig(pigId: String) = breedingRepository.getEventsForPig(pigId)

    fun addPig(
        tagCode: String,
        isBatch: Boolean,
        quantity: Int,
        breed: String,
        gender: String,
        status: SowReproductiveStatus,
        location: String,
        currentWeightKg: Float,
        parity: Int = 0
    ) {
        viewModelScope.launch {
            addPigUseCase(
                tagCode = tagCode,
                isBatch = isBatch,
                quantity = quantity,
                breed = breed,
                gender = gender,
                status = status,
                location = location,
                currentWeightKg = currentWeightKg,
                parity = parity
            )
        }
    }

    fun addMatingCycle(
        sowId: String,
        parity: Int,
        matingDate: Long,
        boarCode: String
    ) {
        viewModelScope.launch {
            breedingRepository.addMatingCycle(
                sowId = sowId,
                parity = parity,
                matingDate = matingDate,
                boarCode = boarCode
            )
        }
    }

    fun recordFarrowing(
        sowId: String,
        cycleId: String,
        actualDate: Long,
        totalBorn: Int,
        bornAlive: Int,
        stillborn: Int,
        mummies: Int,
        averageBirthWeightKg: Float
    ) {
        viewModelScope.launch {
            breedingRepository.recordFarrowing(
                sowId = sowId,
                cycleId = cycleId,
                actualDate = actualDate,
                totalBorn = totalBorn,
                bornAlive = bornAlive,
                stillborn = stillborn,
                mummies = mummies,
                averageBirthWeightKg = averageBirthWeightKg
            )
        }
    }

    fun recordUltrasound(
        sowId: String,
        cycleId: String,
        isPositive: Boolean
    ) {
        viewModelScope.launch {
            breedingRepository.recordUltrasound(
                sowId = sowId,
                cycleId = cycleId,
                isPositive = isPositive
            )
        }
    }
}
