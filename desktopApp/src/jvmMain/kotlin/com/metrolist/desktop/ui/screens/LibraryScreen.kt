package com.metrolist.desktop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
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
import com.metrolist.desktop.state.AppState
import com.metrolist.desktop.ui.components.ChipsRow
import com.metrolist.desktop.ui.components.YTGridItem
import com.metrolist.desktop.ui.components.YTListItem
import com.metrolist.shared.model.*

enum class LibraryFilter {
    LIBRARY, PLAYLISTS, SONGS, ARTISTS, ALBUMS, PODCASTS
}

enum class LibraryViewType {
    LIST, GRID
}

enum class LibrarySortType {
    CREATE_DATE, NAME, LAST_UPDATED, SONG_COUNT, ARTIST
}

private data class AutoPlaylist(
    val type: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconTint: @Composable () -> Color,
    val iconBackground: @Composable () -> Color,
    val route: String
)

@Composable
fun LibraryScreen(sections: Map<String, List<YTItem>>, colorScheme: ColorScheme) {
    // State management - matching Android exactly
    var currentFilter by remember { mutableStateOf(LibraryFilter.LIBRARY) }
    var viewType by remember { mutableStateOf(AppState.libraryViewType) }
    var sortType by remember { mutableStateOf(LibrarySortType.CREATE_DATE) }
    var searchQuery by remember { mutableStateOf("") }
    var showSortMenu by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header Section - matching Android layout exactly
        LibraryHeader(
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            viewType = viewType,
            onViewTypeChange = {
                viewType = it
                AppState.setLibraryViewType(it)
            },
            sortType = sortType,
            onSortTypeChange = { sortType = it },
            showSortMenu = showSortMenu,
            onShowSortMenuChange = { showSortMenu = it },
            colorScheme = colorScheme,
            currentFilter = currentFilter
        )

        // Filter Chips - exactly like Android
        FilterChipsRow(
            currentFilter = currentFilter,
            onFilterChange = { currentFilter = it },
            colorScheme = colorScheme
        )

        // Content based on current filter
        LibraryContent(
            currentFilter = currentFilter,
            viewType = viewType,
            sortType = sortType,
            searchQuery = searchQuery,
            sections = sections,
            colorScheme = colorScheme
        )
    }
}

@Composable
private fun LibraryHeader(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    viewType: LibraryViewType,
    onViewTypeChange: (LibraryViewType) -> Unit,
    sortType: LibrarySortType,
    onSortTypeChange: (LibrarySortType) -> Unit,
    showSortMenu: Boolean,
    onShowSortMenuChange: (Boolean) -> Unit,
    colorScheme: ColorScheme,
    currentFilter: LibraryFilter
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Library",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurface
        )

        Spacer(Modifier.width(24.dp))

        // Search Bar - Material 3 style
        Surface(
            modifier = Modifier.width(300.dp).height(48.dp),
            shape = RoundedCornerShape(24.dp),
            color = colorScheme.surfaceContainerHighest,
            shadowElevation = if (searchQuery.isNotEmpty()) 3.dp else 0.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
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
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = colorScheme.onSurface
                    ),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text(
                                "Search your library",
                                style = MaterialTheme.typography.bodyLarge,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                        innerTextField()
                    }
                )
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchChange("") },
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

        // Sort dropdown - only show when not in mix view
        if (currentFilter != LibraryFilter.LIBRARY) {
            Box {
                ElevatedButton(
                    onClick = { onShowSortMenuChange(true) },
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = colorScheme.surfaceContainer
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        sortType.displayName,
                        style = MaterialTheme.typography.labelLarge,
                        color = colorScheme.onSurface
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.Outlined.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { onShowSortMenuChange(false) }
                ) {
                    getSortOptionsForFilter(currentFilter).forEach { sort ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    sort.displayName,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            onClick = {
                                onSortTypeChange(sort)
                                onShowSortMenuChange(false)
                            },
                            leadingIcon = if (sortType == sort) {
                                { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }
            }

            Spacer(Modifier.width(12.dp))
        }

        // View type toggle
        Surface(
            color = colorScheme.surfaceContainer.copy(alpha = 0.6f),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(modifier = Modifier.padding(4.dp)) {
                ViewTypeButton(
                    selected = viewType == LibraryViewType.LIST,
                    onClick = { onViewTypeChange(LibraryViewType.LIST) },
                    icon = Icons.AutoMirrored.Filled.List,
                    colorScheme = colorScheme
                )
                ViewTypeButton(
                    selected = viewType == LibraryViewType.GRID,
                    onClick = { onViewTypeChange(LibraryViewType.GRID) },
                    icon = Icons.Default.GridView,
                    colorScheme = colorScheme
                )
            }
        }
    }
}

@Composable
private fun FilterChipsRow(
    currentFilter: LibraryFilter,
    onFilterChange: (LibraryFilter) -> Unit,
    colorScheme: ColorScheme
) {
    ChipsRow(
        chips = listOf(
            LibraryFilter.LIBRARY to "Mix",
            LibraryFilter.PLAYLISTS to "Playlists",
            LibraryFilter.SONGS to "Songs",
            LibraryFilter.ARTISTS to "Artists",
            LibraryFilter.ALBUMS to "Albums"
        ),
        currentValue = currentFilter,
        onValueUpdate = onFilterChange,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    )
}

@Composable
private fun LibraryContent(
    currentFilter: LibraryFilter,
    viewType: LibraryViewType,
    sortType: LibrarySortType,
    searchQuery: String,
    sections: Map<String, List<YTItem>>,
    colorScheme: ColorScheme
) {
    when (currentFilter) {
        LibraryFilter.LIBRARY -> LibraryMixContent(viewType, searchQuery, sections, colorScheme)
        LibraryFilter.PLAYLISTS -> LibraryPlaylistsContent(viewType, sortType, searchQuery, sections, colorScheme)
        LibraryFilter.SONGS -> LibrarySongsContent(viewType, sortType, searchQuery, sections, colorScheme)
        LibraryFilter.ARTISTS -> LibraryArtistsContent(viewType, sortType, searchQuery, sections, colorScheme)
        LibraryFilter.ALBUMS -> LibraryAlbumsContent(viewType, sortType, searchQuery, sections, colorScheme)
        LibraryFilter.PODCASTS -> LibraryPodcastsContent(viewType, sortType, searchQuery, sections, colorScheme)
    }
}

@Composable
private fun LibraryMixContent(
    viewType: LibraryViewType,
    searchQuery: String,
    sections: Map<String, List<YTItem>>,
    colorScheme: ColorScheme
) {
    val autoPlaylists = getAutoPlaylists()
    val allItems = sections.values.flatten()
    val filteredItems = if (searchQuery.isEmpty()) allItems
        else allItems.filter { it.title.contains(searchQuery, ignoreCase = true) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (viewType == LibraryViewType.LIST) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Autogenerated playlists first
                items(autoPlaylists, key = { it.type }) { playlist ->
                    AutoPlaylistListItem(
                        playlist = playlist,
                        colorScheme = colorScheme
                    ) {
                        AppState.selectedLocalPlaylist = playlist.type
                    }
                }

                // Library content
                if (sections.isEmpty()) {
                    item {
                        if (AppState.isSignedIn) LoadingItem(colorScheme)
                        else SignInItem(colorScheme)
                    }
                } else if (filteredItems.isEmpty() && searchQuery.isNotEmpty()) {
                    item {
                        EmptySearchItem(searchQuery, colorScheme)
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
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                contentPadding = PaddingValues(bottom = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Autogenerated playlists first
                items(autoPlaylists, key = { it.type }) { playlist ->
                    AutoPlaylistGridItem(
                        playlist = playlist,
                        colorScheme = colorScheme
                    ) {
                        AppState.selectedLocalPlaylist = playlist.type
                    }
                }

                // Library content
                if (sections.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        if (AppState.isSignedIn) LoadingItem(colorScheme)
                        else SignInItem(colorScheme)
                    }
                } else if (filteredItems.isEmpty() && searchQuery.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EmptySearchItem(searchQuery, colorScheme)
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

@Composable
private fun LibraryPlaylistsContent(
    viewType: LibraryViewType,
    sortType: LibrarySortType,
    searchQuery: String,
    sections: Map<String, List<YTItem>>,
    colorScheme: ColorScheme
) {
    val autoPlaylists = getAutoPlaylists()
    val playlists = sections.values.flatten().filterIsInstance<PlaylistItem>()
    val sortedPlaylists = sortPlaylists(playlists, sortType)
    val filteredPlaylists = if (searchQuery.isEmpty()) sortedPlaylists
        else sortedPlaylists.filter { it.title.contains(searchQuery, ignoreCase = true) }

    if (viewType == LibraryViewType.LIST) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Auto playlists first
            items(autoPlaylists, key = { it.type }) { playlist ->
                AutoPlaylistListItem(playlist, colorScheme) {
                    AppState.selectedLocalPlaylist = playlist.type
                }
            }

            // Regular playlists
            items(filteredPlaylists) { playlist ->
                YTListItem(playlist, colorScheme) {
                    AppState.fetchPlaylistData(playlist.id, playlist.thumbnail)
                }
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 180.dp),
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            contentPadding = PaddingValues(bottom = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Auto playlists first
            items(autoPlaylists, key = { it.type }) { playlist ->
                AutoPlaylistGridItem(playlist, colorScheme) {
                    AppState.selectedLocalPlaylist = playlist.type
                }
            }

            // Regular playlists
            items(filteredPlaylists) { playlist ->
                YTGridItem(playlist, colorScheme) {
                    AppState.fetchPlaylistData(playlist.id, playlist.thumbnail)
                }
            }
        }
    }
}

// Similar implementations for other filter types...
@Composable
private fun LibrarySongsContent(
    viewType: LibraryViewType,
    sortType: LibrarySortType,
    searchQuery: String,
    sections: Map<String, List<YTItem>>,
    colorScheme: ColorScheme
) {
    val songs = sections.values.flatten().filterIsInstance<SongItem>()
    val sortedSongs = sortSongs(songs, sortType)
    val filteredSongs = if (searchQuery.isEmpty()) sortedSongs
        else sortedSongs.filter { it.title.contains(searchQuery, ignoreCase = true) }

    if (viewType == LibraryViewType.LIST) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(filteredSongs) { song ->
                YTListItem(song, colorScheme) { AppState.playTrack(song) }
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 180.dp),
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            contentPadding = PaddingValues(bottom = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(filteredSongs) { song ->
                YTGridItem(song, colorScheme) { AppState.playTrack(song) }
            }
        }
    }
}

@Composable
private fun LibraryArtistsContent(
    viewType: LibraryViewType,
    sortType: LibrarySortType,
    searchQuery: String,
    sections: Map<String, List<YTItem>>,
    colorScheme: ColorScheme
) {
    val artists = sections.values.flatten().filterIsInstance<ArtistItem>()
    val sortedArtists = sortArtists(artists, sortType)
    val filteredArtists = if (searchQuery.isEmpty()) sortedArtists
        else sortedArtists.filter { it.title.contains(searchQuery, ignoreCase = true) }

    if (viewType == LibraryViewType.LIST) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(filteredArtists) { artist ->
                YTListItem(artist, colorScheme) {
                    AppState.selectedArtistId = artist.id
                    AppState.fetchArtistData(artist.id)
                }
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 180.dp),
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            contentPadding = PaddingValues(bottom = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(filteredArtists) { artist ->
                YTGridItem(artist, colorScheme) {
                    AppState.selectedArtistId = artist.id
                    AppState.fetchArtistData(artist.id)
                }
            }
        }
    }
}

@Composable
private fun LibraryAlbumsContent(
    viewType: LibraryViewType,
    sortType: LibrarySortType,
    searchQuery: String,
    sections: Map<String, List<YTItem>>,
    colorScheme: ColorScheme
) {
    val albums = sections.values.flatten().filterIsInstance<AlbumItem>()
    val sortedAlbums = sortAlbums(albums, sortType)
    val filteredAlbums = if (searchQuery.isEmpty()) sortedAlbums
        else sortedAlbums.filter { it.title.contains(searchQuery, ignoreCase = true) }

    if (viewType == LibraryViewType.LIST) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(filteredAlbums) { album ->
                YTListItem(album, colorScheme) {
                    val browseId = album.playlistId ?: album.id
                    AppState.selectedAlbumId = browseId
                    AppState.fetchAlbumData(browseId)
                }
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 180.dp),
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            contentPadding = PaddingValues(bottom = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(filteredAlbums) { album ->
                YTGridItem(album, colorScheme) {
                    val browseId = album.playlistId ?: album.id
                    AppState.selectedAlbumId = browseId
                    AppState.fetchAlbumData(browseId)
                }
            }
        }
    }
}

@Composable
private fun LibraryPodcastsContent(
    viewType: LibraryViewType,
    sortType: LibrarySortType,
    searchQuery: String,
    sections: Map<String, List<YTItem>>,
    colorScheme: ColorScheme
) {
    // Placeholder for podcasts - could be implemented later
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Outlined.Podcasts,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Podcasts coming soon",
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

// Auto playlist components
@Composable
private fun AutoPlaylistListItem(
    playlist: AutoPlaylist,
    colorScheme: ColorScheme,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(playlist.iconBackground()),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                playlist.icon,
                null,
                modifier = Modifier.size(28.dp),
                tint = playlist.iconTint()
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                playlist.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                playlist.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AutoPlaylistGridItem(
    playlist: AutoPlaylist,
    colorScheme: ColorScheme,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(bottom = 8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(playlist.iconBackground()),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                playlist.icon,
                null,
                modifier = Modifier.size(56.dp),
                tint = playlist.iconTint()
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            playlist.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            playlist.subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// Utility functions
@Composable
private fun getAutoPlaylists(): List<AutoPlaylist> {
    val colorScheme = MaterialTheme.colorScheme
    return listOfNotNull(
        if (AppState.showLikedPlaylist) {
            AutoPlaylist(
                type = "liked",
                title = "Liked Songs",
                subtitle = "${AppState.likedSongIds.size} songs",
                icon = Icons.Filled.Favorite,
                iconTint = { Color.White },
                iconBackground = { colorScheme.error },
                route = "auto_playlist/liked"
            )
        } else null,
        if (AppState.showDownloadedPlaylist) {
            AutoPlaylist(
                type = "downloaded",
                title = "Downloaded",
                subtitle = "Offline songs",
                icon = Icons.Outlined.Download,
                iconTint = { colorScheme.onSecondaryContainer },
                iconBackground = { colorScheme.secondaryContainer },
                route = "auto_playlist/downloaded"
            )
        } else null,
        if (AppState.showTopPlaylist) {
            AutoPlaylist(
                type = "top50",
                title = "My Top 50",
                subtitle = "${AppState.topSongs.size} songs",
                icon = Icons.AutoMirrored.Outlined.TrendingUp,
                iconTint = { colorScheme.onPrimaryContainer },
                iconBackground = { colorScheme.primaryContainer },
                route = "top_playlist/50"
            )
        } else null,
        if (AppState.showUploadedPlaylist) {
            AutoPlaylist(
                type = "uploaded",
                title = "Uploaded",
                subtitle = "Your uploaded songs",
                icon = Icons.Outlined.CloudUpload,
                iconTint = { colorScheme.onTertiaryContainer },
                iconBackground = { colorScheme.tertiaryContainer },
                route = "auto_playlist/uploaded"
            )
        } else null,
        if (AppState.showCachedPlaylist) {
            AutoPlaylist(
                type = "cached",
                title = "Cached",
                subtitle = "Cached songs",
                icon = Icons.Outlined.Storage,
                iconTint = { colorScheme.onSurfaceVariant },
                iconBackground = { colorScheme.surfaceVariant },
                route = "cache_playlist/cached"
            )
        } else null
    )
}

private fun getSortOptionsForFilter(filter: LibraryFilter): List<LibrarySortType> {
    return when (filter) {
        LibraryFilter.PLAYLISTS -> listOf(
            LibrarySortType.CREATE_DATE,
            LibrarySortType.NAME,
            LibrarySortType.LAST_UPDATED,
            LibrarySortType.SONG_COUNT
        )
        LibraryFilter.SONGS -> listOf(
            LibrarySortType.CREATE_DATE,
            LibrarySortType.NAME,
            LibrarySortType.ARTIST
        )
        LibraryFilter.ARTISTS, LibraryFilter.ALBUMS -> listOf(
            LibrarySortType.CREATE_DATE,
            LibrarySortType.NAME
        )
        else -> listOf(LibrarySortType.CREATE_DATE, LibrarySortType.NAME)
    }
}

private fun sortPlaylists(playlists: List<PlaylistItem>, sortType: LibrarySortType): List<PlaylistItem> {
    return when (sortType) {
        LibrarySortType.NAME -> playlists.sortedBy { it.title }
        LibrarySortType.SONG_COUNT -> playlists.sortedByDescending {
            it.songCount?.toIntOrNull() ?: 0
        }
        LibrarySortType.LAST_UPDATED, LibrarySortType.CREATE_DATE -> playlists.reversed()
        else -> playlists
    }
}

private fun sortSongs(songs: List<SongItem>, sortType: LibrarySortType): List<SongItem> {
    return when (sortType) {
        LibrarySortType.NAME -> songs.sortedBy { it.title }
        LibrarySortType.ARTIST -> songs.sortedBy { it.artists.firstOrNull()?.name ?: "" }
        LibrarySortType.CREATE_DATE -> songs.reversed()
        else -> songs
    }
}

private fun sortArtists(artists: List<ArtistItem>, sortType: LibrarySortType): List<ArtistItem> {
    return when (sortType) {
        LibrarySortType.NAME -> artists.sortedBy { it.title }
        LibrarySortType.CREATE_DATE -> artists.reversed()
        else -> artists
    }
}

private fun sortAlbums(albums: List<AlbumItem>, sortType: LibrarySortType): List<AlbumItem> {
    return when (sortType) {
        LibrarySortType.NAME -> albums.sortedBy { it.title }
        LibrarySortType.ARTIST -> albums.sortedBy { it.artists.firstOrNull()?.name ?: "" }
        LibrarySortType.CREATE_DATE -> albums.reversed()
        else -> albums
    }
}

private val LibrarySortType.displayName: String
    get() = when (this) {
        LibrarySortType.CREATE_DATE -> "Recently Added"
        LibrarySortType.NAME -> "Name"
        LibrarySortType.LAST_UPDATED -> "Recently Played"
        LibrarySortType.SONG_COUNT -> "Song Count"
        LibrarySortType.ARTIST -> "Artist"
    }

@Composable
private fun ViewTypeButton(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    colorScheme: ColorScheme
) {
    Surface(
        onClick = onClick,
        color = if (selected) colorScheme.primary else Color.Transparent,
        shape = RoundedCornerShape(16.dp),
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

// Helper components
@Composable
private fun LoadingItem(colorScheme: ColorScheme) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            Text("Loading your library...", color = colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SignInItem(colorScheme: ColorScheme) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
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
private fun EmptySearchItem(query: String, colorScheme: ColorScheme) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "No items found for \"$query\"",
            style = MaterialTheme.typography.bodyLarge,
            color = colorScheme.onSurfaceVariant
        )
    }
}
