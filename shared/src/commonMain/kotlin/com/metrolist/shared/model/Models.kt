package com.metrolist.shared.model

import kotlinx.serialization.Serializable

@Serializable
sealed class YTItem {
    abstract val id: String
    abstract val title: String
    abstract val thumbnail: String?
}

@Serializable
data class SongItem(
    override val id: String,
    override val title: String,
    override val thumbnail: String?,
    val artists: List<ArtistTiny>,
    val album: AlbumTiny?,
    val duration: Long? = null,
    val isExplicit: Boolean = false,
    val endpointParams: String? = null,
    val libraryAddToken: String? = null,
    val libraryRemoveToken: String? = null,
    val musicVideoType: String? = null
) : YTItem()

@Serializable
data class AlbumItem(
    override val id: String,
    override val title: String,
    override val thumbnail: String?,
    val artists: List<ArtistTiny>,
    val year: String? = null,
    val playlistId: String? = null,
    val isExplicit: Boolean = false
) : YTItem()

@Serializable
data class ArtistItem(
    override val id: String,
    override val title: String,
    override val thumbnail: String?,
    val subscribers: String? = null,
    val banner: String? = null,
    val description: String? = null,
    val monthlyListeners: String? = null,
    val radioEndpointParams: String? = null,
    val isSubscribed: Boolean = false
) : YTItem()

@Serializable
data class PlaylistItem(
    override val id: String,
    override val title: String,
    override val thumbnail: String?,
    val author: String? = null,
    val songCount: String? = null,
    val playEndpointParams: String? = null,
    val radioEndpointParams: String? = null
) : YTItem()

@Serializable
data class PodcastItem(
    override val id: String,
    override val title: String,
    override val thumbnail: String?,
    val author: String? = null,
    val episodeCount: String? = null
) : YTItem()

@Serializable
data class EpisodeItem(
    override val id: String,
    override val title: String,
    override val thumbnail: String?,
    val author: ArtistTiny? = null,
    val publishDateText: String? = null,
    val duration: Long? = null,
) : YTItem()

@Serializable
data class ArtistTiny(
    val id: String?,
    val name: String,
    val thumbnail: String? = null
)

@Serializable
data class AlbumTiny(
    val id: String?,
    val name: String
)

@Serializable
data class HomeSection(
    val title: String,
    val items: List<YTItem>,
    val subtitle: String? = null,
    val browseId: String? = null,
    val params: String? = null,
    val isListStyle: Boolean = false
)

@Serializable
data class AlbumPage(
    val album: AlbumItem,
    val songs: List<SongItem>,
    val otherVersions: List<AlbumItem> = emptyList()
)

@Serializable
data class Chip(
    val title: String,
    val params: String? = null
)

@Serializable
data class HomePageData(
    val chips: List<Chip> = emptyList(),
    val sections: List<HomeSection> = emptyList(),
    val continuation: String? = null
)

@Serializable
data class SearchSummary(
    val title: String,
    val items: List<YTItem>
)

@Serializable
data class SearchSummaryPage(
    val summaries: List<SearchSummary>
)

@Serializable
enum class SearchFilter(val value: String, val label: String) {
    SONG("EgWKAQIIAWoKEAkQBRAKEAMQBA%3D%3D", "Songs"),
    VIDEO("EgWKAQIQAWoKEAkQChAFEAMQBA%3D%3D", "Videos"),
    ALBUM("EgWKAQIYAWoKEAkQChAFEAMQBA%3D%3D", "Albums"),
    ARTIST("EgWKAQIgAWoKEAkQChAFEAMQBA%3D%3D", "Artists"),
    FEATURED_PLAYLIST("EgeKAQQoADgBagwQDhAKEAMQBRAJEAQ%3D", "Playlists"),
    COMMUNITY_PLAYLIST("EgeKAQQoAEABagoQAxAEEAoQCRAF", "Community Playlists"),
    PODCAST("EgWKAQioAWoKEAkQChAFEAMQBA%3D%3D", "Podcasts"),
    EPISODE("EgWKAQisAWoKEAkQChAFEAMQBA%3D%3D", "Episodes")
}

@Serializable
data class SearchResultPage(
    val items: List<YTItem>,
    val continuation: String? = null
)

@Serializable
data class SearchSuggestions(
    val queries: List<String>,
    val recommendedItems: List<YTItem>
)

// Legacy Track for compatibility during migration
typealias Track = SongItem

// Filtering extensions for search results
fun List<YTItem>.filterExplicit(hideExplicit: Boolean): List<YTItem> =
    if (!hideExplicit) this else this.filter {
        when (it) {
            is SongItem -> !it.isExplicit
            is AlbumItem -> !it.isExplicit
            else -> true
        }
    }

fun List<YTItem>.filterVideoSongs(hideVideoSongs: Boolean): List<YTItem> =
    if (!hideVideoSongs) this else this.filter {
        when (it) {
            is SongItem -> it.endpointParams?.contains("videoId") != true
            else -> true
        }
    }

fun List<YTItem>.filterYoutubeShorts(hideYoutubeShorts: Boolean): List<YTItem> =
    if (!hideYoutubeShorts) this else this.filter {
        when (it) {
            is SongItem -> it.endpointParams?.contains("shortsId") != true
            else -> true
        }
    }

enum class LyricsProvider { AUTO, LRCLIB, LYRICSPLUS, YOUTUBE }
