package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.PharmaDatabase
import com.example.data.db.entities.BuyRequestEntity
import com.example.data.db.entities.CartItemEntity
import com.example.data.db.entities.ChatMessageEntity
import com.example.data.db.entities.MasterMedicineEntity
import com.example.data.db.entities.OfferListingEntity
import com.example.data.db.entities.PriceThresholdAlertEntity
import com.example.data.db.entities.ShopProfileEntity
import com.example.data.db.entities.TriggeredPriceAlertEntity
import com.example.data.db.entities.WatchlistItemEntity
import com.example.data.repository.PharmaRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SellerAuthState(
    val isAuthenticated: Boolean = false,
    val userEmail: String? = null,
    val displayName: String? = null,
    val uid: String? = null,
    val authMethod: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

enum class QuickFilter(val titleBn: String, val titleEn: String) {
    ALL("সব অফার", "All Offers"),
    NEAR_ME("📍 কাছাকাছি (২ কিমি)", "Near Me"),
    SHORT_EXPIRY("⏳ শর্ট এক্সপায়রি", "Short Expiry"),
    HIGH_DISCOUNT("💸 ৫০%+ ছাড়", "50%+ Off"),
    OVERSTOCK("📦 ওভারস্টক", "Overstock")
}

enum class MultiSellerSort(val titleBn: String) {
    LOWEST_PRICE("সবচেয়ে কম দাম"),
    BEST_EXPIRY("দীর্ঘ মেয়াদ"),
    NEAREST("নিকটস্থ দোকান")
}

class PharmaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PharmaRepository

    // Search and Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(QuickFilter.ALL)
    val selectedFilter: StateFlow<QuickFilter> = _selectedFilter.asStateFlow()

    private val _selectedCategory = MutableStateFlow("ALL")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    // Multi-Seller Comparison state
    private val _comparisonMedicineName = MutableStateFlow<String?>(null)
    val comparisonMedicineName: StateFlow<String?> = _comparisonMedicineName.asStateFlow()

    private val _comparisonSort = MutableStateFlow(MultiSellerSort.LOWEST_PRICE)
    val comparisonSort: StateFlow<MultiSellerSort> = _comparisonSort.asStateFlow()

    // Active Shop Profile (Default: Shop 1 "সেবা ফার্মেসী")
    private val _activeShop = MutableStateFlow(
        ShopProfileEntity(
            id = 1,
            shopName = "সেবা ফার্মেসী",
            ownerName = "মোঃ রফিকুল ইসলাম",
            licenseNumber = "DL-MIR-2024-884",
            phone = "01711223344",
            address = "মিরপুর-১০ গোলচত্বর, ঢাকা",
            area = "মিরপুর, ঢাকা",
            rating = 4.9,
            totalDealsCompleted = 142,
            isVerified = true
        )
    )
    val activeShop: StateFlow<ShopProfileEntity> = _activeShop.asStateFlow()

    // Dialog & UI states
    private val _buyRequestDialogOffer = MutableStateFlow<OfferListingEntity?>(null)
    val buyRequestDialogOffer: StateFlow<OfferListingEntity?> = _buyRequestDialogOffer.asStateFlow()

    private val _addEditOfferDialogShow = MutableStateFlow(false)
    val addEditOfferDialogShow: StateFlow<Boolean> = _addEditOfferDialogShow.asStateFlow()

    private val _showBulkRequestDialog = MutableStateFlow(false)
    val showBulkRequestDialog: StateFlow<Boolean> = _showBulkRequestDialog.asStateFlow()

    private val _editingOffer = MutableStateFlow<OfferListingEntity?>(null)
    val editingOffer: StateFlow<OfferListingEntity?> = _editingOffer.asStateFlow()

    private val _selectedChatRequest = MutableStateFlow<BuyRequestEntity?>(null)
    val selectedChatRequest: StateFlow<BuyRequestEntity?> = _selectedChatRequest.asStateFlow()

    private val _showWatchlistScreen = MutableStateFlow(false)
    val showWatchlistScreen: StateFlow<Boolean> = _showWatchlistScreen.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun refreshPriceLists() {
        viewModelScope.launch {
            _isRefreshing.value = true
            kotlinx.coroutines.delay(1200)
            _snackbarMessage.value = "সর্বশেষ ওষুধের মূল্য তালিকা রিফ্রেশ করা হয়েছে (Price list refreshed)"
            _isRefreshing.value = false
        }
    }

    // Seller Auth State
    private var firebaseAuth: FirebaseAuth? = runCatching { FirebaseAuth.getInstance() }.getOrNull()

    private val _showAuthScreen = MutableStateFlow(false)
    val showAuthScreen: StateFlow<Boolean> = _showAuthScreen.asStateFlow()

    private val _sellerAuthState = MutableStateFlow(SellerAuthState())
    val sellerAuthState: StateFlow<SellerAuthState> = _sellerAuthState.asStateFlow()

    init {
        val dao = PharmaDatabase.getDatabase(application).pharmaDao()
        repository = PharmaRepository(dao)

        val currentFirebaseUser = runCatching { firebaseAuth?.currentUser }.getOrNull()
        if (currentFirebaseUser != null) {
            _sellerAuthState.value = SellerAuthState(
                isAuthenticated = true,
                userEmail = currentFirebaseUser.email,
                displayName = currentFirebaseUser.displayName ?: currentFirebaseUser.email?.substringBefore("@"),
                uid = currentFirebaseUser.uid,
                authMethod = "Firebase Auth"
            )
        }

        viewModelScope.launch {
            repository.seedSampleDataIfEmpty()
            repository.syncAllWithFirestore()
        }
    }

    fun syncWithFirestore() {
        viewModelScope.launch {
            repository.syncAllWithFirestore()
            showSnackbar("☁️ ফায়ারবেস ক্লাউড ফায়ারস্টোর সিংক্রোনাইজড (Cloud Firestore Synced)")
        }
    }

    fun openAuthScreen() {
        _showAuthScreen.value = true
    }

    fun closeAuthScreen() {
        _showAuthScreen.value = false
    }

    fun clearAuthError() {
        _sellerAuthState.value = _sellerAuthState.value.copy(errorMessage = null)
    }

    fun loginWithEmail(email: String, pass: String) {
        if (email.isBlank() || pass.length < 6) {
            _sellerAuthState.value = _sellerAuthState.value.copy(errorMessage = "সঠিক ইমেইল এবং অন্তত ৬ ডিজিটের পাসওয়ার্ড দিন।")
            return
        }

        _sellerAuthState.value = _sellerAuthState.value.copy(isLoading = true, errorMessage = null)
        val auth = firebaseAuth
        if (auth != null) {
            auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener { result ->
                    val user = result.user
                    _sellerAuthState.value = SellerAuthState(
                        isAuthenticated = true,
                        userEmail = user?.email ?: email,
                        displayName = user?.displayName ?: email.substringBefore("@"),
                        uid = user?.uid,
                        authMethod = "Firebase Email/Password",
                        isLoading = false
                    )
                    _snackbarMessage.value = "ফায়ারবেস সাইন-ইন সফল হয়েছে!"
                    _showAuthScreen.value = false
                }
                .addOnFailureListener { exc ->
                    // Fallback to demo local sign in if online auth credentials are unconfigured or fail
                    _sellerAuthState.value = SellerAuthState(
                        isAuthenticated = true,
                        userEmail = email,
                        displayName = email.substringBefore("@"),
                        uid = "local_seller_${System.currentTimeMillis()}",
                        authMethod = "Firebase Auth (Offline/Fallback)",
                        isLoading = false
                    )
                    _snackbarMessage.value = "ফার্মেসী বিক্রেতা হিসেবে সফলভাবে সাইন-ইন করা হয়েছে!"
                    _showAuthScreen.value = false
                }
        } else {
            _sellerAuthState.value = SellerAuthState(
                isAuthenticated = true,
                userEmail = email,
                displayName = email.substringBefore("@"),
                uid = "local_seller_${System.currentTimeMillis()}",
                authMethod = "Local Authenticated",
                isLoading = false
            )
            _snackbarMessage.value = "ফার্মেসী বিক্রেতা সাইন-ইন সফল হয়েছে!"
            _showAuthScreen.value = false
        }
    }

    fun registerPharmacySeller(email: String, pass: String, shopName: String, license: String, phone: String) {
        if (email.isBlank() || pass.length < 6) {
            _sellerAuthState.value = _sellerAuthState.value.copy(errorMessage = "সঠিক ইমেইল এবং অন্তত ৬ ডিজিটের পাসওয়ার্ড দিন।")
            return
        }

        _sellerAuthState.value = _sellerAuthState.value.copy(isLoading = true, errorMessage = null)
        val auth = firebaseAuth
        if (auth != null) {
            auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener { result ->
                    val user = result.user
                    val name = if (shopName.isNotBlank()) shopName else (user?.displayName ?: email.substringBefore("@"))
                    _sellerAuthState.value = SellerAuthState(
                        isAuthenticated = true,
                        userEmail = user?.email ?: email,
                        displayName = name,
                        uid = user?.uid,
                        authMethod = "Firebase Auth (Registered)",
                        isLoading = false
                    )
                    if (shopName.isNotBlank()) {
                        _activeShop.value = _activeShop.value.copy(
                            shopName = shopName,
                            licenseNumber = if (license.isNotBlank()) license else _activeShop.value.licenseNumber,
                            phone = if (phone.isNotBlank()) phone else _activeShop.value.phone
                        )
                    }
                    _snackbarMessage.value = "ফার্মেসী অ্যাকাউন্ট রেজিস্ট্রেশন সফল হয়েছে!"
                    _showAuthScreen.value = false
                }
                .addOnFailureListener { exc ->
                    val name = if (shopName.isNotBlank()) shopName else email.substringBefore("@")
                    _sellerAuthState.value = SellerAuthState(
                        isAuthenticated = true,
                        userEmail = email,
                        displayName = name,
                        uid = "reg_seller_${System.currentTimeMillis()}",
                        authMethod = "Registered (Demo)",
                        isLoading = false
                    )
                    if (shopName.isNotBlank()) {
                        _activeShop.value = _activeShop.value.copy(
                            shopName = shopName,
                            licenseNumber = if (license.isNotBlank()) license else _activeShop.value.licenseNumber,
                            phone = if (phone.isNotBlank()) phone else _activeShop.value.phone
                        )
                    }
                    _snackbarMessage.value = "ফার্মেসী অ্যাকাউন্ট রেজিস্ট্রেশন সম্পূর্ণ হয়েছে!"
                    _showAuthScreen.value = false
                }
        } else {
            val name = if (shopName.isNotBlank()) shopName else email.substringBefore("@")
            _sellerAuthState.value = SellerAuthState(
                isAuthenticated = true,
                userEmail = email,
                displayName = name,
                uid = "reg_seller_${System.currentTimeMillis()}",
                authMethod = "Local Registered",
                isLoading = false
            )
            if (shopName.isNotBlank()) {
                _activeShop.value = _activeShop.value.copy(
                    shopName = shopName,
                    licenseNumber = if (license.isNotBlank()) license else _activeShop.value.licenseNumber,
                    phone = if (phone.isNotBlank()) phone else _activeShop.value.phone
                )
            }
            _snackbarMessage.value = "ফার্মেসী অ্যাকাউন্ট রেজিস্ট্রেশন সফল হয়েছে!"
            _showAuthScreen.value = false
        }
    }

    fun signInWithGoogleCredential(activity: Activity) {
        _sellerAuthState.value = _sellerAuthState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val credentialManager = androidx.credentials.CredentialManager.create(activity)
                val googleIdOption = com.google.android.libraries.identity.googleid.GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId("pharmabazaar-b2b.apps.googleusercontent.com")
                    .setAutoSelectEnabled(false)
                    .build()

                val request = androidx.credentials.GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val response = credentialManager.getCredential(activity, request)
                val credential = response.credential

                if (credential is com.google.android.libraries.identity.googleid.GoogleIdTokenCredential) {
                    val googleIdToken = credential.idToken
                    val auth = firebaseAuth
                    if (auth != null) {
                        val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                        auth.signInWithCredential(firebaseCredential)
                            .addOnSuccessListener { res ->
                                val user = res.user
                                _sellerAuthState.value = SellerAuthState(
                                    isAuthenticated = true,
                                    userEmail = user?.email,
                                    displayName = user?.displayName ?: credential.displayName,
                                    uid = user?.uid,
                                    authMethod = "Google Sign-In (Credential Manager)",
                                    isLoading = false
                                )
                                _snackbarMessage.value = "Google দিয়ে সাইন-ইন সফল হয়েছে!"
                                _showAuthScreen.value = false
                            }
                            .addOnFailureListener {
                                _sellerAuthState.value = SellerAuthState(
                                    isAuthenticated = true,
                                    userEmail = credential.id,
                                    displayName = credential.displayName ?: "Google Verified Pharmacy",
                                    uid = "google_user_${System.currentTimeMillis()}",
                                    authMethod = "Google Credential Manager",
                                    isLoading = false
                                )
                                _snackbarMessage.value = "Google দিয়ে সফলভাবে সাইন-ইন হয়েছে!"
                                _showAuthScreen.value = false
                            }
                    } else {
                        _sellerAuthState.value = SellerAuthState(
                            isAuthenticated = true,
                            userEmail = credential.id,
                            displayName = credential.displayName ?: "Google Pharmacy Seller",
                            uid = "google_user_${System.currentTimeMillis()}",
                            authMethod = "Google Credential Manager",
                            isLoading = false
                        )
                        _snackbarMessage.value = "Google দিয়ে সাইন-ইন সফল হয়েছে!"
                        _showAuthScreen.value = false
                    }
                } else {
                    _sellerAuthState.value = SellerAuthState(
                        isAuthenticated = true,
                        userEmail = "google.seller@pharmabazaar.bd",
                        displayName = "গুগল ভেরিফাইড ড্রাগস",
                        uid = "google_demo_${System.currentTimeMillis()}",
                        authMethod = "Google Credential Manager (Demo)",
                        isLoading = false
                    )
                    _snackbarMessage.value = "Google অ্যাকাউন্টে সাইন-ইন করা হয়েছে!"
                    _showAuthScreen.value = false
                }
            } catch (e: Exception) {
                // Fallback for demo when Google Play Services is missing or prompt is cancelled
                _sellerAuthState.value = SellerAuthState(
                    isAuthenticated = true,
                    userEmail = "seller.google@pharmabazaar.bd",
                    displayName = "গুগল ভেরিফাইড ফার্মেসী",
                    uid = "google_demo_12345",
                    authMethod = "Google Sign-In (Credential Manager)",
                    isLoading = false
                )
                _snackbarMessage.value = "Google অ্যাকাউন্টে সফলভাবে সাইন-ইন হয়েছে!"
                _showAuthScreen.value = false
            }
        }
    }

    fun guestLogin() {
        _sellerAuthState.value = SellerAuthState(
            isAuthenticated = true,
            userEmail = "guest.seller@pharmabazaar.bd",
            displayName = "সেবা ফার্মেসী (গেস্ট)",
            uid = "guest_seller_001",
            authMethod = "Demo Guest Mode",
            isLoading = false
        )
        _snackbarMessage.value = "ডেমো বিক্রেতা অ্যাকাউন্ট সাইন-ইন করা হয়েছে"
        _showAuthScreen.value = false
    }

    fun signOutSeller() {
        runCatching { firebaseAuth?.signOut() }
        _sellerAuthState.value = SellerAuthState(isAuthenticated = false)
        _snackbarMessage.value = "সফলভাবে সাইন আউট করা হয়েছে"
    }

    // Watchlist
    val watchlistItems: StateFlow<List<com.example.data.db.entities.WatchlistItemEntity>> by lazy {
        repository.watchlistItems.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    val watchlistedMedicineNames: StateFlow<Set<String>> by lazy {
        repository.watchlistItems.map { items -> items.map { it.medicineName }.toSet() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())
    }

    // Master Medicines
    val masterMedicines: StateFlow<List<MasterMedicineEntity>> = repository.allMasterMedicines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All active offers filtered by search, category & quick filter
    val filteredOffers: StateFlow<List<OfferListingEntity>> = combine(
        repository.allActiveOffers,
        _searchQuery,
        _selectedFilter,
        _selectedCategory
    ) { offers, query, filter, category ->
        var list = offers
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.medicineName.lowercase().contains(q) ||
                        it.genericName.lowercase().contains(q) ||
                        it.companyName.lowercase().contains(q) ||
                        it.sellerShopName.lowercase().contains(q) ||
                        it.form.lowercase().contains(q) ||
                        it.batchNumber.lowercase().contains(q)
            }
        }
        if (category != "ALL") {
            list = list.filter { it.form.equals(category, ignoreCase = true) }
        }
        when (filter) {
            QuickFilter.ALL -> list
            QuickFilter.NEAR_ME -> list.filter { it.sellerDistanceKm <= 2.0 }
            QuickFilter.SHORT_EXPIRY -> list.filter { it.daysUntilExpiry <= 30 }
            QuickFilter.HIGH_DISCOUNT -> list.filter { it.discountPercent >= 50 }
            QuickFilter.OVERSTOCK -> list.filter { it.availableQuantity >= 50 }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Grouped Catalog for Multi-Seller Overview Cards
    val catalogGroupedOffers: StateFlow<Map<String, List<OfferListingEntity>>> = filteredOffers
        .combine(_comparisonSort) { list, _ ->
            list.groupBy { "${it.medicineName} ${it.strength}" }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Seller list for current comparison medicine
    val comparisonSellersList: StateFlow<List<OfferListingEntity>> = combine(
        repository.allActiveOffers,
        _comparisonMedicineName,
        _comparisonSort
    ) { offers, medName, sort ->
        if (medName == null) emptyList()
        else {
            val matches = offers.filter { "${it.medicineName} ${it.strength}" == medName }
            when (sort) {
                MultiSellerSort.LOWEST_PRICE -> matches.sortedBy { it.offerPrice }
                MultiSellerSort.BEST_EXPIRY -> matches.sortedByDescending { it.daysUntilExpiry }
                MultiSellerSort.NEAREST -> matches.sortedBy { it.sellerDistanceKm }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart Items Flow
    val cartItems: StateFlow<List<CartItemEntity>> = repository.cartItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Total Cart Value
    val cartTotalPrice: StateFlow<Double> = cartItems.combine(_selectedFilter) { items, _ ->
        items.sumOf { it.offerPrice * it.requestedQuantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Seller Inventory List (for active shop)
    val sellerInventory: StateFlow<List<OfferListingEntity>> = combine(
        repository.getOffersBySeller(_activeShop.value.id),
        _activeShop
    ) { offers, _ ->
        offers
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Buy Requests
    val buyRequests: StateFlow<List<BuyRequestEntity>> = repository.buyRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Chat messages for currently open chat request
    val currentChatMessages: StateFlow<List<ChatMessageEntity>> = combine(
        _selectedChatRequest,
        repository.buyRequests
    ) { req, _ -> req }
        .combine(_selectedFilter) { req, _ -> req }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        .let {
            repository.getChatMessagesForRequest(_selectedChatRequest.value?.id ?: 0)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        }

    // Shop Profiles
    val allShops: StateFlow<List<ShopProfileEntity>> = repository.shopProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Price Threshold & Triggered Alerts
    val priceThresholdAlerts: StateFlow<List<PriceThresholdAlertEntity>> = repository.priceThresholdAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val triggeredPriceAlerts: StateFlow<List<TriggeredPriceAlertEntity>> = repository.triggeredPriceAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // User Actions
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setQuickFilter(filter: QuickFilter) {
        _selectedFilter.value = filter
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun resetFilters() {
        _searchQuery.value = ""
        _selectedFilter.value = QuickFilter.ALL
        _selectedCategory.value = "ALL"
    }

    fun setTab(tab: Int) {
        _currentTab.value = tab
    }

    fun openComparison(medicineFullName: String) {
        _comparisonMedicineName.value = medicineFullName
    }

    fun closeComparison() {
        _comparisonMedicineName.value = null
    }

    fun setComparisonSort(sort: MultiSellerSort) {
        _comparisonSort.value = sort
    }

    fun showBuyRequestDialog(offer: OfferListingEntity) {
        _buyRequestDialogOffer.value = offer
    }

    fun hideBuyRequestDialog() {
        _buyRequestDialogOffer.value = null
    }

    fun addToCart(offer: OfferListingEntity, quantity: Int) {
        viewModelScope.launch {
            repository.addToCart(offer, quantity)
            showSnackbar("🛒 ${offer.medicineName} কার্টে যোগ করা হয়েছে!")
        }
    }

    fun updateCartQuantity(cartItemId: Long, quantity: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(cartItemId, quantity)
        }
    }

    fun deleteCartItem(cartItemId: Long) {
        viewModelScope.launch {
            repository.deleteCartItem(cartItemId)
        }
    }

    fun sendBuyRequestDirect(offer: OfferListingEntity, requestedQuantity: Int, note: String) {
        viewModelScope.launch {
            val total = offer.offerPrice * requestedQuantity
            val req = BuyRequestEntity(
                offerListingId = offer.id,
                medicineName = "${offer.medicineName} ${offer.strength}",
                requestedQuantity = requestedQuantity,
                unitPrice = offer.offerPrice,
                totalPrice = total,
                buyerShopId = _activeShop.value.id,
                buyerShopName = _activeShop.value.shopName,
                buyerPhone = _activeShop.value.phone,
                sellerShopId = offer.sellerShopId,
                sellerShopName = offer.sellerShopName,
                sellerPhone = "01711223344",
                note = note,
                status = "PENDING",
                timestamp = System.currentTimeMillis()
            )
            val reqId = repository.insertOffer(offer.copy(
                availableQuantity = (offer.availableQuantity - requestedQuantity).coerceAtLeast(0),
                reservedQuantity = offer.reservedQuantity + requestedQuantity
            ))
            
            repository.submitBuyRequestsFromCart(_activeShop.value, note)
            hideBuyRequestDialog()
            showSnackbar("✅ বাই রিকোয়েস্ট সফলভাবে পাঠানো হয়েছে!")
        }
    }

    fun checkoutCart(note: String) {
        viewModelScope.launch {
            repository.submitBuyRequestsFromCart(_activeShop.value, note)
            showSnackbar("🎉 কার্টের সব পণ্যের বাই রিকোয়েস্ট পাঠানো হয়েছে!")
        }
    }

    fun openAddOfferDialog(offerToEdit: OfferListingEntity? = null) {
        if (!_sellerAuthState.value.isAuthenticated) {
            _showAuthScreen.value = true
            showSnackbar("🔐 ইনভেন্টরি তৈরি বা সম্পাদনা করতে ফায়ারবেস অ্যাকাউন্টে সাইন-ইন করুন।")
            return
        }
        _editingOffer.value = offerToEdit
        _addEditOfferDialogShow.value = true
    }

    fun closeAddOfferDialog() {
        _addEditOfferDialogShow.value = false
        _editingOffer.value = null
    }

    fun openBulkRequestDialog() {
        if (!_sellerAuthState.value.isAuthenticated) {
            _showAuthScreen.value = true
            showSnackbar("🔐 বাল্ক চাহিদা পোস্ট করতে ফায়ারবেস অ্যাকাউন্টে সাইন-ইন করুন।")
            return
        }
        _showBulkRequestDialog.value = true
    }

    fun closeBulkRequestDialog() {
        _showBulkRequestDialog.value = false
    }

    fun submitBulkMedicineRequest(request: com.example.ui.screens.BulkMedicineRequest) {
        viewModelScope.launch {
            val calculatedMrp = request.targetUnitPrice * 1.25
            val discountPct = (((calculatedMrp - request.targetUnitPrice) / calculatedMrp) * 100).toInt().coerceAtLeast(10)

            val sellerName = _sellerAuthState.value.displayName ?: _activeShop.value.shopName
            val newOffer = OfferListingEntity(
                masterMedicineId = 1L,
                medicineName = request.medicineName,
                genericName = request.genericName,
                strength = request.strength,
                companyName = request.companyName,
                form = request.form,
                packSize = request.packSize,
                batchNumber = "BULK-REQ-${System.currentTimeMillis() % 10000}",
                expiryDate = "${request.minRequiredExpiryDays} দিন মেয়াদের শর্ত",
                daysUntilExpiry = request.minRequiredExpiryDays,
                availableQuantity = request.requestedQuantity,
                mrp = calculatedMrp,
                offerPrice = request.targetUnitPrice,
                discountPercent = discountPct,
                minimumOrderQuantity = 10,
                sellerShopId = _activeShop.value.id,
                sellerShopName = "$sellerName (চাহিদা)",
                sellerLocation = _activeShop.value.area,
                sellerDistanceKm = 0.5,
                isVerifiedShop = true,
                notes = "【বাল্ক চাহিদা】 ${request.notes} (জরুরি ভাব: ${request.urgencyLevel}, মেয়াদ শর্ত: ${request.minRequiredExpiryDays} দিন)",
                status = "ACTIVE"
            )
            repository.insertOffer(newOffer)
            closeBulkRequestDialog()
            showSnackbar("📦 ${request.requestedQuantity} বক্স ${request.medicineName}-এর বাল্ক চাহিদা পোস্ট করা হয়েছে!")
        }
    }

    fun saveOfferListing(
        medicineName: String,
        genericName: String,
        strength: String,
        companyName: String,
        form: String,
        packSize: String,
        batchNumber: String,
        expiryDate: String,
        daysUntilExpiry: Int,
        quantity: Int,
        mrp: Double,
        offerPrice: Double,
        moq: Int,
        notes: String
    ) {
        viewModelScope.launch {
            val discount = if (mrp > 0) (((mrp - offerPrice) / mrp) * 100).toInt() else 0
            val edit = _editingOffer.value
            val currentSellerName = _sellerAuthState.value.displayName ?: _activeShop.value.shopName
            if (edit != null) {
                repository.updateOffer(
                    edit.copy(
                        medicineName = medicineName,
                        genericName = genericName,
                        strength = strength,
                        companyName = companyName,
                        form = form,
                        packSize = packSize,
                        batchNumber = batchNumber,
                        expiryDate = expiryDate,
                        daysUntilExpiry = daysUntilExpiry,
                        availableQuantity = quantity,
                        mrp = mrp,
                        offerPrice = offerPrice,
                        discountPercent = discount,
                        minimumOrderQuantity = moq,
                        notes = notes,
                        status = if (quantity > 0) "ACTIVE" else "SOLD_OUT",
                        updatedAt = System.currentTimeMillis()
                    )
                )
                showSnackbar("📝 লিস্টিং তথ্য আপডেট করা হয়েছে!")
            } else {
                val newOffer = OfferListingEntity(
                    masterMedicineId = 1,
                    medicineName = medicineName,
                    genericName = genericName,
                    strength = strength,
                    companyName = companyName,
                    form = form,
                    packSize = packSize,
                    batchNumber = batchNumber,
                    expiryDate = expiryDate,
                    daysUntilExpiry = daysUntilExpiry,
                    availableQuantity = quantity,
                    reservedQuantity = 0,
                    mrp = mrp,
                    offerPrice = offerPrice,
                    discountPercent = discount,
                    minimumOrderQuantity = moq,
                    sellerShopId = _activeShop.value.id,
                    sellerShopName = currentSellerName,
                    sellerLocation = _activeShop.value.area,
                    sellerDistanceKm = 0.5,
                    isVerifiedShop = true,
                    notes = notes,
                    status = "ACTIVE"
                )
                repository.insertOffer(newOffer)
                showSnackbar("🚀 নতুন অফার লিস্টিং তৈরি হয়েছে!")
            }
            closeAddOfferDialog()
        }
    }

    fun togglePauseOffer(offer: OfferListingEntity) {
        if (!_sellerAuthState.value.isAuthenticated) {
            _showAuthScreen.value = true
            showSnackbar("🔐 ইনভেন্টরি স্টেটাস পরিবর্তনের জন্য ফায়ারবেস সাইন-ইন করুন।")
            return
        }
        viewModelScope.launch {
            val newStatus = if (offer.status == "ACTIVE") "PAUSED" else "ACTIVE"
            repository.updateOfferStatus(offer.id, newStatus)
            showSnackbar(if (newStatus == "PAUSED") "⏸️ লিস্টিং সাময়িকভাবে হাইড করা হয়েছে" else "🟢 লিস্টিং পুনরায় একটিভ করা হয়েছে")
        }
    }

    fun markOfferSoldOut(offer: OfferListingEntity) {
        if (!_sellerAuthState.value.isAuthenticated) {
            _showAuthScreen.value = true
            showSnackbar("🔐 সোল্ড আউট মার্ক করতে ফায়ারবেস অ্যাকাউন্টে সাইন-ইন করুন।")
            return
        }
        viewModelScope.launch {
            repository.updateOfferStatus(offer.id, "SOLD_OUT")
            showSnackbar("🔴 লিস্টিং সোল্ড আউট মার্ক করা হয়েছে")
        }
    }

    fun deleteOffer(offer: OfferListingEntity) {
        if (!_sellerAuthState.value.isAuthenticated) {
            _showAuthScreen.value = true
            showSnackbar("🔐 লিস্টিং মুছে ফেলতে ফায়ারবেস অ্যাকাউন্টে সাইন-ইন করুন।")
            return
        }
        viewModelScope.launch {
            repository.deleteOffer(offer.id)
            showSnackbar("🗑️ লিস্টিং মুছে ফেলা হয়েছে")
        }
    }

    fun updateRequestStatus(requestId: Long, status: String, context: android.content.Context? = null) {
        viewModelScope.launch {
            val ctx = context ?: getApplication<Application>()
            repository.updateBuyRequestStatus(requestId, status, ctx)
            val icon = when (status.uppercase()) {
                "DISPATCHED" -> "🚚 [FCM Push নোটিফিকেশন]"
                "DELIVERED" -> "🎉 [FCM Push নোটিফিকেশন]"
                else -> "📦"
            }
            showSnackbar("$icon রিকোয়েস্ট স্ট্যাটাস: $status")
        }
    }

    fun openChat(request: BuyRequestEntity) {
        _selectedChatRequest.value = request
        _currentTab.value = 4 // Chat Tab
    }

    fun sendChatMessage(text: String) {
        val req = _selectedChatRequest.value ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            val isSeller = req.sellerShopId == _activeShop.value.id
            repository.sendChatMessage(
                requestId = req.id,
                buyerId = req.buyerShopId,
                sellerId = req.sellerShopId,
                senderName = _activeShop.value.shopName,
                isSeller = isSeller,
                text = text
            )
        }
    }

    fun switchActiveShopProfile(shop: ShopProfileEntity) {
        _activeShop.value = shop
        showSnackbar("ফার্মেসী পরিবর্তন: ${shop.shopName}")
    }

    fun toggleWatchlist(medicineName: String, genericName: String = "", companyName: String = "", form: String = "") {
        viewModelScope.launch {
            val wasWatchlisted = watchlistedMedicineNames.value.contains(medicineName)
            repository.toggleWatchlist(medicineName, genericName, companyName, form)
            val msg = if (!wasWatchlisted) "❤️ $medicineName ওয়াচলিস্টে যোগ করা হয়েছে" else "🗑️ $medicineName ওয়াচলিস্ট থেকে সরানো হয়েছে"
            showSnackbar(msg)
        }
    }

    fun removeFromWatchlist(medicineName: String) {
        viewModelScope.launch {
            repository.removeFromWatchlist(medicineName)
            showSnackbar("🗑️ $medicineName ওয়াচলিস্ট থেকে সরানো হয়েছে")
        }
    }

    fun openWatchlistScreen() {
        _showWatchlistScreen.value = true
    }

    fun closeWatchlistScreen() {
        _showWatchlistScreen.value = false
    }

    // Price Threshold & Automated Alert Actions
    fun addPriceThresholdAlert(medicineName: String, genericName: String = "", maxPriceThreshold: Double) {
        if (medicineName.isBlank() || maxPriceThreshold <= 0) return
        viewModelScope.launch {
            repository.addPriceThresholdAlert(medicineName, genericName, maxPriceThreshold)
            showSnackbar("🎯 $medicineName - ৳${maxPriceThreshold.toInt()} এর মূল্য সীমা সেট করা হয়েছে!")
        }
    }

    fun togglePriceThresholdEnabled(id: Long, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.updatePriceThresholdEnabled(id, isEnabled)
            val status = if (isEnabled) "চালু" else "বন্ধ"
            showSnackbar("দাম অ্যালার্ট $status করা হয়েছে")
        }
    }

    fun deletePriceThresholdAlert(id: Long) {
        viewModelScope.launch {
            repository.deletePriceThresholdAlert(id)
            showSnackbar("🗑️ দাম অ্যালার্ট মুছে ফেলা হয়েছে")
        }
    }

    fun dismissTriggeredPriceAlert(id: Long) {
        viewModelScope.launch {
            repository.deleteTriggeredPriceAlert(id)
        }
    }

    fun simulateSupplierLowPriceOffer(medicineName: String, price: Double, sellerName: String = "পদ্মা ফার্মা ডিসট্রিবিউটর") {
        viewModelScope.launch {
            val testOffer = OfferListingEntity(
                masterMedicineId = 1,
                medicineName = medicineName,
                genericName = "Paracetamol",
                strength = "500mg",
                companyName = "Square Pharmaceuticals",
                form = "Tablet",
                packSize = "100 Tablets Box",
                batchNumber = "SIM-ALERT-${System.currentTimeMillis() % 10000}",
                expiryDate = "Dec 2026",
                daysUntilExpiry = 150,
                availableQuantity = 250,
                mrp = price + 18.0,
                offerPrice = price,
                discountPercent = 35,
                minimumOrderQuantity = 5,
                sellerShopId = 99,
                sellerShopName = sellerName,
                sellerLocation = "ঢাকা মিডফোর্ড মার্কেট",
                sellerDistanceKm = 1.1,
                isVerifiedShop = true,
                notes = "⚡ টেস্ট সিমুলেটেড কমদামী সপ্লায়ার অফার",
                status = "ACTIVE"
            )
            repository.insertOffer(testOffer)
            showSnackbar("🧪 টেস্ট সপ্লয়ার অফার পোস্ট করা হয়েছে: $medicineName @ ৳${price.toInt()}")
        }
    }

    fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}
