package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class FeedStockAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        // Kích hoạt quét tồn kho thức ăn và đẩy thông báo nếu chạm ngưỡng an toàn
        FeedStockNotificationHelper.checkAndDispatchStockAlerts(context)
    }
}
