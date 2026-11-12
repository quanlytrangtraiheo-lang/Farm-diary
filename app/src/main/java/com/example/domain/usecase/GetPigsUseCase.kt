package com.example.domain.usecase

import com.example.data.local.entity.PigEntity
import com.example.domain.repository.PigRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to retrieve the reactive stream of all pigs and batches.
 * Encapsulates the retrieval logic from the Domain layer.
 */
class GetPigsUseCase(private val pigRepository: PigRepository) {
    operator fun invoke(): Flow<List<PigEntity>> {
        return pigRepository.allPigs
    }
}
