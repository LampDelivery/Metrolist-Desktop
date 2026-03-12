package com.metrolist.desktop.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metrolist.desktop.state.AppState
import com.metrolist.desktop.ui.components.AsyncImage
import com.metrolist.desktop.ui.components.YTListItem
import com.metrolist.shared.model.*

@Composable
fun AlbumScreen(albumId: String, colorScheme: ColorScheme) {
    LaunchedEffect(albumId) {
        AppState.fetchAlbumData(albumId)
    }

    val albumPage = AppState.albumPageData
    val isLoading = AppState.isAlbumLoading

    if (isLoading || albumPage == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = colorScheme.primary)
        }
        return
    }

    val album = albumPage.album
    val songs = albumPage.songs

    Column(Modifier.fillMaxSize()) {
        // Back Button
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { AppState.selectedAlbumId = null }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = colorScheme.onSurface)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier
                            .size(240.dp)
                            .shadow(24.dp, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        AsyncImage(
                            url = album.thumbnail ?: "",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    Text(
                        album.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        album.artists.joinToString { it.name },
                        style = MaterialTheme.typography.titleMedium,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (album.year != null) {
                            Text(
                                album.year!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Text(" • ", color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        }
                        Text(
                            "${songs.size} songs",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { 
                                if (songs.isNotEmpty()) {
                                    AppState.playTrack(songs[0])
                                }
                            },
                            modifier = Modifier.height(48.dp),
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 24.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Play")
                        }

                        OutlinedIconButton(
                            onClick = { },
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape
                        ) {
                            Icon(Icons.Default.MoreVert, null)
                        }
                    }
                }
            }

            itemsIndexed(songs) { index, song ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { AppState.playTrack(song) }
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        (index + 1).toString(),
                        modifier = Modifier.width(32.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                    
                    Column(Modifier.weight(1f)) {
                        Text(
                            song.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            song.artists.joinToString { it.name },
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    if (song.duration != null) {
                        Text(
                            formatDuration(song.duration!!),
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "%d:%02d".format(mins, secs)
}
