package com.metrolist.desktop.ui.screens.settings

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
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.metrolist.desktop.state.AppState

@Composable
fun IntegrationsSettingsScreen(colorScheme: ColorScheme) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp).verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { AppState.showIntegrationsSettings = false }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                "Integrations",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        SettingsGroup(title = "Discord Rich Presence", colorScheme = colorScheme) {
            SettingsToggleWithIcon(
                title = "Enable Discord RPC",
                subtitle = "Shows what you're listening to in Discord",
                icon = Icons.Outlined.Forum,
                checked = AppState.discordRpcEnabled,
                onCheckedChange = { AppState.toggleDiscordRpc(it) },
                colorScheme = colorScheme
            )

            if (AppState.discordRpcEnabled) {
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    SettingsToggle(
                        title = "Show buttons",
                        subtitle = "Displays buttons in Discord status (visible to others)",
                        checked = AppState.discordRpcShowButtons,
                        onCheckedChange = { AppState.toggleDiscordRpcShowButtons(it) },
                        colorScheme = colorScheme
                    )

                    if (AppState.discordRpcShowButtons) {
                        Spacer(Modifier.height(8.dp))

                        SettingsToggle(
                            title = "Show button 1",
                            subtitle = "\"Listen on YouTube Music\" button",
                            checked = AppState.discordRpcButton1Visible,
                            onCheckedChange = { AppState.toggleDiscordRpcButton1Visible(it) },
                            colorScheme = colorScheme
                        )

                        SettingsToggle(
                            title = "Show button 2",
                            subtitle = "\"View on Metrolist Desktop\" button",
                            checked = AppState.discordRpcButton2Visible,
                            onCheckedChange = { AppState.toggleDiscordRpcButton2Visible(it) },
                            colorScheme = colorScheme
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        SettingsGroup(title = "Last.fm Scrobbling", colorScheme = colorScheme) {
            SettingsNavigationWithIcon(
                title = "Last.fm settings",
                subtitle = if (AppState.lastFmUsername.isNotBlank()) "Signed in as ${AppState.lastFmUsername}" else "Not connected",
                icon = Icons.Outlined.MusicNote,
                colorScheme = colorScheme,
                onClick = { AppState.showLastFmSettings = true }
            )
        }

        Spacer(Modifier.height(16.dp))

        SettingsGroup(title = "Updates", colorScheme = colorScheme) {
            SettingsToggleWithIcon(
                title = "Check for updates",
                subtitle = "Automatically checks for new app versions",
                icon = Icons.Outlined.Update,
                checked = AppState.checkForUpdates,
                onCheckedChange = { AppState.toggleCheckForUpdates(it) },
                colorScheme = colorScheme
            )

            if (AppState.availableUpdate != null) {
                SettingsNavigationWithIcon(
                    title = "Update available",
                    subtitle = "Version ${AppState.availableUpdate} is available",
                    icon = Icons.Outlined.NewReleases,
                    colorScheme = colorScheme,
                    onClick = { AppState.openUpdatePage() }
                )
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}
