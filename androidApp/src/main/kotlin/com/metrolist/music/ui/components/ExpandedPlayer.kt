package com.metrolist.music.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import coil3.compose.AsyncImage
import com.metrolist.music.playback.PlayerConnection

@OptIn(ExperimentalMaterial3Api::class)
@UnstableApi
@Composable
fun ExpandedPlayer(
    playerConnection: PlayerConnection?,
    onCollapse: () -> Unit = {}
) {
    if (playerConnection == null) {
        onCollapse()
        return
    }
    
    val currentSong by playerConnection.currentSong.collectAsState(initial = null)
    val isPlaying by playerConnection.isPlaying.collectAsState(initial = false)
    val progress by playerConnection.progress.collectAsState(initial = 0f)
    val duration by playerConnection.duration.collectAsState(initial = 0L)
    val currentPosition by playerConnection.currentPosition.collectAsState(initial = 0L)
    val repeatMode by playerConnection.repeatMode.collectAsState(initial = Player.REPEAT_MODE_OFF)
    val shuffleEnabled by playerConnection.shuffleModeEnabled.collectAsState(initial = false)

    if (currentSong == null) {
        onCollapse()
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Now Playing") },
                navigationIcon = {
                    IconButton(onClick = onCollapse) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Show options */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Album Art
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                AsyncImage(
                    model = currentSong?.thumbnail,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Song Info
            Text(
                text = currentSong?.title ?: "",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = currentSong?.artists?.joinToString(", ") { it.name } ?: "",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            if (currentSong?.album?.name != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currentSong?.album?.name ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Progress Slider
            var sliderPosition by remember { mutableFloatStateOf(0f) }
            var isDragging by remember { mutableFloatStateOf(0f) }

            Slider(
                value = if (isDragging > 0) sliderPosition else progress,
                onValueChange = { 
                    isDragging = 1f
                    sliderPosition = it 
                },
                onValueChangeFinished = {
                    playerConnection.seekTo((sliderPosition * duration).toLong())
                    isDragging = 0f
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Time indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatDuration(if (isDragging > 0) (sliderPosition * duration).toLong() else currentPosition),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatDuration(duration),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Playback Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle
                IconButton(
                    onClick = { playerConnection.toggleShuffle() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Text(
                        text = "SHUF",
                        color = if (shuffleEnabled) MaterialTheme.colorScheme.primary 
                              else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                // Previous
                IconButton(
                    onClick = { playerConnection.skipToPrevious() },
                    modifier = Modifier.size(56.dp)
                ) {
                    Text(
                        text = "|<<",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                // Play/Pause
                IconButton(
                    onClick = { playerConnection.togglePlayPause() },
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(
                        text = if (isPlaying) "PAUSE" else "PLAY",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                // Next
                IconButton(
                    onClick = { playerConnection.skipToNext() },
                    modifier = Modifier.size(56.dp)
                ) {
                    Text(
                        text = ">>|",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                // Repeat
                IconButton(
                    onClick = { playerConnection.cycleRepeatMode() },
                    modifier = Modifier.size(48.dp)
                ) {
                    val repeatText = when (repeatMode) {
                        Player.REPEAT_MODE_ONE -> "RP1"
                        Player.REPEAT_MODE_ALL -> "RPL"
                        else -> "RPO"
                    }
                    Text(
                        text = repeatText,
                        color = if (repeatMode != Player.REPEAT_MODE_OFF) MaterialTheme.colorScheme.primary
                              else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    if (durationMs <= 0) return "0:00"
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
