package com.example.service

import android.util.Log
import com.example.util.PharmaNotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PharmaFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Registration Token: $token")
        // Store FCM token locally or update Firestore for pharmacist user
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM Message received from: ${remoteMessage.from}")

        val data = remoteMessage.data
        val messageType = data["type"] ?: if (data.containsKey("request_id")) "NEW_MEDICINE_REQUEST" else "ORDER_STATUS_UPDATE"

        if (messageType == "NEW_MEDICINE_REQUEST") {
            val requestId = data["request_id"]?.toLongOrNull() ?: System.currentTimeMillis()
            val medicineName = data["medicine_name"] ?: remoteMessage.notification?.title ?: "নতুন ওষুধের রিকোয়েস্ট"
            val quantity = data["quantity"]?.toIntOrNull() ?: 10
            val buyerPharmacy = data["buyer_pharmacy"] ?: "ফার্মেসী কাস্টমার"
            val targetPrice = data["target_price"]?.toDoubleOrNull()

            Log.d(TAG, "Received FCM Push payload for New Medicine Request: $medicineName from $buyerPharmacy")

            PharmaNotificationHelper.showNewMedicineRequestNotificationForSupplier(
                context = applicationContext,
                requestId = requestId,
                medicineName = medicineName,
                quantity = quantity,
                buyerPharmacyName = buyerPharmacy,
                targetPrice = targetPrice
            )
        } else {
            val orderId = data["order_id"]?.toLongOrNull() ?: 0L
            val medicineName = data["medicine_name"] ?: "ওষুধের অর্ডার"
            val status = data["status"] ?: "DISPATCHED"
            val sellerName = data["seller_name"] ?: "মেডিসিন সাপ্লায়ার"
            val customBody = remoteMessage.notification?.body ?: data["message"]

            Log.d(TAG, "Received FCM Push payload for Order ID: $orderId Status: $status")

            PharmaNotificationHelper.showOrderStatusNotification(
                context = applicationContext,
                orderId = orderId,
                medicineName = medicineName,
                newStatus = status,
                sellerShopName = sellerName,
                customMessage = customBody
            )
        }
    }

    companion object {
        private const val TAG = "PharmaFCMService"
    }
}
