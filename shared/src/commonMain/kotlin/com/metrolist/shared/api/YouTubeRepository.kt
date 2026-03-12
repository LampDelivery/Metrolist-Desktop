package com.metrolist.shared.api

import com.metrolist.shared.api.innertube.InnerTube
import com.metrolist.shared.api.innertube.models.*
import com.metrolist.shared.api.innertube.models.response.*
import com.metrolist.shared.api.lyrics.*
import com.metrolist.shared.model.*
import io.ktor.client.call.*
import kotlinx.serialization.json.*

class YouTubeRepository(private val innerTube: InnerTube) {
    private val lyricsProvider = LrcLibLyricsProvider(innerTube.httpClient)
    var lastAccountInfo: AccountInfo? = null

    fun setCookie(cookie: String?) {
        innerTube.cookie = cookie
    }

    suspend fun getAccountInfo(): AccountInfo? {
        return try {
            val response = innerTube.accountMenu(YouTubeClient.WEB_REMIX).body<AccountMenuResponse>()
            val info = response.actions.firstOrNull()?.openPopupAction?.popup?.multiPageMenuRenderer?.header?.activeAccountHeaderRenderer?.toAccountInfo()
            if (info != null) lastAccountInfo = info
            info
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getHomeData(): Map<String, List<YTItem>> {
        return try {
            val response = innerTube.browse(YouTubeClient.WEB_REMIX, browseId = "FEmusic_home", setLogin = true).body<BrowseResponse>()
            parseBrowseResponse(response).sections
        } catch (_: Exception) {
            emptyMap()
        }
    }

    suspend fun getLibrary(): com.metrolist.shared.api.innertube.models.YouTubeDataPage<YTItem> {
        return try {
            val response = innerTube.browse(YouTubeClient.WEB_REMIX, browseId = "FEmusic_library_landing", setLogin = true).body<BrowseResponse>()
            val parsed = parseBrowseResponse(response)
            com.metrolist.shared.api.innertube.models.YouTubeDataPage(parsed.sections.values.flatten(), parsed.continuation)
        } catch (_: Exception) {
            com.metrolist.shared.api.innertube.models.YouTubeDataPage(emptyList())
        }
    }

    suspend fun getArtist(channelId: String): Map<String, List<YTItem>> {
        return try {
            val response = innerTube.browse(YouTubeClient.WEB_REMIX, browseId = channelId, setLogin = true).body<BrowseResponse>()
            val parsed = parseBrowseResponse(response)
            
            val header = response.header
            val h = header?.musicImmersiveHeaderRenderer
                ?: header?.musicVisualHeaderRenderer
                ?: header?.musicHeaderRenderer
                ?: header?.musicDetailHeaderRenderer
            
            val banner = h?.get("thumbnail")?.jsonObject?.get("musicThumbnailRenderer")?.jsonObject?.get("thumbnail")?.jsonObject?.get("thumbnails")?.jsonArray?.lastOrNull()?.jsonObject?.get("url")?.jsonPrimitive?.contentOrNull
                ?: h?.get("foregroundThumbnail")?.jsonObject?.get("musicThumbnailRenderer")?.jsonObject?.get("thumbnail")?.jsonObject?.get("thumbnails")?.jsonArray?.lastOrNull()?.jsonObject?.get("url")?.jsonPrimitive?.contentOrNull
                ?: h?.get("backgroundImage")?.jsonObject?.get("musicThumbnailRenderer")?.jsonObject?.get("thumbnail")?.jsonObject?.get("thumbnails")?.jsonArray?.lastOrNull()?.jsonObject?.get("url")?.jsonPrimitive?.contentOrNull
            
            val subscribers = h?.get("subscriptionButton")?.jsonObject?.get("subscribeButtonRenderer")?.jsonObject?.get("subscriberCountText")?.jsonObject?.get("runs")?.jsonArray?.getOrNull(0)?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull
                ?: h?.get("subscriptionButton")?.jsonObject?.get("subscribeButtonRenderer")?.jsonObject?.get("longSubscriberCountText")?.jsonObject?.get("runs")?.jsonArray?.getOrNull(0)?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull
            
            val title = h?.get("title")?.jsonObject?.get("runs")?.jsonArray?.getOrNull(0)?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull 
                ?: h?.get("title")?.jsonObject?.get("simpleText")?.jsonPrimitive?.contentOrNull
                ?: h?.get("title")?.jsonPrimitive?.contentOrNull
                ?: "Artist"

            val artistInfo = ArtistItem(
                id = channelId,
                title = title,
                thumbnail = banner,
                subscribers = subscribers,
                banner = banner
            )
            
            val sections = parsed.sections.toMutableMap()
            sections["header"] = listOf(artistInfo)
            sections
        } catch (e: Exception) {
            emptyMap()
        }
    }

    suspend fun getPlaylist(playlistId: String): Map<String, List<YTItem>> {
        val id = if (playlistId.startsWith("VL") || playlistId.startsWith("PL") || playlistId.startsWith("RD")) playlistId else "VL$playlistId"
        return try {
            val response = innerTube.browse(YouTubeClient.WEB_REMIX, browseId = id, setLogin = true).body<BrowseResponse>()
            val parsed = parseBrowseResponse(response)
            
            val header = response.header
            var title: String? = null
            var thumbnail: String? = null
            var author: String? = null
            
            val detailHeader = header?.musicDetailHeaderRenderer
            if (detailHeader != null) {
                title = detailHeader["title"]?.jsonObject?.get("runs")?.jsonArray?.getOrNull(0)?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull 
                    ?: detailHeader["title"]?.jsonObject?.get("simpleText")?.jsonPrimitive?.contentOrNull
                thumbnail = detailHeader["thumbnail"]?.jsonObject?.get("croppedSquareThumbnailRenderer")?.jsonObject?.get("thumbnail")?.jsonObject?.get("thumbnails")?.jsonArray?.lastOrNull()?.jsonObject?.get("url")?.jsonPrimitive?.contentOrNull
                author = detailHeader["subtitle"]?.jsonObject?.get("runs")?.jsonArray?.getOrNull(0)?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull
            } else {
                val editableHeader = header?.musicEditablePlaylistDetailHeaderRenderer
                if (editableHeader != null) {
                    val h = editableHeader["header"]?.jsonObject?.get("musicResponsiveHeaderRenderer")?.jsonObject
                    title = h?.get("title")?.jsonObject?.get("runs")?.jsonArray?.getOrNull(0)?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull
                    thumbnail = h?.get("thumbnail")?.jsonObject?.get("musicThumbnailRenderer")?.jsonObject?.get("thumbnail")?.jsonObject?.get("thumbnails")?.jsonArray?.lastOrNull()?.jsonObject?.get("url")?.jsonPrimitive?.contentOrNull
                    author = h?.get("straplineTextOne")?.jsonObject?.get("runs")?.jsonArray?.getOrNull(0)?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull
                }
            }
            
            if (title == null) {
                val h = response.contents?.twoColumnBrowseResultsRenderer?.let { it["tabs"]?.jsonArray?.getOrNull(0)?.jsonObject }
                    ?.get("tabRenderer")?.jsonObject?.get("content")?.jsonObject?.get("sectionListRenderer")?.jsonObject
                    ?.get("contents")?.jsonArray?.getOrNull(0)?.jsonObject?.get("musicResponsiveHeaderRenderer")?.jsonObject
                
                title = h?.get("title")?.jsonObject?.get("runs")?.jsonArray?.getOrNull(0)?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull
                thumbnail = h?.get("thumbnail")?.jsonObject?.get("musicThumbnailRenderer")?.jsonObject?.get("thumbnail")?.jsonObject?.get("thumbnails")?.jsonArray?.lastOrNull()?.jsonObject?.get("url")?.jsonPrimitive?.contentOrNull
                author = h?.get("straplineTextOne")?.jsonObject?.get("runs")?.jsonArray?.getOrNull(0)?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull
            }

            val playlistInfo = PlaylistItem(
                id = playlistId,
                title = title ?: "Playlist",
                thumbnail = thumbnail,
                author = author
            )
            
            val sections = parsed.sections.toMutableMap()
            sections["header"] = listOf(playlistInfo)
            sections
        } catch (_: Exception) {
            emptyMap()
        }
    }

    suspend fun getAlbum(albumId: String): AlbumPage? {
        return try {
            val response = innerTube.browse(YouTubeClient.WEB_REMIX, browseId = albumId, setLogin = true).body<BrowseResponse>()
            
            val playlistId = response.microformat?.get("microformatDataRenderer")?.jsonObject?.get("urlCanonical")?.jsonPrimitive?.contentOrNull?.substringAfterLast('=')
                ?: response.header?.musicDetailHeaderRenderer?.get("menu")?.jsonObject?.get("menuRenderer")?.jsonObject?.get("topLevelButtons")?.jsonArray?.firstOrNull()?.jsonObject?.get("buttonRenderer")?.jsonObject?.get("navigationEndpoint")?.jsonObject?.get("watchPlaylistEndpoint")?.jsonObject?.get("playlistId")?.jsonPrimitive?.contentOrNull
            
            if (playlistId == null) return null

            val header = response.contents?.twoColumnBrowseResultsRenderer?.let { it["tabs"]?.jsonArray?.getOrNull(0)?.jsonObject }
                ?.get("tabRenderer")?.jsonObject?.get("content")?.jsonObject?.get("sectionListRenderer")?.jsonObject
                ?.get("contents")?.jsonArray?.getOrNull(0)?.jsonObject?.get("musicResponsiveHeaderRenderer")?.jsonObject
                ?: response.header?.musicDetailHeaderRenderer

            val title = header?.get("title")?.jsonObject?.get("runs")?.jsonArray?.getOrNull(0)?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull
                ?: header?.get("title")?.jsonObject?.get("simpleText")?.jsonPrimitive?.contentOrNull
                ?: ""
            
            val artists = mutableListOf<ArtistTiny>()
            header?.get("straplineTextOne")?.jsonObject?.get("runs")?.jsonArray?.forEachIndexed { index, run ->
                if (index % 2 == 0) {
                    val runObj = run.jsonObject
                    val name = runObj["text"]?.jsonPrimitive?.contentOrNull
                    val id = runObj["navigationEndpoint"]?.jsonObject?.get("browseEndpoint")?.jsonObject?.get("browseId")?.jsonPrimitive?.contentOrNull
                    if (name != null) artists.add(ArtistTiny(id, name))
                }
            } ?: header?.get("subtitle")?.jsonObject?.get("runs")?.jsonArray?.let { runs ->
                // Fallback for detail header
                if (runs.size >= 3) {
                    val name = runs[2].jsonObject["text"]?.jsonPrimitive?.contentOrNull
                    val id = runs[2].jsonObject["navigationEndpoint"]?.jsonObject?.get("browseEndpoint")?.jsonObject?.get("browseId")?.jsonPrimitive?.contentOrNull
                    if (name != null) artists.add(ArtistTiny(id, name))
                }
            }

            val year = header?.get("subtitle")?.jsonObject?.get("runs")?.jsonArray?.lastOrNull()?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull
            
            val thumbnail = (header?.get("thumbnail")?.jsonObject?.get("musicThumbnailRenderer")?.jsonObject ?: header?.get("thumbnail")?.jsonObject?.get("croppedSquareThumbnailRenderer")?.jsonObject)
                ?.get("thumbnail")?.jsonObject?.get("thumbnails")?.jsonArray?.lastOrNull()?.jsonObject?.get("url")?.jsonPrimitive?.contentOrNull
                ?.replace(Regex("=w\\d+-h\\d+.*$"), "=w1000-h1000")

            val albumItem = AlbumItem(
                id = albumId,
                title = title,
                thumbnail = thumbnail,
                artists = artists,
                year = year,
                playlistId = playlistId
            )

            val songs = getAlbumSongs(playlistId, albumItem)
            
            val otherVersions = response.contents?.twoColumnBrowseResultsRenderer?.get("secondaryContents")?.jsonObject
                ?.get("sectionListRenderer")?.jsonObject?.get("contents")?.jsonArray?.getOrNull(1)?.jsonObject
                ?.get("musicCarouselShelfRenderer")?.jsonObject?.get("contents")?.jsonArray?.mapNotNull { 
                    parseItem(it.jsonObject["musicTwoRowItemRenderer"]?.jsonObject) as? AlbumItem
                } ?: emptyList()

            AlbumPage(albumItem, songs, otherVersions)
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun getAlbumSongs(playlistId: String, album: AlbumItem): List<SongItem> {
        val songs = mutableListOf<SongItem>()
        try {
            var response = innerTube.browse(YouTubeClient.WEB_REMIX, browseId = "VL$playlistId", setLogin = true).body<BrowseResponse>()
            
            fun processPlaylistShelf(shelf: JsonObject) {
                shelf["contents"]?.jsonArray?.forEach { 
                    val item = parseItem(it.jsonObject["musicResponsiveListItemRenderer"]?.jsonObject)
                    if (item is SongItem) {
                        songs.add(item.copy(album = AlbumTiny(album.id, album.title), thumbnail = item.thumbnail ?: album.thumbnail))
                    }
                }
            }

            val contents = response.contents?.twoColumnBrowseResultsRenderer?.get("secondaryContents")?.jsonObject
                ?.get("sectionListRenderer")?.jsonObject?.get("contents")?.jsonArray
            
            contents?.forEach { 
                it.jsonObject["musicPlaylistShelfRenderer"]?.jsonObject?.let { shelf -> processPlaylistShelf(shelf) }
            }

            var continuationToken = contents?.firstOrNull()?.jsonObject?.get("musicPlaylistShelfRenderer")?.jsonObject
                ?.get("continuations")?.jsonArray?.getOrNull(0)?.jsonObject?.get("nextContinuationData")?.jsonObject?.get("continuation")?.jsonPrimitive?.contentOrNull

            while (continuationToken != null) {
                val contResponse = innerTube.browse(YouTubeClient.WEB_REMIX, continuation = continuationToken, setLogin = true).body<BrowseResponse>()
                val contShelf = contResponse.continuationContents?.musicPlaylistShelfContinuation
                if (contShelf != null) {
                    contShelf["contents"]?.jsonArray?.forEach { 
                        val item = parseItem(it.jsonObject["musicResponsiveListItemRenderer"]?.jsonObject)
                        if (item is SongItem) {
                            songs.add(item.copy(album = AlbumTiny(album.id, album.title), thumbnail = item.thumbnail ?: album.thumbnail))
                        }
                    }
                    continuationToken = contShelf["continuations"]?.jsonArray?.getOrNull(0)?.jsonObject
                        ?.get("nextContinuationData")?.jsonObject?.get("continuation")?.jsonPrimitive?.contentOrNull
                } else {
                    continuationToken = null
                }
            }
        } catch (_: Exception) {}
        return songs
    }

    suspend fun loadContinuation(token: String): com.metrolist.shared.api.innertube.models.YouTubeDataPage<YTItem> {
        return try {
            val response = innerTube.browse(YouTubeClient.WEB_REMIX, continuation = token, setLogin = true).body<BrowseResponse>()
            val parsed = parseBrowseResponse(response)
            com.metrolist.shared.api.innertube.models.YouTubeDataPage(parsed.sections.values.flatten(), parsed.continuation)
        } catch (_: Exception) {
            com.metrolist.shared.api.innertube.models.YouTubeDataPage(emptyList())
        }
    }

    suspend fun search(query: String): List<YTItem> {
        return try {
            val response = innerTube.search(YouTubeClient.WEB_REMIX, query = query).body<JsonObject>()
            parseSearchResponse(response)
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun getStreamUrl(videoId: String): String? {
        if (innerTube.visitorData == null) {
            innerTube.fetchFreshVisitorData()
        }

        suspend fun tryPlayer(client: YouTubeClient): PlayerResponse? {
            return try {
                val resp = innerTube.player(client, videoId).body<PlayerResponse>()
                if (resp.playabilityStatus.status == "OK" && resp.streamingData != null) resp else null
            } catch (_: Exception) { null }
        }

        val response = tryPlayer(YouTubeClient.TVHTML5_EMBEDDED)
            ?: tryPlayer(YouTubeClient.ANDROID_VR)
            ?: tryPlayer(YouTubeClient.ANDROID)
            ?: tryPlayer(YouTubeClient.WEB_REMIX)
        
        if (response == null) return null

        val formats = response.streamingData?.adaptiveFormats ?: emptyList()
        val audioFormat = formats.filter { it.isAudio }
            .sortedByDescending { it.bitrate }
            .firstOrNull { it.mimeType.contains("audio/webm") } 
            ?: formats.filter { it.isAudio }.maxByOrNull { it.bitrate }
        
        return audioFormat?.url
    }

    suspend fun getLyrics(title: String, artist: String, duration: Long?, album: String? = null): String? {
        return lyricsProvider.getLyrics(title, artist, duration, album)
    }

    private data class ParsedBrowse(val sections: Map<String, List<YTItem>>, val continuation: String?)

    private fun parseBrowseResponse(response: BrowseResponse): ParsedBrowse {
        val sections = mutableMapOf<String, List<YTItem>>()
        var continuationToken: String? = null
        
        val contents = response.contents ?: return ParsedBrowse(emptyMap(), null)
        
        val sectionList = contents.singleColumnBrowseResultsRenderer?.let { it["tabs"]?.jsonArray?.getOrNull(0)?.jsonObject }
            ?.get("tabRenderer")?.jsonObject?.get("content")?.jsonObject?.get("sectionListRenderer")?.jsonObject
            ?: contents.twoColumnBrowseResultsRenderer?.let { it["tabs"]?.jsonArray?.getOrNull(0)?.jsonObject }
            ?.get("tabRenderer")?.jsonObject?.get("content")?.jsonObject?.get("sectionListRenderer")?.jsonObject
            ?: contents.twoColumnBrowseResultsRenderer?.get("secondaryContents")?.jsonObject?.get("sectionListRenderer")?.jsonObject
            ?: contents.sectionListRenderer

        sectionList?.get("contents")?.jsonArray?.forEach { sectionElement ->
            val sectionObj = sectionElement.jsonObject
            if (sectionObj.containsKey("itemSectionRenderer")) {
                sectionObj["itemSectionRenderer"]?.jsonObject?.get("contents")?.jsonArray?.forEach { subElement ->
                    processShelf(subElement.jsonObject, sections)
                }
            } else {
                processShelf(sectionObj, sections)
            }
        }
        
        continuationToken = sectionList?.get("continuations")?.jsonArray?.getOrNull(0)?.jsonObject
            ?.get("nextContinuationData")?.jsonObject?.get("continuation")?.jsonPrimitive?.contentOrNull

        // Handle musicPlaylistShelfRenderer which is common in playlist responses
        val secondaryContents = contents.twoColumnBrowseResultsRenderer?.get("secondaryContents")?.jsonObject
        secondaryContents?.get("sectionListRenderer")?.jsonObject?.get("contents")?.jsonArray?.forEach { section ->
            val playlistShelf = section.jsonObject["musicPlaylistShelfRenderer"]?.jsonObject
            if (playlistShelf != null) {
                processShelf(section.jsonObject, sections)
            }
        }

        response.continuationContents?.let { cont ->
            val musicShelf = cont.musicShelfContinuation 
                ?: cont.musicPlaylistShelfContinuation
                ?: cont.gridContinuation
                ?: cont.sectionListContinuation?.get("contents")?.jsonArray?.getOrNull(0)?.jsonObject?.get("musicShelfRenderer")?.jsonObject
                
            if (musicShelf != null) {
                val itemsArray = musicShelf["contents"]?.jsonArray ?: musicShelf["items"]?.jsonArray
                val items = itemsArray?.mapNotNull { parseItem(it.jsonObject["musicResponsiveListItemRenderer"]?.jsonObject ?: it.jsonObject) } ?: emptyList()
                sections["Continuation"] = items
                continuationToken = musicShelf["continuations"]?.jsonArray?.getOrNull(0)?.jsonObject
                    ?.get("nextContinuationData")?.jsonObject?.get("continuation")?.jsonPrimitive?.contentOrNull
            }
        }

        return ParsedBrowse(sections, continuationToken)
    }

    private fun processShelf(shelfObj: JsonObject, sections: MutableMap<String, List<YTItem>>) {
        val shelf = shelfObj["musicShelfRenderer"]?.jsonObject
            ?: shelfObj["musicCarouselShelfRenderer"]?.jsonObject
            ?: shelfObj["musicPlaylistShelfRenderer"]?.jsonObject
            ?: shelfObj["gridRenderer"]?.jsonObject
            ?: return

        val title = shelf["title"]?.jsonObject?.get("runs")?.jsonArray?.getOrNull(0)?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull 
            ?: shelf["title"]?.jsonObject?.get("simpleText")?.jsonPrimitive?.contentOrNull
            ?: shelf["header"]?.jsonObject?.get("musicCarouselShelfBasicHeaderRenderer")?.jsonObject?.get("title")?.jsonObject?.get("runs")?.jsonArray?.getOrNull(0)?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull
            ?: shelf["header"]?.jsonObject?.get("gridHeaderRenderer")?.jsonObject?.get("title")?.jsonObject?.get("runs")?.jsonArray?.getOrNull(0)?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull
            ?: "Tracks"
            
        val itemsArray = shelf["contents"]?.jsonArray ?: shelf["items"]?.jsonArray
        val items = itemsArray?.mapNotNull { item ->
            val itemObj = item.jsonObject
            parseItem(itemObj["musicResponsiveListItemRenderer"]?.jsonObject 
                ?: itemObj["musicTwoRowItemRenderer"]?.jsonObject
                ?: itemObj)
        } ?: emptyList()
        
        if (items.isNotEmpty()) {
            val existing = sections[title] ?: emptyList()
            sections[title] = existing + items
        }
    }

    private fun parseSearchResponse(response: JsonObject): List<YTItem> {
        val items = mutableListOf<YTItem>()
        val contents = response["contents"]?.jsonObject
            ?.get("tabbedSearchResultsRenderer")?.jsonObject
            ?.get("tabs")?.jsonArray?.getOrNull(0)?.jsonObject
            ?.get("tabRenderer")?.jsonObject
            ?.get("content")?.jsonObject
            ?.get("sectionListRenderer")?.jsonObject
            ?.get("contents")?.jsonArray

        contents?.forEach { section ->
            val shelf = section.jsonObject["musicShelfRenderer"]?.jsonObject
            shelf?.get("contents")?.jsonArray?.forEach { item ->
                val ytItem = parseItem(item.jsonObject["musicResponsiveListItemRenderer"]?.jsonObject)
                if (ytItem != null) items.add(ytItem)
            }
        }
        return items
    }

    private fun parseItem(item: JsonObject?): YTItem? {
        if (item == null) return null
        return try {
            fun parseDuration(text: String?): Long? {
                if (text == null) return null
                val parts = text.split(":").mapNotNull { it.toLongOrNull() }
                return when (parts.size) {
                    1 -> parts[0]
                    2 -> parts[0] * 60 + parts[1]
                    3 -> parts[0] * 3600 + parts[1] * 60 + parts[2]
                    else -> null
                }
            }

            fun extractMetadata(runs: JsonArray?): Triple<List<ArtistTiny>, AlbumTiny?, Long?> {
                val artists = mutableListOf<ArtistTiny>()
                var album: AlbumTiny? = null
                var duration: Long? = null
                val potentialAlbums = mutableListOf<AlbumTiny>()
                val metadataStrings = mutableListOf<String>()

                runs?.forEach { run ->
                    val runObj = run.jsonObject
                    val text = (runObj["text"]?.jsonPrimitive?.contentOrNull ?: "").trim()
                    val nav = runObj["navigationEndpoint"]?.jsonObject
                    val runBrowseId = nav?.get("browseEndpoint")?.jsonObject?.get("browseId")?.jsonPrimitive?.contentOrNull
                    
                    if (text == "•" || text == "&" || text == "," || text.isBlank()) return@forEach
                    
                    // Filter out non-artist/album strings
                    val lower = text.lowercase()
                    if (lower == "song" || lower == "video" || lower.contains("views") || lower.contains("likes") || lower.contains("play count") || lower.contains("subscribers")) {
                        return@forEach
                    }
                    
                    if (runBrowseId != null) {
                        if (runBrowseId.startsWith("MPREb") || runBrowseId.contains("release_detail") || runBrowseId.startsWith("FEmusic_release_detail")) {
                            album = AlbumTiny(runBrowseId, text)
                        } else if (runBrowseId.startsWith("UC")) {
                            artists.add(ArtistTiny(runBrowseId, text))
                        } else {
                            // Catch-all for linked items that aren't artists
                            potentialAlbums.add(AlbumTiny(runBrowseId, text))
                        }
                    } else {
                        val d = parseDuration(text)
                        if (d != null) {
                            duration = d
                        } else {
                            metadataStrings.add(text)
                        }
                    }
                }
                
                // If we didn't find an explicit album browseId, use the first potential album
                if (album == null && potentialAlbums.isNotEmpty()) {
                    album = potentialAlbums[0]
                }

                // Heuristic for unlinked metadata
                if (artists.isEmpty() || album == null) {
                    val filtered = metadataStrings.filter { str ->
                        !str.matches(Regex("\\d{4}")) && // Year
                        !str.contains(Regex("\\d+.*", RegexOption.IGNORE_CASE)) // Likely views/likes
                    }
                    
                    if (artists.isEmpty() && filtered.isNotEmpty()) {
                        artists.add(ArtistTiny(null, filtered[0]))
                        if (album == null && filtered.size >= 2) {
                            album = AlbumTiny(null, filtered[1])
                        }
                    } else if (album == null && filtered.isNotEmpty()) {
                        album = AlbumTiny(null, filtered[0])
                    }
                }

                return Triple(artists, album, duration)
            }

            val flexColumns = item["flexColumns"]?.jsonArray
            if (flexColumns != null) {
                // musicResponsiveListItemRenderer
                val titleColumn = flexColumns.getOrNull(0)?.jsonObject?.get("musicResponsiveListItemFlexColumnRenderer")?.jsonObject
                val title = titleColumn?.get("text")?.jsonObject?.get("runs")?.jsonArray?.mapNotNull { it.jsonObject["text"]?.jsonPrimitive?.contentOrNull }
                    ?.firstOrNull { 
                        val l = it.lowercase()
                        l != "song" && l != "video" && it != " • " && it != "•" 
                    } ?: return null
                
                val navigationEndpoint = item["navigationEndpoint"]?.jsonObject ?: item["onTap"]?.jsonObject
                val browseId = navigationEndpoint?.get("browseEndpoint")?.jsonObject?.get("browseId")?.jsonPrimitive?.contentOrNull
                val videoId = item["playlistItemData"]?.jsonObject?.get("videoId")?.jsonPrimitive?.contentOrNull
                    ?: navigationEndpoint?.get("watchEndpoint")?.jsonObject?.get("videoId")?.jsonPrimitive?.contentOrNull

                var thumbnail = item["thumbnail"]?.jsonObject?.get("musicThumbnailRenderer")?.jsonObject
                    ?.get("thumbnail")?.jsonObject?.get("thumbnails")?.jsonArray?.lastOrNull()?.jsonObject?.get("url")?.jsonPrimitive?.contentOrNull
                thumbnail = thumbnail?.replace(Regex("=w\\d+-h\\d+.*$"), "=w1000-h1000")

                return when {
                    videoId != null -> {
                        var artists = emptyList<ArtistTiny>()
                        var album: AlbumTiny? = null
                        var duration: Long? = null
                        for (i in 1 until flexColumns.size) {
                            val runs = flexColumns[i].jsonObject["musicResponsiveListItemFlexColumnRenderer"]?.jsonObject?.get("text")?.jsonObject?.get("runs")?.jsonArray
                            if (runs != null) {
                                val meta = extractMetadata(runs)
                                if (meta.first.isNotEmpty()) artists = meta.first
                                if (meta.second != null) album = meta.second
                                if (meta.third != null) duration = meta.third
                            }
                        }
                        SongItem(videoId, title, thumbnail, artists, album, duration)
                    }
                    browseId != null -> {
                        if (browseId.startsWith("FEmusic_library_") || browseId.startsWith("VL") || browseId.startsWith("PL") || browseId.startsWith("RD")) PlaylistItem(browseId, title, thumbnail)
                        else if (browseId.startsWith("UC")) ArtistItem(browseId, title, thumbnail)
                        else {
                            val subtitleRuns = item["subtitle"]?.jsonObject?.get("runs")?.jsonArray
                            val (artists, _, _) = extractMetadata(subtitleRuns)
                            AlbumItem(browseId, title, thumbnail, artists)
                        }
                    }
                    else -> null
                }
            } 
            
            // musicTwoRowItemRenderer
            val titleNode = item["title"]?.jsonObject
            val title = titleNode?.get("runs")?.jsonArray?.getOrNull(0)?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull 
                ?: titleNode?.get("simpleText")?.jsonPrimitive?.contentOrNull
                ?: item["title"]?.jsonPrimitive?.contentOrNull
            
            if (title != null) {
                val navigationEndpoint = item["navigationEndpoint"]?.jsonObject
                val browseId = navigationEndpoint?.get("browseEndpoint")?.jsonObject?.get("browseId")?.jsonPrimitive?.contentOrNull
                val videoId = navigationEndpoint?.get("watchEndpoint")?.jsonObject?.get("videoId")?.jsonPrimitive?.contentOrNull
                
                var thumbnail = (item["thumbnailRenderer"]?.jsonObject ?: item["thumbnail"]?.jsonObject)
                    ?.get("musicThumbnailRenderer")?.jsonObject
                    ?.get("thumbnail")?.jsonObject?.get("thumbnails")?.jsonArray?.lastOrNull()?.jsonObject?.get("url")?.jsonPrimitive?.contentOrNull
                thumbnail = thumbnail?.replace(Regex("=w\\d+-h\\d+.*$"), "=w1000-h1000")
                
                val subtitleRuns = item["subtitle"]?.jsonObject?.get("runs")?.jsonArray
                val (artists, album, duration) = extractMetadata(subtitleRuns)

                return when {
                    videoId != null -> SongItem(videoId, title, thumbnail, artists, album, duration)
                    browseId != null -> {
                        if (browseId.startsWith("FEmusic_library_") || browseId.startsWith("VL") || browseId.startsWith("PL") || browseId.startsWith("RD")) PlaylistItem(browseId, title, thumbnail)
                        else if (browseId.startsWith("UC")) ArtistItem(browseId, title, thumbnail)
                        else AlbumItem(browseId, title, thumbnail, artists)
                    }
                    else -> null
                }
            }

            null
        } catch (_: Exception) {
            null
        }
    }
}
