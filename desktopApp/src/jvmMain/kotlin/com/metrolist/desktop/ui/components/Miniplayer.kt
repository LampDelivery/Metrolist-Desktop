package com.metrolist.desktop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.metrolist.desktop.state.AppState
import com.metrolist.desktop.constants.SliderStyle

@Composable
fun MiniplayerWindow() {
    if (!AppState.showMiniplayer) return

    var offsetX by remember { mutableStateOf(100f) }
    var offsetY by remember { mutableStateOf(100f) }
    var width by remember { mutableStateOf(340.dp) }
    var height by remember { mutableStateOf(120.dp) }

    val colorScheme = if (AppState.miniplayerNightMode) darkColorScheme() else MaterialTheme.colorScheme

    Popup(alignment = Alignment.TopStart, offset = androidx.compose.ui.unit.IntOffset(offsetX.toInt(), offsetY.toInt())) {
        Surface(
            modifier = Modifier
                .size(width, height)
                .clip(RoundedCornerShape(24.dp))
                .background(colorScheme.surfaceContainer.copy(alpha = 0.98f)),
            shape = RoundedCornerShape(24.dp),
            color = colorScheme.surfaceContainer.copy(alpha = 0.98f),
            tonalElevation = 8.dp,
            shadowElevation = 16.dp,
            border = BorderStroke(1.dp, colorScheme.outline)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Album art
                    AsyncImage(url = AppState.currentTrack?.thumbnail ?: "", modifier = Modifier.size(64.dp).clip(RoundedCornerShape(16.dp)))
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(AppState.currentTrack?.title ?: "Not Playing", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, maxLines = 1)
                        val artists = AppState.currentTrack?.artists ?: emptyList()
                        val album = AppState.currentTrack?.album
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            artists.forEachIndexed { idx, artist ->
                                Text(artist.name, style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant, maxLines = 1)
                                if (idx < artists.lastIndex) Text(" / ", style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant)
                            }
                            if (album != null && album.name.isNotBlank()) {
                                Text(" • ", style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant)
                                Text(album.name, style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant, maxLines = 1)
                            }
                        }
                    }
                    IconButton(onClick = { AppState.togglePlay() }) {
                        Icon(if (AppState.isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow, null, tint = colorScheme.primary)
                    }
                }
                Spacer(Modifier.height(8.dp))
                // Playbar
                when (AppState.sliderStyleState) {
                    SliderStyle.SQUIGGLY -> SquigglySlider(progress = AppState.progress, onSeek = { AppState.seekTo(it) }, color = colorScheme.primary)
                    SliderStyle.THIN -> WavySlider(progress = AppState.progress, onSeek = { AppState.seekTo(it) }, color = colorScheme.primary)
                    else -> PlayerSlider(progress = AppState.progress, onSeek = { AppState.seekTo(it) }, color = colorScheme.primary)
                }
            }
        }
    }
}

