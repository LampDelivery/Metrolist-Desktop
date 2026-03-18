package com.metrolist.desktop.ui.screens.settings

// Import settings components
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.outlined.Crop
import androidx.compose.material.icons.outlined.FastForward
import androidx.compose.material.icons.outlined.HideImage
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.Lyrics
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.metrolist.desktop.state.AppState
import com.metrolist.desktop.ui.components.EnumDialog

@Composable
fun PlayerSettingsScreen(colorScheme: ColorScheme) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp).verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { AppState.showPlayerSettings = false }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                "Player",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        SettingsGroup(title = "Audio Quality", colorScheme = colorScheme) {
            var showQualityDialog by remember { mutableStateOf(false) }
            SettingsNavigationWithIcon(
                title = "Audio quality",
                subtitle = when (AppState.audioQuality) {
                    "HIGH" -> "High quality"
                    "LOW" -> "Low quality"
                    else -> "Auto"
                },
                icon = Icons.Outlined.HighQuality,
                colorScheme = colorScheme,
                onClick = { showQualityDialog = true }
            )

            if (showQualityDialog) {
                EnumDialog(
                    onDismiss = { showQualityDialog = false },
                    onSelect = {
                        AppState.updateAudioQuality(it)
                        showQualityDialog = false
                    },
                    title = "Audio Quality",
                    current = AppState.audioQuality,
                    values = listOf("AUTO", "HIGH", "LOW"),
                    valueText = { quality ->
                        when (quality) {
                            "HIGH" -> "High quality"
                            "LOW" -> "Low quality"
                            else -> "Auto"
                        }
                    },
                    colorScheme = colorScheme
                )
            }

            SettingsToggleWithIcon(
                title = "Audio offload",
                subtitle = "Reduces CPU usage during audio playback",
                icon = Icons.Outlined.Memory,
                checked = AppState.audioOffload,
                onCheckedChange = { AppState.toggleAudioOffload(it) },
                colorScheme = colorScheme
            )
        }

        Spacer(Modifier.height(16.dp))

        SettingsGroup(title = "Playback Behavior", colorScheme = colorScheme) {
            SettingsToggleWithIcon(
                title = "Persistent queue",
                subtitle = "Remembers queue between app sessions",
                icon = Icons.AutoMirrored.Outlined.QueueMusic,
                checked = AppState.persistentQueue,
                onCheckedChange = { AppState.togglePersistentQueue(it) },
                colorScheme = colorScheme
            )

            SettingsToggleWithIcon(
                title = "Persistent shuffle across queues",
                subtitle = "Maintains shuffle state when switching between queues",
                icon = Icons.Outlined.Shuffle,
                checked = AppState.persistentShuffleAcrossQueues,
                onCheckedChange = { AppState.togglePersistentShuffleAcrossQueues(it) },
                colorScheme = colorScheme
            )

            SettingsToggleWithIcon(
                title = "Remember shuffle and repeat",
                subtitle = "Remembers shuffle and repeat mode between sessions",
                icon = Icons.Outlined.Repeat,
                checked = AppState.rememberShuffleAndRepeat,
                onCheckedChange = { AppState.toggleRememberShuffleAndRepeat(it) },
                colorScheme = colorScheme
            )
        }

        Spacer(Modifier.height(16.dp))

        SettingsGroup(title = "Player Interface", colorScheme = colorScheme) {
            SettingsToggleWithIcon(
                title = "New player design",
                subtitle = "Enhanced player interface with improved layout",
                icon = Icons.Outlined.NewReleases,
                checked = AppState.newPlayerDesign,
                onCheckedChange = { AppState.toggleNewPlayerDesign(it) },
                colorScheme = colorScheme
            )

            SettingsToggleWithIcon(
                title = "Hide player thumbnail",
                subtitle = "Hides album artwork thumbnail in player",
                icon = Icons.Outlined.HideImage,
                checked = AppState.hidePlayerThumbnail,
                onCheckedChange = { AppState.toggleHidePlayerThumbnail(it) },
                colorScheme = colorScheme
            )

            SettingsToggleWithIcon(
                title = "Crop album art",
                subtitle = "Crops album artwork to fit player dimensions",
                icon = Icons.Outlined.Crop,
                checked = AppState.cropAlbumArt,
                onCheckedChange = { AppState.toggleCropAlbumArt(it) },
                colorScheme = colorScheme
            )

            SettingsToggleWithIcon(
                title = "Show lyrics",
                subtitle = "Displays lyrics in the player when available",
                icon = Icons.Outlined.Lyrics,
                checked = AppState.showLyrics,
                onCheckedChange = { AppState.toggleShowLyrics(it) },
                colorScheme = colorScheme
            )
        }

        Spacer(Modifier.height(16.dp))

        SettingsGroup(title = "Advanced", colorScheme = colorScheme) {
            SettingsToggleWithIcon(
                title = "Seek extra seconds",
                subtitle = "Adds extra seconds when seeking forward/backward",
                icon = Icons.Outlined.FastForward,
                checked = AppState.seekExtraSeconds,
                onCheckedChange = { AppState.toggleSeekExtraSeconds(it) },
                colorScheme = colorScheme
            )

            SettingsToggleWithIcon(
                title = "Pause on mute",
                subtitle = "Automatically pauses playback when volume is muted",
                icon = Icons.Outlined.VolumeOff,
                checked = AppState.pauseOnMute,
                onCheckedChange = { AppState.togglePauseOnMute(it) },
                colorScheme = colorScheme
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}