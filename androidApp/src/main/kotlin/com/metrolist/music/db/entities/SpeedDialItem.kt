/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.metrolist.shared.model.AlbumItem
import com.metrolist.shared.model.AlbumTiny
import com.metrolist.shared.model.ArtistItem
import com.metrolist.shared.model.ArtistTiny
import com.metrolist.shared.model.EpisodeItem
import com.metrolist.shared.model.PlaylistItem
import com.metrolist.shared.model.PodcastItem
import com.metrolist.shared.model.SongItem
import com.metrolist.shared.model.YTItem

@Entity(tableName = "speed_dial_item")
data class SpeedDialItem(
    @PrimaryKey val id: String,
    val secondaryId: String? = null,
    val title: String,
    val subtitle: String? = null,
    val thumbnailUrl: String? = null,
    val type: String, // "SONG", "ALBUM", "ARTIST", "PLAYLIST", "LOCAL_PLAYLIST"
    val explicit: Boolean = false,
    val createDate: Long = System.currentTimeMillis()
) {
    fun toYTItem(): YTItem {
        return when (type) {
            "SONG" -> SongItem(
                id = id,
                title = title,
                artists = subtitle?.split(", ")?.map { ArtistTiny(id = null, name = it) } ?: emptyList(),
                thumbnail = thumbnailUrl ?: "",
                album = null,
                isExplicit = explicit
            )
            "ALBUM" -> AlbumItem(
                id = id,
                title = title,
                artists = subtitle?.split(", ")?.map { ArtistTiny(id = null, name = it) } ?: emptyList(),
                thumbnail = thumbnailUrl ?: "",
                isExplicit = explicit
            )
            "ARTIST" -> ArtistItem(
                id = id,
                title = title,
                thumbnail = thumbnailUrl
            )
            "PLAYLIST", "LOCAL_PLAYLIST" -> PlaylistItem(
                id = id,
                title = title,
                thumbnail = thumbnailUrl
            )
            else -> throw IllegalArgumentException("Unknown type: $type")
        }
    }

    companion object {
        fun fromYTItem(item: YTItem): SpeedDialItem {
            return when (item) {
                is SongItem -> SpeedDialItem(
                    id = item.id,
                    title = item.title,
                    subtitle = item.artists.joinToString(", ") { it.name },
                    thumbnailUrl = item.thumbnail,
                    type = "SONG",
                    explicit = item.isExplicit
                )
                is AlbumItem -> SpeedDialItem(
                    id = item.id,
                    title = item.title,
                    subtitle = item.artists?.joinToString(", ") { it.name },
                    thumbnailUrl = item.thumbnail,
                    type = "ALBUM",
                    explicit = item.isExplicit
                )
                is ArtistItem -> SpeedDialItem(
                    id = item.id,
                    title = item.title,
                    thumbnailUrl = item.thumbnail,
                    type = "ARTIST"
                )
                is PlaylistItem -> SpeedDialItem(
                    id = item.id,
                    title = item.title,
                    subtitle = item.author,
                    thumbnailUrl = item.thumbnail,
                    type = "PLAYLIST"
                )
                is PodcastItem -> SpeedDialItem(
                    id = item.id,
                    title = item.title,
                    subtitle = item.author,
                    thumbnailUrl = item.thumbnail,
                    type = "PLAYLIST"
                )
                is EpisodeItem -> SpeedDialItem(
                    id = item.id,
                    title = item.title,
                    subtitle = item.author?.name,
                    thumbnailUrl = item.thumbnail,
                    type = "SONG"
                )
            }
        }
    }
}
