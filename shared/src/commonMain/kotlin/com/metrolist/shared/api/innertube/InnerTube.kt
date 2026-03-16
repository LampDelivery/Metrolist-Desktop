package com.metrolist.shared.api.innertube

import com.metrolist.shared.api.innertube.models.Context
import com.metrolist.shared.api.innertube.models.YouTubeClient
import com.metrolist.shared.api.innertube.models.YouTubeLocale
import com.metrolist.shared.api.innertube.models.body.*
import com.metrolist.shared.api.innertube.utils.parseCookieString
import com.metrolist.shared.api.innertube.utils.sha1
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

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

    var useLoginForBrowse: Boolean = false

    private fun HttpRequestBuilder.ytClient(client: YouTubeClient, setLogin: Boolean = false) {
        contentType(ContentType.Application.Json)
        headers {
            append("X-Goog-Api-Format-Version", "1")
            append("X-YouTube-Client-Name", client.clientId)
            append("X-YouTube-Client-Version", client.clientVersion)
            append("X-Origin", YouTubeClient.ORIGIN_YOUTUBE_MUSIC)
            append("Referer", YouTubeClient.REFERER_YOUTUBE_MUSIC)
            visitorData?.let { append("X-Goog-Visitor-Id", it) }
            if (setLogin && client.loginSupported) {
                cookie?.let { cookieValue ->
                    append("cookie", cookieValue)
                    if ("SAPISID" in cookieMap) {
                        val currentTime = System.currentTimeMillis() / 1000
                        val sapisidHash = sha1("$currentTime ${cookieMap["SAPISID"]} ${YouTubeClient.ORIGIN_YOUTUBE_MUSIC}")
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
        parameter("prettyPrint", false)
    }

    private suspend fun <T> withRetry(
        maxAttempts: Int = 3,
        initialDelay: Long = 500L,
        factor: Double = 2.0,
        block: suspend () -> T,
    ): T {
        var currentDelay = initialDelay
        var attempt = 0
        while (true) {
            try {
                return block()
            } catch (e: Exception) {
                attempt++
                if (attempt >= maxAttempts) throw e
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong()
            }
        }
    }

    suspend fun browse(
        client: YouTubeClient,
        browseId: String? = null,
        params: String? = null,
        continuation: String? = null,
        setLogin: Boolean = false,
    ) = withRetry {
        httpClient.post("browse") {
            ytClient(client, setLogin = setLogin || useLoginForBrowse)
            setBody(
                BrowseBody(
                    context = client.toContext(
                        locale,
                        visitorData,
                        if (setLogin || useLoginForBrowse) dataSyncId else null
                    ),
                    browseId = browseId,
                    params = params,
                    continuation = continuation
                )
            )
        }
    }

    suspend fun player(
        client: YouTubeClient,
        videoId: String,
        playlistId: String? = null,
        signatureTimestamp: Int? = null,
        poToken: String? = null,
    ) = withRetry {
        httpClient.post("player") {
            ytClient(client, setLogin = true)
            setBody(
                PlayerBody(
                    context = client.toContext(locale, visitorData, dataSyncId),
                    videoId = videoId,
                    playlistId = playlistId,
                    playbackContext = if (client.useSignatureTimestamp && signatureTimestamp != null) {
                        PlayerBody.PlaybackContext(
                            PlayerBody.PlaybackContext.ContentPlaybackContext(signatureTimestamp)
                        )
                    } else null,
                    serviceIntegrityDimensions = if (client.useWebPoTokens && poToken != null) {
                        PlayerBody.ServiceIntegrityDimensions(poToken)
                    } else null,
                )
            )
        }
    }

    suspend fun next(
        client: YouTubeClient,
        videoId: String? = null,
        playlistId: String? = null,
        playlistSetVideoId: String? = null,
        index: Int? = null,
        params: String? = null,
        continuation: String? = null,
    ) = withRetry {
        httpClient.post("next") {
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
    }

    suspend fun search(
        client: YouTubeClient,
        query: String? = null,
        params: String? = null,
        continuation: String? = null,
    ) = withRetry {
        httpClient.post("search") {
            ytClient(client, setLogin = useLoginForBrowse)
            setBody(
                SearchBody(
                    context = client.toContext(
                        locale,
                        visitorData,
                        if (useLoginForBrowse) dataSyncId else null
                    ),
                    query = query,
                    params = params
                )
            )
            if (continuation != null) {
                parameter("continuation", continuation)
                parameter("ctoken", continuation)
            }
        }
    }

    suspend fun getSearchSuggestions(
        client: YouTubeClient,
        input: String,
    ) = withRetry {
        httpClient.post("music/get_search_suggestions") {
            ytClient(client)
            setBody(
                GetSearchSuggestionsBody(
                    context = client.toContext(locale, visitorData, null),
                    input = input
                )
            )
        }
    }

    suspend fun feedback(
        client: YouTubeClient,
        tokens: List<String>,
    ) = withRetry {
        httpClient.post("feedback") {
            ytClient(client, setLogin = true)
            setBody(
                FeedbackBody(
                    context = client.toContext(locale, visitorData, dataSyncId),
                    feedbackTokens = tokens
                )
            )
        }
    }

    suspend fun getQueue(
        client: YouTubeClient,
        videoIds: List<String>?,
        playlistId: String?,
    ) = withRetry {
        httpClient.post("music/get_queue") {
            ytClient(client)
            setBody(
                GetQueueBody(
                    context = client.toContext(locale, visitorData, null),
                    videoIds = videoIds,
                    playlistId = playlistId
                )
            )
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun getTranscript(
        client: YouTubeClient,
        videoId: String,
    ) = withRetry {
        httpClient.post("https://music.youtube.com/youtubei/v1/get_transcript") {
            parameter("key", "AIzaSyC9XL3ZjWddXya6X74dJoCTL-WEYFDNX3")
            headers {
                append("Content-Type", "application/json")
            }
            setBody(
                GetTranscriptBody(
                    context = client.toContext(locale, null, null),
                    params = Base64.Default.encode(
                        "\n${11.toChar()}$videoId".encodeToByteArray()
                    )
                )
            )
        }
    }

    suspend fun accountMenu(client: YouTubeClient) = withRetry {
        httpClient.post("account/account_menu") {
            ytClient(client, setLogin = true)
            setBody(AccountMenuBody(client.toContext(locale, visitorData, dataSyncId)))
        }
    }

    suspend fun likeVideo(client: YouTubeClient, videoId: String) = withRetry {
        httpClient.post("like/like") {
            ytClient(client, setLogin = true)
            setBody(LikeBody(client.toContext(locale, visitorData, dataSyncId), LikeBody.Target.video(videoId)))
        }
    }

    suspend fun unlikeVideo(client: YouTubeClient, videoId: String) = withRetry {
        httpClient.post("like/removelike") {
            ytClient(client, setLogin = true)
            setBody(LikeBody(client.toContext(locale, visitorData, dataSyncId), LikeBody.Target.video(videoId)))
        }
    }

    suspend fun subscribeChannel(client: YouTubeClient, channelId: String, params: String? = null) = withRetry {
        httpClient.post("subscription/subscribe") {
            ytClient(client, setLogin = true)
            setBody(SubscribeBody(client.toContext(locale, visitorData, dataSyncId), listOf(channelId), params))
        }
    }

    suspend fun unsubscribeChannel(client: YouTubeClient, channelId: String, params: String? = null) = withRetry {
        httpClient.post("subscription/unsubscribe") {
            ytClient(client, setLogin = true)
            setBody(SubscribeBody(client.toContext(locale, visitorData, dataSyncId), listOf(channelId), params))
        }
    }

    suspend fun likePlaylist(client: YouTubeClient, playlistId: String) = withRetry {
        httpClient.post("like/like") {
            ytClient(client, setLogin = true)
            setBody(LikeBody(client.toContext(locale, visitorData, dataSyncId), LikeBody.Target.playlist(playlistId)))
        }
    }

    suspend fun unlikePlaylist(client: YouTubeClient, playlistId: String) = withRetry {
        httpClient.post("like/removelike") {
            ytClient(client, setLogin = true)
            setBody(LikeBody(client.toContext(locale, visitorData, dataSyncId), LikeBody.Target.playlist(playlistId)))
        }
    }

    suspend fun addToPlaylist(client: YouTubeClient, playlistId: String, videoId: String) = withRetry {
        httpClient.post("browse/edit_playlist") {
            ytClient(client, setLogin = true)
            setBody(EditPlaylistBody(client.toContext(locale, visitorData, dataSyncId), playlistId.removePrefix("VL"), listOf(Action.AddVideoAction(addedVideoId = videoId))))
        }
    }

    suspend fun createPlaylist(client: YouTubeClient, title: String) = withRetry {
        httpClient.post("playlist/create") {
            ytClient(client, setLogin = true)
            setBody(CreatePlaylistBody(client.toContext(locale, visitorData, dataSyncId), title))
        }
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
