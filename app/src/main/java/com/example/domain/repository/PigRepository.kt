package com.example.domain.repository

import com.example.data.local.entity.PigEntity
import com.example.data.model.SowReproductiveStatus
import kotlinx.coroutines.flow.Flow

/**
 * Giao diện Repository (Domain Layer) trong kiến trúc sạch (Clean Architecture).
 * Trừu tượng hóa hoàn toàn các thao tác dữ liệu liên quan đến đối tượng Heo (Pig),
 * tách biệt tầng giao diện (UI Layer / ViewModel) khỏi các chi tiết triển khai cơ sở dữ liệu Room (Data Layer).
 */
interface PigRepository {
    /**
     * Lắng nghe danh sách tất cả cá thể/lô heo theo thời gian thực (Reactive Stream)
     */
    val allPigs: Flow<List<PigEntity>>

    /**
     * Tổng số lượng bản ghi heo trong cơ sở dữ liệu
     */
    val totalPigCount: Flow<Int>

    /**
     * Tổng số đầu con thực tế trong trang trại (cộng dồn cả nái cá thể và đàn nuôi thịt)
     */
    val totalHeadCount: Flow<Int?>

    /**
     * Lấy thông tin cá thể heo theo ID dạng luồng dữ liệu Flow
     */
    fun getPigById(id: String): Flow<PigEntity?>

    /**
     * Lấy thông tin cá thể heo một lần duy nhất qua suspend function
     */
    suspend fun getPigByIdOnce(id: String): PigEntity?

    /**
     * Thêm một cá thể hoặc lô heo mới vào hệ thống
     */
    suspend fun addPig(
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
    ): String

    /**
     * Cập nhật thông tin cá thể heo
     */
    suspend fun updatePig(pig: PigEntity)

    /**
     * Xóa một cá thể hoặc lô heo khỏi hệ thống
     */
    suspend fun deletePig(pig: PigEntity)

    /**
     * Thêm hàng loạt bản ghi heo vào cơ sở dữ liệu
     */
    suspend fun insertPigs(pigs: List<PigEntity>)
}
