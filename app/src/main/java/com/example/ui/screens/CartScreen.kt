package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.RemoveShoppingCart
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.entities.BuyRequestEntity
import com.example.data.db.entities.CartItemEntity
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoyalPharmaBlue
import com.example.ui.theme.SoftPaperGray
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CartScreen(
    cartItems: List<CartItemEntity>,
    totalPrice: Double,
    buyRequests: List<BuyRequestEntity> = emptyList(),
    activeShopName: String = "My Pharmacy",
    onUpdateQuantity: (cartItemId: Long, newQuantity: Int) -> Unit,
    onDeleteItem: (cartItemId: Long) -> Unit,
    onCheckout: (note: String) -> Unit,
    onUpdateOrderStatus: (requestId: Long, newStatus: String) -> Unit = { _, _ -> },
    onRefreshFirestoreOrders: () -> Unit = {},
    onReorderClick: (request: BuyRequestEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedScreenTab by remember { mutableStateOf(0) } // 0 = Active Cart, 1 = Order History
    var checkoutNote by remember { mutableStateOf("") }

    // Group cart items by seller
    val groupedBySeller = cartItems.groupBy { it.sellerShopId }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SoftPaperGray)
    ) {
        // Top Header & View Tab Selector
        Surface(
            color = RoyalPharmaBlue,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (selectedScreenTab == 0) "মাল্টি-ভেন্ডর কার্ট (Multi-Vendor Cart)" else "ফার্মেসী অর্ডার হিস্ট্রি (Order History)",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (selectedScreenTab == 0) "দোকান অনুযায়ী আলাদা আলাদা বাই রিকোয়েস্ট তৈরি হবে" else "ক্লাউড ফায়ারস্টোর সংরক্ষিত অতীতের সব লেনদেন",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = if (selectedScreenTab == 0) "${cartItems.size} টি কার্ট আইটেম" else "${buyRequests.size} টি অর্ডার",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Top Tab Strip: Cart vs Order History
                TabRow(
                    selectedTabIndex = selectedScreenTab,
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedScreenTab]),
                            color = Color.White,
                            height = 3.dp
                        )
                    }
                ) {
                    Tab(
                        selected = selectedScreenTab == 0,
                        onClick = { selectedScreenTab = 0 },
                        modifier = Modifier.testTag("cart_tab_active_cart")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 10.dp)
                        ) {
                            Icon(imageVector = Icons.Outlined.ShoppingCart, contentDescription = "Cart", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("সক্রিয় কার্ট", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Tab(
                        selected = selectedScreenTab == 1,
                        onClick = { selectedScreenTab = 1 },
                        modifier = Modifier.testTag("cart_tab_order_history")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 10.dp)
                        ) {
                            Icon(imageVector = Icons.Outlined.History, contentDescription = "History", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("অর্ডার হিস্ট্রি (${buyRequests.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (selectedScreenTab == 1) {
            // Render Order History Component for Pharmacists
            OrderHistoryScreen(
                ordersList = buyRequests,
                activeShopName = activeShopName,
                isSupplierView = false,
                onUpdateOrderStatus = onUpdateOrderStatus,
                onRefreshFirestoreOrders = onRefreshFirestoreOrders,
                onReorderClick = onReorderClick
            )
        } else {
            // Render Active Cart Content
            if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.RemoveShoppingCart,
                        contentDescription = "Empty Cart",
                        tint = TextSecondary,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "আপনার কার্ট এখন খালি!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "হোম ফিড থেকে পছন্দের ওষুধ নির্বাচন করে Buy Request বা Add to Cart করুন।",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Grouped Sellers Sections
                groupedBySeller.forEach { (sellerId, sellerItems) ->
                    item(key = "seller_$sellerId") {
                        val sellerName = sellerItems.first().sellerShopName
                        val sellerLocation = sellerItems.first().sellerLocation
                        val sellerSubtotal = sellerItems.sumOf { it.offerPrice * it.requestedQuantity }

                        Card(
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("cart_vendor_card_$sellerId")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                // Seller Header
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Storefront,
                                        contentDescription = "Seller",
                                        tint = RoyalPharmaBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "দোকান: $sellerName ($sellerLocation)",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    color = Color(0xFFF1F5F9)
                                )

                                // Seller Items List
                                sellerItems.forEach { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${item.medicineName} ${item.strength}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "₹${item.offerPrice.toInt()}/বক্স (MOQ: ${item.minimumOrderQuantity} Box)",
                                                fontSize = 12.sp,
                                                color = TextSecondary
                                            )
                                            Text(
                                                text = "সাবটোটাল: ₹${(item.offerPrice * item.requestedQuantity).toInt()}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = EmeraldGreen
                                            )
                                        }

                                        // Quantity Controls
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = { onUpdateQuantity(item.id, item.requestedQuantity - 1) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Remove,
                                                    contentDescription = "Decrease",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            Text(
                                                text = "${item.requestedQuantity}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp)
                                            )

                                            IconButton(
                                                onClick = { onUpdateQuantity(item.id, item.requestedQuantity + 1) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Add,
                                                    contentDescription = "Increase",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = { onDeleteItem(item.id) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Delete,
                                                    contentDescription = "Delete",
                                                    tint = Color.Red,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    color = Color(0xFFF1F5F9)
                                )

                                // Seller Total
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$sellerName এর মোট বিল:",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "₹${sellerSubtotal.toInt()}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldGreen
                                    )
                                }
                            }
                        }
                    }
                }

                item(key = "checkout_notes_section") {
                    Card(
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "অর্ডার সংক্রান্ত তথ্য ও নোট:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = checkoutNote,
                                onValueChange = { checkoutNote = it },
                                placeholder = { Text("যেমন: দোকান পিকআপ সময় বা ডেলিভারি ইনস্ট্রাকশন...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("checkout_note_input"),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }

            // Bottom Fixed Checkout Summary Bar
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("সর্বমোট প্রাক্কলিত বিল:", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                text = "₹${totalPrice.toInt()}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen
                            )
                        }

                        Button(
                            onClick = { onCheckout(checkoutNote) },
                            modifier = Modifier
                                .height(46.dp)
                                .testTag("place_all_orders_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalPharmaBlue)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Send,
                                contentDescription = "Submit",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "সব বাই রিকোয়েস্ট পাঠান (Place All)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
}
