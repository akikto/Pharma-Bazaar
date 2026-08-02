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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Compare
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material.icons.outlined.TrendingFlat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.data.db.entities.OfferListingEntity
import com.example.data.db.entities.PriceThresholdAlertEntity
import com.example.data.db.entities.TriggeredPriceAlertEntity
import com.example.data.db.entities.WatchlistItemEntity
import com.example.ui.components.PriceThresholdAlertSection
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
import com.example.ui.theme.UrgentRedLight

@Composable
fun WatchlistScreen(
    watchlistItems: List<WatchlistItemEntity>,
    allOffers: List<OfferListingEntity>,
    onRemoveWatchlist: (String) -> Unit,
    onCompareClick: (String) -> Unit,
    onBackClick: () -> Unit,
    thresholdAlerts: List<PriceThresholdAlertEntity> = emptyList(),
    triggeredAlerts: List<TriggeredPriceAlertEntity> = emptyList(),
    onAddThreshold: (medicineName: String, genericName: String, maxPrice: Double) -> Unit = { _, _, _ -> },
    onToggleThreshold: (id: Long, isEnabled: Boolean) -> Unit = { _, _ -> },
    onDeleteThreshold: (id: Long) -> Unit = { _ -> },
    onDismissTriggeredAlert: (id: Long) -> Unit = { _ -> },
    onAddToCart: (OfferListingEntity, Int) -> Unit = { _, _ -> },
    onSimulateOffer: (medicineName: String, price: Double, sellerName: String) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SoftPaperGray)
    ) {
        // Top Header Bar
        Surface(
            color = SoftPaperGray,
            modifier = Modifier.fillMaxWidth()
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("watchlist_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                    Text(
                        text = "ওয়াচলিস্ট ও প্রাইজ ট্র্যাকার",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFCE7F3),
                    border = BorderStroke(1.dp, Color(0xFFF472B6))
                ) {
                    Text(
                        text = "${watchlistItems.size} টি বইমার্ক",
                        color = UrgentRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Sub-Tab Switcher (Bookmarks vs Price Threshold Alerts)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text("❤️ ওয়াচলিস্ট (${watchlistItems.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                SegmentedButton(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    modifier = Modifier.testTag("price_threshold_alerts_tab")
                ) {
                    Text(
                        text = if (triggeredAlerts.isNotEmpty()) "⚡ দাম অ্যালার্ট (${triggeredAlerts.size})" else "🎯 প্রাইজ সীমা (${thresholdAlerts.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (triggeredAlerts.isNotEmpty() && selectedTab != 1) UrgentRed else Color.Unspecified
                    )
                }
            }
        }

        if (selectedTab == 1) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                item {
                    PriceThresholdAlertSection(
                        thresholdAlerts = thresholdAlerts,
                        triggeredAlerts = triggeredAlerts,
                        allOffers = allOffers,
                        onAddThreshold = onAddThreshold,
                        onToggleThreshold = onToggleThreshold,
                        onDeleteThreshold = onDeleteThreshold,
                        onDismissTriggeredAlert = onDismissTriggeredAlert,
                        onAddToCart = onAddToCart,
                        onSimulateOffer = onSimulateOffer
                    )
                }
            }
        } else {
            // Info Banner Notice
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = InfoBlueBg,
                border = BorderStroke(1.dp, InfoBlueBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
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
                        text = "আপনার বুকমার্ক করা ওষুধগুলোর লাইভ দামের তারতম্য এবং বিভিন্ন বিক্রেতার অফার একসাথে ট্র্যাকিং করুন।",
                        fontSize = 12.sp,
                        color = InfoBlueText,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

        if (watchlistItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = UrgentRedLight,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.FavoriteBorder,
                                contentDescription = "Empty Watchlist",
                                tint = UrgentRed,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "ওয়াচলিস্টে কোনো ওষুধ নেই",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "পছন্দের ওষুধের হার্ট (❤️) আইকনে ট্যাপ করে বুকমার্ক করুন এবং প্রাইজ ফ্লাকচুয়েশন ট্র্যাক করুন।",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onBackClick,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalPharmaBlue)
                    ) {
                        Text(
                            text = "ওষুধ ব্রাউজ করুন",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(watchlistItems, key = { it.id }) { item ->
                    // Find matching offers for this medicine name
                    val matchingOffers = allOffers.filter { offer ->
                        offer.medicineName.contains(item.medicineName, ignoreCase = true) ||
                                item.medicineName.contains(offer.medicineName, ignoreCase = true)
                    }

                    WatchlistItemCard(
                        item = item,
                        offers = matchingOffers,
                        onRemove = { onRemoveWatchlist(item.medicineName) },
                        onCompareClick = {
                            val targetName = matchingOffers.firstOrNull()?.let { "${it.medicineName} ${it.strength}" } ?: item.medicineName
                            onCompareClick(targetName)
                        }
                    )
                }
            }
        }
    }
}
}

@Composable
fun WatchlistItemCard(
    item: WatchlistItemEntity,
    offers: List<OfferListingEntity>,
    onRemove: () -> Unit,
    onCompareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sellerCount = offers.size
    val minOfferPrice = if (offers.isNotEmpty()) offers.minOf { it.offerPrice } else 0.0
    val maxOfferPrice = if (offers.isNotEmpty()) offers.maxOf { it.offerPrice } else 0.0
    val minMrp = if (offers.isNotEmpty()) offers.minOf { it.mrp } else 0.0
    val maxDiscount = if (offers.isNotEmpty()) offers.maxOf { it.discountPercent } else 0

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CardBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("watchlist_item_card_${item.medicineName}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Name + Form Tag + Remove Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = item.medicineName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (item.form.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SoftPaperGray,
                                border = BorderStroke(1.dp, BorderGray)
                            ) {
                                Text(
                                    text = item.form,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    if (item.genericName.isNotBlank() || item.companyName.isNotBlank()) {
                        Text(
                            text = "${item.genericName} • ${item.companyName}".trim(' ', '•'),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("remove_watchlist_${item.medicineName}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Remove from Watchlist",
                        tint = UrgentRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Price Fluctuation & Market Insight Box
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = SoftPaperGray,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (maxDiscount >= 40) Icons.Outlined.TrendingDown else Icons.Outlined.TrendingFlat,
                                contentDescription = "Trend",
                                tint = if (maxDiscount >= 40) EmeraldGreen else RoyalPharmaBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (maxDiscount >= 40) "📉 সর্বোচ্চ ${maxDiscount}% ছাড়ে মিলছে" else "📊 মার্কেট প্রাইস স্থিতিশীল",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (maxDiscount >= 40) EmeraldGreen else RoyalPharmaBlue
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PharmaBlueLight
                        ) {
                            Text(
                                text = "$sellerCount টি দোকানে স্টক আছে",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoyalPharmaBlue,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "সর্বনিম্ন অফার মূল্য",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (minOfferPrice > 0) "৳${minOfferPrice.toInt()}/বক্স" else "উপলব্ধ নয়",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreen
                                )
                                if (minMrp > minOfferPrice && minOfferPrice > 0) {
                                    Text(
                                        text = "MRP ৳${minMrp.toInt()}",
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                    )
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "দামের ব্যাপ্তি (Range)",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (minOfferPrice > 0) "৳${minOfferPrice.toInt()} — ৳${maxOfferPrice.toInt()}" else "-",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Compare Offers Action Button
            Button(
                onClick = onCompareClick,
                enabled = sellerCount > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .testTag("compare_watchlist_button_${item.medicineName}"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalPharmaBlue)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Compare,
                    contentDescription = "Compare",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (sellerCount > 0) "সব বিক্রেতার অফার তুলনা করুন ($sellerCount)" else "বর্তমানে কোনো বিক্রেতার স্টক নেই",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
