package com.metrolist.desktop.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.metrolist.desktop.constants.SliderStyle
import com.metrolist.desktop.ui.components.ChipsRow

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
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text("Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp))
                }

                // User Interface Section
                SettingsGroup(title = "User Interface", colorScheme = colorScheme) {
                    SettingsNavigationItem(
                        title = "Appearance",
                        subtitle = "Theme, colors, and visual preferences",
                        icon = Icons.Outlined.Palette,
                        colorScheme = colorScheme,
                        onClick = { AppState.showAppearanceSettings = true }
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Player & Content Section
                SettingsGroup(title = "Player & Content", colorScheme = colorScheme) {
                    SettingsNavigationItem(
                        title = "Player & Audio",
                        subtitle = "Playback settings and audio preferences",
                        icon = Icons.Outlined.PlayArrow,
                        colorScheme = colorScheme,
                        onClick = { AppState.showPlayerSettings = true }
                    )
                    SettingsNavigationItem(
                        title = "Content & Filters",
                        subtitle = "Content filtering and display options",
                        icon = Icons.Outlined.Language,
                        colorScheme = colorScheme,
                        onClick = { AppState.showContentSettings = true }
                    )
                    SettingsNavigationItem(
                        title = "AI & Translation",
                        subtitle = "AI-powered lyrics translation settings",
                        icon = Icons.Outlined.Translate,
                        colorScheme = colorScheme,
                        onClick = { AppState.showAiSettings = true }
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Privacy & Security Section
                SettingsGroup(title = "Privacy & Security", colorScheme = colorScheme) {
                    SettingsNavigationItem(
                        title = "Privacy",
                        subtitle = "History and data privacy controls",
                        icon = Icons.Outlined.Security,
                        colorScheme = colorScheme,
                        onClick = { AppState.showPrivacySettings = true }
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Storage & Data Section
                SettingsGroup(title = "Storage & Data", colorScheme = colorScheme) {
                    SettingsNavigationItem(
                        title = "Storage",
                        subtitle = "Cache management and storage settings",
                        icon = Icons.Outlined.Storage,
                        colorScheme = colorScheme,
                        onClick = { AppState.showStorageSettings = true }
                    )
                }

                Spacer(Modifier.height(8.dp))

                // System & About Section
                SettingsGroup(title = "System & About", colorScheme = colorScheme) {
                    SettingsNavigationItem(
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

@Composable
fun SettingsGroup(title: String, colorScheme: ColorScheme, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = colorScheme.primary, modifier = Modifier.padding(bottom = 12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(16.dp)
        ) {
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
                    imageVector = if (checked) Icons.Filled.Check else Icons.Filled.Close,
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

// Navigation item component for main settings screen
@Composable
private fun SettingsNavigationItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    colorScheme: ColorScheme,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        color = androidx.compose.ui.graphics.Color.Transparent,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon container to match Android app style
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = colorScheme.secondaryContainer
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.onSurface
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
            }
            Text(
                "→",
                style = MaterialTheme.typography.headlineSmall,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}
