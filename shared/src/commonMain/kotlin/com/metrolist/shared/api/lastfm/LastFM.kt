package com.metrolist.shared.api.lastfm

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
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
}
