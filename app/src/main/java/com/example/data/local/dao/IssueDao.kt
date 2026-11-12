package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.IssueEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) cho Phiếu Xuất kho (Issue).
 */
@Dao
interface IssueDao {
    @Query("SELECT * FROM issues ORDER BY date DESC, createdAt DESC")
    fun getAllIssues(): Flow<List<IssueEntity>>

    @Query("SELECT * FROM issues WHERE itemId = :itemId ORDER BY date DESC")
    fun getIssuesByItemId(itemId: String): Flow<List<IssueEntity>>

    @Query("SELECT * FROM issues WHERE targetBarnId = :targetBarnId ORDER BY date DESC")
    fun getIssuesByTargetBarn(targetBarnId: String): Flow<List<IssueEntity>>

    @Query("SELECT * FROM issues WHERE issueId = :issueId LIMIT 1")
    suspend fun getIssueById(issueId: String): IssueEntity?

    @Query("SELECT SUM(quantity) FROM issues WHERE itemId = :itemId")
    fun getTotalExportQuantity(itemId: String): Flow<Float?>

    @Query("SELECT SUM(totalCost) FROM issues WHERE targetBarnId = :targetBarnId")
    fun getTotalCostByTargetBarn(targetBarnId: String): Flow<Long?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIssue(issue: IssueEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIssues(issues: List<IssueEntity>)

    @Query("SELECT COUNT(*) FROM issues")
    suspend fun countIssues(): Int
}
