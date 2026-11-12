package com.example.domain.usecase

import com.example.data.model.SowReproductiveStatus
import com.example.domain.repository.PigRepository

/**
 * Use case encapsulating the business rules for creating and registering a new pig or batch.
 */
class AddPigUseCase(private val pigRepository: PigRepository) {

    suspend operator fun invoke(
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
    ): Result<String> {
        val cleanTag = tagCode.trim()
        if (cleanTag.isBlank()) {
            return Result.failure(IllegalArgumentException("Mã định danh (Số tai hoặc mã lô) không được để trống"))
        }

        val effectiveQuantity = if (isBatch) quantity.coerceAtLeast(1) else 1
        val effectiveWeight = currentWeightKg.coerceAtLeast(0f)
        val effectiveParity = parity.coerceAtLeast(0)

        val id = pigRepository.addPig(
            tagCode = cleanTag,
            isBatch = isBatch,
            quantity = effectiveQuantity,
            breed = breed.trim().ifBlank { "Không xác định" },
            gender = gender.trim().ifBlank { "Cái" },
            status = status,
            location = location.trim().ifBlank { "Chuồng chờ" },
            currentWeightKg = effectiveWeight,
            parity = effectiveParity,
            birthDate = birthDate
        )
        return Result.success(id)
    }
}
