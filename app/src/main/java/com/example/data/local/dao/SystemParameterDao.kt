package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.SystemParameterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SystemParameterDao {

    @Query("SELECT * FROM system_parameters ORDER BY displayOrder ASC, createdAt DESC")
    fun getAllParameters(): Flow<List<SystemParameterEntity>>

    @Query("SELECT * FROM system_parameters WHERE category = :category ORDER BY displayOrder ASC, createdAt DESC")
    fun getParametersByCategory(category: String): Flow<List<SystemParameterEntity>>

    @Query("SELECT * FROM system_parameters WHERE id = :id LIMIT 1")
    suspend fun getParameterById(id: String): SystemParameterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParameter(parameter: SystemParameterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParameters(parameters: List<SystemParameterEntity>)

    @Update
    suspend fun updateParameter(parameter: SystemParameterEntity)

    @Delete
    suspend fun deleteParameter(parameter: SystemParameterEntity)

    @Query("DELETE FROM system_parameters WHERE id = :id")
    suspend fun deleteParameterById(id: String)
}
