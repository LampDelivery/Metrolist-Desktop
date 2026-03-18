package com.metrolist.desktop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.metrolist.desktop.state.AppState
import com.metrolist.desktop.ui.components.AutoPlaylistGridItem
import com.metrolist.desktop.ui.components.AutoPlaylistListItem
import com.metrolist.desktop.ui.components.ChipsRow
import com.metrolist.desktop.ui.components.YTGridItem
import com.metrolist.desktop.ui.components.YTListItem
import com.metrolist.shared.model.AlbumItem
import com.metrolist.shared.model.ArtistItem
import com.metrolist.shared.model.PlaylistItem
import com.metrolist.shared.model.SongItem
import com.metrolist.shared.model.YTItem

enum class LibraryViewMode {
    LIST, GRID
}

enum class LibraryFilter {
    LIBRARY, SONGS, ARTISTS, ALBUMS, PLAYLISTS
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
    return listOfNotNull(
        // Liked playlist - using heart icon like mobile, only show if enabled
        if (AppState.showLikedPlaylist) {
            AutoPlaylist("liked", "Liked Songs", Icons.Filled.Favorite,
                { Color.White }, { cs.error },
                "${AppState.likedSongIds.size} songs")
        } else null,
        // Downloaded playlist - using download icon like mobile, only show if enabled
        if (AppState.showDownloadedPlaylist) {
            AutoPlaylist("downloaded", "Downloaded", Icons.Outlined.Download,
                { cs.onSecondaryContainer }, { cs.secondaryContainer },
                "Offline songs")
        } else null,
        // My Top playlist - using trending up icon like mobile, only show if enabled
        if (AppState.showTopPlaylist) {
            AutoPlaylist("top50", "My Top 50", Icons.AutoMirrored.Outlined.TrendingUp,
                { cs.onPrimaryContainer }, { cs.primaryContainer },
                "${AppState.topSongs.size} songs")
        } else null,
        // Uploaded playlist - using backup icon like mobile, only show if enabled
        if (AppState.showUploadedPlaylist) {
            AutoPlaylist("uploaded", "Uploaded", Icons.Outlined.Backup,
                { cs.onTertiaryContainer }, { cs.tertiaryContainer },
                "Your uploaded songs")
        } else null,
        // Cached playlist - using cached icon like mobile, only show if enabled
        if (AppState.showCachedPlaylist) {
            AutoPlaylist("cached", "Cached", Icons.Outlined.Storage,
                { cs.onSurfaceVariant }, { cs.surfaceVariant },
                "Cached songs")
        } else null,
    )
}

@Composable
fun LibraryScreen(sections: Map<String, List<YTItem>>, colorScheme: ColorScheme) {
    var viewMode by remember { mutableStateOf(LibraryViewMode.GRID) }
    var filterQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(LibraryFilter.LIBRARY) }

    // Filter out YouTube liked playlist when autogenerated liked playlist is enabled
    val filteredSections = remember(sections) {
        sections.filter { (sectionTitle, items) ->
            // Remove YouTube's "Liked Music" section if we're showing autogenerated liked playlist
            if (AppState.showLikedPlaylist &&
                (sectionTitle.contains("Liked", ignoreCase = true) ||
                 items.any { it is PlaylistItem && it.id == "LM" })) {
                false
            } else {
                true
            }
        }
    }
    val allItems = remember(filteredSections) { filteredSections.values.flatten() }
    val filteredItems = remember(allItems, filterQuery, selectedFilter) {
        val baseItems = when (selectedFilter) {
            LibraryFilter.LIBRARY -> allItems
            LibraryFilter.SONGS -> allItems.filterIsInstance<SongItem>()
            LibraryFilter.ARTISTS -> allItems.filterIsInstance<ArtistItem>()
            LibraryFilter.ALBUMS -> allItems.filterIsInstance<AlbumItem>()
            LibraryFilter.PLAYLISTS -> allItems.filterIsInstance<PlaylistItem>()
        }
        if (filterQuery.isEmpty()) baseItems
        else baseItems.filter { it.title.contains(filterQuery, ignoreCase = true) }
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
            // M3E Style Search Bar
            Surface(
                modifier = Modifier.width(300.dp).height(48.dp),
                shape = RoundedCornerShape(24.dp),
                color = colorScheme.surfaceContainerHighest,
                shadowElevation = if (filterQuery.isNotEmpty()) 3.dp else 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    BasicTextField(
                        value = filterQuery,
                        onValueChange = { filterQuery = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = colorScheme.onSurface
                        ),
                        decorationBox = { innerTextField ->
                            if (filterQuery.isEmpty()) {
                                Text(
                                    "Search your library",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                            innerTextField()
                        }
                    )
                    if (filterQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { filterQuery = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
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

        // Filter Chips Row - matches mobile LibraryMixScreen pattern
        ChipsRow(
            chips = LibraryFilter.values().map { filter ->
                filter to when (filter) {
                    LibraryFilter.LIBRARY -> "All"
                    LibraryFilter.SONGS -> "Songs"
                    LibraryFilter.ARTISTS -> "Artists"
                    LibraryFilter.ALBUMS -> "Albums"
                    LibraryFilter.PLAYLISTS -> "Playlists"
                }
            },
            currentValue = selectedFilter,
            onValueUpdate = { selectedFilter = it },
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)
        )

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
                    // Autogenerated playlists
                    items(playlists, key = { it.type }) { ap ->
                        AutoPlaylistListItem(
                            title = ap.title,
                            subtitle = ap.subtitle,
                            icon = ap.icon,
                            iconTint = ap.iconTint(),
                            iconBackground = ap.iconBackground(),
                            colorScheme = colorScheme
                        ) {
                            // Route to regular PlaylistScreen instead of LocalPlaylistScreen
                            AppState.selectedPlaylistId = "auto_playlist_${ap.type}"
                            AppState.fetchPlaylistData("auto_playlist_${ap.type}")
                        }
                    }
                    // Library items
                    if (sections.isEmpty()) {
                        item {
                            if (AppState.isSignedIn) LibraryLoadingItem(colorScheme)
                        }
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
                        AutoPlaylistGridItem(
                            title = ap.title,
                            subtitle = ap.subtitle,
                            icon = ap.icon,
                            iconTint = ap.iconTint(),
                            iconBackground = ap.iconBackground(),
                            colorScheme = colorScheme
                        ) {
                            // Route to regular PlaylistScreen instead of LocalPlaylistScreen
                            AppState.selectedPlaylistId = "auto_playlist_${ap.type}"
                            AppState.fetchPlaylistData("auto_playlist_${ap.type}")
                        }
                    }
                    // Library items
                    if (sections.isEmpty()) {
                        if (AppState.isSignedIn) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                LibraryLoadingItem(colorScheme)
                            }
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
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                // Route to regular playlist screen with auto-playlist prefix
                AppState.selectedPlaylistId = "auto_playlist_${ap.type}"
            }
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)  // Larger size to match Android
                .clip(RoundedCornerShape(12.dp))
                .background(ap.iconBackground()),
            contentAlignment = Alignment.Center
        ) {
            Icon(ap.icon, null, modifier = Modifier.size(28.dp), tint = ap.iconTint())  // Larger icon
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                ap.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
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
    }
}

@Composable
private fun AutoPlaylistGridItem(ap: AutoPlaylist, colorScheme: ColorScheme) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                // Route to regular playlist screen with auto-playlist prefix
                AppState.selectedPlaylistId = "auto_playlist_${ap.type}"
            }
            .padding(bottom = 8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(ap.iconBackground()),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                ap.icon,
                null,
                modifier = Modifier.size(56.dp),  // Much larger icon for grid
                tint = ap.iconTint()
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            ap.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            ap.subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
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
private fun LibrarySignInItem(colorScheme: ColorScheme) {
    Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Outlined.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(Modifier.height(12.dp))
            Text("Sign in to see your library", color = colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            Button(onClick = { AppState.showSignIn = true }) {
                Text("Sign in")
            }
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
