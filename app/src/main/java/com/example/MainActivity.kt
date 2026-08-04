package com.example

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.util.PharmaNotificationHelper
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.PersistentRequestBottomSheet
import com.example.ui.screens.AddEditOfferDialog
import com.example.ui.screens.BulkMedicineRequestDialog
import com.example.ui.screens.BuyRequestDialog
import com.example.ui.screens.CartScreen
import com.example.ui.screens.FirestoreProductsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InAppChatScreen
import com.example.ui.screens.MultiSellerComparisonScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SellerAuthScreen
import com.example.ui.screens.SellerDashboardScreen
import com.example.ui.screens.ShopProfileScreen
import com.example.ui.screens.WatchlistScreen
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PharmaBazaarTheme
import com.example.ui.theme.RoyalPharmaBlue
import com.example.ui.viewmodel.PharmaViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    try {
      if (FirebaseApp.getApps(this).isEmpty()) {
        FirebaseApp.initializeApp(this)
      }
    } catch (e: Exception) {
      // Safe fallback if Firebase config is missing
    }

    // Initialize FCM Notification Channel
    PharmaNotificationHelper.createNotificationChannel(this)

    // Request notification permission for Android 13+ (API level 33+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
        requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
      }
    }

    // Fetch FCM Registration Token
    try {
      FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
        if (task.isSuccessful) {
          val fcmToken = task.result
          Log.d("PharmaFCM", "FCM Device Token retrieved successfully: $fcmToken")
        }
      }
    } catch (e: Exception) {
      Log.w("PharmaFCM", "FCM token initialization skipped: ${e.message}")
    }

    enableEdgeToEdge()
    setContent {
      PharmaBazaarTheme {
        PharmaBazaarApp()
      }
    }
  }
}

@Composable
fun PharmaBazaarApp(viewModel: PharmaViewModel = viewModel()) {
  val context = LocalContext.current
  val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
  val activeShop by viewModel.activeShop.collectAsStateWithLifecycle()
  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
  val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
  val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
  val marketplaceSort by viewModel.marketplaceSort.collectAsStateWithLifecycle()
  val marketplaceFilter by viewModel.marketplaceFilter.collectAsStateWithLifecycle()
  val filteredOffers by viewModel.filteredOffers.collectAsStateWithLifecycle()
  val catalogGroupedOffers by viewModel.catalogGroupedOffers.collectAsStateWithLifecycle()
  val comparisonMedicineName by viewModel.comparisonMedicineName.collectAsStateWithLifecycle()
  val comparisonSellersList by viewModel.comparisonSellersList.collectAsStateWithLifecycle()
  val comparisonSort by viewModel.comparisonSort.collectAsStateWithLifecycle()
  val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
  val cartTotalPrice by viewModel.cartTotalPrice.collectAsStateWithLifecycle()
  val masterMedicines by viewModel.masterMedicines.collectAsStateWithLifecycle()
  val sellerInventory by viewModel.sellerInventory.collectAsStateWithLifecycle()
  val buyRequests by viewModel.buyRequests.collectAsStateWithLifecycle()
  val selectedChatRequest by viewModel.selectedChatRequest.collectAsStateWithLifecycle()
  val currentChatMessages by viewModel.currentChatMessages.collectAsStateWithLifecycle()
  val allShops by viewModel.allShops.collectAsStateWithLifecycle()

  val watchlistItems by viewModel.watchlistItems.collectAsStateWithLifecycle()
  val watchlistedMedicineNames by viewModel.watchlistedMedicineNames.collectAsStateWithLifecycle()
  val showWatchlistScreen by viewModel.showWatchlistScreen.collectAsStateWithLifecycle()
  val showSearchScreen by viewModel.showSearchScreen.collectAsStateWithLifecycle()
  val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()
  val thresholdAlerts by viewModel.priceThresholdAlerts.collectAsStateWithLifecycle()
  val triggeredAlerts by viewModel.triggeredPriceAlerts.collectAsStateWithLifecycle()

  val showAuthScreen by viewModel.showAuthScreen.collectAsStateWithLifecycle()
  val sellerAuthState by viewModel.sellerAuthState.collectAsStateWithLifecycle()

  val showFirestoreProductsScreen by viewModel.showFirestoreProductsScreen.collectAsStateWithLifecycle()
  val firestoreProducts by viewModel.filteredFirestoreProducts.collectAsStateWithLifecycle()
  val isFirestoreLoading by viewModel.isFirestoreLoading.collectAsStateWithLifecycle()
  val firestoreSearchQuery by viewModel.firestoreSearchQuery.collectAsStateWithLifecycle()
  val firestoreSortOption by viewModel.firestoreSortOption.collectAsStateWithLifecycle()
  val firestoreCategoryFilter by viewModel.firestoreCategoryFilter.collectAsStateWithLifecycle()

  val buyRequestDialogOffer by viewModel.buyRequestDialogOffer.collectAsStateWithLifecycle()
  val addEditOfferDialogShow by viewModel.addEditOfferDialogShow.collectAsStateWithLifecycle()
  val showBulkRequestDialog by viewModel.showBulkRequestDialog.collectAsStateWithLifecycle()
  val editingOffer by viewModel.editingOffer.collectAsStateWithLifecycle()
  val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()
  val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
  val aiSuggestions by viewModel.aiSuggestions.collectAsStateWithLifecycle()
  val isGeneratingAiSuggestions by viewModel.isGeneratingAiSuggestions.collectAsStateWithLifecycle()

  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(snackbarMessage) {
    snackbarMessage?.let {
      snackbarHostState.showSnackbar(it)
      viewModel.clearSnackbar()
    }
  }

  Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    bottomBar = {
      if (comparisonMedicineName == null) {
        NavigationBar(
          containerColor = Color.White,
          tonalElevation = 8.dp,
          modifier = Modifier.testTag("main_bottom_nav")
        ) {
          // Tab 0: Home Feed
          NavigationBarItem(
            selected = currentTab == 0,
            onClick = { viewModel.setTab(0) },
            icon = { Icon(imageVector = Icons.Outlined.Storefront, contentDescription = "Feed") },
            label = { Text("ফিড", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = RoyalPharmaBlue,
              selectedTextColor = RoyalPharmaBlue,
              indicatorColor = Color(0xFFE0EDFF)
            ),
            modifier = Modifier.testTag("nav_tab_feed")
          )

          // Tab 1: Cart
          NavigationBarItem(
            selected = currentTab == 1,
            onClick = { viewModel.setTab(1) },
            icon = {
              BadgedBox(
                badge = {
                  if (cartItems.isNotEmpty()) {
                    Badge(containerColor = EmeraldGreen) {
                      Text("${cartItems.size}", color = Color.White, fontSize = 10.sp)
                    }
                  }
                }
              ) {
                Icon(imageVector = Icons.Outlined.ShoppingCart, contentDescription = "Cart")
              }
            },
            label = { Text("কার্ট", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = RoyalPharmaBlue,
              selectedTextColor = RoyalPharmaBlue,
              indicatorColor = Color(0xFFE0EDFF)
            ),
            modifier = Modifier.testTag("nav_tab_cart")
          )

          // Tab 2: Seller Inventory
          NavigationBarItem(
            selected = currentTab == 2,
            onClick = { viewModel.setTab(2) },
            icon = { Icon(imageVector = Icons.Outlined.Inventory2, contentDescription = "Inventory") },
            label = { Text("ইনভেন্টরি", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = RoyalPharmaBlue,
              selectedTextColor = RoyalPharmaBlue,
              indicatorColor = Color(0xFFE0EDFF)
            ),
            modifier = Modifier.testTag("nav_tab_inventory")
          )

          // Tab 3: Chat
          NavigationBarItem(
            selected = currentTab == 3,
            onClick = { viewModel.setTab(3) },
            icon = { Icon(imageVector = Icons.Outlined.Chat, contentDescription = "Chat") },
            label = { Text("চ্যাট", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = RoyalPharmaBlue,
              selectedTextColor = RoyalPharmaBlue,
              indicatorColor = Color(0xFFE0EDFF)
            ),
            modifier = Modifier.testTag("nav_tab_chat")
          )

          // Tab 4: Shop Profile
          NavigationBarItem(
            selected = currentTab == 4,
            onClick = { viewModel.setTab(4) },
            icon = { Icon(imageVector = Icons.Outlined.Person, contentDescription = "Profile") },
            label = { Text("প্রোফাইল", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = RoyalPharmaBlue,
              selectedTextColor = RoyalPharmaBlue,
              indicatorColor = Color(0xFFE0EDFF)
            ),
            modifier = Modifier.testTag("nav_tab_profile")
          )
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      if (showAuthScreen) {
        SellerAuthScreen(
          isAuthenticated = sellerAuthState.isAuthenticated,
          userEmail = sellerAuthState.userEmail,
          displayName = sellerAuthState.displayName,
          authMethod = sellerAuthState.authMethod,
          isLoading = sellerAuthState.isLoading,
          errorMessage = sellerAuthState.errorMessage,
          onLoginClick = { email, pass -> viewModel.loginWithEmail(email, pass) },
          onRegisterClick = { email, pass, shop, lic, ph ->
            viewModel.registerPharmacySeller(email, pass, shop, lic, ph)
          },
          onGoogleSignInClick = { activity -> viewModel.signInWithGoogleCredential(activity) },
          onGuestLoginClick = { viewModel.guestLogin() },
          onSignOutClick = { viewModel.signOutSeller() },
          onClearError = { viewModel.clearAuthError() },
          onBackClick = { viewModel.closeAuthScreen() }
        )
      } else if (showFirestoreProductsScreen) {
        FirestoreProductsScreen(
          products = firestoreProducts,
          isLoading = isFirestoreLoading,
          searchQuery = firestoreSearchQuery,
          onSearchQueryChange = { viewModel.setFirestoreSearchQuery(it) },
          selectedCategory = firestoreCategoryFilter,
          onCategorySelect = { viewModel.setFirestoreCategoryFilter(it) },
          selectedSort = firestoreSortOption,
          onSortSelect = { viewModel.setFirestoreSortOption(it) },
          onRefresh = { viewModel.loadFirestoreProducts() },
          onBackClick = { viewModel.closeFirestoreProductsScreen() },
          onBuyClick = { offer -> viewModel.showBuyRequestDialog(offer) },
          onToggleWatchlist = { id ->
            val offer = firestoreProducts.find { it.id == id }
            if (offer != null) {
              viewModel.toggleWatchlist(offer.medicineName, offer.genericName, offer.companyName, offer.form)
            }
          },
          watchlistIds = firestoreProducts.filter { watchlistedMedicineNames.contains(it.medicineName) }.map { it.id },
          onOpenChat = { offer ->
            val req = buyRequests.find { it.offerListingId == offer.id }
            if (req != null) {
              viewModel.openChat(req)
              viewModel.setTab(3)
              viewModel.closeFirestoreProductsScreen()
            } else {
              viewModel.showBuyRequestDialog(offer)
            }
          }
        )
      } else if (showWatchlistScreen) {
        WatchlistScreen(
          watchlistItems = watchlistItems,
          allOffers = filteredOffers,
          onRemoveWatchlist = { med -> viewModel.removeFromWatchlist(med) },
          onCompareClick = { medName ->
            viewModel.closeWatchlistScreen()
            viewModel.openComparison(medName)
          },
          onBackClick = { viewModel.closeWatchlistScreen() },
          thresholdAlerts = thresholdAlerts,
          triggeredAlerts = triggeredAlerts,
          onAddThreshold = { med, gen, price -> viewModel.addPriceThresholdAlert(med, gen, price) },
          onToggleThreshold = { id, enabled -> viewModel.togglePriceThresholdEnabled(id, enabled) },
          onDeleteThreshold = { id -> viewModel.deletePriceThresholdAlert(id) },
          onDismissTriggeredAlert = { id -> viewModel.dismissTriggeredPriceAlert(id) },
          onAddToCart = { offer, qty -> viewModel.addToCart(offer, qty) },
          onSimulateOffer = { med, price, seller -> viewModel.simulateSupplierLowPriceOffer(med, price, seller) }
        )
      } else if (showSearchScreen) {
        SearchScreen(
          searchQuery = searchQuery,
          onSearchQueryChange = { viewModel.setSearchQuery(it) },
          selectedCategory = selectedCategory,
          onCategorySelected = { viewModel.setSelectedCategory(it) },
          offersList = filteredOffers,
          masterMedicines = masterMedicines,
          recentSearches = recentSearches,
          onAddRecentSearch = { q -> viewModel.addRecentSearch(q) },
          onClearRecentSearches = { viewModel.clearRecentSearches() },
          watchlistedNames = watchlistedMedicineNames,
          onToggleWatchlist = { name, gen, comp, form -> viewModel.toggleWatchlist(name, gen, comp, form) },
          onAddToCart = { offer -> viewModel.addToCart(offer, 1) },
          onCompareClick = { medName ->
            viewModel.closeSearchScreen()
            viewModel.openComparison(medName)
          },
          onBackClick = { viewModel.closeSearchScreen() }
        )
      } else if (comparisonMedicineName != null) {
        MultiSellerComparisonScreen(
          medicineFullName = comparisonMedicineName!!,
          sellerOffers = comparisonSellersList,
          activeSort = comparisonSort,
          onSortSelected = { viewModel.setComparisonSort(it) },
          onBackClick = { viewModel.closeComparison() },
          onBuyRequestClick = { offer -> viewModel.showBuyRequestDialog(offer) },
          onChatClick = { offer ->
            val req = buyRequests.find { it.offerListingId == offer.id }
            if (req != null) {
              viewModel.openChat(req)
              viewModel.setTab(3)
            } else {
              viewModel.showBuyRequestDialog(offer)
            }
          }
        )
      } else {
        when (currentTab) {
          0 -> HomeScreen(
            activeShop = activeShop,
            searchQuery = searchQuery,
            onSearchQueryChange = { viewModel.setSearchQuery(it) },
            selectedFilter = selectedFilter,
            onFilterSelected = { viewModel.setQuickFilter(it) },
            selectedCategory = selectedCategory,
            onCategorySelected = { viewModel.setSelectedCategory(it) },
            activeSort = marketplaceSort,
            onSortSelected = { viewModel.setMarketplaceSort(it) },
            activeFilter = marketplaceFilter,
            onFilterChanged = { viewModel.setMarketplaceFilter(it) },
            onResetFilters = { viewModel.resetFilters() },
            offersList = filteredOffers,
            groupedCatalog = catalogGroupedOffers,
            cartCount = cartItems.size,
            watchlistedNames = watchlistedMedicineNames,
            onToggleWatchlist = { med, gen, comp, form ->
              viewModel.toggleWatchlist(med, gen, comp, form)
            },
            onOpenWatchlistClick = { viewModel.openWatchlistScreen() },
            onOpenFirestoreCatalogClick = { viewModel.openFirestoreProductsScreen() },
            onOpenSearchScreenClick = { viewModel.openSearchScreen() },
            onBuyRequestClick = { offer -> viewModel.showBuyRequestDialog(offer) },
            onChatClick = { offer ->
              // Open or create chat
              val req = buyRequests.find { it.offerListingId == offer.id }
              if (req != null) viewModel.openChat(req)
              else viewModel.showBuyRequestDialog(offer)
            },
            onCompareClick = { medName -> viewModel.openComparison(medName) },
            onOpenCartClick = { viewModel.setTab(1) },
            onPostBulkRequestClick = { viewModel.openBulkRequestDialog() },
            aiSuggestions = aiSuggestions,
            isGeneratingAiSuggestions = isGeneratingAiSuggestions,
            onRefreshAiSuggestions = { viewModel.loadGeminiAiSuggestions() },
            onAddToCartFromAiMatch = { offerId ->
              val matchOffer = filteredOffers.find { it.id == offerId }
              if (matchOffer != null) {
                viewModel.addToCart(matchOffer, 1)
              }
            },
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshPriceLists() }
          )

          1 -> CartScreen(
            cartItems = cartItems,
            totalPrice = cartTotalPrice,
            buyRequests = buyRequests,
            activeShopName = activeShop?.shopName ?: "My Pharmacy",
            onUpdateQuantity = { id, qty -> viewModel.updateCartQuantity(id, qty) },
            onDeleteItem = { id -> viewModel.deleteCartItem(id) },
            onCheckout = { note -> viewModel.checkoutCart(note) },
            onUpdateOrderStatus = { id, status -> viewModel.updateRequestStatus(id, status) },
            onRefreshFirestoreOrders = { viewModel.refreshOrderHistoryFromFirestore() },
            onReorderClick = { req -> viewModel.reorderPreviousRequest(req) }
          )

          2 -> SellerDashboardScreen(
            activeShop = activeShop,
            sellerOffers = sellerInventory,
            masterMedicines = masterMedicines,
            onAddOfferClick = { viewModel.openAddOfferDialog(null) },
            onEditOfferClick = { offer -> viewModel.openAddOfferDialog(offer) },
            onTogglePauseClick = { offer -> viewModel.togglePauseOffer(offer) },
            onMarkSoldClick = { offer -> viewModel.markOfferSoldOut(offer) },
            onDeleteClick = { offer -> viewModel.deleteOffer(offer) },
            onQuickRestockClick = { offer, addQty -> viewModel.quickRestockOffer(offer, addQty) },
            onUpdateLowStockThreshold = { offer, newThresh -> viewModel.updateOfferLowStockThreshold(offer, newThresh) },
            sellerAuthState = sellerAuthState,
            buyRequests = buyRequests,
            onUpdateStatus = { requestId, status -> viewModel.updateRequestStatus(requestId, status) },
            onBulkUpdateStatus = { requestIds, status -> viewModel.updateMultipleOrderStatuses(requestIds, status) },
            onOpenAuthClick = { viewModel.openAuthScreen() },
            onPostBulkRequestClick = { viewModel.openBulkRequestDialog() },
            onExportCsvClick = { viewModel.exportInventoryCsv(context) },
            onRefreshFirestoreOrders = { viewModel.refreshOrderHistoryFromFirestore() }
          )

          3 -> InAppChatScreen(
            activeShop = activeShop,
            buyRequests = buyRequests,
            selectedRequest = selectedChatRequest,
            chatMessages = currentChatMessages,
            onSelectRequest = { req -> viewModel.openChat(req) },
            onSendMessage = { text -> viewModel.sendChatMessage(text) },
            onUpdateStatus = { id, status -> viewModel.updateRequestStatus(id, status) }
          )

          4 -> ShopProfileScreen(
            activeShop = activeShop,
            allShops = allShops,
            onSwitchShop = { shop -> viewModel.switchActiveShopProfile(shop) },
            sellerAuthState = sellerAuthState,
            onOpenAuthClick = { viewModel.openAuthScreen() },
            onSignOutClick = { viewModel.signOutSeller() }
          )
        }
      }

      // Persistent Request Bottom Sheet
      PersistentRequestBottomSheet(
        cartItems = cartItems,
        cartTotalPrice = cartTotalPrice,
        buyRequests = buyRequests,
        onUpdateQuantity = { id, qty -> viewModel.updateCartQuantity(id, qty) },
        onDeleteItem = { id -> viewModel.deleteCartItem(id) },
        onCheckoutCart = { note -> viewModel.checkoutCart(note) },
        onOpenChatForRequest = { req ->
          viewModel.openChat(req)
          viewModel.setTab(3) // Switch to Chat tab
        },
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(horizontal = 8.dp, vertical = 4.dp)
      )

      // Dialogs
      buyRequestDialogOffer?.let { offer ->
        BuyRequestDialog(
          offer = offer,
          onDismiss = { viewModel.hideBuyRequestDialog() },
          onSubmitRequest = { qty, note ->
            viewModel.sendBuyRequestDirect(offer, qty, note)
          },
          onAddToCart = { qty ->
            viewModel.addToCart(offer, qty)
          }
        )
      }

      if (addEditOfferDialogShow) {
        AddEditOfferDialog(
          masterMedicines = masterMedicines,
          offerToEdit = editingOffer,
          onDismiss = { viewModel.closeAddOfferDialog() },
          onSave = { medName, generic, str, comp, form, pack, batch, exp, days, qty, threshold, mrp, offerVal, moq, notes ->
            viewModel.saveOfferListing(
              medName, generic, str, comp, form, pack, batch, exp, days, qty, threshold, mrp, offerVal, moq, notes
            )
          }
        )
      }

      if (showBulkRequestDialog) {
        BulkMedicineRequestDialog(
          masterMedicines = masterMedicines,
          activeShop = activeShop,
          onDismiss = { viewModel.closeBulkRequestDialog() },
          onSubmitRequest = { request ->
            viewModel.submitBulkMedicineRequest(request)
          }
        )
      }
    }
  }
}
