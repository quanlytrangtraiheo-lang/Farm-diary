package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.BreedingCycleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BreedingCycleDao {
    @Query("SELECT * FROM breeding_cycles WHERE sowId = :sowId ORDER BY parity DESC")
    fun getCyclesForSow(sowId: String): Flow<List<BreedingCycleEntity>>

    @Query("SELECT * FROM breeding_cycles WHERE sowId = :sowId AND cycleStatus = 'IN_PROGRESS' LIMIT 1")
    suspend fun getActiveCycleForSow(sowId: String): BreedingCycleEntity?

    @Query("SELECT * FROM breeding_cycles WHERE cycleId = :cycleId LIMIT 1")
    suspend fun getCycleById(cycleId: String): BreedingCycleEntity?

    @Query("SELECT * FROM breeding_cycles")
    fun getAllCycles(): Flow<List<BreedingCycleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCycle(cycle: BreedingCycleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCycles(cycles: List<BreedingCycleEntity>)

    @Update
    suspend fun updateCycle(cycle: BreedingCycleEntity)
}
