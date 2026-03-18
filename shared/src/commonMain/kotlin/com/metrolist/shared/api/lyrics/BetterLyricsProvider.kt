package com.metrolist.shared.api.lyrics

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class BetterLyricsResponse(val ttml: String)

class BetterLyricsProvider(private val httpClient: HttpClient) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getLyrics(
        title: String,
        artist: String,
        duration: Long?,
        album: String? = null,
    ): String? = try {
        val response = httpClient.get("https://lyrics-api.boidu.dev/getLyrics") {
            parameter("s", title)
            parameter("a", artist)
            if (duration != null && duration > 0) parameter("d", duration)
            if (!album.isNullOrBlank()) parameter("al", album)
            header("Accept", "application/json")
        }
        if (response.status == HttpStatusCode.OK) {
            val ttml = json.decodeFromString<BetterLyricsResponse>(response.body<String>()).ttml
            parseTtmlToLrc(ttml).takeIf { it.isNotBlank() }
        } else null
    } catch (_: Exception) { null }
}

// ── Regex-based TTML → LRC conversion (KMP-safe, no DOM APIs) ─────────────

private val pBlockRegex   = Regex("""<p\s([^>]*)>(.*?)</p>""", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
private val spanRegex     = Regex("""<span\s([^>]*)>(.*?)</span>""", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
private val attrBeginRegex = Regex("""begin="([^"]+)"""")
private val attrEndRegex   = Regex("""end="([^"]+)"""")
private val attrRoleRegex  = Regex("""(?:ttm:)?role="([^"]+)"""")
private val anyTagRegex    = Regex("""<[^>]+>""")

private fun parseTtmlToLrc(ttml: String): String {
    val sb = StringBuilder()
    pBlockRegex.findAll(ttml).forEach { pMatch ->
        val pAttrs   = pMatch.groupValues[1]
        val pContent = pMatch.groupValues[2]
        val pBegin   = attrBeginRegex.find(pAttrs)?.groupValues?.get(1) ?: return@forEach
        val startSec = parseTime(pBegin)

        data class Word(val text: String, val startSec: Double, val endSec: Double)
        val words = mutableListOf<Word>()

        spanRegex.findAll(pContent).forEach { spanMatch ->
            val spanAttrs = spanMatch.groupValues[1]
            val spanText  = stripTags(spanMatch.groupValues[2]).trim()
            val role = attrRoleRegex.find(spanAttrs)?.groupValues?.get(1) ?: ""
            if (role in listOf("x-bg", "x-translation", "x-roman")) return@forEach
            if (spanText.isEmpty()) return@forEach
            val wBegin = attrBeginRegex.find(spanAttrs)?.groupValues?.get(1) ?: return@forEach
            val wEnd   = attrEndRegex.find(spanAttrs)?.groupValues?.get(1) ?: return@forEach
            words.add(Word(spanText, parseTime(wBegin), parseTime(wEnd)))
        }

        val lineText = if (words.isNotEmpty()) words.joinToString(" ") { it.text }
                       else stripTags(pContent).trim()
        if (lineText.isEmpty()) return@forEach

        val timeMs = (startSec * 1000).toLong()
        val min    = timeMs / 60000
        val sec    = (timeMs % 60000) / 1000
        val cs     = (timeMs % 1000) / 10
        val ts = "[${min.pad()}:${sec.pad()}.${cs.pad()}]"
        sb.appendLine("$ts$lineText")

        if (words.isNotEmpty()) {
            val wordsData = words.joinToString("|") { "${it.text}:${it.startSec}:${it.endSec}" }
            sb.appendLine("<$wordsData>")
        }
    }
    return sb.toString()
}

private fun stripTags(s: String) = s.replace(anyTagRegex, "")

private fun Long.pad() = toString().padStart(2, '0')

private fun parseTime(t: String): Double = try {
    val parts = t.trim().split(":")
    when (parts.size) {
        2 -> parts[0].toDouble() * 60 + parts[1].toDouble()
        3 -> parts[0].toDouble() * 3600 + parts[1].toDouble() * 60 + parts[2].toDouble()
        else -> t.toDoubleOrNull() ?: 0.0
    }
} catch (_: Exception) { 0.0 }
