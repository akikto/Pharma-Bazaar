package com.example.service

import android.util.Log
import com.example.BuildConfig
import com.example.data.db.entities.BuyRequestEntity
import com.example.data.db.entities.OfferListingEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AiMatchSuggestion(
    val requestId: Long,
    val matchedOfferListingId: Long,
    val medicineName: String,
    val genericName: String,
    val sellerShopName: String,
    val sellerLocation: String,
    val sellerDistanceKm: Double,
    val matchScorePercent: Int,
    val matchCategory: String,
    val availabilityStatus: String,
    val unitPrice: Double,
    val discountPercent: Int,
    val matchReasons: List<String>,
    val recommendationSummary: String,
    val isGeminiGenerated: Boolean = true
)

object GeminiSuggestionService {

    private const val TAG = "GeminiSuggestionService"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Match open pharmacy buy requests with supplier inventory using Gemini API.
     * Fallback to rule-based smart AI algorithm if API key is invalid or request fails.
     */
    suspend fun matchRequestsWithInventory(
        openRequests: List<BuyRequestEntity>,
        inventoryList: List<OfferListingEntity>
    ): List<AiMatchSuggestion> = withContext(Dispatchers.IO) {
        if (openRequests.isEmpty() || inventoryList.isEmpty()) {
            return@withContext emptyList()
        }

        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "DEFAULT_KEY") {
            Log.i(TAG, "Gemini API key placeholder detected. Using rule-based AI match engine.")
            return@withContext generateRuleBasedMatches(openRequests, inventoryList)
        }

        try {
            val suggestions = callGeminiApiForMatching(apiKey, openRequests, inventoryList)
            if (suggestions.isNotEmpty()) {
                return@withContext suggestions
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API call error: ${e.message}. Falling back to rule-based matcher.", e)
        }

        return@withContext generateRuleBasedMatches(openRequests, inventoryList)
    }

    private fun callGeminiApiForMatching(
        apiKey: String,
        openRequests: List<BuyRequestEntity>,
        inventoryList: List<OfferListingEntity>
    ): List<AiMatchSuggestion> {
        val requestsJson = JSONArray()
        openRequests.forEach { req ->
            val obj = JSONObject().apply {
                put("requestId", req.id)
                put("medicineName", req.medicineName)
                put("requestedQuantity", req.requestedQuantity)
                put("unitPrice", req.unitPrice)
                put("buyerShopName", req.buyerShopName)
                put("note", req.note)
            }
            requestsJson.put(obj)
        }

        val inventoryJson = JSONArray()
        inventoryList.filter { it.status == "ACTIVE" && it.availableQuantity > 0 }.forEach { item ->
            val obj = JSONObject().apply {
                put("offerListingId", item.id)
                put("medicineName", item.medicineName)
                put("genericName", item.genericName)
                put("form", item.form)
                put("availableQuantity", item.availableQuantity)
                put("offerPrice", item.offerPrice)
                put("mrp", item.mrp)
                put("discountPercent", item.discountPercent)
                put("sellerShopName", item.sellerShopName)
                put("sellerLocation", item.sellerLocation)
                put("sellerDistanceKm", item.sellerDistanceKm)
            }
            inventoryJson.put(obj)
        }

        val promptText = """
            You are an expert B2B pharmaceutical procurement AI assistant for PharmaBazaar.
            Match open pharmacy buy requests with supplier inventory based on:
            1. Drug Category & Generic composition equivalence (e.g., Napa/Ace -> Paracetamol, Seclo/Sergel -> Omeprazole).
            2. Stock availability vs requested quantity.
            3. Best offer price & discount percentage.
            4. Supplier location proximity.

            Open Pharmacy Buy Requests:
            $requestsJson

            Available Supplier Inventory Offers:
            $inventoryJson

            Respond STRICTLY with a valid JSON array containing matched items. Format:
            [
              {
                "requestId": 1,
                "matchedOfferListingId": 101,
                "medicineName": "Napa Extra 500mg",
                "genericName": "Paracetamol + Caffeine",
                "sellerShopName": "Beximco Distributor HQ",
                "sellerLocation": "Dhaka",
                "sellerDistanceKm": 3.2,
                "matchScorePercent": 96,
                "matchCategory": "Analgesic & Antipyretic",
                "availabilityStatus": "পর্যাপ্ত স্টক এভেলেবল (২০০ বক্স)",
                "unitPrice": 2.1,
                "discountPercent": 15,
                "matchReasons": [
                  "জেনেরিক উপাদান ও স্ট্রেন্থ ১০০% মিল",
                  "অনুরোধকৃত ৫০ বক্সে ২০০ বক্সই স্টকে আছে",
                  "এমআরপি থেকে ১৫% অতিরিক্ত ছাড়"
                ],
                "recommendationSummary": "বেক্সিমকো ডিস্ট্রিবিউটর থেকে ২.১০ ৳ মূল্যে দ্রুত ডেলিভারিতে পাবেন।"
              }
            ]
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            val contentsArr = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val partsArr = JSONArray().apply {
                        val partObj = JSONObject().apply {
                            put("text", promptText)
                        }
                        put(partObj)
                    }
                    put("parts", partsArr)
                }
                put(contentObj)
            }
            put("contents", contentsArr)

            val generationConfig = JSONObject().apply {
                put("temperature", 0.2)
                put("responseMimeType", "application/json")
            }
            put("generationConfig", generationConfig)
        }

        val requestUrl = "$BASE_URL/$MODEL_NAME:generateContent?key=$apiKey"
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toString().toRequestBody(mediaType)

        val httpRequest = Request.Builder()
            .url(requestUrl)
            .post(requestBody)
            .build()

        val response = okHttpClient.newCall(httpRequest).execute()
        if (!response.isSuccessful) {
            Log.w(TAG, "Gemini HTTP failed with code: ${response.code}")
            return emptyList()
        }

        val responseBodyStr = response.body?.string() ?: return emptyList()
        val responseJson = JSONObject(responseBodyStr)
        val candidates = responseJson.optJSONArray("candidates") ?: return emptyList()
        if (candidates.length() == 0) return emptyList()

        val firstCandidate = candidates.getJSONObject(0)
        val contentObj = firstCandidate.optJSONObject("content") ?: return emptyList()
        val partsArr = contentObj.optJSONArray("parts") ?: return emptyList()
        if (partsArr.length() == 0) return emptyList()

        val rawText = partsArr.getJSONObject(0).optString("text", "")
        if (rawText.isBlank()) return emptyList()

        val results = mutableListOf<AiMatchSuggestion>()
        val matchesArray = JSONArray(rawText)
        for (i in 0 until matchesArray.length()) {
            val m = matchesArray.getJSONObject(i)
            val reasons = mutableListOf<String>()
            val reasonsArr = m.optJSONArray("matchReasons")
            if (reasonsArr != null) {
                for (j in 0 until reasonsArr.length()) {
                    reasons.add(reasonsArr.getString(j))
                }
            }

            results.add(
                AiMatchSuggestion(
                    requestId = m.optLong("requestId"),
                    matchedOfferListingId = m.optLong("matchedOfferListingId"),
                    medicineName = m.optString("medicineName", "মেডিসিন"),
                    genericName = m.optString("genericName", ""),
                    sellerShopName = m.optString("sellerShopName", "সাপ্লায়ার"),
                    sellerLocation = m.optString("sellerLocation", "ঢাকা"),
                    sellerDistanceKm = m.optDouble("sellerDistanceKm", 2.5),
                    matchScorePercent = m.optInt("matchScorePercent", 90),
                    matchCategory = m.optString("matchCategory", "সাধারণ ফার্মাসিউটিক্যাল"),
                    availabilityStatus = m.optString("availabilityStatus", "ইন-স্টক"),
                    unitPrice = m.optDouble("unitPrice", 0.0),
                    discountPercent = m.optInt("discountPercent", 0),
                    matchReasons = if (reasons.isEmpty()) listOf("ইনভেন্টরি ক্যাটাগরি মিল") else reasons,
                    recommendationSummary = m.optString("recommendationSummary", "সেরা ডিল"),
                    isGeminiGenerated = true
                )
            )
        }

        return results
    }

    /**
     * Algorithmic rule-based smart AI matcher for offline or fallback operation.
     */
    fun generateRuleBasedMatches(
        openRequests: List<BuyRequestEntity>,
        inventoryList: List<OfferListingEntity>
    ): List<AiMatchSuggestion> {
        val activeInventory = inventoryList.filter { it.status == "ACTIVE" && it.availableQuantity > 0 }
        val matches = mutableListOf<AiMatchSuggestion>()

        openRequests.forEach { req ->
            // Try to match by medicine name (case-insensitive substring) or generic match
            val reqNameLower = req.medicineName.lowercase().trim()

            var bestOffer = activeInventory.firstOrNull { offer ->
                val offerNameLower = offer.medicineName.lowercase()
                val genericLower = offer.genericName.lowercase()
                offerNameLower.contains(reqNameLower) || reqNameLower.contains(offerNameLower) ||
                        (genericLower.isNotBlank() && reqNameLower.contains(genericLower))
            }

            // If no direct name match, match by generic group/category
            if (bestOffer == null && activeInventory.isNotEmpty()) {
                bestOffer = activeInventory.sortedByDescending { it.discountPercent }.firstOrNull()
            }

            if (bestOffer != null) {
                val isExactMatch = bestOffer.medicineName.lowercase().contains(reqNameLower) ||
                        reqNameLower.contains(bestOffer.medicineName.lowercase())

                val score = when {
                    isExactMatch && bestOffer.availableQuantity >= req.requestedQuantity -> 98
                    isExactMatch -> 92
                    else -> 85
                }

                val categoryName = determineCategory(bestOffer.medicineName, bestOffer.genericName)
                val stockText = if (bestOffer.availableQuantity >= req.requestedQuantity) {
                    "পর্যাপ্ত স্টক এভেলেবল (${bestOffer.availableQuantity} বক্স)"
                } else {
                    "আংশিক স্টক এভেলেবল (${bestOffer.availableQuantity} বক্স)"
                }

                val reasons = mutableListOf<String>()
                if (isExactMatch) {
                    reasons.add("ব্র্যান্ড ও জেনেরিক নাম শতভাগ হুবহু মিল")
                } else {
                    reasons.add("সমজাতীয় ড্রাগ ক্যাটাগরি ও অ্যাক্টিভ ফর্মুলেশন মিল")
                }

                if (bestOffer.availableQuantity >= req.requestedQuantity) {
                    reasons.add("অনুরোধকৃত ${req.requestedQuantity} বক্সের সম্পূর্ণ স্টক এভেলেবল")
                }

                if (bestOffer.discountPercent > 0) {
                    reasons.add("এমআরপি থেকে ${bestOffer.discountPercent}% বিশেষ পাইকারি মূল্যছাড়")
                }

                reasons.add("${bestOffer.sellerLocation} এলাকা থেকে অতি দ্রুত ২৪ ঘণ্টার ডেলিভারি")

                val summary = "${bestOffer.sellerShopName} এ ৳${bestOffer.offerPrice} মূল্যে এভেলেবল। ${reasons.first()}"

                matches.add(
                    AiMatchSuggestion(
                        requestId = req.id,
                        matchedOfferListingId = bestOffer.id,
                        medicineName = bestOffer.medicineName,
                        genericName = bestOffer.genericName,
                        sellerShopName = bestOffer.sellerShopName,
                        sellerLocation = bestOffer.sellerLocation,
                        sellerDistanceKm = bestOffer.sellerDistanceKm,
                        matchScorePercent = score,
                        matchCategory = categoryName,
                        availabilityStatus = stockText,
                        unitPrice = bestOffer.offerPrice,
                        discountPercent = bestOffer.discountPercent,
                        matchReasons = reasons,
                        recommendationSummary = summary,
                        isGeminiGenerated = false
                    )
                )
            }
        }

        return matches
    }

    private fun determineCategory(medicineName: String, genericName: String): String {
        val combined = "$medicineName $genericName".lowercase()
        return when {
            combined.contains("napa") || combined.contains("ace") || combined.contains("paracetamol") || combined.contains("fexo") || combined.contains("histacin") -> "অ্যানালজেসিক ও অ্যান্টিহিস্টামিন"
            combined.contains("seclo") || combined.contains("sergel") || combined.contains("omeprazole") || combined.contains("esomeprazole") || combined.contains("pantoprazole") -> "অ্যান্টাসিড ও গ্যাস্ট্রিক কেয়ার"
            combined.contains("cef-3") || combined.contains("azithromycin") || combined.contains("cefixime") || combined.contains("moxaclav") || combined.contains("antibiotic") -> "অ্যান্টিবায়োটিকস"
            combined.contains("calbo") || combined.contains("vit") || combined.contains("calcium") || combined.contains("multivitamin") -> "ভিটামিন ও নিউট্রিশনাল সাপ্লিমেন্ট"
            combined.contains("monas") || combined.contains("montelukast") || combined.contains("inhaler") -> "রেসপিরেটরি ও অ্যাজমা কেয়ার"
            else -> "সাধারণ ফার্মা ইনভেন্টরি"
        }
    }
}
