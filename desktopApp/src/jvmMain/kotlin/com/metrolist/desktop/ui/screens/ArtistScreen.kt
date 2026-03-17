package com.metrolist.desktop.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metrolist.shared.model.*
import com.metrolist.desktop.state.AppState
import com.metrolist.desktop.ui.components.AsyncImage
import com.metrolist.desktop.ui.components.YTGridItem
import com.metrolist.desktop.ui.components.YTListItem

@Composable
fun ArtistScreen(colorScheme: ColorScheme) {
    if (AppState.artistSectionTitle != null) {
        ArtistSectionScreen(colorScheme)
        return
    }

    if (AppState.isArtistLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = colorScheme.primary)
        }
        return
    }

    val artistInfo = AppState.artistData["header"]?.firstOrNull() as? ArtistItem
    val isSubscribed = AppState.artistIsSubscribed
    val hasSongs = AppState.artistData.entries.any { it.key != "header" && it.value.any { item -> item is SongItem } }
    var descriptionExpanded by remember { mutableStateOf(false) }
    var showAllPhotosFullScreen by remember { mutableStateOf(false) }
    val lazyListState = rememberLazyListState()

    LaunchedEffect(lazyListState) {
        snapshotFlow {
            if (lazyListState.firstVisibleItemIndex > 0) Int.MAX_VALUE
            else lazyListState.firstVisibleItemScrollOffset
        }.collect { AppState.topBarScrollOffset = it }
    }

    // Show either the full screen photos view or the regular artist screen
    if (showAllPhotosFullScreen) {
        ArtistPhotosFullScreen(
            onBack = { showAllPhotosFullScreen = false },
            colorScheme = colorScheme
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(state = lazyListState, modifier = Modifier.fillMaxSize()) {

            // ── Header with banner ──────────────────────────────────────────────
            item {
                Box(modifier = Modifier.fillMaxWidth().height(420.dp)) {
                    // Check cached banner first to prevent YouTube flash
                    val cachedBanner = artistInfo?.id?.let { AppState.artistBannerCache[it] }
                    val bannerUrl = if (cachedBanner != null && AppState.artistBannerSource != ArtistSource.YOUTUBE) {
                        cachedBanner
                    } else {
                        when (AppState.artistBannerSource) {
                            ArtistSource.YOUTUBE -> artistInfo?.banner ?: artistInfo?.thumbnail
                            ArtistSource.ITUNES -> AppState.artistPhotos.find { it.source == "iTunes" }?.url ?: (artistInfo?.banner ?: artistInfo?.thumbnail)
                            ArtistSource.LASTFM -> AppState.selectedLastFmPhotoUrl ?: (artistInfo?.banner ?: artistInfo?.thumbnail)
                            ArtistSource.SPOTIFY -> AppState.artistPhotos.find { it.source == "Spotify" }?.url ?: (artistInfo?.banner ?: artistInfo?.thumbnail)
                        }
                    }

                    Crossfade(
                        targetState = bannerUrl,
                        animationSpec = tween(durationMillis = 800),
                        modifier = Modifier.fillMaxSize(),
                        label = "BannerCrossfade"
                    ) { url ->
                        if (url != null) {
                            AsyncImage(
                                url = url,
                                modifier = Modifier.fillMaxSize(),
                                shape = RoundedCornerShape(0.dp)
                            )
                        } else {
                            Box(Modifier.fillMaxSize().background(colorScheme.surfaceVariant))
                        }
                    }

                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        colorScheme.background.copy(alpha = 0.7f),
                                        colorScheme.background
                                    ),
                                    startY = 200f
                                )
                            )
                    )

                    IconButton(
                        onClick = { AppState.selectedArtistId = null },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = colorScheme.surface.copy(alpha = 0.5f),
                            contentColor = colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 12.dp, top = 60.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 32.dp, vertical = 24.dp)
                    ) {
                        Text(
                            text = artistInfo?.title ?: "Unknown Artist",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            fontSize = 44.sp,
                            color = colorScheme.onSurface
                        )

                        if (artistInfo?.subscribers != null) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = artistInfo.subscribers!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (AppState.isSignedIn) {
                                OutlinedButton(
                                    onClick = { AppState.toggleArtistSubscription() },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isSubscribed)
                                            colorScheme.primaryContainer.copy(alpha = 0.85f)
                                        else
                                            Color.Transparent
                                    ),
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.height(40.dp)
                                ) {
                                    Icon(
                                        if (isSubscribed) Icons.Outlined.NotificationsNone else Icons.Outlined.AddAlert,
                                        null,
                                        modifier = Modifier.size(17.dp),
                                        tint = if (isSubscribed) colorScheme.onPrimaryContainer else colorScheme.primary
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = if (isSubscribed) "Subscribed" else "Subscribe",
                                        color = if (isSubscribed) colorScheme.onPrimaryContainer else colorScheme.primary
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = { AppState.startArtistRadio() },
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.height(40.dp)
                            ) {
                                Icon(Icons.Outlined.Radio, null, modifier = Modifier.size(17.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Radio")
                            }

                            IconButton(
                                onClick = { AppState.shuffleArtistSongs() },
                                enabled = hasSongs,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (hasSongs) colorScheme.secondaryContainer
                                        else colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    )
                            ) {
                                Icon(
                                    Icons.Default.Shuffle,
                                    null,
                                    tint = if (hasSongs) colorScheme.onSecondaryContainer
                                    else colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }
            }

            // ── Artist Photos Section (Last.fm images) ─────────────────────────
            if (AppState.showArtistPhotos && AppState.artistPhotos.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(vertical = 16.dp)) {
                        val lastFmCount = AppState.artistPhotos.count { it.source == "Last.fm" }
                        Row(
                            modifier = Modifier.padding(start = 32.dp, end = 32.dp, bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Artist Photos",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurfaceVariant
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (AppState.artistPhotos.size > 3) {
                                    TextButton(
                                        onClick = { showAllPhotosFullScreen = true },
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = colorScheme.primary
                                        )
                                    ) {
                                        Text("Show All", style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                                if (lastFmCount > 1) {
                                    IconButton(
                                        onClick = { AppState.toggleLastFmAutoCycle() },
                                        colors = IconButtonDefaults.iconButtonColors(
                                            contentColor = if (AppState.lastFmPhotoAutoCycle) colorScheme.primary else colorScheme.onSurfaceVariant
                                        )
                                    ) {
                                        Icon(Icons.Outlined.Sync, contentDescription = if (AppState.lastFmPhotoAutoCycle) "Stop cycling" else "Auto cycle photos")
                                    }
                                }
                            }
                        }
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 32.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(AppState.artistPhotos) { photo ->
                                val isBanner = when (photo.source) {
                                    "Last.fm" -> photo.url == AppState.selectedLastFmPhotoUrl
                                    "iTunes" -> AppState.artistBannerSource == ArtistSource.ITUNES &&
                                            AppState.artistPhotos.find { it.source == "iTunes" }?.url == photo.url
                                    "Spotify" -> AppState.artistBannerSource == ArtistSource.SPOTIFY &&
                                            AppState.artistPhotos.find { it.source == "Spotify" }?.url == photo.url
                                    else -> false
                                }

                                // Show checkmark only when recently manually selected (not during auto-cycle)
                                val showCheckmark = !AppState.lastFmPhotoAutoCycle &&
                                                  photo.url == AppState.recentlySelectedPhotoUrl

                                Box(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colorScheme.surfaceVariant)
                                        .clickable {
                                            when (photo.source) {
                                                "Last.fm" -> AppState.selectLastFmPhoto(photo.url)
                                                "iTunes" -> AppState.updateArtistBannerSource(ArtistSource.ITUNES)
                                                "Spotify" -> AppState.updateArtistBannerSource(ArtistSource.SPOTIFY)
                                            }
                                        }
                                ) {
                                    AsyncImage(
                                        url = photo.url,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    if (showCheckmark) {
                                        Box(
                                            Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Check, null, tint = Color.White)
                                        }
                                    }
                                    if (photo.source != "Last.fm") {
                                        Text(
                                            photo.source,
                                            modifier = Modifier.align(Alignment.BottomStart).padding(4.dp).background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (artistInfo?.description != null) {
                item {
                    val desc = artistInfo.description!!
                    val isLong = desc.length > 180
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 32.dp)
                            .padding(top = 4.dp, bottom = 16.dp)
                    ) {
                        Text(
                            text = "About",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant,
                            maxLines = if (descriptionExpanded) Int.MAX_VALUE else 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.animateContentSize()
                        )
                        if (isLong) {
                            TextButton(
                                onClick = { descriptionExpanded = !descriptionExpanded },
                                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    if (descriptionExpanded) "Show less" else "Show more",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 32.dp),
                        color = colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            AppState.artistData.forEach { (sectionTitle, sectionItems) ->
                if (sectionTitle == "header") return@forEach
                item(key = sectionTitle) {
                    Column(modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)) {
                        val browseLink = AppState.artistSectionBrowseIds[sectionTitle]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                sectionTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurface
                            )
                            if (browseLink?.first != null) {
                                Text(
                                    "See all",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = colorScheme.primary,
                                    modifier = Modifier.clickable {
                                        AppState.fetchArtistSection(sectionTitle, browseLink.first!!, browseLink.second)
                                    }
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))

                        if (sectionTitle == "Songs") {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                sectionItems.forEach { item ->
                                    YTListItem(item, colorScheme) {
                                        if (item is SongItem) AppState.playTrack(item)
                                    }
                                }
                            }
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                items(sectionItems) { item ->
                                    YTGridItem(item, colorScheme) {
                                        when (item) {
                                            is SongItem -> AppState.playTrack(item)
                                            is ArtistItem -> AppState.fetchArtistData(item.id)
                                            is AlbumItem -> AppState.fetchAlbumData(item.id)
                                            is PlaylistItem -> AppState.fetchPlaylistData(item.id)
                                            else -> {}
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}
}

@Composable
fun ArtistPhotosFullScreen(
    onBack: () -> Unit,
    colorScheme: ColorScheme
) {
    Column(modifier = Modifier.fillMaxSize().background(colorScheme.surface)) {
        // Header with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = colorScheme.onSurface
                )
            }
            Text(
                "Artist Photos",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Photos grid
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 280.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(AppState.artistPhotos) { photo ->
                val isBanner = when (photo.source) {
                    "Last.fm" -> photo.url == AppState.selectedLastFmPhotoUrl
                    "iTunes" -> AppState.artistBannerSource == ArtistSource.ITUNES &&
                            AppState.artistPhotos.find { it.source == "iTunes" }?.url == photo.url
                    "Spotify" -> AppState.artistBannerSource == ArtistSource.SPOTIFY &&
                            AppState.artistPhotos.find { it.source == "Spotify" }?.url == photo.url
                    else -> false
                }

                // Show checkmark only when recently manually selected (not during auto-cycle)
                val showCheckmark = !AppState.lastFmPhotoAutoCycle &&
                                  photo.url == AppState.recentlySelectedPhotoUrl

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clickable {
                            when (photo.source) {
                                "Last.fm" -> AppState.selectLastFmPhoto(photo.url)
                                "iTunes" -> AppState.updateArtistBannerSource(ArtistSource.ITUNES)
                                "Spotify" -> AppState.updateArtistBannerSource(ArtistSource.SPOTIFY)
                            }
                            onBack()
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (showCheckmark) colorScheme.primaryContainer else colorScheme.surface
                    ),
                    border = if (showCheckmark) BorderStroke(2.dp, colorScheme.primary) else null,
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            url = photo.url,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Source and checkmark overlay
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Only show source label if it's not Last.fm
                            if (photo.source != "Last.fm") {
                                Text(
                                    photo.source,
                                    modifier = Modifier
                                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (showCheckmark) {
                                Icon(
                                    Icons.Default.Check,
                                    "Recently selected",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(colorScheme.primary, CircleShape)
                                        .padding(4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtistSectionScreen(colorScheme: ColorScheme) {
    val sectionTitle = AppState.artistSectionTitle ?: return
    val items = AppState.artistSectionItems
    val loading = AppState.isArtistSectionLoading

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 60.dp, end = 16.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { AppState.clearArtistSection() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = colorScheme.onSurface)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = sectionTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
        }
        HorizontalDivider(color = colorScheme.surfaceVariant.copy(alpha = 0.5f))

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colorScheme.primary)
            }
        } else if (items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No items found", color = colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(items) { item ->
                    YTListItem(item, colorScheme) {
                        when (item) {
                            is SongItem -> AppState.playTrack(item)
                            is ArtistItem -> { AppState.clearArtistSection(); AppState.fetchArtistData(item.id) }
                            is AlbumItem -> AppState.fetchAlbumData(item.id)
                            is PlaylistItem -> AppState.fetchPlaylistData(item.id)
                            else -> {}
                        }
                    }
                }
            }
        }
    }
}
