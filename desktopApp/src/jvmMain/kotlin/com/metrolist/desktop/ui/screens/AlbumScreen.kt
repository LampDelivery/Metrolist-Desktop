package com.metrolist.desktop.ui.screens

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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.metrolist.desktop.state.AppState
import com.metrolist.desktop.ui.components.AsyncImage
import com.metrolist.desktop.ui.components.NowPlayingBars
import com.metrolist.desktop.ui.components.SongContextMenu
import com.metrolist.shared.model.AlbumItem
import com.metrolist.shared.model.SongItem

@Composable
fun AlbumScreen(albumId: String, colorScheme: ColorScheme) {
    LaunchedEffect(albumId) { AppState.fetchAlbumData(albumId) }

    if (AppState.isAlbumLoading || AppState.albumPageData == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = colorScheme.primary)
        }
        return
    }

    val album = AppState.albumPageData!!.album
    val songs = AppState.albumPageData!!.songs

    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth < 700.dp) AlbumNarrowLayout(album, songs, colorScheme)
        else AlbumWideLayout(album, songs, colorScheme)
    }
}

@Composable
private fun AlbumWideLayout(album: AlbumItem, songs: List<SongItem>, colorScheme: ColorScheme) {
    // Centered, max-width container — no edge-to-edge stretching
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
                IconButton(onClick = { AppState.selectedAlbumId = null }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = colorScheme.onSurface)
                }
                Spacer(Modifier.height(20.dp))

                AsyncImage(
                    url = album.thumbnail ?: "",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .shadow(24.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(20.dp))

                Text(
                    "ALBUM",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                    letterSpacing = androidx.compose.ui.unit.TextUnit(1f, androidx.compose.ui.unit.TextUnitType.Sp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    album.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    album.artists.joinToString { it.name },
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    buildString {
                        if (album.year != null) { append(album.year!!); append("  •  ") }
                        append("${songs.size} song${if (songs.size != 1) "s" else ""}")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                )
                Spacer(Modifier.height(20.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { if (songs.isNotEmpty()) AppState.playTrack(songs[0], songs) },
                        modifier = Modifier.height(36.dp),
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 20.dp)
                    ) {
                        Icon(Icons.Outlined.PlayArrow, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Play", style = MaterialTheme.typography.labelLarge)
                    }
                    OutlinedButton(
                        onClick = { if (songs.isNotEmpty()) { val s = songs.shuffled(); AppState.playTrack(s[0], s) } },
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
                    Spacer(Modifier.height(52.dp)) // align top of list with top of art
                    AlbumTableHeader(colorScheme)
                }
                itemsIndexed(songs) { index, song ->
                    AlbumTrackRow(index, song, songs, colorScheme)
                }
            }
        }
    }
}

@Composable
private fun AlbumNarrowLayout(album: AlbumItem, songs: List<SongItem>, colorScheme: ColorScheme) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp)) {
        item {
            IconButton(onClick = { AppState.selectedAlbumId = null }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = colorScheme.onSurface)
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth().padding(bottom = 24.dp), verticalAlignment = Alignment.Bottom) {
                AsyncImage(
                    url = album.thumbnail ?: "",
                    modifier = Modifier.size(160.dp).shadow(16.dp, RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(Modifier.width(20.dp))
                Column(Modifier.weight(1f)) {
                    Text(album.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(4.dp))
                    Text(album.artists.joinToString { it.name }, style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { if (songs.isNotEmpty()) AppState.playTrack(songs[0], songs) }, modifier = Modifier.height(36.dp), shape = CircleShape, contentPadding = PaddingValues(horizontal = 16.dp)) {
                            Icon(Icons.Outlined.PlayArrow, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Play", style = MaterialTheme.typography.labelLarge)
                        }
                        OutlinedButton(onClick = { if (songs.isNotEmpty()) { val s = songs.shuffled(); AppState.playTrack(s[0], s) } }, modifier = Modifier.height(36.dp), shape = CircleShape, contentPadding = PaddingValues(horizontal = 12.dp)) {
                            Icon(Icons.Default.Shuffle, null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
            AlbumTableHeader(colorScheme)
        }
        itemsIndexed(songs) { index, song ->
            AlbumTrackRow(index, song, songs, colorScheme)
        }
    }
}

@Composable
private fun AlbumTableHeader(colorScheme: ColorScheme) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 40.dp, end = 44.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("#", modifier = Modifier.width(32.dp),
            style = MaterialTheme.typography.labelSmall,
            color = colorScheme.onSurfaceVariant.copy(alpha = 0.45f), textAlign = TextAlign.Center)
        Spacer(Modifier.width(12.dp))
        Text("Title", modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant.copy(alpha = 0.45f))
        Text("Duration",
            style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant.copy(alpha = 0.45f))
    }
    HorizontalDivider(color = colorScheme.onSurface.copy(alpha = 0.07f))
}

@Composable
private fun AlbumTrackRow(index: Int, song: SongItem, songs: List<SongItem>, colorScheme: ColorScheme) {
    var showContextMenu by remember { mutableStateOf(false) }
    val isCurrentTrack = AppState.currentTrack?.id == song.id

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(6.dp))
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type == PointerEventType.Press && event.buttons.isSecondaryPressed) {
                                showContextMenu = true
                            }
                        }
                    }
                }
                .clickable { AppState.playTrack(song, songs) }
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.width(32.dp), contentAlignment = Alignment.Center) {
                if (isCurrentTrack) NowPlayingBars(AppState.isPlaying, colorScheme.primary)
                else Text((index + 1).toString(), style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.4f), textAlign = TextAlign.Center)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(song.title, style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isCurrentTrack) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isCurrentTrack) colorScheme.primary else colorScheme.onSurface,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(song.artists.joinToString { it.name }, style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (song.duration != null) {
                Text(formatAlbumDuration(song.duration!!), style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.widthIn(min = 40.dp), textAlign = TextAlign.End)
            }
            IconButton(onClick = { showContextMenu = true }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.MoreVert, null, tint = colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
            }
        }
        SongContextMenu(song = song, expanded = showContextMenu) { showContextMenu = false }
    }
}

private fun formatAlbumDuration(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "%d:%02d".format(mins, secs)
}
