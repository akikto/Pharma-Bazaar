package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderGray
import com.example.ui.theme.CardBorder
import com.example.ui.theme.PharmaBlueLight
import com.example.ui.theme.RoyalPharmaBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Category model for filtering medicine listings on the pharmacy request screen.
 */
data class MedicineCategory(
    val id: String,
    val nameBn: String,
    val iconEmoji: String
)

val PHARMACY_MEDICINE_CATEGORIES = listOf(
    MedicineCategory("ALL", "সব ওষুধ", "🏷️"),
    MedicineCategory("Tablet", "ট্যাবলেট", "💊"),
    MedicineCategory("Capsule", "ক্যাপসুল", "💊"),
    MedicineCategory("Syrup", "সিরাপ", "🧪"),
    MedicineCategory("Chewable", "ড্রপ/চিবানো", "🍬"),
    MedicineCategory("Injection", "ইনজেকশন", "💉")
)

/**
 * High-performance search & category filter bar component for pharmacy request and medicine listing screens.
 */
@Composable
fun PharmacyRequestSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    totalResultCount: Int = -1,
    onScanBarcodeClick: (() -> Unit)? = null,
    onResetFiltersClick: (() -> Unit)? = null,
    placeholderText: String = "ওষুধের নাম, জেনেরিক বা প্রস্তুতকারক দিয়ে খুঁজুন...",
    modifier: Modifier = Modifier
) {
    val isFilterActive = searchQuery.isNotBlank() || selectedCategory != "ALL"

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // --- Main Search Bar Input Text Field ---
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = {
                Text(
                    text = placeholderText,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search Medicines",
                    tint = RoyalPharmaBlue,
                    modifier = Modifier.size(22.dp)
                )
            },
            trailingIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(end = 6.dp)
                ) {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchQueryChange("") },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("clear_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Clear,
                                contentDescription = "Clear Search",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (onScanBarcodeClick != null) {
                        IconButton(
                            onClick = onScanBarcodeClick,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("scan_barcode_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.QrCodeScanner,
                                contentDescription = "Scan Barcode",
                                tint = RoyalPharmaBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = RoyalPharmaBlue,
                unfocusedBorderColor = BorderGray
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("pharmacy_request_search_input")
        )

        // --- Category Selection Chips Bar ---
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(PHARMACY_MEDICINE_CATEGORIES, key = { it.id }) { cat ->
                val isSelected = cat.id.equals(selectedCategory, ignoreCase = true)
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelected(cat.id) },
                    label = {
                        Text(
                            text = "${cat.iconEmoji} ${cat.nameBn}",
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    shape = CircleShape,
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
                    ),
                    elevation = FilterChipDefaults.filterChipElevation(elevation = 1.dp),
                    modifier = Modifier.testTag("category_chip_${cat.id}")
                )
            }
        }

        // --- Active Filter Summary Indicator Banner ---
        AnimatedVisibility(
            visible = isFilterActive,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PharmaBlueLight.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, RoyalPharmaBlue.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("active_filter_summary_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FilterAlt,
                            contentDescription = "Active Filter",
                            tint = RoyalPharmaBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        val filterText = buildString {
                            if (searchQuery.isNotBlank()) append("‘$searchQuery’ ")
                            if (selectedCategory != "ALL") {
                                val catObj = PHARMACY_MEDICINE_CATEGORIES.find { it.id.equals(selectedCategory, ignoreCase = true) }
                                append("[ক্যাটাগরি: ${catObj?.nameBn ?: selectedCategory}]")
                            }
                            if (totalResultCount >= 0) append(" (${totalResultCount} টি পাওয়া গেছে)")
                        }
                        Text(
                            text = filterText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RoyalPharmaBlue
                        )
                    }

                    if (onResetFiltersClick != null || searchQuery.isNotBlank() || selectedCategory != "ALL") {
                        TextButton(
                            onClick = {
                                onSearchQueryChange("")
                                onCategorySelected("ALL")
                                onResetFiltersClick?.invoke()
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                            modifier = Modifier
                                .height(28.dp)
                                .testTag("reset_filters_button")
                        ) {
                            Text(
                                text = "ফিল্টার রিসেট",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoyalPharmaBlue
                            )
                        }
                    }
                }
            }
        }
    }
}
