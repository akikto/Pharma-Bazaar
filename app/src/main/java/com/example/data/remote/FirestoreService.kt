package com.example.data.remote

import android.util.Log
import com.example.data.db.entities.BuyRequestEntity
import com.example.data.db.entities.OfferListingEntity
import com.google.firebase.FirebaseApp
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

    companion object {
        private const val TAG = "FirestoreService"
        const val COLLECTION_INVENTORY_LISTINGS = "inventory_listings"
        const val COLLECTION_PHARMACY_REQUESTS = "pharmacy_requests"
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
                "updatedAt" to System.currentTimeMillis()
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
                "syncedAt" to System.currentTimeMillis()
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
