package com.metrolist.desktop.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.metrolist.desktop.state.AppState
import com.metrolist.desktop.constants.SliderStyle
import com.metrolist.shared.model.LyricsProvider

@Composable
fun SettingsScreen(colorScheme: ColorScheme) {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp).verticalScroll(rememberScrollState())) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(24.dp))

        SettingsGroup(title = "Appearance", colorScheme = colorScheme) {
            SettingsToggle(
                title = "Night Mode",
                subtitle = "Uses absolute black for backgrounds",
                checked = AppState.pureBlack,
                onCheckedChange = { AppState.togglePureBlack(it) },
                colorScheme = colorScheme
            )

            SettingsToggle(
                title = "Auto Night Mode",
                subtitle = "Enables night mode automatically when album art is dark",
                checked = AppState.autoNightMode,
                onCheckedChange = { AppState.toggleAutoNightMode(it) },
                colorScheme = colorScheme
            )

            SettingsToggle(
                title = "Miniplayer Night Mode",
                subtitle = "Uses night mode for the miniplayer",
                checked = AppState.miniplayerNightMode,
                onCheckedChange = { AppState.toggleMiniplayerNightMode(it) },
                colorScheme = colorScheme
            )

            SettingsToggle(
                title = "Animated Gradient",
                subtitle = "Apple Music-style flowing background in expanded mode",
                checked = AppState.animatedGradient,
                onCheckedChange = { AppState.toggleAnimatedGradient(it) },
                colorScheme = colorScheme
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Text("Slider Style", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SliderStyle.entries.forEach { style ->
                        FilterChip(
                            selected = AppState.sliderStyleState == style,
                            onClick = { AppState.setSliderStyle(style) },
                            label = { Text(style.name.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
            }

            SettingsToggle(
                title = "Swap player buttons and song info",
                subtitle = "Places the player controls in the middle and song info on the left",
                checked = AppState.swapPlayerControls,
                onCheckedChange = { AppState.toggleSwapPlayerControls(it) },
                colorScheme = colorScheme
            )
        }

        Spacer(Modifier.height(8.dp))

        SettingsGroup(title = "Player", colorScheme = colorScheme) {
            SettingsToggle(
                title = "Crossfade",
                subtitle = "Smoothly fade between tracks at the end of each song",
                checked = AppState.crossfadeEnabled,
                onCheckedChange = { AppState.toggleCrossfade(it) },
                colorScheme = colorScheme
            )

            if (AppState.crossfadeEnabled) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Crossfade duration", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text("${AppState.crossfadeDuration.toInt()} s", style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant)
                    }
                    Slider(
                        value = AppState.crossfadeDuration,
                        onValueChange = { AppState.updateCrossfadeDuration(it) },
                        valueRange = 1f..15f,
                        steps = 13,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text("Default tab on launch", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("home" to "Home", "library" to "Library", "history" to "History", "stats" to "Stats").forEach { (id, label) ->
                        FilterChip(
                            selected = AppState.defaultOpenTab == id,
                            onClick = { AppState.updateDefaultOpenTab(id) },
                            label = { Text(label) }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        SettingsGroup(title = "Content", colorScheme = colorScheme) {
            SettingsToggle(
                title = "Hide explicit content",
                subtitle = "Filters songs marked as explicit from search and recommendations",
                checked = AppState.hideExplicit,
                onCheckedChange = { AppState.toggleHideExplicit(it) },
                colorScheme = colorScheme
            )

            SettingsToggle(
                title = "Hide video songs",
                subtitle = "Hides songs that are music videos rather than audio tracks",
                checked = AppState.hideVideoSongs,
                onCheckedChange = { AppState.toggleHideVideoSongs(it) },
                colorScheme = colorScheme
            )
        }

        Spacer(Modifier.height(8.dp))

        SettingsGroup(title = "Lyrics", colorScheme = colorScheme) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Provider", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LyricsProvider.entries.forEach { provider ->
                        FilterChip(
                            selected = AppState.lyricsProviderPref == provider,
                            onClick = { AppState.setLyricsProvider(provider) },
                            label = {
                                Text(
                                    when (provider) {
                                        LyricsProvider.AUTO -> "Auto"
                                        LyricsProvider.LRCLIB -> "LrcLib"
                                        LyricsProvider.LYRICSPLUS -> "LyricsPlus"
                                        LyricsProvider.YOUTUBE -> "YouTube"
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        SettingsGroup(title = "Privacy", colorScheme = colorScheme) {
            SettingsToggle(
                title = "Pause listen history",
                subtitle = "Stops recording played songs to your local history",
                checked = AppState.pauseListenHistory,
                onCheckedChange = { AppState.togglePauseListenHistory(it) },
                colorScheme = colorScheme
            )

            SettingsActionRow(
                title = "Clear listen history",
                subtitle = "Permanently deletes all locally stored play history",
                colorScheme = colorScheme,
                onClick = { AppState.clearListenHistory() }
            )

            SettingsToggle(
                title = "Pause search history",
                subtitle = "Stops saving your search queries",
                checked = AppState.pauseSearchHistory,
                onCheckedChange = { AppState.togglePauseSearchHistory(it) },
                colorScheme = colorScheme
            )

            SettingsActionRow(
                title = "Clear search history",
                subtitle = "Removes all saved search queries",
                colorScheme = colorScheme,
                onClick = { AppState.clearSearchHistory() }
            )
        }
    }
}

@Composable
fun SettingsGroup(title: String, colorScheme: ColorScheme, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = colorScheme.primary, modifier = Modifier.padding(bottom = 12.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant.copy(alpha = 0.3f)), shape = RoundedCornerShape(16.dp)) {
            Column(content = content)
        }
    }
}

@Composable
fun SettingsToggle(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, colorScheme: ColorScheme) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            thumbContent = {
                Icon(
                    imageVector = if (checked) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                )
            }
        )
    }
}

@Composable
fun SettingsActionRow(title: String, subtitle: String, colorScheme: ColorScheme, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = colorScheme.error)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant)
        }
    }
}
