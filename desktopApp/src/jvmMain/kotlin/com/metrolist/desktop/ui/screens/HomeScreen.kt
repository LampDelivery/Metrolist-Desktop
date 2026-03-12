package com.metrolist.desktop.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.metrolist.shared.model.*
import com.metrolist.desktop.state.AppState
import com.metrolist.desktop.ui.components.SpeedDialGridItem
import com.metrolist.desktop.ui.components.YTGridItem

@Composable
fun HomeScreen(sections: Map<String, List<YTItem>>, colorScheme: ColorScheme) {
    if (sections.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = colorScheme.primary)
                Spacer(Modifier.height(16.dp))
                Text("Fetching your music...", color = colorScheme.onSurfaceVariant)
            }
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp), contentPadding = PaddingValues(vertical = 32.dp)) {
            val speedDialItems = sections["Quick picks"] ?: emptyList()
            if (speedDialItems.isNotEmpty()) {
                item {
                    Text("Speed Dial", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(20.dp))
                    Box(modifier = Modifier.height(360.dp).fillMaxWidth()) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize(),
                            userScrollEnabled = false
                        ) {
                            items(speedDialItems.take(9)) { item ->
                                SpeedDialGridItem(item, colorScheme) { 
                                    if (item is SongItem) AppState.playTrack(item)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(40.dp))
                }
            }

            sections.forEach { (header, items) ->
                if (header != "Quick picks" && header != "header") {
                    item {
                        Text(header, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(20.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 40.dp)
                        ) {
                            items(items) { item ->
                                YTGridItem(item, colorScheme) { 
                                    if (item is SongItem) AppState.playTrack(item)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
