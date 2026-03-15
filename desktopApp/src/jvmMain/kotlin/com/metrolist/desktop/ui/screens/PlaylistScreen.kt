package com.metrolist.desktop.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.metrolist.desktop.state.AppState
import com.metrolist.desktop.ui.components.AsyncImage
import com.metrolist.desktop.ui.components.YTListItem
import com.metrolist.shared.model.*

@Composable
fun PlaylistScreen(colorScheme: ColorScheme) {
    if (AppState.isPlaylistLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = colorScheme.primary)
        }
        return
    }

    val playlistInfo = AppState.playlistData["header"]?.firstOrNull() as? PlaylistItem
    val songs = AppState.playlistData.values.flatten().filterIsInstance<SongItem>()

    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth < 700.dp) PlaylistNarrowLayout(playlistInfo, songs, colorScheme)
        else PlaylistWideLayout(playlistInfo, songs, colorScheme)
    }
}

@Composable
private fun PlaylistWideLayout(playlistInfo: PlaylistItem?, songs: List<SongItem>, colorScheme: ColorScheme) {
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
                IconButton(onClick = { AppState.selectedPlaylistId = null }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = colorScheme.onSurface)
                }
                Spacer(Modifier.height(20.dp))

                AsyncImage(
                    url = playlistInfo?.thumbnail?.takeIf { it.isNotEmpty() } ?: AppState.pendingPlaylistThumbnail ?: "",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .shadow(24.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(20.dp))

                Text(
                    "PLAYLIST",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                    letterSpacing = androidx.compose.ui.unit.TextUnit(1f, androidx.compose.ui.unit.TextUnitType.Sp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    playlistInfo?.title ?: "Playlist",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    buildString {
                        val author = playlistInfo?.author
                        if (!author.isNullOrBlank()) append(author)
                        if (songs.isNotEmpty()) {
                            if (!author.isNullOrBlank()) append("  •  ")
                            append("${songs.size} song${if (songs.size != 1) "s" else ""}")
                        }
                    },
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
                    Spacer(Modifier.height(52.dp)) // align with top of art
                    PlaylistTableHeader(colorScheme)
                }
                items(songs) { song ->
                    YTListItem(song, colorScheme) { AppState.playTrack(song, songs) }
                }
            }
        }
    }
}

@Composable
private fun PlaylistNarrowLayout(playlistInfo: PlaylistItem?, songs: List<SongItem>, colorScheme: ColorScheme) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp)) {
        item {
            IconButton(onClick = { AppState.selectedPlaylistId = null }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = colorScheme.onSurface)
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth().padding(bottom = 24.dp), verticalAlignment = Alignment.Bottom) {
                AsyncImage(
                    url = playlistInfo?.thumbnail?.takeIf { it.isNotEmpty() } ?: AppState.pendingPlaylistThumbnail ?: "",
                    modifier = Modifier.size(160.dp).shadow(16.dp, RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(Modifier.width(20.dp))
                Column(Modifier.weight(1f)) {
                    Text(playlistInfo?.title ?: "Playlist", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(4.dp))
                    Text(playlistInfo?.author ?: "", style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
            PlaylistTableHeader(colorScheme)
        }
        items(songs) { song ->
            YTListItem(song, colorScheme) { AppState.playTrack(song, songs) }
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
            "Title",
            modifier = Modifier.weight(1f).padding(start = 56.dp), // aligns with text after thumbnail
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
