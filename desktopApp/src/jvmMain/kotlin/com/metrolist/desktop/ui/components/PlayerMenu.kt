/**
 * Metrolist Desktop Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.desktop.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Radio
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.metrolist.desktop.state.AppState

// ── Player Menu Panel ────────────────────────────────────────────────────

@Composable
fun PlayerMenuPanel(
    colorScheme: ColorScheme,
    onDismiss: () -> Unit,
    onShowAddToPlaylistDialog: () -> Unit,
    onShowDetailsDialog: () -> Unit,
    onShowSleepTimerDialog: () -> Unit
) {
    val currentTrack = AppState.currentTrack

    Popup(
        alignment = Alignment.BottomEnd,
        offset = IntOffset(-8, -8),
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Card(
            modifier = Modifier
                .width(320.dp)
                .heightIn(max = 500.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerHigh),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Quick Actions Grid (Android style)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    userScrollEnabled = false
                ) {
                    // Start Radio
                    item {
                        AndroidStyleActionButton(
                            icon = Icons.Outlined.Radio,
                            text = "Start Radio",
                            backgroundColor = colorScheme.surfaceVariant,
                            contentColor = colorScheme.onSurfaceVariant
                        ) {
                            currentTrack?.let { AppState.startRadio(it) }
                            onDismiss()
                        }
                    }

                    // Add to Playlist
                    item {
                        AndroidStyleActionButton(
                            icon = Icons.AutoMirrored.Outlined.PlaylistAdd,
                            text = "Add to playlist",
                            backgroundColor = colorScheme.primaryContainer,
                            contentColor = colorScheme.onPrimaryContainer
                        ) {
                            onShowAddToPlaylistDialog()
                            onDismiss()
                        }
                    }

                    // Copy Link
                    item {
                        AndroidStyleActionButton(
                            icon = Icons.Outlined.Link,
                            text = "Copy Link",
                            backgroundColor = colorScheme.secondaryContainer,
                            contentColor = colorScheme.onSecondaryContainer
                        ) {
                            currentTrack?.let { track ->
                                AppState.copyToClipboard("https://music.youtube.com/watch?v=${track.id}")
                            }
                            onDismiss()
                        }
                    }
                }

                // Navigation & Library Actions Card Group
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        currentTrack?.let { track ->
                            // View Artist
                            if (track.artists.isNotEmpty()) {
                                MenuActionRow(
                                    icon = Icons.Outlined.Person,
                                    title = "View Artist",
                                    subtitle = track.artists.joinToString { it.name },
                                    colorScheme = colorScheme
                                ) {
                                    if (track.artists.size == 1 && track.artists[0].id != null) {
                                        AppState.fetchArtistData(track.artists[0].id!!)
                                    }
                                    onDismiss()
                                }
                            }

                            // View Album
                            track.album?.let { album ->
                                MenuActionRow(
                                    icon = Icons.Outlined.Album,
                                    title = "View Album",
                                    subtitle = album.name,
                                    colorScheme = colorScheme
                                ) {
                                    AppState.fetchAlbumData(album.id ?: "")
                                    onDismiss()
                                }
                            }

                            // Add/Remove from Library (Like)
                            val isLiked = AppState.likedSongIds.contains(track.id)
                            MenuActionRow(
                                icon = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                title = if (isLiked) "Remove from Library" else "Add to Library",
                                subtitle = if (isLiked) "Remove from your liked songs" else "Add to your liked songs",
                                colorScheme = colorScheme
                            ) {
                                AppState.toggleLike(track)
                                onDismiss()
                            }
                        }
                    }
                }

                // Advanced Options Card Group
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Song Details - Now properly functional like Android
                        MenuActionRow(
                            icon = Icons.Outlined.Info,
                            title = "Details",
                            subtitle = "View the song's information",
                            colorScheme = colorScheme
                        ) {
                            onShowDetailsDialog()
                        }

                        // Sleep Timer
                        val sleepActive = AppState.sleepTimerEnabled
                        MenuActionRow(
                            icon = Icons.Outlined.Bedtime,
                            title = if (sleepActive) {
                                when {
                                    AppState.sleepTimerStopAfterCurrentSong -> "Stopping after current song"
                                    else -> "Sleep: ${formatSleepTime(AppState.sleepTimerTimeLeftMs)}"
                                }
                            } else "Sleep Timer",
                            subtitle = if (sleepActive) "Tap to modify or cancel" else "Stop music after a set time",
                            colorScheme = colorScheme
                        ) {
                            if (sleepActive) {
                                AppState.clearSleepTimer()
                            } else {
                                onShowSleepTimerDialog()
                            }
                        }

                        // Download (Future Implementation)
                        MenuActionRow(
                            icon = Icons.Outlined.Download,
                            title = "Download",
                            subtitle = "Save for offline listening",
                            colorScheme = colorScheme,
                            enabled = false
                        ) {
                            // TODO: Implement download functionality
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AndroidStyleActionButton(
    icon: ImageVector,
    text: String,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = text,
                style = if (text.length > 12) MaterialTheme.typography.labelSmall
                       else MaterialTheme.typography.labelMedium,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun MenuActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    colorScheme: ColorScheme,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val contentAlpha = if (enabled) 1f else 0.6f
    val contentColor by animateColorAsState(
        targetValue = colorScheme.onSurface.copy(alpha = contentAlpha),
        label = "contentColor"
    )
    val subtitleColor by animateColorAsState(
        targetValue = colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
        label = "subtitleColor"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) colorScheme.primary else colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(24.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = subtitleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun formatSleepTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return if (m > 0) "${m}m ${s}s" else "${s}s"
}