package com.example.domain.repository

import com.example.data.local.entity.SystemParameterEntity
import kotlinx.coroutines.flow.Flow

interface SystemParameterRepository {
    fun getAllParameters(): Flow<List<SystemParameterEntity>>
    fun getParametersByCategory(category: String): Flow<List<SystemParameterEntity>>
    suspend fun getParameterById(id: String): SystemParameterEntity?
    suspend fun addParameter(
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
    ): String
    suspend fun updateParameter(parameter: SystemParameterEntity)
    suspend fun deleteParameter(parameter: SystemParameterEntity)
    suspend fun deleteParameterById(id: String)
}
