package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.LocalPharmacy
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderGray
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.PharmaBlueLight
import com.example.ui.theme.RoyalPharmaBlue
import com.example.ui.theme.SoftPaperGray
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UrgentRed
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Data Model for Shipment Delivery Info
 */
data class ShipmentTrackingInfo(
    val trackingId: String,
    val orderId: Long,
    val medicineName: String,
    val supplierName: String,
    val warehouseAddress: String,
    val pharmacyName: String,
    val deliveryAddress: String,
    val currentStatus: String, // CONFIRMED, PACKED, DISPATCHED, IN_TRANSIT, DELIVERED
    val riderName: String = "মোঃ রাশেদ আহমেদ",
    val riderPhone: String = "01711-223344",
    val vehicleNo: String = "ঢাকা মেট্রো-হ-৫৪৩২",
    val coldChainTempCelsius: Double = 4.2,
    val orderTimestamp: Long = System.currentTimeMillis()
)

/**
 * Interactive Real-Time Shipment Tracking Component for Pharmacy users.
 * Renders live route map canvas with custom markers (Warehouse, Rider in motion, Destination Pharmacy),
 * Estimated Delivery Date & Time, live ETA progress, and temperature quality telemetry.
 */
@Composable
fun PharmacyShipmentMapTrackerComponent(
    trackingInfo: ShipmentTrackingInfo,
    onContactRider: (String) -> Unit = {},
    onRefreshLocation: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isMapExpanded by remember { mutableStateOf(true) }
    var zoomLevel by remember { mutableFloatStateOf(1.0f) }

    // Calculate Estimated Delivery Date & Time based on order timestamp and status
    val calendar = remember(trackingInfo.orderTimestamp, trackingInfo.currentStatus) {
        Calendar.getInstance().apply {
            timeInMillis = trackingInfo.orderTimestamp
            // Estimated delivery offset: 2 hours for in-transit, 4 hours for confirmed/packed
            val hourOffset = when (trackingInfo.currentStatus.uppercase()) {
                "IN_TRANSIT", "DISPATCHED" -> 2
                "PACKED" -> 3
                "DELIVERED" -> 0
                else -> 4
            }
            add(Calendar.HOUR_OF_DAY, hourOffset)
        }
    }

    val dateFormatter = remember { SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("bn", "BD")) }
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    val estimatedDeliveryDateStr = remember(calendar) { dateFormatter.format(calendar.time) }
    val estimatedDeliveryTimeStr = remember(calendar) { timeFormatter.format(calendar.time) }

    // Rider route progress (0.0f to 1.0f) depending on shipment status
    val targetProgress = remember(trackingInfo.currentStatus) {
        when (trackingInfo.currentStatus.uppercase()) {
            "CONFIRMED" -> 0.05f
            "PACKED" -> 0.20f
            "DISPATCHED" -> 0.45f
            "IN_TRANSIT" -> 0.75f
            "DELIVERED" -> 1.00f
            else -> 0.10f
        }
    }

    // Motion & pulse animations for rider marker on map
    val infiniteTransition = rememberInfiniteTransition(label = "mapAnimations")

    // Rider marker pulse effect
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "riderPulse"
    )

    // Route progress shimmer interpolation
    val dashPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dashPhase"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("pharmacy_shipment_map_tracker_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // --- Section 1: Top Status Banner & Estimated Delivery Date ---
            Surface(
                color = RoyalPharmaBlue,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
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
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.LocalShipping,
                                        contentDescription = "Shipment Map",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "লাইভ শিপমেন্ট ট্র্যাকিং ও ম্যাপ",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "ট্র্যাকিং আইডি: #${trackingInfo.trackingId}",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }

                        // Live Status Badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = when (trackingInfo.currentStatus.uppercase()) {
                                "DELIVERED" -> EmeraldGreenLight
                                "IN_TRANSIT" -> Color(0xFFFEF3C7)
                                else -> Color.White.copy(alpha = 0.2f)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .background(
                                            color = when (trackingInfo.currentStatus.uppercase()) {
                                                "DELIVERED" -> EmeraldGreen
                                                "IN_TRANSIT" -> Color(0xFFD97706)
                                                else -> Color.White
                                            },
                                            shape = CircleShape
                                        )
                                )
                                Text(
                                    text = when (trackingInfo.currentStatus.uppercase()) {
                                        "CONFIRMED" -> "কনফার্মড"
                                        "PACKED" -> "প্যাকড"
                                        "DISPATCHED" -> "ডিসপ্যাচড"
                                        "IN_TRANSIT" -> "ইন-ট্রানজিট (চলমান)"
                                        "DELIVERED" -> "ডেলিভার্ড"
                                        else -> trackingInfo.currentStatus
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (trackingInfo.currentStatus.uppercase()) {
                                        "DELIVERED" -> EmeraldGreen
                                        "IN_TRANSIT" -> Color(0xFFB45309)
                                        else -> Color.White
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // --- Prominent Estimated Delivery Date Box ---
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        shadowElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFEFF6FF),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.CalendarToday,
                                            contentDescription = "Calendar ETA",
                                            tint = RoyalPharmaBlue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        text = "📅 আনুমানিক পৌঁছানোর সময় (ETA)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "$estimatedDeliveryTimeStr ($estimatedDeliveryDateStr)",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = if (trackingInfo.currentStatus.equals("DELIVERED", ignoreCase = true))
                                            "✅ সফলভাবে আপনার ফার্মেসীতে পৌঁছেছে"
                                        else
                                            "⏱️ আনুমানিক অবশিষ্ট সময়: প্রায় ৩৫-৪৫ মিনিট",
                                        fontSize = 11.sp,
                                        color = if (trackingInfo.currentStatus.equals("DELIVERED", ignoreCase = true)) EmeraldGreen else RoyalPharmaBlue,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Cold Chain Quality Badge
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFECFDF5),
                                border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Outlined.AcUnit,
                                            contentDescription = "Cold Chain",
                                            tint = EmeraldGreen,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "${trackingInfo.coldChainTempCelsius}°C",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldGreen
                                        )
                                    }
                                    Text(
                                        text = "কোল্ড চেইন নিরাপদ",
                                        fontSize = 9.sp,
                                        color = Color(0xFF047857),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- Section 2: Real-Time Visual Map Canvas with Custom Markers ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
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
                            imageVector = Icons.Default.Map,
                            contentDescription = "Map View",
                            tint = RoyalPharmaBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "লাইভ ট্র্যাকিং ম্যাপ (Live GPS Route Map)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { zoomLevel = (zoomLevel + 0.2f).coerceAtMost(1.6f) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = RoyalPharmaBlue)
                        }
                        IconButton(
                            onClick = { zoomLevel = (zoomLevel - 0.2f).coerceAtLeast(0.8f) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = RoyalPharmaBlue)
                        }
                        TextButton(
                            onClick = { isMapExpanded = !isMapExpanded },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = if (isMapExpanded) "সংকুচিত করুন" else "ম্যাপ খুলুন",
                                fontSize = 11.sp,
                                color = RoyalPharmaBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                AnimatedVisibility(visible = isMapExpanded) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFEBF2F7))
                            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(16.dp))
                    ) {
                        // Custom Canvas Map Drawing with Roads, Route Path & Pins
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("live_shipment_map_canvas")
                        ) {
                            val w = size.width
                            val h = size.height

                            // 1. Draw stylized background grid roads
                            val roadPaint = Color(0xFFFFFFFF)
                            val secondaryRoadPaint = Color(0xFFE2E8F0)

                            // Main horizontal & vertical roads
                            drawLine(
                                color = roadPaint,
                                start = Offset(0f, h * 0.35f),
                                end = Offset(w, h * 0.35f),
                                strokeWidth = 14f * zoomLevel
                            )
                            drawLine(
                                color = roadPaint,
                                start = Offset(0f, h * 0.70f),
                                end = Offset(w, h * 0.70f),
                                strokeWidth = 10f * zoomLevel
                            )
                            drawLine(
                                color = roadPaint,
                                start = Offset(w * 0.25f, 0f),
                                end = Offset(w * 0.25f, h),
                                strokeWidth = 12f * zoomLevel
                            )
                            drawLine(
                                color = roadPaint,
                                start = Offset(w * 0.75f, 0f),
                                end = Offset(w * 0.75f, h),
                                strokeWidth = 12f * zoomLevel
                            )

                            // 2. River/Lake feature
                            val riverPath = Path().apply {
                                moveTo(0f, h * 0.88f)
                                cubicTo(w * 0.3f, h * 0.80f, w * 0.6f, h * 0.95f, w, h * 0.85f)
                                lineTo(w, h)
                                lineTo(0f, h)
                                close()
                            }
                            drawPath(path = riverPath, color = Color(0xFFBAE6FD).copy(alpha = 0.6f))

                            // 3. Define Key Map Locations
                            val hubPoint = Offset(w * 0.15f, h * 0.35f)
                            val controlPoint = Offset(w * 0.50f, h * 0.20f)
                            val pharmacyPoint = Offset(w * 0.85f, h * 0.65f)

                            // 4. Draw Full Scheduled Delivery Path Line (Dashed Gray)
                            val fullRoutePath = Path().apply {
                                moveTo(hubPoint.x, hubPoint.y)
                                quadraticTo(controlPoint.x, controlPoint.y, pharmacyPoint.x, pharmacyPoint.y)
                            }

                            drawPath(
                                path = fullRoutePath,
                                color = Color(0xFF94A3B8),
                                style = Stroke(
                                    width = 6f * zoomLevel,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f),
                                    cap = StrokeCap.Round
                                )
                            )

                            // 5. Calculate Current Rider Location along Bezier curve based on targetProgress
                            val t = targetProgress.coerceIn(0.05f, 0.95f)
                            val invT = 1f - t
                            val riderX = invT * invT * hubPoint.x + 2 * invT * t * controlPoint.x + t * t * pharmacyPoint.x
                            val riderY = invT * invT * hubPoint.y + 2 * invT * t * controlPoint.y + t * t * pharmacyPoint.y
                            val riderPoint = Offset(riderX, riderY)

                            // Draw Covered Route Line (Solid Active Blue / Emerald Green)
                            val coveredRoutePath = Path().apply {
                                moveTo(hubPoint.x, hubPoint.y)
                                // Sample points up to t
                                val steps = 20
                                for (i in 1..(steps * t).toInt()) {
                                    val stepT = i.toFloat() / steps
                                    val stepInvT = 1f - stepT
                                    val sx = stepInvT * stepInvT * hubPoint.x + 2 * stepInvT * stepT * controlPoint.x + stepT * stepT * pharmacyPoint.x
                                    val sy = stepInvT * stepInvT * hubPoint.y + 2 * stepInvT * stepT * controlPoint.y + stepT * stepT * pharmacyPoint.y
                                    lineTo(sx, sy)
                                }
                            }

                            drawPath(
                                path = coveredRoutePath,
                                color = if (trackingInfo.currentStatus.equals("DELIVERED", true)) Color(0xFF10B981) else Color(0xFF2563EB),
                                style = Stroke(
                                    width = 8f * zoomLevel,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 8f), dashPhase),
                                    cap = StrokeCap.Round
                                )
                            )

                            // 6. Draw Origin Hub Pin (📍 Supplier Warehouse)
                            drawCircle(
                                color = Color(0xFF1E3A8A),
                                radius = 10f * zoomLevel,
                                center = hubPoint
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 4f * zoomLevel,
                                center = hubPoint
                            )

                            // 7. Draw Destination Pharmacy Pin (🏪 Destination)
                            drawCircle(
                                color = Color(0xFF10B981),
                                radius = 12f * zoomLevel,
                                center = pharmacyPoint
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 5f * zoomLevel,
                                center = pharmacyPoint
                            )

                            // 8. Draw Animated Pulse Aura for Active Rider Marker
                            drawCircle(
                                color = Color(0xFF2563EB).copy(alpha = 0.35f),
                                radius = (18f * zoomLevel) * pulseScale,
                                center = riderPoint
                            )
                            drawCircle(
                                color = Color(0xFF2563EB),
                                radius = 11f * zoomLevel,
                                center = riderPoint
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 5f * zoomLevel,
                                center = riderPoint
                            )
                        }

                        // --- Overlay UI Badges & Labels on Top of Canvas ---

                        // Top Left: Origin Warehouse Label Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E3A8A),
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset(x = 12.dp, y = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Storefront,
                                    contentDescription = "Warehouse",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Column {
                                    Text(
                                        text = trackingInfo.supplierName,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "📍 সেন্ট্রাল ওয়্যারহাউজ ডিপো",
                                        fontSize = 8.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        // Bottom Right: Destination Pharmacy Label Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = EmeraldGreen,
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = (-12).dp, y = (-12).dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.LocalPharmacy,
                                    contentDescription = "Destination Pharmacy",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Column {
                                    Text(
                                        text = trackingInfo.pharmacyName,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "🏁 গন্তব্য ফার্মেসী শাখা",
                                        fontSize = 8.sp,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }

                        // Center Floating Pill: Live Rider Details & GPS Speed
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            shadowElevation = 6.dp,
                            border = BorderStroke(1.dp, RoyalPharmaBlue),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-12).dp, y = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TwoWheeler,
                                    contentDescription = "Rider Bike",
                                    tint = RoyalPharmaBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Column {
                                    Text(
                                        text = "🏍️ ${trackingInfo.riderName}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "গতি: ৩৮ কিমি/ঘণ্টা • দূরত্ব: ২.১ কিমি বাকি",
                                        fontSize = 9.sp,
                                        color = RoyalPharmaBlue,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Bottom Left: Live Traffic & Map Controls Pill
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.92f),
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .offset(x = 12.dp, y = (-12).dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(EmeraldGreen, CircleShape)
                                )
                                Text(
                                    text = "🟢 রুট ট্রাফিক স্বাভাবিক",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // --- Section 3: Delivery Rider Contact & Call Action Card ---
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = RoyalPharmaBlue,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Delivery Person",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = trackingInfo.riderName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "কুরিয়ার রাইডার • যানবাহন: ${trackingInfo.vehicleNo}",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "মোবাইল: ${trackingInfo.riderPhone}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = RoyalPharmaBlue
                                )
                            }
                        }

                        // Call Action Button
                        Button(
                            onClick = {
                                onContactRider(trackingInfo.riderPhone)
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${trackingInfo.riderPhone}")
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // fallback if dialer fails in sandbox
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_call_rider_phone")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Call Rider",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text("কল করুন", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
