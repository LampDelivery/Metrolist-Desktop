package com.metrolist.desktop.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
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

                // Section Title
                item(key = "section_title_$index") {
                    NavigationTitle(
                        title = section.title,
                        label = section.subtitle,
                        onPlayAllClick = if (sectionSongs.isNotEmpty()) {
                            { AppState.playTrack(sectionSongs.first(), sectionSongs) }
                        } else null,
                        onClick = if (section.browseId != null) { {} } else null
                    )
                }

                // Section Content
                item(key = "section_content_$index") {
                    if (section.isListStyle || isSongsOnly) {
                        SongsGridWithNavigation(sectionSongs, colorScheme)
                    } else {
                        CarouselWithNavigation(section.items, colorScheme, sectionSongs)
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun CarouselWithNavigation(
    items: List<YTItem>,
    colorScheme: ColorScheme,
    sectionSongs: List<SongItem>
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Box {
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items) { item ->
                YTGridItem(item, colorScheme) {
                    if (item is SongItem) AppState.playTrack(item, sectionSongs.ifEmpty { null })
                }
            }
        }

        AnimatedVisibility(
            visible = listState.canScrollBackward,
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 6.dp),
            enter = fadeIn(), exit = fadeOut()
        ) {
            FilledTonalIconButton(
                onClick = { scope.launch { listState.animateScrollBy(-480f) } },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Outlined.ChevronLeft, null, modifier = Modifier.size(20.dp))
            }
        }

        AnimatedVisibility(
            visible = listState.canScrollForward,
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 6.dp),
            enter = fadeIn(), exit = fadeOut()
        ) {
            FilledTonalIconButton(
                onClick = { scope.launch { listState.animateScrollBy(480f) } },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Outlined.ChevronRight, null, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun SongsGridWithNavigation(
    songs: List<SongItem>,
    colorScheme: ColorScheme
) {
    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()

    Box {
        LazyHorizontalGrid(
            rows = GridCells.Fixed(4),
            state = gridState,
            modifier = Modifier
                .fillMaxWidth()
                .height(268.dp), // 4 × 64dp rows + 3 × 4dp gaps
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

        AnimatedVisibility(
            visible = gridState.canScrollBackward,
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 6.dp),
            enter = fadeIn(), exit = fadeOut()
        ) {
            FilledTonalIconButton(
                onClick = { scope.launch { gridState.animateScrollBy(-560f) } },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Outlined.ChevronLeft, null, modifier = Modifier.size(20.dp))
            }
        }

        AnimatedVisibility(
            visible = gridState.canScrollForward,
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 6.dp),
            enter = fadeIn(), exit = fadeOut()
        ) {
            FilledTonalIconButton(
                onClick = { scope.launch { gridState.animateScrollBy(560f) } },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Outlined.ChevronRight, null, modifier = Modifier.size(20.dp))
            }
        }
    }
}
