package com.metrolist.shared.api.lyrics

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.math.abs

class SimpMusicLyricsProvider(private val httpClient: HttpClient) {
    private val baseUrl = "https://api-lyrics.simpmusic.org/v1"
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    suspend fun getLyrics(videoId: String, duration: Long? = null): String? {
        return try {
            val responseText = httpClient.get("$baseUrl/$videoId") {
                header("User-Agent", "SimpMusicLyrics/1.0")
                header("Accept", "application/json")
            }.bodyAsText()

            if (responseText.isBlank()) return null

            val root = json.parseToJsonElement(responseText).jsonObject
            if (root["type"]?.jsonPrimitive?.content != "success") return null

            val data = root["data"]?.jsonArray ?: return null
            if (data.isEmpty()) return null

            // Filter to duration-matching tracks (within 10 seconds) if duration provided
            val tracks = if (duration != null && duration > 0) {
                val durationSecs = (duration / 1000).toInt()
                val filtered = data.filter { el ->
                    val trackDur = el.jsonObject["durationSeconds"]?.jsonPrimitive?.intOrNull ?: 0
                    trackDur == 0 || abs(trackDur - durationSecs) <= 10
                }
                filtered.ifEmpty { data.toList() }
            } else {
                data.toList()
            }

            val best = if (duration != null) {
                val durationSecs = (duration / 1000).toInt()
                tracks.minByOrNull { el ->
                    val trackDur = el.jsonObject["durationSeconds"]?.jsonPrimitive?.intOrNull ?: 0
                    abs(trackDur - durationSecs)
                }
            } else {
                tracks.firstOrNull()
            } ?: return null

            val obj = best.jsonObject
            // Prefer richSyncLyrics (word-level) → syncedLyrics → null
            obj["richSyncLyrics"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
                ?: obj["syncedLyrics"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }
}
