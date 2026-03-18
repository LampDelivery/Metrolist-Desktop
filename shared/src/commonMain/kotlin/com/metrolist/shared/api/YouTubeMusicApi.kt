package com.metrolist.shared.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

class YouTubeMusicApi(private val httpClient: HttpClient) {
    private val baseUrl = "https://music.youtube.com/youtubei/v1"
    private val clientName = "WEB_REMIX"
    private val clientVersion = "1.20240101.01.00"
    
    var cookieString: String? = null

    private fun buildContext() = buildJsonObject {
        put("context", buildJsonObject {
            put("client", buildJsonObject {
                put("clientName", clientName)
                put("clientVersion", clientVersion)
                // Set language and region to US to avoid localized recommendations
                put("hl", "en")
                put("gl", "US")
            })
        })
    }

    private fun HttpRequestBuilder.applyAuth() {
        cookieString?.let { header(HttpHeaders.Cookie, it) }
    }

    suspend fun getHomeData(): JsonObject {
        val response: HttpResponse = httpClient.post("$baseUrl/browse") {
            applyAuth()
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject {
                val context = buildContext()
                context.forEach { (key, value) -> put(key, value) }
                put("browseId", "FEmusic_home")
            })
        }
        return response.body()
    }

    suspend fun getLibrary(): JsonObject {
        val response: HttpResponse = httpClient.post("$baseUrl/browse") {
            applyAuth()
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject {
                val context = buildContext()
                context.forEach { (key, value) -> put(key, value) }
                put("browseId", "FEmusic_library_landing")
            })
        }
        return response.body()
    }

    suspend fun search(query: String): JsonObject {
        val response: HttpResponse = httpClient.post("$baseUrl/search") {
            applyAuth()
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject {
                val context = buildContext()
                context.forEach { (key, value) -> put(key, value) }
                put("query", query)
            })
        }
        return response.body()
    }

    suspend fun getAccountInfo(): JsonObject? {
        return try {
            val response: HttpResponse = httpClient.post("$baseUrl/account/get_account_menu") {
                applyAuth()
                contentType(ContentType.Application.Json)
                setBody(buildContext())
            }
            response.body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getArtist(channelId: String): JsonObject {
        val response: HttpResponse = httpClient.post("$baseUrl/browse") {
            applyAuth()
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject {
                val context = buildContext()
                context.forEach { (key, value) -> put(key, value) }
                put("browseId", channelId)
            })
        }
        return response.body()
    }

    suspend fun getAlbum(browseId: String): JsonObject {
        val response: HttpResponse = httpClient.post("$baseUrl/browse") {
            applyAuth()
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject {
                val context = buildContext()
                context.forEach { (key, value) -> put(key, value) }
                put("browseId", browseId)
            })
        }
        return response.body()
    }

    suspend fun getNext(videoId: String, playlistId: String? = null): JsonObject {
        val response: HttpResponse = httpClient.post("$baseUrl/next") {
            applyAuth()
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject {
                val context = buildContext()
                context.forEach { (key, value) -> put(key, value) }
                put("videoId", videoId)
                if (playlistId != null) put("playlistId", playlistId)
            })
        }
        return response.body()
    }

    suspend fun getStreamUrl(videoId: String): String? {
        return try {
            val response: HttpResponse = httpClient.post("$baseUrl/player") {
                applyAuth()
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    val context = buildContext()
                    context.forEach { (key, value) -> put(key, value) }
                    put("videoId", videoId)
                })
            }
            val json: JsonObject = response.body()
            json["streamingData"]?.jsonObject?.get("adaptiveFormats")?.jsonArray
                ?.map { it.jsonObject }
                ?.filter { it["mimeType"]?.jsonPrimitive?.content?.contains("audio") == true }
                ?.maxByOrNull { it["bitrate"]?.jsonPrimitive?.int ?: 0 }
                ?.get("url")?.jsonPrimitive?.content
        } catch (e: Exception) {
            null
        }
    }
}
