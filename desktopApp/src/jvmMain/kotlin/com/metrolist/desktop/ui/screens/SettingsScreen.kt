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
                title = "Floating Player",
                subtitle = "Makes the player bar a floating pill shape",
                checked = AppState.isFloatingPlayer,
                onCheckedChange = { AppState.updateFloatingPlayerMode(it) },
                colorScheme = colorScheme
            )

            SettingsToggle(
                title = "Animated Gradient",
                subtitle = "Apple Music-style flowing background in expanded mode",
                checked = AppState.animatedGradient,
                onCheckedChange = { AppState.toggleAnimatedGradient(it) },
                colorScheme = colorScheme
            )

            SettingsToggle(
                title = "Night floating player",
                subtitle = "Uses absolute black for the bottom player bar",
                checked = AppState.pureBlackMiniPlayer,
                onCheckedChange = { AppState.togglePureBlackMiniPlayer(it) },
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
