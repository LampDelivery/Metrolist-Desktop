package com.metrolist.desktop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.metrolist.desktop.state.AppState
import com.metrolist.desktop.ui.components.AsyncImage
import com.metrolist.desktop.utils.HistoryStat

// ───────────────────────────────────────────────────────────────── Router ──

@Composable
fun LocalPlaylistScreen(type: String, colorScheme: ColorScheme) {
    when (type) {
        "top50" -> Top50Screen(colorScheme)
        "downloaded" -> DownloadedScreen(colorScheme)
        "cached" -> CachedScreen(colorScheme)
        "uploaded" -> UploadedScreen(colorScheme)
    }
}

// ──────────────────────────────────────────────────────────── My Top 50 ──

@Composable
private fun Top50Screen(colorScheme: ColorScheme) {
    val songs = AppState.topSongs

    Column(Modifier.fillMaxSize()) {
        LocalPlaylistHeader(
            title = "My Top 50",
            subtitle = "${songs.size} songs • Based on your play history",
            icon = Icons.Outlined.Star,
            iconTint = colorScheme.onPrimaryContainer,
            iconBackground = colorScheme.primaryContainer,
            colorScheme = colorScheme
        ) {
            Button(
                onClick = { if (songs.isNotEmpty()) AppState.playFromId(songs.first().songId) },
                enabled = songs.isNotEmpty()
            ) {
                Icon(Icons.Outlined.PlayArrow, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Play")
            }
            OutlinedButton(
                onClick = { if (songs.isNotEmpty()) AppState.playFromId(songs.shuffled().first().songId) },
                enabled = songs.isNotEmpty()
            ) {
                Icon(Icons.Default.Shuffle, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Shuffle")
            }
        }

        if (songs.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.Star,
                message = "No songs yet",
                hint = "Play some music to build your top 50.",
                colorScheme = colorScheme
            )
        } else {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                itemsIndexed(songs, key = { _, s -> s.songId }) { idx, stat ->
                    RankedSongRow(rank = idx + 1, stat = stat, colorScheme = colorScheme)
                }
            }
        }
    }
}

// ───────────────────────────────────────────────────────────── Downloaded ──

@Composable
private fun DownloadedScreen(colorScheme: ColorScheme) {
    Column(Modifier.fillMaxSize()) {
        LocalPlaylistHeader(
            title = "Downloaded",
            subtitle = "Songs saved for offline listening",
            icon = Icons.Outlined.Download,
            iconTint = colorScheme.onSecondaryContainer,
            iconBackground = colorScheme.secondaryContainer,
            colorScheme = colorScheme
        )
        EmptyState(
            icon = Icons.Outlined.Download,
            message = "No downloaded songs",
            hint = "Download support is coming soon.\nDownloaded songs will be available here for offline playback.",
            colorScheme = colorScheme
        )
    }
}

// ────────────────────────────────────────────────────────────────── Cached ──

@Composable
private fun CachedScreen(colorScheme: ColorScheme) {
    Column(Modifier.fillMaxSize()) {
        LocalPlaylistHeader(
            title = "Cached",
            subtitle = "Songs cached while streaming",
            icon = Icons.Outlined.OfflinePin,
            iconTint = colorScheme.onTertiaryContainer,
            iconBackground = colorScheme.tertiaryContainer,
            colorScheme = colorScheme
        )
        EmptyState(
            icon = Icons.Outlined.OfflinePin,
            message = "No cached songs",
            hint = "Songs cached as a side effect of playback will appear here.\nCache browsing support is coming soon.",
            colorScheme = colorScheme
        )
    }
}

// ────────────────────────────────────────────────────────────────── Uploaded ──

@Composable
private fun UploadedScreen(colorScheme: ColorScheme) {
    Column(Modifier.fillMaxSize()) {
        LocalPlaylistHeader(
            title = "Uploaded",
            subtitle = "Songs you've uploaded to YouTube Music",
            icon = Icons.Outlined.CloudUpload,
            iconTint = colorScheme.onSurfaceVariant,
            iconBackground = colorScheme.surfaceVariant,
            colorScheme = colorScheme
        )
        EmptyState(
            icon = Icons.Outlined.CloudUpload,
            message = "No uploaded songs",
            hint = "Songs you upload to YouTube Music will appear here.\nRequires YouTube Music Premium.",
            colorScheme = colorScheme
        )
    }
}

// ─────────────────────────────────────────────────────── Shared components ──

@Composable
private fun LocalPlaylistHeader(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
    colorScheme: ColorScheme,
    actions: @Composable (RowScope.() -> Unit)? = null
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { AppState.selectedLocalPlaylist = null }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = colorScheme.onSurface)
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, modifier = Modifier.size(28.dp), tint = iconTint)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    title.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
        if (actions != null) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                content = actions
            )
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
    }
}

@Composable
private fun EmptyState(
    icon: ImageVector,
    message: String,
    hint: String,
    colorScheme: ColorScheme
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                icon, null,
                modifier = Modifier.size(52.dp),
                tint = colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
            )
            Spacer(Modifier.height(14.dp))
            Text(
                message,
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Text(
                hint,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RankedSongRow(rank: Int, stat: HistoryStat, colorScheme: ColorScheme) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { AppState.playFromId(stat.songId) }
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "$rank",
            style = MaterialTheme.typography.labelMedium,
            color = colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
            modifier = Modifier.width(28.dp),
            fontWeight = FontWeight.Bold
        )
        AsyncImage(
            url = stat.thumbnailUrl ?: "",
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(6.dp))
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                stat.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                stat.artists,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            "${stat.playCount} plays",
            style = MaterialTheme.typography.labelSmall,
            color = colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
        )
    }
}
