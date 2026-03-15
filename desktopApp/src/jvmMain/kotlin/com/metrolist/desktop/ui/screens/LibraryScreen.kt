package com.metrolist.desktop.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.metrolist.shared.model.*
import com.metrolist.desktop.state.AppState
import com.metrolist.desktop.ui.components.YTListItem
import com.metrolist.desktop.ui.components.YTGridItem

enum class LibraryViewMode {
    LIST, GRID
}

private data class AutoPlaylist(
    val type: String,
    val title: String,
    val icon: ImageVector,
    val iconTint: @Composable () -> Color,
    val iconBackground: @Composable () -> Color,
    val subtitle: String
)

@Composable
private fun autoPlaylists(): List<AutoPlaylist> {
    val cs = MaterialTheme.colorScheme
    return listOf(
        AutoPlaylist("top50", "My Top 50", Icons.Outlined.Star,
            { cs.onPrimaryContainer }, { cs.primaryContainer },
            "${AppState.topSongs.size} songs • Based on your play history"),
        AutoPlaylist("downloaded", "Downloaded", Icons.Outlined.Download,
            { cs.onSecondaryContainer }, { cs.secondaryContainer },
            "Offline songs"),
        AutoPlaylist("cached", "Cached", Icons.Outlined.OfflinePin,
            { cs.onTertiaryContainer }, { cs.tertiaryContainer },
            "Streamed & cached songs"),
        AutoPlaylist("uploaded", "Uploaded", Icons.Outlined.CloudUpload,
            { cs.onSurfaceVariant }, { cs.surfaceVariant },
            "Your uploaded songs")
    )
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
    val playlists = autoPlaylists()

    Column(modifier = Modifier.fillMaxSize()) {
        // Header / Toolbar
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
            OutlinedTextField(
                value = filterQuery,
                onValueChange = { filterQuery = it },
                placeholder = { Text("Search your library") },
                modifier = Modifier.width(300.dp).heightIn(min = 48.dp),
                shape = RoundedCornerShape(24.dp),
                leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
                trailingIcon = if (filterQuery.isNotEmpty()) {
                    { IconButton(onClick = { filterQuery = "" }) { Icon(Icons.Default.Clear, null, modifier = Modifier.size(18.dp)) } }
                } else null,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedContainerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                singleLine = true
            )
            Spacer(Modifier.weight(1f))
            Surface(
                color = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    LibraryViewModeButton(
                        selected = viewMode == LibraryViewMode.LIST,
                        onClick = { viewMode = LibraryViewMode.LIST },
                        icon = Icons.AutoMirrored.Filled.List,
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

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (viewMode == LibraryViewMode.LIST) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(playlists, key = { it.type }) { ap ->
                        AutoPlaylistListItem(ap, colorScheme)
                    }
                    // Library items
                    if (sections.isEmpty()) {
                        item { LibraryLoadingItem(colorScheme) }
                    } else if (filteredItems.isEmpty() && filterQuery.isNotEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("No items found matching \"$filterQuery\"", color = colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        items(filteredItems) { item ->
                            YTListItem(item, colorScheme) {
                                if (item is SongItem) AppState.playTrack(item)
                            }
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
                    items(playlists, key = { it.type }) { ap ->
                        AutoPlaylistGridItem(ap, colorScheme)
                    }
                    // Library items
                    if (sections.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            LibraryLoadingItem(colorScheme)
                        }
                    } else if (filteredItems.isEmpty() && filterQuery.isNotEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("No items found matching \"$filterQuery\"", color = colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
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
private fun AutoPlaylistListItem(ap: AutoPlaylist, colorScheme: ColorScheme) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { AppState.selectedLocalPlaylist = ap.type }
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(ap.iconBackground()),
            contentAlignment = Alignment.Center
        ) {
            Icon(ap.icon, null, modifier = Modifier.size(22.dp), tint = ap.iconTint())
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                ap.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                ap.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(Icons.Default.ChevronRight, null, tint = colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
    }
}

@Composable
private fun AutoPlaylistGridItem(ap: AutoPlaylist, colorScheme: ColorScheme) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { AppState.selectedLocalPlaylist = ap.type }
            .padding(bottom = 4.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(ap.iconBackground()),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                ap.icon,
                null,
                modifier = Modifier.size(44.dp),
                tint = ap.iconTint().copy(alpha = 0.85f)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            ap.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            "Auto playlist",
            style = MaterialTheme.typography.labelSmall,
            color = colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
private fun LibraryLoadingItem(colorScheme: ColorScheme) {
    Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            Text("Syncing your library...", color = colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LibraryViewModeButton(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
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
