package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.ExpiryAmber
import com.example.ui.theme.PharmaBlueLight
import com.example.ui.theme.RoyalPharmaBlue
import com.example.ui.theme.SoftPaperGray
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UrgentRed
import com.example.ui.theme.UrgentRedLight
import java.util.Locale

/**
 * Historical Data Point model for medicine price trend chart
 */
data class PricePoint(
    val monthLabel: String,
    val offerPrice: Double,
    val mrp: Double,
    val averageMarketPrice: Double,
    val sellerShopName: String
)

/**
 * Time range filter options for the chart
 */
enum class TrendTimeRange(val labelBn: String, val pointCount: Int) {
    ONE_MONTH("১ মাস", 4),
    THREE_MONTHS("৩ মাস", 6),
    SIX_MONTHS("৬ মাস", 8),
    ONE_YEAR("১ বছর", 12)
}

/**
 * Generates realistic historical price trend data based on current offer parameters.
 */
fun generateHistoricalPriceData(
    medicineName: String,
    mrp: Double,
    currentOfferPrice: Double,
    timeRange: TrendTimeRange
): List<PricePoint> {
    val months = listOf(
        "সেপ্টেম্বর ২৫", "অক্টোবর ২৫", "নভেম্বর ২৫", "ডিসেম্বর ২৫",
        "জানুয়ারি ২৬", "ফেব্রুয়ারি ২৬", "মার্চ ২৬", "এপ্রিল ২৬",
        "মে ২৬", "জুন ২৬", "জুলাই ২৬", "আগস্ট ২৬"
    )

    val count = timeRange.pointCount.coerceAtMost(months.size)
    val startIndex = months.size - count
    val seed = (medicineName.hashCode() and 0x7FFFFFFF) % 100

    val points = mutableListOf<PricePoint>()
    for (i in 0 until count) {
        val monthStr = months[startIndex + i]
        // Create realistic fluctuations relative to MRP & current price
        val randomFactor = ((seed + i * 7) % 15 - 7) / 100.0 // -7% to +7%
        val marketAvg = (mrp * 0.72) * (1.0 + randomFactor)
        val offerVal = if (i == count - 1) currentOfferPrice else marketAvg * (0.92 + ((i % 3) * 0.03))

        points.add(
            PricePoint(
                monthLabel = monthStr,
                offerPrice = String.format(Locale.US, "%.1f", offerVal).toDouble(),
                mrp = mrp,
                averageMarketPrice = String.format(Locale.US, "%.1f", marketAvg).toDouble(),
                sellerShopName = if (i % 2 == 0) "মেসার্স রয়াল ফার্মা" else "পপুলার মেডিসিন ডিস্ট্রিবিউটর"
            )
        )
    }
    return points
}

/**
 * Complete Recharts-inspired Data Visualization Component for Medicine Historical Price Trends.
 */
@Composable
fun MedicinePriceTrendVisualizer(
    medicineName: String,
    genericName: String = "",
    mrp: Double,
    currentOfferPrice: Double,
    modifier: Modifier = Modifier
) {
    var selectedTimeRange by remember { mutableStateOf(TrendTimeRange.SIX_MONTHS) }
    var selectedPointIndex by remember { mutableIntStateOf(-1) }

    val dataPoints = remember(medicineName, mrp, currentOfferPrice, selectedTimeRange) {
        generateHistoricalPriceData(medicineName, mrp, currentOfferPrice, selectedTimeRange)
    }

    val minPrice = dataPoints.minOfOrNull { it.offerPrice } ?: currentOfferPrice
    val maxPrice = dataPoints.maxOfOrNull { it.mrp } ?: mrp
    val avgPrice = if (dataPoints.isNotEmpty()) dataPoints.map { it.offerPrice }.average() else currentOfferPrice

    val priceSavingsPct = if (mrp > 0) ((mrp - currentOfferPrice) / mrp * 100).toInt() else 0
    val trendVsAvg = if (avgPrice > 0) ((currentOfferPrice - avgPrice) / avgPrice * 100) else 0.0

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("medicine_price_trend_visualizer")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // --- Title & Medicine Info ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = PharmaBlueLight,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Analytics,
                                contentDescription = null,
                                tint = RoyalPharmaBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "📊 ঐতিহাসিক মূল্য ট্রেন্ড ও অ্যানালিটিক্স",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "$medicineName ${if (genericName.isNotBlank()) "($genericName)" else ""}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // --- Time Range Selector ---
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("trend_time_range_segmented")
            ) {
                TrendTimeRange.entries.forEachIndexed { index, range ->
                    SegmentedButton(
                        selected = selectedTimeRange == range,
                        onClick = {
                            selectedTimeRange = range
                            selectedPointIndex = -1
                        },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = TrendTimeRange.entries.size
                        )
                    ) {
                        Text(
                            text = range.labelBn,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // --- Key Metrics Overview Chips ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricSummaryChip(
                    title = "সর্বনিম্ন দাম",
                    value = "৳${minPrice.toInt()}",
                    color = EmeraldGreen,
                    bgColor = EmeraldGreenLight,
                    icon = Icons.Outlined.TrendingDown,
                    modifier = Modifier.weight(1f)
                )
                MetricSummaryChip(
                    title = "গড় পাইকারি",
                    value = "৳${avgPrice.toInt()}",
                    color = RoyalPharmaBlue,
                    bgColor = PharmaBlueLight,
                    icon = Icons.Outlined.ShowChart,
                    modifier = Modifier.weight(1f)
                )
                MetricSummaryChip(
                    title = "এমআরপি (MRP)",
                    value = "৳${mrp.toInt()}",
                    color = UrgentRed,
                    bgColor = UrgentRedLight,
                    icon = Icons.Outlined.TrendingUp,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Interactive Recharts-Style Line Chart Canvas ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFFF8FAFC), shape = RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFE2E8F0), shape = RoundedCornerShape(12.dp))
                    .padding(12.dp)
                    .testTag("price_trend_chart_canvas")
            ) {
                val activePoint = if (selectedPointIndex in dataPoints.indices) dataPoints[selectedPointIndex] else null

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(176.dp)
                        .pointerInput(dataPoints) {
                            detectTapGestures { tapOffset ->
                                val width = size.width
                                val stepX = width / (dataPoints.size - 1).coerceAtLeast(1)
                                val closestIndex = ((tapOffset.x + stepX / 2) / stepX).toInt()
                                    .coerceIn(0, dataPoints.size - 1)
                                selectedPointIndex = closestIndex
                            }
                        }
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val bottomPadding = 28f
                    val topPadding = 16f
                    val chartHeight = canvasHeight - bottomPadding - topPadding

                    val priceMin = (minPrice * 0.85).toFloat().coerceAtLeast(0f)
                    val priceMax = (maxPrice * 1.05).toFloat()
                    val priceRange = (priceMax - priceMin).coerceAtLeast(1f)

                    fun getY(price: Double): Float {
                        val norm = (price.toFloat() - priceMin) / priceRange
                        return canvasHeight - bottomPadding - (norm * chartHeight)
                    }

                    fun getX(index: Int): Float {
                        if (dataPoints.size <= 1) return canvasWidth / 2
                        return index * (canvasWidth / (dataPoints.size - 1))
                    }

                    // 1. Draw Grid Lines & Y-Axis Baseline
                    val gridLineCount = 3
                    val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    for (i in 0..gridLineCount) {
                        val gridY = topPadding + (i * chartHeight / gridLineCount)
                        drawLine(
                            color = Color(0xFFE2E8F0),
                            start = Offset(0f, gridY),
                            end = Offset(canvasWidth, gridY),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = dashPathEffect
                        )
                    }

                    // 2. MRP Target Line (Dashed Red Line)
                    val mrpY = getY(mrp)
                    drawLine(
                        color = UrgentRed.copy(alpha = 0.5f),
                        start = Offset(0f, mrpY),
                        end = Offset(canvasWidth, mrpY),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = dashPathEffect
                    )

                    // 3. Construct Path & Area Gradient Fill for Offer Prices
                    val offerPath = Path()
                    val areaPath = Path()

                    dataPoints.forEachIndexed { index, point ->
                        val x = getX(index)
                        val y = getY(point.offerPrice)

                        if (index == 0) {
                            offerPath.moveTo(x, y)
                            areaPath.moveTo(x, canvasHeight - bottomPadding)
                            areaPath.lineTo(x, y)
                        } else {
                            val prevX = getX(index - 1)
                            val prevY = getY(dataPoints[index - 1].offerPrice)
                            val controlX1 = prevX + (x - prevX) / 2
                            val controlX2 = prevX + (x - prevX) / 2

                            offerPath.cubicTo(controlX1, prevY, controlX2, y, x, y)
                            areaPath.cubicTo(controlX1, prevY, controlX2, y, x, y)
                        }

                        if (index == dataPoints.size - 1) {
                            areaPath.lineTo(x, canvasHeight - bottomPadding)
                            areaPath.close()
                        }
                    }

                    // Draw Gradient Fill Area
                    drawPath(
                        path = areaPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                RoyalPharmaBlue.copy(alpha = 0.30f),
                                RoyalPharmaBlue.copy(alpha = 0.02f)
                            ),
                            startY = topPadding,
                            endY = canvasHeight - bottomPadding
                        )
                    )

                    // Draw Main Trend Spline Line
                    drawPath(
                        path = offerPath,
                        color = RoyalPharmaBlue,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // 4. Draw Data Point Circles & Highlight Lines
                    dataPoints.forEachIndexed { index, point ->
                        val x = getX(index)
                        val y = getY(point.offerPrice)
                        val isSelected = index == selectedPointIndex

                        if (isSelected) {
                            // Vertical dashed guide line
                            drawLine(
                                color = RoyalPharmaBlue.copy(alpha = 0.6f),
                                start = Offset(x, topPadding),
                                end = Offset(x, canvasHeight - bottomPadding),
                                strokeWidth = 1.5.dp.toPx(),
                                pathEffect = dashPathEffect
                            )
                            // Outer glow ring
                            drawCircle(
                                color = RoyalPharmaBlue.copy(alpha = 0.25f),
                                radius = 10.dp.toPx(),
                                center = Offset(x, y)
                            )
                            drawCircle(
                                color = RoyalPharmaBlue,
                                radius = 6.dp.toPx(),
                                center = Offset(x, y)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 3.dp.toPx(),
                                center = Offset(x, y)
                            )
                        } else {
                            drawCircle(
                                color = Color.White,
                                radius = 4.5.dp.toPx(),
                                center = Offset(x, y)
                            )
                            drawCircle(
                                color = RoyalPharmaBlue,
                                radius = 4.5.dp.toPx(),
                                style = Stroke(width = 2.dp.toPx()),
                                center = Offset(x, y)
                            )
                        }

                        // Draw X-Axis Month Label using native Canvas
                        val labelText = point.monthLabel.split(" ").firstOrNull() ?: point.monthLabel
                        drawContext.canvas.nativeCanvas.drawText(
                            labelText,
                            x,
                            canvasHeight - 6f,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.parseColor("#64748B")
                                textSize = 26f
                                textAlign = android.graphics.Paint.Align.CENTER
                                isAntiAlias = true
                                isFakeBoldText = isSelected
                            }
                        )
                    }
                }

                // Interactive Tooltip Card Overlay when a data point is tapped
                if (activePoint != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = RoyalPharmaBlue,
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 4.dp)
                            .testTag("chart_active_point_tooltip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "📅 ${activePoint.monthLabel}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "|",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "অফার: ৳${activePoint.offerPrice.toInt()}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF86EFAC)
                            )
                            Text(
                                text = "(MRP: ৳${activePoint.mrp.toInt()})",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- AI Purchasing Decision Guidance Banner ---
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (trendVsAvg <= 0) EmeraldGreenLight else UrgentRedLight,
                border = BorderStroke(
                    1.dp,
                    if (trendVsAvg <= 0) Color(0xFFA7F3D0) else Color(0xFFFCA5A5)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_purchase_decision_banner")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (trendVsAvg <= 0) "💡" else "⚠️",
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (trendVsAvg <= 0) "ক্রয় সিদ্ধান্ত পরামর্শ: অনুকূল ক্রয় সময় (Best Buy Window)" else "ক্রয় সিদ্ধান্ত পরামর্শ: বাজার দর অপেক্ষা সামান্য বেশি",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (trendVsAvg <= 0) EmeraldGreen else UrgentRed
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (trendVsAvg <= 0)
                                "বর্তমান পাইকারি মূল্য (৳${currentOfferPrice.toInt()}) গত $selectedTimeRange-এর গড় দামের চেয়ে ${Math.abs(trendVsAvg).toInt()}% কম (MRP থেকে $priceSavingsPct% ছাড়)! এখন ফার্মেসির জন্য মজুদ করা লাভজনক।"
                            else
                                "বর্তমান পাইকারি মূল্য গত সময়ের গড়ের চেয়ে কিছুটা বেশি। জরুরী প্রয়োজন ব্যতীত পরিমাণ সীমিত রাখা বিবেচনা করুন।",
                            fontSize = 11.sp,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricSummaryChip(
    title: String,
    value: String,
    color: Color,
    bgColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = title,
                    fontSize = 10.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

/**
 * Fullscreen or Dialog Wrapper to launch the Medicine Price Trend Chart from anywhere in the app.
 */
@Composable
fun MedicinePriceTrendDialog(
    medicineName: String,
    genericName: String = "",
    mrp: Double,
    currentOfferPrice: Double,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SoftPaperGray,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Analytics,
                            contentDescription = null,
                            tint = RoyalPharmaBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "মূল্য ট্রেন্ড অ্যানালিটিক্স",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalPharmaBlue
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "বন্ধ করুন"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                MedicinePriceTrendVisualizer(
                    medicineName = medicineName,
                    genericName = genericName,
                    mrp = mrp,
                    currentOfferPrice = currentOfferPrice
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalPharmaBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ঠিক আছে", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
