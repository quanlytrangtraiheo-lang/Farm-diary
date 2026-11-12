package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.local.entity.PigEntity
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Tiện ích xuất dữ liệu danh sách heo và hồ sơ sức khỏe sang định dạng CSV.
 * Hỗ trợ bảng mã UTF-8 có BOM để Microsoft Excel mở tiếng Việt không bị lỗi font.
 */
object PigCsvExporter {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

    /**
     * Tạo nội dung CSV đầy đủ chi tiết cho danh sách heo và tình trạng sức khỏe.
     */
    fun generatePigsCsv(pigs: List<PigEntity>): String {
        val sb = StringBuilder()
        // Thêm UTF-8 Byte Order Mark (BOM) để Excel hiển thị đúng dấu tiếng Việt
        sb.append("\uFEFF")

        // Dòng tiêu đề cột (CSV Header)
        val headers = listOf(
            "STT",
            "Mã số tai (Tag ID)",
            "Loại đàn",
            "Số lượng (con)",
            "Giống heo",
            "Giới tính",
            "Trạng thái sinh sản",
            "Vị trí chuồng / ô",
            "Lứa đẻ",
            "Trọng lượng (kg)",
            "Ngày sinh",
            "Độ tuổi",
            "Tiêm phòng vắc-xin",
            "Siêu âm thai",
            "Chậm lên giống",
            "Mang thai quá hạn",
            "Ngày phối giống",
            "Ngày dự sinh",
            "Trạng thái đồng bộ",
            "Cập nhật lần cuối"
        )
        sb.append(headers.joinToString(",") { escapeCsv(it) }).append("\n")

        val now = System.currentTimeMillis()

        // Các dòng dữ liệu
        pigs.forEachIndexed { index, pig ->
            val birthDateStr = dateFormat.format(Date(pig.birthDate))
            val ageDays = ((now - pig.birthDate) / 86_400_000L).coerceAtLeast(0)
            val ageMonths = (ageDays / 30.44).toInt()
            val ageDisplay = if (ageMonths < 1) "$ageDays ngày" else "$ageMonths tháng ($ageDays ngày)"

            val lastMatingStr = pig.lastMatingDate?.let { dateFormat.format(Date(it)) } ?: "—"
            val expectedFarrowingStr = pig.expectedFarrowingDate?.let { dateFormat.format(Date(it)) } ?: "—"
            val updatedAtStr = dateTimeFormat.format(Date(pig.updatedAt))

            val row = listOf(
                (index + 1).toString(),
                pig.tagCode,
                if (pig.isBatch) "Nuôi theo lô" else "Cá thể nái",
                pig.quantity.toString(),
                pig.breed,
                pig.gender,
                pig.status.shortLabel,
                pig.location,
                "Lứa ${pig.parity}",
                String.format(Locale.US, "%.1f", pig.currentWeightKg),
                birthDateStr,
                ageDisplay,
                if (pig.hasOverdueVaccine) "QUÁ HẠN TIÊM" else "Đã tiêm đủ",
                if (pig.hasUltrasoundWarning) "QUÁ HẠN SIÊU ÂM" else "Bình thường",
                if (pig.hasDelayedHeatWarning) "CHẬM LÊN GIỐNG" else "Bình thường",
                if (pig.hasProlongedGestationWarning) "QUÁ NGÀY DỰ SINH" else "Bình thường",
                lastMatingStr,
                expectedFarrowingStr,
                pig.syncStatus.name,
                updatedAtStr
            )

            sb.append(row.joinToString(",") { escapeCsv(it) }).append("\n")
        }

        return sb.toString()
    }

    /**
     * Lưu nội dung CSV vào bộ nhớ đệm ứng dụng (cache directory).
     */
    fun saveCsvToFile(context: Context, csvContent: String, fileName: String? = null): File {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val actualFileName = fileName ?: "BaoCao_DanHeo_$timeStamp.csv"
        val file = File(exportDir, actualFileName)

        OutputStreamWriter(FileOutputStream(file), StandardCharsets.UTF_8).use { writer ->
            writer.write(csvContent)
            writer.flush()
        }

        return file
    }

    /**
     * Mở hộp thoại hệ thống để chia sẻ hoặc lưu tệp tin CSV qua FileProvider.
     */
    fun shareCsvFile(context: Context, file: File, chooserTitle: String = "Xuất dữ liệu đàn heo (CSV)") {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, file.name)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(chooser)
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
