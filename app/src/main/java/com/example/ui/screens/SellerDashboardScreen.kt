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
import com.example.ui.components.SupplierBulkActionBar
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import com.example.data.db.entities.MasterMedicineEntity
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
    masterMedicines: List<MasterMedicineEntity> = emptyList(),
    onAddOfferClick: () -> Unit,
    onEditOfferClick: (OfferListingEntity) -> Unit,
    onTogglePauseClick: (OfferListingEntity) -> Unit,
    onMarkSoldClick: (OfferListingEntity) -> Unit,
    onDeleteClick: (OfferListingEntity) -> Unit,
    onQuickRestockClick: (OfferListingEntity, Int) -> Unit = { _, _ -> },
    onUpdateLowStockThreshold: (OfferListingEntity, Int) -> Unit = { _, _ -> },
    sellerAuthState: SellerAuthState = SellerAuthState(),
    buyRequests: List<BuyRequestEntity> = emptyList(),
    onUpdateStatus: (requestId: Long, newStatus: String) -> Unit = { _, _ -> },
    onBulkUpdateStatus: (requestIds: List<Long>, newStatus: String) -> Unit = { _, _ -> },
    onOpenAuthClick: () -> Unit = {},
    onPostBulkRequestClick: () -> Unit = {},
    onExportCsvClick: () -> Unit = {},
    onRefreshFirestoreOrders: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 0 = Active, 1 = Paused, 2 = Sold Out, 3 = Low Stock, 4 = Requests, 5 = Order History
    var statusFilterTab by remember { mutableIntStateOf(0) }
    var dashboardSearchQuery by remember { mutableStateOf("") }
    var selectedRequestIds by remember { mutableStateOf(setOf<Long>()) }

    val activeCount = sellerOffers.count { it.status == "ACTIVE" }
    val pausedCount = sellerOffers.count { it.status == "PAUSED" }
    val soldCount = sellerOffers.count { it.status == "SOLD_OUT" }
    val lowStockCount = sellerOffers.count { it.availableQuantity <= it.lowStockThreshold }

    val filteredList = sellerOffers.filter { offer ->
        val matchesStatus = when (statusFilterTab) {
            0 -> offer.status == "ACTIVE"
            1 -> offer.status == "PAUSED"
            2 -> offer.status == "SOLD_OUT"
            3 -> offer.availableQuantity <= offer.lowStockThreshold
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
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StatChipCard("🟢 রানিং", "$activeCount", modifier = Modifier.weight(1f))
                        StatChipCard("⚠️ লো স্টক", "$lowStockCount", modifier = Modifier.weight(1f))
                        StatChipCard("🔴 সোল্ড আউট", "$soldCount", modifier = Modifier.weight(1f))
                    }
                }
            }

            // --- Low Stock Monitoring Alert Banner ---
            if (lowStockCount > 0) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("low_stock_summary_alert_banner")
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
                            Text("⚠️", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "লো স্টক অ্যালার্ট ($lowStockCount টি ওষুধ)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF991B1B)
                                )
                                Text(
                                    text = "নির্ধারিত স্টক থ্রেশহোল্ডের নিচে নেমে গেছে। অবিলম্বে রি-স্টক করুন!",
                                    fontSize = 11.sp,
                                    color = Color(0xFFB91C1C)
                                )
                            }
                        }
                        Button(
                            onClick = { statusFilterTab = 3 }, // Switch to Low Stock Tab
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("view_low_stock_button")
                        ) {
                            Text("🔍 ফিল্টার", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
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

            // --- Offline CSV Inventory Stock Management & Export Card ---
            val totalBoxes = sellerOffers.sumOf { it.availableQuantity }
            val totalStockVal = sellerOffers.sumOf { it.offerPrice * it.availableQuantity }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.2.dp, Color(0xFFC7D2FE)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .testTag("csv_inventory_export_card")
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFEEF2FF),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.TableChart,
                                        contentDescription = "CSV Export",
                                        tint = RoyalPharmaBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "📊 অফলাইন ইনভেন্টরি সিএসভি এক্সপোর্ট",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Excel/Google Sheets এ অফলাইনে স্টক ও প্রাইস ট্র্যাকিং",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Button(
                            onClick = onExportCsvClick,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalPharmaBlue),
                            modifier = Modifier.testTag("btn_export_inventory_csv")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FileDownload,
                                contentDescription = "Download CSV",
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "CSV এক্সপোর্ট",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), shape = RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "মোট পণ্য: ${sellerOffers.size} টি",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "মোট স্টক: $totalBoxes বক্স",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "স্টক ভ্যালু: ৳${totalStockVal.toInt()}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen
                        )
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
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 6)
                    ) {
                        Text("🟢 Active ($activeCount)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    SegmentedButton(
                        selected = statusFilterTab == 1,
                        onClick = { statusFilterTab = 1 },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 6)
                    ) {
                        Text("⏸️ Paused ($pausedCount)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    SegmentedButton(
                        selected = statusFilterTab == 2,
                        onClick = { statusFilterTab = 2 },
                        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 6)
                    ) {
                        Text("🔴 Sold Out ($soldCount)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    SegmentedButton(
                        selected = statusFilterTab == 3,
                        onClick = { statusFilterTab = 3 },
                        shape = SegmentedButtonDefaults.itemShape(index = 3, count = 6),
                        modifier = Modifier.testTag("seller_low_stock_tab")
                    ) {
                        Text("⚠️ Low Stock ($lowStockCount)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    SegmentedButton(
                        selected = statusFilterTab == 4,
                        onClick = { statusFilterTab = 4 },
                        shape = SegmentedButtonDefaults.itemShape(index = 4, count = 6),
                        modifier = Modifier.testTag("seller_requests_tracking_tab")
                    ) {
                        Text("📦 রিকোয়েস্ট (${buyRequests.size})", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    SegmentedButton(
                        selected = statusFilterTab == 5,
                        onClick = { statusFilterTab = 5 },
                        shape = SegmentedButtonDefaults.itemShape(index = 5, count = 6),
                        modifier = Modifier.testTag("seller_order_history_tab")
                    ) {
                        Text("📜 হিস্ট্রি", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Inventory Offers, Low Stock Alert, Request Tracking, or Order History
            if (statusFilterTab == 5) {
                OrderHistoryScreen(
                    ordersList = buyRequests,
                    activeShopName = activeShop?.shopName ?: "Supplier Shop",
                    isSupplierView = true,
                    onUpdateOrderStatus = onUpdateStatus,
                    onBulkUpdateStatus = onBulkUpdateStatus,
                    onRefreshFirestoreOrders = onRefreshFirestoreOrders
                )
            } else if (statusFilterTab == 4) {
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
                    Column(modifier = Modifier.fillMaxSize()) {
                        val isAllSelected = buyRequests.isNotEmpty() && selectedRequestIds.size == buyRequests.size
                        SupplierBulkActionBar(
                            selectedCount = selectedRequestIds.size,
                            totalSelectableCount = buyRequests.size,
                            isAllSelected = isAllSelected,
                            onToggleSelectAll = {
                                selectedRequestIds = if (isAllSelected) emptySet() else buyRequests.map { it.id }.toSet()
                            },
                            onClearSelection = { selectedRequestIds = emptySet() },
                            onApplyBulkStatus = { targetStatus ->
                                onBulkUpdateStatus(selectedRequestIds.toList(), targetStatus)
                                selectedRequestIds = emptySet()
                            }
                        )

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(buyRequests, key = { it.id }) { req ->
                                val isSelected = selectedRequestIds.contains(req.id)
                                PharmacyRequestStatusTracker(
                                    request = req,
                                    onUpdateStatus = onUpdateStatus,
                                    isSupplierView = true,
                                    isSelected = isSelected,
                                    onSelectToggle = { checked ->
                                        selectedRequestIds = if (checked) selectedRequestIds + req.id else selectedRequestIds - req.id
                                    },
                                    isBulkMode = selectedRequestIds.isNotEmpty()
                                )
                            }
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
                            text = if (statusFilterTab == 3) "কোনো লো স্টক ওষুধ নেই! 🎉" else "এই স্ট্যাটাসে কোনো লিস্টিং নেই",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (statusFilterTab == 3) "আপনার সব ওষুধের মজুদ পর্যাপ্ত রয়েছে।" else "নতুন অফার বা ওষুধ পোস্ট করতে নিচের বাটনটিতে ক্লিক করুন।",
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
                            onDeleteClick = { onDeleteClick(offer) },
                            onQuickRestockClick = { addQty -> onQuickRestockClick(offer, addQty) },
                            onUpdateThresholdClick = { newThreshold -> onUpdateLowStockThreshold(offer, newThreshold) }
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
    onDeleteClick: () -> Unit,
    onQuickRestockClick: (Int) -> Unit = {},
    onUpdateThresholdClick: (Int) -> Unit = {}
) {
    val totalInitialStock = (offer.availableQuantity + offer.reservedQuantity).coerceAtLeast(1)
    val stockProgress = (offer.availableQuantity.toFloat() / totalInitialStock.toFloat()).coerceIn(0f, 1f)
    val isLowStock = offer.availableQuantity <= offer.lowStockThreshold
    var showThresholdDialog by remember { mutableStateOf(false) }

    if (showThresholdDialog) {
        var tempThresholdText by remember { mutableStateOf(offer.lowStockThreshold.toString()) }
        AlertDialog(
            onDismissRequest = { showThresholdDialog = false },
            title = {
                Text("⚠️ লো স্টক থ্রেশহোল্ড সেট করুন", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Column {
                    Text(
                        text = "${offer.medicineName} (${offer.strength})-এর ইনভেন্টরি স্টক এই সংখ্যার নিচে নামলে লোকাল নোটিফিকেশন সতর্কবার্তা পাঠানো হবে।",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = tempThresholdText,
                        onValueChange = { tempThresholdText = it },
                        label = { Text("থ্রেশহোল্ড পরিমাণ (বক্স)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newThresh = tempThresholdText.toIntOrNull() ?: 10
                        onUpdateThresholdClick(newThresh)
                        showThresholdDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalPharmaBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("সেভ করুন", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showThresholdDialog = false }) {
                    Text("বাতিল")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = if (isLowStock) BorderStroke(1.5.dp, Color(0xFFFCA5A5)) else null,
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
                        "ACTIVE" -> if (isLowStock) Color(0xFFFEF2F2) else EmeraldGreenLight
                        "PAUSED" -> Color(0xFFFEF3C7)
                        else -> Color(0xFFFEE2E2)
                    }
                ) {
                    Text(
                        text = when (offer.status) {
                            "ACTIVE" -> if (isLowStock) "⚠️ Low Stock" else "🟢 Active"
                            "PAUSED" -> "⏸️ Paused"
                            else -> "🔴 Sold Out"
                        },
                        color = when (offer.status) {
                            "ACTIVE" -> if (isLowStock) Color(0xFFDC2626) else EmeraldGreen
                            "PAUSED" -> ExpiryAmber
                            else -> Color.Red
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (isLowStock) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color(0xFFFEF2F2),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding( horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Text("⚠️", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "মজুদ সতর্কবার্তা: মাত্র ${offer.availableQuantity} বক্স বাকি (থ্রেশহোল্ড: ${offer.lowStockThreshold} Box)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B)
                            )
                        }
                        Button(
                            onClick = { onQuickRestockClick(50) },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("⚡ +50 বক্স", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
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
                    color = if (isLowStock) Color(0xFFDC2626) else RoyalPharmaBlue
                )
                Surface(
                    onClick = { showThresholdDialog = true },
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Text(
                        text = "⚙️ থ্রেশহোল্ড: ${offer.lowStockThreshold} Box",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { stockProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = if (isLowStock) Color.Red else EmeraldGreen,
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
                    text = "ব্যাচ: ${offer.batchNumber} • Expiry: ${offer.expiryDate}",
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
