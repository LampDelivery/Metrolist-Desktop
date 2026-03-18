package com.metrolist.desktop.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LibraryAddCheck
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metrolist.desktop.constants.ListItemHeight
import com.metrolist.desktop.constants.ListThumbnailSize
import com.metrolist.desktop.constants.ThumbnailCornerRadius
import com.metrolist.desktop.state.AppState
import com.metrolist.desktop.ui.theme.MetrolistShareIcon
import com.metrolist.shared.model.AlbumItem
import com.metrolist.shared.model.ArtistItem
import com.metrolist.shared.model.ArtistSource
import com.metrolist.shared.model.EpisodeItem
import com.metrolist.shared.model.PlaylistItem
import com.metrolist.shared.model.PodcastItem
import com.metrolist.shared.model.SongItem
import com.metrolist.shared.model.YTItem

/** Animated equalizer bars shown on currently playing tracks */
@Composable
fun NowPlayingBars(isPlaying: Boolean, color: Color, modifier: Modifier = Modifier) {
    if (!isPlaying) {
        Icon(Icons.Default.Pause, contentDescription = null, tint = color, modifier = modifier.size(20.dp))
        return
    }
    val infiniteTransition = rememberInfiniteTransition(label = "bars")
    val bar1 by infiniteTransition.animateFloat(0.3f, 1f, infiniteRepeatable(tween(400), RepeatMode.Reverse), label = "b1")
    val bar2 by infiniteTransition.animateFloat(1f, 0.3f, infiniteRepeatable(tween(500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "b2")
    val bar3 by infiniteTransition.animateFloat(0.5f, 1f, infiniteRepeatable(tween(350, easing = LinearEasing), RepeatMode.Reverse), label = "b3")

    Row(modifier.size(20.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceEvenly) {
        listOf(bar1, bar2, bar3).forEach { h ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight(h)
                    .background(color, RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
            )
        }
    }
}

/** Explicit content E rating badge */
@Composable
fun ExplicitBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(18.dp)
            .background(
                color = Color.White,
                shape = RoundedCornerShape(2.dp)
            )
            .border(
                width = 1.dp,
                color = Color.Gray,
                shape = RoundedCornerShape(2.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "E",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}

@Composable
fun YTGridItem(item: YTItem, colorScheme: ColorScheme, onClick: () -> Unit) {
    val shape = if (item is ArtistItem) CircleShape else RoundedCornerShape(ThumbnailCornerRadius)
    var showContextMenu by remember { mutableStateOf(false) }
    val hoverIS = remember { MutableInteractionSource() }
    val isHovered by hoverIS.collectIsHoveredAsState()
    val hoverAlpha by animateFloatAsState(if (isHovered) 1f else 0f, label = "hoverAlpha")

    val finalOnClick = {
        when (item) {
            is ArtistItem -> AppState.fetchArtistData(item.id)
            is PlaylistItem -> AppState.fetchPlaylistData(item.id, item.thumbnail)
            is AlbumItem -> AppState.fetchAlbumData(item.id)
            else -> onClick()
        }
    }

    val isCurrentTrack = item is SongItem && AppState.currentTrack?.id == item.id

    // Resolve thumbnail with source priority for ArtistItem - reactive to cache changes
    val thumbnail = if (item is ArtistItem) {
        val cacheKey = item.id
        val cached = AppState.artistIconCache[cacheKey]
        cached ?: item.thumbnail
    } else {
        item.thumbnail
    }

    // Trigger on-demand icon fetch for ArtistItems when cache is empty and non-YouTube source is selected
    if (item is ArtistItem && AppState.artistIconSource != ArtistSource.YOUTUBE) {
        LaunchedEffect(item.id, AppState.artistIconSource) {
            if (AppState.artistIconCache[item.id] == null) {
                AppState.fetchArtistIconOnDemand(item.id, item.title, AppState.artistIconSource)
            }
        }
    }

    Box {
        Column(
            modifier = Modifier
                .width(160.dp)
                .hoverable(hoverIS)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type == PointerEventType.Press && event.buttons.isSecondaryPressed) {
                                showContextMenu = true
                            }
                        }
                    }
                }
                .clickable(onClick = finalOnClick, indication = null, interactionSource = remember { MutableInteractionSource() }),
            horizontalAlignment = if (item is ArtistItem) Alignment.CenterHorizontally else Alignment.Start
        ) {
            Box {
                AsyncImage(
                    url = thumbnail ?: "",
                    modifier = Modifier.size(160.dp).shadow(8.dp, shape),
                    shape = shape
                )
                if (isCurrentTrack) {
                    Box(
                        modifier = Modifier.matchParentSize().clip(shape).background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        NowPlayingBars(AppState.isPlaying, Color.White)
                    }
                }
                // Hover overlay: play button + action icons (Android app style)
                if (!isCurrentTrack && item !is ArtistItem) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .alpha(hoverAlpha)
                            .clip(shape)
                            .background(Color.Black.copy(alpha = 0.6f)) // Android app alpha = 0.6f
                    ) {
                        // Play button positioning: center for all items
                        Box(
                            modifier = Modifier.align(Alignment.Center),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                onClick = {
                                    when (item) {
                                        is SongItem -> AppState.playTrack(item)
                                        is AlbumItem -> AppState.playAlbum(item.id)
                                        is PlaylistItem -> AppState.fetchPlaylistData(item.id, item.thumbnail)
                                        is PodcastItem -> {} // No action defined yet
                                        is EpisodeItem -> {} // No action defined yet
                                    }
                                },
                                shape = CircleShape,
                                color = colorScheme.primaryContainer,
                                modifier = Modifier.size(36.dp), // Android app size
                                shadowElevation = 0.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Outlined.PlayArrow,
                                        contentDescription = "Play",
                                        tint = colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp) // Android app icon size
                                    )
                                }
                            }
                        }

                        // Action icons in bottom right corner (only for songs)
                        if (item is SongItem) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(4.dp)
                            ) {
                                IconButton(
                                    onClick = { AppState.toggleLike(item) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Outlined.FavoriteBorder, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                                IconButton(
                                    onClick = { /* TODO: share */ },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(MetrolistShareIcon, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                                IconButton(
                                    onClick = { showContextMenu = true },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.MoreVert, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        } else {
                            // For albums/playlists/etc., show menu icon in top right corner to avoid overlap
                            IconButton(
                                onClick = { showContextMenu = true },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(32.dp)
                            ) {
                                Icon(Icons.Default.MoreVert, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                item.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = if (item is ArtistItem) TextAlign.Center else TextAlign.Start,
                color = if (isCurrentTrack) colorScheme.primary else colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )

            val subtitle = buildAnnotatedString {
                when (item) {
                    is SongItem -> {
                        item.artists.forEachIndexed { index, artist ->
                            artist.id?.let { pushStringAnnotation("ARTIST", it) }
                            withStyle(SpanStyle(color = if (artist.id != null) colorScheme.primary else colorScheme.onSurfaceVariant)) {
                                append(artist.name)
                            }
                            if (artist.id != null) pop()
                            if (index < item.artists.lastIndex || item.album != null) {
                                append(if (index < item.artists.lastIndex) ", " else " • ")
                            }
                        }
                        item.album?.let { album ->
                            album.id?.let { pushStringAnnotation("ALBUM", it) }
                            withStyle(SpanStyle(color = if (album.id != null) colorScheme.primary else colorScheme.onSurfaceVariant)) { append(album.name) }
                            if (album.id != null) pop()
                        }
                    }
                    is AlbumItem -> {
                        item.artists.forEachIndexed { index, artist ->
                            artist.id?.let { pushStringAnnotation("ARTIST", it) }
                            withStyle(SpanStyle(color = if (artist.id != null) colorScheme.primary else colorScheme.onSurfaceVariant)) { append(artist.name) }
                            if (artist.id != null) pop()
                            if (index < item.artists.lastIndex) append(", ")
                        }
                    }
                    is ArtistItem -> append("Artist")
                    is PlaylistItem -> append(item.author ?: "Playlist")
                    is PodcastItem -> append(item.author ?: "Podcast")
                    is EpisodeItem -> append(item.author?.name ?: "Episode")
                }
            }

            ClickableText(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = colorScheme.onSurfaceVariant,
                    textAlign = if (item is ArtistItem) TextAlign.Center else TextAlign.Start
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
                onClick = { offset ->
                    subtitle.getStringAnnotations("ARTIST", offset, offset).firstOrNull()?.let { AppState.fetchArtistData(it.item) }
                    subtitle.getStringAnnotations("ALBUM", offset, offset).firstOrNull()?.let { AppState.fetchAlbumData(it.item) }
                }
            )

            // Song state badges (for songs only) - compact horizontal layout for grid
            if (item is SongItem) {
                Row(
                    modifier = Modifier.padding(top = 4.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Liked icon
                    if (AppState.likedSongIds.contains(item.id)) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Liked",
                            tint = colorScheme.error,
                            modifier = Modifier.size(14.dp).padding(end = 2.dp)
                        )
                    }

                    // Explicit content badge
                    if (item.isExplicit) {
                        ExplicitBadge(modifier = Modifier.padding(end = 2.dp))
                    }

                    // Downloaded icon - demo placeholder
                    if (item.id.hashCode() % 5 == 0) {
                        Icon(
                            imageVector = Icons.Outlined.Download,
                            contentDescription = "Downloaded",
                            tint = colorScheme.primary,
                            modifier = Modifier.size(14.dp).padding(end = 2.dp)
                        )
                    }

                    // Library icon
                    if (AppState.likedSongIds.contains(item.id)) {
                        Icon(
                            imageVector = Icons.Outlined.LibraryAddCheck,
                            contentDescription = "In Library",
                            tint = colorScheme.tertiary,
                            modifier = Modifier.size(14.dp).padding(end = 2.dp)
                        )
                    }
                }
            }
        }

        // Context menus
        when (item) {
            is SongItem -> SongContextMenu(item, showContextMenu) { showContextMenu = false }
            is AlbumItem -> AlbumContextMenu(item, expanded = showContextMenu) { showContextMenu = false }
            is ArtistItem -> ArtistContextMenu(item, showContextMenu) { showContextMenu = false }
            is PlaylistItem -> PlaylistContextMenu(item, expanded = showContextMenu) { showContextMenu = false }
            else -> {}
        }
    }
}

@Composable
fun YTListItem(item: YTItem, colorScheme: ColorScheme, onClick: () -> Unit) {
    val shape = if (item is ArtistItem) CircleShape else RoundedCornerShape(ThumbnailCornerRadius)
    var showContextMenu by remember { mutableStateOf(false) }
    val hoverIS = remember { MutableInteractionSource() }
    val isHovered by hoverIS.collectIsHoveredAsState()
    val iconAlpha by animateFloatAsState(if (isHovered) 1f else 0f, label = "iconAlpha")

    val finalOnClick = {
        when (item) {
            is ArtistItem -> AppState.fetchArtistData(item.id)
            is PlaylistItem -> AppState.fetchPlaylistData(item.id, item.thumbnail)
            is AlbumItem -> AppState.fetchAlbumData(item.id)
            else -> onClick()
        }
    }

    val isCurrentTrack = item is SongItem && AppState.currentTrack?.id == item.id

    // Resolve thumbnail with source priority for ArtistItem - reactive to cache changes
    val thumbnail = if (item is ArtistItem) {
        val cacheKey = item.id
        val cached = AppState.artistIconCache[cacheKey]
        cached ?: item.thumbnail
    } else {
        item.thumbnail
    }

    // Trigger on-demand icon fetch for ArtistItems when cache is empty and non-YouTube source is selected
    if (item is ArtistItem && AppState.artistIconSource != ArtistSource.YOUTUBE) {
        LaunchedEffect(item.id, AppState.artistIconSource) {
            if (AppState.artistIconCache[item.id] == null) {
                AppState.fetchArtistIconOnDemand(item.id, item.title, AppState.artistIconSource)
            }
        }
    }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(ListItemHeight)
                .clip(RoundedCornerShape(8.dp))
                .hoverable(hoverIS)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type == PointerEventType.Press && event.buttons.isSecondaryPressed) {
                                showContextMenu = true
                            }
                        }
                    }
                }
                .clickable(onClick = finalOnClick)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                AsyncImage(url = thumbnail ?: "", modifier = Modifier.size(ListThumbnailSize), shape = shape)
                if (isCurrentTrack) {
                    Box(
                        modifier = Modifier.matchParentSize().clip(shape).background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        NowPlayingBars(AppState.isPlaying, Color.White, Modifier.size(ListThumbnailSize.times(0.5f)))
                    }
                }
                // Play button overlay on hover for songs/albums (Android app style)
                if (!isCurrentTrack && (item is SongItem || item is AlbumItem)) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .alpha(iconAlpha)
                            .clip(shape)
                            .background(Color.Black.copy(alpha = 0.6f)), // Android app alpha = 0.6f
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            onClick = {
                                when (item) {
                                    is SongItem -> AppState.playTrack(item)
                                    is AlbumItem -> AppState.playAlbum(item.id)
                                }
                            },
                            shape = CircleShape,
                            color = colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp), // Android app size
                            shadowElevation = 0.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Outlined.PlayArrow,
                                    contentDescription = "Play",
                                    tint = colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp) // Android app icon size
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isCurrentTrack) colorScheme.primary else colorScheme.onSurface
                )

                val subtitle = buildAnnotatedString {
                    when (item) {
                        is SongItem -> {
                            item.artists.forEachIndexed { index, artist ->
                                artist.id?.let { pushStringAnnotation("ARTIST", it) }
                                withStyle(SpanStyle(color = if (artist.id != null) colorScheme.primary else colorScheme.onSurfaceVariant)) { append(artist.name) }
                                if (artist.id != null) pop()
                                if (index < item.artists.lastIndex || item.album != null) {
                                    append(if (index < item.artists.lastIndex) ", " else " • ")
                                }
                            }
                            item.album?.let { album ->
                                album.id?.let { pushStringAnnotation("ALBUM", it) }
                                withStyle(SpanStyle(color = if (album.id != null) colorScheme.primary else colorScheme.onSurfaceVariant)) { append(album.name) }
                                if (album.id != null) pop()
                            }
                        }
                        is AlbumItem -> {
                            item.artists.forEachIndexed { index, artist ->
                                artist.id?.let { pushStringAnnotation("ARTIST", it) }
                                withStyle(SpanStyle(color = if (artist.id != null) colorScheme.primary else colorScheme.onSurfaceVariant)) { append(artist.name) }
                                if (artist.id != null) pop()
                                if (index < item.artists.lastIndex) append(", ")
                            }
                        }
                        is ArtistItem -> append("Artist")
                        is PlaylistItem -> append(item.author ?: "Playlist")
                        is PodcastItem -> append(item.author ?: "Podcast")
                        is EpisodeItem -> append(item.author?.name ?: "Episode")
                    }
                }

                ClickableText(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(color = colorScheme.onSurfaceVariant),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    onClick = { offset ->
                        subtitle.getStringAnnotations("ARTIST", offset, offset).firstOrNull()?.let { AppState.fetchArtistData(it.item) }
                        subtitle.getStringAnnotations("ALBUM", offset, offset).firstOrNull()?.let { AppState.fetchAlbumData(it.item) }
                    }
                )

                // Song state badges (for songs only)
                if (item is SongItem) {
                    Row(
                        modifier = Modifier.padding(top = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Liked icon - matches Android behavior
                        if (AppState.likedSongIds.contains(item.id)) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = "Liked",
                                tint = colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Explicit content badge
                        if (item.isExplicit) {
                            ExplicitBadge()
                        }

                        // Downloaded icon - placeholder for when download system is implemented
                        // TODO: Add actual download state checking when download system is ready
                        // For now, just show offline icon for demo purposes on some tracks
                        if (item.id.hashCode() % 5 == 0) { // Demo: show on every 5th track
                            Icon(
                                imageVector = Icons.Outlined.Download,
                                contentDescription = "Downloaded",
                                tint = colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Library icon - show for songs that are in user's library
                        // TODO: Add actual library state checking when library system is enhanced
                        // For now, treat liked songs as library songs
                        if (AppState.likedSongIds.contains(item.id)) {
                            Icon(
                                imageVector = Icons.Outlined.LibraryAddCheck,
                                contentDescription = "In Library",
                                tint = colorScheme.tertiary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            // Duration fades out when hovering to make room for action button
            if (item is SongItem && item.duration != null) {
                Text(
                    text = formatListDuration(item.duration!!),
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(end = 4.dp).widthIn(min = 36.dp).alpha(1f - iconAlpha),
                    textAlign = TextAlign.End
                )
            }
            // Three dots — visible only on hover
            IconButton(
                onClick = { showContextMenu = true },
                modifier = Modifier.alpha(iconAlpha)
            ) {
                Icon(Icons.Default.MoreVert, null, tint = colorScheme.onSurfaceVariant)
            }
        }

        // Context menus
        when (item) {
            is SongItem -> SongContextMenu(item, showContextMenu) { showContextMenu = false }
            is AlbumItem -> AlbumContextMenu(item, expanded = showContextMenu) { showContextMenu = false }
            is ArtistItem -> ArtistContextMenu(item, showContextMenu) { showContextMenu = false }
            is PlaylistItem -> PlaylistContextMenu(item, expanded = showContextMenu) { showContextMenu = false }
            else -> {}
        }
    }
}

@Composable
fun SpeedDialGridItem(item: YTItem, onClick: () -> Unit) {
    val shape = if (item is ArtistItem) CircleShape else RoundedCornerShape(ThumbnailCornerRadius)

    val finalOnClick = {
        when (item) {
            is ArtistItem -> AppState.fetchArtistData(item.id)
            is PlaylistItem -> AppState.fetchPlaylistData(item.id, item.thumbnail)
            is AlbumItem -> AppState.fetchAlbumData(item.id)
            else -> onClick()
        }
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(shape)
            .clickable(onClick = finalOnClick)
    ) {
        AsyncImage(
            url = item.thumbnail ?: "",
            modifier = Modifier.fillMaxSize(),
            shape = shape
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.2f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.7f)
                        )
                    )
                )
        )

        Text(
            text = item.title,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun formatListDuration(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "%d:%02d".format(mins, secs)
}
