package com.example.data.repository

import com.example.data.local.dao.SystemParameterDao
import com.example.data.local.entity.SystemParameterEntity
import com.example.domain.repository.SystemParameterRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class SystemParameterRepositoryImpl(
    private val systemParameterDao: SystemParameterDao
) : SystemParameterRepository {

    override fun getAllParameters(): Flow<List<SystemParameterEntity>> {
        return systemParameterDao.getAllParameters()
    }

    override fun getParametersByCategory(category: String): Flow<List<SystemParameterEntity>> {
        return systemParameterDao.getParametersByCategory(category)
    }

    override suspend fun getParameterById(id: String): SystemParameterEntity? {
        return systemParameterDao.getParameterById(id)
    }

    override suspend fun addParameter(
        category: String,
        name: String,
        code: String,
        description: String,
        unit: String,
        subType: String,
        secondaryValue: String,
        numericValue: Double,
        statusText: String,
        safetyDaysThreshold: Int,
        minStockThreshold: Double
    ): String {
        val id = UUID.randomUUID().toString()
        val entity = SystemParameterEntity(
            id = id,
            category = category,
            name = name.trim(),
            code = code.trim().uppercase(),
            description = description.trim(),
            unit = unit.trim(),
            subType = subType.trim(),
            secondaryValue = secondaryValue.trim(),
            numericValue = numericValue,
            statusText = statusText.trim().ifBlank { "Đang hoạt động" },
            safetyDaysThreshold = safetyDaysThreshold,
            minStockThreshold = minStockThreshold,
            createdAt = System.currentTimeMillis()
        )
        systemParameterDao.insertParameter(entity)
        return id
    }

    override suspend fun updateParameter(parameter: SystemParameterEntity) {
        systemParameterDao.updateParameter(parameter)
    }

    override suspend fun deleteParameter(parameter: SystemParameterEntity) {
        systemParameterDao.deleteParameter(parameter)
    }

    override suspend fun deleteParameterById(id: String) {
        systemParameterDao.deleteParameterById(id)
    }
}
