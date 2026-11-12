package com.example.domain.usecase

import com.example.domain.repository.PigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class PigStats(
    val recordCount: Int,
    val totalHeadCount: Int
)

/**
 * Use case to compute and observe aggregated farm pig statistics.
 */
class GetPigStatsUseCase(private val pigRepository: PigRepository) {

    operator fun invoke(): Flow<PigStats> {
        return combine(
            pigRepository.totalPigCount,
            pigRepository.totalHeadCount
        ) { records, heads ->
            PigStats(
                recordCount = records,
                totalHeadCount = heads ?: records
            )
        }
    }
}
