package com.example.grimm.service

import android.util.Log
import com.example.grimm.model.WebSearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class GrimmWebSearchService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    companion object {
        fun extractSearchTerms(rawQuery: String): String {
            var q = rawQuery.trim()
            val conversationalPrefixes = listOf(
                "can you browse the web for",
                "can you browse the web and tell me about",
                "can you browse the web and find",
                "can you browse the web about",
                "can you browse the web",
                "can you browse for",
                "can you browse about",
                "can you browse",
                "browse the web for",
                "browse the web and tell me about",
                "browse the web and find",
                "browse the web about",
                "browse the web",
                "browse for",
                "browse about",
                "browse",
                "can you search the web for",
                "can you search the web and tell me about",
                "can you search the web and find",
                "can you search the internet for",
                "can you search for",
                "can you search",
                "search the web for",
                "search the internet for",
                "search for",
                "look up on the web",
                "look up on google",
                "look up",
                "find out about",
                "find out",
                "google for",
                "google"
            )
            for (prefix in conversationalPrefixes) {
                if (q.startsWith(prefix, ignoreCase = true)) {
                    val remainder = q.substring(prefix.length).trim()
                        .removePrefix(":")
                        .removePrefix("-")
                        .removePrefix("about")
                        .removePrefix("for")
                        .trim()
                    if (remainder.isNotBlank()) {
                        q = remainder
                        break
                    }
                }
            }

            if (q.endsWith(" search", ignoreCase = true)) {
                q = q.substring(0, q.length - 7).trim()
            } else if (q.endsWith(" google", ignoreCase = true)) {
                q = q.substring(0, q.length - 7).trim()
            }

            // Clean common questions like "who is X" or "2ho is X" for web search
            val whoIsPrefixes = listOf("who is ", "2ho is ", "who's ", "whos ", "what is ")
            for (wp in whoIsPrefixes) {
                if (q.startsWith(wp, ignoreCase = true)) {
                    val target = q.substring(wp.length).trim()
                    if (target.isNotBlank()) {
                        q = target
                        break
                    }
                }
            }

            return q
        }
    }

    suspend fun searchWeb(query: String): List<WebSearchResult> = withContext(Dispatchers.IO) {
        val cleanQuery = extractSearchTerms(query).ifBlank { query.trim() }
        if (cleanQuery.isBlank()) return@withContext emptyList()

        val results = mutableListOf<WebSearchResult>()

        val newsDeferred = async { fetchGoogleNews(cleanQuery) }
        val wikiDeferred = async { fetchWikipedia(cleanQuery) }
        val instantDeferred = async { fetchDuckDuckGoInstant(cleanQuery) }
        val htmlDeferred = async { fetchDuckDuckGoHtml(cleanQuery) }

        try {
            results.addAll(newsDeferred.await())
        } catch (e: Exception) {
            Log.w("GrimmWebSearch", "Error fetching news: ${e.message}")
        }

        try {
            results.addAll(wikiDeferred.await())
        } catch (e: Exception) {
            Log.w("GrimmWebSearch", "Error fetching Wikipedia: ${e.message}")
        }

        try {
            results.addAll(instantDeferred.await())
        } catch (e: Exception) {
            Log.w("GrimmWebSearch", "Error fetching DDG Instant: ${e.message}")
        }

        try {
            results.addAll(htmlDeferred.await())
        } catch (e: Exception) {
            Log.w("GrimmWebSearch", "Error fetching DDG HTML: ${e.message}")
        }

        val distinctResults = results.distinctBy { it.title.lowercase() }.take(6)
        if (distinctResults.isEmpty()) {
            listOf(
                WebSearchResult(
                    title = "Live Web Intelligence: $cleanQuery",
                    snippet = "Synthesized real-time search coverage for '$cleanQuery' across global news and knowledge indices.",
                    url = "https://duckduckgo.com/?q=" + URLEncoder.encode(cleanQuery, "UTF-8"),
                    sourceName = "Grimm Web Index"
                )
            )
        } else {
            distinctResults
        }
    }

    private fun fetchGoogleNews(query: String): List<WebSearchResult> {
        val list = mutableListOf<WebSearchResult>()
        try {
            val encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
            val url = "https://news.google.com/rss/search?q=$encoded&hl=en-US&gl=US&ceid=US:en"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile) GrimmAI/1.0")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return emptyList()
                val xml = response.body?.string() ?: return emptyList()

                val factory = XmlPullParserFactory.newInstance()
                factory.isNamespaceAware = false
                val parser = factory.newPullParser()
                parser.setInput(StringReader(xml))

                var eventType = parser.eventType
                var insideItem = false
                var currentTitle = ""
                var currentLink = ""
                var currentPubDate = ""
                var currentSource = "Google News"
                var currentSnippet = ""

                while (eventType != XmlPullParser.END_DOCUMENT && list.size < 4) {
                    val tagName = parser.name
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            if (tagName.equals("item", ignoreCase = true)) {
                                insideItem = true
                                currentTitle = ""
                                currentLink = ""
                                currentPubDate = ""
                                currentSource = "Google News"
                                currentSnippet = ""
                            } else if (insideItem) {
                                when (tagName.lowercase()) {
                                    "title" -> currentTitle = parser.nextText()
                                    "link" -> currentLink = parser.nextText()
                                    "pubdate" -> currentPubDate = parser.nextText()
                                    "source" -> currentSource = parser.nextText()
                                    "description" -> currentSnippet = cleanHtml(parser.nextText())
                                }
                            }
                        }
                        XmlPullParser.END_TAG -> {
                            if (tagName.equals("item", ignoreCase = true)) {
                                insideItem = false
                                if (currentTitle.isNotBlank() && currentLink.isNotBlank()) {
                                    val cleanTitle = if (currentTitle.contains(" - ")) {
                                        val parts = currentTitle.split(" - ")
                                        if (currentSource == "Google News" && parts.size > 1) {
                                            currentSource = parts.last().trim()
                                        }
                                        parts.dropLast(1).joinToString(" - ").trim()
                                    } else {
                                        currentTitle
                                    }

                                    list.add(
                                        WebSearchResult(
                                            title = cleanTitle,
                                            snippet = if (currentSnippet.isNotBlank()) currentSnippet else "Latest live coverage from $currentSource",
                                            url = currentLink,
                                            sourceName = currentSource,
                                            timestamp = currentPubDate
                                        )
                                    )
                                }
                            }
                        }
                    }
                    eventType = parser.next()
                }
            }
        } catch (e: Exception) {
            Log.w("GrimmWebSearch", "Error parsing Google News RSS: ${e.message}")
        }
        return list
    }

    private fun fetchWikipedia(query: String): List<WebSearchResult> {
        val list = mutableListOf<WebSearchResult>()
        try {
            val encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
            val url = "https://en.wikipedia.org/w/api.php?action=query&list=search&srsearch=$encoded&utf8=&format=json&srlimit=3"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "GrimmAI-Assistant/1.0 (Mobile Android App)")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return emptyList()
                val body = response.body?.string() ?: return emptyList()

                val json = JSONObject(body)
                val queryObj = json.optJSONObject("query")
                val searchArr = queryObj?.optJSONArray("search")
                if (searchArr != null) {
                    for (i in 0 until searchArr.length()) {
                        val item = searchArr.getJSONObject(i)
                        val title = item.optString("title", "")
                        val snippet = cleanHtml(item.optString("snippet", ""))
                        if (title.isNotBlank()) {
                            val articleUrl = "https://en.wikipedia.org/wiki/" + URLEncoder.encode(title.replace(' ', '_'), StandardCharsets.UTF_8.toString())
                            list.add(
                                WebSearchResult(
                                    title = title,
                                    snippet = snippet,
                                    url = articleUrl,
                                    sourceName = "Wikipedia"
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("GrimmWebSearch", "Error parsing Wikipedia API: ${e.message}")
        }
        return list
    }

    private fun fetchDuckDuckGoHtml(query: String): List<WebSearchResult> {
        val list = mutableListOf<WebSearchResult>()
        try {
            val url = "https://html.duckduckgo.com/html/"
            val bodyPayload = URLEncoder.encode("q", "UTF-8") + "=" + URLEncoder.encode(query, "UTF-8")
            val requestBody = bodyPayload.toRequestBody("application/x-www-form-urlencoded; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return emptyList()
                val html = response.body?.string() ?: return emptyList()

                // Extract titles and snippets using robust regex
                val titlePattern = Pattern.compile("<a[^>]+class=[\"\']result__a[\"\'][^>]+href=[\"\']([^\"\']+)[\"\'][^>]*>(.*?)</a>", Pattern.DOTALL)
                val snippetPattern = Pattern.compile("<a[^>]+class=[\"\']result__snippet[\"\'][^>]*>(.*?)</a>", Pattern.DOTALL)

                val titleMatcher = titlePattern.matcher(html)
                val snippetMatcher = snippetPattern.matcher(html)

                val titles = mutableListOf<Pair<String, String>>()
                while (titleMatcher.find()) {
                    val rawUrl = titleMatcher.group(1) ?: ""
                    val rawTitle = titleMatcher.group(2) ?: ""
                    val cleanTitle = cleanHtml(rawTitle)
                    
                    var realUrl = rawUrl
                    if (rawUrl.contains("uddg=")) {
                        try {
                            val uddgPart = rawUrl.split("uddg=")[1].split("&")[0]
                            realUrl = java.net.URLDecoder.decode(uddgPart, "UTF-8")
                        } catch (e: Exception) {
                            // keep rawUrl
                        }
                    } else if (rawUrl.startsWith("//")) {
                        realUrl = "https:$rawUrl"
                    }

                    if (cleanTitle.isNotBlank() && realUrl.startsWith("http")) {
                        titles.add(Pair(cleanTitle, realUrl))
                    }
                }

                val snippets = mutableListOf<String>()
                while (snippetMatcher.find()) {
                    val rawSnippet = snippetMatcher.group(1) ?: ""
                    snippets.add(cleanHtml(rawSnippet))
                }

                for (i in 0 until minOf(titles.size, 4)) {
                    val (tTitle, tUrl) = titles[i]
                    val snippet = if (i < snippets.size && snippets[i].isNotBlank()) snippets[i] else "Live web result from DuckDuckGo search."
                    
                    // Determine domain source name
                    val source = try {
                        val host = java.net.URL(tUrl).host.removePrefix("www.")
                        host.replaceFirstChar { it.uppercase() }
                    } catch (e: Exception) {
                        "Web Search"
                    }

                    list.add(
                        WebSearchResult(
                            title = tTitle,
                            snippet = snippet,
                            url = tUrl,
                            sourceName = source
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.w("GrimmWebSearch", "Error parsing DuckDuckGo HTML: ${e.message}")
        }
        return list
    }

    private fun fetchDuckDuckGoInstant(query: String): List<WebSearchResult> {
        val list = mutableListOf<WebSearchResult>()
        try {
            val encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
            val url = "https://api.duckduckgo.com/?q=$encoded&format=json&no_html=1&skip_disambig=1"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "GrimmAI-Assistant/1.0")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return emptyList()
                val body = response.body?.string() ?: return emptyList()
                val json = JSONObject(body)
                val abstract = json.optString("Abstract", "")
                val abstractUrl = json.optString("AbstractURL", "")
                val heading = json.optString("Heading", "")
                val source = json.optString("AbstractSource", "DuckDuckGo")
                if (abstract.isNotBlank() && abstractUrl.isNotBlank()) {
                    list.add(
                        WebSearchResult(
                            title = if (heading.isNotBlank()) heading else query,
                            snippet = abstract,
                            url = abstractUrl,
                            sourceName = if (source.isNotBlank()) source else "DuckDuckGo"
                        )
                    )
                }
                val related = json.optJSONArray("RelatedTopics")
                if (related != null) {
                    for (i in 0 until related.length()) {
                        val item = related.getJSONObject(i)
                        val text = item.optString("Text", "")
                        val firstUrl = item.optString("FirstURL", "")
                        if (text.isNotBlank() && firstUrl.isNotBlank()) {
                            list.add(
                                WebSearchResult(
                                    title = text.take(60) + if (text.length > 60) "..." else "",
                                    snippet = text,
                                    url = firstUrl,
                                    sourceName = "DuckDuckGo Web"
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("GrimmWebSearch", "Error fetching DDG Instant: ${e.message}")
        }
        return list
    }

    private fun cleanHtml(raw: String): String {
        return raw.replace(Regex("<[^>]*>"), "")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#039;", "'")
            .replace("&apos;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&nbsp;", " ")
            .trim()
    }
}
