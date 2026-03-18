package com.metrolist.desktop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AutoPlaylistThumbnail(
    iconIdentifier: String?,
    colorScheme: ColorScheme,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 160.dp
) {
    val (icon, iconTint, iconBackground) = when (iconIdentifier) {
        "auto_playlist_icon_liked" -> Triple(
            Icons.Filled.Favorite,
            Color.White,
            colorScheme.error
        )
        "auto_playlist_icon_downloaded" -> Triple(
            Icons.Outlined.Download,
            colorScheme.onSecondaryContainer,
            colorScheme.secondaryContainer
        )
        "auto_playlist_icon_top50" -> Triple(
            Icons.AutoMirrored.Outlined.TrendingUp,
            colorScheme.onPrimaryContainer,
            colorScheme.primaryContainer
        )
        "auto_playlist_icon_uploaded" -> Triple(
            Icons.Outlined.Backup,
            colorScheme.onTertiaryContainer,
            colorScheme.tertiaryContainer
        )
        "auto_playlist_icon_cached" -> Triple(
            Icons.Outlined.Storage,
            colorScheme.onSurfaceVariant,
            colorScheme.surfaceVariant
        )
        else -> Triple(
            Icons.AutoMirrored.Outlined.QueueMusic,
            colorScheme.onSurfaceVariant,
            colorScheme.surfaceVariant
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(iconBackground),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(size * 0.4f) // 40% of thumbnail size
        )
    }
}