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

    const val LOW_STOCK_CHANNEL_ID = "pharma_low_stock_alerts"
    const val LOW_STOCK_CHANNEL_NAME = "লো স্টক অ্যালার্ট নোটিফিকেশন"
    const val LOW_STOCK_CHANNEL_DESC = "ইনভেন্টরি স্টক নির্দিষ্ট থ্রেশহোল্ডের নিচে নামলে স্থানীয় নোটিফিকেশন অ্যালার্ট"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val orderChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(orderChannel)

            val lowStockChannel = NotificationChannel(
                LOW_STOCK_CHANNEL_ID,
                LOW_STOCK_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = LOW_STOCK_CHANNEL_DESC
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(lowStockChannel)
        }
    }

    fun showLowStockAlertNotification(
        context: Context,
        medicineName: String,
        currentStock: Int,
        threshold: Int,
        sellerShopName: String
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("target_screen", "seller_dashboard")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            (System.currentTimeMillis() % 10000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "⚠️ লো স্টক অ্যালার্ট! $medicineName"
        val body = "$sellerShopName-এর $medicineName স্টক কমে মাত্র $currentStock বক্সে নেমেছে (নির্ধারিত থ্রেশহোল্ড: $threshold বক্স)। অবিলম্বে রি-স্টক করার পরামর্শ দেওয়া হচ্ছে।"

        val builder = NotificationCompat.Builder(context, LOW_STOCK_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify((medicineName.hashCode() and 0x7FFFFFFF), builder.build())
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

    fun showNewMedicineRequestNotificationForSupplier(
        context: Context,
        requestId: Long,
        medicineName: String,
        quantity: Int,
        buyerPharmacyName: String,
        targetPrice: Double? = null
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("target_screen", "seller_dashboard")
            putExtra("request_id", requestId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            (requestId % 10000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "🔔 নতুন ওষুধের রিকোয়েস্ট এসেছে! ($medicineName)"
        val priceText = if (targetPrice != null && targetPrice > 0) " (অফার প্রাইস: ৳${targetPrice.toInt()})" else ""
        val body = "$buyerPharmacyName থেকে $medicineName-এর $quantity বক্সে নতুন রিকোয়েস্ট পোস্ট হয়েছে$priceText। ক্লিক করে অফার জমা দিন।"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify((System.currentTimeMillis() % 100000).toInt(), builder.build())
    }
}
