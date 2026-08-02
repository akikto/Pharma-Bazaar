package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.entities.BuyRequestEntity
import com.example.data.db.entities.ChatMessageEntity
import com.example.data.db.entities.ShopProfileEntity
import com.example.ui.components.PharmacyRequestStatusTracker
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.RoyalPharmaBlue
import com.example.ui.theme.SoftPaperGray
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun InAppChatScreen(
    activeShop: ShopProfileEntity,
    buyRequests: List<BuyRequestEntity>,
    selectedRequest: BuyRequestEntity?,
    chatMessages: List<ChatMessageEntity>,
    onSelectRequest: (BuyRequestEntity) -> Unit,
    onSendMessage: (String) -> Unit,
    onUpdateStatus: (requestId: Long, status: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var messageInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SoftPaperGray)
    ) {
        // Top Header with Quick Call / WhatsApp actions
        Surface(
            color = RoyalPharmaBlue,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "💬 যোগাযোগ ও বাই রিকোয়েস্ট",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "ইন-অ্যাপ চ্যাট, কল ও হোয়াটসঅ্যাপ লেনদেন",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Call Quick Button
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${activeShop.phone}"))
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.Call, contentDescription = "Call", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("কল", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // WhatsApp Quick Button
                    Button(
                        onClick = {
                            val cleanPhone = activeShop.phone.replace("+", "").replace("-", "").replace(" ", "")
                            val formattedPhone = if (cleanPhone.startsWith("0")) "88$cleanPhone" else cleanPhone
                            val waUrl = "https://wa.me/$formattedPhone?text=${Uri.encode("হাই, আমি ইন-অ্যাপ চ্যাট সম্পর্কিত বিষয়ে কথা বলতে চাই।")}"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl))
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("🟢 WA", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (buyRequests.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.Chat,
                        contentDescription = "No Chats",
                        tint = TextSecondary,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "এখনও কোনো একটিভ বাই রিকোয়েস্ট নেই",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "অফার ফিড থেকে Buy Request পাঠালে এখানে চ্যাট থ্রেড চালু হবে।",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Request selector list horizontal row
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(Color.White),
                    contentPadding = PaddingValues(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(buyRequests, key = { it.id }) { req ->
                        val isSelected = req.id == selectedRequest?.id
                        Card(
                            onClick = { onSelectRequest(req) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFFE0EDFF) else Color(0xFFF8FAFC)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = req.medicineName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "ক্রেতা: ${req.buyerShopName} • বিক্রেতা: ${req.sellerShopName}",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when (req.status) {
                                        "ACCEPTED" -> EmeraldGreenLight
                                        "REJECTED" -> Color(0xFFFEE2E2)
                                        else -> Color(0xFFFEF3C7)
                                    }
                                ) {
                                    Text(
                                        text = req.status,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFCBD5E1))

                // Chat Messages View Area
                if (selectedRequest == null) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("উপরে একটি বাই রিকোয়েস্ট সিলেক্ট করে মেসেজ দেখুন")
                    }
                } else {
                    Column(modifier = Modifier.weight(1f)) {
                        // Request Info Summary & Status Tracker
                        PharmacyRequestStatusTracker(
                            request = selectedRequest,
                            onUpdateStatus = onUpdateStatus,
                            isSupplierView = selectedRequest.sellerShopId == activeShop.id,
                            modifier = Modifier.padding(10.dp)
                        )

                        // Messages List
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            items(chatMessages, key = { it.id }) { msg ->
                                val isMe = msg.senderName == activeShop.shopName
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                                ) {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isMe) RoyalPharmaBlue else Color.White
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = msg.senderName,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isMe) Color.White.copy(alpha = 0.8f) else TextSecondary
                                            )
                                            Text(
                                                text = msg.messageText,
                                                fontSize = 13.sp,
                                                color = if (isMe) Color.White else TextPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Message Input Field
                        Surface(
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = messageInput,
                                    onValueChange = { messageInput = it },
                                    placeholder = { Text("বার্তা লিখুন...") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("chat_message_input"),
                                    shape = RoundedCornerShape(20.dp)
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                IconButton(
                                    onClick = {
                                        if (messageInput.isNotBlank()) {
                                            onSendMessage(messageInput)
                                            messageInput = ""
                                        }
                                    },
                                    modifier = Modifier.testTag("send_chat_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Send,
                                        contentDescription = "Send",
                                        tint = RoyalPharmaBlue
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
