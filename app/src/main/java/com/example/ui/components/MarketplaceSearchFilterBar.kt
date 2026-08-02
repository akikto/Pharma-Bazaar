package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.BorderGray
import com.example.ui.theme.CardBorder
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.PharmaBlueLight
import com.example.ui.theme.RoyalPharmaBlue
import com.example.ui.theme.SoftPaperGray
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MarketplaceFilterState
import com.example.ui.viewmodel.MarketplaceSortOption

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MarketplaceSortFilterBar(
    activeSort: MarketplaceSortOption,
    onSortSelected: (MarketplaceSortOption) -> Unit,
    activeFilter: MarketplaceFilterState,
    onFilterChanged: (MarketplaceFilterState) -> Unit,
    onResetAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showFilterDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Full Filter Dialog Trigger Button
            Surface(
                onClick = { showFilterDialog = true },
                shape = RoundedCornerShape(20.dp),
                color = if (activeFilter.isActive) RoyalPharmaBlue else SoftPaperGray,
                border = BorderStroke(
                    1.dp,
                    if (activeFilter.isActive) RoyalPharmaBlue else CardBorder
                ),
                modifier = Modifier.testTag("open_marketplace_filter_dialog_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = "Filter",
                        tint = if (activeFilter.isActive) Color.White else RoyalPharmaBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ফিল্টার",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeFilter.isActive) Color.White else TextPrimary
                    )
                    if (activeFilter.activeCount > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (activeFilter.isActive) EmeraldGreen else RoyalPharmaBlue)
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "${activeFilter.activeCount}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Quick Sort Option Chips
            MarketplaceSortOption.entries.forEach { option ->
                val isSelected = activeSort == option
                Surface(
                    onClick = { onSortSelected(option) },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) PharmaBlueLight else Color.White,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) RoyalPharmaBlue else CardBorder
                    ),
                    modifier = Modifier.testTag("sort_chip_${option.name}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(option.iconEmoji, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = option.titleBn,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) RoyalPharmaBlue else TextSecondary
                        )
                    }
                }
            }
        }
    }

    if (showFilterDialog) {
        MarketplaceFilterDialog(
            currentSort = activeSort,
            onSelectSort = onSortSelected,
            currentFilter = activeFilter,
            onApplyFilter = { newFilter ->
                onFilterChanged(newFilter)
                showFilterDialog = false
            },
            onResetAll = {
                onResetAll()
                showFilterDialog = false
            },
            onDismiss = { showFilterDialog = false }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MarketplaceFilterDialog(
    currentSort: MarketplaceSortOption,
    onSelectSort: (MarketplaceSortOption) -> Unit,
    currentFilter: MarketplaceFilterState,
    onApplyFilter: (MarketplaceFilterState) -> Unit,
    onResetAll: () -> Unit,
    onDismiss: () -> Unit
) {
    var tempSort by remember { mutableStateOf(currentSort) }
    var tempMaxPrice by remember { mutableDoubleStateOf(currentFilter.maxPrice) }
    var tempMinRating by remember { mutableDoubleStateOf(currentFilter.minSupplierRating) }
    var tempMaxDistance by remember { mutableDoubleStateOf(currentFilter.maxDistanceKm) }
    var tempVerifiedOnly by remember { mutableStateOf(currentFilter.verifiedOnly) }
    var tempInStockOnly by remember { mutableStateOf(currentFilter.inStockOnly) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp)
                .testTag("marketplace_filter_dialog_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = PharmaBlueLight,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.FilterList,
                                    contentDescription = null,
                                    tint = RoyalPharmaBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "মেডিসিন ফিল্টার ও সাজানো",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "পছন্দের ক্রাইটেরিয়া অনুযায়ী সাজান",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_filter_dialog_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 14.dp),
                    color = BorderGray
                )

                // Section 1: Sorting Options
                Text(
                    text = "সাজানোর মাধ্যম (Sorting)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoyalPharmaBlue
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MarketplaceSortOption.entries.forEach { option ->
                        val isSelected = tempSort == option
                        Surface(
                            onClick = { tempSort = option },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) RoyalPharmaBlue else SoftPaperGray,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) RoyalPharmaBlue else CardBorder
                            ),
                            modifier = Modifier.testTag("dialog_sort_option_${option.name}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(option.iconEmoji, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = option.titleBn,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else TextPrimary
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 14.dp),
                    color = BorderGray
                )

                // Section 2: Max Price Filter Slider & Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "সর্বোচ্চ বাজেট (Price Filter)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalPharmaBlue
                    )
                    Text(
                        text = if (tempMaxPrice >= 10000.0) "সব দাম" else "₹${tempMaxPrice.toInt()} পর্যন্ত",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen
                    )
                }

                Slider(
                    value = tempMaxPrice.toFloat(),
                    onValueChange = { tempMaxPrice = it.toDouble() },
                    valueRange = 50f..10000f,
                    steps = 19,
                    colors = SliderDefaults.colors(
                        thumbColor = RoyalPharmaBlue,
                        activeTrackColor = RoyalPharmaBlue,
                        inactiveTrackColor = BorderGray
                    ),
                    modifier = Modifier.testTag("filter_price_slider")
                )

                // Price Presets
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(
                        "সব দাম" to 10000.0,
                        "₹২০০" to 200.0,
                        "₹৫০০" to 500.0,
                        "₹১০০০" to 1000.0
                    ).forEach { (label, priceVal) ->
                        val isSel = (priceVal == tempMaxPrice)
                        Surface(
                            onClick = { tempMaxPrice = priceVal },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) EmeraldGreenLight else SoftPaperGray,
                            border = BorderStroke(1.dp, if (isSel) EmeraldGreen else CardBorder)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) EmeraldGreen else TextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 14.dp),
                    color = BorderGray
                )

                // Section 3: Min Supplier Rating Filter
                Text(
                    text = "সাপ্লায়ার রেটিং (Min Supplier Rating)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoyalPharmaBlue
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(
                        "সব রেটিং" to 0.0,
                        "৪.০★+" to 4.0,
                        "৪.৫★+" to 4.5,
                        "৪.8★+" to 4.8
                    ).forEach { (label, minRating) ->
                        val isSel = (minRating == tempMinRating)
                        Surface(
                            onClick = { tempMinRating = minRating },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) PharmaBlueLight else SoftPaperGray,
                            border = BorderStroke(1.dp, if (isSel) RoyalPharmaBlue else CardBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) RoyalPharmaBlue else TextPrimary
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 14.dp),
                    color = BorderGray
                )

                // Section 4: Distance Filter
                Text(
                    text = "সর্বোচ্চ দূরত্ব (Max Seller Distance)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoyalPharmaBlue
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(
                        "সব দূরত্ব" to 50.0,
                        "১ কিমি" to 1.0,
                        "২ কিমি" to 2.0,
                        "৫ কিমি" to 5.0
                    ).forEach { (label, maxDist) ->
                        val isSel = (maxDist == tempMaxDistance)
                        Surface(
                            onClick = { tempMaxDistance = maxDist },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) PharmaBlueLight else SoftPaperGray,
                            border = BorderStroke(1.dp, if (isSel) RoyalPharmaBlue else CardBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) RoyalPharmaBlue else TextPrimary
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 14.dp),
                    color = BorderGray
                )

                // Section 5: Toggle Switches (Verified Sellers & In-Stock)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "শুধু ভেরিফাইড সাপ্লায়ার (Verified Only)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "ডিজিডিএ লাইসেন্সপ্রাপ্ত ও ভেরিফাইড শপ",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = tempVerifiedOnly,
                        onCheckedChange = { tempVerifiedOnly = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = RoyalPharmaBlue
                        ),
                        modifier = Modifier.testTag("filter_verified_switch")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "শুধু ইন-স্টক অফার (In-Stock Only)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "স্টকে থাকা ওষুধসমূহ দেখাবে",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = tempInStockOnly,
                        onCheckedChange = { tempInStockOnly = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = RoyalPharmaBlue
                        ),
                        modifier = Modifier.testTag("filter_instock_switch")
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons: Reset All & Apply
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            tempSort = MarketplaceSortOption.RECOMMENDED
                            tempMaxPrice = 10000.0
                            tempMinRating = 0.0
                            tempMaxDistance = 50.0
                            tempVerifiedOnly = false
                            tempInStockOnly = false
                            onResetAll()
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reset_filters_dialog_btn")
                    ) {
                        Text("রিসেট করুন", color = TextSecondary, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            onSelectSort(tempSort)
                            onApplyFilter(
                                MarketplaceFilterState(
                                    maxPrice = tempMaxPrice,
                                    minSupplierRating = tempMinRating,
                                    maxDistanceKm = tempMaxDistance,
                                    verifiedOnly = tempVerifiedOnly,
                                    inStockOnly = tempInStockOnly
                                )
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalPharmaBlue),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("apply_filters_dialog_btn")
                    ) {
                        Text("প্রয়োগ করুন", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
