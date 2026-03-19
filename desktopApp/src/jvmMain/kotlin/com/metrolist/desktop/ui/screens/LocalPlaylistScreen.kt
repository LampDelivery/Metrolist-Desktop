package com.metrolist.desktop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.OfflinePin
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.metrolist.desktop.state.AppState
import com.metrolist.desktop.ui.components.AsyncImage
import com.metrolist.desktop.ui.components.YTListItem
import com.metrolist.desktop.utils.DownloadedFile
import com.metrolist.desktop.utils.HistoryStat
import com.metrolist.shared.model.SongItem
import com.metrolist.desktop.utils.DownloadManager

// ───────────────────────────────────────────────────────────────── Router ──

@Composable
fun LocalPlaylistScreen(type: String, colorScheme: ColorScheme) {
    when (type) {
        "liked" -> LikedPlaylistScreen(colorScheme)
        "top50" -> TopPlaylistScreen(colorScheme)
        "downloaded" -> DownloadedPlaylistScreen(colorScheme)
        "cached" -> CachedPlaylistScreen(colorScheme)
        "uploaded" -> UploadedPlaylistScreen(colorScheme)
    }
}

// ──────────────────────────────────────────────────────────── Liked Songs ──

@Composable
private fun LikedPlaylistScreen(colorScheme: ColorScheme) {
    val likedIds = AppState.likedSongIds.toList()
    val songs = likedIds.mapNotNull { songId ->
        val song = AppState.queue.items.value.find { it.id == songId }
            ?: if (AppState.currentTrack?.id == songId) AppState.currentTrack else null
        song ?: SongItem(
            id = songId,
            title = "Liked Song",
            artists = listOf(),
            thumbnail = "https://img.youtube.com/vi/$songId/maxresdefault.jpg",
            duration = null,
            album = null
        )
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth < 700.dp) {
            LikedPlaylistNarrowLayout(songs, colorScheme)
        } else {
            LikedPlaylistWideLayout(songs, colorScheme)
        }
    }
}

@Composable
private fun LikedPlaylistWideLayout(songs: List<SongItem>, colorScheme: ColorScheme) {
    Box(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .widthIn(max = 1360.dp)
                .fillMaxWidth()
                .fillMaxHeight()
                .align(Alignment.TopCenter)
                .padding(start = 48.dp, end = 48.dp, top = 28.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(56.dp)
        ) {
            // ── Left panel ───────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .width(220.dp)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
            ) {
                IconButton(onClick = { AppState.selectedLocalPlaylist = null }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = colorScheme.onSurface)
                }
                Spacer(Modifier.height(20.dp))

                // Playlist thumbnail
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .shadow(24.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(colorScheme.error),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Favorite, null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.White
                    )
                }
                Spacer(Modifier.height(20.dp))

                Text(
                    "PLAYLIST",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                    letterSpacing = androidx.compose.ui.unit.TextUnit(1f, androidx.compose.ui.unit.TextUnitType.Sp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Liked Songs",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "${songs.size} song${if (songs.size != 1) "s" else ""} • Your liked songs",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(20.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { if (songs.isNotEmpty()) AppState.playTrack(songs.first(), songs) },
                        modifier = Modifier.height(36.dp),
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 20.dp)
                    ) {
                        Icon(Icons.Outlined.PlayArrow, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Play", style = MaterialTheme.typography.labelLarge)
                    }
                    OutlinedButton(
                        onClick = { if (songs.isNotEmpty()) { val s = songs.shuffled(); AppState.playTrack(s.first(), s) } },
                        modifier = Modifier.height(36.dp),
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 14.dp)
                    ) {
                        Icon(Icons.Default.Shuffle, null, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // ── Right panel ──────────────────────────────────────────────
            if (songs.isEmpty()) {
                EmptyState(
                    icon = Icons.Filled.Favorite,
                    message = "No liked songs",
                    hint = "Songs you like will appear here.\nTap the heart icon on any song to add it to your liked songs.",
                    colorScheme = colorScheme,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item {
                        Spacer(Modifier.height(52.dp)) // align with top of art
                        PlaylistTableHeader(colorScheme)
                    }
                    itemsIndexed(songs, key = { _, song -> song.id }) { idx, song ->
                        YTListItem(song, colorScheme) {
                            AppState.playTrack(song, songs)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LikedPlaylistNarrowLayout(songs: List<SongItem>, colorScheme: ColorScheme) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp)) {
        item {
            IconButton(onClick = { AppState.selectedLocalPlaylist = null }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = colorScheme.onSurface)
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth().padding(bottom = 24.dp), verticalAlignment = Alignment.Bottom) {
                // Playlist thumbnail
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .shadow(16.dp, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(colorScheme.error),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Favorite, null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.White
                    )
                }
                Spacer(Modifier.width(20.dp))
                Column(Modifier.weight(1f)) {
                    Text("Liked Songs", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(4.dp))
                    Text("${songs.size} song${if (songs.size != 1) "s" else ""}", style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { if (songs.isNotEmpty()) AppState.playTrack(songs.first(), songs) }, modifier = Modifier.height(36.dp), shape = CircleShape, contentPadding = PaddingValues(horizontal = 16.dp)) {
                            Icon(Icons.Outlined.PlayArrow, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Play", style = MaterialTheme.typography.labelLarge)
                        }
                        OutlinedButton(onClick = { if (songs.isNotEmpty()) { val s = songs.shuffled(); AppState.playTrack(s.first(), s) } }, modifier = Modifier.height(36.dp), shape = CircleShape, contentPadding = PaddingValues(horizontal = 12.dp)) {
                            Icon(Icons.Default.Shuffle, null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            if (songs.isNotEmpty()) {
                PlaylistTableHeader(colorScheme)
            }
        }

        if (songs.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Filled.Favorite,
                    message = "No liked songs",
                    hint = "Songs you like will appear here.\nTap the heart icon on any song to add it to your liked songs.",
                    colorScheme = colorScheme
                )
            }
        } else {
            itemsIndexed(songs, key = { _, song -> song.id }) { idx, song ->
                YTListItem(song, colorScheme) {
                    AppState.playTrack(song, songs)
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────── My Top 50 ──

@Composable
private fun TopPlaylistScreen(colorScheme: ColorScheme) {
    val stats = AppState.topSongs
    val songs = stats.map { stat ->
        SongItem(
            id = stat.songId,
            title = stat.title,
            artists = stat.artists.split(", ").map { name ->
                com.metrolist.shared.model.ArtistTiny(id = null, name = name)
            },
            thumbnail = stat.thumbnailUrl ?: "https://img.youtube.com/vi/${stat.songId}/maxresdefault.jpg",
            duration = null,
            album = null
        )
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth < 700.dp) {
            TopPlaylistNarrowLayout(stats, songs, colorScheme)
        } else {
            TopPlaylistWideLayout(stats, songs, colorScheme)
        }
    }
}

@Composable
private fun TopPlaylistWideLayout(stats: List<HistoryStat>, songs: List<SongItem>, colorScheme: ColorScheme) {
    Box(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .widthIn(max = 1360.dp)
                .fillMaxWidth()
                .fillMaxHeight()
                .align(Alignment.TopCenter)
                .padding(start = 48.dp, end = 48.dp, top = 28.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(56.dp)
        ) {
            // ── Left panel ───────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .width(220.dp)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
            ) {
                IconButton(onClick = { AppState.selectedLocalPlaylist = null }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = colorScheme.onSurface)
                }
                Spacer(Modifier.height(20.dp))

                // Playlist thumbnail
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .shadow(24.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Star, null,
                        modifier = Modifier.size(64.dp),
                        tint = colorScheme.onPrimaryContainer
                    )
                }
                Spacer(Modifier.height(20.dp))

                Text(
                    "PLAYLIST",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                    letterSpacing = androidx.compose.ui.unit.TextUnit(1f, androidx.compose.ui.unit.TextUnitType.Sp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "My Top 50",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "${songs.size} song${if (songs.size != 1) "s" else ""} • Based on your play history",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(20.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { if (songs.isNotEmpty()) AppState.playTrack(songs.first(), songs) },
                        modifier = Modifier.height(36.dp),
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 20.dp)
                    ) {
                        Icon(Icons.Outlined.PlayArrow, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Play", style = MaterialTheme.typography.labelLarge)
                    }
                    OutlinedButton(
                        onClick = { if (songs.isNotEmpty()) { val s = songs.shuffled(); AppState.playTrack(s.first(), s) } },
                        modifier = Modifier.height(36.dp),
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 14.dp)
                    ) {
                        Icon(Icons.Default.Shuffle, null, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // ── Right panel ──────────────────────────────────────────────
            if (songs.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.Star,
                    message = "No songs yet",
                    hint = "Play some music to build your top 50.",
                    colorScheme = colorScheme,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item {
                        Spacer(Modifier.height(52.dp)) // align with top of art
                        TopPlaylistTableHeader(colorScheme)
                    }
                    itemsIndexed(stats, key = { _, stat -> stat.songId }) { idx, stat ->
                        TopSongRow(rank = idx + 1, stat = stat, colorScheme = colorScheme, songs = songs)
                    }
                }
            }
        }
    }
}

@Composable
private fun TopPlaylistNarrowLayout(stats: List<HistoryStat>, songs: List<SongItem>, colorScheme: ColorScheme) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp)) {
        item {
            IconButton(onClick = { AppState.selectedLocalPlaylist = null }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = colorScheme.onSurface)
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth().padding(bottom = 24.dp), verticalAlignment = Alignment.Bottom) {
                // Playlist thumbnail
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .shadow(16.dp, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Star, null,
                        modifier = Modifier.size(48.dp),
                        tint = colorScheme.onPrimaryContainer
                    )
                }
                Spacer(Modifier.width(20.dp))
                Column(Modifier.weight(1f)) {
                    Text("My Top 50", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(4.dp))
                    Text("${songs.size} song${if (songs.size != 1) "s" else ""}", style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { if (songs.isNotEmpty()) AppState.playTrack(songs.first(), songs) }, modifier = Modifier.height(36.dp), shape = CircleShape, contentPadding = PaddingValues(horizontal = 16.dp)) {
                            Icon(Icons.Outlined.PlayArrow, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Play", style = MaterialTheme.typography.labelLarge)
                        }
                        OutlinedButton(onClick = { if (songs.isNotEmpty()) { val s = songs.shuffled(); AppState.playTrack(s.first(), s) } }, modifier = Modifier.height(36.dp), shape = CircleShape, contentPadding = PaddingValues(horizontal = 12.dp)) {
                            Icon(Icons.Default.Shuffle, null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            if (songs.isNotEmpty()) {
                TopPlaylistTableHeader(colorScheme)
            }
        }

        if (songs.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Outlined.Star,
                    message = "No songs yet",
                    hint = "Play some music to build your top 50.",
                    colorScheme = colorScheme
                )
            }
        } else {
            itemsIndexed(stats, key = { _, stat -> stat.songId }) { idx, stat ->
                TopSongRow(rank = idx + 1, stat = stat, colorScheme = colorScheme, songs = songs)
            }
        }
    }
}

// ───────────────────────────────────────────────────────────── Downloaded ──

@Composable
private fun DownloadedPlaylistScreen(colorScheme: ColorScheme) {
    val downloadedFiles = AppState.downloadedFiles
    val songs = downloadedFiles.map { it.toSongItem() }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth < 700.dp) {
            if (songs.isEmpty()) {
                EmptyPlaylistNarrowLayout(
                    title = "Downloaded",
                    subtitle = "Songs saved for offline listening",
                    icon = Icons.Outlined.Download,
                    iconTint = colorScheme.onSecondaryContainer,
                    iconBackground = colorScheme.secondaryContainer,
                    emptyMessage = "No downloaded songs",
                    emptyHint = "Download support is now available!\nClick the download button on any song to save it for offline playback.",
                    colorScheme = colorScheme
                )
            } else {
                DownloadedPlaylistNarrowLayout(songs, colorScheme)
            }
        } else {
            if (songs.isEmpty()) {
                EmptyPlaylistWideLayout(
                    title = "Downloaded",
                    subtitle = "Songs saved for offline listening",
                    icon = Icons.Outlined.Download,
                    iconTint = colorScheme.onSecondaryContainer,
                    iconBackground = colorScheme.secondaryContainer,
                    emptyMessage = "No downloaded songs",
                    emptyHint = "Download support is now available!\nClick the download button on any song to save it for offline playback.",
                    colorScheme = colorScheme
                )
            } else {
                DownloadedPlaylistWideLayout(songs, colorScheme)
            }
        }
    }
}

@Composable
private fun DownloadedPlaylistWideLayout(songs: List<SongItem>, colorScheme: ColorScheme) {
    Box(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .widthIn(max = 1360.dp)
                .fillMaxWidth()
                .fillMaxHeight()
                .align(Alignment.TopCenter)
                .padding(start = 48.dp, end = 48.dp, top = 28.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(56.dp)
        ) {
            // ── Left panel ───────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .width(220.dp)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
            ) {
                IconButton(onClick = { AppState.selectedLocalPlaylist = null }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = colorScheme.onSurface)
                }
                Spacer(Modifier.height(20.dp))

                // Playlist thumbnail
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .shadow(24.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Download, null,
                        modifier = Modifier.size(64.dp),
                        tint = colorScheme.onSecondaryContainer
                    )
                }
                Spacer(Modifier.height(20.dp))

                Text(
                    "PLAYLIST",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                    letterSpacing = androidx.compose.ui.unit.TextUnit(1f, androidx.compose.ui.unit.TextUnitType.Sp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Downloaded Songs",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "${songs.size} song${if (songs.size != 1) "s" else ""} • Available offline",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(20.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { if (songs.isNotEmpty()) AppState.playTrack(songs.first(), songs) },
                        modifier = Modifier.height(36.dp),
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 20.dp)
                    ) {
                        Icon(Icons.Outlined.PlayArrow, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Play", style = MaterialTheme.typography.labelLarge)
                    }
                    OutlinedButton(
                        onClick = { if (songs.isNotEmpty()) { val s = songs.shuffled(); AppState.playTrack(s.first(), s) } },
                        modifier = Modifier.height(36.dp),
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 14.dp)
                    ) {
                        Icon(Icons.Default.Shuffle, null, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // ── Right panel ──────────────────────────────────────────────
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    Spacer(Modifier.height(52.dp))
                    PlaylistTableHeader(colorScheme)
                }
                itemsIndexed(songs, key = { _, song -> song.id }) { idx, song ->
                    DownloadedSongRow(idx + 1, song, colorScheme)
                }
            }
        }
    }
}

@Composable
private fun DownloadedPlaylistNarrowLayout(songs: List<SongItem>, colorScheme: ColorScheme) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp)) {
        item {
            IconButton(onClick = { AppState.selectedLocalPlaylist = null }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = colorScheme.onSurface)
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth().padding(bottom = 24.dp), verticalAlignment = Alignment.Bottom) {
                // Playlist thumbnail
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .shadow(16.dp, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Download, null,
                        modifier = Modifier.size(48.dp),
                        tint = colorScheme.onSecondaryContainer
                    )
                }
                Spacer(Modifier.width(20.dp))
                Column(Modifier.weight(1f)) {
                    Text("Downloaded Songs", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(4.dp))
                    Text("${songs.size} song${if (songs.size != 1) "s" else ""}", style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { if (songs.isNotEmpty()) AppState.playTrack(songs.first(), songs) }, modifier = Modifier.height(36.dp), shape = CircleShape, contentPadding = PaddingValues(horizontal = 16.dp)) {
                            Icon(Icons.Outlined.PlayArrow, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Play", style = MaterialTheme.typography.labelLarge)
                        }
                        OutlinedButton(onClick = { if (songs.isNotEmpty()) { val s = songs.shuffled(); AppState.playTrack(s.first(), s) } }, modifier = Modifier.height(36.dp), shape = CircleShape, contentPadding = PaddingValues(horizontal = 12.dp)) {
                            Icon(Icons.Default.Shuffle, null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
        if (songs.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Outlined.Download,
                    message = "No downloaded songs",
                    hint = "Download support is now available!\nClick the download button on any song to save it for offline playback.",
                    colorScheme = colorScheme
                )
            }
        } else {
            itemsIndexed(songs, key = { _, song -> song.id }) { idx, song ->
                DownloadedSongRow(idx + 1, song, colorScheme)
            }
        }
    }
}

@Composable
private fun DownloadedSongRow(rank: Int, song: SongItem, colorScheme: ColorScheme) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { AppState.playTrack(song, AppState.downloadedFiles.map { it.toSongItem() }) }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "$rank",
            style = MaterialTheme.typography.labelMedium,
            color = colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
            modifier = Modifier.width(28.dp),
            fontWeight = FontWeight.Bold
        )
        AsyncImage(
            url = song.thumbnail ?: "https://img.youtube.com/vi/${song.id}/maxresdefault.jpg",
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(6.dp))
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                song.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                song.artists.joinToString { it.name },
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(8.dp))
        Icon(
            Icons.Outlined.Download,
            contentDescription = "Downloaded",
            tint = colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
    }
}

// ────────────────────────────────────────────────────────────────── Cached ──

@Composable
private fun CachedPlaylistScreen(colorScheme: ColorScheme) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth < 700.dp) {
            EmptyPlaylistNarrowLayout(
                title = "Cached",
                subtitle = "Songs cached while streaming",
                icon = Icons.Outlined.OfflinePin,
                iconTint = colorScheme.onTertiaryContainer,
                iconBackground = colorScheme.tertiaryContainer,
                emptyMessage = "No cached songs",
                emptyHint = "Songs cached as a side effect of playback will appear here.\nCache browsing support is coming soon.",
                colorScheme = colorScheme
            )
        } else {
            EmptyPlaylistWideLayout(
                title = "Cached",
                subtitle = "Songs cached while streaming",
                icon = Icons.Outlined.OfflinePin,
                iconTint = colorScheme.onTertiaryContainer,
                iconBackground = colorScheme.tertiaryContainer,
                emptyMessage = "No cached songs",
                emptyHint = "Songs cached as a side effect of playback will appear here.\nCache browsing support is coming soon.",
                colorScheme = colorScheme
            )
        }
    }
}

// ────────────────────────────────────────────────────────────────── Uploaded ──

@Composable
private fun UploadedPlaylistScreen(colorScheme: ColorScheme) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth < 700.dp) {
            EmptyPlaylistNarrowLayout(
                title = "Uploaded",
                subtitle = "Songs you've uploaded to YouTube Music",
                icon = Icons.Outlined.CloudUpload,
                iconTint = colorScheme.onSurfaceVariant,
                iconBackground = colorScheme.surfaceVariant,
                emptyMessage = "No uploaded songs",
                emptyHint = "Songs you upload to YouTube Music will appear here.\nRequires YouTube Music Premium.",
                colorScheme = colorScheme
            )
        } else {
            EmptyPlaylistWideLayout(
                title = "Uploaded",
                subtitle = "Songs you've uploaded to YouTube Music",
                icon = Icons.Outlined.CloudUpload,
                iconTint = colorScheme.onSurfaceVariant,
                iconBackground = colorScheme.surfaceVariant,
                emptyMessage = "No uploaded songs",
                emptyHint = "Songs you upload to YouTube Music will appear here.\nRequires YouTube Music Premium.",
                colorScheme = colorScheme
            )
        }
    }
}

// ─────────────────────────────────────────────────────── Shared components ──

@Composable
private fun EmptyPlaylistWideLayout(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
    emptyMessage: String,
    emptyHint: String,
    colorScheme: ColorScheme
) {
    Box(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .widthIn(max = 1360.dp)
                .fillMaxWidth()
                .fillMaxHeight()
                .align(Alignment.TopCenter)
                .padding(start = 48.dp, end = 48.dp, top = 28.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(56.dp)
        ) {
            // ── Left panel ───────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .width(220.dp)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
            ) {
                IconButton(onClick = { AppState.selectedLocalPlaylist = null }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = colorScheme.onSurface)
                }
                Spacer(Modifier.height(20.dp))

                // Playlist thumbnail
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .shadow(24.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon, null,
                        modifier = Modifier.size(64.dp),
                        tint = iconTint
                    )
                }
                Spacer(Modifier.height(20.dp))

                Text(
                    "PLAYLIST",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                    letterSpacing = androidx.compose.ui.unit.TextUnit(1f, androidx.compose.ui.unit.TextUnitType.Sp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // ── Right panel ──────────────────────────────────────────────
            EmptyState(
                icon = icon,
                message = emptyMessage,
                hint = emptyHint,
                colorScheme = colorScheme,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
    }
}

@Composable
private fun EmptyPlaylistNarrowLayout(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
    emptyMessage: String,
    emptyHint: String,
    colorScheme: ColorScheme
) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp)) {
        item {
            IconButton(onClick = { AppState.selectedLocalPlaylist = null }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = colorScheme.onSurface)
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth().padding(bottom = 24.dp), verticalAlignment = Alignment.Bottom) {
                // Playlist thumbnail
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .shadow(16.dp, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon, null,
                        modifier = Modifier.size(48.dp),
                        tint = iconTint
                    )
                }
                Spacer(Modifier.width(20.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(4.dp))
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }

            EmptyState(
                icon = icon,
                message = emptyMessage,
                hint = emptyHint,
                colorScheme = colorScheme
            )
        }
    }
}

@Composable
private fun PlaylistTableHeader(colorScheme: ColorScheme) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "#",
            modifier = Modifier.width(28.dp),
            style = MaterialTheme.typography.labelSmall,
            color = colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
        )
        Text(
            "Title",
            modifier = Modifier.weight(1f).padding(start = 60.dp), // aligns with text after thumbnail
            style = MaterialTheme.typography.labelSmall,
            color = colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
        )
        Text(
            "Duration",
            style = MaterialTheme.typography.labelSmall,
            color = colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
            modifier = Modifier.padding(end = 40.dp)
        )
    }
    HorizontalDivider(color = colorScheme.onSurface.copy(alpha = 0.07f))
}

@Composable
private fun TopPlaylistTableHeader(colorScheme: ColorScheme) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "#",
            modifier = Modifier.width(28.dp),
            style = MaterialTheme.typography.labelSmall,
            color = colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
        )
        Text(
            "Title",
            modifier = Modifier.weight(1f).padding(start = 60.dp), // aligns with text after thumbnail
            style = MaterialTheme.typography.labelSmall,
            color = colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
        )
        Text(
            "Plays",
            style = MaterialTheme.typography.labelSmall,
            color = colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
            modifier = Modifier.padding(end = 40.dp)
        )
    }
    HorizontalDivider(color = colorScheme.onSurface.copy(alpha = 0.07f))
}

@Composable
private fun EmptyState(
    icon: ImageVector,
    message: String,
    hint: String,
    colorScheme: ColorScheme,
    modifier: Modifier = Modifier
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                icon, null,
                modifier = Modifier.size(52.dp),
                tint = colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
            )
            Spacer(Modifier.height(14.dp))
            Text(
                message,
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Text(
                hint,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TopSongRow(rank: Int, stat: HistoryStat, colorScheme: ColorScheme, songs: List<SongItem>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { AppState.playTrack(songs[rank - 1], songs) }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "$rank",
            style = MaterialTheme.typography.labelMedium,
            color = colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
            modifier = Modifier.width(28.dp),
            fontWeight = FontWeight.Bold
        )
        AsyncImage(
            url = stat.thumbnailUrl ?: "https://img.youtube.com/vi/${stat.songId}/maxresdefault.jpg",
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(6.dp))
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                stat.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                stat.artists,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            "${stat.playCount}",
            style = MaterialTheme.typography.labelSmall,
            color = colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
            modifier = Modifier.padding(end = 32.dp)
        )
    }
}