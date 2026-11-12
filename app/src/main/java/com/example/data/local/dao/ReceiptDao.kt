package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.ReceiptEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) cho Phiếu Nhập kho (Receipt).
 */
@Dao
interface ReceiptDao {
    @Query("SELECT * FROM receipts ORDER BY date DESC, createdAt DESC")
    fun getAllReceipts(): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts WHERE itemId = :itemId ORDER BY date DESC")
    fun getReceiptsByItemId(itemId: String): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts WHERE receiptId = :receiptId LIMIT 1")
    suspend fun getReceiptById(receiptId: String): ReceiptEntity?

    @Query("SELECT SUM(quantity) FROM receipts WHERE itemId = :itemId")
    fun getTotalImportQuantity(itemId: String): Flow<Float?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: ReceiptEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipts(receipts: List<ReceiptEntity>)

    @Query("SELECT COUNT(*) FROM receipts")
    suspend fun countReceipts(): Int
}
