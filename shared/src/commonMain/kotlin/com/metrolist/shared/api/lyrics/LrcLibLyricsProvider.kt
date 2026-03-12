package com.metrolist.shared.api.lyrics

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
data class LrcLibResponse(
    val id: Long,
    val name: String,
    val artistName: String,
    val albumName: String? = null,
    val duration: Float,
    val instrumental: Boolean,
    val plainLyrics: String? = null,
    val syncedLyrics: String? = null
)

class LrcLibLyricsProvider(private val httpClient: HttpClient) {
    private val baseUrl = "https://lrclib.net/api"

    suspend fun getLyrics(title: String, artist: String, duration: Long?, album: String? = null): String? {
        return try {
            val response: List<LrcLibResponse> = httpClient.get("$baseUrl/search") {
                parameter("track_name", title)
                parameter("artist_name", artist)
                if (album != null) parameter("album_name", album)
                if (duration != null) parameter("duration", duration / 1000)
            }.body()

            val bestMatch = response.firstOrNull()
            bestMatch?.syncedLyrics ?: bestMatch?.plainLyrics
        } catch (e: Exception) {
            null
        }
    }
}
