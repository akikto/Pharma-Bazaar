package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.PendingActions
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.entities.BuyRequestEntity
import com.example.ui.theme.BorderGray
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.ExpiryAmber
import com.example.ui.theme.PharmaBlueLight
import com.example.ui.theme.RoyalPharmaBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Status step definition for Pharmacy Request tracking flow.
 */
enum class RequestStatusStep(
    val code: String,
    val titleBn: String,
    val descriptionBn: String,
    val icon: ImageVector,
    val stepIndex: Int
) {
    PENDING("PENDING", "অপেক্ষমাণ", "রিকোয়েস্ট জমা হয়েছে", Icons.Outlined.PendingActions, 0),
    DISPATCHED("DISPATCHED", "প্রেরিত", "সরবরাহের জন্য রওনা হয়েছে", Icons.Outlined.LocalShipping, 1),
    DELIVERED("DELIVERED", "পৌঁছেছে", "ডেলিভারি সম্পন্ন হয়েছে", Icons.Outlined.CheckCircle, 2);

    companion object {
        fun fromCode(code: String): RequestStatusStep {
            return when (code.uppercase()) {
                "DISPATCHED" -> DISPATCHED
                "DELIVERED" -> DELIVERED
                "ACCEPTED" -> DISPATCHED // map legacy accepted to dispatched
                else -> PENDING
            }
        }
    }
}

/**
 * Status Tracking component for Pharmacy Requests.
 * Allows suppliers to update the state from 'Pending' to 'Dispatched' or 'Delivered' within Firestore.
 */
@Composable
fun PharmacyRequestStatusTracker(
    request: BuyRequestEntity,
    onUpdateStatus: (requestId: Long, newStatus: String) -> Unit,
    isSupplierView: Boolean = true,
    isSelected: Boolean = false,
    onSelectToggle: ((Boolean) -> Unit)? = null,
    isBulkMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val currentStep = RequestStatusStep.fromCode(request.status)
    val isCancelledOrRejected = request.status.equals("REJECTED", ignoreCase = true) || request.status.equals("CANCELLED", ignoreCase = true)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pharmacy_request_status_tracker_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFEFF6FF) else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) RoyalPharmaBlue else BorderGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // --- Header: Title, Order ID & Firestore Live Sync Badge ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onSelectToggle != null) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onSelectToggle(it) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = RoyalPharmaBlue
                            ),
                            modifier = Modifier.testTag("order_select_checkbox_${request.id}")
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = PharmaBlueLight,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.ReceiptLong,
                                contentDescription = "Request Tracking",
                                tint = RoyalPharmaBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "ফার্মেসী অর্ডার ট্র্যাকিং",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "রিকোয়েস্ট আইডি: #${request.id} • ${request.medicineName}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Cloud Firestore Live Sync Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFEFF6FF),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CloudSync,
                            contentDescription = "Firestore Synced",
                            tint = RoyalPharmaBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "ফায়ারস্টোর লাইভ",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalPharmaBlue
                        )
                    }
                }
            }

            HorizontalDivider(color = BorderGray.copy(alpha = 0.6f))

            // --- Order Summary Grid ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "পরিমাণ: ${request.requestedQuantity} বক্স/প্যাক",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "ক্রেতা: ${request.buyerShopName}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "বিক্রেতা: ${request.sellerShopName}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "৳${request.totalPrice.toInt()}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = RoyalPharmaBlue
                    )
                    Text(
                        text = "একক মূল্য: ৳${request.unitPrice.toInt()}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            // --- Visual Progress Stepper UI ---
            OrderShipmentStatusStepper(
                currentStatus = request.status,
                onUpdateStatus = { newStatus -> onUpdateStatus(request.id, newStatus) },
                isSupplierView = isSupplierView
            )

            // --- Supplier / Seller Status Update Controls ---
            if (isSupplierView) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Storefront,
                                    contentDescription = "Supplier Action",
                                    tint = RoyalPharmaBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "সরবরাহকারী অ্যাকশন (FCM রিয়েল-টাইম পুশ নোটিফিকেশন)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when (request.status.uppercase()) {
                                    "DISPATCHED" -> Color(0xFFDBEAFE)
                                    "DELIVERED" -> EmeraldGreenLight
                                    else -> Color(0xFFFEF3C7)
                                }
                            ) {
                                Text(
                                    text = "বর্তমান: ${currentStep.titleBn}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (request.status.uppercase()) {
                                        "DISPATCHED" -> RoyalPharmaBlue
                                        "DELIVERED" -> EmeraldGreen
                                        else -> Color(0xFF92400E)
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // State transition action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Pending Button
                            OutlinedButton(
                                onClick = { onUpdateStatus(request.id, "PENDING") },
                                enabled = request.status != "PENDING",
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (request.status == "PENDING") ExpiryAmber else BorderGray
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFB45309)
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("status_pending_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.PendingActions,
                                    contentDescription = "Pending",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("অপেক্ষমাণ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Dispatched Button
                            Button(
                                onClick = { onUpdateStatus(request.id, "DISPATCHED") },
                                enabled = request.status != "DISPATCHED",
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RoyalPharmaBlue,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .weight(1.1f)
                                    .height(38.dp)
                                    .testTag("status_dispatched_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.LocalShipping,
                                    contentDescription = "Dispatched",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ডিসপ্যাচড 🚚", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Delivered Button
                            Button(
                                onClick = { onUpdateStatus(request.id, "DELIVERED") },
                                enabled = request.status != "DELIVERED",
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmeraldGreen,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .weight(1.1f)
                                    .height(38.dp)
                                    .testTag("status_delivered_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = "Delivered",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("পৌঁছেছে ✅", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
