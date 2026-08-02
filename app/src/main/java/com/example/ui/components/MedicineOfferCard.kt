package com.example.ui.components

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.theme.PharmaBlueLight
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.entities.OfferListingEntity
import com.example.ui.theme.BorderGray
import com.example.ui.theme.CardBorder
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.ExpiryAmber
import com.example.ui.theme.ExpiryAmberLight
import com.example.ui.theme.RoyalPharmaBlue
import com.example.ui.theme.SoftPaperGray
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UrgentRed
import com.example.ui.theme.UrgentRedBorder
import com.example.ui.theme.UrgentRedLight

@Composable
fun MedicineOfferCard(
    offer: OfferListingEntity,
    onBuyRequestClick: () -> Unit,
    onChatClick: () -> Unit,
    modifier: Modifier = Modifier,
    isWatchlisted: Boolean = false,
    onToggleWatchlist: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var showPriceTrendDialog by remember { mutableStateOf(false) }

    // Expiry badge color logic based on days remaining
    val (expiryColor, expiryBg, expiryBorder, expiryText) = when {
        offer.daysUntilExpiry <= 30 -> Quadruple(
            UrgentRed,
            UrgentRedLight,
            UrgentRedBorder,
            "${offer.daysUntilExpiry} দিন বাকি"
        )
        offer.daysUntilExpiry <= 60 -> Quadruple(
            ExpiryAmber,
            ExpiryAmberLight,
            Color(0xFFFDE68A),
            "${offer.daysUntilExpiry} দিন বাকি"
        )
        else -> Quadruple(
            EmeraldGreen,
            EmeraldGreenLight,
            Color(0xFFA7F3D0),
            offer.expiryDate
        )
    }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CardBorder),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("offer_card_${offer.id}")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Main Top Content Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Top Status Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Timer Expiry Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = expiryBg,
                        border = BorderStroke(1.dp, expiryBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Timer,
                                contentDescription = "Expiry",
                                tint = expiryColor,
                                modifier = Modifier.size(14.dp)
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

                    // Discount Badge & Heart Watchlist Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = EmeraldGreen
                        ) {
                            Text(
                                text = "🔥 ${offer.discountPercent}% ছাড়",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        if (onToggleWatchlist != null) {
                            IconButton(
                                onClick = onToggleWatchlist,
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("watchlist_button_${offer.id}")
                            ) {
                                Icon(
                                    imageVector = if (isWatchlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Watchlist Toggle",
                                    tint = if (isWatchlisted) UrgentRed else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Medicine Name & Company
                Text(
                    text = "${offer.medicineName} ${offer.strength}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = offer.companyName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Spec Tags Row: Pack Size, Avail Qty, MOQ
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SoftPaperGray,
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Text(
                            text = "Pack: ${offer.packSize}",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SoftPaperGray,
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Text(
                            text = "Avail: ${offer.availableQuantity} Box",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SoftPaperGray,
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Text(
                            text = "MOQ: ${offer.minimumOrderQuantity}",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Pricing Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "MRP: ৳${offer.mrp.toInt()}",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textDecoration = TextDecoration.LineThrough,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "অফার:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen,
                            modifier = Modifier.padding(bottom = 3.dp, end = 2.dp)
                        )
                        Text(
                            text = "৳${offer.offerPrice.toInt()}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = EmeraldGreen
                        )
                        Text(
                            text = " /বক্স",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }

                    Surface(
                        onClick = { showPriceTrendDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        color = PharmaBlueLight,
                        border = BorderStroke(1.dp, RoyalPharmaBlue.copy(alpha = 0.3f)),
                        modifier = Modifier.testTag("open_price_trend_dialog_btn_${offer.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📊", fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "দামের ট্রেন্ড",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoyalPharmaBlue
                            )
                        }
                    }
                }

                if (showPriceTrendDialog) {
                    MedicinePriceTrendDialog(
                        medicineName = "${offer.medicineName} ${offer.strength}",
                        genericName = offer.genericName,
                        mrp = offer.mrp,
                        currentOfferPrice = offer.offerPrice,
                        onDismiss = { showPriceTrendDialog = false }
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(top = 12.dp, bottom = 10.dp),
                    color = BorderGray
                )

                // Location & Verified Shop Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = "Location",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "⭐ ${offer.sellerRating} • ${offer.sellerShopName} (${offer.sellerLocation} • ${offer.sellerDistanceKm} km)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (offer.isVerifiedShop) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = "Verified Shop",
                            tint = RoyalPharmaBlue,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            // Bottom Action Bar Container (Soft slate background)
            Surface(
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        text = "💬 যোগাযোগ করুন:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val phoneNum = "01711223344"

                        // 1. In-app Chat Button
                        Button(
                            onClick = onChatClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RoyalPharmaBlue,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("chat_button_${offer.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ChatBubbleOutline,
                                contentDescription = "In-app Chat",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "চ্যাট",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // 2. Call Button
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNum"))
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldGreen,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("call_button_${offer.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Call,
                                contentDescription = "Call Seller",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "কল",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // 3. WhatsApp Button
                        Button(
                            onClick = {
                                val cleanPhone = phoneNum.replace("+", "").replace("-", "").replace(" ", "")
                                val formattedPhone = if (cleanPhone.startsWith("0")) "88$cleanPhone" else cleanPhone
                                val waUrl = "https://wa.me/$formattedPhone?text=${Uri.encode("হাই, আমি ${offer.medicineName} (${offer.strength}) অফার সম্পর্কে জানতে চাই।")}"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl))
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF25D366), // WhatsApp Green
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(42.dp)
                                .testTag("whatsapp_button_${offer.id}")
                        ) {
                            Text(
                                text = "🟢 WhatsApp",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Buy Request Primary Action Button
                    Button(
                        onClick = onBuyRequestClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("buy_request_button_${offer.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E293B), // Dark Slate Primary
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ShoppingCart,
                            contentDescription = "Buy Request",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "বাই রিকোয়েস্ট পাঠান (Buy Request)",
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

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

