package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Compare
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.entities.OfferListingEntity
import com.example.data.db.entities.ShopProfileEntity
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.outlined.PostAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import com.example.ui.components.CategoryFilterBar
import com.example.ui.components.MedicineOfferCard
import com.example.ui.components.MedicineOfferGridCard
import com.example.ui.components.MultiSellerComparisonCard
import com.example.ui.components.PharmacyRequestSearchBar
import com.example.ui.components.QuickFilterBar
import com.example.ui.theme.BorderGray
import com.example.ui.theme.CardBorder
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.InfoBlueBg
import com.example.ui.theme.InfoBlueBorder
import com.example.ui.theme.InfoBlueText
import com.example.ui.theme.PharmaBlueLight
import com.example.ui.theme.RoyalPharmaBlue
import com.example.ui.theme.SoftPaperGray
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UrgentRed
import com.example.ui.viewmodel.QuickFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    activeShop: ShopProfileEntity,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: QuickFilter,
    onFilterSelected: (QuickFilter) -> Unit,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    onResetFilters: () -> Unit,
    offersList: List<OfferListingEntity>,
    groupedCatalog: Map<String, List<OfferListingEntity>>,
    cartCount: Int,
    watchlistedNames: Set<String> = emptySet(),
    onToggleWatchlist: ((String, String, String, String) -> Unit)? = null,
    onOpenWatchlistClick: () -> Unit = {},
    onBuyRequestClick: (OfferListingEntity) -> Unit,
    onChatClick: (OfferListingEntity) -> Unit,
    onCompareClick: (medicineFullName: String) -> Unit,
    onOpenCartClick: () -> Unit,
    onPostBulkRequestClick: () -> Unit = {},
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 0 = Live Offers Feed, 1 = Multi-Seller Catalog Compare View
    var viewMode by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SoftPaperGray)
    ) {
        // App Header Bar - Vibrant Theme Clean Surface
        Surface(
            color = SoftPaperGray,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Avatar Store Icon Box
                        Surface(
                            shape = CircleShape,
                            color = RoyalPharmaBlue,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Storefront,
                                    contentDescription = "Shop Logo",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = activeShop.shopName,
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = "Verified Shop",
                                    tint = RoyalPharmaBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "${activeShop.area}, Dhaka",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = onOpenWatchlistClick,
                            modifier = Modifier.testTag("nav_watchlist_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (watchlistedNames.isNotEmpty()) {
                                        Badge(containerColor = UrgentRed) {
                                            Text("${watchlistedNames.size}", color = Color.White, fontSize = 10.sp)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (watchlistedNames.isNotEmpty()) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Watchlist",
                                    tint = UrgentRed
                                )
                            }
                        }

                        IconButton(onClick = onOpenCartClick) {
                            BadgedBox(
                                badge = {
                                    if (cartCount > 0) {
                                        Badge(containerColor = EmeraldGreen) {
                                            Text("$cartCount", color = Color.White, fontSize = 10.sp)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ShoppingCart,
                                    contentDescription = "Cart",
                                    tint = TextSecondary
                                )
                            }
                        }

                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Pharmacy Request Search & Category Filter Component
                PharmacyRequestSearchBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    selectedCategory = selectedCategory,
                    onCategorySelected = onCategorySelected,
                    totalResultCount = offersList.size,
                    onScanBarcodeClick = { /* Barcode scanner callback */ },
                    onResetFiltersClick = {
                        onSearchQueryChange("")
                        onCategorySelected("ALL")
                    }
                )
            }
        }

        // Quick Status Filter Chips Bar
        QuickFilterBar(
            selectedFilter = selectedFilter,
            onFilterSelected = onFilterSelected
        )

        // View Mode Switcher Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (viewMode) {
                    0 -> "সেরা অফার (${offersList.size} টি)"
                    1 -> "গ্রিড ভিউ (${offersList.size} টি)"
                    else -> "ক্যাটালগ (${groupedCatalog.size} টি)"
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.5.sp
            )

            SingleChoiceSegmentedButtonRow {
                SegmentedButton(
                    selected = viewMode == 0,
                    onClick = { viewMode = 0 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                ) {
                    Text("ফিড", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                SegmentedButton(
                    selected = viewMode == 1,
                    onClick = { viewMode = 1 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                ) {
                    Text("গ্রিড", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                SegmentedButton(
                    selected = viewMode == 2,
                    onClick = { viewMode = 2 },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                ) {
                    Text("তুলনা", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Bulk Procurement Request Banner Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, CardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .testTag("bulk_request_banner_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = PharmaBlueLight,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.PostAdd,
                                contentDescription = "Post Request",
                                tint = RoyalPharmaBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "পাইকারি ওষুধ প্রয়োজন?",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "পরিমাণ ও মেয়াদের শর্ত সহ বাল্ক চাহিদা পোস্ট করুন",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }

                Button(
                    onClick = onPostBulkRequestClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalPharmaBlue),
                    modifier = Modifier.testTag("btn_open_bulk_request_form")
                ) {
                    Text("চাহিদা দিন", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // Partial Buying Banner Notice
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = InfoBlueBg,
            border = BorderStroke(1.dp, InfoBlueBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = InfoBlueBorder,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Notice",
                            tint = InfoBlueText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = "আংশিক ক্রয় সুবিধা: আপনার যতটুকু প্রয়োজন (যেমন ২০ বক্স) ততটুকুই Buy Request পাঠাতে পারেন।",
                    fontSize = 11.sp,
                    color = InfoBlueText,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 15.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Main List & Grid Content wrapped in PullToRefreshBox
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .testTag("dashboard_pull_refresh")
        ) {
            when (viewMode) {
                0 -> {
                // View Mode 0: Live Offer Feed
                if (offersList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.Storefront,
                                contentDescription = "No offers",
                                tint = TextSecondary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "কোনো ওষুধ বা অফার পাওয়া যায়নি",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "অন্য নাম, ক্যাটাগরি বা ফিল্টার সিলেক্ট করে চেষ্টা করুন।",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onResetFilters,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalPharmaBlue)
                            ) {
                                Text(
                                    text = "সব ফিল্টার রিসেট করুন",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(offersList, key = { it.id }) { offer ->
                            val isBookmarked = watchlistedNames.contains(offer.medicineName) ||
                                    watchlistedNames.contains("${offer.medicineName} ${offer.strength}")
                            MedicineOfferCard(
                                offer = offer,
                                isWatchlisted = isBookmarked,
                                onToggleWatchlist = if (onToggleWatchlist != null) {
                                    { onToggleWatchlist(offer.medicineName, offer.genericName, offer.companyName, offer.form) }
                                } else null,
                                onBuyRequestClick = { onBuyRequestClick(offer) },
                                onChatClick = { onChatClick(offer) }
                            )
                        }
                    }
                }
            }
            1 -> {
                // View Mode 1: Dashboard Grid (LazyVerticalGrid)
                if (offersList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.Storefront,
                                contentDescription = "No offers",
                                tint = TextSecondary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "কোনো ওষুধ বা অফার পাওয়া যায়নি",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "অন্য নাম, ক্যাটাগরি বা ফিল্টার সিলেক্ট করে চেষ্টা করুন।",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onResetFilters,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalPharmaBlue)
                            ) {
                                Text(
                                    text = "সব ফিল্টার রিসেট করুন",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("dashboard_medicine_grid"),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 80.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        gridItems(offersList, key = { it.id }) { offer ->
                            val isBookmarked = watchlistedNames.contains(offer.medicineName) ||
                                    watchlistedNames.contains("${offer.medicineName} ${offer.strength}")
                            MedicineOfferGridCard(
                                offer = offer,
                                isWatchlisted = isBookmarked,
                                onToggleWatchlist = if (onToggleWatchlist != null) {
                                    { onToggleWatchlist(offer.medicineName, offer.genericName, offer.companyName, offer.form) }
                                } else null,
                                onBuyRequestClick = { onBuyRequestClick(offer) },
                                onChatClick = { onChatClick(offer) }
                            )
                        }
                    }
                }
            }
            else -> {
                // View Mode 2: Multi-Seller Catalog Compare
                if (groupedCatalog.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.Compare,
                                contentDescription = "Empty Catalog",
                                tint = TextSecondary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "ক্যাটালগে কোনো প্রডাক্ট নেই",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(groupedCatalog.entries.toList(), key = { it.key }) { (medName, offers) ->
                            val firstOffer = offers.firstOrNull()
                            val isBookmarked = watchlistedNames.contains(medName) ||
                                    offers.any { watchlistedNames.contains(it.medicineName) }
                            MultiSellerComparisonCard(
                                medicineFullName = medName,
                                offers = offers,
                                isWatchlisted = isBookmarked,
                                onToggleWatchlist = if (onToggleWatchlist != null && firstOffer != null) {
                                    { onToggleWatchlist(firstOffer.medicineName, firstOffer.genericName, firstOffer.companyName, firstOffer.form) }
                                } else null,
                                onCompareClick = { onCompareClick(medName) }
                            )
                        }
                    }
                }
            }
        }
    }
}
}

