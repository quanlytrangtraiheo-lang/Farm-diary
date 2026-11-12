package com.example.domain.usecase

import com.example.data.local.entity.PigEntity
import com.example.domain.repository.PigRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to retrieve a specific pig by its unique ID.
 */
class GetPigByIdUseCase(private val pigRepository: PigRepository) {
    operator fun invoke(id: String): Flow<PigEntity?> {
        return pigRepository.getPigById(id)
    }

    suspend fun getOnce(id: String): PigEntity? {
        return pigRepository.getPigByIdOnce(id)
    }
}
