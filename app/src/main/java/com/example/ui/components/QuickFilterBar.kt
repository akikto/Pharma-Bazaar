package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderGray
import com.example.ui.theme.RoyalPharmaBlue
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.QuickFilter

data class DrugCategoryItem(val id: String, val labelBn: String, val icon: String)

val DRUG_CATEGORIES = listOf(
    DrugCategoryItem("ALL", "সব ক্যাটালগ", "🏷️"),
    DrugCategoryItem("Tablet", "ট্যাবলেট", "💊"),
    DrugCategoryItem("Capsule", "ক্যাপসুল", "💊"),
    DrugCategoryItem("Syrup", "সিরাপ", "🧪"),
    DrugCategoryItem("Chewable", "চিবানো/ড্রপ", "🍬"),
    DrugCategoryItem("Injection", "ইনজেকশন", "💉")
)

@Composable
fun QuickFilterBar(
    selectedFilter: QuickFilter,
    onFilterSelected: (QuickFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(QuickFilter.entries.toTypedArray()) { filter ->
            val isSelected = filter == selectedFilter
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        text = filter.titleBn,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) RoyalPharmaBlue else TextSecondary
                    )
                },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0x1A0D6EFD), // 10% opacity blue
                    selectedLabelColor = RoyalPharmaBlue,
                    containerColor = Color.White,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = BorderGray,
                    selectedBorderColor = Color(0x330D6EFD)
                ),
                elevation = FilterChipDefaults.filterChipElevation(elevation = 1.dp),
                modifier = Modifier.testTag("quick_filter_${filter.name}")
            )
        }
    }
}

@Composable
fun CategoryFilterBar(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(DRUG_CATEGORIES) { item ->
            val isSelected = item.id == selectedCategory
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(item.id) },
                label = {
                    Text(
                        text = "${item.icon} ${item.labelBn}",
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) RoyalPharmaBlue else TextSecondary
                    )
                },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0x1A0D6EFD),
                    selectedLabelColor = RoyalPharmaBlue,
                    containerColor = Color.White,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = BorderGray,
                    selectedBorderColor = Color(0x330D6EFD)
                ),
                elevation = FilterChipDefaults.filterChipElevation(elevation = 1.dp),
                modifier = Modifier.testTag("category_filter_${item.id}")
            )
        }
    }
}

