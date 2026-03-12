package com.metrolist.desktop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.outlined.Radio
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metrolist.shared.model.*
import com.metrolist.desktop.state.AppState
import com.metrolist.desktop.ui.components.AsyncImage
import com.metrolist.desktop.ui.components.YTGridItem
import com.metrolist.desktop.ui.components.YTListItem

@Composable
fun ArtistScreen(artistId: String, colorScheme: ColorScheme) {
    if (AppState.isArtistLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = colorScheme.primary)
        }
    } else {
        val artistInfo = AppState.artistData["header"]?.firstOrNull() as? ArtistItem
        
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // Header with Banner
            item {
                Box(modifier = Modifier.fillMaxWidth().height(350.dp)) {
                    if (artistInfo?.banner != null) {
                        AsyncImage(
                            url = artistInfo.banner!!,
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(0.dp)
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, colorScheme.background.copy(alpha = 0.8f), colorScheme.background),
                                    startY = 300f
                                )
                            )
                    )
                    
                    // Artist info text and buttons
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 32.dp, vertical = 24.dp)
                    ) {
                        Text(
                            text = artistInfo?.title ?: "Unknown Artist",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            fontSize = 48.sp
                        )
                        
                        if (artistInfo?.subscribers != null) {
                            Text(
                                text = artistInfo.subscribers!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Spacer(Modifier.height(20.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(onClick = { /* Toggle Sub */ }, colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)) {
                                Text("Subscribe")
                            }
                            
                            OutlinedButton(onClick = { /* Start Radio */ }) {
                                Icon(Icons.Outlined.Radio, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Radio")
                            }
                            
                            IconButton(
                                onClick = { /* Shuffle */ },
                                modifier = Modifier.clip(CircleShape).background(colorScheme.secondaryContainer)
                            ) {
                                Icon(Icons.Default.Shuffle, null, tint = colorScheme.onSecondaryContainer)
                            }
                        }
                    }
                }
            }

            // Artist Sections (Songs, Albums, etc.)
            AppState.artistData.forEach { (header, items) ->
                if (header != "header") {
                    item {
                        Column(modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)) {
                            Text(header, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(16.dp))
                            
                            if (header == "Songs") {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items.forEach { item ->
                                        YTListItem(item, colorScheme) { 
                                            if (item is SongItem) AppState.playTrack(item)
                                        }
                                    }
                                }
                            } else {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    items(items) { item ->
                                        YTGridItem(item, colorScheme) { 
                                            if (item is SongItem) AppState.playTrack(item)
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}
