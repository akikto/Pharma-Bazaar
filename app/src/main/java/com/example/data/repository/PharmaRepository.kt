package com.example.data.repository

import android.content.Context
import com.example.data.db.dao.PharmaDao
import com.example.data.db.entities.BuyRequestEntity
import com.example.data.db.entities.CartItemEntity
import com.example.data.db.entities.ChatMessageEntity
import com.example.data.db.entities.MasterMedicineEntity
import com.example.data.db.entities.OfferListingEntity
import com.example.data.db.entities.PriceThresholdAlertEntity
import com.example.data.db.entities.ShopProfileEntity
import com.example.data.db.entities.TriggeredPriceAlertEntity
import com.example.data.db.entities.WatchlistItemEntity
import com.example.data.remote.FirestoreService
import com.example.util.PharmaNotificationHelper
import com.example.service.AiMatchSuggestion
import com.example.service.GeminiSuggestionService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class PharmaRepository(
    private val pharmaDao: PharmaDao,
    val firestoreService: FirestoreService = FirestoreService()
) {

    val allActiveOffers: Flow<List<OfferListingEntity>> = pharmaDao.getAllActiveOffers()
    val allMasterMedicines: Flow<List<MasterMedicineEntity>> = pharmaDao.getAllMasterMedicines()
    val cartItems: Flow<List<CartItemEntity>> = pharmaDao.getCartItems()
    val buyRequests: Flow<List<BuyRequestEntity>> = pharmaDao.getAllBuyRequests()
    val shopProfiles: Flow<List<ShopProfileEntity>> = pharmaDao.getAllShops()
    val watchlistItems: Flow<List<WatchlistItemEntity>> = pharmaDao.getWatchlistItems()
    val priceThresholdAlerts: Flow<List<PriceThresholdAlertEntity>> = pharmaDao.getPriceThresholdAlerts()
    val triggeredPriceAlerts: Flow<List<TriggeredPriceAlertEntity>> = pharmaDao.getTriggeredPriceAlerts()

    suspend fun addPriceThresholdAlert(medicineName: String, genericName: String = "", maxPriceThreshold: Double): Long {
        val alert = PriceThresholdAlertEntity(
            medicineName = medicineName,
            genericName = genericName,
            maxPriceThreshold = maxPriceThreshold,
            isEnabled = true
        )
        val id = pharmaDao.insertPriceThresholdAlert(alert)

        // Evaluate existing active offers against this new threshold immediately
        try {
            val activeOffers = pharmaDao.getAllActiveOffers().first()
            activeOffers.forEach { offer ->
                val nameMatches = offer.medicineName.contains(medicineName, ignoreCase = true) ||
                        (genericName.isNotEmpty() && offer.genericName.contains(genericName, ignoreCase = true)) ||
                        medicineName.contains(offer.medicineName, ignoreCase = true)

                if (nameMatches && offer.offerPrice <= maxPriceThreshold) {
                    pharmaDao.insertTriggeredPriceAlert(
                        TriggeredPriceAlertEntity(
                            thresholdId = id,
                            offerListingId = offer.id,
                            medicineName = offer.medicineName,
                            genericName = offer.genericName,
                            offerPrice = offer.offerPrice,
                            targetThresholdPrice = maxPriceThreshold,
                            sellerShopName = offer.sellerShopName,
                            discountPercent = offer.discountPercent
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Handled safely
        }
        return id
    }

    suspend fun updatePriceThresholdEnabled(id: Long, isEnabled: Boolean) {
        pharmaDao.updatePriceThresholdEnabled(id, isEnabled)
    }

    suspend fun deletePriceThresholdAlert(id: Long) {
        pharmaDao.deletePriceThresholdAlert(id)
    }

    suspend fun markTriggeredAlertRead(id: Long) {
        pharmaDao.markTriggeredAlertRead(id)
    }

    suspend fun deleteTriggeredPriceAlert(id: Long) {
        pharmaDao.deleteTriggeredPriceAlert(id)
    }

    suspend fun checkAndTriggerPriceAlertsForOffer(offer: OfferListingEntity) {
        if (offer.status != "ACTIVE") return
        try {
            val enabledThresholds = pharmaDao.getEnabledPriceThresholdsSync()
            enabledThresholds.forEach { threshold ->
                val nameMatches = offer.medicineName.contains(threshold.medicineName, ignoreCase = true) ||
                        (threshold.genericName.isNotEmpty() && offer.genericName.contains(threshold.genericName, ignoreCase = true)) ||
                        threshold.medicineName.contains(offer.medicineName, ignoreCase = true)

                if (nameMatches && offer.offerPrice <= threshold.maxPriceThreshold) {
                    pharmaDao.insertTriggeredPriceAlert(
                        TriggeredPriceAlertEntity(
                            thresholdId = threshold.id,
                            offerListingId = offer.id,
                            medicineName = offer.medicineName,
                            genericName = offer.genericName,
                            offerPrice = offer.offerPrice,
                            targetThresholdPrice = threshold.maxPriceThreshold,
                            sellerShopName = offer.sellerShopName,
                            discountPercent = offer.discountPercent
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Handled safely
        }
    }

    suspend fun toggleWatchlist(
        medicineName: String,
        genericName: String = "",
        companyName: String = "",
        form: String = ""
    ) {
        if (pharmaDao.isWatchlisted(medicineName)) {
            pharmaDao.deleteWatchlistItemByName(medicineName)
        } else {
            pharmaDao.insertWatchlistItem(
                WatchlistItemEntity(
                    medicineName = medicineName,
                    genericName = genericName,
                    companyName = companyName,
                    form = form
                )
            )
        }
    }

    suspend fun removeFromWatchlist(medicineName: String) {
        pharmaDao.deleteWatchlistItemByName(medicineName)
    }

    fun searchOffers(query: String): Flow<List<OfferListingEntity>> = pharmaDao.searchOffers(query)

    fun getOffersBySeller(sellerShopId: Long): Flow<List<OfferListingEntity>> =
        pharmaDao.getOffersBySeller(sellerShopId)

    fun getOffersByMedicineName(medicineName: String): Flow<List<OfferListingEntity>> =
        pharmaDao.getOffersByMedicineName(medicineName)

    fun getChatMessagesForRequest(requestId: Long): Flow<List<ChatMessageEntity>> =
        pharmaDao.getChatMessagesForRequest(requestId)

    suspend fun insertOffer(offer: OfferListingEntity): Long {
        val id = pharmaDao.insertOffer(offer)
        val syncedOffer = if (offer.id == 0L) offer.copy(id = id) else offer
        firestoreService.saveInventoryListing(syncedOffer)
        checkAndTriggerPriceAlertsForOffer(syncedOffer)
        return id
    }

    suspend fun updateOffer(offer: OfferListingEntity) {
        pharmaDao.updateOffer(offer)
        firestoreService.saveInventoryListing(offer)
        checkAndTriggerPriceAlertsForOffer(offer)
    }

    suspend fun updateOfferStatus(id: Long, status: String) {
        pharmaDao.updateOfferStatus(id, status)
        firestoreService.updateInventoryListingStatus(id, status)
    }

    suspend fun deleteOffer(id: Long) {
        pharmaDao.deleteOfferById(id)
        firestoreService.deleteInventoryListing(id)
    }

    suspend fun addToCart(offer: OfferListingEntity, quantity: Int) {
        val existing = pharmaDao.getCartItemByOffer(offer.id)
        if (existing != null) {
            val newQty = (existing.requestedQuantity + quantity).coerceAtMost(offer.availableQuantity)
            pharmaDao.updateCartItem(existing.copy(requestedQuantity = newQty))
        } else {
            val cartItem = CartItemEntity(
                offerListingId = offer.id,
                sellerShopId = offer.sellerShopId,
                sellerShopName = offer.sellerShopName,
                sellerLocation = offer.sellerLocation,
                medicineName = offer.medicineName,
                genericName = offer.genericName,
                strength = offer.strength,
                offerPrice = offer.offerPrice,
                mrp = offer.mrp,
                requestedQuantity = quantity.coerceAtLeast(offer.minimumOrderQuantity),
                minimumOrderQuantity = offer.minimumOrderQuantity,
                maxAvailableQuantity = offer.availableQuantity,
                expiryDaysLeft = offer.daysUntilExpiry
            )
            pharmaDao.insertCartItem(cartItem)
        }
    }

    suspend fun updateCartQuantity(cartItemId: Long, newQuantity: Int) {
        if (newQuantity <= 0) {
            pharmaDao.deleteCartItem(cartItemId)
        } else {
            val items = pharmaDao.getCartItems().first()
            val target = items.find { it.id == cartItemId }
            if (target != null) {
                val clamped = newQuantity.coerceIn(target.minimumOrderQuantity, target.maxAvailableQuantity)
                pharmaDao.updateCartItem(target.copy(requestedQuantity = clamped))
            }
        }
    }

    suspend fun deleteCartItem(cartItemId: Long) = pharmaDao.deleteCartItem(cartItemId)

    suspend fun clearCart() = pharmaDao.clearCart()

    suspend fun insertSingleBuyRequest(request: BuyRequestEntity): Long {
        val reqId = pharmaDao.insertBuyRequest(request)
        firestoreService.savePharmacyRequest(request.copy(id = reqId))
        return reqId
    }

    suspend fun submitBuyRequestsFromCart(buyerShop: ShopProfileEntity, note: String = "") {
        val items = pharmaDao.getCartItems().first()
        if (items.isEmpty()) return

        // Group items by seller
        val grouped = items.groupBy { it.sellerShopId }
        for ((sellerId, vendorItems) in grouped) {
            val firstItem = vendorItems.first()
            val sellerShop = pharmaDao.getShopById(sellerId)

            for (item in vendorItems) {
                val total = item.offerPrice * item.requestedQuantity
                val req = BuyRequestEntity(
                    offerListingId = item.offerListingId,
                    medicineName = "${item.medicineName} ${item.strength}",
                    requestedQuantity = item.requestedQuantity,
                    unitPrice = item.offerPrice,
                    totalPrice = total,
                    buyerShopId = buyerShop.id,
                    buyerShopName = buyerShop.shopName,
                    buyerPhone = buyerShop.phone,
                    sellerShopId = sellerId,
                    sellerShopName = item.sellerShopName,
                    sellerPhone = sellerShop?.phone ?: "01711002233",
                    note = note,
                    status = "PENDING",
                    timestamp = System.currentTimeMillis()
                )
                val reqId = pharmaDao.insertBuyRequest(req)
                firestoreService.savePharmacyRequest(req.copy(id = reqId))

                // Add initial chat message
                pharmaDao.insertChatMessage(
                    ChatMessageEntity(
                        buyRequestId = reqId,
                        sellerShopId = sellerId,
                        buyerShopId = buyerShop.id,
                        senderName = buyerShop.shopName,
                        isFromSeller = false,
                        messageText = "আসসালামু আলাইকুম। ${item.requestedQuantity} বক্স ${item.medicineName}-এর জন্য একটি বাই রিকোয়েস্ট পাঠালাম।"
                    )
                )

                // Reserve quantity in offer
                val offer = pharmaDao.getOfferById(item.offerListingId)
                if (offer != null) {
                    val newReserved = offer.reservedQuantity + item.requestedQuantity
                    val newAvail = (offer.availableQuantity - item.requestedQuantity).coerceAtLeast(0)
                    pharmaDao.updateOffer(
                        offer.copy(
                            availableQuantity = newAvail,
                            reservedQuantity = newReserved,
                            status = if (newAvail == 0) "PAUSED" else offer.status
                        )
                    )
                }
            }
        }
        pharmaDao.clearCart()
    }

    suspend fun updateBuyRequestStatus(requestId: Long, newStatus: String, context: Context? = null) {
        val existingRequest = pharmaDao.getBuyRequestById(requestId)
        pharmaDao.updateRequestStatus(requestId, newStatus)
        firestoreService.updatePharmacyRequestStatus(requestId, newStatus)

        if (existingRequest != null) {
            val statusUpper = newStatus.uppercase()
            if (statusUpper == "DISPATCHED" || statusUpper == "DELIVERED" || statusUpper == "ACCEPTED" || statusUpper == "CANCELLED") {
                val chatMsg = when (statusUpper) {
                    "DISPATCHED" -> "🚚 [FCM Push] সাপ্লায়ার আপনার অর্ডারটি কুরিয়ারে ডিসপ্যাচ (Dispatched) করেছেন।"
                    "DELIVERED" -> "🎉 [FCM Push] অর্ডারটি সফলভাবে আপনার ফার্মেসিতে ডেলিভারি (Delivered) হয়েছে।"
                    "ACCEPTED" -> "👍 [FCM Push] আপনার অর্ডার প্রস্তাবটি একসেপ্ট করা হয়েছে।"
                    "CANCELLED" -> "❌ [FCM Push] আপনার অর্ডারটি বাতিল করা হয়েছে।"
                    else -> "📦 অর্ডার স্ট্যাটাস আপডেট: $newStatus"
                }

                pharmaDao.insertChatMessage(
                    ChatMessageEntity(
                        buyRequestId = requestId,
                        sellerShopId = existingRequest.sellerShopId,
                        buyerShopId = existingRequest.buyerShopId,
                        senderName = existingRequest.sellerShopName,
                        isFromSeller = true,
                        messageText = chatMsg
                    )
                )

                context?.let { ctx ->
                    PharmaNotificationHelper.showOrderStatusNotification(
                        context = ctx,
                        orderId = requestId,
                        medicineName = existingRequest.medicineName,
                        newStatus = newStatus,
                        sellerShopName = existingRequest.sellerShopName
                    )
                }
            }
        }
    }

    suspend fun getAiInventoryMatches(): List<AiMatchSuggestion> {
        val openRequests = pharmaDao.getAllBuyRequests().first()
        val activeOffers = pharmaDao.getAllActiveOffers().first()
        return GeminiSuggestionService.matchRequestsWithInventory(openRequests, activeOffers)
    }

    suspend fun syncAllWithFirestore() {
        try {
            val offers = pharmaDao.getAllActiveOffers().first()
            offers.forEach { offer ->
                firestoreService.saveInventoryListing(offer)
            }
            val requests = pharmaDao.getAllBuyRequests().first()
            requests.forEach { req ->
                firestoreService.savePharmacyRequest(req)
            }
            // Fetch remote order history from Firestore
            val cloudOrders = firestoreService.fetchOrderHistoryFromFirestore()
            for (order in cloudOrders) {
                val local = pharmaDao.getBuyRequestById(order.id)
                if (local == null) {
                    pharmaDao.insertBuyRequest(order)
                } else if (local.status != order.status) {
                    pharmaDao.updateRequestStatus(order.id, order.status)
                }
            }
        } catch (e: Exception) {
            // Handled safely
        }
    }

    suspend fun syncOrderHistoryFromFirestore(): List<BuyRequestEntity> {
        return try {
            val cloudOrders = firestoreService.fetchOrderHistoryFromFirestore()
            for (order in cloudOrders) {
                val local = pharmaDao.getBuyRequestById(order.id)
                if (local == null) {
                    pharmaDao.insertBuyRequest(order)
                } else if (local.status != order.status) {
                    pharmaDao.updateRequestStatus(order.id, order.status)
                }
            }
            pharmaDao.getAllBuyRequests().first()
        } catch (e: Exception) {
            pharmaDao.getAllBuyRequests().first()
        }
    }

    suspend fun sendChatMessage(requestId: Long, buyerId: Long, sellerId: Long, senderName: String, isSeller: Boolean, text: String) {
        val msg = ChatMessageEntity(
            buyRequestId = requestId,
            sellerShopId = sellerId,
            buyerShopId = buyerId,
            senderName = senderName,
            isFromSeller = isSeller,
            messageText = text
        )
        pharmaDao.insertChatMessage(msg)
    }

    suspend fun seedSampleDataIfEmpty() {
        val masterList = pharmaDao.getAllMasterMedicines().first()
        if (masterList.isNotEmpty()) return

        // 1. Seed Master Medicine Database (DGDA list)
        val sampleMasters = listOf(
            MasterMedicineEntity(1, "Napa Extra", "Paracetamol + Caffeine", "500mg + 65mg", "Beximco Pharmaceuticals", "Tablet", 250.0, "10x10 Box"),
            MasterMedicineEntity(2, "Seclo", "Omeprazole", "20mg", "Square Pharmaceuticals", "Capsule", 600.0, "10x10 Box"),
            MasterMedicineEntity(3, "Ceevit", "Ascorbic Acid (Vit C)", "250mg", "Square Pharmaceuticals", "Chewable", 180.0, "10x10 Box"),
            MasterMedicineEntity(4, "Ace", "Paracetamol", "500mg", "Square Pharmaceuticals", "Tablet", 120.0, "10x10 Box"),
            MasterMedicineEntity(5, "Maxpro", "Esomeprazole", "20mg", "Renata Limited", "Capsule", 700.0, "10x10 Box"),
            MasterMedicineEntity(6, "Sergel", "Esomeprazole", "20mg", "Incepta Pharmaceuticals", "Capsule", 680.0, "10x10 Box"),
            MasterMedicineEntity(7, "Monas 10", "Montelukast", "10mg", "Acme Laboratories", "Tablet", 520.0, "3x10 Box"),
            MasterMedicineEntity(8, "Azithro 500", "Azithromycin", "500mg", "Opsonin Pharma", "Tablet", 450.0, "3x6 Box"),
            MasterMedicineEntity(9, "Bizoran 5/20", "Amlodipine + Olmesartan", "5mg + 20mg", "Incepta Pharmaceuticals", "Tablet", 380.0, "3x10 Box"),
            MasterMedicineEntity(10, "Fast 500", "Paracetamol", "500mg", "Acme Laboratories", "Tablet", 110.0, "10x10 Box")
        )
        pharmaDao.insertMasterMedicines(sampleMasters)

        // 2. Seed B2B Verified Shop Profiles
        val sampleShops = listOf(
            ShopProfileEntity(1, "সেবা ফার্মেসী", "মোঃ রফিকুল ইসলাম", "DL-MIR-2024-884", "01711223344", "মিরপুর-১০ গোলচত্বর, ঢাকা", "মিরপুর, ঢাকা", 4.9, 142, true),
            ShopProfileEntity(2, "গ্রিন ফার্মা বাজার", "তানভীর আহমেদ", "DL-UTT-2023-112", "01819887766", "হাউস ১২, রোড ৪, সেক্টর ৭, উত্তরা", "উত্তরা, ঢাকা", 4.8, 98, true),
            ShopProfileEntity(3, "মেডিসিন পয়েন্ট", "কে. এম. জাহিদ", "DL-DHA-2022-559", "01912334455", "ধানমন্ডি ২৭, ঢাকা", "ধানমন্ডি, ঢাকা", 4.7, 76, true),
            ShopProfileEntity(4, "জনতা ডিসপেনসারি", "আব্দুল হাকীম", "DL-CTG-2021-304", "01552331100", "জিইসি মোড়, চট্টগ্রাম", "চট্টগ্রাম", 4.9, 210, true)
        )
        pharmaDao.insertShops(sampleShops)

        // 3. Seed Live Offers (with short expiry, overstock, discounts)
        val sampleOffers = listOf(
            OfferListingEntity(
                id = 1,
                masterMedicineId = 1,
                medicineName = "Napa Extra",
                genericName = "Paracetamol + Caffeine",
                strength = "500mg + 65mg",
                companyName = "Beximco Pharmaceuticals",
                form = "Tablet",
                packSize = "10x10 Box",
                batchNumber = "BX-2025-09",
                expiryDate = "15 Aug 2026",
                daysUntilExpiry = 14, // Short Expiry Red < 30 days
                availableQuantity = 50,
                reservedQuantity = 0,
                mrp = 250.0,
                offerPrice = 125.0,
                discountPercent = 50,
                minimumOrderQuantity = 5,
                sellerShopId = 1,
                sellerShopName = "সেবা ফার্মেসী",
                sellerLocation = "মিরপুর-১০, ঢাকা",
                sellerDistanceKm = 1.2,
                sellerRating = 4.9,
                isVerifiedShop = true,
                notes = "মেয়াদ কম থাকায় ৫০% ছাড়। ক্যাশ অন ক্যাশ ব্যাক বা পিকআপ সুবিধা আছে।"
            ),
            OfferListingEntity(
                id = 2,
                masterMedicineId = 1,
                medicineName = "Napa Extra",
                genericName = "Paracetamol + Caffeine",
                strength = "500mg + 65mg",
                companyName = "Beximco Pharmaceuticals",
                form = "Tablet",
                packSize = "10x10 Box",
                batchNumber = "BX-2026-02",
                expiryDate = "28 Sep 2026",
                daysUntilExpiry = 58, // Orange 30-60 days
                availableQuantity = 120,
                reservedQuantity = 0,
                mrp = 250.0,
                offerPrice = 175.0,
                discountPercent = 30,
                minimumOrderQuantity = 10,
                sellerShopId = 2,
                sellerShopName = "গ্রিন ফার্মা বাজার",
                sellerLocation = "উত্তরা, ঢাকা",
                sellerDistanceKm = 4.5,
                sellerRating = 4.8,
                isVerifiedShop = true,
                notes = "অভারস্টক লট। অরিজিনাল ইনভয়েস সাথে দেওয়া হবে।"
            ),
            OfferListingEntity(
                id = 3,
                masterMedicineId = 2,
                medicineName = "Seclo",
                genericName = "Omeprazole",
                strength = "20mg",
                companyName = "Square Pharmaceuticals",
                form = "Capsule",
                packSize = "10x10 Box",
                batchNumber = "SQ-8812",
                expiryDate = "10 Oct 2026",
                daysUntilExpiry = 70, // Green > 60 days
                availableQuantity = 80,
                reservedQuantity = 0,
                mrp = 600.0,
                offerPrice = 360.0,
                discountPercent = 40,
                minimumOrderQuantity = 2,
                sellerShopId = 3,
                sellerShopName = "মেডিসিন পয়েন্ট",
                sellerLocation = "ধানমন্ডি, ঢাকা",
                sellerDistanceKm = 2.8,
                sellerRating = 4.7,
                isVerifiedShop = true,
                notes = "হোলসেল স্টক ক্লিয়ারেন্স।"
            ),
            OfferListingEntity(
                id = 4,
                masterMedicineId = 3,
                medicineName = "Ceevit",
                genericName = "Ascorbic Acid (Vit C)",
                strength = "250mg",
                companyName = "Square Pharmaceuticals",
                form = "Chewable",
                packSize = "10x10 Box",
                batchNumber = "SQ-9901",
                expiryDate = "20 Aug 2026",
                daysUntilExpiry = 19, // Short expiry
                availableQuantity = 200,
                reservedQuantity = 0,
                mrp = 180.0,
                offerPrice = 90.0,
                discountPercent = 50,
                minimumOrderQuantity = 20,
                sellerShopId = 1,
                sellerShopName = "সেবা ফার্মেসী",
                sellerLocation = "মিরপুর-১০, ঢাকা",
                sellerDistanceKm = 1.2,
                sellerRating = 4.9,
                isVerifiedShop = true,
                notes = "দ্রুত বিক্রয়ের জন্য বিশেষ দাম।"
            ),
            OfferListingEntity(
                id = 5,
                masterMedicineId = 6,
                medicineName = "Sergel",
                genericName = "Esomeprazole",
                strength = "20mg",
                companyName = "Incepta Pharmaceuticals",
                form = "Capsule",
                packSize = "10x10 Box",
                batchNumber = "INC-302",
                expiryDate = "15 Jan 2027",
                daysUntilExpiry = 167,
                availableQuantity = 45,
                reservedQuantity = 0,
                mrp = 680.0,
                offerPrice = 476.0,
                discountPercent = 30,
                minimumOrderQuantity = 3,
                sellerShopId = 4,
                sellerShopName = "জনতা ডিসপেনসারি",
                sellerLocation = "চট্টগ্রাম",
                sellerDistanceKm = 8.5,
                sellerRating = 4.6,
                isVerifiedShop = true,
                notes = "কুরিয়ার এবং কন্ডিশনে পাঠানো যাবে।"
            )
        )
        pharmaDao.insertOffers(sampleOffers)

        // Seed a sample Buy Request and Chat
        val sampleReq = BuyRequestEntity(
            id = 1,
            offerListingId = 3,
            medicineName = "Seclo 20mg",
            requestedQuantity = 10,
            unitPrice = 360.0,
            totalPrice = 3600.0,
            buyerShopId = 1,
            buyerShopName = "সেবা ফার্মেসী",
            buyerPhone = "01711223344",
            sellerShopId = 3,
            sellerShopName = "মেডিসিন পয়েন্ট",
            sellerPhone = "01912334455",
            note = "আজ বিকেলের মধ্যে পিকআপ নিতে পারব।",
            status = "PENDING",
            timestamp = System.currentTimeMillis() - 3600000
        )
        pharmaDao.insertBuyRequest(sampleReq)

        pharmaDao.insertChatMessage(
            ChatMessageEntity(
                id = 1,
                buyRequestId = 1,
                sellerShopId = 3,
                buyerShopId = 1,
                senderName = "সেবা ফার্মেসী",
                isFromSeller = false,
                messageText = "ভাই, ১০ বক্স সেকলো ২০mg রিকোয়েস্ট পাঠালাম। স্টক আছে তো?",
                timestamp = System.currentTimeMillis() - 3500000
            )
        )
        pharmaDao.insertChatMessage(
            ChatMessageEntity(
                id = 2,
                buyRequestId = 1,
                sellerShopId = 3,
                buyerShopId = 1,
                senderName = "মেডিসিন পয়েন্ট",
                isFromSeller = true,
                messageText = "হ্যাঁ ভাই স্টক আছে, চলে আসেন। ইনভয়েস রেডি রাখা হয়েছে।",
                timestamp = System.currentTimeMillis() - 1800000
            )
        )

        // Seed Initial Watchlist Items
        pharmaDao.insertWatchlistItem(
            WatchlistItemEntity(
                id = 1,
                medicineName = "Napa Extra 500mg",
                genericName = "Paracetamol + Caffeine",
                companyName = "Beximco Pharmaceuticals",
                form = "Tablet"
            )
        )
        pharmaDao.insertWatchlistItem(
            WatchlistItemEntity(
                id = 2,
                medicineName = "Seclo 20mg",
                genericName = "Omeprazole",
                companyName = "Square Pharmaceuticals",
                form = "Capsule"
            )
        )
    }

    suspend fun updateOfferLowStockThreshold(offerId: Long, newThreshold: Int) {
        pharmaDao.updateOfferLowStockThreshold(offerId, newThreshold)
    }
}

