package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AddShoppingCart
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.CompareArrows
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LocalPharmacy
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.db.entities.MasterMedicineEntity
import com.example.data.db.entities.OfferListingEntity
import com.example.ui.theme.BorderGray
import com.example.ui.theme.CardBorder
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PharmaBlueLight
import com.example.ui.theme.RoyalPharmaBlue
import com.example.ui.theme.SoftPaperGray
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UrgentRed

/**
 * Therapeutic Category definition for filtering.
 */
data class TherapeuticCategory(
    val id: String,
    val nameBn: String,
    val nameEn: String,
    val iconEmoji: String,
    val sampleGenerics: List<String>
)

val THERAPEUTIC_CATEGORIES = listOf(
    TherapeuticCategory("ALL", "সব ওষুধ", "All Medicines", "💊", emptyList()),
    TherapeuticCategory("GASTRIC", "গ্যাস্ট্রিক ও আলসার", "Gastric & Ulcer", "🩺", listOf("Esomeprazole", "Omeprazole", "Rabeprazole")),
    TherapeuticCategory("PAIN", "ব্যথানাশক ও জ্বর", "Pain & Fever", "🌡️", listOf("Paracetamol", "Paracetamol + Caffeine", "Aceclofenac")),
    TherapeuticCategory("ANTIBIOTIC", "এন্টিবায়োটিক", "Antibiotics", "🧫", listOf("Azithromycin", "Cefuroxime", "Ciprofloxacin")),
    TherapeuticCategory("CARDIO", "হৃদরোগ ও প্রেসার", "Cardiovascular", "🫀", listOf("Amlodipine", "Amlodipine + Olmesartan", "Atenolol")),
    TherapeuticCategory("RESPIRATORY", "হাঁপানি ও শ্বাসকষ্ট", "Respiratory & Asthma", "🫁", listOf("Montelukast", "Fexofenadine", "Salbutamol")),
    TherapeuticCategory("VITAMIN", "ভিটামিন ও নিউট্রিশন", "Vitamins & Supplements", "🍊", listOf("Ascorbic Acid (Vit C)", "Calcium", "Multivitamin"))
)

data class PredictiveSuggestion(
    val title: String,
    val subtitle: String,
    val categoryType: String, // BRAND, GENERIC, COMPANY, FORM
    val genericName: String = "",
    val iconEmoji: String = "🔍"
)

/**
 * Dedicated, feature-packed Search Screen with Predictive Text Auto-Complete
 * and Generic Alternatives Discovery for Pharmacies.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    offersList: List<OfferListingEntity>,
    masterMedicines: List<MasterMedicineEntity>,
    recentSearches: List<String>,
    onAddRecentSearch: (String) -> Unit,
    onClearRecentSearches: () -> Unit,
    watchlistedNames: Set<String>,
    onToggleWatchlist: (String, String, String, String) -> Unit,
    onAddToCart: (OfferListingEntity) -> Unit,
    onCompareClick: (medicineFullName: String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFormFilter by remember { mutableStateOf("ALL") }
    var selectedTherapeuticCategory by remember { mutableStateOf("ALL") }
    var isVoiceActive by remember { mutableStateOf(false) }

    // Form options
    val formFilters = listOf("ALL", "Tablet", "Capsule", "Syrup", "Injection", "Chewable")

    // Predictive Text Generator based on search query
    val predictiveSuggestions by remember(searchQuery, masterMedicines, offersList) {
        derivedStateOf {
            if (searchQuery.trim().length < 1) {
                emptyList()
            } else {
                val queryLower = searchQuery.trim().lowercase()
                val list = mutableListOf<PredictiveSuggestion>()

                // 1. Master medicine brand matches
                masterMedicines.filter {
                    it.brandName.lowercase().contains(queryLower) || it.genericName.lowercase().contains(queryLower)
                }.take(4).forEach { master ->
                    list.add(
                        PredictiveSuggestion(
                            title = "${master.brandName} ${master.strength}",
                            subtitle = "${master.genericName} • ${master.companyName}",
                            categoryType = "BRAND",
                            genericName = master.genericName,
                            iconEmoji = "💊"
                        )
                    )
                }

                // 2. Generic name matches
                masterMedicines.map { it.genericName }.distinct().filter {
                    it.lowercase().contains(queryLower)
                }.take(3).forEach { gen ->
                    list.add(
                        PredictiveSuggestion(
                            title = gen,
                            subtitle = "জেনেরিক কেমিক্যাল প্রস্তুতকারক একাধিক",
                            categoryType = "GENERIC",
                            genericName = gen,
                            iconEmoji = "🧬"
                        )
                    )
                }

                // 3. Company matches
                masterMedicines.map { it.companyName }.distinct().filter {
                    it.lowercase().contains(queryLower)
                }.take(2).forEach { comp ->
                    list.add(
                        PredictiveSuggestion(
                            title = comp,
                            subtitle = "ফার্মাসিউটিক্যাল কোম্পানি ইনভেন্টরি",
                            categoryType = "COMPANY",
                            iconEmoji = "🏢"
                        )
                    )
                }

                list.distinctBy { it.title }
            }
        }
    }

    // Filter offers list by search, therapeutic area, and form
    val filteredSearchResults by remember(offersList, searchQuery, selectedTherapeuticCategory, selectedFormFilter) {
        derivedStateOf {
            var result = offersList

            // Text search
            if (searchQuery.isNotBlank()) {
                val q = searchQuery.trim().lowercase()
                result = result.filter {
                    it.medicineName.lowercase().contains(q) ||
                            it.genericName.lowercase().contains(q) ||
                            it.companyName.lowercase().contains(q) ||
                            it.form.lowercase().contains(q) ||
                            it.sellerShopName.lowercase().contains(q)
                }
            }

            // Therapeutic category
            if (selectedTherapeuticCategory != "ALL") {
                val catObj = THERAPEUTIC_CATEGORIES.find { it.id == selectedTherapeuticCategory }
                if (catObj != null && catObj.sampleGenerics.isNotEmpty()) {
                    result = result.filter { offer ->
                        catObj.sampleGenerics.any { gen ->
                            offer.genericName.contains(gen, ignoreCase = true)
                        }
                    }
                }
            }

            // Form filter
            if (selectedFormFilter != "ALL") {
                result = result.filter { it.form.equals(selectedFormFilter, ignoreCase = true) }
            }

            result
        }
    }

    // Find Generic Alternatives if a specific query is typed or suggestion selected
    val genericAlternatives by remember(searchQuery, offersList) {
        derivedStateOf {
            if (searchQuery.trim().length >= 2) {
                val matchingOffer = offersList.find {
                    it.medicineName.contains(searchQuery, ignoreCase = true) ||
                            searchQuery.contains(it.medicineName, ignoreCase = true)
                }
                if (matchingOffer != null) {
                    // Find all OTHER offers that match the same generic name but different brand names
                    val currentBrand = matchingOffer.medicineName.lowercase()
                    val matches = offersList.filter {
                        it.genericName.equals(matchingOffer.genericName, ignoreCase = true) &&
                                !it.medicineName.lowercase().contains(currentBrand)
                    }
                    matchingOffer to matches
                } else null
            } else null
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    // Row 1: Top Bar Title & Back
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = onBackClick,
                                modifier = Modifier.testTag("search_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                    contentDescription = "Back",
                                    tint = RoyalPharmaBlue
                                )
                            }
                            Image(
                                painter = painterResource(id = R.drawable.ic_pharma_logo),
                                contentDescription = "Pharma Logo",
                                modifier = Modifier.size(32.dp)
                            )
                            Column {
                                Text(
                                    text = "স্মার্ট ফার্মেসি সার্চ",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "ব্র্যান্ড, জেনেরিক বিকল্প ও সরাসরি মূল্য তুলনা",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 2: Live Predictive Input Text Field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { newQuery ->
                            onSearchQueryChange(newQuery)
                        },
                        placeholder = {
                            Text(
                                text = "ওষুধের নাম, জেনেরিক (যেমন: Napa, Sergel, Paracetamol) খুঁজুন...",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search",
                                tint = RoyalPharmaBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        trailingIcon = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { onSearchQueryChange("") },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Clear,
                                            contentDescription = "Clear search",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        // Quick voice simulation
                                        isVoiceActive = !isVoiceActive
                                        if (isVoiceActive) {
                                            onSearchQueryChange("Sergel")
                                            onAddRecentSearch("Sergel")
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Mic,
                                        contentDescription = "Voice Search",
                                        tint = if (isVoiceActive) UrgentRed else RoyalPharmaBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF0F6FF),
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedBorderColor = RoyalPharmaBlue,
                            unfocusedBorderColor = BorderGray
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_screen_input_field")
                    )
                }
            }
        },
        containerColor = SoftPaperGray
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Section 1: Predictive Text / Auto-Complete Floating Card (if typing)
            if (predictiveSuggestions.isNotEmpty()) {
                item(key = "predictive_suggestions_section") {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, CardBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("predictive_suggestions_card")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "💡 অটো-সাজেশন ও প্রেডিক্টিভ সার্চ (${predictiveSuggestions.size})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalPharmaBlue
                                )
                                Text(
                                    text = "ট্যাপ করে সিলেক্ট করুন",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            predictiveSuggestions.forEachIndexed { index, suggestion ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            onSearchQueryChange(suggestion.title)
                                            onAddRecentSearch(suggestion.title)
                                        }
                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(text = suggestion.iconEmoji, fontSize = 16.sp)
                                        Column {
                                            Text(
                                                text = suggestion.title,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = suggestion.subtitle,
                                                fontSize = 11.sp,
                                                color = TextSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = when (suggestion.categoryType) {
                                            "BRAND" -> Color(0xFFE0EDFF)
                                            "GENERIC" -> Color(0xFFD1FAE5)
                                            else -> Color(0xFFF3F4F6)
                                        },
                                        modifier = Modifier.padding(start = 6.dp)
                                    ) {
                                        Text(
                                            text = when (suggestion.categoryType) {
                                                "BRAND" -> "ব্র্যান্ড"
                                                "GENERIC" -> "জেনেরিক"
                                                "COMPANY" -> "কোম্পানি"
                                                else -> "ফর্ম"
                                            },
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (suggestion.categoryType) {
                                                "BRAND" -> RoyalPharmaBlue
                                                "GENERIC" -> EmeraldGreen
                                                else -> TextSecondary
                                            },
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                if (index < predictiveSuggestions.size - 1) {
                                    HorizontalDivider(color = BorderGray.copy(alpha = 0.5f))
                                }
                            }
                        }
                    }
                }
            }

            // Section 2: Recent Search Pills (if no active predictive box or as quick tags)
            if (recentSearches.isNotEmpty() && searchQuery.isBlank()) {
                item(key = "recent_searches_section") {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.History,
                                        contentDescription = "Recent",
                                        tint = RoyalPharmaBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "সাম্প্রতিক অনুসন্ধান (Recent Searches)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                                TextButton(
                                    onClick = onClearRecentSearches,
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("মুছে ফেলুন", fontSize = 11.sp, color = UrgentRed)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(recentSearches) { tag ->
                                    AssistChip(
                                        onClick = {
                                            onSearchQueryChange(tag)
                                        },
                                        label = { Text(tag, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Outlined.Search,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = RoyalPharmaBlue
                                            )
                                        },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = Color(0xFFF0F6FF),
                                            labelColor = RoyalPharmaBlue
                                        ),
                                        border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 3: Therapeutic Category Filter Bar
            item(key = "therapeutic_category_section") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "রোগের ক্যাটাগরি ও থেরাপিউটিক গ্রুপ",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (selectedTherapeuticCategory != "ALL" || selectedFormFilter != "ALL") {
                            TextButton(
                                onClick = {
                                    selectedTherapeuticCategory = "ALL"
                                    selectedFormFilter = "ALL"
                                },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("ফিল্টার রিসেট", fontSize = 11.sp, color = RoyalPharmaBlue)
                            }
                        }
                    }

                    // Therapeutic Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(THERAPEUTIC_CATEGORIES) { cat ->
                            val isSelected = selectedTherapeuticCategory == cat.id
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedTherapeuticCategory = cat.id
                                },
                                label = {
                                    Text(
                                        text = "${cat.iconEmoji} ${cat.nameBn}",
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RoyalPharmaBlue,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White,
                                    labelColor = TextPrimary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    selectedBorderColor = RoyalPharmaBlue,
                                    borderColor = BorderGray
                                )
                            )
                        }
                    }

                    // Dosage Form Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(formFilters) { form ->
                            val isSelected = selectedFormFilter == form
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) EmeraldGreen else Color.White,
                                border = BorderStroke(1.dp, if (isSelected) EmeraldGreen else BorderGray),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { selectedFormFilter = form }
                            ) {
                                Text(
                                    text = if (form == "ALL") "সব ফর্ম" else form,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else TextSecondary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Section 4: Generic Alternatives Discovery Card (জেনেরিক বিকল্প)
            genericAlternatives?.let { (searchedOffer, altList) ->
                if (altList.isNotEmpty()) {
                    item(key = "generic_alternatives_card") {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                            border = BorderStroke(1.5.dp, EmeraldGreen),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("generic_alternatives_card")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = EmeraldGreen,
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Outlined.SwapHoriz,
                                                    contentDescription = "Generic Match",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "জেনেরিক সমজাতীয় বিকল্প (${searchedOffer.genericName})",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF065F46)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = EmeraldGreen
                                    ) {
                                        Text(
                                            text = "${altList.size}টি বিকল্প প্রাপ্ত",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "'${searchedOffer.medicineName}'-এর একই জেনেরিক কেমিক্যাল '${searchedOffer.genericName}' যুক্ত অন্যান্য সাশ্রয়ী ব্র্যান্ডগুলো দেখুন:",
                                    fontSize = 11.sp,
                                    color = Color(0xFF047857)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                altList.forEachIndexed { index, alt ->
                                    val priceDiff = searchedOffer.offerPrice - alt.offerPrice
                                    val savingsText = if (priceDiff > 0) "₹${priceDiff.toInt()} সাশ্রয়" else "একই মান"

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color.White)
                                            .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(10.dp))
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = "${alt.medicineName} ${alt.strength}",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color(0xFFDCFCE7)
                                                ) {
                                                    Text(
                                                        text = alt.companyName,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = EmeraldGreen,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "প্যাক: ${alt.packSize} • বিক্রেতা: ${alt.sellerShopName}",
                                                fontSize = 11.sp,
                                                color = TextSecondary
                                            )
                                            Text(
                                                text = "অফার মূল্য: ₹${alt.offerPrice} (MRP: ₹${alt.mrp})",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = RoyalPharmaBlue
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            if (priceDiff > 0) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = Color(0xFFDEF7EC)
                                                ) {
                                                    Text(
                                                        text = "💸 $savingsText",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = EmeraldGreen,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                            }

                                            Button(
                                                onClick = { onAddToCart(alt) },
                                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text("কার্টে নিন", fontSize = 10.sp, color = Color.White)
                                            }
                                        }
                                    }

                                    if (index < altList.size - 1) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section 5: Search Results Header
            item(key = "search_results_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "অনুসন্ধানের ফলাফল (${filteredSearchResults.size}টি অফার)"
                        else "উপলব্ধ সব অফার ইনভেন্টরি (${filteredSearchResults.size}টি)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    if (searchQuery.isNotBlank()) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE0EDFF)
                        ) {
                            Text(
                                text = "'$searchQuery'",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = RoyalPharmaBlue,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // Section 6: Offer Listing Results
            if (filteredSearchResults.isEmpty()) {
                item(key = "search_results_empty") {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, CardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "কোনো ওষুধ পাওয়া যায়নি",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "বানান পরীক্ষা করুন অথবা অন্য জেনেরিক বা থেরাপিউটিক ক্যাটাগরি সিলেক্ট করুন।",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            } else {
                items(filteredSearchResults, key = { it.id }) { offer ->
                    val isWatchlisted = watchlistedNames.contains(offer.medicineName)

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, CardBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_result_card_${offer.id}")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Top Row: Brand name, Expiry pill & Watchlist
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = offer.medicineName,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFE0EDFF)
                                        ) {
                                            Text(
                                                text = offer.form,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = RoyalPharmaBlue,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${offer.genericName} • ${offer.strength}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = EmeraldGreen
                                    )
                                    Text(
                                        text = offer.companyName,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // Watchlist button
                                    IconButton(
                                        onClick = {
                                            onToggleWatchlist(offer.medicineName, offer.genericName, offer.companyName, offer.form)
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isWatchlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                            contentDescription = "Watchlist",
                                            tint = UrgentRed,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    // Discount Pill
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = UrgentRed
                                    ) {
                                        Text(
                                            text = "${offer.discountPercent}% ছাড়",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = BorderGray.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(8.dp))

                            // Middle Row: Price, Stock & Expiry details
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "₹${offer.offerPrice}",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = RoyalPharmaBlue
                                        )
                                        Text(
                                            text = "MRP: ₹${offer.mrp}",
                                            fontSize = 12.sp,
                                            color = TextSecondary,
                                            style = androidx.compose.ui.text.TextStyle(
                                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                            )
                                        )
                                    }
                                    Text(
                                        text = "প্যাক সাইজ: ${offer.packSize} • নূন্যতম অর্ডার: ${offer.minimumOrderQuantity}টি",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }

                                // Expiry Pill
                                val expiryBg = when {
                                    offer.daysUntilExpiry <= 30 -> Color(0xFFFEE2E2)
                                    offer.daysUntilExpiry <= 60 -> Color(0xFFFFEDD5)
                                    else -> Color(0xFFD1FAE5)
                                }
                                val expiryTextColor = when {
                                    offer.daysUntilExpiry <= 30 -> UrgentRed
                                    offer.daysUntilExpiry <= 60 -> Color(0xFFC2410C)
                                    else -> EmeraldGreen
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = expiryBg
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "⏳ মেয়াদ: ${offer.expiryDate}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = expiryTextColor
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Supplier info
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SoftPaperGray, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.LocalPharmacy,
                                        contentDescription = null,
                                        tint = RoyalPharmaBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = offer.sellerShopName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    if (offer.isVerifiedShop) {
                                        Icon(
                                            imageVector = Icons.Outlined.Verified,
                                            contentDescription = "Verified",
                                            tint = RoyalPharmaBlue,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = "Rating",
                                            tint = Color(0xFFF59E0B),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = "${offer.sellerRating}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    }
                                    Text(
                                        text = "📍 ${offer.sellerDistanceKm} কিমি",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action Buttons: Cart & Multi-Seller Comparison
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onCompareClick("${offer.medicineName} ${offer.strength}") },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, RoyalPharmaBlue),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.CompareArrows,
                                            contentDescription = null,
                                            tint = RoyalPharmaBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text("দাম তুলনা", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalPharmaBlue)
                                    }
                                }

                                Button(
                                    onClick = { onAddToCart(offer) },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .height(36.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.AddShoppingCart,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text("কার্টে যোগ করুন", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
