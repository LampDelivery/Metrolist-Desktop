package com.metrolist.desktop.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.metrolist.desktop.state.AppState
import com.metrolist.desktop.ui.screens.settings.*

@Composable
fun SettingsScreen(colorScheme: ColorScheme) {
    // Show sub-screens based on state
    when {
        AppState.showThemeSettings -> ThemeSettingsScreen(colorScheme)
        AppState.showAppearanceSettings -> AppearanceSettingsScreen(colorScheme)
        AppState.showPlayerSettings -> PlayerSettingsScreen(colorScheme)
        AppState.showContentSettings -> ContentSettingsScreen(colorScheme)
        AppState.showAiSettings -> AiSettingsScreen(colorScheme)
        AppState.showPrivacySettings -> PrivacySettingsScreen(colorScheme)
        AppState.showStorageSettings -> StorageSettingsScreen(colorScheme)
        else -> {
            // Main settings screen
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp).verticalScroll(scrollState)
            ) {
                // Back button and title row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { AppState.showSettings = false }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text("Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp))
                }

                // User Interface Section
                SettingsGroup(title = "User Interface", colorScheme = colorScheme) {
                    SettingsNavigationWithIcon(
                        title = "Appearance",
                        subtitle = "Theme, colors, and visual preferences",
                        icon = Icons.Outlined.Palette,
                        colorScheme = colorScheme,
                        onClick = { AppState.showAppearanceSettings = true }
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Player & Content Section
                SettingsGroup(title = "Player & Content", colorScheme = colorScheme) {
                    SettingsNavigationWithIcon(
                        title = "Player & Audio",
                        subtitle = "Playback settings and audio preferences",
                        icon = Icons.Outlined.PlayArrow,
                        colorScheme = colorScheme,
                        onClick = { AppState.showPlayerSettings = true }
                    )
                    SettingsNavigationWithIcon(
                        title = "Content & Filters",
                        subtitle = "Content filtering and display options",
                        icon = Icons.Outlined.Language,
                        colorScheme = colorScheme,
                        onClick = { AppState.showContentSettings = true }
                    )
                    SettingsNavigationWithIcon(
                        title = "AI & Translation",
                        subtitle = "AI-powered lyrics translation settings",
                        icon = Icons.Outlined.Translate,
                        colorScheme = colorScheme,
                        onClick = { AppState.showAiSettings = true }
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Privacy & Security Section
                SettingsGroup(title = "Privacy & Security", colorScheme = colorScheme) {
                    SettingsNavigationWithIcon(
                        title = "Privacy",
                        subtitle = "History and data privacy controls",
                        icon = Icons.Outlined.Security,
                        colorScheme = colorScheme,
                        onClick = { AppState.showPrivacySettings = true }
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Storage & Data Section
                SettingsGroup(title = "Storage & Data", colorScheme = colorScheme) {
                    SettingsNavigationWithIcon(
                        title = "Storage",
                        subtitle = "Cache management and storage settings",
                        icon = Icons.Outlined.Storage,
                        colorScheme = colorScheme,
                        onClick = { AppState.showStorageSettings = true }
                    )
                }

                Spacer(Modifier.height(16.dp))

                // System & About Section
                SettingsGroup(title = "System & About", colorScheme = colorScheme) {
                    SettingsNavigationWithIcon(
                        title = "About",
                        subtitle = "App information and updates",
                        icon = Icons.Outlined.Info,
                        colorScheme = colorScheme,
                        onClick = { AppState.showAboutSettings = true }
                    )
                }
            }
        }
    }
}
