package com.metrolist.shared.api.lyrics

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

class LyricsPlusProvider(private val httpClient: HttpClient) {
    private val backends = listOf(
        "https://lyricsplus.binimum.org",
        "https://lyricsplus.atomix.one",
        "https://lyricsplus-seven.vercel.app"
    )

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getLyrics(title: String, artist: String, duration: Long?, videoId: String? = null): String? {
        for (backend in backends) {
            try {
                val responseText = httpClient.get("$backend/v2/lyrics/get") {
                    parameter("title", title)
                    parameter("artist", artist)
                    if (duration != null) parameter("duration", duration / 1000)
                    if (videoId != null) parameter("videoId", videoId)
                    parameter("sources", "apple,lyricsplus,musixmatch,spotify,musixmatch-word")
                }.bodyAsText()

                if (responseText.isBlank()) continue

                val jsonObj = json.parseToJsonElement(responseText).jsonObject
                val lyricsArray = jsonObj["lyrics"]?.jsonArray ?: continue
                if (lyricsArray.isEmpty()) continue

                val lrc = buildString {
                    for (line in lyricsArray) {
                        val lineObj = line.jsonObject
                        val time = lineObj["time"]?.jsonPrimitive?.longOrNull ?: continue
                        val text = lineObj["text"]?.jsonPrimitive?.content ?: continue
                        appendLine("${time.toLrcTimestamp()}$text")
                    }
                }.trim()

                if (lrc.isNotBlank()) return lrc
            } catch (_: Exception) {}
        }
        return null
    }

    private fun Long.toLrcTimestamp(): String {
        val minutes = this / 60000
        val seconds = (this % 60000) / 1000
        val centiseconds = (this % 1000) / 10
        return "[%02d:%02d.%02d]".format(minutes, seconds, centiseconds)
    }
}
