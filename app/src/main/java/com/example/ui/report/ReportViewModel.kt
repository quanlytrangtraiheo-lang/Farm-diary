package com.example.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.BreedingRepository
import com.example.domain.repository.PigRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class ReportPeriod(val label: String) {
    THIS_MONTH("Tháng này"),
    THIS_QUARTER("Quý này"),
    ALL("Toàn bộ"),
    BY_LOT("Theo lô")
}

data class WeighingRecord(
    val title: String,
    val timeLabel: String,
    val weightStr: String,
    val gainStr: String,
    val evaluation: String
)

data class ReportUiState(
    val period: ReportPeriod = ReportPeriod.THIS_MONTH,
    val averageWeightKg: Float = 68.5f,
    val adgDailyGainKg: Float = 1.2f,
    val fcrIndex: Float = 2.42f,
    val conceptionRate: Float = 88.5f,
    val weanedPigletsPerSowPerYear: Float = 25.4f,
    val weightTrendData: List<Pair<String, Float>> = listOf(
        Pair("05/10", 35.0f),
        Pair("Tuần 2", 45.2f),
        Pair("Tuần 3", 56.8f),
        Pair("05/11", 68.5f)
    ),
    val recentWeighingRecords: List<WeighingRecord> = listOf(
        WeighingRecord("Cân định kỳ Lô LH-204", "Hôm nay, 10:30", "75.2 kg", "+4.5 kg", "Tốt"),
        WeighingRecord("Cân định kỳ Lô L-105", "Hôm nay, 07:30", "92.4 kg", "+3.8 kg", "Tốt"),
        WeighingRecord("Cân định kỳ Heo Nái NY-089", "02/11, 14:00", "215.0 kg", "+2.1 kg", "Chuẩn"),
        WeighingRecord("Cân định kỳ Lô Thịt 01", "28/10, 08:00", "68.5 kg", "+5.2 kg", "Tốt")
    )
)

class ReportViewModel(
    private val pigRepository: PigRepository,
    private val breedingRepository: BreedingRepository
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(ReportPeriod.THIS_MONTH)
    val selectedPeriod: StateFlow<ReportPeriod> = _selectedPeriod.asStateFlow()

    val uiState: StateFlow<ReportUiState> = combine(
        pigRepository.allPigs,
        _selectedPeriod
    ) { pigs, period ->
        val weights = pigs.filter { it.currentWeightKg > 0 }.map { it.currentWeightKg }
        val avgWeight = if (weights.isNotEmpty()) weights.average().toFloat() else 68.5f

        ReportUiState(
            period = period,
            averageWeightKg = (avgWeight * 10).toInt() / 10f,
            adgDailyGainKg = 1.2f,
            fcrIndex = 2.42f,
            conceptionRate = 88.5f,
            weanedPigletsPerSowPerYear = 25.4f
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReportUiState()
    )

    fun onPeriodSelected(period: ReportPeriod) {
        _selectedPeriod.value = period
    }
}
