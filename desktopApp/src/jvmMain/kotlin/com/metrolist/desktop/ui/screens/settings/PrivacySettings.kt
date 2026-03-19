package com.metrolist.desktop.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.metrolist.desktop.state.AppState

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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                "Privacy",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        SettingsGroup(title = "History & Data", colorScheme = colorScheme) {
            SettingsToggleWithIcon(
                title = "Pause listen history",
                subtitle = "Stops recording played songs to your local history",
                icon = Icons.Outlined.Description,
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

            SettingsToggleWithIcon(
                title = "Pause search history",
                subtitle = "Stops saving your search queries",
                icon = Icons.Outlined.Visibility,
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