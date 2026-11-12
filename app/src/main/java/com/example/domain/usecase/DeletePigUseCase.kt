package com.example.domain.usecase

import com.example.data.local.entity.PigEntity
import com.example.domain.repository.PigRepository

/**
 * Use case to delete a pig or batch from the local repository.
 */
class DeletePigUseCase(private val pigRepository: PigRepository) {

    suspend operator fun invoke(pig: PigEntity) {
        pigRepository.deletePig(pig)
    }
}
