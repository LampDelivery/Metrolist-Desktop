package com.metrolist.shared.api.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class YouTubeClient(
    val clientName: String,
    val clientVersion: String,
    val clientId: String,
    val userAgent: String,
    val loginSupported: Boolean = false,
    val useSignatureTimestamp: Boolean = false,
    val useWebPoTokens: Boolean = false,
) {
    fun toContext(locale: YouTubeLocale, visitorData: String?, dataSyncId: String?) = Context(
        client = Context.Client(
            clientName = clientName,
            clientVersion = clientVersion,
            gl = locale.gl,
            hl = locale.hl,
            visitorData = visitorData
        ),
        user = Context.User(
            onBehalfOfUser = if (loginSupported) dataSyncId else null
        ),
    )

    companion object {
        const val USER_AGENT_WEB = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:140.0) Gecko/20100101 Firefox/140.0"
        const val USER_AGENT_ANDROID = "com.google.android.youtube/19.05.36 (Linux; U; Android 14; en_US) gzip"
        const val USER_AGENT_TV = "Mozilla/5.0 (PlayStation; PlayStation 4/12.02) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.4 Safari/605.1.15"
        const val USER_AGENT_IOS = "com.google.ios.youtube/19.05.36 (iPhone16,2; U; CPU iOS 17_2_1 like Mac OS X; en_US)"

        const val ORIGIN_YOUTUBE_MUSIC = "https://music.youtube.com"
        const val REFERER_YOUTUBE_MUSIC = "https://music.youtube.com/"
        const val API_URL_YOUTUBE_MUSIC = "https://music.youtube.com/youtubei/v1/"

        val WEB_REMIX = YouTubeClient(
            clientName = "WEB_REMIX",
            clientVersion = "1.20260213.01.00",
            clientId = "67",
            userAgent = USER_AGENT_WEB,
            loginSupported = true,
            useSignatureTimestamp = true,
            useWebPoTokens = true,
        )

        val ANDROID = YouTubeClient(
            clientName = "ANDROID",
            clientVersion = "19.05.36",
            clientId = "3",
            userAgent = USER_AGENT_ANDROID,
            loginSupported = true,
            useSignatureTimestamp = true
        )

        val ANDROID_VR = YouTubeClient(
            clientName = "ANDROID_VR",
            clientVersion = "1.61.48",
            clientId = "28",
            userAgent = "com.google.android.apps.youtube.vr.oculus/1.61.48 (Linux; U; Android 12; en_US; Oculus Quest 3; Build/SQ3A.220605.009.A1; Cronet/132.0.6808.3)",
            loginSupported = false,
            useSignatureTimestamp = false
        )

        val IOS = YouTubeClient(
            clientName = "IOS",
            clientVersion = "19.05.36",
            clientId = "5",
            userAgent = USER_AGENT_IOS,
            loginSupported = true,
            useSignatureTimestamp = true
        )

        val TVHTML5_EMBEDDED = YouTubeClient(
            clientName = "TVHTML5_SIMPLY_EMBEDDED_PLAYER",
            clientVersion = "2.0",
            clientId = "85",
            userAgent = USER_AGENT_TV,
            loginSupported = true
        )
    }
}
