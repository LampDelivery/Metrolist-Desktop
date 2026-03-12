package com.metrolist.desktop.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.metrolist.shared.model.*
import com.metrolist.desktop.state.AppState
import com.metrolist.desktop.ui.components.YTListItem
import com.metrolist.desktop.ui.components.YTGridItem

enum class LibraryViewMode {
    LIST, GRID
}

@Composable
fun LibraryScreen(sections: Map<String, List<YTItem>>, colorScheme: ColorScheme) {
    var viewMode by remember { mutableStateOf(LibraryViewMode.GRID) }
    var filterQuery by remember { mutableStateOf("") }
    
    val allItems = remember(sections) { sections.values.flatten() }
    val filteredItems = remember(allItems, filterQuery) {
        if (filterQuery.isEmpty()) allItems
        else allItems.filter { it.title.contains(filterQuery, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Library Header / Toolbar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Library", 
                style = MaterialTheme.typography.headlineMedium, 
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
            
            Spacer(Modifier.width(24.dp))
            
            // Search within library
            OutlinedTextField(
                value = filterQuery,
                onValueChange = { filterQuery = it },
                placeholder = { Text("Search your library") },
                modifier = Modifier.width(300.dp).heightIn(min = 48.dp),
                shape = RoundedCornerShape(24.dp),
                leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
                trailingIcon = if (filterQuery.isNotEmpty()) {
                    { 
                        IconButton(onClick = { filterQuery = "" }) { 
                            Icon(Icons.Default.Clear, null, modifier = Modifier.size(18.dp))
                        } 
                    }
                } else null,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedContainerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                singleLine = true
            )

            Spacer(Modifier.weight(1f))

            // View Mode Toggles
            Surface(
                color = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    LibraryViewModeButton(
                        selected = viewMode == LibraryViewMode.LIST,
                        onClick = { viewMode = LibraryViewMode.LIST },
                        icon = Icons.Default.List,
                        colorScheme = colorScheme
                    )
                    LibraryViewModeButton(
                        selected = viewMode == LibraryViewMode.GRID,
                        onClick = { viewMode = LibraryViewMode.GRID },
                        icon = Icons.Default.GridView,
                        colorScheme = colorScheme
                    )
                }
            }
        }

        if (sections.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = colorScheme.primary)
                    Spacer(Modifier.height(16.dp))
                    Text("Syncing your library...", color = colorScheme.onSurfaceVariant)
                }
            }
        } else if (filteredItems.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No items found matching \"$filterQuery\"", color = colorScheme.onSurfaceVariant)
            }
        } else {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (viewMode == LibraryViewMode.LIST) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
                        contentPadding = PaddingValues(bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredItems) { item ->
                            YTListItem(item, colorScheme) { 
                                if (item is SongItem) AppState.playTrack(item)
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 180.dp),
                        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
                        contentPadding = PaddingValues(bottom = 32.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalArrangement = Arrangement.spacedBy(32.dp)
                    ) {
                        items(filteredItems) { item ->
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

@Composable
private fun LibraryViewModeButton(
    selected: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    colorScheme: ColorScheme
) {
    Surface(
        onClick = onClick,
        color = if (selected) colorScheme.primary else Color.Transparent,
        shape = CircleShape,
        modifier = Modifier.size(32.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                icon, 
                null, 
                modifier = Modifier.size(18.dp),
                tint = if (selected) colorScheme.onPrimary else colorScheme.onSurfaceVariant
            )
        }
    }
}
