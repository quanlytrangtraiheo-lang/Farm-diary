package com.example.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.data.local.AppDatabase
import com.example.data.local.entity.InventoryItemEntity
import com.example.domain.feed.FeedRationCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Calendar

object FeedStockNotificationHelper {

    const val CHANNEL_ID = "feed_stock_channel"
    private const val CHANNEL_NAME = "Cảnh báo tồn kho cám"
    private const val CHANNEL_DESC = "Thông báo cảnh báo thức ăn chạm ngưỡng an toàn"
    private const val ALARM_REQUEST_CODE = 7001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Bắn Local Notification thông báo cám chạm ngưỡng an toàn ra ngoài màn hình khóa
     */
    fun sendStockAlertNotification(
        context: Context,
        feedName: String,
        daysRemaining: Int,
        notificationId: Int = 1001
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val message = "Cảnh báo: Cám $feedName chỉ còn đủ dùng trong $daysRemaining ngày. Vui lòng kiểm tra và lên đơn nhập kho!"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Cảnh báo tồn kho cám")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            Log.w("FeedStockNotification", "Notification permission not granted: ${e.message}")
        }
    }

    /**
     * Đặt lịch Alarm định kỳ lúc 07:00 sáng hàng ngày để quét lượng tồn kho
     */
    fun scheduleDailyFeedCheckAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, FeedStockAlarmReceiver::class.java).apply {
            action = "com.example.action.FEED_STOCK_ALERT"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 7)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // Nếu 07:00 sáng hôm nay đã qua, đặt lịch cho 07:00 sáng ngày mai
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
            Log.d("FeedStockNotification", "Scheduled daily feed alert alarm for 07:00 AM")
        } catch (e: Exception) {
            Log.w("FeedStockNotification", "Failed to set repeating alarm: ${e.message}")
        }
    }

    /**
     * Quét cơ sở dữ liệu và gửi thông báo nếu có cám rơi vào mức Cảnh báo hoặc Khẩn cấp
     */
    fun checkAndDispatchStockAlerts(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getDatabase(context)
                val pigs = database.pigDao().getAllPigs().firstOrNull() ?: emptyList()
                val systemParams = database.systemParameterDao().getAllParameters().firstOrNull() ?: emptyList()
                val allInv = database.inventoryDao().getAllItems().firstOrNull() ?: emptyList()
                val feedItems = allInv.filter { it.category == InventoryItemEntity.CATEGORY_FEED }

                val result = FeedRationCalculator.calculate(
                    pigs = pigs,
                    rationMatrix = systemParams,
                    inventoryFeedItems = feedItems
                )

                val alertFeeds = result.feedResults.filter { it.isCritical || it.isWarning }
                alertFeeds.forEachIndexed { index, feed ->
                    sendStockAlertNotification(
                        context = context,
                        feedName = feed.feedName,
                        daysRemaining = feed.daysRemaining,
                        notificationId = 2000 + index
                    )
                }
            } catch (e: Exception) {
                Log.e("FeedStockNotification", "Error checking feed stock alerts: ${e.message}", e)
            }
        }
    }
}
