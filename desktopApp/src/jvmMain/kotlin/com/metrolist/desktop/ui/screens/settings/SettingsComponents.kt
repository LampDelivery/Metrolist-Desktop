package com.metrolist.desktop.ui.screens.settings

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.metrolist.desktop.constants.SliderStyle
import com.metrolist.desktop.state.AppState
import com.metrolist.desktop.ui.screens.settings.LocalSettingsSearchQuery
import com.metrolist.desktop.state.CountryCodeToName
import com.metrolist.desktop.state.LanguageCodeToName
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

@Composable
fun SettingsGroup(
    title: String,
    colorScheme: ColorScheme,
    searchKeywords: List<String> = emptyList(),
    content: @Composable ColumnScope.() -> Unit
) {
    val searchQuery = LocalSettingsSearchQuery.current.lowercase()
    
    // Filter based on search query
    if (searchQuery.isNotEmpty() && searchKeywords.isNotEmpty()) {
        val matches = searchKeywords.any { keyword ->
            keyword.lowercase().contains(searchQuery) || searchQuery.contains(keyword.lowercase())
        }
        if (!matches) return
    }
    
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            color = colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
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
fun SettingsToggle(
    title: String,
    subtitle: String? = null,
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
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )

            subtitle?.let {
                Text(
                    text = it,
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
fun SettingsActionRow(
    title: String,
    subtitle: String,
    colorScheme: ColorScheme,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = colorScheme.error
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant
            )
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
                        val checked = provider in AppState.lyricsEnabledProviders
                        Switch(
                            checked = checked,
                            onCheckedChange = { AppState.toggleLyricsProvider(provider, it) },
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
            }
        }
    }
}

@Composable
fun LanguageDialog(
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
    title: String,
    current: String,
    colorScheme: ColorScheme
) {
    val sortedLanguages = LanguageCodeToName.toList().sortedBy { it.second }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = {
            Text(title)
        },
        text = {
            LazyColumn(
                modifier = Modifier.height(400.dp)
            ) {
                items(sortedLanguages) { (code, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(code) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = current == code,
                            onClick = { onSelect(code) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(name, style = MaterialTheme.typography.bodyMedium)
                    }
                    if (code != sortedLanguages.last().first) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            thickness = 0.5.dp,
                            color = colorScheme.outline.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        },
        containerColor = colorScheme.surface,
        modifier = Modifier.widthIn(max = 500.dp)
    )
}

@Composable
fun CountryDialog(
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
    title: String,
    current: String,
    colorScheme: ColorScheme
) {
    val sortedCountries = CountryCodeToName.toList().sortedBy { it.second }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = {
            Text(title)
        },
        text = {
            LazyColumn(
                modifier = Modifier.height(400.dp)
            ) {
                items(sortedCountries) { (code, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(code) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = current == code,
                            onClick = { onSelect(code) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(name, style = MaterialTheme.typography.bodyMedium)
                    }
                    if (code != sortedCountries.last().first) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            thickness = 0.5.dp,
                            color = colorScheme.outline.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        },
        containerColor = colorScheme.surface,
        modifier = Modifier.widthIn(max = 500.dp)
    )
}

@Composable
fun TopListLengthDialog(
    onDismiss: () -> Unit,
    onSelect: (Float) -> Unit,
    current: Float,
    colorScheme: ColorScheme
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        },
        title = {
            Text("Top List Length")
        },
        text = {
            Column {
                Text(
                    "Number of items to show in Top playlist sections",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                var sliderValue by remember { mutableStateOf(current) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("1", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        onValueChangeFinished = { onSelect(sliderValue) },
                        valueRange = 1f..100f,
                        steps = 98,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = colorScheme.primary,
                            activeTrackColor = colorScheme.primary,
                            inactiveTrackColor = colorScheme.outline
                        )
                    )
                    Text("100", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                }
                Text(
                    "${sliderValue.toInt()} items",
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
        },
        containerColor = colorScheme.surface,
        modifier = Modifier.widthIn(max = 400.dp)
    )
}

@Composable
fun QuickPicksDialog(
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
    current: String,
    colorScheme: ColorScheme
) {
    val options = listOf(
        "QUICK_PICKS" to "Quick Picks",
        "LAST_LISTEN" to "Last Listen"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = {
            Text("Quick Picks Algorithm")
        },
        text = {
            Column {
                Text(
                    "Choose the algorithm for home screen quick picks section",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                options.forEach { (code, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(code) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = current == code,
                            onClick = { onSelect(code) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(name, style = MaterialTheme.typography.bodyMedium)
                    }
                    if (code != options.last().first) {
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = colorScheme.outline.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        },
        containerColor = colorScheme.surface,
        modifier = Modifier.widthIn(max = 400.dp)
    )
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

                // Second row: Slim and Squiggly
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
                    // Squiggly slider preview
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