package com.example.domain.usecase

import com.example.data.local.entity.PigEntity
import com.example.domain.repository.PigRepository

/**
 * Use case encapsulating the business rules for updating an existing pig record.
 */
class UpdatePigUseCase(private val pigRepository: PigRepository) {

    suspend operator fun invoke(pig: PigEntity): Result<Unit> {
        if (pig.tagCode.isBlank()) {
            return Result.failure(IllegalArgumentException("Mã định danh không được để trống khi cập nhật"))
        }

        pigRepository.updatePig(
            pig.copy(
                tagCode = pig.tagCode.trim(),
                currentWeightKg = pig.currentWeightKg.coerceAtLeast(0f)
            )
        )
        return Result.success(Unit)
    }
}
