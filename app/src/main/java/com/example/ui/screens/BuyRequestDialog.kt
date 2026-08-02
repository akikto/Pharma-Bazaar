package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.entities.OfferListingEntity
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoyalPharmaBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun BuyRequestDialog(
    offer: OfferListingEntity,
    onDismiss: () -> Unit,
    onSubmitRequest: (quantity: Int, note: String) -> Unit,
    onAddToCart: (quantity: Int) -> Unit
) {
    var quantity by remember { mutableIntStateOf(offer.minimumOrderQuantity.coerceAtLeast(1)) }
    var note by remember { mutableStateOf("") }

    val totalPrice = offer.offerPrice * quantity

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "বাই রিকোয়েস্ট পাঠান (Buy Request)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${offer.medicineName} ${offer.strength}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RoyalPharmaBlue
                )
                Text(
                    text = "বিক্রেতা: ${offer.sellerShopName} (${offer.sellerLocation})",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("একক অফার মূল্য:", fontSize = 12.sp, color = TextSecondary)
                            Text("৳${offer.offerPrice.toInt()}/বক্স", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("মোট মজুদ স্টক:", fontSize = 12.sp, color = TextSecondary)
                            Text("${offer.availableQuantity} বক্স", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("সর্বনিম্ন ক্রয় (MOQ):", fontSize = 12.sp, color = TextSecondary)
                            Text("${offer.minimumOrderQuantity} বক্স", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoyalPharmaBlue)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quantity Selector (Partial Buying feature)
                Text(
                    text = "আপনার ক্রয়ের পরিমাণ (Quantity):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = {
                            if (quantity > offer.minimumOrderQuantity) quantity--
                        },
                        enabled = quantity > offer.minimumOrderQuantity
                    ) {
                        Icon(imageVector = Icons.Outlined.Remove, contentDescription = "Decrease")
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = "$quantity বক্স",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            if (quantity < offer.availableQuantity) quantity++
                        },
                        enabled = quantity < offer.availableQuantity
                    ) {
                        Icon(imageVector = Icons.Outlined.Add, contentDescription = "Increase")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Note Input
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("অতিরিক্ত নোট / শর্তাবলী (ঐচ্ছিক)") },
                    placeholder = { Text("যেমন: ক্যাশ অন পিকআপ বা ডেলিভারি টাইম") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("buy_request_note_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Contact Bar inside dialog
                val context = LocalContext.current
                val phoneNum = "01711223344"

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💬 যোগাযোগ:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNum"))
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.Call, contentDescription = "Call", modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("কল", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val cleanPhone = phoneNum.replace("+", "").replace("-", "").replace(" ", "")
                            val formattedPhone = if (cleanPhone.startsWith("0")) "88$cleanPhone" else cleanPhone
                            val waUrl = "https://wa.me/$formattedPhone?text=${Uri.encode("হাই, আমি ${offer.medicineName} কেনা সংক্রান্ত বিষয়ে কথা বলতে চাই।")}"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl))
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(32.dp)
                    ) {
                        Text("🟢 WA", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Total Summary
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("সর্বমোট আনুমানিক মূল্য:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "৳${totalPrice.toInt()}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen
                    )
                }
            }
        },
        confirmButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { onSubmitRequest(quantity, note) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_buy_request_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalPharmaBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("রিকোয়েস্ট কনফার্ম করুন", color = Color.White, fontWeight = FontWeight.Bold)
                }

                TextButton(
                    onClick = {
                        onAddToCart(quantity)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Outlined.ShoppingCart, contentDescription = "Add Cart", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("শুধু কার্টে যোগ করুন (Add to Cart)")
                }
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
