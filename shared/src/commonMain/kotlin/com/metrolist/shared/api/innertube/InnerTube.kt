package com.metrolist.shared.api.innertube

import com.metrolist.shared.api.innertube.models.Context
import com.metrolist.shared.api.innertube.models.YouTubeClient
import com.metrolist.shared.api.innertube.models.YouTubeLocale
import com.metrolist.shared.api.innertube.models.body.*
import com.metrolist.shared.api.innertube.utils.parseCookieString
import com.metrolist.shared.api.innertube.utils.sha1
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class InnerTube(val httpClient: HttpClient) {
    var locale = YouTubeLocale(gl = "US", hl = "en")
    var visitorData: String? = null
    var dataSyncId: String? = null
    var cookie: String? = null
        set(value) {
            field = value
            cookieMap = if (value == null) emptyMap() else parseCookieString(value)
        }
    private var cookieMap = emptyMap<String, String>()

    private fun HttpRequestBuilder.ytClient(client: YouTubeClient, setLogin: Boolean = false) {
        contentType(ContentType.Application.Json)
        headers {
            append("X-Goog-Api-Format-Version", "1")
            append("X-YouTube-Client-Name", client.clientId)
            append("X-YouTube-Client-Version", client.clientVersion)
            append("X-Origin", "https://music.youtube.com")
            append("Referer", "https://music.youtube.com/")
            append("User-Agent", client.userAgent)
            visitorData?.let { append("X-Goog-Visitor-Id", it) }
            if (setLogin && client.loginSupported) {
                cookie?.let { cookieValue ->
                    append("cookie", cookieValue)
                    if ("SAPISID" in cookieMap) {
                        val currentTime = System.currentTimeMillis() / 1000
                        val sapisidHash = sha1("$currentTime ${cookieMap["SAPISID"]} https://music.youtube.com")
                        append("Authorization", "SAPISIDHASH ${currentTime}_${sapisidHash}")
                    }
                }
            }
        }
        
        val apiBase = if (client.clientName == "ANDROID" || client.clientName == "IOS") {
            "https://youtubei.googleapis.com/youtubei/v1"
        } else {
            "https://music.youtube.com/youtubei/v1"
        }
        
        val endpoint = url.encodedPath.removePrefix("/")
        url("$apiBase/$endpoint")
    }

    suspend fun browse(
        client: YouTubeClient,
        browseId: String? = null,
        params: String? = null,
        continuation: String? = null,
        setLogin: Boolean = false,
    ) = httpClient.post("browse") {
        ytClient(client, setLogin = setLogin)
        setBody(
            BrowseBody(
                context = client.toContext(locale, visitorData, dataSyncId),
                browseId = browseId,
                params = params,
                continuation = continuation
            )
        )
    }

    suspend fun player(
        client: YouTubeClient,
        videoId: String,
        playlistId: String? = null,
    ) = httpClient.post("player") {
        ytClient(client, setLogin = true)
        val sts = 20142 
        setBody(
            PlayerBody(
                context = client.toContext(locale, visitorData, dataSyncId),
                videoId = videoId,
                playlistId = playlistId,
                playbackContext = PlayerBody.PlaybackContext(
                    PlayerBody.PlaybackContext.ContentPlaybackContext(sts)
                )
            )
        )
    }

    suspend fun next(
        client: YouTubeClient,
        videoId: String? = null,
        playlistId: String? = null,
        playlistSetVideoId: String? = null,
        index: Int? = null,
        params: String? = null,
        continuation: String? = null,
    ) = httpClient.post("next") {
        ytClient(client, setLogin = true)
        setBody(
            NextBody(
                context = client.toContext(locale, visitorData, dataSyncId),
                videoId = videoId,
                playlistId = playlistId,
                playlistSetVideoId = playlistSetVideoId,
                index = index,
                params = params,
                continuation = continuation
            )
        )
    }

    suspend fun search(
        client: YouTubeClient,
        query: String? = null,
        params: String? = null,
        continuation: String? = null,
    ) = httpClient.post("search") {
        ytClient(client, setLogin = true)
        setBody(
            SearchBody(
                context = client.toContext(locale, visitorData, dataSyncId),
                query = query,
                params = params
            )
        )
        if (continuation != null) {
            parameter("continuation", continuation)
            parameter("ctoken", continuation)
        }
    }

    suspend fun accountMenu(client: YouTubeClient) = httpClient.post("account/account_menu") {
        ytClient(client, setLogin = true)
        setBody(AccountMenuBody(client.toContext(locale, visitorData, dataSyncId)))
    }

    suspend fun fetchFreshVisitorData(): String? {
        return try {
            val response = httpClient.post("https://music.youtube.com/youtubei/v1/browse") {
                contentType(ContentType.Application.Json)
                header("User-Agent", YouTubeClient.USER_AGENT_WEB)
                setBody(
                    BrowseBody(
                        context = YouTubeClient.WEB_REMIX.toContext(locale, null, null),
                        browseId = "FEmusic_home"
                    )
                )
            }
            val responseText = response.bodyAsText()
            val match = Regex(""""visitorData"\s*:\s*"([^"]+)"""").find(responseText)
            val data = match?.groupValues?.get(1)
            visitorData = data
            data
        } catch (e: Exception) {
            null
        }
    }
}
