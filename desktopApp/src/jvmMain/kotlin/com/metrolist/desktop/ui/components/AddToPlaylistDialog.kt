package com.metrolist.desktop.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.metrolist.desktop.state.AppState

@Composable
fun AddToPlaylistDialog() {
    val song = AppState.showAddToPlaylistSong ?: return

    AlertDialog(
        onDismissRequest = { AppState.showAddToPlaylistSong = null },
        title = { Text("Add to Playlist") },
        text = {
            when {
                !AppState.isSignedIn -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Outlined.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text("Sign in to add to playlists", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(onClick = {
                            AppState.showAddToPlaylistSong = null
                            AppState.showSignIn = true
                        }) {
                            Text("Sign in")
                        }
                    }
                }
                AppState.userPlaylists.isEmpty() -> {
                    Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                        items(AppState.userPlaylists) { playlist ->
                            ListItem(
                                headlineContent = { Text(playlist.title) },
                                supportingContent = playlist.author?.let { { Text(it) } },
                                leadingContent = {
                                    if (playlist.thumbnail != null) {
                                        AsyncImage(
                                            url = playlist.thumbnail ?: "",
                                            modifier = Modifier.size(40.dp)
                                        )
                                    }
                                },
                                modifier = Modifier.clickable {
                                    AppState.addToPlaylist(playlist.id, song)
                                    AppState.showAddToPlaylistSong = null
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = { AppState.showAddToPlaylistSong = null }) {
                Text("Cancel")
            }
        }
    )
}
