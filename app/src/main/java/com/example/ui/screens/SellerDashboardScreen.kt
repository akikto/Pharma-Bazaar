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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.PostAdd
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import com.example.data.db.entities.BuyRequestEntity
import com.example.data.db.entities.OfferListingEntity
import com.example.data.db.entities.ShopProfileEntity
import com.example.ui.components.PharmacyRequestStatusTracker
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.mutableStateOf
import com.example.ui.theme.BorderGray
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.ExpiryAmber
import com.example.ui.theme.RoyalPharmaBlue
import com.example.ui.theme.SoftPaperGray
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

import com.example.ui.viewmodel.SellerAuthState

@Composable
fun SellerDashboardScreen(
    activeShop: ShopProfileEntity,
    sellerOffers: List<OfferListingEntity>,
    onAddOfferClick: () -> Unit,
    onEditOfferClick: (OfferListingEntity) -> Unit,
    onTogglePauseClick: (OfferListingEntity) -> Unit,
    onMarkSoldClick: (OfferListingEntity) -> Unit,
    onDeleteClick: (OfferListingEntity) -> Unit,
    sellerAuthState: SellerAuthState = SellerAuthState(),
    buyRequests: List<BuyRequestEntity> = emptyList(),
    onUpdateStatus: (requestId: Long, newStatus: String) -> Unit = { _, _ -> },
    onOpenAuthClick: () -> Unit = {},
    onPostBulkRequestClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 0 = Active, 1 = Paused, 2 = Sold Out
    var statusFilterTab by remember { mutableIntStateOf(0) }
    var dashboardSearchQuery by remember { mutableStateOf("") }

    val activeCount = sellerOffers.count { it.status == "ACTIVE" }
    val pausedCount = sellerOffers.count { it.status == "PAUSED" }
    val soldCount = sellerOffers.count { it.status == "SOLD_OUT" }

    val filteredList = sellerOffers.filter { offer ->
        val matchesStatus = when (statusFilterTab) {
            0 -> offer.status == "ACTIVE"
            1 -> offer.status == "PAUSED"
            2 -> offer.status == "SOLD_OUT"
            else -> true
        }
        val matchesSearch = if (dashboardSearchQuery.isBlank()) {
            true
        } else {
            val q = dashboardSearchQuery.trim().lowercase()
            offer.medicineName.lowercase().contains(q) ||
                    offer.companyName.lowercase().contains(q) ||
                    offer.genericName.lowercase().contains(q) ||
                    offer.batchNumber.lowercase().contains(q)
        }
        matchesStatus && matchesSearch
    }

    Scaffold(
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FloatingActionButton(
                    onClick = onPostBulkRequestClick,
                    containerColor = Color.White,
                    contentColor = RoyalPharmaBlue,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("post_bulk_request_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Outlined.PostAdd, contentDescription = "Bulk Request")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("বাল্ক চাহিদা পোস্ট", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                FloatingActionButton(
                    onClick = onAddOfferClick,
                    containerColor = RoyalPharmaBlue,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("add_listing_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Outlined.Add, contentDescription = "Add Offer")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("নতুন লিস্টিং যোগ করুন", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SoftPaperGray)
        ) {
            // Header Stats Bar
            Surface(
                color = RoyalPharmaBlue,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "বিক্রেতা ইনভেন্টরি ড্যাশবোর্ড",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${activeShop.shopName} (${activeShop.licenseNumber})",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }

                        Surface(
                            onClick = onOpenAuthClick,
                            shape = RoundedCornerShape(12.dp),
                            color = if (sellerAuthState.isAuthenticated) EmeraldGreen else Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.testTag("seller_dashboard_auth_pill")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = "Auth Status",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (sellerAuthState.isAuthenticated) "সাইন-ইন করা" else "লগইন করুন",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Overview Stat Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatChipCard("🟢 রানিং অফার", "$activeCount টি", modifier = Modifier.weight(1f))
                        StatChipCard("⏸️ পজ করা", "$pausedCount টি", modifier = Modifier.weight(1f))
                        StatChipCard("🔴 সোল্ড আউট", "$soldCount টি", modifier = Modifier.weight(1f))
                    }
                }
            }

            // --- Supplier Firebase Authentication Status Banner Card ---
            if (!sellerAuthState.isAuthenticated) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    border = BorderStroke(1.dp, RoyalPharmaBlue.copy(alpha = 0.35f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("supplier_auth_prompt_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = RoyalPharmaBlue.copy(alpha = 0.12f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.Lock,
                                        contentDescription = "Sign In Required",
                                        tint = RoyalPharmaBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "ফায়ারবেস সেশন সাইন-ইন প্রয়োজন",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "ইনভেন্টরি লিস্টিং ও অফার নিরাপদ রাখতে সাইন-ইন করুন",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                        Button(
                            onClick = onOpenAuthClick,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalPharmaBlue),
                            modifier = Modifier.testTag("btn_supplier_login_banner")
                        ) {
                            Text("লগইন", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            } else {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldGreenLight),
                    border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.35f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("supplier_auth_status_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = "Verified Supplier",
                                tint = EmeraldGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "সাপ্লায়ার অ্যাকাউন্ট: ${sellerAuthState.userEmail ?: sellerAuthState.displayName ?: "Firebase Pharmacist"}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "অথেন্টিকেশন: ${sellerAuthState.authMethod ?: "Firebase Auth"}",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = onOpenAuthClick,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, EmeraldGreen),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("btn_manage_supplier_account")
                        ) {
                            Text("ম্যানেজ অ্যাকাউন্ট", fontSize = 10.sp, color = EmeraldGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Dashboard Search Bar & Status Segmented Filter Tabs
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                // Search Bar Input
                OutlinedTextField(
                    value = dashboardSearchQuery,
                    onValueChange = { dashboardSearchQuery = it },
                    placeholder = {
                        Text(
                            text = "লিস্টিংয়ের নাম বা প্রস্তুতকারক/কোম্পানি খুঁজুন...",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search Dashboard",
                            tint = RoyalPharmaBlue
                        )
                    },
                    trailingIcon = {
                        if (dashboardSearchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { dashboardSearchQuery = "" },
                                modifier = Modifier.testTag("clear_seller_search_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Clear,
                                    contentDescription = "Clear Search",
                                    tint = TextSecondary
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = RoyalPharmaBlue,
                        unfocusedBorderColor = BorderGray
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .testTag("seller_dashboard_search_field")
                )

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = statusFilterTab == 0,
                        onClick = { statusFilterTab = 0 },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 4)
                    ) {
                        Text("🟢 Active ($activeCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    SegmentedButton(
                        selected = statusFilterTab == 1,
                        onClick = { statusFilterTab = 1 },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 4)
                    ) {
                        Text("⏸️ Paused ($pausedCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    SegmentedButton(
                        selected = statusFilterTab == 2,
                        onClick = { statusFilterTab = 2 },
                        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 4)
                    ) {
                        Text("🔴 Sold Out ($soldCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    SegmentedButton(
                        selected = statusFilterTab == 3,
                        onClick = { statusFilterTab = 3 },
                        shape = SegmentedButtonDefaults.itemShape(index = 3, count = 4),
                        modifier = Modifier.testTag("seller_requests_tracking_tab")
                    ) {
                        Text("📦 রিকোয়েস্ট (${buyRequests.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Inventory Offers or Request Status Tracking List
            if (statusFilterTab == 3) {
                if (buyRequests.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.Inventory2,
                                contentDescription = "Empty Requests",
                                tint = TextSecondary,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "এখনও কোনো ট্র্যাকিং রিকোয়েস্ট নেই",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "ফার্মেসী ক্যাটালগ বা কার্ট থেকে বাই রিকোয়েস্ট জমা হলে এখানে লাইভ ফায়ারস্টোর স্টেট ট্র্যাক করতে পারবেন।",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(buyRequests, key = { it.id }) { req ->
                            PharmacyRequestStatusTracker(
                                request = req,
                                onUpdateStatus = onUpdateStatus,
                                isSupplierView = true
                            )
                        }
                    }
                }
            } else if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.Inventory2,
                            contentDescription = "Empty Inventory",
                            tint = TextSecondary,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "এই স্ট্যাটাসে কোনো লিস্টিং নেই",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "নতুন অফার বা ওষুধ পোস্ট করতে নিচের বাটনটিতে ক্লিক করুন।",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList, key = { it.id }) { offer ->
                        SellerInventoryCard(
                            offer = offer,
                            onEditClick = { onEditOfferClick(offer) },
                            onTogglePauseClick = { onTogglePauseClick(offer) },
                            onMarkSoldClick = { onMarkSoldClick(offer) },
                            onDeleteClick = { onDeleteClick(offer) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatChipCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
            Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SellerInventoryCard(
    offer: OfferListingEntity,
    onEditClick: () -> Unit,
    onTogglePauseClick: () -> Unit,
    onMarkSoldClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val totalInitialStock = (offer.availableQuantity + offer.reservedQuantity).coerceAtLeast(1)
    val stockProgress = (offer.availableQuantity.toFloat() / totalInitialStock.toFloat()).coerceIn(0f, 1f)

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("seller_inventory_item_${offer.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "💊 ${offer.medicineName} ${offer.strength}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${offer.genericName} • ${offer.companyName} (${offer.packSize})",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (offer.status) {
                        "ACTIVE" -> EmeraldGreenLight
                        "PAUSED" -> Color(0xFFFEF3C7)
                        else -> Color(0xFFFEE2E2)
                    }
                ) {
                    Text(
                        text = when (offer.status) {
                            "ACTIVE" -> "🟢 Active"
                            "PAUSED" -> "⏸️ Paused"
                            else -> "🔴 Sold Out"
                        },
                        color = when (offer.status) {
                            "ACTIVE" -> EmeraldGreen
                            "PAUSED" -> ExpiryAmber
                            else -> Color.Red
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stock progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "📦 মজুদ স্টক: ${offer.availableQuantity} Box (MOQ: ${offer.minimumOrderQuantity} Box)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RoyalPharmaBlue
                )
                Text(
                    text = "📅 Expiry: ${offer.expiryDate}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { stockProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = if (offer.availableQuantity < 10) Color.Red else EmeraldGreen,
                trackColor = Color(0xFFE2E8F0)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "৳${offer.offerPrice.toInt()}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen
                    )
                    Text(
                        text = " (MRP: ৳${offer.mrp.toInt()})",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Text(
                    text = "ব্যাচ: ${offer.batchNumber}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))

            // Quick Action Buttons Row: [ Edit ], [ Pause/Resume ], [ Mark Sold ], [ Delete ]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onEditClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    Icon(imageVector = Icons.Outlined.Edit, contentDescription = "Edit", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Edit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onTogglePauseClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (offer.status == "ACTIVE") Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                        contentDescription = "Pause",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(if (offer.status == "ACTIVE") "Pause" else "Resume", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onMarkSoldClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    Icon(imageVector = Icons.Outlined.CheckCircle, contentDescription = "Sold", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Sold", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
