package com.metrolist.desktop.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.metrolist.shared.model.*
import com.metrolist.desktop.state.AppState
import com.metrolist.desktop.constants.ThumbnailCornerRadius
import com.metrolist.desktop.constants.ListItemHeight
import com.metrolist.desktop.constants.ListThumbnailSize
import com.metrolist.desktop.ui.theme.MetrolistShareIcon

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

    // Resolve thumbnail with source priority for ArtistItem
    val thumbnail = remember(item, AppState.artistIconSource) {
        if (item is ArtistItem) {
            val cacheKey = item.id
            val cached = AppState.artistIconCache[cacheKey]
            cached ?: item.thumbnail
        } else item.thumbnail
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
                // Hover overlay: dark scrim + action icons
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .alpha(hoverAlpha)
                        .clip(shape)
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Row(modifier = Modifier.padding(4.dp)) {
                        if (item is SongItem) {
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
                        }
                        IconButton(
                            onClick = { showContextMenu = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.MoreVert, null, tint = Color.White, modifier = Modifier.size(16.dp))
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

    // Resolve thumbnail with source priority for ArtistItem
    val thumbnail = remember(item, AppState.artistIconSource) {
        if (item is ArtistItem) {
            val cacheKey = item.id
            val cached = AppState.artistIconCache[cacheKey]
            cached ?: item.thumbnail
        } else item.thumbnail
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
