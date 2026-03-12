package com.metrolist.desktop.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.metrolist.shared.model.*
import com.metrolist.desktop.state.AppState
import com.metrolist.desktop.ui.components.YTListItem

@Composable
fun SearchResultsList(items: List<YTItem>, colorScheme: ColorScheme) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp).padding(top = 32.dp)) {
        Text("Search Results", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(20.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items) { item ->
                YTListItem(item, colorScheme) { 
                    if (item is SongItem) AppState.playTrack(item)
                }
            }
        }
    }
}
