package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import com.example.ui.components.SupplierBulkActionBar
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.entities.BuyRequestEntity
import com.example.ui.components.OrderShipmentStatusStepper
import com.example.ui.theme.BorderGray
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.InfoBlueBg
import com.example.ui.theme.InfoBlueBorder
import com.example.ui.theme.InfoBlueText
import com.example.ui.theme.PharmaBlueLight
import com.example.ui.theme.RoyalPharmaBlue
import com.example.ui.theme.SoftPaperGray
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrderHistoryScreen(
    ordersList: List<BuyRequestEntity>,
    activeShopName: String,
    isSupplierView: Boolean,
    onUpdateOrderStatus: (requestId: Long, newStatus: String) -> Unit,
    onRefreshFirestoreOrders: () -> Unit,
    onReorderClick: (request: BuyRequestEntity) -> Unit = {},
    onBulkUpdateStatus: (requestIds: List<Long>, newStatus: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedStatusFilter by remember { mutableStateOf("ALL") } // ALL, PENDING, ACCEPTED, COMPLETED, REJECTED
    var searchQuery by remember { mutableStateOf("") }
    var receiptDialogOrder by remember { mutableStateOf<BuyRequestEntity?>(null) }
    var selectedOrderIds by remember { mutableStateOf(setOf<Long>()) }

    // Filter Logic
    val filteredOrders = ordersList.filter { order ->
        val matchesStatus = when (selectedStatusFilter) {
            "PENDING" -> order.status.equals("PENDING", ignoreCase = true)
            "ACCEPTED" -> order.status.equals("ACCEPTED", ignoreCase = true) || order.status.equals("DISPATCHED", ignoreCase = true)
            "COMPLETED" -> order.status.equals("COMPLETED", ignoreCase = true) || order.status.equals("DELIVERED", ignoreCase = true)
            "REJECTED" -> order.status.equals("REJECTED", ignoreCase = true) || order.status.equals("CANCELLED", ignoreCase = true)
            else -> true
        }

        val matchesSearch = searchQuery.isBlank() ||
                order.medicineName.contains(searchQuery, ignoreCase = true) ||
                order.buyerShopName.contains(searchQuery, ignoreCase = true) ||
                order.sellerShopName.contains(searchQuery, ignoreCase = true) ||
                "BUY-${order.id}".contains(searchQuery, ignoreCase = true)

        matchesStatus && matchesSearch
    }.sortedByDescending { it.timestamp }

    // Calculate Summary
    val totalOrders = filteredOrders.size
    val completedOrders = ordersList.count { it.status.equals("COMPLETED", ignoreCase = true) || it.status.equals("DELIVERED", ignoreCase = true) }
    val totalVolumeBdt = ordersList.filter { it.status.equals("COMPLETED", ignoreCase = true) || it.status.equals("ACCEPTED", ignoreCase = true) || it.status.equals("DELIVERED", ignoreCase = true) }.sumOf { it.totalPrice }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SoftPaperGray)
            .testTag("order_history_screen")
    ) {
        // Top Header Banner
        Surface(
            color = RoyalPharmaBlue,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Order History",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "অর্ডার হিস্ট্রি ও ট্রানজাকশন (Order History)",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (isSupplierView) "সাপ্লায়ার অ্যাকাউন্ট: $activeShopName" else "ফার্মেসী অ্যাকাউন্ট: $activeShopName",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }

                    // Firestore Sync Refresh Button
                    IconButton(
                        onClick = onRefreshFirestoreOrders,
                        modifier = Modifier.testTag("refresh_firestore_orders_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync Cloud Firestore",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Firestore Sync Indicator Tag
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = "Cloud Synced",
                            tint = EmeraldGreenLight,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "🔥 Cloud Firestore সাথে রিয়েল-টাইম সিনক্রোনাইজড",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Summary Metric Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("মোট অর্ডার", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        text = "$totalOrders টি",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalPharmaBlue
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("সম্পন্ন লেনদেন", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        text = "$completedOrders টি",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1.2f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("মোট লেনদেন ভ্যালু", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        text = "৳${totalVolumeBdt.toInt()}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldGreen
                    )
                }
            }
        }

        // Search Bar & Filter Chips
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("ওষুধ, শপ নাম বা অর্ডার আইডি দিয়ে খুঁজুন...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = TextSecondary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("order_history_search_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = RoyalPharmaBlue,
                    unfocusedBorderColor = BorderGray
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Status Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val filters = listOf(
                    "ALL" to "সবগুলো (${ordersList.size})",
                    "PENDING" to "প্রসেসিং",
                    "ACCEPTED" to "গৃহীত/ডিসপ্যাচ",
                    "COMPLETED" to "সম্পন্ন",
                    "REJECTED" to "বাতিল"
                )

                filters.forEach { (code, label) ->
                    val isSelected = selectedStatusFilter == code
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedStatusFilter = code },
                        label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RoyalPharmaBlue,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = TextPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = BorderGray,
                            selectedBorderColor = RoyalPharmaBlue
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Supplier Bulk Order Action Bar
        if (isSupplierView) {
            val selectableOrders = filteredOrders
            val isAllSelected = selectableOrders.isNotEmpty() && selectedOrderIds.size == selectableOrders.size
            SupplierBulkActionBar(
                selectedCount = selectedOrderIds.size,
                totalSelectableCount = selectableOrders.size,
                isAllSelected = isAllSelected,
                onToggleSelectAll = {
                    selectedOrderIds = if (isAllSelected) emptySet() else selectableOrders.map { it.id }.toSet()
                },
                onClearSelection = { selectedOrderIds = emptySet() },
                onApplyBulkStatus = { targetStatus ->
                    onBulkUpdateStatus(selectedOrderIds.toList(), targetStatus)
                    selectedOrderIds = emptySet()
                }
            )
        }

        // Orders List
        if (filteredOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.ReceiptLong,
                        contentDescription = "No Orders",
                        tint = TextSecondary,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "কোনো অর্ডার ইতিহাস পাওয়া যায়নি",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ফিল্টার পরিবর্তন করুন অথবা নতুন বাই রিকোয়েস্ট পাঠালৈ এখানে জমা হবে।",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredOrders, key = { it.id }) { order ->
                    val isSelected = selectedOrderIds.contains(order.id)
                    OrderHistoryCard(
                        order = order,
                        isSupplierView = isSupplierView,
                        isSelected = isSelected,
                        onSelectToggle = if (isSupplierView) {
                            { checked ->
                                selectedOrderIds = if (checked) selectedOrderIds + order.id else selectedOrderIds - order.id
                            }
                        } else null,
                        onUpdateStatus = { newStatus -> onUpdateOrderStatus(order.id, newStatus) },
                        onViewReceipt = { receiptDialogOrder = order },
                        onReorder = { onReorderClick(order) }
                    )
                }
            }
        }
    }

    // Receipt Dialog Modal
    receiptDialogOrder?.let { req ->
        OrderReceiptModalDialog(
            order = req,
            onDismiss = { receiptDialogOrder = null },
            onShareReceipt = {
                val shareText = "📄 PharmaBazaar B2B রসিদ\n" +
                        "অর্ডার আইডি: #BUY-${req.id}\n" +
                        "ওষুধ: ${req.medicineName}\n" +
                        "পরিমাণ: ${req.requestedQuantity} বক্স\n" +
                        "মোট বিল: ৳${req.totalPrice.toInt()}\n" +
                        "ফার্মেসী: ${req.buyerShopName}\n" +
                        "সাপ্লায়ার: ${req.sellerShopName}\n" +
                        "স্ট্যাটাস: ${req.status}\n" +
                        "তারিখ: ${SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(req.timestamp))}"

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "PharmaBazaar Receipt #BUY-${req.id}")
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                context.startActivity(Intent.createChooser(shareIntent, "রসিদ শেয়ার করুন"))
            }
        )
    }
}

@Composable
fun OrderHistoryCard(
    order: BuyRequestEntity,
    isSupplierView: Boolean,
    isSelected: Boolean = false,
    onSelectToggle: ((Boolean) -> Unit)? = null,
    onUpdateStatus: (String) -> Unit,
    onViewReceipt: () -> Unit,
    onReorder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val formattedDate = dateFormat.format(Date(order.timestamp))
    var showShipmentStepper by remember { mutableStateOf(false) }

    val statusBg: Color
    val statusText: Color
    val statusLabel: String

    when (order.status.uppercase()) {
        "COMPLETED", "DELIVERED" -> {
            statusBg = EmeraldGreenLight
            statusText = EmeraldGreen
            statusLabel = "✅ সম্পন্ন (Completed)"
        }
        "ACCEPTED", "DISPATCHED" -> {
            statusBg = InfoBlueBg
            statusText = InfoBlueText
            statusLabel = "🚚 গৃহীত/ডিসপ্যাচ"
        }
        "REJECTED", "CANCELLED" -> {
            statusBg = Color(0xFFFEE2E2)
            statusText = Color(0xFFDC2626)
            statusLabel = "❌ বাতিল/প্রত্যাখ্যাত"
        }
        else -> { // PENDING
            statusBg = Color(0xFFFEF3C7)
            statusText = Color(0xFFD97706)
            statusLabel = "⏳ পেন্ডিং/প্রসেসিং"
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("order_history_item_${order.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFEFF6FF) else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) RoyalPharmaBlue else Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            // Header Row: Checkbox, Order ID, Date & Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onSelectToggle != null) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onSelectToggle(it) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = RoyalPharmaBlue
                            ),
                            modifier = Modifier.testTag("order_history_checkbox_${order.id}")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "#BUY-${order.id}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = RoyalPharmaBlue
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFF1F5F9)
                            ) {
                                Text(
                                    text = "🔥 Firestore Synced",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF475569),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = formattedDate,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusBg
                ) {
                    Text(
                        text = statusLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Medicine Name & Details
            Text(
                text = order.medicineName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "পরিমাণ: ${order.requestedQuantity} বক্স × ৳${order.unitPrice.toInt()}",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Text(
                    text = "মোট: ৳${order.totalPrice.toInt()}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = EmeraldGreen
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Buyer / Seller Shop Info
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Store,
                                contentDescription = "Shop",
                                tint = RoyalPharmaBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSupplierView) "ফার্মেসী: ${order.buyerShopName}" else "সাপ্লায়ার: ${order.sellerShopName}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        val phone = if (isSupplierView) order.buyerPhone else order.sellerPhone
                        if (phone.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Phone",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = phone,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            if (order.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "নোট: ${order.note}",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Shipment Tracking Stepper Toggle Button
            Button(
                onClick = { showShipmentStepper = !showShipmentStepper },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (showShipmentStepper) RoyalPharmaBlue else PharmaBlueLight,
                    contentColor = if (showShipmentStepper) Color.White else RoyalPharmaBlue
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_toggle_shipment_stepper_${order.id}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = "Shipment Tracking",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (showShipmentStepper) "শিপমেন্ট ট্র্যাকার বন্ধ করুন" else "🚚 শিপমেন্ট প্রোগ্রেস ট্র্যাকার দেখুন (Confirmed ➔ Packed ➔ Dispatched...)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            AnimatedVisibility(visible = showShipmentStepper) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    OrderShipmentStatusStepper(
                        currentStatus = order.status,
                        onUpdateStatus = { newStatus -> onUpdateStatus(newStatus) },
                        isSupplierView = isSupplierView
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onViewReceipt,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_view_receipt_${order.id}"),
                    border = BorderStroke(1.dp, RoyalPharmaBlue)
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = "Receipt",
                        tint = RoyalPharmaBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "রসিদ দেখুন",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalPharmaBlue
                    )
                }

                if (isSupplierView) {
                    if (order.status.equals("PENDING", ignoreCase = true)) {
                        Button(
                            onClick = { onUpdateStatus("ACCEPTED") },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalPharmaBlue),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_accept_order_${order.id}")
                        ) {
                            Text("একসেপ্ট করুন", fontSize = 11.sp, color = Color.White)
                        }
                    } else if (order.status.equals("ACCEPTED", ignoreCase = true) || order.status.equals("DISPATCHED", ignoreCase = true)) {
                        Button(
                            onClick = { onUpdateStatus("COMPLETED") },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("btn_complete_order_${order.id}")
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Complete", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("মার্কেট কমপ্লিট", fontSize = 11.sp, color = Color.White)
                        }
                    }
                } else {
                    Button(
                        onClick = onReorder,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_reorder_${order.id}")
                    ) {
                        Text("পুনরায় অর্ডার", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun OrderReceiptModalDialog(
    order: BuyRequestEntity,
    onDismiss: () -> Unit,
    onShareReceipt: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault()) }
    val formattedDate = dateFormat.format(Date(order.timestamp))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = "Receipt",
                        tint = RoyalPharmaBlue,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ডিজিটাল B2B ইনভয়েস",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = EmeraldGreenLight
                ) {
                    Text(
                        text = "🔥 Verified",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                HorizontalDivider(color = Color(0xFFE2E8F0))

                Spacer(modifier = Modifier.height(10.dp))

                // Invoice Header Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("ইনভয়েস নং: #BUY-${order.id}", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = RoyalPharmaBlue)
                        Text("তারিখ: $formattedDate", fontSize = 11.sp, color = TextSecondary)
                    }
                    Text(
                        text = order.status.uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (order.status.equals("COMPLETED", true)) EmeraldGreen else RoyalPharmaBlue
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Parties Info Box
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("ক্রেতা (ফার্মেসী):", fontSize = 11.sp, color = TextSecondary)
                            Text(order.buyerShopName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("ফোন: ${order.buyerPhone}", fontSize = 11.sp, color = TextSecondary)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("বিক্রেতা (সাপ্লায়ার):", fontSize = 11.sp, color = TextSecondary)
                            Text(order.sellerShopName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("ফোন: ${order.sellerPhone}", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Item Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFEEF2FF))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("ওষুধের বিবরণ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalPharmaBlue, modifier = Modifier.weight(2f))
                    Text("পরিমাণ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalPharmaBlue, modifier = Modifier.weight(1f))
                    Text("একক মূল্য", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalPharmaBlue, modifier = Modifier.weight(1f))
                    Text("মোট", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalPharmaBlue, modifier = Modifier.weight(1f))
                }

                // Item Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(order.medicineName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(2f))
                    Text("${order.requestedQuantity} Box", fontSize = 11.sp, modifier = Modifier.weight(1f))
                    Text("৳${order.unitPrice.toInt()}", fontSize = 11.sp, modifier = Modifier.weight(1f))
                    Text("৳${order.totalPrice.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen, modifier = Modifier.weight(1f))
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                Spacer(modifier = Modifier.height(10.dp))

                // Bill Breakdown Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("সর্বমোট প্রদেয় বিল:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("৳${order.totalPrice.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = EmeraldGreen)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Visual Progress Stepper inside Receipt Modal
                OrderShipmentStatusStepper(
                    currentStatus = order.status
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "বি.দ্র: এটি PharmaBazaar প্ল্যাটফর্মের ডিজিটাল ক্লাউড সমর্থিত প্রমাণীকৃত রসিদ।",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onShareReceipt,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalPharmaBlue),
                modifier = Modifier.testTag("btn_share_receipt_dialog")
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("শেয়ার / ডাউনলোড", fontSize = 12.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বন্ধ করুন", color = TextSecondary)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}
