package com.example.data.remote

import android.util.Log
import com.example.data.db.entities.BuyRequestEntity
import com.example.data.db.entities.OfferListingEntity
import com.example.data.db.entities.ShopProfileEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreService {

    private fun getFirestore(): FirebaseFirestore? {
        return try {
            FirebaseFirestore.getInstance()
        } catch (e: Throwable) {
            Log.d(TAG, "Cloud Firestore unavailable or FirebaseApp not initialized: ${e.message}")
            null
        }
    }

    /**
     * Firebase Auth uid of the currently signed-in user, or null if signed out.
     * Written onto every document this service creates so Firestore security
     * rules can verify ownership (see firestore.rules).
     */
    private fun currentUid(): String? = try {
        FirebaseAuth.getInstance().currentUser?.uid
    } catch (e: Throwable) {
        null
    }

    companion object {
        private const val TAG = "FirestoreService"
        const val COLLECTION_INVENTORY_LISTINGS = "inventory_listings"
        const val COLLECTION_PHARMACY_REQUESTS = "pharmacy_requests"
        const val COLLECTION_SHOPS = "shops"
    }

    /**
     * Stores or updates a Shop Profile in Cloud Firestore, keyed by the
     * shop's local Long id (which becomes globally unique because it's
     * minted from System.currentTimeMillis() at registration time — see
     * PharmaViewModel.registerPharmacySeller).
     */
    suspend fun saveShopProfile(shop: ShopProfileEntity): Boolean {
        val db = getFirestore() ?: return false
        return try {
            val data = hashMapOf(
                "id" to shop.id,
                "shopName" to shop.shopName,
                "ownerName" to shop.ownerName,
                "licenseNumber" to shop.licenseNumber,
                "phone" to shop.phone,
                "address" to shop.address,
                "area" to shop.area,
                "rating" to shop.rating,
                "totalDealsCompleted" to shop.totalDealsCompleted,
                "isVerified" to shop.isVerified,
                "ownerUid" to shop.ownerUid
            )
            db.collection(COLLECTION_SHOPS).document(shop.id.toString())
                .set(data, SetOptions.merge()).await()
            Log.d(TAG, "Successfully saved shop profile ${shop.id} to Firestore")
            true
        } catch (e: Exception) {
            Log.w(TAG, "Unable to save shop profile ${shop.id} to Firestore: ${e.message}")
            false
        }
    }

    /**
     * Looks up the shop profile owned by the given Firebase Auth uid, if any.
     * Used at login to restore the signed-in seller's real shop instead of
     * the hardcoded demo shop.
     */
    suspend fun fetchShopProfileByOwnerUid(uid: String): ShopProfileEntity? {
        val db = getFirestore() ?: return null
        return try {
            val snapshot = db.collection(COLLECTION_SHOPS)
                .whereEqualTo("ownerUid", uid)
                .limit(1)
                .get()
                .await()
            val doc = snapshot.documents.firstOrNull() ?: return null
            val data = doc.data ?: return null
            ShopProfileEntity(
                id = (data["id"] as? Long) ?: doc.id.toLongOrNull() ?: 0L,
                shopName = (data["shopName"] as? String) ?: "",
                ownerName = (data["ownerName"] as? String) ?: "",
                licenseNumber = (data["licenseNumber"] as? String) ?: "",
                phone = (data["phone"] as? String) ?: "",
                address = (data["address"] as? String) ?: "",
                area = (data["area"] as? String) ?: "",
                rating = (data["rating"] as? Double) ?: ((data["rating"] as? Long)?.toDouble() ?: 5.0),
                totalDealsCompleted = (data["totalDealsCompleted"] as? Long)?.toInt() ?: 0,
                isVerified = (data["isVerified"] as? Boolean) ?: true,
                ownerUid = (data["ownerUid"] as? String) ?: uid
            )
        } catch (e: Exception) {
            Log.w(TAG, "Unable to fetch shop profile for uid $uid: ${e.message}")
            null
        }
    }

    /**
     * Stores or updates an Inventory Listing in Cloud Firestore.
     */
    suspend fun saveInventoryListing(offer: OfferListingEntity): Boolean {
        val db = getFirestore() ?: return false
        return try {
            val docRef = db.collection(COLLECTION_INVENTORY_LISTINGS).document(offer.id.toString())
            val data = hashMapOf(
                "id" to offer.id,
                "masterMedicineId" to offer.masterMedicineId,
                "medicineName" to offer.medicineName,
                "genericName" to offer.genericName,
                "strength" to offer.strength,
                "companyName" to offer.companyName,
                "form" to offer.form,
                "packSize" to offer.packSize,
                "batchNumber" to offer.batchNumber,
                "expiryDate" to offer.expiryDate,
                "daysUntilExpiry" to offer.daysUntilExpiry,
                "availableQuantity" to offer.availableQuantity,
                "mrp" to offer.mrp,
                "offerPrice" to offer.offerPrice,
                "discountPercent" to offer.discountPercent,
                "minimumOrderQuantity" to offer.minimumOrderQuantity,
                "sellerShopId" to offer.sellerShopId,
                "sellerShopName" to offer.sellerShopName,
                "sellerLocation" to offer.sellerLocation,
                "sellerDistanceKm" to offer.sellerDistanceKm,
                "isVerifiedShop" to offer.isVerifiedShop,
                "notes" to offer.notes,
                "status" to offer.status,
                "createdAt" to offer.createdAt,
                "updatedAt" to System.currentTimeMillis(),
                "ownerUid" to currentUid()
            )
            docRef.set(data, SetOptions.merge()).await()
            Log.d(TAG, "Successfully saved inventory listing ${offer.id} to Firestore")
            true
        } catch (e: Exception) {
            Log.w(TAG, "Unable to save inventory listing ${offer.id} to Firestore: ${e.message}")
            false
        }
    }

    /**
     * Updates the status of an Inventory Listing in Cloud Firestore.
     */
    suspend fun updateInventoryListingStatus(offerId: Long, status: String): Boolean {
        val db = getFirestore() ?: return false
        return try {
            db.collection(COLLECTION_INVENTORY_LISTINGS)
                .document(offerId.toString())
                .update(
                    mapOf(
                        "status" to status,
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
            Log.d(TAG, "Updated inventory listing $offerId status to $status in Firestore")
            true
        } catch (e: Exception) {
            Log.w(TAG, "Unable to update inventory listing status in Firestore: ${e.message}")
            false
        }
    }

    /**
     * Deletes an Inventory Listing from Cloud Firestore.
     */
    suspend fun deleteInventoryListing(offerId: Long): Boolean {
        val db = getFirestore() ?: return false
        return try {
            db.collection(COLLECTION_INVENTORY_LISTINGS)
                .document(offerId.toString())
                .delete()
                .await()
            Log.d(TAG, "Deleted inventory listing $offerId from Firestore")
            true
        } catch (e: Exception) {
            Log.w(TAG, "Unable to delete inventory listing $offerId from Firestore: ${e.message}")
            false
        }
    }

    /**
     * Stores or updates a Pharmacy Medicine Request in Cloud Firestore.
     */
    suspend fun savePharmacyRequest(request: BuyRequestEntity): Boolean {
        val db = getFirestore() ?: return false
        return try {
            val docRef = db.collection(COLLECTION_PHARMACY_REQUESTS).document(request.id.toString())
            val data = hashMapOf(
                "id" to request.id,
                "offerListingId" to request.offerListingId,
                "medicineName" to request.medicineName,
                "requestedQuantity" to request.requestedQuantity,
                "unitPrice" to request.unitPrice,
                "totalPrice" to request.totalPrice,
                "buyerShopId" to request.buyerShopId,
                "buyerShopName" to request.buyerShopName,
                "buyerPhone" to request.buyerPhone,
                "sellerShopId" to request.sellerShopId,
                "sellerShopName" to request.sellerShopName,
                "sellerPhone" to request.sellerPhone,
                "note" to request.note,
                "status" to request.status,
                "timestamp" to request.timestamp,
                "syncedAt" to System.currentTimeMillis(),
                "buyerUid" to currentUid()
            )
            docRef.set(data, SetOptions.merge()).await()
            Log.d(TAG, "Successfully saved pharmacy request ${request.id} to Firestore")
            true
        } catch (e: Exception) {
            Log.w(TAG, "Unable to save pharmacy request ${request.id} to Firestore: ${e.message}")
            false
        }
    }

    /**
     * Updates status of a Pharmacy Request in Cloud Firestore.
     */
    suspend fun updatePharmacyRequestStatus(requestId: Long, status: String): Boolean {
        val db = getFirestore() ?: return false
        return try {
            db.collection(COLLECTION_PHARMACY_REQUESTS)
                .document(requestId.toString())
                .update(
                    mapOf(
                        "status" to status,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            true
        } catch (e: Exception) {
            Log.w(TAG, "Unable to update request status in Firestore: ${e.message}")
            false
        }
    }

    /**
     * One-time fetch of order history transactions from Cloud Firestore.
     */
    suspend fun fetchOrderHistoryFromFirestore(): List<BuyRequestEntity> {
        val db = getFirestore() ?: return emptyList()
        return try {
            val snapshot = db.collection(COLLECTION_PHARMACY_REQUESTS).get().await()
            snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                val id = (data["id"] as? Long) ?: doc.id.toLongOrNull() ?: 0L
                BuyRequestEntity(
                    id = id,
                    offerListingId = (data["offerListingId"] as? Long) ?: 0L,
                    medicineName = (data["medicineName"] as? String) ?: "",
                    requestedQuantity = (data["requestedQuantity"] as? Long)?.toInt() ?: ((data["requestedQuantity"] as? Int) ?: 1),
                    unitPrice = (data["unitPrice"] as? Double) ?: ((data["unitPrice"] as? Long)?.toDouble() ?: 0.0),
                    totalPrice = (data["totalPrice"] as? Double) ?: ((data["totalPrice"] as? Long)?.toDouble() ?: 0.0),
                    buyerShopId = (data["buyerShopId"] as? Long) ?: 1L,
                    buyerShopName = (data["buyerShopName"] as? String) ?: "Pharmacist Shop",
                    buyerPhone = (data["buyerPhone"] as? String) ?: "01700000000",
                    sellerShopId = (data["sellerShopId"] as? Long) ?: 1L,
                    sellerShopName = (data["sellerShopName"] as? String) ?: "Pharma Supplier",
                    sellerPhone = (data["sellerPhone"] as? String) ?: "01800000000",
                    note = (data["note"] as? String) ?: "",
                    status = (data["status"] as? String) ?: "PENDING",
                    timestamp = (data["timestamp"] as? Long) ?: System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error fetching order history from Firestore: ${e.message}")
            emptyList()
        }
    }

    /**
     * One-time fetch of pharmaceutical inventory products from Cloud Firestore.
     */
    suspend fun fetchProductsFromFirestore(): List<OfferListingEntity> {
        val db = getFirestore() ?: return emptyList()
        return try {
            val snapshot = db.collection(COLLECTION_INVENTORY_LISTINGS).get().await()
            snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                val id = (data["id"] as? Long) ?: doc.id.toLongOrNull() ?: 0L
                OfferListingEntity(
                    id = id,
                    masterMedicineId = (data["masterMedicineId"] as? Long) ?: 0L,
                    medicineName = (data["medicineName"] as? String) ?: "",
                    genericName = (data["genericName"] as? String) ?: "",
                    strength = (data["strength"] as? String) ?: "",
                    companyName = (data["companyName"] as? String) ?: "",
                    form = (data["form"] as? String) ?: "",
                    packSize = (data["packSize"] as? String) ?: "",
                    batchNumber = (data["batchNumber"] as? String) ?: "",
                    expiryDate = (data["expiryDate"] as? String) ?: "",
                    daysUntilExpiry = (data["daysUntilExpiry"] as? Long)?.toInt() ?: ((data["daysUntilExpiry"] as? Int) ?: 180),
                    availableQuantity = (data["availableQuantity"] as? Long)?.toInt() ?: ((data["availableQuantity"] as? Int) ?: 100),
                    mrp = (data["mrp"] as? Double) ?: ((data["mrp"] as? Long)?.toDouble() ?: 0.0),
                    offerPrice = (data["offerPrice"] as? Double) ?: ((data["offerPrice"] as? Long)?.toDouble() ?: 0.0),
                    discountPercent = (data["discountPercent"] as? Long)?.toInt() ?: ((data["discountPercent"] as? Int) ?: 0),
                    minimumOrderQuantity = (data["minimumOrderQuantity"] as? Long)?.toInt() ?: ((data["minimumOrderQuantity"] as? Int) ?: 1),
                    sellerShopId = (data["sellerShopId"] as? Long) ?: 1L,
                    sellerShopName = (data["sellerShopName"] as? String) ?: "Pharma Supplier",
                    sellerLocation = (data["sellerLocation"] as? String) ?: "Mumbai, India",
                    sellerDistanceKm = (data["sellerDistanceKm"] as? Double) ?: ((data["sellerDistanceKm"] as? Long)?.toDouble() ?: 2.5),
                    sellerRating = (data["sellerRating"] as? Double) ?: 4.8,
                    isVerifiedShop = (data["isVerifiedShop"] as? Boolean) ?: true,
                    notes = (data["notes"] as? String) ?: "",
                    status = (data["status"] as? String) ?: "ACTIVE",
                    createdAt = (data["createdAt"] as? Long) ?: System.currentTimeMillis(),
                    updatedAt = (data["updatedAt"] as? Long) ?: System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error fetching products from Firestore: ${e.message}")
            emptyList()
        }
    }

    /**
     * Observe real-time Cloud Firestore inventory listings.
     */
    fun observeCloudInventoryListings(): Flow<List<Map<String, Any>>> = callbackFlow {
        val db = getFirestore()
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listenerRegistration = db.collection(COLLECTION_INVENTORY_LISTINGS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed for inventory listings", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { doc ->
                        doc.data
                    }
                    trySend(items)
                }
            }

        awaitClose { listenerRegistration.remove() }
    }

    /**
     * Observe real-time Cloud Firestore pharmacy requests.
     */
    fun observeCloudPharmacyRequests(): Flow<List<Map<String, Any>>> = callbackFlow {
        val db = getFirestore()
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listenerRegistration = db.collection(COLLECTION_PHARMACY_REQUESTS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed for pharmacy requests", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val requests = snapshot.documents.mapNotNull { doc ->
                        doc.data
                    }
                    trySend(requests)
                }
            }

        awaitClose { listenerRegistration.remove() }
    }
}
