package com.metrolist.shared.api

import com.metrolist.shared.model.*
import kotlinx.serialization.json.*

object YouTubeMusicParser {
    fun parseHomeSections(json: JsonObject): Map<String, List<YTItem>> {
        val sections = mutableMapOf<String, List<YTItem>>()
        try {
            val contents = json["contents"]?.jsonObject
                ?.get("singleColumnBrowseResultsRenderer")?.jsonObject
                ?.get("tabs")?.jsonArray?.get(0)?.jsonObject
                ?.get("tabRenderer")?.jsonObject
                ?.get("content")?.jsonObject
                ?.get("sectionListRenderer")?.jsonObject
                ?.get("contents")?.jsonArray

            contents?.forEach { section ->
                val shelf = section.jsonObject["musicShelfRenderer"]?.jsonObject
                    ?: section.jsonObject["musicCarouselShelfRenderer"]?.jsonObject
                
                if (shelf != null) {
                    val title = shelf["title"]?.jsonObject?.get("runs")?.jsonArray?.get(0)?.jsonObject?.get("text")?.jsonPrimitive?.content ?: "Unknown"
                    val items = shelf["contents"]?.jsonArray?.mapNotNull { item ->
                        parseItem(item.jsonObject["musicResponsiveListItemRenderer"]?.jsonObject 
                            ?: item.jsonObject["musicTwoColumnItemRenderer"]?.jsonObject
                            ?: item.jsonObject)
                    } ?: emptyList()
                    if (items.isNotEmpty()) {
                        sections[title] = items
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return sections
    }

    fun parseSearchResults(json: JsonObject): List<YTItem> {
        val results = mutableListOf<YTItem>()
        try {
            val contents = json["contents"]?.jsonObject
                ?.get("sectionListRenderer")?.jsonObject
                ?.get("contents")?.jsonArray
                ?: json["contents"]?.jsonObject
                    ?.get("tabbedSearchResultsRenderer")?.jsonObject
                    ?.get("tabs")?.jsonArray?.get(0)?.jsonObject
                    ?.get("tabRenderer")?.jsonObject
                    ?.get("content")?.jsonObject
                    ?.get("sectionListRenderer")?.jsonObject
                    ?.get("contents")?.jsonArray

            contents?.forEach { section ->
                val shelf = section.jsonObject["musicShelfRenderer"]?.jsonObject
                shelf?.get("contents")?.jsonArray?.forEach { item ->
                    val ytItem = parseItem(item.jsonObject["musicResponsiveListItemRenderer"]?.jsonObject)
                    if (ytItem != null) results.add(ytItem)
                }
            }
        } catch (e: Exception) {}
        return results
    }

    private fun parseItem(item: JsonObject?): YTItem? {
        if (item == null) return null
        return try {
            val flexColumns = item["flexColumns"]?.jsonArray
            val titleColumn = flexColumns?.getOrNull(0)?.jsonObject?.get("musicResponsiveListItemFlexColumnRenderer")?.jsonObject
            val title = titleColumn?.get("text")?.jsonObject?.get("runs")?.jsonArray?.get(0)?.jsonObject?.get("text")?.jsonPrimitive?.content ?: return null
            
            val navigationEndpoint = item["navigationEndpoint"]?.jsonObject
                ?: item["onTap"]?.jsonObject
            
            val browseId = navigationEndpoint?.get("browseEndpoint")?.jsonObject?.get("browseId")?.jsonPrimitive?.content
            val videoId = item["playlistItemData"]?.jsonObject?.get("videoId")?.jsonPrimitive?.content
                ?: navigationEndpoint?.get("watchEndpoint")?.jsonObject?.get("videoId")?.jsonPrimitive?.content

            var thumbnail = item["thumbnail"]?.jsonObject?.get("musicThumbnailRenderer")?.jsonObject
                ?.get("thumbnail")?.jsonObject?.get("thumbnails")?.jsonArray?.lastOrNull()?.jsonObject?.get("url")?.jsonPrimitive?.content
            
            // Enhance thumbnail quality by removing size constraints (e.g., =w120-h120-l90-rj)
            thumbnail = thumbnail?.replace(Regex("=w\\d+-h\\d+.*$"), "=w1000-h1000")

            when {
                videoId != null -> {
                    val artistColumn = flexColumns?.getOrNull(1)?.jsonObject?.get("musicResponsiveListItemFlexColumnRenderer")?.jsonObject
                    val artistRuns = artistColumn?.get("text")?.jsonObject?.get("runs")?.jsonArray
                    val artists = mutableListOf<ArtistTiny>()
                    artistRuns?.forEach { run ->
                        val text = run.jsonObject["text"]?.jsonPrimitive?.content
                        val aBrowseId = run.jsonObject["navigationEndpoint"]?.jsonObject
                            ?.get("browseEndpoint")?.jsonObject?.get("browseId")?.jsonPrimitive?.content
                        if (text != null && text != " & " && text != ", " && text != " • ") {
                            artists.add(ArtistTiny(aBrowseId, text))
                        }
                    }
                    SongItem(videoId, title, thumbnail, artists, null)
                }
                browseId != null -> {
                    if (browseId.startsWith("FEmusic_library_") || browseId.startsWith("VL")) {
                        PlaylistItem(browseId, title, thumbnail)
                    } else if (browseId.startsWith("UC")) {
                        ArtistItem(browseId, title, thumbnail)
                    } else {
                        AlbumItem(browseId, title, thumbnail, emptyList())
                    }
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
}
