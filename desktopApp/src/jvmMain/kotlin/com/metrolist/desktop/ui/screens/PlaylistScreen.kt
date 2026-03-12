package com.metrolist.desktop.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.metrolist.shared.model.*
import com.metrolist.desktop.state.AppState
import com.metrolist.desktop.ui.components.AsyncImage
import com.metrolist.desktop.ui.components.YTListItem

@Composable
fun PlaylistScreen(playlistId: String, colorScheme: ColorScheme) {
    if (AppState.isPlaylistLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = colorScheme.primary)
        }
    } else {
        val playlistInfo = AppState.playlistData["header"]?.firstOrNull() as? PlaylistItem
        val songs = AppState.playlistData.values.flatten().filterIsInstance<SongItem>()
        
        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp), contentPadding = PaddingValues(vertical = 32.dp)) {
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
                    AsyncImage(
                        url = playlistInfo?.thumbnail ?: "",
                        modifier = Modifier.size(240.dp).shadow(12.dp, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp)
                    )
                    
                    Spacer(Modifier.width(32.dp))
                    
                    Column(modifier = Modifier.align(Alignment.Bottom)) {
                        Text(
                            text = playlistInfo?.title ?: "Playlist",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Text(
                            text = "${playlistInfo?.author ?: "Unknown"} • ${songs.size} songs",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(Modifier.height(24.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { if (songs.isNotEmpty()) AppState.playTrack(songs.first()) },
                                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Play")
                            }
                            
                            OutlinedButton(
                                onClick = { /* Shuffle */ },
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                            ) {
                                Icon(Icons.Default.Shuffle, null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Shuffle")
                            }
                        }
                    }
                }
            }

            items(songs) { song ->
                YTListItem(song, colorScheme) { 
                    AppState.playTrack(song)
                }
            }
        }
    }
}
