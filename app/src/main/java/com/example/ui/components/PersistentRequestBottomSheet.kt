package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.entities.BuyRequestEntity
import com.example.data.db.entities.CartItemEntity
import com.example.ui.theme.BorderGray
import com.example.ui.theme.CardBorder
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
import com.example.ui.theme.UrgentRed

@Composable
fun PersistentRequestBottomSheet(
    cartItems: List<CartItemEntity>,
    cartTotalPrice: Double,
    buyRequests: List<BuyRequestEntity>,
    onUpdateQuantity: (cartItemId: Long, newQuantity: Int) -> Unit,
    onDeleteItem: (cartItemId: Long) -> Unit,
    onCheckoutCart: (note: String) -> Unit,
    onOpenChatForRequest: (BuyRequestEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val pendingRequests = remember(buyRequests) {
        buyRequests.filter { it.status == "PENDING" || it.status == "ACCEPTED" }
    }

    // Do not show bottom bar if no items in cart and no pending requests
    if (cartItems.isEmpty() && pendingRequests.isEmpty()) return

    var isExpanded by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var batchNote by remember { mutableStateOf("") }
    var sheetQuery by remember { mutableStateOf("") }
    var sheetCategory by remember { mutableStateOf("ALL") }

    val filteredCartItems = remember(cartItems, sheetQuery, sheetCategory) {
        cartItems.filter { item ->
            val matchesQuery = sheetQuery.isBlank() ||
                    item.medicineName.contains(sheetQuery, ignoreCase = true) ||
                    item.genericName.contains(sheetQuery, ignoreCase = true) ||
                    item.sellerShopName.contains(sheetQuery, ignoreCase = true)
            matchesQuery
        }
    }

    val totalItemsCount = cartItems.sumOf { it.requestedQuantity }

    Surface(
        shape = if (isExpanded) RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp) else RoundedCornerShape(20.dp),
        color = Color.White,
        tonalElevation = 8.dp,
        shadowElevation = 12.dp,
        border = BorderStroke(1.dp, CardBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("persistent_request_bottom_sheet_bar")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Drag handle / Persistent Summary Header
            Surface(
                color = RoyalPharmaBlue,
                shape = if (isExpanded) RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp) else RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .testTag("expand_request_bottom_sheet_button")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        BadgedBox(
                            badge = {
                                if (cartItems.isNotEmpty()) {
                                    Badge(containerColor = EmeraldGreen) {
                                        Text("$totalItemsCount", color = Color.White, fontSize = 11.sp)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ShoppingCart,
                                contentDescription = "Request Cart",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = if (cartItems.isNotEmpty()) "অনুরোধে ${cartItems.size}টি পদের মাল্টি-ভেন্ডর আইটেম"
                                else "${pendingRequests.size}টি ব্যাচ রিকোয়েস্ট প্রসেসিং",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (cartItems.isNotEmpty()) {
                                    Text(
                                        text = "আনুমানিক মোট: TK ${cartTotalPrice.toInt()}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                                if (pendingRequests.isNotEmpty()) {
                                    Text(
                                        text = "• ${pendingRequests.size}টি অপেক্ষমাণ",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFFDE047)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!isExpanded && cartItems.isNotEmpty()) {
                            Button(
                                onClick = { onCheckoutCart(batchNote) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("submit_batch_request_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Send,
                                    contentDescription = "Submit",
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "পাঠান",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        IconButton(
                            onClick = { isExpanded = !isExpanded },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Expanded Content Panel
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.65f)
                        .background(SoftPaperGray)
                ) {
                    // Tabs Header
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.White
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                Text(
                                    text = "অনুরোধ তালিকা (${cartItems.size})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                Text(
                                    text = "পেন্ডিং ব্যাচ রিকোয়েস্ট (${pendingRequests.size})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        )
                    }

                    if (selectedTab == 0) {
                        // Cart Items Section
                        if (cartItems.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "অনুরোধ করার মতো কোনো পণ্য কার্টে নেই",
                                    color = TextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                PharmacyRequestSearchBar(
                                    searchQuery = sheetQuery,
                                    onSearchQueryChange = { sheetQuery = it },
                                    selectedCategory = sheetCategory,
                                    onCategorySelected = { sheetCategory = it },
                                    totalResultCount = filteredCartItems.size,
                                    placeholderText = "অনুরোধ তালিকায় ওষুধ খুঁজুন..."
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(filteredCartItems, key = { it.id }) { item ->
                                        CartItemSheetRow(
                                            item = item,
                                            onUpdateQuantity = { qty -> onUpdateQuantity(item.id, qty) },
                                            onDelete = { onDeleteItem(item.id) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = batchNote,
                                    onValueChange = { batchNote = it },
                                    placeholder = { Text("বিক্রেতাদের জন্য কোনো নোট (ঐচ্ছিক)...", fontSize = 12.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    )
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        onCheckoutCart(batchNote)
                                        isExpanded = false
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .testTag("submit_all_batch_requests_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Send,
                                        contentDescription = "Send",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "সব ভেন্ডরকে ব্যাচ রিকোয়েস্ট পাঠান (TK ${cartTotalPrice.toInt()})",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    } else {
                        // Pending Batch Requests Section
                        if (pendingRequests.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "বর্তমানে কোনো পেন্ডিং বাই রিকোয়েস্ট নেই",
                                    color = TextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(pendingRequests, key = { it.id }) { req ->
                                    PendingRequestSheetCard(
                                        request = req,
                                        onOpenChat = {
                                            onOpenChatForRequest(req)
                                            isExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CartItemSheetRow(
    item: CartItemEntity,
    onUpdateQuantity: (Int) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cart_item_row_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.medicineName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "🏪 ${item.sellerShopName}",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "TK ${item.offerPrice.toInt()} × ${item.requestedQuantity} = TK ${(item.offerPrice * item.requestedQuantity).toInt()}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldGreen
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SoftPaperGray,
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(2.dp)
                    ) {
                        IconButton(
                            onClick = {
                                if (item.requestedQuantity > item.minimumOrderQuantity) {
                                    onUpdateQuantity(item.requestedQuantity - 1)
                                }
                            },
                            enabled = item.requestedQuantity > item.minimumOrderQuantity,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Remove,
                                contentDescription = "Decrease",
                                modifier = Modifier.size(12.dp)
                            )
                        }

                        Text(
                            text = "${item.requestedQuantity}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )

                        IconButton(
                            onClick = {
                                if (item.requestedQuantity < item.maxAvailableQuantity) {
                                    onUpdateQuantity(item.requestedQuantity + 1)
                                }
                            },
                            enabled = item.requestedQuantity < item.maxAvailableQuantity,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = "Increase",
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        tint = UrgentRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PendingRequestSheetCard(
    request: BuyRequestEntity,
    onOpenChat: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pending_request_card_${request.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = request.medicineName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (request.status == "ACCEPTED") EmeraldGreenLight else InfoBlueBg,
                    border = BorderStroke(1.dp, if (request.status == "ACCEPTED") Color(0xFFA7F3D0) else InfoBlueBorder)
                ) {
                    Text(
                        text = if (request.status == "ACCEPTED") "✅ গৃহীত" else "⏳ অপেক্ষমাণ",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (request.status == "ACCEPTED") EmeraldGreen else InfoBlueText,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "বিক্রেতা: ${request.sellerShopName}",
                fontSize = 11.sp,
                color = TextSecondary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "পরিমাণ: ${request.requestedQuantity} বক্স • মোট: TK ${request.totalPrice.toInt()}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                IconButton(
                    onClick = onOpenChat,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Chat,
                        contentDescription = "Chat",
                        tint = RoyalPharmaBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
