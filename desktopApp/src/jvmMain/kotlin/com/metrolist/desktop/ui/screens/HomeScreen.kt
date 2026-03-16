package com.metrolist.desktop.ui.screens

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.metrolist.shared.model.*
import com.metrolist.desktop.state.AppState
import com.metrolist.desktop.ui.components.ChipsRow
import com.metrolist.desktop.ui.components.NavigationTitle
import com.metrolist.desktop.ui.components.YTGridItem
import com.metrolist.desktop.ui.components.YTListItem

@Composable
fun HomeScreen(colorScheme: ColorScheme) {
    val homePageData = AppState.homePageData
    val isLoading = AppState.isHomeLoading
    val selectedChip = AppState.selectedChip

    if (isLoading && homePageData.sections.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = colorScheme.primary)
                Spacer(Modifier.height(16.dp))
                Text("Fetching your music...", color = colorScheme.onSurfaceVariant)
            }
        }
    } else {
        val lazyListState = rememberLazyListState()

        LaunchedEffect(lazyListState) {
            snapshotFlow {
                if (lazyListState.firstVisibleItemIndex > 0) Int.MAX_VALUE
                else lazyListState.firstVisibleItemScrollOffset
            }.collect { AppState.topBarScrollOffset = it }
        }

        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 48.dp, bottom = 32.dp)
        ) {
            // Chips Row
            item {
                ChipsRow(
                    chips = homePageData.chips.map { it to it.title },
                    currentValue = selectedChip,
                    onValueUpdate = { AppState.toggleChip(it) }
                )
            }

            // Sections
            homePageData.sections.forEachIndexed { index, section ->
                val sectionSongs = section.items.filterIsInstance<SongItem>()
                val isSongsOnly = section.items.isNotEmpty() && section.items.all { it is SongItem }

                item(key = "section_$index") {
                    if (section.isListStyle || isSongsOnly) {
                        SongsGridSection(section, sectionSongs, colorScheme)
                    } else {
                        CarouselSection(section, sectionSongs, colorScheme)
                    }
                }
            }
        }
    }
}

@Composable
private fun CarouselSection(
    section: HomeSection,
    sectionSongs: List<SongItem>,
    colorScheme: ColorScheme
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    NavigationTitle(
        title = section.title,
        label = section.subtitle,
        onPlayAllClick = if (sectionSongs.isNotEmpty()) {
            { AppState.playTrack(sectionSongs.first(), sectionSongs) }
        } else null,
        onClick = if (section.browseId != null) { {} } else null,
        canScrollBack = listState.canScrollBackward,
        canScrollForward = listState.canScrollForward,
        onScrollBack = { scope.launch { listState.animateScrollBy(-480f) } },
        onScrollForward = { scope.launch { listState.animateScrollBy(480f) } }
    )

    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(section.items) { item ->
            YTGridItem(item, colorScheme) {
                if (item is SongItem) AppState.playTrack(item, sectionSongs.ifEmpty { null })
            }
        }
    }

    Spacer(Modifier.height(24.dp))
}

@Composable
private fun SongsGridSection(
    section: HomeSection,
    songs: List<SongItem>,
    colorScheme: ColorScheme
) {
    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()

    NavigationTitle(
        title = section.title,
        label = section.subtitle,
        onPlayAllClick = if (songs.isNotEmpty()) {
            { AppState.playTrack(songs.first(), songs) }
        } else null,
        onClick = if (section.browseId != null) { {} } else null,
        canScrollBack = gridState.canScrollBackward,
        canScrollForward = gridState.canScrollForward,
        onScrollBack = { scope.launch { gridState.animateScrollBy(-560f) } },
        onScrollForward = { scope.launch { gridState.animateScrollBy(560f) } }
    )

    LazyHorizontalGrid(
        rows = GridCells.Fixed(
            when {
                songs.size <= 4 -> 1
                songs.size <= 8 -> 2
                songs.size <= 16 -> 4
                else -> 5
            }
        ),
        state = gridState,
        modifier = Modifier
            .fillMaxWidth()
            .height(
                when {
                    songs.size <= 4 -> 68.dp
                    songs.size <= 8 -> 140.dp
                    songs.size <= 16 -> 276.dp
                    else -> 344.dp
                }
            ),
        contentPadding = PaddingValues(horizontal = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(songs) { song ->
            Box(Modifier.width(280.dp)) {
                YTListItem(song, colorScheme) {
                    AppState.playTrack(song, songs)
                }
            }
        }
    }

    Spacer(Modifier.height(24.dp))
}
