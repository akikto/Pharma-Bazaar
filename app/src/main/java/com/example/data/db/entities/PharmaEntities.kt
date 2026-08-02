package com.example.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "master_medicines")
data class MasterMedicineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val brandName: String,
    val genericName: String,
    val strength: String,
    val companyName: String,
    val form: String, // Tablet, Capsule, Syrup, Injection
    val standardMrp: Double,
    val defaultPackSize: String
)

@Entity(tableName = "offer_listings")
data class OfferListingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val masterMedicineId: Long,
    val medicineName: String,
    val genericName: String,
    val strength: String,
    val companyName: String,
    val form: String,
    val packSize: String,
    val batchNumber: String,
    val expiryDate: String, // e.g. "2026-12-31" or "Dec 2026"
    val daysUntilExpiry: Int,
    val availableQuantity: Int,
    val reservedQuantity: Int = 0,
    val mrp: Double,
    val offerPrice: Double,
    val discountPercent: Int,
    val minimumOrderQuantity: Int = 1, // MOQ
    val sellerShopId: Long,
    val sellerShopName: String,
    val sellerLocation: String,
    val sellerDistanceKm: Double,
    val sellerRating: Double = 4.8,
    val isVerifiedShop: Boolean = true,
    val lowStockThreshold: Int = 10,
    val notes: String = "",
    val status: String = "ACTIVE", // ACTIVE, PAUSED, SOLD_OUT
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val offerListingId: Long,
    val sellerShopId: Long,
    val sellerShopName: String,
    val sellerLocation: String,
    val medicineName: String,
    val genericName: String,
    val strength: String,
    val offerPrice: Double,
    val mrp: Double,
    val requestedQuantity: Int,
    val minimumOrderQuantity: Int,
    val maxAvailableQuantity: Int,
    val expiryDaysLeft: Int
)

@Entity(tableName = "buy_requests")
data class BuyRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val offerListingId: Long,
    val medicineName: String,
    val requestedQuantity: Int,
    val unitPrice: Double,
    val totalPrice: Double,
    val buyerShopId: Long,
    val buyerShopName: String,
    val buyerPhone: String,
    val sellerShopId: Long,
    val sellerShopName: String,
    val sellerPhone: String,
    val note: String = "",
    val status: String = "PENDING", // PENDING, ACCEPTED, REJECTED, COMPLETED
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val buyRequestId: Long = 0,
    val sellerShopId: Long,
    val buyerShopId: Long,
    val senderName: String,
    val isFromSeller: Boolean,
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "shop_profiles")
data class ShopProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val shopName: String,
    val ownerName: String,
    val licenseNumber: String,
    val phone: String,
    val address: String,
    val area: String,
    val rating: Double,
    val totalDealsCompleted: Int,
    val isVerified: Boolean = true
)

@Entity(tableName = "watchlist_items")
data class WatchlistItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicineName: String,
    val genericName: String = "",
    val companyName: String = "",
    val form: String = "",
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "price_threshold_alerts")
data class PriceThresholdAlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicineName: String,
    val genericName: String = "",
    val maxPriceThreshold: Double,
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "triggered_price_alerts")
data class TriggeredPriceAlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val thresholdId: Long,
    val offerListingId: Long,
    val medicineName: String,
    val genericName: String = "",
    val offerPrice: Double,
    val targetThresholdPrice: Double,
    val sellerShopName: String,
    val discountPercent: Int,
    val triggeredAt: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

