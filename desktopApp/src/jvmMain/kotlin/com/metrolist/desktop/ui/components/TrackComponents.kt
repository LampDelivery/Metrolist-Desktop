package com.metrolist.desktop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

@Composable
fun YTGridItem(item: YTItem, colorScheme: ColorScheme, onClick: () -> Unit) {
    val shape = if (item is ArtistItem) CircleShape else RoundedCornerShape(ThumbnailCornerRadius)
    
    val finalOnClick = {
        when (item) {
            is ArtistItem -> AppState.fetchArtistData(item.id)
            is PlaylistItem -> AppState.fetchPlaylistData(item.id)
            is AlbumItem -> AppState.fetchAlbumData(item.id)
            else -> onClick()
        }
    }

    Column(
        modifier = Modifier.width(160.dp).clickable(onClick = finalOnClick, indication = null, interactionSource = remember { MutableInteractionSource() }),
        horizontalAlignment = if (item is ArtistItem) Alignment.CenterHorizontally else Alignment.Start
    ) {
        AsyncImage(
            url = item.thumbnail ?: "",
            modifier = Modifier.size(160.dp).shadow(8.dp, shape),
            shape = shape
        )
        Spacer(Modifier.height(12.dp))
        Text(
            item.title, 
            style = MaterialTheme.typography.bodyLarge, 
            fontWeight = FontWeight.Medium, 
            maxLines = 1, 
            overflow = TextOverflow.Ellipsis,
            textAlign = if (item is ArtistItem) TextAlign.Center else TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
        
        val subtitle = buildAnnotatedString {
            when (item) {
                is SongItem -> {
                    item.artists.forEachIndexed { index, artist ->
                        artist.id?.let { id ->
                            pushStringAnnotation("ARTIST", id)
                        }
                        withStyle(SpanStyle(color = if (artist.id != null) colorScheme.primary else colorScheme.onSurfaceVariant)) {
                            append(artist.name)
                        }
                        if (artist.id != null) pop()
                        
                        if (index < item.artists.lastIndex || item.album != null) {
                            append(if (index < item.artists.lastIndex) ", " else " • ")
                        }
                    }
                    item.album?.let { album ->
                        album.id?.let { id ->
                            pushStringAnnotation("ALBUM", id)
                        }
                        withStyle(SpanStyle(color = if (album.id != null) colorScheme.primary else colorScheme.onSurfaceVariant)) {
                            append(album.name)
                        }
                        if (album.id != null) pop()
                    }
                }
                is AlbumItem -> {
                    item.artists.forEachIndexed { index, artist ->
                        artist.id?.let { id ->
                            pushStringAnnotation("ARTIST", id)
                        }
                        withStyle(SpanStyle(color = if (artist.id != null) colorScheme.primary else colorScheme.onSurfaceVariant)) {
                            append(artist.name)
                        }
                        if (artist.id != null) pop()
                        if (index < item.artists.lastIndex) append(", ")
                    }
                }
                is ArtistItem -> append("Artist")
                is PlaylistItem -> append(item.author ?: "Playlist")
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
                subtitle.getStringAnnotations("ARTIST", offset, offset).firstOrNull()?.let { annotation ->
                    AppState.fetchArtistData(annotation.item)
                }
                subtitle.getStringAnnotations("ALBUM", offset, offset).firstOrNull()?.let { annotation ->
                    AppState.fetchAlbumData(annotation.item)
                }
            }
        )
    }
}

@Composable
fun YTListItem(item: YTItem, colorScheme: ColorScheme, onClick: () -> Unit) {
    val shape = if (item is ArtistItem) CircleShape else RoundedCornerShape(ThumbnailCornerRadius)
    
    val finalOnClick = {
        when (item) {
            is ArtistItem -> AppState.fetchArtistData(item.id)
            is PlaylistItem -> AppState.fetchPlaylistData(item.id)
            is AlbumItem -> AppState.fetchAlbumData(item.id)
            else -> onClick()
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth().height(ListItemHeight).clip(RoundedCornerShape(8.dp)).clickable(onClick = finalOnClick).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(url = item.thumbnail ?: "", modifier = Modifier.size(ListThumbnailSize), shape = shape)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(item.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            
            val subtitle = buildAnnotatedString {
                when (item) {
                    is SongItem -> {
                        item.artists.forEachIndexed { index, artist ->
                            artist.id?.let { id ->
                                pushStringAnnotation("ARTIST", id)
                            }
                            withStyle(SpanStyle(color = if (artist.id != null) colorScheme.primary else colorScheme.onSurfaceVariant)) {
                                append(artist.name)
                            }
                            if (artist.id != null) pop()

                            if (index < item.artists.lastIndex || item.album != null) {
                                append(if (index < item.artists.lastIndex) ", " else " • ")
                            }
                        }
                        item.album?.let { album ->
                            album.id?.let { id ->
                                pushStringAnnotation("ALBUM", id)
                            }
                            withStyle(SpanStyle(color = if (album.id != null) colorScheme.primary else colorScheme.onSurfaceVariant)) {
                                append(album.name)
                            }
                            if (album.id != null) pop()
                        }
                    }
                    is AlbumItem -> {
                        item.artists.forEachIndexed { index, artist ->
                            artist.id?.let { id ->
                                pushStringAnnotation("ARTIST", id)
                            }
                            withStyle(SpanStyle(color = if (artist.id != null) colorScheme.primary else colorScheme.onSurfaceVariant)) {
                                append(artist.name)
                            }
                            if (artist.id != null) pop()
                            if (index < item.artists.lastIndex) append(", ")
                        }
                    }
                    is ArtistItem -> append("Artist")
                    is PlaylistItem -> append(item.author ?: "Playlist")
                }
            }

            ClickableText(
                text = subtitle, 
                style = MaterialTheme.typography.bodyMedium.copy(color = colorScheme.onSurfaceVariant), 
                maxLines = 1, 
                overflow = TextOverflow.Ellipsis,
                onClick = { offset ->
                    subtitle.getStringAnnotations("ARTIST", offset, offset).firstOrNull()?.let { annotation ->
                        AppState.fetchArtistData(annotation.item)
                    }
                    subtitle.getStringAnnotations("ALBUM", offset, offset).firstOrNull()?.let { annotation ->
                        AppState.fetchAlbumData(annotation.item)
                    }
                }
            )
        }
        IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, null, tint = colorScheme.onSurfaceVariant) }
    }
}

@Composable
fun SpeedDialGridItem(item: YTItem, colorScheme: ColorScheme, onClick: () -> Unit) {
    val shape = if (item is ArtistItem) CircleShape else RoundedCornerShape(ThumbnailCornerRadius)
    
    val finalOnClick = {
        when (item) {
            is ArtistItem -> AppState.fetchArtistData(item.id)
            is PlaylistItem -> AppState.fetchPlaylistData(item.id)
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
