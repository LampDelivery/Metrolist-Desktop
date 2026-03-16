package com.metrolist.desktop.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.metrolist.desktop.state.AppState
import com.metrolist.desktop.state.ThemeMode
import com.metrolist.desktop.constants.SliderStyle
import com.metrolist.desktop.ui.components.ChipsRow
import com.metrolist.shared.model.LyricsProvider
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun SettingsToggleWithIcon(
    title: String,
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    colorScheme: ColorScheme
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
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
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
            }
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
fun SettingsNavigationWithIcon(
    title: String,
    subtitle: String? = null,
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
                if (subtitle != null) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                "→",
                style = MaterialTheme.typography.headlineSmall,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

// Sub-screen composables continuation
@Composable
fun AppearanceSettingsScreen(colorScheme: ColorScheme) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp).verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { AppState.showAppearanceSettings = false }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                "Appearance",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        SettingsGroup(title = "Display", colorScheme = colorScheme) {
            SettingsToggleWithIcon(
                title = "Enable high refresh rate",
                subtitle = "Higher refresh rates provide smoother visual experience",
                icon = Icons.Outlined.Speed,
                checked = AppState.enableHighRefreshRate,
                onCheckedChange = { AppState.toggleHighRefreshRate(it) },
                colorScheme = colorScheme
            )

            // Only show dynamic theme when using default color
            val isUsingDefaultColor = AppState.selectedThemeColor == com.metrolist.desktop.constants.DefaultThemeColor.value.toLong()
            if (isUsingDefaultColor) {
                SettingsToggleWithIcon(
                    title = "Enable dynamic theme",
                    subtitle = "Uses colors from album artwork and system accent color",
                    icon = Icons.Outlined.Palette,
                    checked = AppState.dynamicTheme,
                    onCheckedChange = { AppState.toggleDynamicTheme(it) },
                    colorScheme = colorScheme
                )
            }

            SettingsNavigationWithIcon(
                title = "Theme",
                subtitle = "Color palette and theme customization",
                icon = Icons.Outlined.Palette,
                colorScheme = colorScheme,
                onClick = { AppState.showThemeSettings = true }
            )
        }

        Spacer(Modifier.height(16.dp))

        SettingsGroup(title = "Mini-player", colorScheme = colorScheme) {
            SettingsToggleWithIcon(
                title = "New mini player design",
                subtitle = "Enhanced mini-player with improved layout",
                icon = Icons.Outlined.ViewCompact,
                checked = AppState.newMiniPlayerDesign,
                onCheckedChange = { AppState.toggleNewMiniPlayerDesign(it) },
                colorScheme = colorScheme
            )

            SettingsToggleWithIcon(
                title = "Pure black mini-player",
                subtitle = "Dark theme mini-player uses pure black background",
                icon = Icons.Outlined.Contrast,
                checked = AppState.pureBlackMiniPlayer,
                onCheckedChange = { AppState.togglePureBlackMiniPlayer(it) },
                colorScheme = colorScheme
            )
        }

        Spacer(Modifier.height(16.dp))

        SettingsGroup(title = "Player", colorScheme = colorScheme) {
            SettingsToggleWithIcon(
                title = "New player design",
                subtitle = "Enhanced player interface with modern styling",
                icon = Icons.Outlined.Palette,
                checked = AppState.newPlayerDesign,
                onCheckedChange = { AppState.toggleNewPlayerDesign(it) },
                colorScheme = colorScheme
            )

            SettingsToggleWithIcon(
                title = "Hide player thumbnail",
                subtitle = "Hide album artwork in the player interface",
                icon = Icons.Outlined.HideImage,
                checked = AppState.hidePlayerThumbnail,
                onCheckedChange = { AppState.toggleHidePlayerThumbnail(it) },
                colorScheme = colorScheme
            )

            SettingsToggleWithIcon(
                title = "Crop album art",
                subtitle = "Crop album artwork to fit player interface",
                icon = Icons.Outlined.Crop,
                checked = AppState.cropAlbumArt,
                onCheckedChange = { AppState.toggleCropAlbumArt(it) },
                colorScheme = colorScheme
            )

            SettingsNavigationWithIcon(
                title = "Slider Style",
                subtitle = "Choose your preferred slider appearance",
                icon = Icons.Outlined.Tune,
                colorScheme = colorScheme,
                onClick = { AppState.showSliderStyleDialog = true }
            )

            SettingsToggleWithIcon(
                title = "Swap player buttons and song info",
                subtitle = "Places the player controls in the middle and song info on the left",
                icon = Icons.Outlined.SwapHoriz,
                checked = AppState.swapPlayerControls,
                onCheckedChange = { AppState.toggleSwapPlayerControls(it) },
                colorScheme = colorScheme
            )
        }

        // Slider style dialog
        if (AppState.showSliderStyleDialog) {
            SliderStyleDialog(
                colorScheme = colorScheme,
                onDismiss = { AppState.showSliderStyleDialog = false },
                onStyleSelected = { style ->
                    AppState.setSliderStyle(style)
                    AppState.showSliderStyleDialog = false
                }
            )
        }
    }
}

@Composable
fun ThemeSettingsScreen(colorScheme: ColorScheme) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp).verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { AppState.showThemeSettings = false }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                "Theme & Colors",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Theme preview mockup (like Android app)
        SettingsGroup(title = "Preview", colorScheme = colorScheme) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                ThemeMockup(
                    themeMode = AppState.themeMode,
                    selectedThemeColor = androidx.compose.ui.graphics.Color(AppState.selectedThemeColor.toULong()),
                    colorScheme = colorScheme
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        SettingsGroup(title = "Theme Mode", colorScheme = colorScheme) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ThemeModeButton(
                    mode = ThemeMode.AUTO,
                    isSelected = AppState.themeMode == ThemeMode.AUTO,
                    colorScheme = colorScheme,
                    onClick = { AppState.themeMode = ThemeMode.AUTO }
                )
                ThemeModeButton(
                    mode = ThemeMode.LIGHT,
                    isSelected = AppState.themeMode == ThemeMode.LIGHT,
                    colorScheme = colorScheme,
                    onClick = { AppState.themeMode = ThemeMode.LIGHT }
                )
                ThemeModeButton(
                    mode = ThemeMode.DARK,
                    isSelected = AppState.themeMode == ThemeMode.DARK,
                    colorScheme = colorScheme,
                    onClick = { AppState.themeMode = ThemeMode.DARK }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        SettingsGroup(title = "Color Palette", colorScheme = colorScheme) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Choose your preferred color scheme",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Color palette grid (like Android app)
                val colors = listOf(
                    "Dynamic" to androidx.compose.ui.graphics.Color.Transparent,
                    "Red" to androidx.compose.ui.graphics.Color(0xFFED5564),
                    "Blue" to androidx.compose.ui.graphics.Color(0xFF1E88E5),
                    "Green" to androidx.compose.ui.graphics.Color(0xFF43A047),
                    "Purple" to androidx.compose.ui.graphics.Color(0xFF8E24AA),
                    "Orange" to androidx.compose.ui.graphics.Color(0xFFFB8C00)
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(colors) { (name, color) ->
                        PaletteItem(
                            name = name,
                            color = color,
                            isSelected = if (name == "Dynamic") {
                                AppState.selectedThemeColor == com.metrolist.desktop.constants.DefaultThemeColor.value.toLong()
                            } else {
                                AppState.selectedThemeColor == color.value.toLong()
                            },
                            colorScheme = colorScheme,
                            onClick = {
                                if (name == "Dynamic") {
                                    AppState.setSelectedThemeColor(com.metrolist.desktop.constants.DefaultThemeColor.value)
                                    AppState.toggleDynamicTheme(true)
                                } else {
                                    AppState.setSelectedThemeColor(color.value)
                                    AppState.toggleDynamicTheme(false)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeMockup(
    themeMode: ThemeMode,
    selectedThemeColor: androidx.compose.ui.graphics.Color,
    colorScheme: ColorScheme
) {
    Card(
        modifier = Modifier
            .widthIn(max = 500.dp)
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Title bar — matches actual TopBar layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(colorScheme.surfaceContainerHigh)
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Metrolist",
                    style = MaterialTheme.typography.titleSmall,
                    color = colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = null,
                        tint = colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Box(Modifier.size(8.dp).background(colorScheme.error.copy(alpha = 0.8f), CircleShape))
                        Box(Modifier.size(8.dp).background(colorScheme.tertiary.copy(alpha = 0.8f), CircleShape))
                        Box(Modifier.size(8.dp).background(colorScheme.primary.copy(alpha = 0.8f), CircleShape))
                    }
                }
            }

            // App body — sidebar + content
            Row(modifier = Modifier.weight(1f)) {
                // Navigation sidebar (NavigationRail style)
                Column(
                    modifier = Modifier
                        .width(60.dp)
                        .fillMaxHeight()
                        .background(colorScheme.surfaceContainerLow)
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val navIcons = listOf(
                        Icons.Outlined.Home,
                        Icons.Outlined.LibraryMusic,
                        Icons.Outlined.History,
                        Icons.Outlined.BarChart
                    )
                    navIcons.forEachIndexed { index, icon ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .background(
                                    if (index == 0) colorScheme.secondaryContainer
                                    else colorScheme.surfaceContainerLow,
                                    RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = if (index == 0) colorScheme.onSecondaryContainer
                                       else colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Main content area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(colorScheme.surface)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Page heading
                    Box(
                        Modifier.width(72.dp).height(7.dp)
                            .background(colorScheme.onSurface.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                    )
                    Spacer(Modifier.height(2.dp))
                    // Song rows
                    val titleWidths = listOf(80.dp, 68.dp, 90.dp, 60.dp)
                    val subtitleWidths = listOf(50.dp, 44.dp, 56.dp, 38.dp)
                    repeat(4) { index ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(30.dp)
                                .background(
                                    if (index == 0) colorScheme.primaryContainer.copy(alpha = 0.45f)
                                    else colorScheme.surface,
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                Modifier.size(18.dp).background(
                                    if (index == 0) colorScheme.primary.copy(alpha = 0.6f)
                                    else colorScheme.surfaceVariant,
                                    RoundedCornerShape(3.dp)
                                )
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Box(
                                    Modifier.width(titleWidths[index]).height(4.dp).background(
                                        if (index == 0) colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        else colorScheme.onSurface.copy(alpha = 0.65f),
                                        RoundedCornerShape(2.dp)
                                    )
                                )
                                Box(
                                    Modifier.width(subtitleWidths[index]).height(3.dp)
                                        .background(colorScheme.onSurfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
                                )
                            }
                        }
                    }
                }
            }

            // Bottom player bar — album art + track info on left, prev/play/next on right
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(colorScheme.surfaceContainerHigh)
            ) {
                // Progress bar
                Box(
                    Modifier.fillMaxWidth().height(2.dp)
                        .background(colorScheme.surfaceContainerHighest)
                ) {
                    Box(Modifier.fillMaxHeight().fillMaxWidth(0.38f).background(colorScheme.primary))
                }
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            Modifier.size(26.dp)
                                .background(colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Box(Modifier.width(52.dp).height(4.dp).background(colorScheme.onSurface.copy(alpha = 0.8f), RoundedCornerShape(2.dp)))
                            Box(Modifier.width(36.dp).height(3.dp).background(colorScheme.onSurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(2.dp)))
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Outlined.SkipPrevious, null, tint = colorScheme.onSurface, modifier = Modifier.size(16.dp))
                        Surface(
                            shape = CircleShape,
                            color = colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.PlayArrow, null, tint = colorScheme.onPrimary, modifier = Modifier.size(14.dp))
                            }
                        }
                        Icon(Icons.Outlined.SkipNext, null, tint = colorScheme.onSurface, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeModeButton(
    mode: ThemeMode,
    isSelected: Boolean,
    colorScheme: ColorScheme,
    onClick: () -> Unit
) {
    val borderWidth by animateFloatAsState(
        targetValue = if (isSelected) 3f else 1f,
        animationSpec = tween(200)
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = tween(200)
    )

    val backgroundColor = when (mode) {
        ThemeMode.LIGHT -> androidx.compose.ui.graphics.Color.White
        ThemeMode.DARK -> androidx.compose.ui.graphics.Color.Black
        ThemeMode.AUTO -> colorScheme.surface
    }

    val borderColor = if (isSelected) colorScheme.primary else colorScheme.outline

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(CircleShape)
                .background(backgroundColor)
                .border(borderWidth.dp, borderColor, CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            when (mode) {
                ThemeMode.AUTO -> {
                    Icon(
                        Icons.Outlined.Sync,
                        contentDescription = "Auto",
                        tint = colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
                ThemeMode.LIGHT -> {
                    Icon(
                        Icons.Outlined.LightMode,
                        contentDescription = "Light",
                        tint = androidx.compose.ui.graphics.Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
                ThemeMode.DARK -> {
                    Icon(
                        Icons.Outlined.DarkMode,
                        contentDescription = "Dark",
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Text(
            text = when (mode) {
                ThemeMode.AUTO -> "Auto"
                ThemeMode.LIGHT -> "Light"
                ThemeMode.DARK -> "Dark"
            },
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PaletteItem(
    name: String,
    color: androidx.compose.ui.graphics.Color,
    isSelected: Boolean,
    colorScheme: ColorScheme,
    onClick: () -> Unit
) {
    val cornerRadius by animateFloatAsState(
        targetValue = if (isSelected) 12f else 24f,
        animationSpec = tween(200)
    )

    val borderWidth by animateFloatAsState(
        targetValue = if (isSelected) 3f else 1f,
        animationSpec = tween(200)
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1f,
        animationSpec = tween(200)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(RoundedCornerShape(cornerRadius.dp))
                .background(if (name == "Dynamic") colorScheme.primary else color)
                .border(
                    borderWidth.dp,
                    if (isSelected) colorScheme.primary else colorScheme.outline,
                    RoundedCornerShape(cornerRadius.dp)
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            if (name == "Dynamic") {
                Icon(
                    Icons.Outlined.AutoAwesome,
                    contentDescription = "Dynamic",
                    tint = colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

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
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                "Player & Audio",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        SettingsGroup(title = "Playback", colorScheme = colorScheme) {
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
        }

        Spacer(Modifier.height(8.dp))

        SettingsGroup(title = "Interface", colorScheme = colorScheme) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    "Default tab on launch",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                ChipsRow(
                    chips = listOf("home" to "Home", "library" to "Library", "history" to "History", "stats" to "Stats"),
                    currentValue = AppState.defaultOpenTab,
                    onValueUpdate = { AppState.updateDefaultOpenTab(it) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

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
    }
}

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
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                "Content & Filters",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

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
    }
}

@Composable
fun PrivacySettingsScreen(colorScheme: ColorScheme) {
    var showClearListenConfirm by remember { mutableStateOf(false) }
    var showClearSearchConfirm by remember { mutableStateOf(false) }

    // Confirmation dialogs
    if (showClearListenConfirm) {
        AlertDialog(
            onDismissRequest = { showClearListenConfirm = false },
            title = { Text("Clear listen history?") },
            text = { Text("This will permanently delete all locally stored play history. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { AppState.clearListenHistory(); showClearListenConfirm = false }) {
                    Text("Clear", color = colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearListenConfirm = false }) { Text("Cancel") }
            }
        )
    }

    if (showClearSearchConfirm) {
        AlertDialog(
            onDismissRequest = { showClearSearchConfirm = false },
            title = { Text("Clear search history?") },
            text = { Text("This will remove all saved search queries. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { AppState.clearSearchHistory(); showClearSearchConfirm = false }) {
                    Text("Clear", color = colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearSearchConfirm = false }) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp).verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { AppState.showPrivacySettings = false }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                "Privacy",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        SettingsGroup(title = "History & Data", colorScheme = colorScheme) {
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
                onClick = { showClearListenConfirm = true }
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
                onClick = { showClearSearchConfirm = true }
            )
        }
    }
}

@Composable
fun AiSettingsScreen(colorScheme: ColorScheme) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp).verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { AppState.showAiSettings = false }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                "AI & Translation",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        SettingsGroup(title = "AI Translation", colorScheme = colorScheme) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "AI-powered lyrics translation",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    "Coming soon - AI translation features will be available in a future update",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun StorageSettingsScreen(colorScheme: ColorScheme) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp).verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { AppState.showStorageSettings = false }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                "Storage",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        SettingsGroup(title = "Cache Management", colorScheme = colorScheme) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Cache and storage management",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    "Coming soon - storage management features will be available in a future update",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorScheme.onSurface
                )
            }
        }
    }
}

private fun lyricsProviderLabel(provider: LyricsProvider) = when (provider) {
    LyricsProvider.AUTO -> "Auto"
    LyricsProvider.BETTERLYRICS -> "BetterLyrics"
    LyricsProvider.SIMPMUSIC -> "SimpMusic"
    LyricsProvider.LRCLIB -> "LrcLib"
    LyricsProvider.LYRICSPLUS -> "LyricsPlus"
    LyricsProvider.YOUTUBE -> "YouTube"
}

@Composable
fun LyricsProviderList(colorScheme: ColorScheme) {
    val listState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(listState) { from, to ->
        AppState.updateLyricsProviderOrder(
            AppState.lyricsProviderOrder.toMutableList().apply { add(to.index, removeAt(from.index)) }
        )
    }

    LazyColumn(
        state = listState,
        userScrollEnabled = false,
        modifier = Modifier
            .fillMaxWidth()
            .height((AppState.lyricsProviderOrder.size * 56).dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(AppState.lyricsProviderOrder, key = { it.name }) { provider ->
            ReorderableItem(reorderableState, key = provider.name) { isDragging ->
                val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp, tween(200))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = elevation,
                    color = if (isDragging) colorScheme.surfaceContainerHigh else colorScheme.surfaceContainerLow
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.DragIndicator,
                            contentDescription = "Drag to reorder",
                            tint = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp).draggableHandle()
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            lyricsProviderLabel(provider),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = provider in AppState.lyricsEnabledProviders,
                            onCheckedChange = { AppState.toggleLyricsProvider(provider, it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SliderStyleDialog(
    colorScheme: ColorScheme,
    onDismiss: () -> Unit,
    onStyleSelected: (SliderStyle) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Slider Style", style = MaterialTheme.typography.headlineSmall)
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // First row: Default and Wavy
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SliderStyleOption(
                        style = SliderStyle.DEFAULT,
                        isSelected = AppState.sliderStyleState == SliderStyle.DEFAULT,
                        colorScheme = colorScheme,
                        modifier = Modifier.weight(1f),
                        onClick = { onStyleSelected(SliderStyle.DEFAULT) }
                    )
                    SliderStyleOption(
                        style = SliderStyle.WAVY,
                        isSelected = AppState.sliderStyleState == SliderStyle.WAVY,
                        colorScheme = colorScheme,
                        modifier = Modifier.weight(1f),
                        onClick = { onStyleSelected(SliderStyle.WAVY) }
                    )
                }

                // Second row: Slim and Extended (if available)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SliderStyleOption(
                        style = SliderStyle.SLIM,
                        isSelected = AppState.sliderStyleState == SliderStyle.SLIM,
                        colorScheme = colorScheme,
                        modifier = Modifier.weight(1f),
                        onClick = { onStyleSelected(SliderStyle.SLIM) }
                    )
                    if (SliderStyle.entries.contains(SliderStyle.SQUIGGLY)) {
                        SliderStyleOption(
                            style = SliderStyle.SQUIGGLY,
                            isSelected = AppState.sliderStyleState == SliderStyle.SQUIGGLY,
                            colorScheme = colorScheme,
                            modifier = Modifier.weight(1f),
                            onClick = { onStyleSelected(SliderStyle.SQUIGGLY) }
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
fun SliderStyleOption(
    style: SliderStyle,
    isSelected: Boolean,
    colorScheme: ColorScheme,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) colorScheme.primary else colorScheme.outlineVariant

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        // Slider preview based on style
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when (style) {
                SliderStyle.DEFAULT -> {
                    // Standard Material3 slider
                    Slider(
                        value = 0.6f,
                        onValueChange = { /* preview only */ },
                        enabled = false,
                        colors = SliderDefaults.colors(
                            thumbColor = colorScheme.primary,
                            activeTrackColor = colorScheme.primary,
                            inactiveTrackColor = colorScheme.outlineVariant
                        )
                    )
                }
                SliderStyle.WAVY -> {
                    // Wavy slider preview - simplified representation
                    Canvas(modifier = Modifier.fillMaxWidth().height(4.dp)) {
                        val path = androidx.compose.ui.graphics.Path().apply {
                            val width = size.width
                            val height = size.height
                            moveTo(0f, height / 2)
                            // Create wavy pattern
                            for (i in 0..10) {
                                val x = width * i / 10f
                                val y = height / 2 + kotlin.math.sin(i * 0.8) * height / 4
                                lineTo(x, y.toFloat())
                            }
                        }
                        drawPath(
                            path,
                            color = colorScheme.primary,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
                        )
                    }
                }
                SliderStyle.SLIM -> {
                    // Slim slider preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(colorScheme.outlineVariant, RoundedCornerShape(1.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(2.dp)
                                .background(colorScheme.primary, RoundedCornerShape(1.dp))
                        )
                    }
                }
                SliderStyle.SQUIGGLY -> {
                    // Extended slider preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(colorScheme.outlineVariant, RoundedCornerShape(4.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(8.dp)
                                .background(colorScheme.primary, RoundedCornerShape(4.dp))
                        )
                    }
                }
            }
        }

        Text(
            text = style.name.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}