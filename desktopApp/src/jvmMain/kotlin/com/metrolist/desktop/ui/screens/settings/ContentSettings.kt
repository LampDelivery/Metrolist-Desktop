package com.metrolist.desktop.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.metrolist.desktop.state.AppState
import com.metrolist.desktop.state.LanguageCodeToName
import com.metrolist.desktop.state.CountryCodeToName
import com.metrolist.desktop.ui.components.EnumDialog
import com.metrolist.shared.model.ArtistSource

@Composable
fun ContentSettingsScreen(colorScheme: ColorScheme) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp).verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { AppState.showContentSettings = false }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                "Content & Filters",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        SettingsGroup(title = "Localization", colorScheme = colorScheme) {
            var showLanguageDialog by remember { mutableStateOf(false) }
            SettingsNavigationWithIcon(
                title = "Default content language",
                subtitle = LanguageCodeToName[AppState.contentLanguage] ?: AppState.contentLanguage,
                icon = Icons.Outlined.Language,
                colorScheme = colorScheme,
                onClick = { showLanguageDialog = true }
            )
            if (showLanguageDialog) {
                LanguageDialog(
                    onDismiss = { showLanguageDialog = false },
                    onSelect = {
                        AppState.updateContentLanguage(it)
                        showLanguageDialog = false
                    },
                    title = "Default Content Language",
                    current = AppState.contentLanguage,
                    colorScheme = colorScheme
                )
            }

            var showCountryDialog by remember { mutableStateOf(false) }
            SettingsNavigationWithIcon(
                title = "Default content country",
                subtitle = CountryCodeToName[AppState.contentCountry] ?: AppState.contentCountry,
                icon = Icons.Outlined.Public,
                colorScheme = colorScheme,
                onClick = { showCountryDialog = true }
            )
            if (showCountryDialog) {
                CountryDialog(
                    onDismiss = { showCountryDialog = false },
                    onSelect = {
                        AppState.updateContentCountry(it)
                        showCountryDialog = false
                    },
                    title = "Default Content Country",
                    current = AppState.contentCountry,
                    colorScheme = colorScheme
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        SettingsGroup(title = "Content Filtering", colorScheme = colorScheme) {
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

        Spacer(Modifier.height(16.dp))

        SettingsGroup(title = "Artist Page Display", colorScheme = colorScheme) {
            SettingsToggleWithIcon(
                title = "Show artist description",
                subtitle = "Displays artist biography and description on artist pages",
                icon = Icons.Outlined.Description,
                checked = AppState.showArtistDescription,
                onCheckedChange = { AppState.toggleShowArtistDescription(it) },
                colorScheme = colorScheme
            )

            SettingsToggleWithIcon(
                title = "Show subscriber count",
                subtitle = "Displays subscriber count with 'subscribers' text on artist pages",
                icon = Icons.Outlined.People,
                checked = AppState.showArtistSubscriberCount,
                onCheckedChange = { AppState.toggleShowArtistSubscriberCount(it) },
                colorScheme = colorScheme
            )

            SettingsToggleWithIcon(
                title = "Show monthly listeners",
                subtitle = "Displays monthly listener count on artist pages",
                icon = Icons.Outlined.PlayArrow,
                checked = AppState.showMonthlyListeners,
                onCheckedChange = { AppState.toggleShowMonthlyListeners(it) },
                colorScheme = colorScheme
            )
        }

        Spacer(Modifier.height(16.dp))

        SettingsGroup(title = "Artist Sources", colorScheme = colorScheme) {
            var showIconSourceDialog by remember { mutableStateOf(false) }
            SettingsNavigationWithIcon(
                title = "Artist icon source",
                subtitle = AppState.artistIconSource.name.lowercase().replaceFirstChar { it.uppercase() },
                icon = Icons.Outlined.AccountCircle,
                colorScheme = colorScheme,
                onClick = { showIconSourceDialog = true }
            )
            if (showIconSourceDialog) {
                EnumDialog(
                    onDismiss = { showIconSourceDialog = false },
                    onSelect = { AppState.updateArtistIconSource(it); showIconSourceDialog = false },
                    title = "Artist Icon Source",
                    current = AppState.artistIconSource,
                    values = ArtistSource.entries,
                    valueText = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
                    colorScheme = colorScheme
                )
            }

            var showBannerSourceDialog by remember { mutableStateOf(false) }
            SettingsNavigationWithIcon(
                title = "Artist banner source",
                subtitle = AppState.artistBannerSource.name.lowercase().replaceFirstChar { it.uppercase() },
                icon = Icons.Outlined.Image,
                colorScheme = colorScheme,
                onClick = { showBannerSourceDialog = true }
            )
            if (showBannerSourceDialog) {
                EnumDialog(
                    onDismiss = { showBannerSourceDialog = false },
                    onSelect = { AppState.updateArtistBannerSource(it); showBannerSourceDialog = false },
                    title = "Artist Banner Source",
                    current = AppState.artistBannerSource,
                    values = ArtistSource.entries,
                    valueText = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
                    colorScheme = colorScheme
                )
            }

            // Only show Last.fm auto-cycle options when Last.fm is selected as banner source
            if (AppState.artistBannerSource == ArtistSource.LASTFM) {
                SettingsToggleWithIcon(
                    title = "Auto-cycle Last.fm photos",
                    subtitle = "Automatically cycles through Last.fm artist photos every ${AppState.lastFmPhotoCycleInterval.toInt()} seconds",
                    icon = Icons.Outlined.Sync,
                    checked = AppState.lastFmPhotoAutoCycle,
                    onCheckedChange = { AppState.toggleLastFmAutoCycle() },
                    colorScheme = colorScheme
                )

                if (AppState.lastFmPhotoAutoCycle) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text(
                            "Auto-cycle interval",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("2s", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                            Slider(
                                value = AppState.lastFmPhotoCycleInterval,
                                onValueChange = { AppState.updateLastFmPhotoCycleInterval(it) },
                                valueRange = 2f..15f,
                                steps = 12, // 2,3,4,5,6,7,8,9,10,11,12,13,14,15
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = colorScheme.primary,
                                    activeTrackColor = colorScheme.primary,
                                    inactiveTrackColor = colorScheme.outline
                                )
                            )
                            Text("15s", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                        }
                        Text(
                            "${AppState.lastFmPhotoCycleInterval.toInt()} seconds",
                            style = MaterialTheme.typography.labelMedium,
                            color = colorScheme.primary,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                }
            }

            if (AppState.artistBannerSource == ArtistSource.LASTFM) {
                SettingsToggleWithIcon(
                    title = "Show artist photos",
                    subtitle = "Shows photo section in artist pages",
                    icon = Icons.Outlined.Photo,
                    checked = AppState.showArtistPhotos,
                    onCheckedChange = { AppState.toggleShowArtistPhotos(it) },
                    colorScheme = colorScheme
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        SettingsGroup(title = "Language & Proxy", colorScheme = colorScheme) {
            SettingsNavigationWithIcon(
                title = "App language",
                subtitle = "Coming Soon - UI language settings",
                icon = Icons.Outlined.Translate,
                colorScheme = colorScheme,
                onClick = { /* Coming Soon */ }
            )

            SettingsNavigationWithIcon(
                title = "Proxy settings",
                subtitle = "Coming Soon - Network proxy configuration",
                icon = Icons.Outlined.VpnKey,
                colorScheme = colorScheme,
                onClick = { /* Coming Soon */ }
            )
        }

        Spacer(Modifier.height(16.dp))

        SettingsGroup(title = "Lyrics", colorScheme = colorScheme) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Providers: drag to reorder, toggle to enable/disable",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LyricsProviderList(colorScheme)
            }
        }

        Spacer(Modifier.height(16.dp))

        SettingsGroup(title = "Home Page", colorScheme = colorScheme) {
            SettingsToggleWithIcon(
                title = "Randomize home order",
                subtitle = "Randomizes the order of content sections on the home screen",
                icon = Icons.Outlined.Shuffle,
                checked = AppState.randomizeHomeOrder,
                onCheckedChange = { AppState.toggleRandomizeHomeOrder(it) },
                colorScheme = colorScheme
            )

            var showTopListDialog by remember { mutableStateOf(false) }
            SettingsNavigationWithIcon(
                title = "Top list length",
                subtitle = "${AppState.topListLength.toInt()} items",
                icon = Icons.Outlined.FormatListNumbered,
                colorScheme = colorScheme,
                onClick = { showTopListDialog = true }
            )
            if (showTopListDialog) {
                TopListLengthDialog(
                    onDismiss = { showTopListDialog = false },
                    onSelect = {
                        AppState.updateTopListLength(it)
                        showTopListDialog = false
                    },
                    current = AppState.topListLength,
                    colorScheme = colorScheme
                )
            }

            var showQuickPicksDialog by remember { mutableStateOf(false) }
            SettingsNavigationWithIcon(
                title = "Quick picks",
                subtitle = AppState.quickPicksMode.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                icon = Icons.Outlined.Speed,
                colorScheme = colorScheme,
                onClick = { showQuickPicksDialog = true }
            )
            if (showQuickPicksDialog) {
                QuickPicksDialog(
                    onDismiss = { showQuickPicksDialog = false },
                    onSelect = {
                        AppState.updateQuickPicksMode(it)
                        showQuickPicksDialog = false
                    },
                    current = AppState.quickPicksMode,
                    colorScheme = colorScheme
                )
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}