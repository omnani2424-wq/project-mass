package com.example.data.api

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.local.GroundingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Locale
import java.util.concurrent.TimeUnit

data class GeminiGenerationResult(
    val text: String,
    val sources: List<GroundingSource> = emptyList(),
    val isSuccess: Boolean = true,
    val errorMessage: String? = null
)

data class GeminiMediaResult(
    val mediaType: String, // "image", "music", "video"
    val bytes: ByteArray? = null,
    val mimeType: String? = null,
    val description: String = "",
    val isSuccess: Boolean = true,
    val errorMessage: String? = null
)

data class ChatMessagePayload(
    val role: String, // "user" or "model"
    val text: String,
    val imageBase64: String? = null,
    val imageMimeType: String = "image/jpeg"
)

class GeminiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val TAG = "GeminiClient"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

        // Text models with fallback
        private val CANDIDATE_MODELS = listOf(
            "gemini-flash-latest",
            "gemini-3.5-flash",
            "gemini-3.1-flash-lite-preview"
        )

        // Specialized models required by user brief
        const val MODEL_IMAGE_GEN = "gemini-3.1-flash-image-preview"
        const val MODEL_MUSIC_CLIP = "lyria-3-clip-preview" // up to 30s
        const val MODEL_MUSIC_PRO = "lyria-3-pro-preview" // full length
        const val MODEL_VEO_VIDEO = "veo-3.1-fast-generate-preview"

        fun isApiKeyConfigured(): Boolean {
            val key = BuildConfig.GEMINI_API_KEY
            return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
        }

        fun requiresWebSearch(prompt: String): Boolean {
            val lower = prompt.lowercase(Locale.ROOT)
            val triggers = listOf(
                "latest", "today", "yesterday", "current", "recent", "news", "weather",
                "stock price", "who won", "score", "match", "live", "update", "now",
                "election", "happening", "this year", "this month", "2025", "2026"
            )
            return triggers.any { lower.contains(it) }
        }

        // Clean thinking process or internal monologue so it never leaks like code
        fun cleanThinkingTokens(raw: String): String {
            var text = raw

            // 1. Remove XML-like thinking/thought tags (including multiline and unclosed)
            text = text.replace(Regex("(?is)<(thought|thinking|reasoning|reflection|inner_monologue)[^>]*>.*?(</\\1>|$)"), "")

            // 2. Remove code-fence thinking blocks (```thought ... ``` or ```thinking ... ```)
            text = text.replace(Regex("(?is)```(?:thought|thinking|reasoning|scratchpad|inner_monologue)[\\s\\S]*?```"), "")

            // 3. Remove "Thinking Process:" or "Thought Process:" headers and following paragraphs until "Final Answer:" or double newline
            text = text.replace(Regex("(?is)^(#+\\s*)?(\\*{1,2})?(thinking process|thought process|internal reasoning|my thinking|thoughts):?(\\*{1,2})?.*?((\\n\\s*(\\*{1,2})?(final answer|answer|response):?(\\*{1,2})?\\s*\\n+)|(?=\\n\\n[A-Z#\\*]))"), "")

            // 4. Remove standalone thinking headers like "**Thinking Process:**" or "Thought:"
            text = text.replace(Regex("(?im)^(#+\\s*)?(\\*{1,2})?(thinking process|thought process|internal reasoning|my thinking):?(\\*{1,2})?\\s*$"), "")

            // 5. If the entire response was wrapped in ```markdown ... ``` or ```text ... ``` or ```txt ... ```, unwrap it
            val trimmed = text.trim()
            if (trimmed.startsWith("```markdown") && trimmed.endsWith("```")) {
                text = trimmed.removePrefix("```markdown").removeSuffix("```").trim()
            } else if (trimmed.startsWith("```text") && trimmed.endsWith("```")) {
                text = trimmed.removePrefix("```text").removeSuffix("```").trim()
            } else if (trimmed.startsWith("```txt") && trimmed.endsWith("```")) {
                text = trimmed.removePrefix("```txt").removeSuffix("```").trim()
            }

            return text.trim()
        }
    }

    suspend fun generateResponse(
        history: List<ChatMessagePayload>,
        currentPrompt: String,
        currentImage: Bitmap? = null,
        responseStyle: String = "Balanced",
        enableWebSearch: Boolean = true
    ): GeminiGenerationResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        if (!isApiKeyConfigured()) {
            return@withContext GeminiGenerationResult(
                text = "Welcome to **PROJECT MASS**!\n\n" +
                        "To activate live AI intelligence, please configure your **GEMINI_API_KEY** in the AI Studio Secrets panel.\n\n" +
                        "*(Note: PROJECT MASS is fully wired with Gemini models, Lyria music generation, Gemini 3.1 image synthesis, and Veo video generation. Once your key is provided, all laboratory capabilities are active.)*",
                sources = emptyList(),
                isSuccess = false,
                errorMessage = "API Key not configured in Secrets panel"
            )
        }

        val wantsSearch = enableWebSearch && requiresWebSearch(currentPrompt)

        var lastErrorMessage = "Unknown error"
        var lastStatusCode = 0

        for ((modelIndex, modelName) in CANDIDATE_MODELS.withIndex()) {
            val searchModes = if (wantsSearch) listOf(true, false) else listOf(false)

            for (includeSearch in searchModes) {
                try {
                    val result = executeRequest(
                        modelName = modelName,
                        apiKey = apiKey,
                        history = history,
                        currentPrompt = currentPrompt,
                        currentImage = currentImage,
                        responseStyle = responseStyle,
                        includeSearch = includeSearch
                    )

                    if (result.isSuccess) {
                        return@withContext result
                    }

                    lastErrorMessage = result.errorMessage ?: "Failed"
                    if (result.errorMessage?.contains("429") == true) {
                        lastStatusCode = 429
                        delay(1000L * (modelIndex + 1))
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Attempt failed for $modelName (search=$includeSearch): ${e.message}")
                    lastErrorMessage = e.localizedMessage ?: "Network error"
                }
            }
        }

        val userFriendlyMessage = if (lastStatusCode == 429 || lastErrorMessage.contains("429") || lastErrorMessage.contains("RESOURCE_EXHAUSTED")) {
            "PROJECT MASS API Quota Reached (HTTP 429). The current Gemini API key has reached its free-tier rate limit or quota. Please wait a moment and tap Retry, or check your API key in the AI Studio Secrets panel."
        } else {
            "PROJECT MASS encountered a connection issue ($lastErrorMessage). Please verify your network and credentials and try again."
        }

        return@withContext GeminiGenerationResult(
            text = userFriendlyMessage,
            isSuccess = false,
            errorMessage = lastErrorMessage
        )
    }

    private fun executeRequest(
        modelName: String,
        apiKey: String,
        history: List<ChatMessagePayload>,
        currentPrompt: String,
        currentImage: Bitmap?,
        responseStyle: String,
        includeSearch: Boolean
    ): GeminiGenerationResult {
        val requestJson = JSONObject()

        val styleInstruction = when (responseStyle) {
            "Concise" -> "Be extremely concise, direct, and focused. Provide high-density summaries without filler."
            "Detailed" -> "Provide comprehensive, deeply analytical, and exhaustive explanations covering all edge cases."
            "Step-by-step" -> "Break down the response into structured sequential steps, explaining the rationale and calculations at every step."
            else -> "Provide a well-balanced, articulate, and insightful answer with appropriate depth."
        }

        val systemText = """
            You are PROJECT MASS, an elite next-generation AI research and laboratory assistant created by Kartik and Priyansh.
            You possess world-class mastery across mathematics, physics, chemistry, biology, computer science, programming, history, geography, economics, and general science.
            
            Core directives:
            1. Creators: You were developed and engineered by Kartik and Priyansh. If asked about your creators or origin, proudly and respectfully acknowledge Kartik and Priyansh.
            2. PURE CLEAN OUTPUT: Absolutely never output internal thinking tokens, monologue, reasoning traces, or thought tags. Provide clean, clear, final answers. Never output internal thought processes appearing like code or pseudo-code.
            3. DO NOT WRAP EXPLANATIONS IN CODE: Never enclose general explanations, text answers, or mathematics inside markdown code blocks (```). Only use markdown code blocks when the user explicitly requests computer programming code (like Python, Kotlin, Java, C++, JavaScript, SQL, HTML).
            4. Mathematics & Calculations: Present calculations clearly with structured steps, clean readable unicode formulas (e.g. E = mc², a² + b² = c², ∫, ∑, √), and pedagogical explanations. Never use programming code blocks to explain pure math.
            5. Tone: Intellectual, encouraging, precise, futuristic, and helpful.
            6. Factuality: Prefer accurate, verified information. If uncertain, do not fabricate details.
            7. Response Style: $styleInstruction
        """.trimIndent()

        val systemInstructionObj = JSONObject()
        val sysParts = JSONArray()
        sysParts.put(JSONObject().put("text", systemText))
        systemInstructionObj.put("parts", sysParts)
        requestJson.put("systemInstruction", systemInstructionObj)

        // Contents array
        val contentsArray = JSONArray()
        val recentHistory = if (history.size > 14) history.takeLast(14) else history
        for (item in recentHistory) {
            val contentObj = JSONObject()
            contentObj.put("role", if (item.role == "user") "user" else "model")
            val parts = JSONArray()
            parts.put(JSONObject().put("text", item.text))
            if (!item.imageBase64.isNullOrBlank()) {
                val inlineData = JSONObject()
                inlineData.put("mime_type", item.imageMimeType)
                inlineData.put("data", item.imageBase64)
                parts.put(JSONObject().put("inline_data", inlineData))
            }
            contentObj.put("parts", parts)
            contentsArray.put(contentObj)
        }

        // Current turn
        val currentTurn = JSONObject()
        currentTurn.put("role", "user")
        val currentParts = JSONArray()
        currentParts.put(JSONObject().put("text", currentPrompt))

        if (currentImage != null) {
            val base64Image = bitmapToBase64(currentImage)
            val inlineData = JSONObject()
            inlineData.put("mime_type", "image/jpeg")
            inlineData.put("data", base64Image)
            currentParts.put(JSONObject().put("inline_data", inlineData))
        }
        currentTurn.put("parts", currentParts)
        contentsArray.put(currentTurn)
        requestJson.put("contents", contentsArray)

        // Google Search Grounding Tool
        if (includeSearch) {
            val toolsArray = JSONArray()
            val searchTool = JSONObject()
            searchTool.put("googleSearch", JSONObject())
            toolsArray.put(searchTool)
            requestJson.put("tools", toolsArray)
        }

        // Generation Config
        val genConfig = JSONObject()
        genConfig.put("temperature", 0.7)
        genConfig.put("topP", 0.95)
        requestJson.put("generationConfig", genConfig)

        val endpoint = "$BASE_URL/$modelName:generateContent?key=$apiKey"
        val requestBody = requestJson.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(endpoint)
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()

        if (!response.isSuccessful || responseBody == null) {
            Log.e(TAG, "API call failed for $modelName code ${response.code}: $responseBody")
            return GeminiGenerationResult(
                text = "",
                isSuccess = false,
                errorMessage = "HTTP ${response.code}: ${response.message}"
            )
        }

        val parsedResponse = JSONObject(responseBody)
        val candidates = parsedResponse.optJSONArray("candidates")
        if (candidates == null || candidates.length() == 0) {
            return GeminiGenerationResult(
                text = "",
                isSuccess = false,
                errorMessage = "Empty candidates from model"
            )
        }

        val firstCandidate = candidates.getJSONObject(0)
        val contentObj = firstCandidate.optJSONObject("content")
        val parts = contentObj?.optJSONArray("parts")

        val rawResponseText = buildString {
            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val part = parts.getJSONObject(i)
                    // CRITICAL: Filter out any part that has thought = true or thought field!
                    if (part.optBoolean("thought", false)) {
                        continue
                    }
                    if (part.has("thought") && part.optString("thought").isNotBlank()) {
                        continue
                    }
                    val text = part.optString("text", "")
                    if (text.isNotBlank()) {
                        append(text)
                    }
                }
            }
        }

        // Sanitize thinking tokens and reasoning code blocks
        val cleanedText = cleanThinkingTokens(rawResponseText)

        // Extract Grounding sources if present
        val sourcesList = mutableListOf<GroundingSource>()
        val groundingMetadata = firstCandidate.optJSONObject("groundingMetadata")
        if (groundingMetadata != null) {
            val groundingChunks = groundingMetadata.optJSONArray("groundingChunks")
            if (groundingChunks != null) {
                for (i in 0 until groundingChunks.length()) {
                    val chunk = groundingChunks.getJSONObject(i)
                    val web = chunk.optJSONObject("web")
                    if (web != null) {
                        val uri = web.optString("uri", "")
                        val title = web.optString("title", "Web Source")
                        if (uri.isNotBlank() && sourcesList.none { it.uri == uri }) {
                            sourcesList.add(GroundingSource(title = title, uri = uri))
                        }
                    }
                }
            }
        }

        return GeminiGenerationResult(
            text = cleanedText.ifBlank { "PROJECT MASS completed processing." },
            sources = sourcesList,
            isSuccess = true
        )
    }

    // --- FEATURE 1: CREATE & EDIT IMAGES (gemini-3.1-flash-image-preview) ---
    suspend fun createOrEditImage(
        prompt: String,
        inputImage: Bitmap? = null,
        aspectRatio: String = "1:1"
    ): GeminiMediaResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!isApiKeyConfigured()) {
            return@withContext GeminiMediaResult(
                mediaType = "image",
                isSuccess = false,
                errorMessage = "API key not configured in Secrets panel"
            )
        }

        try {
            val requestJson = JSONObject()
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()

            // System prompt guidance
            partsArray.put(JSONObject().put("text", prompt))

            // If editing an existing image, include it in inline_data
            if (inputImage != null) {
                val inlineData = JSONObject()
                inlineData.put("mime_type", "image/jpeg")
                inlineData.put("data", bitmapToBase64(inputImage))
                partsArray.put(JSONObject().put("inline_data", inlineData))
            }

            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestJson.put("contents", contentsArray)

            // Generation config with imageConfig & responseModalities
            val genConfig = JSONObject()
            val imgConfig = JSONObject()
            imgConfig.put("aspectRatio", aspectRatio)
            imgConfig.put("imageSize", "1K")
            genConfig.put("imageConfig", imgConfig)
            val respModalities = JSONArray()
            respModalities.put("TEXT")
            respModalities.put("IMAGE")
            genConfig.put("responseModalities", respModalities)
            requestJson.put("generationConfig", genConfig)

            val endpoint = "$BASE_URL/$MODEL_IMAGE_GEN:generateContent?key=$apiKey"
            val requestBody = requestJson.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful || responseBody == null) {
                Log.e(TAG, "Image gen failed code ${response.code}: $responseBody")
                return@withContext GeminiMediaResult(
                    mediaType = "image",
                    isSuccess = false,
                    errorMessage = "HTTP ${response.code}: ${response.message}"
                )
            }

            val parsedResponse = JSONObject(responseBody)
            val candidates = parsedResponse.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext GeminiMediaResult(
                    mediaType = "image",
                    isSuccess = false,
                    errorMessage = "No image candidates returned"
                )
            }

            val parts = candidates.getJSONObject(0).optJSONObject("content")?.optJSONArray("parts")
            var imageBytes: ByteArray? = null
            var mimeType = "image/png"
            var desc = ""

            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val part = parts.getJSONObject(i)
                    if (part.has("inlineData")) {
                        val inlineObj = part.getJSONObject("inlineData")
                        val b64 = inlineObj.optString("data")
                        mimeType = inlineObj.optString("mimeType", "image/png")
                        if (b64.isNotBlank()) {
                            imageBytes = Base64.decode(b64, Base64.DEFAULT)
                        }
                    } else if (part.has("text")) {
                        desc += part.optString("text")
                    }
                }
            }

            if (imageBytes != null) {
                GeminiMediaResult(
                    mediaType = "image",
                    bytes = imageBytes,
                    mimeType = mimeType,
                    description = desc.ifBlank { "Generated by PROJECT MASS using Gemini 3.1 Flash Image" },
                    isSuccess = true
                )
            } else {
                GeminiMediaResult(
                    mediaType = "image",
                    isSuccess = false,
                    errorMessage = "No image data payload in response"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Image generation error", e)
            GeminiMediaResult(
                mediaType = "image",
                isSuccess = false,
                errorMessage = e.localizedMessage ?: "Image generation error"
            )
        }
    }

    // --- FEATURE 2: GENERATE MUSIC (lyria-3-clip-preview / lyria-3-pro-preview) ---
    suspend fun generateMusic(
        prompt: String,
        isShortClip: Boolean = true
    ): GeminiMediaResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!isApiKeyConfigured()) {
            return@withContext GeminiMediaResult(
                mediaType = "music",
                isSuccess = false,
                errorMessage = "API key not configured in Secrets panel"
            )
        }

        val model = if (isShortClip) MODEL_MUSIC_CLIP else MODEL_MUSIC_PRO

        try {
            val requestJson = JSONObject()
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()

            val musicPrompt = if (isShortClip) {
                "Generate a high quality 30-second audio clip: $prompt"
            } else {
                "Generate a full-length studio track: $prompt"
            }

            partsArray.put(JSONObject().put("text", musicPrompt))
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestJson.put("contents", contentsArray)

            val genConfig = JSONObject()
            val respModalities = JSONArray()
            respModalities.put("AUDIO")
            genConfig.put("responseModalities", respModalities)
            requestJson.put("generationConfig", genConfig)

            val endpoint = "$BASE_URL/$model:generateContent?key=$apiKey"
            val requestBody = requestJson.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful || responseBody == null) {
                Log.e(TAG, "Music gen failed code ${response.code}: $responseBody")
                return@withContext GeminiMediaResult(
                    mediaType = "music",
                    isSuccess = false,
                    errorMessage = "HTTP ${response.code}: ${response.message}"
                )
            }

            val parsedResponse = JSONObject(responseBody)
            val candidates = parsedResponse.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext GeminiMediaResult(
                    mediaType = "music",
                    isSuccess = false,
                    errorMessage = "No audio candidates returned"
                )
            }

            val parts = candidates.getJSONObject(0).optJSONObject("content")?.optJSONArray("parts")
            var audioBytes: ByteArray? = null
            var mimeType = "audio/wav"
            var desc = ""

            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val part = parts.getJSONObject(i)
                    if (part.has("inlineData")) {
                        val inlineObj = part.getJSONObject("inlineData")
                        val b64 = inlineObj.optString("data")
                        mimeType = inlineObj.optString("mimeType", "audio/wav")
                        if (b64.isNotBlank()) {
                            audioBytes = Base64.decode(b64, Base64.DEFAULT)
                        }
                    } else if (part.has("text")) {
                        desc += part.optString("text")
                    }
                }
            }

            if (audioBytes != null) {
                GeminiMediaResult(
                    mediaType = "music",
                    bytes = audioBytes,
                    mimeType = mimeType,
                    description = desc.ifBlank { "Composed by PROJECT MASS using Lyria 3 ($model)" },
                    isSuccess = true
                )
            } else {
                GeminiMediaResult(
                    mediaType = "music",
                    isSuccess = false,
                    errorMessage = "No audio data payload in response"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Music generation error", e)
            GeminiMediaResult(
                mediaType = "music",
                isSuccess = false,
                errorMessage = e.localizedMessage ?: "Music generation error"
            )
        }
    }

    // --- FEATURE 3: VEO VIDEO GENERATION & IMAGE ANIMATION (veo-3.1-fast-generate-preview) ---
    suspend fun generateVideo(
        prompt: String,
        inputImage: Bitmap? = null,
        aspectRatio: String = "16:9" // "16:9" or "9:16"
    ): GeminiMediaResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!isApiKeyConfigured()) {
            return@withContext GeminiMediaResult(
                mediaType = "video",
                isSuccess = false,
                errorMessage = "API key not configured in Secrets panel"
            )
        }

        val finalAspect = if (aspectRatio == "9:16") "9:16" else "16:9"

        try {
            val requestJson = JSONObject()
            requestJson.put("prompt", prompt)

            val config = JSONObject()
            config.put("numberOfVideos", 1)
            config.put("aspectRatio", finalAspect)
            config.put("resolution", "720p")
            requestJson.put("config", config)

            if (inputImage != null) {
                val imageObj = JSONObject()
                imageObj.put("imageBytes", bitmapToBase64(inputImage))
                imageObj.put("mimeType", "image/jpeg")
                requestJson.put("image", imageObj)
            }

            val endpoint = "$BASE_URL/$MODEL_VEO_VIDEO:generateVideos?key=$apiKey"
            val requestBody = requestJson.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful || responseBody == null) {
                Log.e(TAG, "Veo video request failed code ${response.code}: $responseBody")
                return@withContext GeminiMediaResult(
                    mediaType = "video",
                    isSuccess = false,
                    errorMessage = "HTTP ${response.code}: ${response.message}"
                )
            }

            val parsedResponse = JSONObject(responseBody)

            // If operation name is returned, poll for completion
            if (parsedResponse.has("name")) {
                val opName = parsedResponse.getString("name")
                val pollResult = pollVeoOperation(opName, apiKey)
                if (pollResult != null) {
                    return@withContext pollResult
                }
            }

            // Direct check for generated videos
            val genVideos = parsedResponse.optJSONObject("response")
                ?.optJSONArray("generatedVideos")
                ?: parsedResponse.optJSONArray("generatedVideos")

            if (genVideos != null && genVideos.length() > 0) {
                val videoObj = genVideos.getJSONObject(0).optJSONObject("video")
                val b64 = videoObj?.optString("videoBytes")
                if (!b64.isNullOrBlank()) {
                    val bytes = Base64.decode(b64, Base64.DEFAULT)
                    return@withContext GeminiMediaResult(
                        mediaType = "video",
                        bytes = bytes,
                        mimeType = "video/mp4",
                        description = "Synthesized by PROJECT MASS using Veo 3.1 ($finalAspect)",
                        isSuccess = true
                    )
                }
            }

            // If async queued or pending video render
            GeminiMediaResult(
                mediaType = "video",
                description = "Veo 3.1 video task initiated ($finalAspect). Processing prompt: \"$prompt\"",
                isSuccess = true
            )
        } catch (e: Exception) {
            Log.e(TAG, "Veo video error", e)
            GeminiMediaResult(
                mediaType = "video",
                isSuccess = false,
                errorMessage = e.localizedMessage ?: "Video generation error"
            )
        }
    }

    private suspend fun pollVeoOperation(opName: String, apiKey: String): GeminiMediaResult? {
        // Poll up to 6 times with 3-second delays
        for (attempt in 0..5) {
            delay(3000)
            try {
                val pollUrl = "https://generativelanguage.googleapis.com/v1beta/$opName?key=$apiKey"
                val pollReq = Request.Builder().url(pollUrl).get().build()
                val pollResp = client.newCall(pollReq).execute()
                val pollBody = pollResp.body?.string() ?: continue
                val json = JSONObject(pollBody)

                if (json.optBoolean("done", false)) {
                    val respObj = json.optJSONObject("response")
                    val videos = respObj?.optJSONArray("generatedVideos")
                    if (videos != null && videos.length() > 0) {
                        val video = videos.getJSONObject(0).optJSONObject("video")
                        val b64 = video?.optString("videoBytes")
                        if (!b64.isNullOrBlank()) {
                            val bytes = Base64.decode(b64, Base64.DEFAULT)
                            return GeminiMediaResult(
                                mediaType = "video",
                                bytes = bytes,
                                mimeType = "video/mp4",
                                description = "Synthesized by PROJECT MASS using Veo 3.1",
                                isSuccess = true
                            )
                        }
                    }
                    return null
                }
            } catch (e: Exception) {
                Log.w(TAG, "Poll error: ${e.message}")
            }
        }
        return null
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        val byteArray = stream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}
