package com.example.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.db.entities.BuyRequestEntity
import com.example.data.db.entities.CartItemEntity
import com.example.data.db.entities.ChatMessageEntity
import com.example.data.db.entities.MasterMedicineEntity
import com.example.data.db.entities.OfferListingEntity
import com.example.data.db.entities.PriceThresholdAlertEntity
import com.example.data.db.entities.ShopProfileEntity
import com.example.data.db.entities.TriggeredPriceAlertEntity
import com.example.data.db.entities.WatchlistItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PharmaDao {

    // Master Medicines
    @Query("SELECT * FROM master_medicines ORDER BY brandName ASC")
    fun getAllMasterMedicines(): Flow<List<MasterMedicineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMasterMedicines(medicines: List<MasterMedicineEntity>)

    // Offer Listings (Home Live Feed & Search)
    @Query("SELECT * FROM offer_listings WHERE status = 'ACTIVE' ORDER BY createdAt DESC")
    fun getAllActiveOffers(): Flow<List<OfferListingEntity>>

    @Query("SELECT * FROM offer_listings WHERE status = 'ACTIVE' AND (medicineName LIKE '%' || :query || '%' OR genericName LIKE '%' || :query || '%' OR companyName LIKE '%' || :query || '%') ORDER BY createdAt DESC")
    fun searchOffers(query: String): Flow<List<OfferListingEntity>>

    @Query("SELECT * FROM offer_listings WHERE id = :id")
    suspend fun getOfferById(id: Long): OfferListingEntity?

    @Query("SELECT * FROM offer_listings WHERE masterMedicineId = :masterId AND status = 'ACTIVE'")
    fun getOffersByMasterId(masterId: Long): Flow<List<OfferListingEntity>>

    @Query("SELECT * FROM offer_listings WHERE (medicineName LIKE '%' || :medicineName || '%') AND status = 'ACTIVE'")
    fun getOffersByMedicineName(medicineName: String): Flow<List<OfferListingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffer(offer: OfferListingEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffers(offers: List<OfferListingEntity>)

    @Update
    suspend fun updateOffer(offer: OfferListingEntity)

    @Query("UPDATE offer_listings SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateOfferStatus(id: Long, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE offer_listings SET availableQuantity = :newQty, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateOfferQuantity(id: Long, newQty: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM offer_listings WHERE id = :id")
    suspend fun deleteOfferById(id: Long)

    // Seller Dashboard Inventory
    @Query("SELECT * FROM offer_listings WHERE sellerShopId = :sellerShopId ORDER BY updatedAt DESC")
    fun getOffersBySeller(sellerShopId: Long): Flow<List<OfferListingEntity>>

    // Cart Items
    @Query("SELECT * FROM cart_items")
    fun getCartItems(): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items WHERE offerListingId = :offerId LIMIT 1")
    suspend fun getCartItemByOffer(offerId: Long): CartItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItemEntity)

    @Update
    suspend fun updateCartItem(cartItem: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE id = :cartItemId")
    suspend fun deleteCartItem(cartItemId: Long)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    // Buy Requests
    @Query("SELECT * FROM buy_requests ORDER BY timestamp DESC")
    fun getAllBuyRequests(): Flow<List<BuyRequestEntity>>

    @Query("SELECT * FROM buy_requests WHERE id = :requestId LIMIT 1")
    suspend fun getBuyRequestById(requestId: Long): BuyRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuyRequest(request: BuyRequestEntity): Long

    @Query("UPDATE buy_requests SET status = :status WHERE id = :requestId")
    suspend fun updateRequestStatus(requestId: Long, status: String)

    // Chat Messages
    @Query("SELECT * FROM chat_messages WHERE buyRequestId = :requestId ORDER BY timestamp ASC")
    fun getChatMessagesForRequest(requestId: Long): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity)

    // Shop Profiles
    @Query("SELECT * FROM shop_profiles")
    fun getAllShops(): Flow<List<ShopProfileEntity>>

    @Query("SELECT * FROM shop_profiles WHERE id = :shopId")
    suspend fun getShopById(shopId: Long): ShopProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShops(shops: List<ShopProfileEntity>)

    // Watchlist / Favorites
    @Query("SELECT * FROM watchlist_items ORDER BY addedAt DESC")
    fun getWatchlistItems(): Flow<List<WatchlistItemEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist_items WHERE medicineName = :medicineName LIMIT 1)")
    suspend fun isWatchlisted(medicineName: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlistItem(item: WatchlistItemEntity): Long

    @Query("DELETE FROM watchlist_items WHERE medicineName = :medicineName")
    suspend fun deleteWatchlistItemByName(medicineName: String)

    // Price Threshold Alerts
    @Query("SELECT * FROM price_threshold_alerts ORDER BY createdAt DESC")
    fun getPriceThresholdAlerts(): Flow<List<PriceThresholdAlertEntity>>

    @Query("SELECT * FROM price_threshold_alerts WHERE isEnabled = 1")
    suspend fun getEnabledPriceThresholdsSync(): List<PriceThresholdAlertEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceThresholdAlert(alert: PriceThresholdAlertEntity): Long

    @Query("UPDATE price_threshold_alerts SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun updatePriceThresholdEnabled(id: Long, isEnabled: Boolean)

    @Query("DELETE FROM price_threshold_alerts WHERE id = :id")
    suspend fun deletePriceThresholdAlert(id: Long)

    // Triggered Price Alerts
    @Query("SELECT * FROM triggered_price_alerts ORDER BY triggeredAt DESC")
    fun getTriggeredPriceAlerts(): Flow<List<TriggeredPriceAlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTriggeredPriceAlert(alert: TriggeredPriceAlertEntity): Long

    @Query("UPDATE triggered_price_alerts SET isRead = 1 WHERE id = :id")
    suspend fun markTriggeredAlertRead(id: Long)

    @Query("DELETE FROM triggered_price_alerts WHERE id = :id")
    suspend fun deleteTriggeredPriceAlert(id: Long)

    @Query("DELETE FROM triggered_price_alerts")
    suspend fun clearAllTriggeredAlerts()
}

