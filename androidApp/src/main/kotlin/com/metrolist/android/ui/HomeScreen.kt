package com.metrolist.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.metrolist.android.HomeViewModel
import com.metrolist.music.playback.LocalPlayerConnection
import com.metrolist.shared.model.AlbumItem
import com.metrolist.shared.model.ArtistItem
import com.metrolist.shared.model.HomeSection
import com.metrolist.shared.model.PlaylistItem
import com.metrolist.shared.model.SongItem
import com.metrolist.shared.model.YTItem

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToAlbum: (String) -> Unit = {},
    onNavigateToArtist: (String) -> Unit = {},
    onNavigateToPlaylist: (String) -> Unit = {},
) {
    val isLoading by viewModel.isLoading
    val homeData by viewModel.homePageData
    val error by viewModel.errorMessage
    val selectedChip by viewModel.selectedChip
    val playerConnection = LocalPlayerConnection.current

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading && homeData.sections.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Error: $error",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    TextButton(onClick = { viewModel.loadHome() }) {
                        Text("Retry")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp) // Space for mini player
            ) {
                // Chips Row
                if (homeData.chips.isNotEmpty()) {
                    item {
                        ChipsRow(
                            chips = homeData.chips,
                            selectedChip = selectedChip,
                            onChipSelected = { viewModel.toggleChip(it) }
                        )
                    }
                }

                // Sections
                homeData.sections
                    .filter { viewModel.shouldShowSection(it) }
                    .forEachIndexed { index, section ->
                        val sectionSongs = viewModel.getSectionSongs(section)
                        val isSongsOnly = viewModel.isSongsOnlySection(section)

                        item(key = "section_${section.title}_$index") {
                            if (section.isListStyle || isSongsOnly) {
                                SongsGridSection(
                                    section = section,
                                    songs = sectionSongs,
                                    onPlayAllClick = {
                                        if (sectionSongs.isNotEmpty()) {
                                            // Play first song with queue
                                            playerConnection?.playQueue(sectionSongs, 0)
                                        }
                                    },
                                    onSongClick = { song ->
                                        val idx = sectionSongs.indexOf(song)
                                        playerConnection?.playQueue(sectionSongs, idx.coerceAtLeast(0))
                                    },
                                    onItemClick = { item ->
                                        when (item) {
                                            is SongItem -> {
                                                val idx = sectionSongs.indexOf(item)
                                                playerConnection?.playQueue(sectionSongs, idx.coerceAtLeast(0))
                                            }
                                            is AlbumItem -> onNavigateToAlbum(item.id)
                                            is ArtistItem -> onNavigateToArtist(item.id)
                                            is PlaylistItem -> onNavigateToPlaylist(item.id)
                                            else -> {}
                                        }
                                    }
                                )
                            } else {
                                CarouselSection(
                                    section = section,
                                    onItemClick = { item ->
                                        when (item) {
                                            is SongItem -> playerConnection?.playSong(item, "")
                                            is AlbumItem -> onNavigateToAlbum(item.id)
                                            is ArtistItem -> onNavigateToArtist(item.id)
                                            is PlaylistItem -> onNavigateToPlaylist(item.id)
                                            else -> {}
                                        }
                                    }
                                )
                            }
                        }
                    }
            }
        }
    }
}

@Composable
private fun ChipsRow(
    chips: List<com.metrolist.shared.model.Chip>,
    selectedChip: com.metrolist.shared.model.Chip?,
    onChipSelected: (com.metrolist.shared.model.Chip) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(chips) { chip ->
            val isSelected = selectedChip?.title == chip.title
            TextButton(
                onClick = { onChipSelected(chip) },
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp)),
                colors = if (isSelected) {
                    androidx.compose.material3.ButtonDefaults.textButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else {
                    androidx.compose.material3.ButtonDefaults.textButtonColors()
                }
            ) {
                Text(
                    text = chip.title,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun CarouselSection(
    section: HomeSection,
    onItemClick: (YTItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Section Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                section.subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            section.browseId?.let {
                IconButton(onClick = { /* Navigate to browse */ }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "See all"
                    )
                }
            }
        }

        // Horizontal scrollable items
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(section.items) { item ->
        YTGridItem(
                    item = item,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun SongsGridSection(
    section: HomeSection,
    songs: List<SongItem>,
    onPlayAllClick: () -> Unit,
    onSongClick: (SongItem) -> Unit,
    onItemClick: (YTItem) -> Unit
) {
    val gridState = rememberLazyGridState()

    Column(modifier = Modifier.fillMaxWidth()) {
        // Section Title with play button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                section.subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (songs.isNotEmpty()) {
                FilledTonalIconButton(onClick = onPlayAllClick) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play all"
                    )
                }
            }
        }

        // Horizontal grid of songs
        val rowCount = when {
            songs.size <= 4 -> 1
            songs.size <= 8 -> 2
            songs.size <= 16 -> 4
            else -> 5
        }

        LazyHorizontalGrid(
            rows = GridCells.Fixed(rowCount),
            state = gridState,
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    when (rowCount) {
                        1 -> 72.dp
                        2 -> 144.dp
                        4 -> 288.dp
                        else -> 360.dp
                    }
                ),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(songs) { song ->
                SongListItemCompact(
                    song = song,
                    onClick = { onSongClick(song) }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun YTGridItem(
    item: YTItem,
    onClick: () -> Unit
) {
    val title = item.title
    val subtitle = when (item) {
        is SongItem -> item.artists.joinToString(", ") { it.name }
        is AlbumItem -> item.artists.joinToString(", ") { it.name }
        is ArtistItem -> item.subscribers ?: "Artist"
        is PlaylistItem -> item.author ?: "Playlist"
        else -> ""
    }
    val thumbnail = item.thumbnail ?: ""

    Column(
        modifier = Modifier
            .width(140.dp)
            .padding(4.dp)
    ) {
        Card(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            AsyncImage(
                model = thumbnail,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SongListItemCompact(
    song: SongItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .width(280.dp)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail
        Card(
            onClick = onClick,
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            AsyncImage(
                model = song.thumbnail,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = song.artists.joinToString(", ") { it.name },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
