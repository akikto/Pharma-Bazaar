package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.entities.MasterMedicineEntity
import com.example.data.db.entities.OfferListingEntity
import com.example.ui.theme.RoyalPharmaBlue
import com.example.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditOfferDialog(
    masterMedicines: List<MasterMedicineEntity>,
    offerToEdit: OfferListingEntity?,
    onDismiss: () -> Unit,
    onSave: (
        medicineName: String,
        genericName: String,
        strength: String,
        companyName: String,
        form: String,
        packSize: String,
        batchNumber: String,
        expiryDate: String,
        daysUntilExpiry: Int,
        quantity: Int,
        mrp: Double,
        offerPrice: Double,
        moq: Int,
        notes: String
    ) -> Unit
) {
    var expandedMasterDropdown by remember { mutableStateOf(false) }

    var medicineName by remember { mutableStateOf(offerToEdit?.medicineName ?: "") }
    var genericName by remember { mutableStateOf(offerToEdit?.genericName ?: "") }
    var strength by remember { mutableStateOf(offerToEdit?.strength ?: "") }
    var companyName by remember { mutableStateOf(offerToEdit?.companyName ?: "") }
    var form by remember { mutableStateOf(offerToEdit?.form ?: "Tablet") }
    var packSize by remember { mutableStateOf(offerToEdit?.packSize ?: "10x10 Box") }
    var batchNumber by remember { mutableStateOf(offerToEdit?.batchNumber ?: "BX-2026") }
    var expiryDate by remember { mutableStateOf(offerToEdit?.expiryDate ?: "31 Dec 2026") }
    var daysUntilExpiryText by remember { mutableStateOf((offerToEdit?.daysUntilExpiry ?: 45).toString()) }
    var quantityText by remember { mutableStateOf((offerToEdit?.availableQuantity ?: 50).toString()) }
    var mrpText by remember { mutableStateOf((offerToEdit?.mrp ?: 250.0).toInt().toString()) }
    var offerPriceText by remember { mutableStateOf((offerToEdit?.offerPrice ?: 150.0).toInt().toString()) }
    var moqText by remember { mutableStateOf((offerToEdit?.minimumOrderQuantity ?: 5).toString()) }
    var notes by remember { mutableStateOf(offerToEdit?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (offerToEdit == null) "নতুন অফার যোগ করুন (Add Offer)" else "অফার আপডেট করুন (Edit Offer)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Master Database Selection
                Text("১. ওষুধ নির্বাচন (Master Medicine DB):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoyalPharmaBlue)

                ExposedDropdownMenuBox(
                    expanded = expandedMasterDropdown,
                    onExpandedChange = { expandedMasterDropdown = !expandedMasterDropdown }
                ) {
                    OutlinedTextField(
                        value = if (medicineName.isNotBlank()) "$medicineName $strength ($companyName)" else "ডাটাবেজ থেকে সিলেক্ট করুন",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMasterDropdown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedMasterDropdown,
                        onDismissRequest = { expandedMasterDropdown = false }
                    ) {
                        masterMedicines.forEach { master ->
                            DropdownMenuItem(
                                text = { Text("${master.brandName} ${master.strength} - ${master.companyName}") },
                                onClick = {
                                    medicineName = master.brandName
                                    genericName = master.genericName
                                    strength = master.strength
                                    companyName = master.companyName
                                    form = master.form
                                    packSize = master.defaultPackSize
                                    mrpText = master.standardMrp.toInt().toString()
                                    expandedMasterDropdown = false
                                }
                            )
                        }
                    }
                }

                // Custom override inputs
                OutlinedTextField(
                    value = medicineName,
                    onValueChange = { medicineName = it },
                    label = { Text("ওষুধের নাম (Brand Name)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = genericName,
                        onValueChange = { genericName = it },
                        label = { Text("জেনেরিক নাম") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = strength,
                        onValueChange = { strength = it },
                        label = { Text("স্ট্রেংথ (e.g. 500mg)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = companyName,
                        onValueChange = { companyName = it },
                        label = { Text("কোম্পানি") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = packSize,
                        onValueChange = { packSize = it },
                        label = { Text("প্যাক সাইজ") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Text("২. ব্যাচ, মেয়াদ ও স্টক তথ্য:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoyalPharmaBlue)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = batchNumber,
                        onValueChange = { batchNumber = it },
                        label = { Text("ব্যাচ নং") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = { expiryDate = it },
                        label = { Text("মেয়াদ উত্তীর্ণের তারিখ") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = daysUntilExpiryText,
                        onValueChange = { daysUntilExpiryText = it },
                        label = { Text("মেয়াদ বাকি (দিন)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("মজুদ পরিমাণ (Box)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Text("৩. মূল্য এবং সর্বনিম্ন অর্ডার পরিমাণ (MOQ):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoyalPharmaBlue)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = mrpText,
                        onValueChange = { mrpText = it },
                        label = { Text("MRP (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = offerPriceText,
                        onValueChange = { offerPriceText = it },
                        label = { Text("অফার মূল্য (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                OutlinedTextField(
                    value = moqText,
                    onValueChange = { moqText = it },
                    label = { Text("সর্বনিম্ন বিক্রয় পরিমাণ / MOQ (Box)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("অতিরিক্ত নোট / শর্তাবলী") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val days = daysUntilExpiryText.toIntOrNull() ?: 30
                    val qty = quantityText.toIntOrNull() ?: 10
                    val mrpVal = mrpText.toDoubleOrNull() ?: 100.0
                    val offerVal = offerPriceText.toDoubleOrNull() ?: 80.0
                    val moqVal = moqText.toIntOrNull() ?: 1

                    if (medicineName.isNotBlank()) {
                        onSave(
                            medicineName,
                            genericName.ifBlank { "N/A" },
                            strength.ifBlank { "500mg" },
                            companyName.ifBlank { "Pharma" },
                            form,
                            packSize.ifBlank { "Box" },
                            batchNumber.ifBlank { "B-1" },
                            expiryDate,
                            days,
                            qty,
                            mrpVal,
                            offerVal,
                            moqVal,
                            notes
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalPharmaBlue),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (offerToEdit == null) "পাবলিশ করুন" else "সেভ করুন", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
