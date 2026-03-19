package com.metrolist.desktop.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.metrolist.desktop.state.AppState
import com.metrolist.shared.model.AlbumItem
import com.metrolist.shared.model.ArtistItem
import com.metrolist.shared.model.PlaylistItem
import com.metrolist.shared.model.SongItem

@Composable
private fun MenuSection(vararg items: @Composable () -> Unit) {
    items.forEach { it() }
}

@Composable
private fun MenuItem(text: String, leadingIcon: ImageVector? = null, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(text) },
        leadingIcon = leadingIcon?.let { icon -> { Icon(icon, contentDescription = null) } },
        onClick = onClick
    )
}

@Composable
fun SongContextMenu(
    song: SongItem,
    expanded: Boolean,
    onDismiss: () -> Unit
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        val isLiked = song.id in AppState.likedSongIds

        val isDownloaded = AppState.downloadStates[song.id] == com.metrolist.desktop.utils.DownloadState.DOWNLOADED
        val isDownloading = AppState.downloadStates[song.id] == com.metrolist.desktop.utils.DownloadState.DOWNLOADING

        MenuItem("Play Next") {
            AppState.playNext(song)
            onDismiss()
        }
        MenuItem("Add to Queue") {
            AppState.addToQueue(song)
            onDismiss()
        }
        MenuItem("Start Radio") {
            AppState.startRadio(song)
            onDismiss()
        }
        HorizontalDivider()
        MenuItem(if (isLiked) "Unlike" else "Like",
            leadingIcon = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder
        ) {
            AppState.toggleLike(song)
            onDismiss()
        }
        if (isDownloading) {
            // Show progress indicator
            val progress = AppState.downloadProgress[song.id] ?: 0f
            MenuItem(
                text = "Downloading... ${(progress * 100).toInt()}%",
                leadingIcon = Icons.Outlined.Download,
                onClick = { onDismiss() }
            )
        } else if (isDownloaded) {
            MenuItem("Remove Download", leadingIcon = Icons.Outlined.Download) {
                com.metrolist.desktop.utils.DownloadManager.removeDownloadMetadata(song.id)
                com.metrolist.desktop.state.AppState.downloadedFiles = com.metrolist.desktop.utils.DownloadManager.getDownloadedFiles()
                com.metrolist.desktop.state.AppState.downloadStates = com.metrolist.desktop.utils.DownloadManager.downloadStates.value
                onDismiss()
            }
        } else {
            MenuItem("Download", leadingIcon = Icons.Outlined.Download) {
                com.metrolist.desktop.utils.DownloadManager.download(song)
                onDismiss()
            }
        }
        if (AppState.isSignedIn) {
            MenuItem("Add to Playlist") {
                AppState.showAddToPlaylistSong = song
                AppState.fetchUserPlaylists()
                onDismiss()
            }
        }
        MenuItem("Share") {
            AppState.copyToClipboard("https://music.youtube.com/watch?v=${song.id}")
            onDismiss()
        }
        val navigableArtists = song.artists.filter { it.id != null }
        val albumId = song.album?.id
        if (navigableArtists.isNotEmpty() || albumId != null) {
            HorizontalDivider()
            if (navigableArtists.size == 1) {
                MenuItem("Go to Artist") {
                    AppState.fetchArtistData(navigableArtists[0].id!!)
                    onDismiss()
                }
            } else if (navigableArtists.size > 1) {
                navigableArtists.forEach { artist ->
                    MenuItem("Go to ${artist.name}") {
                        AppState.fetchArtistData(artist.id!!)
                        onDismiss()
                    }
                }
            }
            if (albumId != null) {
                MenuItem("Go to Album") {
                    AppState.fetchAlbumData(albumId)
                    onDismiss()
                }
            }
        }
    }
}

@Composable
fun AlbumContextMenu(
    album: AlbumItem,
    songs: List<SongItem> = emptyList(),
    expanded: Boolean,
    onDismiss: () -> Unit
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        if (songs.isNotEmpty()) {
            MenuItem("Play") {
                AppState.playTrack(songs.first(), songs)
                onDismiss()
            }
            MenuItem("Play Next") {
                AppState.playNext(songs)
                onDismiss()
            }
            MenuItem("Add to Queue") {
                AppState.addToQueue(songs)
                onDismiss()
            }
            HorizontalDivider()
        }
        MenuItem("Start Radio") {
            AppState.startRadio(SongItem(id = album.id, title = album.title, thumbnail = album.thumbnail, artists = album.artists, album = null))
            onDismiss()
        }
        MenuItem("Share") {
            AppState.copyToClipboard("https://music.youtube.com/playlist?list=${album.playlistId ?: album.id}")
            onDismiss()
        }
        val navigableArtists = album.artists.filter { it.id != null }
        if (navigableArtists.isNotEmpty()) {
            HorizontalDivider()
            if (navigableArtists.size == 1) {
                MenuItem("Go to Artist") {
                    AppState.fetchArtistData(navigableArtists[0].id!!)
                    onDismiss()
                }
            } else {
                navigableArtists.forEach { artist ->
                    MenuItem("Go to ${artist.name}") {
                        AppState.fetchArtistData(artist.id!!)
                        onDismiss()
                    }
                }
            }
        }
    }
}

@Composable
fun ArtistContextMenu(
    artist: ArtistItem,
    expanded: Boolean,
    onDismiss: () -> Unit
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        MenuItem("Share") {
            AppState.copyToClipboard("https://music.youtube.com/channel/${artist.id}")
            onDismiss()
        }
    }
}

@Composable
fun PlaylistContextMenu(
    playlist: PlaylistItem,
    songs: List<SongItem> = emptyList(),
    expanded: Boolean,
    onDismiss: () -> Unit
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        if (songs.isNotEmpty()) {
            MenuItem("Play") {
                AppState.playTrack(songs.first(), songs)
                onDismiss()
            }
            MenuItem("Play Next") {
                AppState.playNext(songs)
                onDismiss()
            }
            MenuItem("Add to Queue") {
                AppState.addToQueue(songs)
                onDismiss()
            }
            HorizontalDivider()
        }
        MenuItem("Share") {
            AppState.copyToClipboard("https://music.youtube.com/playlist?list=${playlist.id}")
            onDismiss()
        }
    }
}
