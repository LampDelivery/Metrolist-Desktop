package com.metrolist.desktop.ui.screens.settings

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.Crop
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.HideImage
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.TableRows
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.ViewCompact
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.metrolist.desktop.constants.DefaultThemeColor
import com.metrolist.desktop.state.AppLayout
import com.metrolist.desktop.state.AppState
import com.metrolist.desktop.state.ThemeMode
import com.metrolist.desktop.ui.components.EnumDialog

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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            val isUsingDefaultColor = AppState.selectedThemeColor == DefaultThemeColor.value.toLong()
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

                SettingsToggleWithIcon(
 title = "Pure Black",
 subtitle = "Use pure black background in dark mode (Night Mode)",
 icon = Icons.Outlined.Contrast,
 checked = AppState.pureBlack,
 onCheckedChange = { AppState.togglePureBlack(it) },
 colorScheme = colorScheme
 )

 SettingsNavigationWithIcon(
 title = "Theme",
                subtitle = "Color palette and theme customization",
                icon = Icons.Outlined.Palette,
                colorScheme = colorScheme,
                onClick = { AppState.showThemeSettings = true }
            )
        }

        Spacer(Modifier.height(16.dp))

        // --- Cider-inspired UI customizations ---
        SettingsGroup(title = "Cider-Inspired UI", colorScheme = colorScheme) {
            // App layout selector
            SettingsNavigationWithIcon(
                title = "App Layout",
                subtitle = when (AppState.appLayout) {
                    AppLayout.CANVAS -> "Canvas - Clean foundation"
                    AppLayout.STUDIO -> "Studio - Expanded workspace"
                    AppLayout.STAGE -> "Stage - Performance focused"
                    AppLayout.FLOW -> "Flow - Streamlined experience"
                },
                icon = Icons.Outlined.ViewCompact,
                colorScheme = colorScheme,
                onClick = { AppState.showLayoutDialog = true }
            )

            // Full gradient background toggle
            SettingsToggleWithIcon(
                title = "Full Gradient Background",
                subtitle = "Gradient covers the entire app (not just player)",
                icon = Icons.Outlined.AutoAwesome,
                checked = AppState.fullGradientBackground,
                onCheckedChange = { AppState.toggleFullGradientBackground(it) },
                colorScheme = colorScheme
            )

            // Persistent side drawer toggle
            SettingsToggleWithIcon(
                title = "Persistent Side Drawer",
                subtitle = "Keep lyrics/album art/queue visible while browsing",
                icon = Icons.Outlined.TableRows,
                checked = AppState.persistentSideDrawer,
                onCheckedChange = { AppState.togglePersistentSideDrawer(it) },
                colorScheme = colorScheme
            )
        }

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

            var showPanelSideDialog by remember { mutableStateOf(false) }
            SettingsNavigationWithIcon(
                title = "Panel side",
                subtitle = AppState.playerPanelSide.replaceFirstChar { it.uppercase() },
                icon = Icons.Outlined.TableRows,
                colorScheme = colorScheme,
                onClick = { showPanelSideDialog = true }
            )

            if (showPanelSideDialog) {
                EnumDialog(
                    onDismiss = { showPanelSideDialog = false },
                    onSelect = {
                        AppState.updatePlayerPanelSide(it)
                        showPanelSideDialog = false
                    },
                    title = "Panel Side",
                    current = AppState.playerPanelSide,
                    values = listOf("right", "left"),
                    valueText = { it.replaceFirstChar { c -> c.uppercase() } },
                    valueDescription = { if (it == "right") "Position the panel on the right side" else "Position the panel on the left side" },
                    colorScheme = colorScheme
                )
            }

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

        // Layout dialog
        if (AppState.showLayoutDialog) {
            AppLayoutDialog(
                colorScheme = colorScheme,
                onDismiss = { AppState.showLayoutDialog = false },
                onLayoutSelected = { layout ->
                    AppState.updateAppLayout(layout)
                    AppState.showLayoutDialog = false
                }
            )
        }

        // Slider style dialog
        if (AppState.showSliderStyleDialog) {
            SliderStyleDialog(
                colorScheme = colorScheme,
                onDismiss = { AppState.showSliderStyleDialog = false },
                onStyleSelected = { style ->
                    AppState.updateSliderStyle(style)
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                contentAlignment = Alignment.CenterStart
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
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ThemeModeButton(
                    mode = ThemeMode.AUTO,
                    isSelected = AppState.themeMode == ThemeMode.AUTO,
                    colorScheme = colorScheme,
                    onClick = { AppState.updateThemeMode(ThemeMode.AUTO) }
                )
                ThemeModeButton(
                    mode = ThemeMode.LIGHT,
                    isSelected = AppState.themeMode == ThemeMode.LIGHT,
                    colorScheme = colorScheme,
                    onClick = { AppState.updateThemeMode(ThemeMode.LIGHT) }
                )
                ThemeModeButton(
                    mode = ThemeMode.DARK,
                    isSelected = AppState.themeMode == ThemeMode.DARK,
                    colorScheme = colorScheme,
                    onClick = { AppState.updateThemeMode(ThemeMode.DARK) }
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
                                AppState.selectedThemeColor == DefaultThemeColor.value.toLong()
                            } else {
                                AppState.selectedThemeColor == color.value.toLong()
                            },
                            colorScheme = colorScheme,
                            onClick = {
                                if (name == "Dynamic") {
                                    AppState.setSelectedThemeColor(DefaultThemeColor.value)
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
fun AppLayoutPreview(
    layout: AppLayout,
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

    val cornerRadius by animateFloatAsState(
        targetValue = if (isSelected) 12f else 16f,
        animationSpec = tween(200)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            modifier = Modifier
                .size(width = 140.dp, height = 100.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clickable { onClick() },
            shape = RoundedCornerShape(cornerRadius.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainer),
            border = BorderStroke(
                borderWidth.dp,
                if (isSelected) colorScheme.primary else colorScheme.outlineVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 2.dp)
        ) {
            when (layout) {
                AppLayout.CANVAS -> CanvasLayoutMockup(colorScheme)
                AppLayout.STUDIO -> StudioLayoutMockup(colorScheme)
                AppLayout.STAGE -> StageLayoutMockup(colorScheme)
                AppLayout.FLOW -> FlowLayoutMockup(colorScheme)
            }
        }

        Text(
            text = when (layout) {
                AppLayout.CANVAS -> "Canvas"
                AppLayout.STUDIO -> "Studio"
                AppLayout.STAGE -> "Stage"
                AppLayout.FLOW -> "Flow"
            },
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )

        Text(
            text = when (layout) {
                AppLayout.CANVAS -> "Clean foundation"
                AppLayout.STUDIO -> "Expanded workspace"
                AppLayout.STAGE -> "Performance focused"
                AppLayout.FLOW -> "Streamlined experience"
            },
            style = MaterialTheme.typography.labelSmall,
            color = colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 2
        )
    }
}

@Composable
fun CalicoLayoutMockup(colorScheme: ColorScheme) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Top player bar - Calico style
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .background(colorScheme.primary.copy(alpha = 0.1f))
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    Modifier.size(16.dp)
                        .background(colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Box(Modifier.width(24.dp).height(2.dp).background(colorScheme.onSurface.copy(alpha = 0.7f), RoundedCornerShape(1.dp)))
                    Box(Modifier.width(18.dp).height(2.dp).background(colorScheme.onSurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(1.dp)))
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(Icons.Outlined.SkipPrevious, null, tint = colorScheme.onSurface, modifier = Modifier.size(10.dp))
                Box(Modifier.size(14.dp).background(colorScheme.primary, CircleShape))
                Icon(Icons.Outlined.SkipNext, null, tint = colorScheme.onSurface, modifier = Modifier.size(10.dp))
            }
        }

        // Content area with sidebar
        Row(modifier = Modifier.weight(1f)) {
            // Left sidebar
            Column(
                modifier = Modifier
                    .width(24.dp)
                    .fillMaxHeight()
                    .background(colorScheme.surfaceContainerLow)
                    .padding(vertical = 4.dp, horizontal = 2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                if (index == 0) colorScheme.secondaryContainer
                                else colorScheme.surfaceContainerLow,
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
            }

            // Main content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(colorScheme.surface)
                    .padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Box(Modifier.width(28.dp).height(2.dp).background(colorScheme.onSurface.copy(alpha = 0.7f), RoundedCornerShape(1.dp)))
                repeat(3) { index ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .background(
                                if (index == 0) colorScheme.primaryContainer.copy(alpha = 0.3f)
                                else colorScheme.surface,
                                RoundedCornerShape(2.dp)
                            )
                            .padding(horizontal = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(
                            Modifier.size(6.dp).background(
                                if (index == 0) colorScheme.primary.copy(alpha = 0.6f)
                                else colorScheme.surfaceVariant,
                                RoundedCornerShape(1.dp)
                            )
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Box(Modifier.width(16.dp).height(1.5.dp).background(colorScheme.onSurface.copy(alpha = 0.6f), RoundedCornerShape(1.dp)))
                            Box(Modifier.width(12.dp).height(1.dp).background(colorScheme.onSurfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(1.dp)))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MavericksLayoutMockup(colorScheme: ColorScheme) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Top menu bar - iTunes style
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .background(colorScheme.surfaceContainerHigh)
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Metrolist",
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Box(Modifier.size(3.dp).background(colorScheme.error, CircleShape))
                Box(Modifier.size(3.dp).background(colorScheme.tertiary, CircleShape))
                Box(Modifier.size(3.dp).background(colorScheme.primary, CircleShape))
            }
        }

        // Toolbar with search and controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(colorScheme.surfaceContainerLow)
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(Icons.Outlined.SkipPrevious, null, tint = colorScheme.onSurface, modifier = Modifier.size(8.dp))
                Box(Modifier.size(10.dp).background(colorScheme.primary, CircleShape))
                Icon(Icons.Outlined.SkipNext, null, tint = colorScheme.onSurface, modifier = Modifier.size(8.dp))
            }
            Box(
                Modifier.width(32.dp).height(8.dp)
                    .background(colorScheme.surfaceContainerHighest, RoundedCornerShape(4.dp))
            )
        }

        // Main content with sidebar
        Row(modifier = Modifier.weight(1f)) {
            // Library sidebar
            Column(
                modifier = Modifier
                    .width(32.dp)
                    .fillMaxHeight()
                    .background(colorScheme.surfaceVariant)
                    .padding(2.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                repeat(6) { index ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(
                                if (index == 1) colorScheme.secondaryContainer
                                else colorScheme.surfaceVariant,
                                RoundedCornerShape(1.dp)
                            )
                            .padding(horizontal = 1.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.width(12.dp).height(1.5.dp).background(colorScheme.onSurfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(1.dp)))
                    }
                }
            }

            // Track listing
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(colorScheme.surface)
                    .padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                // Header row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(colorScheme.surfaceContainerLow)
                        .padding(horizontal = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.width(20.dp).height(1.5.dp).background(colorScheme.onSurfaceVariant, RoundedCornerShape(1.dp)))
                }

                repeat(4) { index ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .background(
                                if (index == 0) colorScheme.primaryContainer.copy(alpha = 0.3f)
                                else colorScheme.surface,
                                RoundedCornerShape(1.dp)
                            )
                            .padding(horizontal = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Box(Modifier.width(6.dp).height(2.dp).background(colorScheme.onSurface.copy(alpha = 0.6f), RoundedCornerShape(1.dp)))
                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Box(Modifier.width(14.dp).height(1.5.dp).background(colorScheme.onSurface.copy(alpha = 0.6f), RoundedCornerShape(1.dp)))
                            Box(Modifier.width(10.dp).height(1.dp).background(colorScheme.onSurfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(1.dp)))
                        }
                    }
                }
            }
        }

        // Bottom status bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(colorScheme.surfaceContainerHigh)
                .padding(horizontal = 6.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(Modifier.width(28.dp).height(1.5.dp).background(colorScheme.onSurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(1.dp)))
        }
    }
}

@Composable
fun CompactLayoutMockup(colorScheme: ColorScheme) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Compact header with integrated controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(colorScheme.surfaceContainerHigh)
        ) {
            // Title and controls row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Metrolist",
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(Modifier.size(12.dp).background(colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(1.dp)))
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Box(Modifier.width(18.dp).height(1.5.dp).background(colorScheme.onSurface.copy(alpha = 0.7f), RoundedCornerShape(1.dp)))
                        Box(Modifier.width(14.dp).height(1.dp).background(colorScheme.onSurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(1.dp)))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Icon(Icons.Outlined.SkipPrevious, null, tint = colorScheme.onSurface, modifier = Modifier.size(8.dp))
                        Box(Modifier.size(10.dp).background(colorScheme.primary, CircleShape))
                        Icon(Icons.Outlined.SkipNext, null, tint = colorScheme.onSurface, modifier = Modifier.size(8.dp))
                    }
                }
            }
            // Progress bar
            Box(
                Modifier.fillMaxWidth().height(16.dp).padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier.fillMaxWidth().height(2.dp)
                        .background(colorScheme.surfaceContainerHighest, RoundedCornerShape(1.dp))
                ) {
                    Box(Modifier.fillMaxHeight().fillMaxWidth(0.4f).background(colorScheme.primary, RoundedCornerShape(1.dp)))
                }
            }
        }

        // Compact content grid
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(colorScheme.surface)
                .padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Left content area
                Column(
                    modifier = Modifier.weight(2f),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    repeat(3) { index ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .background(
                                    if (index == 0) colorScheme.primaryContainer.copy(alpha = 0.3f)
                                    else colorScheme.surface,
                                    RoundedCornerShape(1.dp)
                                )
                                .padding(horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Box(
                                Modifier.size(4.dp).background(
                                    if (index == 0) colorScheme.primary.copy(alpha = 0.6f)
                                    else colorScheme.surfaceVariant,
                                    RoundedCornerShape(1.dp)
                                )
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(0.5.dp)) {
                                Box(Modifier.width(12.dp).height(1.dp).background(colorScheme.onSurface.copy(alpha = 0.6f), RoundedCornerShape(1.dp)))
                                Box(Modifier.width(8.dp).height(1.dp).background(colorScheme.onSurfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(1.dp)))
                            }
                        }
                    }
                }

                // Right sidebar (minimal)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(24.dp)
                        .background(colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                        .padding(2.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    repeat(3) {
                        Box(
                            Modifier.fillMaxWidth().height(2.dp)
                                .background(colorScheme.onSurfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(1.dp))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MinimalLayoutMockup(colorScheme: ColorScheme) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Minimal header - just essential info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .background(colorScheme.surface)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(Modifier.width(20.dp).height(2.dp).background(colorScheme.onSurface.copy(alpha = 0.6f), RoundedCornerShape(1.dp)))
            Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                Box(Modifier.size(3.dp).background(colorScheme.onSurfaceVariant, CircleShape))
                Box(Modifier.size(3.dp).background(colorScheme.onSurfaceVariant, CircleShape))
                Box(Modifier.size(3.dp).background(colorScheme.primary, CircleShape))
            }
        }

        // Full content area - no sidebar
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(colorScheme.surface)
                .padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            // Large current track display
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .background(colorScheme.primaryContainer.copy(alpha = 0.2f), RoundedCornerShape(3.dp))
                    .padding(horizontal = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Box(
                    Modifier.size(10.dp)
                        .background(colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(2.dp))
                )
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Box(Modifier.width(24.dp).height(2.dp).background(colorScheme.onSurface.copy(alpha = 0.8f), RoundedCornerShape(1.dp)))
                    Box(Modifier.width(18.dp).height(1.5.dp).background(colorScheme.onSurfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(1.dp)))
                }
                Spacer(Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Icon(Icons.Outlined.SkipPrevious, null, tint = colorScheme.onSurface, modifier = Modifier.size(8.dp))
                    Box(Modifier.size(10.dp).background(colorScheme.primary, CircleShape))
                    Icon(Icons.Outlined.SkipNext, null, tint = colorScheme.onSurface, modifier = Modifier.size(8.dp))
                }
            }

            // Clean track list
            Column(verticalArrangement = Arrangement.spacedBy(1.5.dp)) {
                repeat(4) { index ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .padding(horizontal = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(
                            Modifier.size(6.dp).background(
                                if (index == 1) colorScheme.primary.copy(alpha = 0.4f)
                                else colorScheme.surfaceVariant,
                                RoundedCornerShape(1.dp)
                            )
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Box(Modifier.width(16.dp).height(1.5.dp).background(
                                if (index == 1) colorScheme.onSurface.copy(alpha = 0.8f)
                                else colorScheme.onSurface.copy(alpha = 0.6f),
                                RoundedCornerShape(1.dp)
                            ))
                            Box(Modifier.width(12.dp).height(1.dp).background(colorScheme.onSurfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(1.dp)))
                        }
                    }
                }
            }
        }

        // Bottom minimal player
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(colorScheme.surfaceContainerLow)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Simple progress indicator
            Box(
                Modifier.width(60.dp).height(2.dp)
                    .background(colorScheme.surfaceContainerHighest, RoundedCornerShape(1.dp))
            ) {
                Box(Modifier.fillMaxHeight().fillMaxWidth(0.35f).background(colorScheme.primary, RoundedCornerShape(1.dp)))
            }
        }
    }
}

@Composable
fun AppLayoutDialog(
    colorScheme: ColorScheme,
    onDismiss: () -> Unit,
    onLayoutSelected: (AppLayout) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("App Layout", style = MaterialTheme.typography.headlineSmall)
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Choose your preferred interface layout inspired by Cider",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )

                // First row: Calico and Mavericks
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AppLayoutOption(
                        layout = AppLayout.CANVAS,
                        isSelected = AppState.appLayout == AppLayout.CANVAS,
                        colorScheme = colorScheme,
                        modifier = Modifier.weight(1f),
                        onClick = { onLayoutSelected(AppLayout.CANVAS) }
                    )
                    AppLayoutOption(
                        layout = AppLayout.STUDIO,
                        isSelected = AppState.appLayout == AppLayout.STUDIO,
                        colorScheme = colorScheme,
                        modifier = Modifier.weight(1f),
                        onClick = { onLayoutSelected(AppLayout.STUDIO) }
                    )
                }

                // Second row: Compact and Minimal
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AppLayoutOption(
                        layout = AppLayout.STAGE,
                        isSelected = AppState.appLayout == AppLayout.STAGE,
                        colorScheme = colorScheme,
                        modifier = Modifier.weight(1f),
                        onClick = { onLayoutSelected(AppLayout.STAGE) }
                    )
                    AppLayoutOption(
                        layout = AppLayout.FLOW,
                        isSelected = AppState.appLayout == AppLayout.FLOW,
                        colorScheme = colorScheme,
                        modifier = Modifier.weight(1f),
                        onClick = { onLayoutSelected(AppLayout.FLOW) }
                    )
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
fun AppLayoutOption(
    layout: AppLayout,
    isSelected: Boolean,
    colorScheme: ColorScheme,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) colorScheme.primary else colorScheme.outlineVariant
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .size(width = 120.dp, height = 90.dp)
                .clickable { onClick() },
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainer),
            border = BorderStroke(borderWidth, borderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 2.dp)
        ) {
            when (layout) {
                AppLayout.CANVAS -> CanvasLayoutMockup(colorScheme)
                AppLayout.STUDIO -> StudioLayoutMockup(colorScheme)
                AppLayout.STAGE -> StageLayoutMockup(colorScheme)
                AppLayout.FLOW -> FlowLayoutMockup(colorScheme)
            }
        }

        Text(
            text = when (layout) {
                AppLayout.CANVAS -> "Canvas"
                AppLayout.STUDIO -> "Studio"
                AppLayout.STAGE -> "Stage"
                AppLayout.FLOW -> "Flow"
            },
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

// Accurate mockups based on Cider screenshots

@Composable
fun CanvasLayoutMockup(colorScheme: ColorScheme) {
    // 1st screenshot: Basic layout with sidebar, main content, bottom player
    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .background(colorScheme.surfaceContainerHigh)
        )

        Row(modifier = Modifier.weight(1f)) {
            // Standard sidebar
            Column(
                modifier = Modifier
                    .width(24.dp)
                    .fillMaxHeight()
                    .background(colorScheme.surfaceContainerLowest),
                verticalArrangement = Arrangement.spacedBy(1.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(2.dp))
                repeat(5) { index ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(6.dp)
                            .background(
                                if (index == 0) colorScheme.primary.copy(alpha = 0.8f)
                                else colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
            }

            // Main content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(colorScheme.surface)
                    .padding(3.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Box(Modifier.fillMaxWidth().height(5.dp)
                    .background(colorScheme.surfaceContainerLow, RoundedCornerShape(1.dp)))
                repeat(3) { index ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(
                                if (index == 0) colorScheme.primaryContainer.copy(alpha = 0.3f)
                                else colorScheme.surface
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(4.dp)
                            .background(colorScheme.surfaceVariant, RoundedCornerShape(1.dp)))
                        Spacer(Modifier.width(2.dp))
                        Box(Modifier.width(12.dp).height(1.dp)
                            .background(colorScheme.onSurface.copy(alpha = 0.6f)))
                    }
                }
            }
        }

        // Bottom player bar with controls
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .background(colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Icon(Icons.Outlined.SkipPrevious, null, tint = colorScheme.onSurface, modifier = Modifier.size(4.dp))
                Box(Modifier.size(5.dp).background(colorScheme.primary, CircleShape))
                Icon(Icons.Outlined.SkipNext, null, tint = colorScheme.onSurface, modifier = Modifier.size(4.dp))
            }
        }
    }
}

@Composable
fun StudioLayoutMockup(colorScheme: ColorScheme) {
    // 2nd screenshot: Expanded sidebar with search at top, floating controls
    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar with floating controls (no "Metrolist" text)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .background(colorScheme.surfaceContainerHigh)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            // Floating action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Box(Modifier.size(4.dp).background(colorScheme.primary, CircleShape))
                Box(Modifier.size(4.dp).background(colorScheme.secondary, CircleShape))
            }
        }

        Row(modifier = Modifier.weight(1f)) {
            // Expanded sidebar with search
            Column(
                modifier = Modifier
                    .width(36.dp)
                    .fillMaxHeight()
                    .background(colorScheme.surfaceContainerLowest)
                    .padding(2.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                // Search bar at top
                Box(
                    Modifier.fillMaxWidth().height(6.dp)
                        .background(colorScheme.surfaceContainerHighest, RoundedCornerShape(3.dp))
                )
                Spacer(Modifier.height(1.dp))
                // Library items
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .background(
                                if (index == 1) colorScheme.primary.copy(alpha = 0.8f)
                                else colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
            }

            // Main content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(colorScheme.surface)
                    .padding(3.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                repeat(4) { index ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(
                                if (index == 0) colorScheme.primaryContainer.copy(alpha = 0.3f)
                                else colorScheme.surface
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(4.dp)
                            .background(colorScheme.surfaceVariant, RoundedCornerShape(1.dp)))
                        Spacer(Modifier.width(2.dp))
                        Box(Modifier.width(12.dp).height(1.dp)
                            .background(colorScheme.onSurface.copy(alpha = 0.6f)))
                    }
                }
            }
        }

        // Bottom player bar with controls
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .background(colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Icon(Icons.Outlined.SkipPrevious, null, tint = colorScheme.onSurface, modifier = Modifier.size(4.dp))
                Box(Modifier.size(5.dp).background(colorScheme.primary, CircleShape))
                Icon(Icons.Outlined.SkipNext, null, tint = colorScheme.onSurface, modifier = Modifier.size(4.dp))
            }
        }
    }
}

@Composable
fun StageLayoutMockup(colorScheme: ColorScheme) {
    // 3rd screenshot: Player in top bar, search in sidebar below player controls
    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar with integrated player controls (where Metrolist text was)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .background(colorScheme.surfaceContainerHigh)
                .padding(horizontal = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Current track info on left
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Box(Modifier.size(4.dp).background(colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(1.dp)))
                Box(Modifier.width(8.dp).height(1.dp).background(colorScheme.onSurface.copy(alpha = 0.7f)))
            }
            // Player controls on right
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Icon(Icons.Outlined.SkipPrevious, null, tint = colorScheme.onSurface, modifier = Modifier.size(3.dp))
                Box(Modifier.size(4.dp).background(colorScheme.primary, CircleShape))
                Icon(Icons.Outlined.SkipNext, null, tint = colorScheme.onSurface, modifier = Modifier.size(3.dp))
            }
        }

        Row(modifier = Modifier.weight(1f)) {
            // Sidebar with search below controls
            Column(
                modifier = Modifier
                    .width(32.dp)
                    .fillMaxHeight()
                    .background(colorScheme.surfaceContainerLowest)
                    .padding(2.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                // Player controls section in sidebar
                Box(
                    Modifier.fillMaxWidth().height(8.dp)
                        .background(colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                )
                // Search bar below controls
                Box(
                    Modifier.fillMaxWidth().height(5.dp)
                        .background(colorScheme.surfaceContainerHighest, RoundedCornerShape(2.dp))
                )
                Spacer(Modifier.height(1.dp))
                // Library items
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(
                                if (index == 0) colorScheme.primary.copy(alpha = 0.8f)
                                else colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                RoundedCornerShape(1.dp)
                            )
                    )
                }
            }

            // Main content (more space due to player in top bar)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(colorScheme.surface)
                    .padding(3.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                repeat(5) { index ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(
                                if (index == 0) colorScheme.primaryContainer.copy(alpha = 0.3f)
                                else colorScheme.surface
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(4.dp)
                            .background(colorScheme.surfaceVariant, RoundedCornerShape(1.dp)))
                        Spacer(Modifier.width(2.dp))
                        Box(Modifier.width(12.dp).height(1.dp)
                            .background(colorScheme.onSurface.copy(alpha = 0.6f)))
                    }
                }
            }
        }

        // Minimal bottom bar (since player is in top bar)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(colorScheme.surfaceContainerLow)
        )
    }
}

@Composable
fun FlowLayoutMockup(colorScheme: ColorScheme) {
    // 4th screenshot: Similar to Stage but player controls outside sidebar
    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar with integrated player (external to sidebar)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .background(colorScheme.surfaceContainerHigh)
                .padding(horizontal = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Current track info on left
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Box(Modifier.size(4.dp).background(colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(1.dp)))
                Box(Modifier.width(10.dp).height(1.dp).background(colorScheme.onSurface.copy(alpha = 0.7f)))
            }
            // Player controls on right (external to sidebar)
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Icon(Icons.Outlined.SkipPrevious, null, tint = colorScheme.onSurface, modifier = Modifier.size(3.dp))
                Box(Modifier.size(4.dp).background(colorScheme.primary, CircleShape))
                Icon(Icons.Outlined.SkipNext, null, tint = colorScheme.onSurface, modifier = Modifier.size(3.dp))
            }
        }

        Row(modifier = Modifier.weight(1f)) {
            // Clean sidebar (no player controls, just search and library)
            Column(
                modifier = Modifier
                    .width(30.dp)
                    .fillMaxHeight()
                    .background(colorScheme.surfaceContainerLowest)
                    .padding(2.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                // Search bar at top
                Box(
                    Modifier.fillMaxWidth().height(6.dp)
                        .background(colorScheme.surfaceContainerHighest, RoundedCornerShape(3.dp))
                )
                Spacer(Modifier.height(2.dp))
                // Library items (clean, no player controls)
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(
                                if (index == 0) colorScheme.primary.copy(alpha = 0.8f)
                                else colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                RoundedCornerShape(1.dp)
                            )
                    )
                }
            }

            // Streamlined content area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(colorScheme.surface)
                    .padding(3.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                repeat(5) { index ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(
                                if (index == 0) colorScheme.primaryContainer.copy(alpha = 0.3f)
                                else colorScheme.surface
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(4.dp)
                            .background(colorScheme.surfaceVariant, RoundedCornerShape(1.dp)))
                        Spacer(Modifier.width(2.dp))
                        Box(Modifier.width(14.dp).height(1.dp)
                            .background(colorScheme.onSurface.copy(alpha = 0.6f)))
                    }
                }
            }
        }

        // Minimal bottom bar (no player since it's all in top bar)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(colorScheme.surface)
        )
    }
}