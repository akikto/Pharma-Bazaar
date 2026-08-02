package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LocalPharmacy
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.entities.ShopProfileEntity
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.BorderGray
import com.example.ui.theme.CardBorder
import com.example.ui.theme.PharmaBlueLight
import com.example.ui.theme.RoyalPharmaBlue
import com.example.ui.theme.SoftPaperGray
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UrgentRed
import com.example.ui.viewmodel.SellerAuthState

@Composable
fun ShopProfileScreen(
    activeShop: ShopProfileEntity,
    allShops: List<ShopProfileEntity>,
    onSwitchShop: (ShopProfileEntity) -> Unit,
    sellerAuthState: SellerAuthState = SellerAuthState(),
    onOpenAuthClick: () -> Unit = {},
    onSignOutClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SoftPaperGray)
            .verticalScroll(rememberScrollState())
    ) {
        // Header Banner
        Surface(
            color = RoyalPharmaBlue,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier.size(70.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.LocalPharmacy,
                            contentDescription = "Shop Profile",
                            tint = RoyalPharmaBlue,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = activeShop.shopName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = "Verified B2B",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "প্রোপ্রাইটর: ${activeShop.ownerName}",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Badge,
                            contentDescription = "License",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "DGDA ড্রাগ লাইসেন্স: ${activeShop.licenseNumber}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            // Stats Card
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Outlined.Star, contentDescription = "Rating", tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                            Text(" ${activeShop.rating}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("B2B রেটিং", fontSize = 11.sp, color = TextSecondary)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${activeShop.totalDealsCompleted}+", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                        Text("সম্পন্ন ডিল", fontSize = 11.sp, color = TextSecondary)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(shape = RoundedCornerShape(6.dp), color = EmeraldGreenLight) {
                            Text("ভেরিফাইড", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen, modifier = Modifier.padding(4.dp))
                        }
                        Text("B2B স্টেটাস", fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Firebase Authentication Card
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shop_auth_status_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "বিক্রেতা অ্যাকাউন্ট সাইন-ইন:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (sellerAuthState.isAuthenticated) EmeraldGreenLight else PharmaBlueLight
                        ) {
                            Text(
                                text = if (sellerAuthState.isAuthenticated) "✅ ভেরিফাইড সাইন-ইন" else "🔒 সাইন-ইন করা নেই",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (sellerAuthState.isAuthenticated) EmeraldGreen else RoyalPharmaBlue,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (sellerAuthState.isAuthenticated) {
                        Text(
                            text = "ইমেইল: ${sellerAuthState.userEmail ?: sellerAuthState.displayName ?: "Logged-in Seller"}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "মেথড: ${sellerAuthState.authMethod ?: "Firebase Auth"}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = onSignOutClick,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, UrgentRed),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = UrgentRed),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("shop_logout_button")
                        ) {
                            Text("সাইন আউট করুন (Sign Out)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text(
                            text = "পণ্য আপলোড, ডিল ম্যানেজমেন্ট ও অর্ডার রিকোয়েস্ট একসেপ্ট করতে ফায়ারবেস অথবা গুগল দিয়ে সাইন-ইন করুন।",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = onOpenAuthClick,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalPharmaBlue),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("open_seller_auth_button")
                        ) {
                            Text("বিক্রেতা লগইন / সাইন-আপ করুন (Seller Auth)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Address & Contact
            val context = LocalContext.current
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("ফার্মেসীর যোগাযোগের ঠিকানা:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Outlined.LocationOn, contentDescription = "Loc", tint = RoyalPharmaBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(activeShop.address, fontSize = 13.sp, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Outlined.Phone, contentDescription = "Phone", tint = RoyalPharmaBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(activeShop.phone, fontSize = 13.sp, color = TextPrimary)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BorderGray)

                    Text(
                        text = "💬 ফার্মেসীর সাথে সরাসরি যোগাযোগ (Contact Us):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Call Button
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${activeShop.phone}"))
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("shop_profile_call_button")
                        ) {
                            Icon(imageVector = Icons.Outlined.Call, contentDescription = "Call", modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("কল করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // WhatsApp Button
                        Button(
                            onClick = {
                                val cleanPhone = activeShop.phone.replace("+", "").replace("-", "").replace(" ", "")
                                val formattedPhone = if (cleanPhone.startsWith("0")) "88$cleanPhone" else cleanPhone
                                val waUrl = "https://wa.me/$formattedPhone?text=${Uri.encode("হাই ${activeShop.shopName}, আমি সরাসরি যোগাযোগ করতে চাই।")}"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl))
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(38.dp)
                                .testTag("shop_profile_wa_button")
                        ) {
                            Text("🟢 WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Switch Shop Demo Perspective Feature
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Outlined.SwapHoriz, contentDescription = "Switch", tint = RoyalPharmaBlue)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ফার্মেসী অ্যাকাউন্ট স্যুইচ করুন (Demo Store Switch):", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    allShops.forEach { shop ->
                        val isCurrent = shop.id == activeShop.id
                        OutlinedButton(
                            onClick = { onSwitchShop(shop) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isCurrent) Color(0xFFE0EDFF) else Color.Transparent
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${shop.shopName} (${shop.area})", fontSize = 13.sp, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal)
                                if (isCurrent) {
                                    Text("এক্টিভ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalPharmaBlue)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
