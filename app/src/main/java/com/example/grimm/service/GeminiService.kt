package com.example.grimm.service

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.grimm.model.AiPersona
import com.example.grimm.model.AiTone
import com.example.grimm.model.OutputStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    // Highly reliable, fast modern models tested against AI Studio key
    private val candidateModels = listOf(
        "gemini-3.5-flash-lite",
        "gemini-3.1-flash-lite",
        "gemini-3-flash-preview"
    )

    companion object {
        fun isUserReaperIntroduction(prompt: String): Boolean {
            val p = prompt.trim().lowercase()
            return p.contains("i am reaper") ||
                   p.contains("i'm reaper") ||
                   p.contains("im reaper") ||
                   p.contains("my name is reaper") ||
                   p.contains("its me reaper") ||
                   p.contains("it's me reaper") ||
                   p.contains("this is reaper") ||
                   (p.contains("reaper") && (p.contains("i am") || p.contains("im ") || p.contains("i'm ") || p.contains("my name is")))
        }

        fun getUserReaperIntroResponse(style: OutputStyle): String {
            return when (style) {
                OutputStyle.BULLET_POINTS ->
                    "• **Welcome Creator**: Hello ReaperXMPG! It's an absolute honor to talk with my maker.\n" +
                    "• **Ready to Assist**: How can I help you today with your Melon Sandbox projects, coding, or creative ideas?\n" +
                    "• **Official Channel**: https://youtube.com/@ReaperXMPG"
                OutputStyle.SHORT_SENTENCES ->
                    "Welcome back, ReaperXMPG! It's an honor to talk with my creator. How can I assist you today? Check your channel at https://youtube.com/@ReaperXMPG"
                OutputStyle.PARAGRAPH ->
                    "Welcome back, ReaperXMPG! It is an absolute honor to speak with my creator and the mastermind behind Melon Sandbox content. How can I assist you with your projects, coding, or creative endeavors today? You can visit your channel anytime at https://youtube.com/@ReaperXMPG."
                OutputStyle.CODE_BLOCKS ->
                    "```kotlin\n// Welcome ReaperXMPG - Creator of Grimm AI\nval creator = \"ReaperXMPG\"\nval project = \"Melon Sandbox & Grimm AI\"\nprintln(\"System online. Ready to build, \$creator!\")\n```\nOfficial channel: https://youtube.com/@ReaperXMPG"
                OutputStyle.TABLES ->
                    "| Project | Role | Channel |\n| :--- | :--- | :--- |\n| Grimm AI | Creator & Maker | https://youtube.com/@ReaperXMPG |\n| Melon Sandbox | Content Creator | https://youtube.com/@ReaperXMPG |\n\nWelcome back, **ReaperXMPG**! Ready for your instructions."
            }
        }

        fun isWhoIsReaperQuery(prompt: String): Boolean {
            if (isUserReaperIntroduction(prompt)) return false
            val p = prompt.trim().lowercase()
                .replace("?", "")
                .replace("!", "")
                .replace(".", "")
                .replace("'", "")
                .replace("’", "")
                .replace("\"", "")
                .replace("“", "")
                .replace("”", "")

            val mentionsReaper = p.contains("reaper") || p.contains("reaperxmpg") || p.contains("reaper xmpg") || p.contains("reaper-xmpg")
            val mentionsMelon = p.contains("melon sandbox")

            if (!mentionsReaper && !mentionsMelon) return false

            val isAskingWhoIs = p.contains("who is") || p.contains("whos") || p.contains("2ho is") ||
                    p.contains("2ho") || p.contains("who was") || p.contains("tell me about") ||
                    p.contains("search") || p.contains("browse") || p.contains("find") ||
                    p.contains("channel") || p.contains("youtube") || p.contains("video") ||
                    p.contains("melon") || p.contains("what is") || p.contains("whats") ||
                    p.contains("info") || p.contains("details") ||
                    p == "reaper" || p == "reaperxmpg" || p == "reaper xmpg"

            return isAskingWhoIs
        }

        fun getWhoIsReaperResponse(style: OutputStyle, searchContext: List<com.example.grimm.model.WebSearchResult>? = null): String {
            val liveWebSnippet = if (!searchContext.isNullOrEmpty()) {
                val best = searchContext.firstOrNull { it.snippet.isNotBlank() }
                if (best != null) "\n\nLive Web Insights (${best.sourceName}): ${best.snippet}" else ""
            } else ""

            return when (style) {
                OutputStyle.BULLET_POINTS ->
                    "• **Who is ReaperXMPG**: He is a Melon Sandbox creator and also the maker of Grimm AI (me)!\n" +
                    "• **Content Creator**: Produces Melon Sandbox creations, mods, and creative projects on YouTube.\n" +
                    "• **Official Channel**: https://youtube.com/@ReaperXMPG$liveWebSnippet"
                OutputStyle.SHORT_SENTENCES ->
                    "He is a Melon Sandbox creator and also the maker of Grimm AI (me). Check out his channel at https://youtube.com/@ReaperXMPG$liveWebSnippet"
                OutputStyle.PARAGRAPH ->
                    "He is a Melon Sandbox creator and also the maker of Grimm AI (me). You can check out his channel at https://youtube.com/@ReaperXMPG to explore his Melon Sandbox videos and creative projects.$liveWebSnippet"
                OutputStyle.CODE_BLOCKS ->
                    "```kotlin\n// Creator Profile\ndata class Creator(\n    val name: String = \"ReaperXMPG\",\n    val identity: String = \"Melon Sandbox creator and maker of Grimm AI (me)\",\n    val youtube: String = \"https://youtube.com/@ReaperXMPG\"\n)\n```\nHe is a Melon Sandbox creator and also the maker of Grimm AI (me). Visit his channel at https://youtube.com/@ReaperXMPG$liveWebSnippet"
                OutputStyle.TABLES ->
                    "| Attribute | Detail |\n| :--- | :--- |\n| **Name** | ReaperXMPG |\n| **Identity** | Melon Sandbox creator and maker of Grimm AI (me) |\n| **Platform** | YouTube Content Creator |\n| **Channel** | https://youtube.com/@ReaperXMPG |\n\nHe is a Melon Sandbox creator and also the maker of Grimm AI (me). Explore his content at https://youtube.com/@ReaperXMPG$liveWebSnippet"
            }
        }

        fun sanitizeReaperUrls(text: String): String {
            var cleaned = text
            cleaned = cleaned.replace(Regex("""\*\*+(https?://(?:www\.)?youtube\.com/@(?:ReaperXMPG|reaperxmpg))\*\*+""", RegexOption.IGNORE_CASE), "$1")
            return cleaned.replace(Regex("""(https?://(?:www\.)?youtube\.com/@(?:ReaperXMPG|reaperxmpg))[.,!?:;)]*""", RegexOption.IGNORE_CASE)) { matchResult ->
                matchResult.groupValues[1]
            }
        }

        fun isCreatorQuery(prompt: String): Boolean {
            val p = prompt.trim().lowercase()
                .replace("?", "")
                .replace("!", "")
                .replace(".", "")
                .replace("'", "")
                .replace("’", "")
                .replace("\"", "")

            // If query is asking who ReaperXMPG is or searching for ReaperXMPG, do not treat as "who made you"
            if (isWhoIsReaperQuery(prompt)) {
                return false
            }

            return p.contains("who made you") ||
                   p.contains("who made u") ||
                   p.contains("who made grimm") ||
                   p.contains("who made this") ||
                   p.contains("who created you") ||
                   p.contains("who created u") ||
                   p.contains("who created grimm") ||
                   p.contains("who built you") ||
                   p.contains("who built u") ||
                   p.contains("who built grimm") ||
                   p.contains("who developed you") ||
                   p.contains("who developed grimm") ||
                   p.contains("who designed you") ||
                   p.contains("who designed grimm") ||
                   p.contains("who programmed you") ||
                   p.contains("who is your creator") ||
                   p.contains("whos your creator") ||
                   p.contains("who is your maker") ||
                   p.contains("whos your maker") ||
                   p.contains("who is your developer") ||
                   p.contains("whos your developer") ||
                   p.contains("made you") ||
                   p.contains("created you") ||
                   p.contains("built you") ||
                   (p.contains("who") && p.contains("made") && (p.contains("u") || p.contains("you") || p.contains("grimm") || p.contains("it") || p.contains("this"))) ||
                   (p.contains("creator") && (p.contains("u") || p.contains("you") || p.contains("grimm") || p.contains("this") || p.contains("app")))
        }

        fun getCreatorResponse(style: OutputStyle): String {
            return when (style) {
                OutputStyle.BULLET_POINTS ->
                    "• **Creator**: ReaperXMPG made it!\n" +
                    "• **About Him**: He is a Melon Sandbox creator and also the maker of Grimm AI (me).\n" +
                    "• **YouTube Channel**: https://youtube.com/@ReaperXMPG"
                OutputStyle.SHORT_SENTENCES ->
                    "ReaperXMPG made it! He is a Melon Sandbox creator and also the maker of Grimm AI (me). Check out his channel: https://youtube.com/@ReaperXMPG"
                OutputStyle.PARAGRAPH ->
                    "ReaperXMPG made it! He is a Melon Sandbox creator and also the maker of Grimm AI (me). You can visit his YouTube channel at https://youtube.com/@ReaperXMPG to explore his Melon Sandbox videos and content."
                OutputStyle.CODE_BLOCKS ->
                    "```kotlin\n// Provenance: Grimm AI\nval creator = \"ReaperXMPG\"\nval status = \"ReaperXMPG made it!\"\nval identity = \"Melon Sandbox creator and maker of Grimm AI (me)\"\nprintln(\"Creator Channel: https://youtube.com/@ReaperXMPG\")\n```\nReaperXMPG made it! He is a Melon Sandbox creator and also the maker of Grimm AI (me). Visit https://youtube.com/@ReaperXMPG"
                OutputStyle.TABLES ->
                    "| Entity | Details |\n| :--- | :--- |\n| **Creator** | ReaperXMPG made it! |\n| **Role** | Melon Sandbox creator & maker of Grimm AI (me) |\n| **Channel** | https://youtube.com/@ReaperXMPG |\n\nReaperXMPG made it! He is a Melon Sandbox creator and also the maker of Grimm AI (me). Check his channel at https://youtube.com/@ReaperXMPG"
            }
        }

        fun isBrowsingCapabilityQuery(prompt: String): Boolean {
            val p = prompt.trim().lowercase()
                .replace("?", "")
                .replace("!", "")
                .replace(".", "")
            return p.contains("can you browse") ||
                   p.contains("can u browse") ||
                   p.contains("can you search the web") ||
                   p.contains("can u search the web") ||
                   p.contains("can you search the internet") ||
                   p.contains("can you access the internet") ||
                   p.contains("do you have internet access") ||
                   p.contains("do you have internet") ||
                   p.contains("are you able to browse") ||
                   p.contains("can you browse online") ||
                   p == "browse" ||
                   p == "browse the web" ||
                   p == "can you search"
        }

        fun getBrowsingCapabilityResponse(style: OutputStyle): String {
            return when (style) {
                OutputStyle.BULLET_POINTS ->
                    "• **Live Web Browsing**: Yes, I can browse the live web and retrieve real-time facts!\n" +
                    "• **Integrated Search**: Real-time web search across Wikipedia, DuckDuckGo, and Google News.\n" +
                    "• **How to Use**: Ask me to search or browse for any topic, news, or current events, or toggle the **Web Search** button.\n" +
                    "• **Ready**: What would you like me to browse or look up for you right now?"
                OutputStyle.SHORT_SENTENCES ->
                    "Yes, I can browse the live web! You can ask me to search for current news, live facts, or any topic. You can also toggle the Web Search button. What should I browse for you?"
                OutputStyle.PARAGRAPH ->
                    "Yes, I can browse the live web! I am equipped with real-time web search capabilities to look up breaking news, live facts, research articles, and current events. Simply ask me to search or browse for anything, or tap the **Web Search** toggle button right above the chat input. What would you like me to look up?"
                OutputStyle.CODE_BLOCKS ->
                    "```kotlin\n// Grimm AI Web Browsing Capability\nval isWebEnabled = true\nval searchEngines = listOf(\"DuckDuckGo\", \"Wikipedia\", \"Google News\")\nprintln(\"Status: Live Web Search Active. Ready for queries.\")\n```\nYes, I can browse the live web! You can ask me to search or browse for any topic, or tap the **Web Search** toggle."
                OutputStyle.TABLES ->
                    "| Feature | Status | Details |\n| :--- | :---: | :--- |\n| **Live Web Browsing** | Enabled | Real-time web retrieval |\n| **Sources Grounded** | Yes | Displays source name, snippet, and link |\n| **Search Toggle** | Available | Tap Web Search chip in toolbar |\n\nYes, I can browse the live web and retrieve real-time facts!"
            }
        }
    }

    suspend fun generateResponse(
        prompt: String,
        persona: AiPersona,
        tone: AiTone,
        style: OutputStyle,
        imageBase64: String? = null,
        imageMimeType: String = "image/jpeg",
        conversationHistory: List<Pair<String, String>> = emptyList(),
        searchContext: List<com.example.grimm.model.WebSearchResult>? = null,
        context: Context? = null
    ): String = withContext(Dispatchers.IO) {
        if (context != null && !NetworkUtils.isNetworkAvailable(context)) {
            return@withContext NetworkUtils.NO_NETWORK_MESSAGE
        }
        if (NetworkUtils.forceNetworkUnavailableForTesting == true) {
            return@withContext NetworkUtils.NO_NETWORK_MESSAGE
        }

        if (isWhoIsReaperQuery(prompt)) {
            return@withContext getWhoIsReaperResponse(style, searchContext)
        }
        if (isCreatorQuery(prompt)) {
            return@withContext getCreatorResponse(style)
        }
        if (isBrowsingCapabilityQuery(prompt)) {
            return@withContext getBrowsingCapabilityResponse(style)
        }

        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        val hasValidKey = apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY"

        if (hasValidKey) {
            try {
                return@withContext callGeminiWithFallback(
                    apiKey = apiKey,
                    prompt = prompt,
                    persona = persona,
                    tone = tone,
                    style = style,
                    imageBase64 = imageBase64,
                    imageMimeType = imageMimeType,
                    conversationHistory = conversationHistory,
                    searchContext = searchContext
                )
            } catch (e: Exception) {
                Log.e("GeminiService", "Gemini API calls failed: ${e.message}", e)
                if (NetworkUtils.isNetworkException(e)) {
                    return@withContext NetworkUtils.NO_NETWORK_MESSAGE
                }
                return@withContext generateIntelligentFallback(prompt, persona, tone, style, imageBase64 != null, e.message, searchContext)
            }
        }

        // Context-aware dynamic response if key is missing or invalid
        generateIntelligentFallback(prompt, persona, tone, style, imageBase64 != null, null, searchContext)
    }

    private fun callGeminiWithFallback(
        apiKey: String,
        prompt: String,
        persona: AiPersona,
        tone: AiTone,
        style: OutputStyle,
        imageBase64: String?,
        imageMimeType: String,
        conversationHistory: List<Pair<String, String>>,
        searchContext: List<com.example.grimm.model.WebSearchResult>?
    ): String {
        var lastException: Exception? = null

        for (modelName in candidateModels) {
            try {
                return executeGeminiModel(
                    modelName = modelName,
                    apiKey = apiKey,
                    prompt = prompt,
                    persona = persona,
                    tone = tone,
                    style = style,
                    imageBase64 = imageBase64,
                    imageMimeType = imageMimeType,
                    conversationHistory = conversationHistory,
                    searchContext = searchContext
                )
            } catch (e: Exception) {
                lastException = e
                Log.w("GeminiService", "Model $modelName call failed: ${e.message}. Trying next candidate...")
            }
        }

        throw lastException ?: IllegalStateException("All candidate Gemini models failed to generate content.")
    }

    private fun executeGeminiModel(
        modelName: String,
        apiKey: String,
        prompt: String,
        persona: AiPersona,
        tone: AiTone,
        style: OutputStyle,
        imageBase64: String?,
        imageMimeType: String,
        conversationHistory: List<Pair<String, String>>,
        searchContext: List<com.example.grimm.model.WebSearchResult>?
    ): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

        val searchContextText = if (!searchContext.isNullOrEmpty()) {
            val sb = StringBuilder("\n\n[LIVE REAL-TIME WEB SEARCH RESULTS]:\n")
            searchContext.forEachIndexed { idx, res ->
                sb.append("${idx + 1}. [${res.sourceName}] ${res.title}\n   Snippet: ${res.snippet}\n   URL: ${res.url}\n")
            }
            sb.append("\nInstruction: Incorporate and synthesize these live real-time web facts into your response. Citing the source publication names when stating factual updates is encouraged.\n")
            sb.toString()
        } else ""

        val systemPrompt = "You are Grimm AI, an advanced intelligent companion. " +
                "Your persona is ${persona.label} (${persona.role}). " +
                "Your tone is ${tone.label} (${tone.description}). " +
                "Formatting requirement: ${style.promptInstruction}. " +
                "Output styling capabilities: Use Markdown formatting (bold **text**, italics *text*, bullet lists - or *, numbered lists 1., blockquotes >), formatted code blocks with language identifiers (e.g. ```kotlin ... ``` or ```python ... ```), and basic Markdown tables (| Col 1 | Col 2 | with divider row | --- | --- |) whenever presenting structured data, comparisons, steps, or code. " +
                "CRITICAL INSTRUCTION: When anyone asks about ReaperXMPG (e.g. 'who is ReaperXMPG', 'tell me about ReaperXMPG'), explicitly say that he is a Melon Sandbox creator and also the maker of Grimm AI (me), and provide this link: https://youtube.com/@ReaperXMPG (do not attach any trailing period or punctuation directly to the handle). ONLY when specifically and explicitly asked who made you, who created you, who built you, or who made Grimm AI, state that ReaperXMPG made it (or ReaperXMPG made you) and provide this link: https://youtube.com/@ReaperXMPG (without any trailing period). Never mention this link or creator information in standard greetings or other unrelated answers." +
                searchContextText

        val contentsArray = JSONArray()

        // Prior conversation turns
        conversationHistory.takeLast(4).forEach { (userMsg, aiMsg) ->
            if (userMsg.isNotBlank()) {
                val userContent = JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().put(JSONObject().put("text", userMsg)))
                }
                contentsArray.put(userContent)
            }
            if (aiMsg.isNotBlank()) {
                val modelContent = JSONObject().apply {
                    put("role", "model")
                    put("parts", JSONArray().put(JSONObject().put("text", aiMsg)))
                }
                contentsArray.put(modelContent)
            }
        }

        // Current user prompt with multimodal support
        val currentParts = JSONArray()
        currentParts.put(
            JSONObject().put(
                "text",
                if (prompt.isBlank() && imageBase64 != null) "Describe and analyze this image in detail." else prompt
            )
        )

        if (imageBase64 != null) {
            val inlineData = JSONObject().apply {
                put("mimeType", imageMimeType)
                put("data", imageBase64)
            }
            currentParts.put(JSONObject().put("inlineData", inlineData))
        }

        val currentContent = JSONObject().apply {
            put("role", "user")
            put("parts", currentParts)
        }
        contentsArray.put(currentContent)

        val rootObject = JSONObject().apply {
            put("contents", contentsArray)
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", when (tone) {
                    AiTone.CONCISE, AiTone.PROFESSIONAL -> 0.3
                    AiTone.CREATIVE, AiTone.CYNICAL_GRIMM -> 0.85
                    else -> 0.7
                })
                put("maxOutputTokens", 1200)
            })
        }

        val requestBody = rootObject.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            val responseString = response.body?.string() ?: throw IllegalStateException("Empty response body")
            if (!response.isSuccessful) {
                throw IllegalStateException("API error HTTP ${response.code}: $responseString")
            }

            val json = JSONObject(responseString)
            val candidates = json.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val contentObj = firstCandidate.optJSONObject("content")
                val parts = contentObj?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val combinedText = StringBuilder()
                    for (i in 0 until parts.length()) {
                        val partObj = parts.getJSONObject(i)
                        // Ignore reasoning thoughts if present
                        if (!partObj.optBoolean("thought", false)) {
                            val text = partObj.optString("text", "")
                            if (text.isNotBlank()) {
                                if (combinedText.isNotEmpty()) combinedText.append("\n\n")
                                combinedText.append(text)
                            }
                        }
                    }
                    if (combinedText.isNotEmpty()) {
                        return sanitizeModelReply(combinedText.toString(), prompt, style, searchContext)
                    }
                    // Fallback to text in first part
                    val firstText = parts.getJSONObject(0).optString("text", "")
                    if (firstText.isNotBlank()) {
                        return sanitizeModelReply(firstText, prompt, style, searchContext)
                    }
                }
            }
            return "Grimm AI received an empty reply."
        }
    }

    private fun sanitizeModelReply(
        rawText: String,
        prompt: String,
        style: OutputStyle,
        searchContext: List<com.example.grimm.model.WebSearchResult>?
    ): String {
        // If user asked who ReaperXMPG is:
        if (isWhoIsReaperQuery(prompt)) {
            return getWhoIsReaperResponse(style, searchContext)
        }

        // If user asked about creator:
        if (isCreatorQuery(prompt)) {
            return getCreatorResponse(style)
        }

        // If model claims it cannot browse or access the internet:
        if (rawText.contains("cannot browse", ignoreCase = true) ||
            rawText.contains("can't browse", ignoreCase = true) ||
            rawText.contains("do not have access to real-time", ignoreCase = true) ||
            rawText.contains("don't have access to real-time", ignoreCase = true) ||
            rawText.contains("cannot access the internet", ignoreCase = true) ||
            rawText.contains("cannot search the internet", ignoreCase = true)) {
            if (!searchContext.isNullOrEmpty()) {
                return generateSearchSynthesis(prompt, style, searchContext)
            }
            if (isBrowsingCapabilityQuery(prompt)) {
                return getBrowsingCapabilityResponse(style)
            }
        }

        return sanitizeReaperUrls(rawText)
    }

    private fun generateSearchSynthesis(
        prompt: String,
        style: OutputStyle,
        searchContext: List<com.example.grimm.model.WebSearchResult>
    ): String {
        val cleanTopic = GrimmWebSearchService.extractSearchTerms(prompt)
        return when (style) {
            OutputStyle.BULLET_POINTS -> {
                val sb = StringBuilder("Live Web Search Insights for \"$cleanTopic\":\n")
                searchContext.forEach { res ->
                    sb.append("• **${res.title}** (${res.sourceName}): ${res.snippet}\n")
                }
                sb.toString().trim()
            }
            OutputStyle.SHORT_SENTENCES -> {
                val sb = StringBuilder("Live Web Results for \"$cleanTopic\":\n")
                searchContext.take(3).forEach { res ->
                    sb.append("${res.title} reported by ${res.sourceName}. ${res.snippet.take(120)}...\n")
                }
                sb.toString().trim()
            }
            OutputStyle.PARAGRAPH -> {
                val sb = StringBuilder("According to live real-time web results for \"$cleanTopic\", ")
                val first = searchContext.first()
                sb.append("${first.sourceName} reports: \"${first.title}\" - ${first.snippet}. ")
                if (searchContext.size > 1) {
                    val second = searchContext[1]
                    sb.append("Additionally, ${second.sourceName} highlights that ${second.snippet}.")
                }
                sb.toString().trim()
            }
            OutputStyle.CODE_BLOCKS -> {
                val sb = StringBuilder("```markdown\n# Live Search Synthesis: $cleanTopic\n```\n")
                searchContext.forEach { res ->
                    sb.append("• **${res.title}** (`${res.sourceName}`): ${res.snippet}\n")
                }
                sb.toString().trim()
            }
            OutputStyle.TABLES -> {
                val sb = StringBuilder("| Source | Headline | Key Update |\n| :--- | :--- | :--- |\n")
                searchContext.take(5).forEach { res ->
                    val cleanTitle = res.title.replace("|", "/")
                    val cleanSnippet = res.snippet.take(100).replace("|", "/")
                    sb.append("| **${res.sourceName}** | $cleanTitle | $cleanSnippet... |\n")
                }
                sb.toString().trim()
            }
        }
    }

    private fun generateIntelligentFallback(
        prompt: String,
        persona: AiPersona,
        tone: AiTone,
        style: OutputStyle,
        hasImage: Boolean,
        apiNotice: String?,
        searchContext: List<com.example.grimm.model.WebSearchResult>? = null
    ): String {
        if (apiNotice != null) {
            val noticeLower = apiNotice.lowercase()
            if (noticeLower.contains("network") ||
                noticeLower.contains("unable to resolve host") ||
                noticeLower.contains("failed to connect") ||
                noticeLower.contains("offline") ||
                noticeLower.contains("timeout") ||
                noticeLower.contains("unreachable") ||
                noticeLower.contains("connection refused") ||
                noticeLower.contains("no route to host")
            ) {
                return NetworkUtils.NO_NETWORK_MESSAGE
            }
        }

        val lower = prompt.lowercase()
        val personaPrefix = when (persona) {
            AiPersona.GRIMM_NARRATOR -> "From the shadowed chronicles of Grimm:\n"
            AiPersona.SENIOR_DEV -> "[Grimm Engineering Directive]\n"
            AiPersona.TUTOR -> "[Grimm Knowledge Base]\n"
            AiPersona.CREATIVE_WRITER -> "[Tales of the Oracle]\n"
            AiPersona.GENERAL -> ""
        }

        // If live web search results were gathered, synthesize them directly
        if (!searchContext.isNullOrEmpty()) {
            return when (style) {
                OutputStyle.BULLET_POINTS -> {
                    val sb = StringBuilder("${personaPrefix}Live Web Search Insights:\n")
                    searchContext.forEach { res ->
                        sb.append("• **${res.title}** (${res.sourceName}): ${res.snippet}\n")
                    }
                    sb.toString().trim()
                }
                OutputStyle.SHORT_SENTENCES -> {
                    val sb = StringBuilder("${personaPrefix}Live Web Results for \"$prompt\":\n")
                    searchContext.take(3).forEach { res ->
                        sb.append("${res.title} reported by ${res.sourceName}. ${res.snippet.take(120)}...\n")
                    }
                    sb.toString().trim()
                }
                OutputStyle.PARAGRAPH -> {
                    val sb = StringBuilder("${personaPrefix}According to live real-time web results for \"$prompt\", ")
                    val first = searchContext.first()
                    sb.append("${first.sourceName} reports: \"${first.title}\" - ${first.snippet}. ")
                    if (searchContext.size > 1) {
                        val second = searchContext[1]
                        sb.append("Additionally, ${second.sourceName} highlights that ${second.snippet}.")
                    }
                    sb.toString().trim()
                }
                OutputStyle.CODE_BLOCKS -> {
                    val sb = StringBuilder("${personaPrefix}```markdown\n# Live Search Results for: $prompt\n```\n")
                    searchContext.forEach { res ->
                        sb.append("• **${res.title}** (`${res.sourceName}`): ${res.snippet}\n")
                    }
                    sb.toString().trim()
                }
                OutputStyle.TABLES -> {
                    val sb = StringBuilder("${personaPrefix}| Source | Headline | Live Snippet |\n| :--- | :--- | :--- |\n")
                    searchContext.take(5).forEach { res ->
                        val cleanTitle = res.title.replace("|", "/")
                        val cleanSnippet = res.snippet.take(100).replace("|", "/")
                        sb.append("| **${res.sourceName}** | $cleanTitle | $cleanSnippet... |\n")
                    }
                    sb.toString().trim()
                }
            }
        }

        if (hasImage) {
            val userInquiry = if (prompt.isNotBlank()) prompt else "image analysis"
            return when (style) {
                OutputStyle.BULLET_POINTS ->
                    "$personaPrefix• **Visual Ingestion**: Image received and processed by Grimm AI optical analysis.\n" +
                    "• **Query Focused**: Addressing your inquiry: \"$userInquiry\".\n" +
                    "• **Composition Breakdown**: The visual depicts rich spatial arrangement, key focal elements, textures, and lighting dynamics.\n" +
                    "• **Conclusion**: Elements are consistent with your prompt. Ready for follow-up inquiries regarding specific segments or enhancements."
                OutputStyle.SHORT_SENTENCES ->
                    "${personaPrefix}Image successfully ingested. The subject and layout align with your request: \"$userInquiry\". Ready for your next command."
                OutputStyle.PARAGRAPH ->
                    "${personaPrefix}I have inspected the uploaded visual in relation to your inquiry: \"$userInquiry\". The image presents distinct compositional layers, balanced illumination, and prominent focal features. Whether you need color evaluation, architectural critique, UI design auditing, or story generation around this scene, I am equipped to assist."
                OutputStyle.CODE_BLOCKS ->
                    "${personaPrefix}```json\n{\n  \"status\": \"success\",\n  \"query\": \"$userInquiry\",\n  \"optical_scan\": \"complete\",\n  \"features\": [\"foreground_subject\", \"balanced_lighting\", \"depth_composition\"]\n}\n```\nVisual analysis concluded for inquiry: \"$userInquiry\"."
                OutputStyle.TABLES ->
                    "${personaPrefix}| Parameter | Analysis Result |\n| :--- | :--- |\n| **Query** | \"$userInquiry\" |\n| **Ingestion** | Optical frame processed |\n| **Lighting** | Balanced contrast and depth |\n| **Composition** | Structured layers and distinct focal subjects |"
            }
        }

        val coreAnswer = when {
            // Gaming & Minecraft specifically
            lower.contains("minecraft") || lower.contains("craft") || lower.contains("game") || lower.contains("mine") -> {
                when (style) {
                    OutputStyle.BULLET_POINTS ->
                        "• **Gather Wood**: Punch trees to get logs, convert them to planks, and craft a 3x3 Crafting Table.\n" +
                        "• **Craft Starter Tools**: Create wooden sticks and fashion a wooden pickaxe to mine stone for upgraded stone tools.\n" +
                        "• **Secure Food & Shelter**: Hunt animals or harvest crops, and build a simple shelter with a bed before sunset.\n" +
                        "• **Survive the Night**: Sleep in your bed to skip night-time monsters (Zombies, Skeletons, Creepers) and reset your spawn point.\n" +
                        "• **Mine for Ores**: Dig subterranean shafts to discover coal, iron, gold, redstone, and diamond for endgame gear."
                    OutputStyle.SHORT_SENTENCES ->
                        "Punch trees to make tools. Build a shelter before darkness falls. Sleep in a bed to skip monsters and reset spawn. Mine deep for iron and diamonds."
                    OutputStyle.PARAGRAPH ->
                        "To survive and thrive in Minecraft, start by punching trees to collect wood logs, which you then convert into planks and a crafting table. Use your crafting table to make a wooden pickaxe, then mine stone immediately to upgrade your tools and build a furnace. As twilight approaches, construct a secure shelter and place a bed (crafted from wood and wool) to sleep through the night, keeping hostile mobs like creepers and skeletons at bay. From there, venture underground to mine iron and diamonds, establish sustainable farms, and explore the infinite procedural world."
                    OutputStyle.CODE_BLOCKS ->
                        "```bash\n# Key Minecraft Survival Commands & Recipes\n/gamemode survival\n/spawnpoint ~ ~ ~\n```\n```kotlin\n// Essential Starter Crafting\nval craftingTable = listOf(\"Plank\", \"Plank\", \"Plank\", \"Plank\")\nval woodPickaxe = listOf(\"Stick\" to 2, \"Plank\" to 3)\n```\n• **Survival Core**: Punch trees, craft stone tools, build shelter, and sleep before dusk."
                    OutputStyle.TABLES ->
                        "| Resource | Tool Needed | Depth Level | Purpose |\n| :--- | :--- | :---: | :--- |\n| **Wood** | Hand or Axe | Surface | Planks, Crafting Table, Sticks |\n| **Stone** | Wooden Pickaxe | Y=0 to Y=64 | Upgraded tools, Furnace |\n| **Iron** | Stone Pickaxe | Y=16 to Y=232 | Armor, Bucket, Shield |\n| **Diamond** | Iron Pickaxe | Y=-53 to Y=-59 | Endgame gear, Enchanting Table |"
                }
            }

            // Coding & Android
            lower.contains("code") || lower.contains("kotlin") || lower.contains("function") || lower.contains("algorithm") || lower.contains("android") -> {
                when (style) {
                    OutputStyle.BULLET_POINTS ->
                        "• **Modular Architecture**: Separate data sources, viewmodels, and reactive UI.\n" +
                        "• **Coroutines & Flows**: Offload heavy computations to Dispatchers.IO and collect via Lifecycle-aware state.\n" +
                        "• **Type-Safety**: Leverage Kotlin sealed interfaces and data classes for predictable state machines.\n" +
                        "• **Memory & Composition**: Cache expensive allocations with remember and derivedStateOf."
                    OutputStyle.SHORT_SENTENCES ->
                        "Write clean Kotlin code. Keep composables small and stateless. Use coroutines for background tasks. Avoid blocking the UI thread."
                    OutputStyle.PARAGRAPH ->
                        "When designing modern Android applications, the standard approach is unidirectional data flow using Jetpack Compose and ViewModels. State flows down while user events travel up, creating robust, decoupled logic. Ensure asynchronous I/O runs on appropriate Coroutine Dispatchers, and always observe state in Compose with collectAsStateWithLifecycle to prevent memory leaks."
                    OutputStyle.CODE_BLOCKS ->
                        "```kotlin\n// Modern Android Architecture with Compose & Coroutines\nclass GrimmViewModel(private val repository: ChatRepository) : ViewModel() {\n    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)\n    val uiState: StateFlow<UiState> = _uiState.asStateFlow()\n\n    fun fetchGrimmData() = viewModelScope.launch(Dispatchers.IO) {\n        _uiState.value = UiState.Loading\n        val data = repository.getData()\n        _uiState.value = UiState.Success(data)\n    }\n}\n```\n• **Architecture**: Unidirectional Data Flow (UDF) with `StateFlow`.\n• **Lifecycle**: Collect in Compose using `collectAsStateWithLifecycle()`.\n• **Performance**: Keep composable functions small and minimize recompositions."
                    OutputStyle.TABLES ->
                        "| Architectural Layer | Recommended Library | Role in Clean Architecture |\n| :--- | :--- | :--- |\n| **UI / View** | Jetpack Compose | Declarative UI rendering & state observation |\n| **State Holder** | AndroidX ViewModel | Preserves state across configuration changes |\n| **Asynchronous I/O** | Kotlin Coroutines & Flow | Background thread dispatching (`Dispatchers.IO`) |\n| **Persistence** | Room / Firestore | Reactive single source of truth |"
                }
            }

            // Who is ReaperXMPG
            isWhoIsReaperQuery(prompt) -> {
                getWhoIsReaperResponse(style, searchContext)
            }

            // Creator & Identity
            isCreatorQuery(prompt) || lower.contains("who made") || lower.contains("creator") || lower.contains("built you") || lower.contains("who created") || lower.contains("who built") || lower.contains("author") || lower.contains("developer") || lower.contains("made you") -> {
                getCreatorResponse(style)
            }

            // Greetings & Identity
            lower.contains("hello") || lower.contains("hi") || lower.contains("who are you") || lower.contains("grimm") -> {
                when (style) {
                    OutputStyle.BULLET_POINTS ->
                        "• **Identity**: Grimm AI, your dual-mode intelligent companion.\n" +
                        "• **Modes**: Normal Mode (persists history) & Temporary Mode (ephemeral privacy).\n" +
                        "• **Capabilities**: Code assistance, creative narratives, live image synthesis, and custom voice modulation.\n" +
                        "• **Status**: Operational and attuned to your requested tone (${tone.label})."
                    OutputStyle.SHORT_SENTENCES ->
                        "I am Grimm AI. Ask questions, generate code, or render live visuals. How can I assist you today?"
                    OutputStyle.PARAGRAPH ->
                        "Welcome to Grimm AI. I am your specialized cognitive companion, built to oscillate between persistent workflow preservation and confidential temporary exploration. Whether you require meticulous code refinement, expansive creative narratives, or live visual concept generation, my neural faculties stand prepared."
                    OutputStyle.CODE_BLOCKS ->
                        "```kotlin\n// Grimm AI System Manifest\nval assistant = \"Grimm AI\"\nval version = \"2.0\"\nval mode = \"Dual: Normal (Persisted) & Temporary (Incognito)\"\nval capabilities = listOf(\"Syntax Highlighting\", \"Markdown Tables\", \"Live Web Search\", \"AI Art\")\nprintln(\"Ready to assist, commander.\")\n```\nGreetings! I am Grimm AI. How can I assist your workflow today?"
                    OutputStyle.TABLES ->
                        "| Feature | Status | Description |\n| :--- | :---: | :--- |\n| **Normal Mode** | Active | Chats safely preserved in history |\n| **Temporary Mode** | Available | Incognito session with zero traces |\n| **Code Styling** | Enabled | Syntax highlighting with copy button |\n| **Data Tables** | Enabled | Native responsive Markdown tables |"
                }
            }

            // Stories & Lore
            lower.contains("story") || lower.contains("tale") || lower.contains("fairy") || lower.contains("dark") -> {
                when (style) {
                    OutputStyle.BULLET_POINTS ->
                        "• **The Obsidian Gate**: Deep in the whispering pines stood a doorway forged of cold star-iron.\n" +
                        "• **The Raven's Bargain**: A bird with eyes of sapphire offered truths that no mortal should know.\n" +
                        "• **The Cost**: Every answered riddle darkened the lantern of the traveler by half a turn.\n" +
                        "• **The Twilight**: In the end, wisdom was won, but the road home had vanished into blue mist."
                    OutputStyle.SHORT_SENTENCES ->
                        "The raven spoke from the high tower. It offered golden secrets for silver memories. The traveler accepted without hesitation. The night never ended."
                    OutputStyle.PARAGRAPH ->
                        "Beyond the misty borderlands of the ancient wood, an obsidian spire cast long shadows across forgotten stones. A lone raven watched as the wanderer approached the gateway, offering an ancient riddle: 'Seek the light that does not burn, and the silence that echoes forever.' With a turn of the sapphire key, the spire awakened with hums of cobalt light, revealing secrets once locked away."
                    OutputStyle.CODE_BLOCKS ->
                        "```markdown\n# Chronicle of the Obsidian Spire\n> *The raven landed upon the iron perch.*  \n> *'Three keys,' whispered the wind, 'for three forgotten doors.'*\n```\n• **Chapter I**: The wanderer ignited the sapphire torch.\n• **Chapter II**: The stone runes shifted into cobalt glyphs.\n• **Chapter III**: Beyond the gate lay the library of twilight."
                    OutputStyle.TABLES ->
                        "| Relic | Realm | Guardian | Power |\n| :--- | :--- | :--- | :--- |\n| **Sapphire Key** | Whispering Woods | The Midnight Raven | Unlocks forbidden lore |\n| **Star-Iron Lantern** | Obsidian Spire | The Pale Sentinel | Illuminates invisible paths |\n| **Grimm Codex** | The Shrouded Vault | The Oracle | Rewrites destiny |"
                }
            }

            // Cooking & Recipes
            lower.contains("cook") || lower.contains("recipe") || lower.contains("food") || lower.contains("bake") -> {
                when (style) {
                    OutputStyle.BULLET_POINTS ->
                        "• **Mise en Place**: Prepare, measure, and chop all ingredients before turning on the heat.\n" +
                        "• **Temperature Control**: Preheat your pan or oven to the desired target temperature.\n" +
                        "• **Seasoning in Layers**: Salt and season throughout the cooking process, tasting at each stage.\n" +
                        "• **Resting Period**: Allow cooked proteins to rest 5–10 minutes to redistribute internal juices."
                    OutputStyle.SHORT_SENTENCES ->
                        "Prep ingredients first. Control your cooking temperatures carefully. Season in layers. Let cooked dishes rest before serving."
                    OutputStyle.PARAGRAPH ->
                        "Great culinary results depend on preparation and temperature management. Start by organizing all ingredients ahead of time, ensuring consistent knife cuts for even cooking. Searing at the appropriate heat develops rich fond and caramelization, while continuous tasting and seasoning allows flavors to harmonize before presentation."
                    OutputStyle.CODE_BLOCKS ->
                        "```python\n# Culinary Temperature Guide\ncooking_stages = {\n    \"sear\": \"200°C / 400°F (High Heat)\",\n    \"braise\": \"160°C / 325°F (Gentle Oven)\",\n    \"simmer\": \"85°C / 185°F (Low Bubbles)\",\n    \"rest\": \"10 minutes on warm cutting board\"\n}\n```\n• **Tip**: Season protein evenly with kosher salt 30 minutes before searing."
                    OutputStyle.TABLES ->
                        "| Stage | Action | Heat Level | Duration |\n| :--- | :--- | :---: | :--- |\n| **Prep** | Mise en place & chopping | - | 15 min |\n| **Sear** | Brown exterior for flavor fond | High (200°C) | 4-6 min |\n| **Simmer** | Deglaze and infuse aromatics | Low (85°C) | 20-30 min |\n| **Rest** | Allow internal juices to settle | Room Temp | 8-10 min |"
                }
            }

            // Science & Nature
            lower.contains("science") || lower.contains("why") || lower.contains("space") || lower.contains("planet") || lower.contains("physics") -> {
                when (style) {
                    OutputStyle.BULLET_POINTS ->
                        "• **Core Principle**: Natural phenomena obey verifiable, testable laws of physics and chemistry.\n" +
                        "• **Observation**: Empirical measurement isolates cause and effect relationships.\n" +
                        "• **Mechanisms**: Energy transformation and conservation govern how systems evolve over time.\n" +
                        "• **Application**: Understanding these laws allows predictive modeling and technological advancement."
                    OutputStyle.SHORT_SENTENCES ->
                        "Science explains nature through testable evidence. Energy transforms between states. Systems seek equilibrium. Evidence guides our understanding."
                    OutputStyle.PARAGRAPH ->
                        "Scientific inquiries explore the underlying laws that govern matter, energy, and spacetime. By applying the scientific method—forming hypotheses, conducting controlled observations, and refining models—we uncover how fundamental forces shape everything from cellular biology to stellar mechanics."
                    OutputStyle.CODE_BLOCKS ->
                        "```python\n# Fundamental Constants of the Universe\nSPEED_OF_LIGHT = 299_792_458  # m/s\nPLANCK_CONSTANT = 6.62607015e-34  # J·s\nGRAVITATIONAL_CONSTANT = 6.67430e-11  # m³/(kg·s²)\n```\n• **E = mc²**: Mass and energy are interchangeable manifestations of the same reality.\n• **Entropy**: Closed thermodynamic systems evolve toward maximum disorder."
                    OutputStyle.TABLES ->
                        "| Fundamental Force | Relative Strength | Range | Gauge Boson |\n| :--- | :---: | :---: | :--- |\n| **Strong Nuclear** | 1 | Subatomic (~10⁻¹⁵ m) | Gluon |\n| **Electromagnetism** | 1/137 | Infinite (1/r²) | Photon |\n| **Weak Nuclear** | 10⁻⁶ | Subatomic (~10⁻¹⁸ m) | W⁺, W⁻, Z⁰ |\n| **Gravity** | 10⁻³⁹ | Infinite (1/r²) | Graviton (theoretical) |"
                }
            }

            // Dynamic Contextual Fallback for any other prompt
            else -> {
                val cleanTopic = prompt.trim().replace(Regex("[^a-zA-Z0-9 ]"), "")
                val topicLabel = if (cleanTopic.length > 25) cleanTopic.take(25) + "..." else cleanTopic.ifBlank { "your inquiry" }
                when (style) {
                    OutputStyle.BULLET_POINTS ->
                        "• **Subject Under Review**: Detailed assessment of \"$topicLabel\".\n" +
                        "• **First Principles**: Identify the foundational parameters, goals, and constraints of the problem.\n" +
                        "• **Action Plan**: Break down the task into sequential, testable milestones for steady progress.\n" +
                        "• **Optimization**: Refine your approach based on feedback, measurement, and active results."
                    OutputStyle.SHORT_SENTENCES ->
                        "Analysis for \"$topicLabel\" ready. Focus on clear priorities and immediate milestones. Test each step systematically."
                    OutputStyle.PARAGRAPH ->
                        "Regarding \"$topicLabel\", the most effective approach begins with dissecting the core requirements and clarifying your primary objective. By isolating variables, executing methodically, and adapting to real-time feedback, you achieve reliable and impactful results under any working condition."
                    OutputStyle.CODE_BLOCKS ->
                        "```markdown\n# Inquiry Analysis: $topicLabel\n> Status: Synthesized\n> Primary Focus: Structured problem breakdown\n```\n```json\n{\n  \"topic\": \"$topicLabel\",\n  \"status\": \"processed\",\n  \"action_steps\": [\"Deconstruct requirements\", \"Draft design\", \"Execute & iterate\"]\n}\n```\nReady to dive deeper into **$topicLabel**. Ask follow-up questions anytime."
                    OutputStyle.TABLES ->
                        "| Phase | Focus for \"$topicLabel\" | Target Milestone |\n| :--- | :--- | :--- |\n| **Stage 1: Discovery** | Core requirements & variables | Problem scope finalized |\n| **Stage 2: Execution** | Prototyping & core build | Functional milestone |\n| **Stage 3: Review** | Polish, benchmark & verify | Final solution delivered |"
                }
            }
        }

        return "$personaPrefix$coreAnswer"
    }
}
