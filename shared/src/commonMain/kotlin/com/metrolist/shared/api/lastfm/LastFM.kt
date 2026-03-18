package com.metrolist.shared.api.lastfm

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Parameters
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.security.MessageDigest

@Serializable
data class LastFmSession(
    val name: String = "",
    val key: String = "",
    val subscriber: Int = 0
)

@Serializable
data class LastFmAuthResponse(
    val session: LastFmSession? = null,
    val error: Int? = null,
    val message: String? = null
)

object LastFM {
    private var apiKey: String = ""
    private var secret: String = ""
    var sessionKey: String? = null

    private val json = Json { 
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    fun initialize(apiKey: String, secret: String) {
        this.apiKey = apiKey
        this.secret = secret
    }

    private fun generateSignature(params: Map<String, String>): String {
        val signature = params.toSortedMap().entries.joinToString("") { it.key + it.value } + secret
        return MessageDigest.getInstance("MD5").digest(signature.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    suspend fun getMobileSession(httpClient: HttpClient, username: String, password: String): Result<LastFmAuthResponse> = runCatching {
        val params = mutableMapOf(
            "method" to "auth.getMobileSession",
            "username" to username,
            "password" to password,
            "api_key" to apiKey
        )
        val apiSig = generateSignature(params)
        
        val response = httpClient.submitForm(
            url = "https://ws.audioscrobbler.com/2.0/",
            formParameters = Parameters.build {
                params.forEach { (k, v) -> append(k, v) }
                append("api_sig", apiSig)
                append("format", "json")
            }
        )
        
        val bodyText = response.bodyAsText()
        println("Last.fm Raw Response: $bodyText")

        val jsonElement = json.parseToJsonElement(bodyText)
        
        // Explicitly check for error object first
        if (jsonElement is JsonObject && jsonElement.containsKey("error")) {
            val message = jsonElement["message"]?.jsonPrimitive?.contentOrNull ?: "Unknown Last.fm error"
            val code = jsonElement["error"]?.jsonPrimitive?.intOrNull ?: 0
            throw Exception("Last.fm Error ($code): $message")
        }

        try {
            json.decodeFromJsonElement<LastFmAuthResponse>(jsonElement)
        } catch (e: Exception) {
            throw Exception("Failed to parse Last.fm success response: ${e.message}")
        }
    }

    suspend fun updateNowPlaying(httpClient: HttpClient, artist: String, track: String, album: String? = null): Result<Unit> = runCatching {
        val sk = sessionKey ?: return Result.failure(Exception("No session key"))
        val params = mutableMapOf(
            "method" to "track.updateNowPlaying",
            "artist" to artist,
            "track" to track,
            "api_key" to apiKey,
            "sk" to sk
        )
        album?.let { params["album"] = it }
        val apiSig = generateSignature(params)

        httpClient.submitForm(
            url = "https://ws.audioscrobbler.com/2.0/",
            formParameters = Parameters.build {
                params.forEach { (k, v) -> append(k, v) }
                append("api_sig", apiSig)
                append("format", "json")
            }
        )
    }

    suspend fun scrobble(httpClient: HttpClient, artist: String, track: String, timestamp: Long, album: String? = null): Result<Unit> = runCatching {
        val sk = sessionKey ?: return Result.failure(Exception("No session key"))
        val params = mutableMapOf(
            "method" to "track.scrobble",
            "artist" to artist,
            "track" to track,
            "timestamp" to (timestamp / 1000).toString(),
            "api_key" to apiKey,
            "sk" to sk
        )
        album?.let { params["album"] = it }
        val apiSig = generateSignature(params)

        httpClient.submitForm(
            url = "https://ws.audioscrobbler.com/2.0/",
            formParameters = Parameters.build {
                params.forEach { (k, v) -> append(k, v) }
                append("api_sig", apiSig)
                append("format", "json")
            }
        )
    }

    suspend fun getArtistInfo(httpClient: HttpClient, artist: String): JsonObject? {
        return try {
            val response = httpClient.get("https://ws.audioscrobbler.com/2.0/") {
                parameter("method", "artist.getInfo")
                parameter("artist", artist)
                parameter("api_key", apiKey)
                parameter("format", "json")
            }
            json.parseToJsonElement(response.bodyAsText()).jsonObject["artist"]?.jsonObject
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getArtistPhotos(httpClient: HttpClient, artist: String): List<String> {
        return try {
            val response = httpClient.get("https://www.last.fm/music/${artist.replace(" ", "+")}/+images")
            val html = response.bodyAsText()

            val photoUrls = mutableListOf<String>()

            // Target specific sections that contain artist photos (not related artists)
            // Look for the main artist photos gallery section
            val galleryPattern = Regex(
                """<li[^>]*class="[^"]*image-list-item[^"]*"[^>]*>.*?<img[^>]*src="([^"]*lastfm\.freetls\.fastly\.net/i/u/[^"]+)"[^>]*>""",
                RegexOption.DOT_MATCHES_ALL
            )

            galleryPattern.findAll(html).forEach { match ->
                val imageUrl = match.groupValues[1]
                    .replace(Regex("i/u/[a-zA-Z0-9]+/"), "i/u/ar0/")
                photoUrls.add(imageUrl)
            }

            // If no gallery images found, try the artist header/avatar area
            if (photoUrls.isEmpty()) {
                val headerPattern = Regex(
                    """<div[^>]*class="[^"]*header-metadata[^"]*"[^>]*>.*?<img[^>]*src="([^"]*lastfm\.freetls\.fastly\.net/i/u/[^"]+)"[^>]*>""",
                    RegexOption.DOT_MATCHES_ALL
                )

                headerPattern.findAll(html).forEach { match ->
                    val imageUrl = match.groupValues[1]
                        .replace(Regex("i/u/[a-zA-Z0-9]+/"), "i/u/ar0/")
                    photoUrls.add(imageUrl)
                }
            }

            // Fallback: if still no images, use a more targeted approach but limit to first 5
            if (photoUrls.isEmpty()) {
                val fallbackPattern = Regex("https://lastfm\\.freetls\\.fastly\\.net/i/u/[a-zA-Z0-9]+/[a-zA-Z0-9]+\\.(jpg|png|webp|jpeg)")
                fallbackPattern.findAll(html).take(5).forEach { match ->
                    val imageUrl = match.value.replace(Regex("i/u/[a-zA-Z0-9]+/"), "i/u/ar0/")
                    photoUrls.add(imageUrl)
                }
            }

            photoUrls.distinct()
        } catch (_: Exception) {
            emptyList()
        }
    }
}
