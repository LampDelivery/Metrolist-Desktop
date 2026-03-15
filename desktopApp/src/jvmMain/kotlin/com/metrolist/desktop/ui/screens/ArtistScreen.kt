package com.metrolist.desktop.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    val lazyListState = rememberLazyListState()

    LaunchedEffect(lazyListState) {
        snapshotFlow {
            if (lazyListState.firstVisibleItemIndex > 0) Int.MAX_VALUE
            else lazyListState.firstVisibleItemScrollOffset
        }.collect { AppState.topBarScrollOffset = it }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(state = lazyListState, modifier = Modifier.fillMaxSize()) {

            // ── Header with banner ──────────────────────────────────────────────
            item {
                Box(modifier = Modifier.fillMaxWidth().height(340.dp)) {
                    val bannerUrl = artistInfo?.banner ?: artistInfo?.thumbnail
                    if (bannerUrl != null) {
                        AsyncImage(
                            url = bannerUrl,
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(0.dp)
                        )
                    } else {
                        Box(Modifier.fillMaxSize().background(colorScheme.surfaceVariant))
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

                    // Back button — positioned below the transparent topbar (48dp)
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
                    // Artist info
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
                            // Subscribe toggle button (matching Android style)
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

                            // Radio button
                            OutlinedButton(
                                onClick = { AppState.startArtistRadio() },
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.height(40.dp)
                            ) {
                                Icon(Icons.Outlined.Radio, null, modifier = Modifier.size(17.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Radio")
                            }

                            // Shuffle button
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

            // ── Artist description ─────────────────────────────────────────────
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

            // ── Content sections ───────────────────────────────────────────────
            AppState.artistData.forEach { (sectionTitle, sectionItems) ->
                if (sectionTitle == "header") return@forEach
                item(key = sectionTitle) {
                    Column(modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)) {
                        Text(
                            sectionTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurface
                        )
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

