/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.extensions

import android.os.Bundle
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata.MEDIA_TYPE_MUSIC
import com.metrolist.innertube.models.SongItem
// import com.metrolist.music.ui.utils.resize  // TODO: Add resize extension
import com.metrolist.music.db.entities.Song
import com.metrolist.music.models.MediaMetadata

val MediaItem.metadata: MediaMetadata?
    get() = localConfiguration?.tag as? MediaMetadata

fun Song.toMediaItem() = MediaItem.Builder()
    .setMediaId(song.id)
    .setUri(song.id)
    .setCustomCacheKey(song.id)
    .setTag(toMediaMetadata())
    .setMediaMetadata(
        androidx.media3.common.MediaMetadata.Builder()
            .setTitle(song.title)
            .setSubtitle(artists.joinToString { it.name })
            .setArtist(artists.joinToString { it.name })
            .setArtworkUri(song.thumbnailUrl?.toUri())
            .setAlbumTitle(song.albumName)
            .setAlbumArtist(artists.firstOrNull()?.name)
            .setDisplayTitle(song.title)
            .setMediaType(MEDIA_TYPE_MUSIC)
            .setIsBrowsable(false)
            .setIsPlayable(true)
            .setExtras(Bundle().apply {
                putString("artwork_uri", song.thumbnailUrl)
            })
            .build()
    )
    .build()

fun SongItem.toMediaItem() = MediaItem.Builder()
    .setMediaId(id)
    .setUri(id)
    .setCustomCacheKey(id)
    .setTag(toMediaMetadata())
    .setMediaMetadata(
        androidx.media3.common.MediaMetadata.Builder()
            .setTitle(title)
            .setSubtitle(artists.joinToString { it.name })
            .setArtist(artists.joinToString { it.name })
            .setArtworkUri(thumbnail?.toUri())
            .setAlbumTitle(album?.name)
            .setAlbumArtist(artists.firstOrNull()?.name)
            .setDisplayTitle(title)
            .setMediaType(MEDIA_TYPE_MUSIC)
            .setIsBrowsable(false)
            .setIsPlayable(true)
            .setExtras(Bundle().apply {
                thumbnail?.let { putString("artwork_uri", it) }
            })
            .build()
    )
    .build()

fun MediaMetadata.toMediaItem() = MediaItem.Builder()
    .setMediaId(id)
    .setUri(id)
    .setCustomCacheKey(id)
    .setTag(this)
    .setMediaMetadata(
        androidx.media3.common.MediaMetadata.Builder()
            .setTitle(title)
            .setSubtitle(artists.joinToString { it.name })
            .setArtist(artists.joinToString { it.name })
            .setArtworkUri(thumbnailUrl?.toUri())
            .setAlbumTitle(album?.title)
            .setAlbumArtist(artists.firstOrNull()?.name)
            .setDisplayTitle(title)
            .setMediaType(MEDIA_TYPE_MUSIC)
            .setIsBrowsable(false)
            .setIsPlayable(true)
            .setExtras(Bundle().apply {
                thumbnailUrl?.let { putString("artwork_uri", it) }
            })
            .build()
    )
    .build()

// Extension function to convert Song to MediaMetadata
fun Song.toMediaMetadata(): MediaMetadata {
    return MediaMetadata(
        id = song.id,
        title = song.title,
        artists = artists.map { 
            com.metrolist.music.models.MediaMetadata.Artist(
                id = it.id,
                name = it.name
            )
        },
        duration = song.duration?.toInt() ?: -1,
        thumbnailUrl = song.thumbnailUrl,
        album = song.albumName?.let { 
            com.metrolist.music.models.MediaMetadata.Album(
                id = song.albumId ?: "",
                title = it
            )
        },
        explicit = song.explicit
    )
}

// Extension function to convert SongItem to MediaMetadata
fun SongItem.toMediaMetadata(): MediaMetadata {
    return MediaMetadata(
        id = id,
        title = title,
        artists = artists.map { 
            com.metrolist.music.models.MediaMetadata.Artist(
                id = it.id,
                name = it.name
            )
        },
        duration = duration?.toInt() ?: -1,
        thumbnailUrl = thumbnail,
        album = album?.let { 
            com.metrolist.music.models.MediaMetadata.Album(
                id = it.id,
                title = it.name
            )
        },
        explicit = explicit
    )
}

// Extension for MediaItem.Builder to set custom cache key
fun MediaItem.Builder.setCustomCacheKey(key: String?): MediaItem.Builder {
    // Note: This is a placeholder - actual implementation depends on ExoPlayer version
    return this
}
