package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalPharmacy
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Money
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.PostAdd
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.entities.MasterMedicineEntity
import com.example.data.db.entities.ShopProfileEntity
import com.example.ui.theme.BorderGray
import com.example.ui.theme.CardBorder
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.ExpiryAmber
import com.example.ui.theme.ExpiryAmberLight
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

/**
 * Data class representing a Bulk Medicine Procurement Request.
 */
data class BulkMedicineRequest(
    val medicineName: String,
    val genericName: String,
    val strength: String,
    val companyName: String,
    val form: String,
    val packSize: String,
    val requestedQuantity: Int, // in Boxes
    val targetUnitPrice: Double, // target price per Box in INR
    val minRequiredExpiryDays: Int, // e.g. 180 days (6 months)
    val expiryRequirementLabel: String,
    val urgencyLevel: String, // URGENT, NORMAL, FLEXIBLE
    val deliveryAddress: String,
    val contactPhone: String,
    val coldChainRequired: Boolean,
    val vatInvoiceRequired: Boolean,
    val factorySealedRequired: Boolean,
    val notes: String
)

/**
 * Expiration Requirement Preset options
 */
enum class ExpiryPresetOption(val labelBn: String, val minDays: Int) {
    MIN_3_MONTHS("কমপক্ষে ৩ মাস (৯০ দিন)", 90),
    MIN_6_MONTHS("কমপক্ষে ৬ মাস (১৮০ দিন)", 180),
    MIN_1_YEAR("কমপক্ষে ১ বছর (৩৬৫ দিন)", 365),
    SHORT_EXPIRY_OK("শর্ট এক্সপায়রি গ্রহণযোগ্য (৩০+ দিন)", 30),
    CUSTOM("কাস্টম দিন সংখ্যা...", -1)
}

/**
 * Interactive Dialog & Form for Pharmacies to post Bulk Medicine Procurement Requests.
 * Features strict real-time validations for quantities, minimum expiry requirements,
 * price estimates, and delivery logistics.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkMedicineRequestDialog(
    masterMedicines: List<MasterMedicineEntity>,
    activeShop: ShopProfileEntity,
    onDismiss: () -> Unit,
    onSubmitRequest: (BulkMedicineRequest) -> Unit
) {
    val focusManager = LocalFocusManager.current
    var expandedMasterDropdown by remember { mutableStateOf(false) }

    // --- Form Inputs ---
    var medicineName by remember { mutableStateOf("") }
    var genericName by remember { mutableStateOf("") }
    var strength by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("") }
    var form by remember { mutableStateOf("Tablet") }
    var packSize by remember { mutableStateOf("10x10 Box") }

    // Quantity State
    var quantityInput by remember { mutableStateOf("50") } // Default 50 boxes
    
    // Target Price State
    var targetUnitPriceInput by remember { mutableStateOf("180") } // Default target price 180 Tk

    // Expiry Requirement State
    var selectedExpiryPreset by remember { mutableStateOf(ExpiryPresetOption.MIN_6_MONTHS) }
    var customExpiryDaysInput by remember { mutableStateOf("180") }

    // Logistics & Special Requirements State
    var urgencyIndex by remember { mutableIntStateOf(1) } // 0 = Urgent (24-48h), 1 = Normal (3-5d), 2 = Flexible
    var deliveryAddress by remember { mutableStateOf(activeShop.address) }
    var contactPhone by remember { mutableStateOf(activeShop.phone) }
    var coldChainRequired by remember { mutableStateOf(false) }
    var vatInvoiceRequired by remember { mutableStateOf(true) }
    var factorySealedRequired by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf("") }

    // --- REAL-TIME VALIDATIONS ---
    val parsedQuantity = remember(quantityInput) { quantityInput.toIntOrNull() ?: 0 }
    val isQuantityValid = remember(parsedQuantity) { parsedQuantity > 0 && parsedQuantity <= 10000 }
    val isQuantityBelowBulkThreshold = remember(parsedQuantity) { parsedQuantity in 1..9 } // Bulk is usually 10+ boxes

    val parsedTargetPrice = remember(targetUnitPriceInput) { targetUnitPriceInput.toDoubleOrNull() ?: 0.0 }
    val isPriceValid = remember(parsedTargetPrice) { parsedTargetPrice > 0.0 }

    val effectiveExpiryDays = remember(selectedExpiryPreset, customExpiryDaysInput) {
        if (selectedExpiryPreset == ExpiryPresetOption.CUSTOM) {
            customExpiryDaysInput.toIntOrNull() ?: 0
        } else {
            selectedExpiryPreset.minDays
        }
    }
    
    // Expiration Requirement Validation: Minimum 30 days remaining for bulk purchase
    val isExpiryValid = remember(effectiveExpiryDays) { effectiveExpiryDays >= 30 }
    val isExpiryWarning = remember(effectiveExpiryDays) { effectiveExpiryDays in 30..59 } // Short expiry warning

    val isMedicineNameValid = remember(medicineName) { medicineName.trim().isNotBlank() }

    // Overall Form Validity
    val isFormValid by remember {
        derivedStateOf {
            isMedicineNameValid && isQuantityValid && isPriceValid && isExpiryValid && deliveryAddress.isNotBlank() && contactPhone.isNotBlank()
        }
    }

    // Calculated Total Budget
    val totalEstimatedBudget = remember(parsedQuantity, parsedTargetPrice) {
        if (isQuantityValid && isPriceValid) parsedQuantity * parsedTargetPrice else 0.0
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(22.dp),
        containerColor = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bulk_request_dialog"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = PharmaBlueLight,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.PostAdd,
                                contentDescription = "Bulk Request",
                                tint = RoyalPharmaBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "বাল্ক ওষুধ চাহিদা ফর্ম",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Post Bulk Procurement Request",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_bulk_dialog")
                ) {
                    Text("✕", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Info Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = InfoBlueBg,
                    border = BorderStroke(1.dp, InfoBlueBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Info",
                            tint = InfoBlueText,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "আপনার প্রয়োজনীয় ওষুধের পাইকারি ক্রয়ের চাহিদা জমা দিন। নিকটস্থ পাইকারি বিক্রেতারা আপনাকে সেরা মূল্য ও স্টক অফার করবে।",
                            fontSize = 11.sp,
                            color = InfoBlueText,
                            lineHeight = 15.sp
                        )
                    }
                }

                // --- SECTION 1: MEDICINE SELECTION ---
                Text(
                    text = "১. ওষুধ নির্বাচন (Medicine Specification)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoyalPharmaBlue
                )

                // Master Database Dropdown Selector
                ExposedDropdownMenuBox(
                    expanded = expandedMasterDropdown,
                    onExpandedChange = { expandedMasterDropdown = !expandedMasterDropdown }
                ) {
                    OutlinedTextField(
                        value = if (medicineName.isNotBlank()) "$medicineName $strength - $companyName" else "মাষ্টার ডাটাবেজ থেকে সহজে পছন্দ করুন",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("মাষ্টার ক্যাটালগ (Quick Auto-Fill)") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.MedicalServices,
                                contentDescription = "Medicine Catalog",
                                tint = RoyalPharmaBlue
                            )
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMasterDropdown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("input_master_medicine_dropdown"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RoyalPharmaBlue,
                            unfocusedBorderColor = BorderGray
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedMasterDropdown,
                        onDismissRequest = { expandedMasterDropdown = false }
                    ) {
                        masterMedicines.forEach { master ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = "${master.brandName} ${master.strength}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "${master.genericName} • ${master.companyName} (${master.form})",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                },
                                onClick = {
                                    medicineName = master.brandName
                                    genericName = master.genericName
                                    strength = master.strength
                                    companyName = master.companyName
                                    form = master.form
                                    packSize = master.defaultPackSize
                                    targetUnitPriceInput = (master.standardMrp * 0.82).toInt().toString()
                                    expandedMasterDropdown = false
                                }
                            )
                        }
                    }
                }

                // Brand Name Input
                OutlinedTextField(
                    value = medicineName,
                    onValueChange = { medicineName = it },
                    label = { Text("ওষুধের বাণিজ্যিক নাম (Brand Name) *") },
                    placeholder = { Text("যেমন: Napa Extend, Sergel 20") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.LocalPharmacy,
                            contentDescription = "Brand Name",
                            tint = RoyalPharmaBlue
                        )
                    },
                    isError = medicineName.isNotEmpty() && !isMedicineNameValid,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_bulk_medicine_name"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoyalPharmaBlue,
                        unfocusedBorderColor = BorderGray
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = genericName,
                        onValueChange = { genericName = it },
                        label = { Text("জেনেরিক নাম") },
                        placeholder = { Text("Paracetamol") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = strength,
                        onValueChange = { strength = it },
                        label = { Text("স্ট্রেংথ (Strength)") },
                        placeholder = { Text("665mg") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = companyName,
                        onValueChange = { companyName = it },
                        label = { Text("কোম্পানি / ম্যানুফ্যাকচারার") },
                        placeholder = { Text("Beximco / Square") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = packSize,
                        onValueChange = { packSize = it },
                        label = { Text("প্যাক সাইজ") },
                        placeholder = { Text("10x10 Box") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                HorizontalDivider(color = CardBorder, modifier = Modifier.padding(vertical = 2.dp))

                // --- SECTION 2: QUANTITY & VALIDATION ---
                Text(
                    text = "২. বাল্ক পরিমাণ ও ভ্যালিডেশন (Quantity Requirements)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoyalPharmaBlue
                )

                OutlinedTextField(
                    value = quantityInput,
                    onValueChange = { quantityInput = it.filter { char -> char.isDigit() } },
                    label = { Text("প্রয়োজনীয় বাল্ক পরিমাণ (Box / Pack) *") },
                    placeholder = { Text("যেমন: 50") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Inventory2,
                            contentDescription = "Quantity",
                            tint = RoyalPharmaBlue
                        )
                    },
                    isError = quantityInput.isNotEmpty() && !isQuantityValid,
                    supportingText = {
                        when {
                            quantityInput.isNotEmpty() && !isQuantityValid -> {
                                Text(
                                    text = "⚠️ সঠিক পরিমাণ লিখুন (১ থেকে ১০,০০০ বক্সের মধ্যে হতে হবে)",
                                    color = UrgentRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            isQuantityBelowBulkThreshold -> {
                                Text(
                                    text = "💡 বাল্ক পাইকারি অর্ডারের জন্য সাধারণত ১০+ বক্সের চাহিদা দেয়া হয়",
                                    color = ExpiryAmber,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            isQuantityValid -> {
                                Text(
                                    text = "✓ বৈধ বাল্ক পরিমাণ: $parsedQuantity বক্স",
                                    color = EmeraldGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_bulk_quantity"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoyalPharmaBlue,
                        unfocusedBorderColor = BorderGray
                    )
                )

                HorizontalDivider(color = CardBorder, modifier = Modifier.padding(vertical = 2.dp))

                // --- SECTION 3: EXPIRATION DATE REQUIREMENTS & VALIDATIONS ---
                Text(
                    text = "৩. মেয়াদের শর্তাবলী (Expiration Date Requirements) *",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoyalPharmaBlue
                )

                Text(
                    text = "ফার্মেসীতে বিক্রয়ের জন্য কতদিন মেয়াদের ওষুধ প্রয়োজন?",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                // Expiry Presets Selection
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ExpiryPresetOption.entries.forEach { option ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedExpiryPreset == option) PharmaBlueLight else SoftPaperGray,
                            border = BorderStroke(
                                1.dp,
                                if (selectedExpiryPreset == option) RoyalPharmaBlue else BorderGray
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedExpiryPreset = option }
                                .testTag("expiry_option_${option.name}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Timer,
                                    contentDescription = "Expiry Option",
                                    tint = if (selectedExpiryPreset == option) RoyalPharmaBlue else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = option.labelBn,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedExpiryPreset == option) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedExpiryPreset == option) RoyalPharmaBlue else TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                if (selectedExpiryPreset == option) {
                                    Icon(
                                        imageVector = Icons.Outlined.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = RoyalPharmaBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Custom Expiry Input field if CUSTOM selected
                AnimatedVisibility(visible = selectedExpiryPreset == ExpiryPresetOption.CUSTOM) {
                    OutlinedTextField(
                        value = customExpiryDaysInput,
                        onValueChange = { customExpiryDaysInput = it.filter { c -> c.isDigit() } },
                        label = { Text("কমপক্ষে কত দিন মেয়াদ অবশিষ্ট থাকতে হবে? *") },
                        placeholder = { Text("যেমন: 180") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.DateRange,
                                contentDescription = "Custom Days",
                                tint = RoyalPharmaBlue
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .testTag("input_custom_expiry_days"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Expiration Validation Feedback Banner
                when {
                    !isExpiryValid -> {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = UrgentRedLight,
                            border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Warning,
                                    contentDescription = "Error",
                                    tint = UrgentRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "⚠️ ভ্যালিডেশন ব্যর্থ: বাল্ক ক্রয়ের জন্য সর্বনিম্ন ৩০ দিন মেয়াদ অবশিষ্ট থাকা বাধ্যতামূলক।",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = UrgentRed
                                )
                            }
                        }
                    }
                    isExpiryWarning -> {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ExpiryAmberLight,
                            border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Timer,
                                    contentDescription = "Warning",
                                    tint = ExpiryAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "⏳ শর্ট এক্সপায়রি অ্যালার্ট: মেয়াদের সময় ৩০-৬০ দিন। দ্রুত বিক্রয়ের পরিকল্পনা নিশ্চিত করুন।",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }
                    }
                    else -> {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = EmeraldGreenLight,
                            border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = "Valid Expiry",
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "✓ মেয়াদ ভ্যালিডেশন সফল: কমপক্ষে $effectiveExpiryDays দিন ($selectedExpiryPreset) মেয়াদ থাকা নিশ্চিত করা হবে।",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreen
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = CardBorder, modifier = Modifier.padding(vertical = 2.dp))

                // --- SECTION 4: TARGET PRICE & LIVE BUDGET ESTIMATION ---
                Text(
                    text = "৪. বাজেট ও টার্গেট মূল্য (Target Price & Budget Estimate)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoyalPharmaBlue
                )

                OutlinedTextField(
                    value = targetUnitPriceInput,
                    onValueChange = { targetUnitPriceInput = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("প্রতি বক্স/প্যাক টার্গেট ক্রয় মূল্য (₹) *") },
                    placeholder = { Text("যেমন: 180") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Money,
                            contentDescription = "Target Price",
                            tint = RoyalPharmaBlue
                        )
                    },
                    isError = targetUnitPriceInput.isNotEmpty() && !isPriceValid,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_bulk_target_price"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Live Total Budget Card Calculation
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SoftPaperGray),
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "মোট আনুমানিক বাজেট (Estimated Budget):",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "₹${String.format("%,.2f", totalEstimatedBudget)}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoyalPharmaBlue
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PharmaBlueLight
                        ) {
                            Text(
                                text = "$parsedQuantity Box × ₹${parsedTargetPrice.toInt()}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoyalPharmaBlue,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = CardBorder, modifier = Modifier.padding(vertical = 2.dp))

                // --- SECTION 5: DELIVERY & SPECIAL REQUIREMENTS ---
                Text(
                    text = "৫. ডেলিভারি ও বিশেষ শর্তাবলী (Logistics & Terms)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoyalPharmaBlue
                )

                Text(
                    text = "ডেলিভারির জরুরি ভাব (Delivery Urgency):",
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SegmentedButton(
                        selected = urgencyIndex == 0,
                        onClick = { urgencyIndex = 0 },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                    ) {
                        Text("জরুরি (24-48h)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    SegmentedButton(
                        selected = urgencyIndex == 1,
                        onClick = { urgencyIndex = 1 },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                    ) {
                        Text("সাধারণ (3-5d)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    SegmentedButton(
                        selected = urgencyIndex == 2,
                        onClick = { urgencyIndex = 2 },
                        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                    ) {
                        Text("নমনীয় (Flexible)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedTextField(
                    value = deliveryAddress,
                    onValueChange = { deliveryAddress = it },
                    label = { Text("ডেলিভারি ঠিকানা / ফার্মেসীর অবস্থান *") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = "Address",
                            tint = RoyalPharmaBlue
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = contactPhone,
                    onValueChange = { contactPhone = it },
                    label = { Text("যোগাযোগের ফোন নম্বর *") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Phone,
                            contentDescription = "Phone",
                            tint = RoyalPharmaBlue
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Checkboxes for special terms
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { coldChainRequired = !coldChainRequired }
                    ) {
                        Checkbox(
                            checked = coldChainRequired,
                            onCheckedChange = { coldChainRequired = it },
                            colors = CheckboxDefaults.colors(checkedColor = RoyalPharmaBlue)
                        )
                        Text("❄️ কোল্ড চেইন সংরক্ষণ প্রয়োজন (২°-৮°C কোল্ড বক্স)", fontSize = 12.sp)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { vatInvoiceRequired = !vatInvoiceRequired }
                    ) {
                        Checkbox(
                            checked = vatInvoiceRequired,
                            onCheckedChange = { vatInvoiceRequired = it },
                            colors = CheckboxDefaults.colors(checkedColor = RoyalPharmaBlue)
                        )
                        Text("🧾 অফিসিয়াল ভ্যাট ইনভয়েস ও চালানের কপি চাই", fontSize = 12.sp)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { factorySealedRequired = !factorySealedRequired }
                    ) {
                        Checkbox(
                            checked = factorySealedRequired,
                            onCheckedChange = { factorySealedRequired = it },
                            colors = CheckboxDefaults.colors(checkedColor = RoyalPharmaBlue)
                        )
                        Text("🔒 ফ্যাক্টরি সিলপ্যাক অক্ষত কার্টন হতে হবে", fontSize = 12.sp)
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("অতিরিক্ত বিশেষ দ্রষ্টব্য (Optional Notes)") },
                    placeholder = { Text("যেমন: নির্দিষ্ট ব্যাচ নম্বর অথবা নির্দিষ্ট পেমেন্ট টার্মস...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isFormValid) {
                        val urgencyLabel = when (urgencyIndex) {
                            0 -> "URGENT_24_48H"
                            2 -> "FLEXIBLE"
                            else -> "NORMAL_3_5D"
                        }

                        val request = BulkMedicineRequest(
                            medicineName = medicineName.trim(),
                            genericName = genericName.ifBlank { "N/A" },
                            strength = strength.ifBlank { "N/A" },
                            companyName = companyName.ifBlank { "General Pharma" },
                            form = form,
                            packSize = packSize,
                            requestedQuantity = parsedQuantity,
                            targetUnitPrice = parsedTargetPrice,
                            minRequiredExpiryDays = effectiveExpiryDays,
                            expiryRequirementLabel = selectedExpiryPreset.labelBn,
                            urgencyLevel = urgencyLabel,
                            deliveryAddress = deliveryAddress.trim(),
                            contactPhone = contactPhone.trim(),
                            coldChainRequired = coldChainRequired,
                            vatInvoiceRequired = vatInvoiceRequired,
                            factorySealedRequired = factorySealedRequired,
                            notes = notes.trim()
                        )
                        onSubmitRequest(request)
                    }
                },
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(containerColor = RoyalPharmaBlue),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("btn_submit_bulk_request")
            ) {
                Text(
                    text = "বাল্ক চাহিদা পোস্ট করুন (Post Bulk Request)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderGray),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("বাতিল করুন", color = TextSecondary, fontSize = 12.sp)
            }
        }
    )
}
