package com.metrolist.shared.api.lyrics

import com.metrolist.shared.api.innertube.InnerTube
import com.metrolist.shared.api.innertube.models.YouTubeClient
import io.ktor.client.call.body
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class YouTubeLyricsProvider(private val innerTube: InnerTube) {

    suspend fun getLyrics(videoId: String): String? {
        return try {
            // Step 1: get next response to find the lyrics browse endpoint
            val nextResponse = innerTube.next(YouTubeClient.WEB_REMIX, videoId = videoId).body<JsonObject>()
            val lyricsEndpointId = extractLyricsEndpointId(nextResponse) ?: return null

            // Step 2: browse lyrics endpoint
            val browseResponse = innerTube.browse(YouTubeClient.WEB_REMIX, browseId = lyricsEndpointId).body<JsonObject>()
            extractLyricsText(browseResponse)
        } catch (_: Exception) {
            null
        }
    }

    private fun extractLyricsEndpointId(nextResponse: JsonObject): String? {
        // Try: contents.singleColumnMusicWatchNextResultsRenderer.tabbedRenderer.watchNextTabbedResultsRenderer.tabs
        val tabs = nextResponse["contents"]?.jsonObject
            ?.get("singleColumnMusicWatchNextResultsRenderer")?.jsonObject
            ?.get("tabbedRenderer")?.jsonObject
            ?.get("watchNextTabbedResultsRenderer")?.jsonObject
            ?.get("tabs")?.jsonArray
            ?: return null

        // Lyrics tab is usually the second tab (index 1)
        tabs.forEach { tab ->
            val tabRenderer = tab.jsonObject["tabRenderer"]?.jsonObject ?: return@forEach
            val title = tabRenderer["title"]?.jsonPrimitive?.contentOrNull
            if (title == "Lyrics") {
                return tabRenderer["endpoint"]?.jsonObject
                    ?.get("browseEndpoint")?.jsonObject
                    ?.get("browseId")?.jsonPrimitive?.contentOrNull
            }
        }
        return null
    }

    private fun extractLyricsText(browseResponse: JsonObject): String? {
        val description = browseResponse["contents"]?.jsonObject
            ?.get("sectionListRenderer")?.jsonObject
            ?.get("contents")?.jsonArray
            ?.getOrNull(0)?.jsonObject
            ?.get("musicDescriptionShelfRenderer")?.jsonObject
            ?.get("description")?.jsonObject
            ?.get("runs")?.jsonArray
            ?.joinToString("") { it.jsonObject["text"]?.jsonPrimitive?.contentOrNull ?: "" }
        return if (description.isNullOrBlank()) null else description
    }
}
