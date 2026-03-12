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
    val libraryRemoveToken: String? = null
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
    val monthlyListeners: String? = null
) : YTItem()

@Serializable
data class PlaylistItem(
    override val id: String,
    override val title: String,
    override val thumbnail: String?,
    val author: String? = null,
    val songCount: String? = null,
    val playEndpointParams: String? = null
) : YTItem()

@Serializable
data class ArtistTiny(
    val id: String?,
    val name: String
)

@Serializable
data class AlbumTiny(
    val id: String?,
    val name: String
)

@Serializable
data class HomeSection(
    val title: String,
    val items: List<YTItem>
)

@Serializable
data class AlbumPage(
    val album: AlbumItem,
    val songs: List<SongItem>,
    val otherVersions: List<AlbumItem> = emptyList()
)

// Legacy Track for compatibility during migration
typealias Track = SongItem
