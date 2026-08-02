package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAlert
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.PriceCheck
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.entities.OfferListingEntity
import com.example.data.db.entities.PriceThresholdAlertEntity
import com.example.data.db.entities.TriggeredPriceAlertEntity
import com.example.ui.theme.BorderGray
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.PharmaBlueLight
import com.example.ui.theme.RoyalPharmaBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UrgentRed
import com.example.ui.theme.UrgentRedLight

/**
 * Price Threshold & Automated Alert Component for Pharmacists.
 * Triggers automated alerts when suppliers post an offer below a pharmacist's target price.
 */
@Composable
fun PriceThresholdAlertSection(
    thresholdAlerts: List<PriceThresholdAlertEntity>,
    triggeredAlerts: List<TriggeredPriceAlertEntity>,
    allOffers: List<OfferListingEntity>,
    onAddThreshold: (medicineName: String, genericName: String, maxPrice: Double) -> Unit,
    onToggleThreshold: (id: Long, isEnabled: Boolean) -> Unit,
    onDeleteThreshold: (id: Long) -> Unit,
    onDismissTriggeredAlert: (id: Long) -> Unit,
    onAddToCart: (OfferListingEntity, Int) -> Unit,
    onSimulateOffer: (medicineName: String, price: Double, sellerName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddForm by remember { mutableStateOf(false) }
    var inputMedicineName by remember { mutableStateOf("") }
    var inputMaxPrice by remember { mutableStateOf("") }

    val sampleSuggestions = listOf(
        "Napa Extra 500mg" to 38.0,
        "Seclo 20mg" to 42.0,
        "Sergel 20mg" to 45.0,
        "Monas 10mg" to 120.0,
        "Ceevit 250mg" to 18.0
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("price_threshold_alert_section"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- Header Banner ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, BorderGray)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFEF3C7),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.PriceCheck,
                                    contentDescription = "Price Threshold Alert",
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "ফার্মাসিস্ট প্রাইজ থ্রেশহোল্ড অ্যালার্ট",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "টার্গেট মূল্য সেট করুন, কমদামী অফার এলেই অটোমেটিক অ্যালার্ট পান",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Button(
                        onClick = { showAddForm = !showAddForm },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RoyalPharmaBlue,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("add_threshold_toggle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AddAlert,
                            contentDescription = "Add Limit",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (showAddForm) "বন্ধ করুন" else "+ নতুন সীমা",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Quick stats row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8FAFC))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${thresholdAlerts.size}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = RoyalPharmaBlue
                        )
                        Text(
                            text = "সেট করা টার্গেট সীমা",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(BorderGray)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${triggeredAlerts.size}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (triggeredAlerts.isNotEmpty()) UrgentRed else EmeraldGreen
                        )
                        Text(
                            text = "অটোমেটিক ট্রিগারড অ্যালার্ট",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // --- Add Price Threshold Form (Expandable) ---
        AnimatedVisibility(
            visible = showAddForm,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                border = BorderStroke(1.dp, Color(0xFFBFDBFE))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "নতুন ওষুধের সর্বোচ্চ মূল্য সীমা যোগ করুন",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalPharmaBlue
                    )

                    OutlinedTextField(
                        value = inputMedicineName,
                        onValueChange = { inputMedicineName = it },
                        label = { Text("ওষুধের নাম (e.g. Napa Extra, Seclo)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("threshold_medicine_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = inputMaxPrice,
                        onValueChange = { inputMaxPrice = it },
                        label = { Text("সর্বোচ্চ কাক্সিক্ষত মূল্য (৳ Target Price)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("threshold_price_input"),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    // Quick suggestion pills
                    Text(
                        text = "জনপ্রিয় ওষুধের কুইক সিলেক্ট:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(sampleSuggestions) { (med, price) ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFF93C5FD)),
                                onClick = {
                                    inputMedicineName = med
                                    inputMaxPrice = price.toInt().toString()
                                }
                            ) {
                                Text(
                                    text = "$med (৳${price.toInt()})",
                                    fontSize = 11.sp,
                                    color = RoyalPharmaBlue,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val priceVal = inputMaxPrice.toDoubleOrNull() ?: 0.0
                            if (inputMedicineName.isNotBlank() && priceVal > 0) {
                                onAddThreshold(inputMedicineName.trim(), "", priceVal)
                                inputMedicineName = ""
                                inputMaxPrice = ""
                                showAddForm = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("save_threshold_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalPharmaBlue)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = "Save Limit"
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("মূল্য সীমা সেভ ও অটো-অ্যালার্ট চালু করুন", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- Active Automated Triggered Price Alerts Feed ---
        if (triggeredAlerts.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.NotificationsActive,
                        contentDescription = "Triggered Alerts",
                        tint = UrgentRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "অটোমেটিক প্রাইস অ্যালার্ট ট্রিগারড! (${triggeredAlerts.size} টি)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = UrgentRed
                    )
                }

                triggeredAlerts.forEach { trigger ->
                    val matchingOffer = allOffers.find { it.id == trigger.offerListingId }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("triggered_alert_card_${trigger.id}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = UrgentRedLight),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = UrgentRed,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Outlined.TrendingDown,
                                                contentDescription = "Price Drop",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    Column {
                                        Text(
                                            text = "⚡ ${trigger.medicineName}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "বিক্রেতা: ${trigger.sellerShopName}",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { onDismissTriggeredAlert(trigger.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.DeleteOutline,
                                        contentDescription = "Dismiss",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            HorizontalDivider(color = Color(0xFFFCA5A5).copy(alpha = 0.5f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "অফার মূল্য: ৳${trigger.offerPrice.toInt()}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = EmeraldGreen
                                    )
                                    Text(
                                        text = "আপনার সীমা ছিল: ৳${trigger.targetThresholdPrice.toInt()}",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = EmeraldGreenLight,
                                    border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.4f))
                                ) {
                                    val savings = trigger.targetThresholdPrice - trigger.offerPrice
                                    Text(
                                        text = if (savings > 0) "সাশ্রয়: ৳${savings.toInt()}/প্যাক 🔥" else "টার্গেট প্রাইসে পাওয়া গেছে!",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            if (matchingOffer != null) {
                                Button(
                                    onClick = { onAddToCart(matchingOffer, matchingOffer.minimumOrderQuantity) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(36.dp)
                                        .testTag("trigger_add_to_cart_button"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ShoppingCart,
                                        contentDescription = "Add to Cart",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("এই মূল্যে এখনই কার্টে যোগ করুন 🛒", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Pharmacist Threshold Limits List ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "আপনার সক্রিয় মূল্য সীমা অ্যালার্টসমূহ",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                // Interactive Test Simulation Button
                OutlinedButton(
                    onClick = {
                        val testName = thresholdAlerts.firstOrNull()?.medicineName ?: "Napa Extra 500mg"
                        val testTarget = thresholdAlerts.firstOrNull()?.maxPriceThreshold ?: 38.0
                        val lowPrice = (testTarget - 3.0).coerceAtLeast(10.0)
                        onSimulateOffer(testName, lowPrice, "মিডফোর্ড হোলসেল কেমিস্ট")
                    },
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, RoyalPharmaBlue),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.testTag("simulate_low_price_offer_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Science,
                        contentDescription = "Test Offer",
                        tint = RoyalPharmaBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("🧪 টেস্ট অফার ট্রাই", fontSize = 11.sp, color = RoyalPharmaBlue, fontWeight = FontWeight.Bold)
                }
            }

            if (thresholdAlerts.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AddAlert,
                            contentDescription = "Empty Thresholds",
                            tint = TextSecondary,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "কোনো মূল্য সীমা সেট করা নেই",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "উপরে '+ নতুন সীমা' বাটনে ক্লিক করে ওষুধের সর্বোচ্চ পছন্দনীয় ক্রয় মূল্য সেট করুন।",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            } else {
                thresholdAlerts.forEach { threshold ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("threshold_item_card_${threshold.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, BorderGray)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (threshold.isEnabled) PharmaBlueLight else Color(0xFFF1F5F9),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.PriceCheck,
                                            contentDescription = "Threshold",
                                            tint = if (threshold.isEnabled) RoyalPharmaBlue else TextSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        text = threshold.medicineName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "সর্বোচ্চ টার্গেট মূল্য: ৳${threshold.maxPriceThreshold.toInt()}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = RoyalPharmaBlue
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Switch(
                                    checked = threshold.isEnabled,
                                    onCheckedChange = { onToggleThreshold(threshold.id, it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = RoyalPharmaBlue
                                    ),
                                    modifier = Modifier.testTag("threshold_switch_${threshold.id}")
                                )

                                IconButton(
                                    onClick = { onDeleteThreshold(threshold.id) },
                                    modifier = Modifier.testTag("threshold_delete_button_${threshold.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.DeleteOutline,
                                        contentDescription = "Delete Limit",
                                        tint = UrgentRed,
                                        modifier = Modifier.size(20.dp)
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
