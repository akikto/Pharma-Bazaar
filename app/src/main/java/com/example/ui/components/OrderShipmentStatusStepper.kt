package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.db.entities.BuyRequestEntity
import com.example.ui.theme.BorderGray
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.ExpiryAmber
import com.example.ui.theme.PharmaBlueLight
import com.example.ui.theme.RoyalPharmaBlue
import com.example.ui.theme.SoftPaperGray
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Ordered Shipment Status Sequence as specified:
 * Confirmed -> Packed -> Dispatched -> In-Transit -> Delivered
 */
enum class OrderShipmentStep(
    val code: String,
    val titleBn: String,
    val subtitleBn: String,
    val icon: ImageVector,
    val stepIndex: Int
) {
    CONFIRMED("CONFIRMED", "কনফার্মড", "অর্ডার গৃহীত হয়েছে", Icons.Outlined.CheckCircle, 0),
    PACKED("PACKED", "প্যাকড", "প্যাকেজিং সম্পন্ন", Icons.Outlined.Inventory2, 1),
    DISPATCHED("DISPATCHED", "ডিসপ্যাচড", "কুরিয়ারে হস্তান্তর", Icons.Outlined.LocalShipping, 2),
    IN_TRANSIT("IN_TRANSIT", "ইন-ট্রানজিট", "পরিবহনে চলমান", Icons.Outlined.LocalShipping, 3),
    DELIVERED("DELIVERED", "ডেলিভার্ড", "গন্তব্যে পৌছেছে", Icons.Outlined.Verified, 4);

    companion object {
        fun fromStatusCode(code: String): OrderShipmentStep {
            return when (code.uppercase()) {
                "CONFIRMED", "ACCEPTED", "PENDING" -> CONFIRMED
                "PACKED" -> PACKED
                "DISPATCHED" -> DISPATCHED
                "IN_TRANSIT", "IN-TRANSIT" -> IN_TRANSIT
                "DELIVERED", "COMPLETED" -> DELIVERED
                else -> CONFIRMED
            }
        }
    }
}

/**
 * Animated Progress Stepper Component for displaying shipment progress.
 * Features pulse scale animations, horizontal truck translation, and status transition lines.
 */
@Composable
fun OrderShipmentStatusStepper(
    currentStatus: String,
    onUpdateStatus: ((String) -> Unit)? = null,
    isSupplierView: Boolean = false,
    orderId: Long = 1001L,
    medicineName: String = "Sergel 20 mg",
    supplierName: String = "সান ফার্মা সেন্ট্রাল ডিপো",
    pharmacyName: String = "আন্ধেরি মেডিপ্লাস ফার্মেসী",
    orderTimestamp: Long = System.currentTimeMillis(),
    modifier: Modifier = Modifier
) {
    val currentStep = OrderShipmentStep.fromStatusCode(currentStatus)
    val isCancelled = currentStatus.equals("REJECTED", ignoreCase = true) || currentStatus.equals("CANCELLED", ignoreCase = true)
    var showDetailTimeline by remember { mutableStateOf(true) }
    var showLiveMapTracker by remember { mutableStateOf(false) }

    // Stepper Animation Drivers
    val infiniteTransition = rememberInfiniteTransition(label = "stepperAnimations")
    
    // Scale pulse animation for current active step
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Motion offset for In-Transit / Dispatched truck icon
    val truckOffset by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "truckOffset"
    )

    val overallProgress = ((currentStep.stepIndex + 1).toFloat() / OrderShipmentStep.entries.size.toFloat())
    val animatedProgress by animateFloatAsState(
        targetValue = if (isCancelled) 0f else overallProgress,
        animationSpec = tween(600),
        label = "overallProgress"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, BorderGray),
        modifier = modifier
            .fillMaxWidth()
            .testTag("order_shipment_status_stepper_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // --- Top Status Header ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = PharmaBlueLight,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = currentStep.icon,
                                contentDescription = null,
                                tint = RoyalPharmaBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "🚚 শিপমেন্ট ট্র্যাকিং স্টেটাস",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (isCancelled) "স্ট্যাটাস: বাতিল" else "বর্তমান অবস্থা: ${currentStep.titleBn} (${(animatedProgress * 100).toInt()}% সম্পন্ন)",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when {
                        isCancelled -> Color(0xFFFEE2E2)
                        currentStep == OrderShipmentStep.DELIVERED -> EmeraldGreenLight
                        else -> Color(0xFFEFF6FF)
                    }
                ) {
                    Text(
                        text = if (isCancelled) "❌ বাতিল" else "● ${currentStep.titleBn}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isCancelled -> Color(0xFFDC2626)
                            currentStep == OrderShipmentStep.DELIVERED -> EmeraldGreen
                            else -> RoyalPharmaBlue
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Overall Progress Bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (currentStep == OrderShipmentStep.DELIVERED) EmeraldGreen else RoyalPharmaBlue,
                trackColor = Color(0xFFE2E8F0)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Visual 5-Step Progress Stepper Row ---
            if (isCancelled) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF2F2),
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("❌", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "এই অর্ডারটির শিপমেন্ট বাতিল বা প্রত্যাখ্যান করা হয়েছে।",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF991B1B)
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    OrderShipmentStep.entries.forEachIndexed { index, step ->
                        val isCompleted = currentStep.stepIndex > step.stepIndex
                        val isCurrent = currentStep == step

                        val stepBgColor by animateColorAsState(
                            targetValue = when {
                                isCurrent -> RoyalPharmaBlue
                                isCompleted -> EmeraldGreen
                                else -> Color(0xFFF1F5F9)
                            },
                            animationSpec = tween(400),
                            label = "stepBgColor"
                        )

                        val stepIconTint by animateColorAsState(
                            targetValue = when {
                                isCurrent || isCompleted -> Color.White
                                else -> TextSecondary
                            },
                            label = "stepIconTint"
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Animated Icon Container Circle
                            Box(contentAlignment = Alignment.Center) {
                                if (isCurrent) {
                                    // Outer Glowing Aura Ring
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .graphicsLayer {
                                                scaleX = pulseScale
                                                scaleY = pulseScale
                                            }
                                            .background(RoyalPharmaBlue.copy(alpha = 0.25f), shape = CircleShape)
                                    )
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = stepBgColor,
                                    shadowElevation = if (isCurrent) 4.dp else 0.dp,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        val iconModifier = if (isCurrent && (step == OrderShipmentStep.IN_TRANSIT || step == OrderShipmentStep.DISPATCHED)) {
                                            Modifier
                                                .size(16.dp)
                                                .offset(x = truckOffset.dp)
                                        } else {
                                            Modifier.size(16.dp)
                                        }

                                        Icon(
                                            imageVector = if (isCompleted) Icons.Default.Check else step.icon,
                                            contentDescription = step.titleBn,
                                            tint = stepIconTint,
                                            modifier = iconModifier
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = step.titleBn,
                                fontSize = 11.sp,
                                fontWeight = if (isCurrent) FontWeight.ExtraBold else if (isCompleted) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCurrent) RoyalPharmaBlue else if (isCompleted) EmeraldGreen else TextSecondary,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = step.subtitleBn,
                                fontSize = 9.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        // Connecting Animated Line between steps
                        if (index < OrderShipmentStep.entries.size - 1) {
                            val lineActive = currentStep.stepIndex > index
                            val lineColor by animateColorAsState(
                                targetValue = if (lineActive) EmeraldGreen else Color(0xFFE2E8F0),
                                animationSpec = tween(400),
                                label = "lineColor"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(0.4f)
                                    .height(3.dp)
                                    .padding(top = 15.dp)
                                    .background(lineColor, shape = RoundedCornerShape(2.dp))
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // --- Supplier / Seller Interactive Status Advancer Controls ---
            if (isSupplierView && !isCancelled && onUpdateStatus != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "⚡ সাপ্লায়ার শিপমেন্ট স্ট্যাটাস পরিবর্তন করুন (Real-Time Push)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalPharmaBlue
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OrderShipmentStep.entries.forEach { step ->
                                val isSelected = currentStep == step
                                OutlinedButton(
                                    onClick = { onUpdateStatus(step.code) },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) RoyalPharmaBlue else BorderGray
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isSelected) RoyalPharmaBlue else Color.White,
                                        contentColor = if (isSelected) Color.White else TextPrimary
                                    ),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(32.dp)
                                        .testTag("shipment_step_btn_${step.code}")
                                ) {
                                    Text(
                                        text = step.titleBn,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Button to toggle Live GPS Route Map & Delivery ETA Tracker
            Button(
                onClick = { showLiveMapTracker = !showLiveMapTracker },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (showLiveMapTracker) RoyalPharmaBlue else Color(0xFFEFF6FF),
                    contentColor = if (showLiveMapTracker) Color.White else RoyalPharmaBlue
                ),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, RoyalPharmaBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("toggle_live_map_tracker_btn")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "Live Map",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (showLiveMapTracker) "🗺️ লাইভ জিপিএস রুট ম্যাপ লুকান" else "🗺️ লাইভ ম্যাপ ট্র্যাকিং ও আনুমানিক ডেলিভারি সময় (ETA) দেখুন",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            AnimatedVisibility(visible = showLiveMapTracker) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    PharmacyShipmentMapTrackerComponent(
                        trackingInfo = ShipmentTrackingInfo(
                            trackingId = "TRK-$orderId",
                            orderId = orderId,
                            medicineName = medicineName,
                            supplierName = supplierName,
                            warehouseAddress = "সেন্ট্রাল লজিস্টিকস হাব, আন্ধেরি ইস্ট, মুম্বাই",
                            pharmacyName = pharmacyName,
                            deliveryAddress = "লিঙ্কিং রোড, বান্দ্রা, মুম্বাই",
                            currentStatus = currentStatus,
                            orderTimestamp = orderTimestamp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Toggle Expandable Timeline Details
            TextButton(
                onClick = { showDetailTimeline = !showDetailTimeline },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .testTag("toggle_shipment_timeline_btn")
            ) {
                Text(
                    text = if (showDetailTimeline) "▼ শিপমেন্ট টাইমলাইন বিবরণ লুকান" else "▲ বিস্তৃত শিপমেন্ট সময়সূচী দেখুন",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoyalPharmaBlue
                )
            }

            AnimatedVisibility(
                visible = showDetailTimeline,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    Text(
                        text = "📅 শিপমেন্ট ট্র্যাকিং ইতিহাস ও টাইমলাইন",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    OrderShipmentStep.entries.forEach { step ->
                        val isPassed = currentStep.stepIndex >= step.stepIndex
                        val isCurrent = currentStep == step

                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isPassed) (if (isCurrent) RoyalPharmaBlue else EmeraldGreen) else Color(0xFFE2E8F0),
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(top = 2.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isPassed) Icons.Default.Check else step.icon,
                                        contentDescription = null,
                                        tint = if (isPassed) Color.White else TextSecondary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = step.titleBn,
                                    fontSize = 12.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isPassed) TextPrimary else TextSecondary
                                )
                                Text(
                                    text = when (step) {
                                        OrderShipmentStep.CONFIRMED -> "সাপ্লায়ার ওষুধ রিকোয়েস্ট গ্রহণ ও কনফার্ম করেছেন"
                                        OrderShipmentStep.PACKED -> "ওষুধের ব্যাচ নম্বর ও মেয়াদের তারিখ চেক করে কোল্ড-চেইন প্যাক সম্পন্ন"
                                        OrderShipmentStep.DISPATCHED -> "ফার্মাপার্সেল কুরিয়ার সার্ভিস হাব-এ পার্সেল স্ক্যান ও হ্যান্ডওভার"
                                        OrderShipmentStep.IN_TRANSIT -> "ডেলিভারি রাইডার আপনার ফার্মেসীর উদ্দেশ্যে রওনা দিয়েছেন"
                                        OrderShipmentStep.DELIVERED -> "ফার্মেসীতে সফলভাবে ইনভয়েস সহ ডেলিভারি সম্পন্ন"
                                    },
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dialog displaying full order details along with the visual progress stepper UI.
 */
@Composable
fun OrderDetailsTrackingDialog(
    order: BuyRequestEntity,
    onDismiss: () -> Unit,
    onUpdateStatus: (String) -> Unit,
    isSupplierView: Boolean = false
) {
    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault()) }
    val formattedDate = dateFormat.format(Date(order.timestamp))

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SoftPaperGray,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.ReceiptLong,
                            contentDescription = null,
                            tint = RoyalPharmaBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "অর্ডার বিস্তারিত ও শিপমেন্ট ট্র্যাকিং",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "আইডি: #BUY-${order.id} • $formattedDate",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Item summary
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, BorderGray),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(order.medicineName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("পরিমাণ: ${order.requestedQuantity} বক্স", fontSize = 12.sp, color = TextSecondary)
                            Text("ফার্মেসী: ${order.buyerShopName} | সাপ্লায়ার: ${order.sellerShopName}", fontSize = 11.sp, color = TextSecondary)
                        }
                        Text(
                            text = "₹${order.totalPrice.toInt()}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Visual Progress Stepper & Live Map Tracker
                OrderShipmentStatusStepper(
                    currentStatus = order.status,
                    onUpdateStatus = onUpdateStatus,
                    isSupplierView = isSupplierView,
                    orderId = order.id,
                    medicineName = order.medicineName,
                    supplierName = if (order.sellerShopName.isNotBlank()) order.sellerShopName else "সাপ্লায়ার সেন্ট্রাল ডিপো",
                    pharmacyName = if (order.buyerShopName.isNotBlank()) order.buyerShopName else "ফার্মেসী স্টোর",
                    orderTimestamp = order.timestamp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalPharmaBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("বন্ধ করুন", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
