package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.entities.OfferListingEntity
import com.example.ui.theme.CardBorder
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.ExpiryAmber
import com.example.ui.theme.ExpiryAmberLight
import com.example.ui.theme.PharmaBlueLight
import com.example.ui.theme.RoyalPharmaBlue
import com.example.ui.theme.SoftPaperGray
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UrgentRed
import com.example.ui.theme.UrgentRedLight

enum class PriceSortOption(val labelBn: String, val icon: @Composable () -> Unit) {
    LOWEST_PRICE("কম মূল্য", { Icon(Icons.Outlined.TrendingDown, contentDescription = null, modifier = Modifier.size(14.dp)) }),
    HIGHEST_DISCOUNT("বেশি ছাড়", { Icon(Icons.Outlined.EmojiEvents, contentDescription = null, modifier = Modifier.size(14.dp)) }),
    LONGEST_EXPIRY("মেয়াদ বেশি", { Icon(Icons.Outlined.Timer, contentDescription = null, modifier = Modifier.size(14.dp)) }),
    NEAREST("নিকটস্থ", { Icon(Icons.Outlined.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp)) })
}

/**
 * Reusable Price Comparison Component that displays a list of seller offers
 * for a medicine in a sortable, card-based list with the best deal highlighted.
 */
@Composable
fun PriceComparisonComponent(
    medicineFullName: String,
    offers: List<OfferListingEntity>,
    onBuyRequestClick: (OfferListingEntity) -> Unit,
    onChatClick: ((OfferListingEntity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedSort by remember { mutableStateOf(PriceSortOption.LOWEST_PRICE) }
    var showPriceTrendVisualizer by remember { mutableStateOf(false) }

    if (offers.isEmpty()) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.Storefront,
                    contentDescription = "No offers",
                    tint = TextSecondary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "বর্তমানে এই ওষুধের কোন সক্রিয় অফার পাওয়া যায়নি",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "নতুন স্টক যুক্ত হলে এখানে বিক্রেতাদের তালিকা দেখতে পাবেন",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
        return
    }

    // Determine Best Deal Offer (lowest offer price; tie break with highest discount)
    val bestDealOffer = remember(offers) {
        offers.minWithOrNull(compareBy<OfferListingEntity> { it.offerPrice }.thenByDescending { it.discountPercent })
    }

    // Sort offers based on selected sort option
    val sortedOffers = remember(offers, selectedSort) {
        when (selectedSort) {
            PriceSortOption.LOWEST_PRICE -> offers.sortedBy { it.offerPrice }
            PriceSortOption.HIGHEST_DISCOUNT -> offers.sortedByDescending { it.discountPercent }
            PriceSortOption.LONGEST_EXPIRY -> offers.sortedByDescending { it.daysUntilExpiry }
            PriceSortOption.NEAREST -> offers.sortedBy { it.sellerDistanceKm }
        }
    }

    val minPrice = offers.minOf { it.offerPrice }.toInt()
    val maxPrice = offers.maxOf { it.offerPrice }.toInt()
    val avgPrice = offers.map { it.offerPrice }.average().toInt()
    val mrpPrice = offers.firstOrNull()?.mrp?.toInt() ?: 0
    val maxSavings = if (mrpPrice > minPrice) mrpPrice - minPrice else 0

    Column(modifier = modifier.fillMaxWidth()) {
        // --- Comparison Header Card ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .testTag("price_comparison_header"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, CardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header Title & Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "📊 মূল্য তুলনা (Price Comparison)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalPharmaBlue
                        )
                        Text(
                            text = "$medicineFullName • ${offers.size} টি দোকান বিক্রি করছে",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (maxSavings > 0) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = EmeraldGreenLight,
                            border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "সর্বোচ্চ ৳$maxSavings সাশ্রয়",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreen
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stats Row: Lowest Price | Avg Price | Price Range
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SoftPaperGray, RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "সর্বনিম্ন মূল্য", fontSize = 10.sp, color = TextSecondary)
                        Text(text = "৳$minPrice", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(28.dp)
                            .background(CardBorder)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "গড় মূল্য", fontSize = 10.sp, color = TextSecondary)
                        Text(text = "৳$avgPrice", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = RoyalPharmaBlue)
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(28.dp)
                            .background(CardBorder)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "সর্বোচ্চ মূল্য", fontSize = 10.sp, color = TextSecondary)
                        Text(text = "৳$maxPrice", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sort Options
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FilterList,
                        contentDescription = "Sort",
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "সাজানোর ধরণ (Sort By):", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                }

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    PriceSortOption.entries.forEachIndexed { index, option ->
                        SegmentedButton(
                            selected = selectedSort == option,
                            onClick = { selectedSort = option },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = PriceSortOption.entries.size
                            ),
                            icon = { option.icon() }
                        ) {
                            Text(
                                text = option.labelBn,
                                fontSize = 10.sp,
                                fontWeight = if (selectedSort == option) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Toggle Button for Historical Price Trend Chart
                Button(
                    onClick = { showPriceTrendVisualizer = !showPriceTrendVisualizer },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showPriceTrendVisualizer) RoyalPharmaBlue else PharmaBlueLight,
                        contentColor = if (showPriceTrendVisualizer) Color.White else RoyalPharmaBlue
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("toggle_price_trend_chart_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(if (showPriceTrendVisualizer) "📊" else "📈", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (showPriceTrendVisualizer) "মূল্য ট্রেন্ড অ্যানালিটিক্স লুকান" else "📊 ঐতিহাসিক মূল্য ট্রেন্ড ও ক্রয় বিশ্লেষণ চার্ট দেখুন",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                AnimatedVisibility(visible = showPriceTrendVisualizer) {
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))
                        MedicinePriceTrendVisualizer(
                            medicineName = medicineFullName,
                            mrp = mrpPrice.toDouble(),
                            currentOfferPrice = minPrice.toDouble()
                        )
                    }
                }
            }
        }

        // --- Card-Based Offers List ---
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(sortedOffers, key = { it.id }) { offer ->
                val isBestDeal = offer.id == bestDealOffer?.id
                PriceComparisonOfferCard(
                    offer = offer,
                    isBestDeal = isBestDeal,
                    onBuyRequestClick = { onBuyRequestClick(offer) },
                    onChatClick = if (onChatClick != null) { { onChatClick(offer) } } else null
                )
            }
        }
    }
}

/**
 * Individual Card representing a single seller offer in the price comparison list.
 * Highlights "BEST DEAL" with a prominent badge, gradient/gold border, and rich contact actions.
 */
@Composable
fun PriceComparisonOfferCard(
    offer: OfferListingEntity,
    isBestDeal: Boolean,
    onBuyRequestClick: () -> Unit,
    onChatClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val phoneNum = "01711223344"

    val (expiryColor, expiryBg, expiryText) = when {
        offer.daysUntilExpiry <= 30 -> Triple(
            UrgentRed,
            UrgentRedLight,
            "মেয়াদ: ${offer.daysUntilExpiry} দিন باقی"
        )
        offer.daysUntilExpiry <= 60 -> Triple(
            ExpiryAmber,
            ExpiryAmberLight,
            "মেয়াদ: ${offer.daysUntilExpiry} দিন باقی"
        )
        else -> Triple(
            EmeraldGreen,
            EmeraldGreenLight,
            "মেয়াদ: ${offer.expiryDate}"
        )
    }

    val cardBg = if (isBestDeal) Color(0xFFF0FDF4) else Color.White
    val borderStroke = if (isBestDeal) BorderStroke(2.dp, EmeraldGreen) else BorderStroke(1.dp, CardBorder)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("price_comparison_offer_card_${offer.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = borderStroke,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isBestDeal) 4.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Best Deal Badge or Seller Verified Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isBestDeal) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = EmeraldGreen
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.EmojiEvents,
                                contentDescription = "Best Deal",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "🏆 সেরা ডিল (BEST DEAL)",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PharmaBlueLight
                    ) {
                        Text(
                            text = "যাচাইকৃত ফার্মেসী",
                            color = RoyalPharmaBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                // Discount Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EmeraldGreenLight
                ) {
                    Text(
                        text = "${offer.discountPercent}% ছাড়",
                        color = EmeraldGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Seller Shop Details & Rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Storefront,
                        contentDescription = "Shop",
                        tint = RoyalPharmaBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = offer.sellerShopName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = "Verified",
                                tint = EmeraldGreen,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = "Location",
                                tint = TextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${offer.sellerLocation} • ${offer.sellerDistanceKm} km",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Rating",
                        tint = ExpiryAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "4.9",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = CardBorder)

            // Price & Expiry Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "অফার মূল্য:", fontSize = 10.sp, color = TextSecondary)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "৳${offer.offerPrice.toInt()}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen
                        )
                        Text(
                            text = "/বক্স",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "৳${offer.mrp.toInt()}",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textDecoration = TextDecoration.LineThrough,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                    Text(
                        text = "স্টক: ${offer.availableQuantity} Box (MOQ: ${offer.minimumOrderQuantity})",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Expiry Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = expiryBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Timer,
                            contentDescription = "Expiry",
                            tint = expiryColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = expiryText,
                            color = expiryColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Row: In-App Chat, Call, WhatsApp, Buy Request
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // In-app Chat
                Button(
                    onClick = { onChatClick?.invoke() },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SoftPaperGray,
                        contentColor = RoyalPharmaBlue
                    ),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("compare_chat_${offer.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Chat",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(text = "চ্যাট", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Call
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNum"))
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldGreenLight,
                        contentColor = EmeraldGreen
                    ),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("compare_call_${offer.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Call,
                        contentDescription = "Call",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(text = "কল", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // WhatsApp
                Button(
                    onClick = {
                        val cleanPhone = phoneNum.replace("+", "").replace("-", "").replace(" ", "")
                        val formattedPhone = if (cleanPhone.startsWith("0")) "88$cleanPhone" else cleanPhone
                        val waUrl = "https://wa.me/$formattedPhone?text=${Uri.encode("হাই, আমি ${offer.medicineName} (${offer.strength}) অফার সম্পর্কে জানতে চাই।")}"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl))
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF25D366),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    modifier = Modifier
                        .weight(1.1f)
                        .height(36.dp)
                        .testTag("compare_wa_${offer.id}")
                ) {
                    Text(text = "🟢 WA", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Buy Request
                Button(
                    onClick = onBuyRequestClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoyalPharmaBlue,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier
                        .weight(1.6f)
                        .height(36.dp)
                        .testTag("compare_buy_request_${offer.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingCart,
                        contentDescription = "Buy Request",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "কিনুন",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
