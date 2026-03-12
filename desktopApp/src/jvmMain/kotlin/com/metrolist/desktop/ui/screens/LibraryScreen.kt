package com.metrolist.desktop.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.metrolist.shared.model.*
import com.metrolist.desktop.state.AppState
import com.metrolist.desktop.ui.components.YTListItem

@Composable
fun LibraryScreen(sections: Map<String, List<YTItem>>, colorScheme: ColorScheme) {
    if (sections.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = colorScheme.primary)
                Spacer(Modifier.height(16.dp))
                Text("Loading your library...", color = colorScheme.onSurfaceVariant)
            }
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp), contentPadding = PaddingValues(vertical = 32.dp)) {
            sections.forEach { (header, items) ->
                item {
                    Text(header, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(20.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 32.dp)) {
                        items.forEach { item ->
                            YTListItem(item, colorScheme) { 
                                if (item is SongItem) AppState.playTrack(item)
                            }
                        }
                    }
                }
            }
        }
    }
}
