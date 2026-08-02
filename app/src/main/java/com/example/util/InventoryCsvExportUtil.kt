package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.example.data.db.entities.OfferListingEntity
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class InventoryExportSummary(
    val totalItems: Int,
    val totalBoxes: Int,
    val totalStockValueBdt: Double,
    val activeCount: Int,
    val pausedCount: Int,
    val soldOutCount: Int,
    val csvFileName: String,
    val filePath: String
)

object InventoryCsvExportUtil {

    private const val TAG = "InventoryCsvExportUtil"

    fun generateCsvText(offers: List<OfferListingEntity>): String {
        val sb = StringBuilder()

        // CSV Header
        val headers = listOf(
            "Listing ID",
            "Medicine Name",
            "Generic Name",
            "Company",
            "Form",
            "Strength",
            "Batch Number",
            "Expiry Date",
            "Available Quantity (Boxes)",
            "Min Order Quantity",
            "Offer Price (BDT)",
            "MRP (BDT)",
            "Discount %",
            "Status",
            "Seller Shop Name",
            "License Number",
            "Seller Location"
        )
        sb.append(headers.joinToString(",") { escapeCsvCell(it) }).append("\n")

        // Rows
        for (offer in offers) {
            val row = listOf(
                offer.id.toString(),
                offer.medicineName,
                offer.genericName,
                offer.companyName,
                offer.form,
                offer.strength,
                offer.batchNumber,
                offer.expiryDate,
                offer.availableQuantity.toString(),
                offer.minimumOrderQuantity.toString(),
                offer.offerPrice.toString(),
                offer.mrp.toString(),
                offer.discountPercent.toString(),
                offer.status,
                offer.sellerShopName,
                if (offer.isVerifiedShop) "ভেরিফাইড দোকান" else "সাধারণ দোকান",
                offer.sellerLocation
            )
            sb.append(row.joinToString(",") { escapeCsvCell(it) }).append("\n")
        }

        return sb.toString()
    }

    private fun escapeCsvCell(value: String): String {
        val needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")
        val escaped = value.replace("\"", "\"\"")
        return if (needsQuotes) {
            "\"$escaped\""
        } else {
            escaped
        }
    }

    fun exportAndShareInventoryCsv(
        context: Context,
        offers: List<OfferListingEntity>,
        shopName: String
    ): InventoryExportSummary? {
        if (offers.isEmpty()) {
            Log.w(TAG, "No inventory offers to export.")
            return null
        }

        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val sanitizedShopName = shopName.replace(Regex("[^a-zA-Z0-9_]"), "_").lowercase()
            val fileName = "pharmabazaar_inventory_${sanitizedShopName}_$timeStamp.csv"

            val cacheDir = File(context.cacheDir, "csv_exports")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }

            val file = File(cacheDir, fileName)
            val csvText = generateCsvText(offers)

            // Write UTF-8 BOM so Excel/Sheets handles Bengali & special characters correctly
            FileOutputStream(file).use { fos ->
                OutputStreamWriter(fos, Charsets.UTF_8).use { writer ->
                    writer.write("\uFEFF") // UTF-8 BOM
                    writer.write(csvText)
                    writer.flush()
                }
            }

            val authority = "${context.packageName}.fileprovider"
            val contentUri: Uri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "PharmaBazaar Inventory CSV - $shopName")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "PharmaBazaar B2B Stock Inventory CSV Export\n" +
                            "দোকান: $shopName\n" +
                            "মোট আইটেম: ${offers.size} টি\n" +
                            "তারিখ: ${SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())}"
                )
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "ইনভেন্টরি সিএসভি ডাউনলোড ও শেয়ার করুন")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

            val totalBoxes = offers.sumOf { it.availableQuantity }
            val totalValue = offers.sumOf { it.offerPrice * it.availableQuantity }
            val activeCount = offers.count { it.status == "ACTIVE" }
            val pausedCount = offers.count { it.status == "PAUSED" }
            val soldOutCount = offers.count { it.status == "SOLD_OUT" }

            return InventoryExportSummary(
                totalItems = offers.size,
                totalBoxes = totalBoxes,
                totalStockValueBdt = totalValue,
                activeCount = activeCount,
                pausedCount = pausedCount,
                soldOutCount = soldOutCount,
                csvFileName = fileName,
                filePath = file.absolutePath
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export inventory CSV: ${e.message}", e)
            return null
        }
    }
}
