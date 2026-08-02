package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoyalPharmaBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Bulk Action Bar for Supplier Order Management.
 * Allows suppliers to select multiple orders and update their status (e.g. 'Dispatched', 'Accepted', 'Delivered', 'Completed') in 1 click.
 */
@Composable
fun SupplierBulkActionBar(
    selectedCount: Int,
    totalSelectableCount: Int,
    isAllSelected: Boolean,
    onToggleSelectAll: () -> Unit,
    onClearSelection: () -> Unit,
    onApplyBulkStatus: (newStatus: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCustomStatusDialog by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = selectedCount > 0,
        enter = slideInVertically { it },
        exit = slideOutVertically { it }
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = RoyalPharmaBlue),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .testTag("supplier_bulk_action_bar")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Top Header: Selection count & Select All / Clear controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isAllSelected && totalSelectableCount > 0,
                                    onCheckedChange = { onToggleSelectAll() },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color.White,
                                        uncheckedColor = Color.White.copy(alpha = 0.7f),
                                        checkmarkColor = RoyalPharmaBlue
                                    ),
                                    modifier = Modifier
                                        .size(20.dp)
                                        .testTag("bulk_select_all_checkbox")
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "সব সিলেক্ট ($selectedCount/$totalSelectableCount)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$selectedCount টি অর্ডার নির্বাচিত",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = onClearSelection,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("clear_bulk_selection_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Clear Selection",
                                tint = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons Row: 1-click status update buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // 1. Mark as Dispatched
                    Button(
                        onClick = { onApplyBulkStatus("DISPATCHED") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = RoyalPharmaBlue
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("bulk_status_dispatched_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocalShipping,
                            contentDescription = "Dispatched",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ডিচপ্যাচড 🚚",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // 2. Mark as Accepted
                    Button(
                        onClick = { onApplyBulkStatus("ACCEPTED") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDBEAFE),
                            contentColor = RoyalPharmaBlue
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("bulk_status_accepted_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = "Accept",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "গৃহীত ✅",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // 3. Mark as Delivered / Completed
                    Button(
                        onClick = { onApplyBulkStatus("DELIVERED") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .weight(1.1f)
                            .testTag("bulk_status_delivered_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = "Delivered",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "পৌঁছেছে 🎉",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // 4. Custom Status Dialog Trigger
                    OutlinedButton(
                        onClick = { showCustomStatusDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("bulk_status_custom_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MoreHoriz,
                            contentDescription = "More Statuses",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    // Custom Status Picker Modal Dialog
    if (showCustomStatusDialog) {
        var selectedStatusOption by remember { mutableStateOf("DISPATCHED") }

        val statusOptions = listOf(
            "ACCEPTED" to "✅ Accepted (অর্ডার গ্রহণ)",
            "DISPATCHED" to "🚚 Dispatched (পণ্য প্রেরিত/ডিচপ্যাচড)",
            "DELIVERED" to "🎉 Delivered (ফার্মেসীতে ডেলিভারড)",
            "COMPLETED" to "🏁 Completed (সম্পূর্ণ সফল লেনদেন)",
            "PENDING" to "⏳ Pending (পেন্ডিং স্ট্যাটাসে ফেরত)",
            "REJECTED" to "❌ Rejected (অর্ডার বাতিল করুন)"
        )

        AlertDialog(
            onDismissRequest = { showCustomStatusDialog = false },
            title = {
                Text(
                    text = "⚙️ বাল্ক স্ট্যাটাস নির্বাচন করুন ($selectedCount টি অর্ডার)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "নির্বাচিত $selectedCount টি অর্ডারের স্ট্যাটাস একই সাথে পরিবর্তন করতে নিচে যেকোনো একটি নির্বাচন করুন:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.size(8.dp))

                    statusOptions.forEach { (code, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            RadioButton(
                                selected = selectedStatusOption == code,
                                onClick = { selectedStatusOption = code }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (selectedStatusOption == code) FontWeight.Bold else FontWeight.Normal,
                                color = TextPrimary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCustomStatusDialog = false
                        onApplyBulkStatus(selectedStatusOption)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalPharmaBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_custom_bulk_status_btn")
                ) {
                    Text("একক ক্লিকে প্রয়োগ করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomStatusDialog = false }) {
                    Text("বাতিল", color = TextSecondary)
                }
            }
        )
    }
}
