package com.metrolist.desktop.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.metrolist.shared.model.*
import com.metrolist.desktop.state.AppState
import com.metrolist.desktop.ui.components.ChipsRow
import com.metrolist.desktop.ui.components.NavigationTitle
import com.metrolist.desktop.ui.components.YTListItem

@Composable
fun SearchResultsList(colorScheme: ColorScheme) {
    val searchSummary = AppState.searchSummaryPage
    val searchResult = AppState.searchResultPage
    val selectedFilter = AppState.selectedSearchFilter
    val isLoading = AppState.isSearchLoading
    val lazyListState = rememberLazyListState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Filter Chips
        ChipsRow(
            chips = listOf(
                null to "All",
                SearchFilter.SONG to "Songs",
                SearchFilter.VIDEO to "Videos",
                SearchFilter.ALBUM to "Albums",
                SearchFilter.ARTIST to "Artists",
                SearchFilter.FEATURED_PLAYLIST to "Featured Playlists",
                SearchFilter.COMMUNITY_PLAYLIST to "Community Playlists",
            ),
            currentValue = selectedFilter,
            onValueUpdate = { AppState.updateSearchFilter(it) }
        )

        Box(modifier = Modifier.weight(1f)) {
            if (isLoading && searchSummary == null && searchResult == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colorScheme.primary)
                }
            } else if (selectedFilter == null) {
                // Summary View (All)
                if (searchSummary == null || searchSummary.summaries.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No results found", color = colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        searchSummary.summaries.forEach { summary ->
                            item {
                                NavigationTitle(
                                    title = summary.title,
                                    onClick = {
                                        val filter = when (summary.title) {
                                            "Songs" -> SearchFilter.SONG
                                            "Videos" -> SearchFilter.VIDEO
                                            "Albums" -> SearchFilter.ALBUM
                                            "Artists" -> SearchFilter.ARTIST
                                            "Community playlists" -> SearchFilter.COMMUNITY_PLAYLIST
                                            "Featured playlists" -> SearchFilter.FEATURED_PLAYLIST
                                            else -> null
                                        }
                                        if (filter != null) AppState.updateSearchFilter(filter)
                                    }
                                )
                            }

                            items(summary.items) { item ->
                                val sectionSongs = summary.items.filterIsInstance<SongItem>()
                                Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                                    YTListItem(item, colorScheme) {
                                        if (item is SongItem) {
                                            AppState.playTrack(item, sectionSongs)
                                        } else if (item is AlbumItem) {
                                            AppState.fetchAlbumData(item.id)
                                        } else if (item is ArtistItem) {
                                            AppState.fetchArtistData(item.id)
                                        } else if (item is PlaylistItem) {
                                            AppState.fetchPlaylistData(item.id)
                                        }
                                    }
                                }
                            }
                            
                            item {
                                Spacer(Modifier.height(16.dp))
                            }
                        }
                    }
                }
            } else {
                // Filtered View
                if (searchResult == null || searchResult.items.isEmpty()) {
                    if (!isLoading) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No results found", color = colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        items(searchResult.items) { item ->
                            val sectionSongs = searchResult.items.filterIsInstance<SongItem>()
                            Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                                YTListItem(item, colorScheme) {
                                    if (item is SongItem) {
                                        AppState.playTrack(item, sectionSongs)
                                    } else if (item is AlbumItem) {
                                        AppState.fetchAlbumData(item.id)
                                    } else if (item is ArtistItem) {
                                        AppState.fetchArtistData(item.id)
                                    } else if (item is PlaylistItem) {
                                        AppState.fetchPlaylistData(item.id)
                                    }
                                }
                            }
                        }
                        
                        if (isLoading) {
                            item {
                                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
