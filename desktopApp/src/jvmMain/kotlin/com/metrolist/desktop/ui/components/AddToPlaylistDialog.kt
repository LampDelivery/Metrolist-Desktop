package com.metrolist.desktop.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.metrolist.desktop.state.AppState

@Composable
fun AddToPlaylistDialog() {
    val song = AppState.showAddToPlaylistSong ?: return

    // Always fetch playlists when dialog opens
    LaunchedEffect(AppState.showAddToPlaylistSong) {
        if (AppState.showAddToPlaylistSong != null) {
            AppState.fetchUserPlaylists()
        }
    }

    var addResult by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { AppState.showAddToPlaylistSong = null; addResult = null },
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
                addResult != null -> {
                    Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                        Text(addResult!!, color = MaterialTheme.colorScheme.primary)
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
                                    addResult = "Added to \"${playlist.title}\"!"
                                    // Optionally update playlist state here
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = { AppState.showAddToPlaylistSong = null; addResult = null }) {
                Text("Cancel")
            }
        }
    )
}
