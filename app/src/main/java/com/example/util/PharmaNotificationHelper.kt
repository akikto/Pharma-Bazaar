package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

object PharmaNotificationHelper {

    const val CHANNEL_ID = "pharma_order_updates"
    const val CHANNEL_NAME = "অর্ডার স্ট্যাটাস নোটিফিকেশন"
    const val CHANNEL_DESC = "সাপ্লায়ার অর্ডার ডিসপ্যাচ ও ডেলিভারি স্ট্যাটাস আপডেট নোটিফিকেশন"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showOrderStatusNotification(
        context: Context,
        orderId: Long,
        medicineName: String,
        newStatus: String,
        sellerShopName: String,
        customMessage: String? = null
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("target_screen", "orders")
            putExtra("order_id", orderId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            orderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val (title, body) = when (newStatus.uppercase()) {
            "DISPATCHED" -> {
                "🚚 অর্ডার ডেলিভারির জন্য রওয়ানা হয়েছে! (Dispatched)" to
                        "$sellerShopName আপনার $medicineName অর্ডারের পণ্যগুলো কুরিয়ার/পরিবহনে পাঠিয়ে দিয়েছে।"
            }
            "DELIVERED" -> {
                "🎉 আপনার অর্ডার সফলভাবে ডেলিভারি হয়েছে! (Delivered)" to
                        "$sellerShopName প্রেরিত $medicineName পণ্যগুলো সফলভাবে আপনার ফার্মেসিতে পৌঁছেছে।"
            }
            "ACCEPTED" -> {
                "👍 অর্ডার কনফার্ম করা হয়েছে (Accepted)" to
                        "$sellerShopName আপনার $medicineName ক্রয়ের প্রস্তাবটি গ্রহণ করেছে।"
            }
            "CANCELLED" -> {
                "❌ অর্ডার বাতিল করা হয়েছে (Cancelled)" to
                        "$sellerShopName আপনার $medicineName অর্ডারটি বাতিল করেছে।"
            }
            else -> {
                "📦 অর্ডারের নতুন আপডেট: $newStatus" to
                        "$sellerShopName আপনার $medicineName অর্ডারের বর্তমান অবস্থা: $newStatus"
            }
        }

        val finalBody = customMessage ?: body

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle(title)
            .setContentText(finalBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(finalBody))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify((System.currentTimeMillis() % 100000).toInt(), builder.build())
    }
}
