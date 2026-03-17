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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.metrolist.desktop.ui.components.EnumDialog
import com.metrolist.shared.model.LyricsProvider
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

// Import the updated settings screens
import com.metrolist.desktop.ui.screens.settings.*

// Components specific to sub-screens (not duplicates of shared components)

@Composable
fun ThemeColorGrid(colorScheme: ColorScheme) {
    val themeColors = listOf(
        0xFF6750A4, 0xFFFF5722, 0xFF2196F3, 0xFF4CAF50,
        0xFFFFC107, 0xFF9C27B0, 0xFF00BCD4, 0xFFE91E63,
        0xFF795548, 0xFF607D8B, 0xFF8BC34A, 0xFFFF9800
    )

    Column(modifier = Modifier.padding(16.dp)) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(themeColors) { color ->
                val isSelected = AppState.selectedThemeColor == color
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.2f else 1f,
                    animationSpec = tween(200)
                )

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                        .background(
                            androidx.compose.ui.graphics.Color(color),
                            CircleShape
                        )
                        .clickable { AppState.updateThemeColor(color) }
                        .then(
                            if (isSelected) {
                                Modifier.border(3.dp, colorScheme.primary, CircleShape)
                            } else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = "Selected",
                            tint = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Custom Color:",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurface
            )

            var customColor by remember { mutableStateOf("#6750A4") }

            OutlinedTextField(
                value = customColor,
                onValueChange = { newValue ->
                    customColor = newValue
                    try {
                        val colorInt = java.lang.Long.parseLong(newValue.removePrefix("#"), 16)
                        AppState.updateThemeColor(colorInt)
                    } catch (_: Exception) {
                        // Invalid color format
                    }
                },
                label = { Text("HEX Color") },
                placeholder = { Text("#6750A4") },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.primary,
                    unfocusedBorderColor = colorScheme.outline
                )
            )
        }
    }
}

// Lyrics Provider List Component (used in ContentSettings)
@Composable
fun LyricsProviderList(colorScheme: ColorScheme) {
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val newOrder = AppState.lyricsProviderOrder.toMutableList()
        val item = newOrder.removeAt(from.index)
        newOrder.add(to.index, item)
        AppState.updateLyricsProviderOrder(newOrder)
    }

    LazyColumn(
        state = lazyListState,
        modifier = Modifier.height(300.dp)
    ) {
        items(AppState.lyricsProviderOrder, { it }) { provider ->
            ReorderableItem(reorderableLazyListState, key = provider) { isDragging ->
                val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDragging) colorScheme.surfaceVariant else colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = elevation)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.DragIndicator,
                            contentDescription = "Drag to reorder",
                            tint = colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                provider.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurface
                            )
                            Text(
                                when (provider) {
                                    LyricsProvider.LRCLIB -> "LRCLib (recommended)"
                                    LyricsProvider.BETTERLYRICS -> "BetterLyrics"
                                    LyricsProvider.SIMPMUSIC -> "SimpMusic"
                                    LyricsProvider.LYRICSPLUS -> "LyricsPlus"
                                    LyricsProvider.YOUTUBE -> "YouTube"
                                    LyricsProvider.AUTO -> "Auto (detect best)"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = provider in AppState.lyricsEnabledProviders,
                            onCheckedChange = { enabled ->
                                AppState.toggleLyricsProvider(provider, enabled)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colorScheme.primary,
                                checkedTrackColor = colorScheme.primary.copy(alpha = 0.3f),
                                uncheckedThumbColor = colorScheme.outline,
                                uncheckedTrackColor = colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }
        }
    }
}

// Wrapper functions to call new settings screens
@Composable
fun AppearanceSettingsWrapper(colorScheme: ColorScheme) {
    AppearanceSettingsScreen(colorScheme)
}

@Composable
fun ContentSettingsScreenWrapper(colorScheme: ColorScheme) {
    ContentSettingsScreen(colorScheme)
}

@Composable
fun PlayerSettingsWrapper(colorScheme: ColorScheme) {
    PlayerSettingsScreen(colorScheme)
}

@Composable
fun PrivacySettingsWrapper(colorScheme: ColorScheme) {
    PrivacySettingsScreen(colorScheme)
}

@Composable
fun IntegrationsSettingsWrapper(colorScheme: ColorScheme) {
    IntegrationsSettingsScreen(colorScheme)
}