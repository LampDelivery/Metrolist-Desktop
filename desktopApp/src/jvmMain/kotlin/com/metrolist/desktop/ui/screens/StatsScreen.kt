package com.metrolist.desktop.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.metrolist.desktop.state.AppState
import com.metrolist.desktop.ui.components.AsyncImage
import com.metrolist.desktop.ui.components.ChipsRow
import com.metrolist.desktop.ui.theme.MetrolistStatsIcon
import com.metrolist.desktop.utils.ArtistStat
import com.metrolist.desktop.utils.HistoryRepository
import com.metrolist.desktop.utils.HistoryStat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private enum class StatPeriod(val label: String, val days: Int?) {
    WEEK("Past week", 7),
    MONTH("Past month", 30),
    THREE_MONTHS("Past 3 months", 90),
    ALL("All time", null)
}

@Composable
fun StatsScreen(colorScheme: ColorScheme) {
    var period by remember { mutableStateOf(StatPeriod.MONTH) }
    var topSongs by remember { mutableStateOf<List<HistoryStat>>(emptyList()) }
    var topArtists by remember { mutableStateOf<List<ArtistStat>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(period) {
        isLoading = true
        val sinceMs = period.days?.let { System.currentTimeMillis() - it * 86_400_000L } ?: 0L
        val songs   = withContext(Dispatchers.IO) { HistoryRepository.getMostPlayed(20, sinceMs) }
        val artists = withContext(Dispatchers.IO) { HistoryRepository.getArtistStats(20, sinceMs) }
        topSongs   = songs
        topArtists = artists
        isLoading  = false
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Period chips
        ChipsRow(
            chips = StatPeriod.values().map { it to it.label },
            currentValue = period,
            onValueUpdate = { period = it }
        )

        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            topSongs.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(MetrolistStatsIcon, null, modifier = Modifier.size(64.dp),
                        tint = colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                    Spacer(Modifier.height(16.dp))
                    Text("No data for this period", color = colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.titleMedium)
                }
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                item {
                    Text(
                        "Most Played Songs",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                itemsIndexed(topSongs, key = { _, s -> s.songId }) { idx, stat ->
                    StatSongRow(idx + 1, stat, colorScheme)
                }
                if (topArtists.isNotEmpty()) {
                    item {
                        Text(
                            "Most Played Artists",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                        )
                    }
                    itemsIndexed(topArtists, key = { _, a -> a.name }) { idx, artist ->
                        ArtistStatRow(idx + 1, artist, colorScheme)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatSongRow(rank: Int, stat: HistoryStat, colorScheme: ColorScheme) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { AppState.playFromId(stat.songId) }
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank
        Text(
            "$rank",
            style = MaterialTheme.typography.labelMedium,
            color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.width(28.dp),
            fontWeight = FontWeight.Bold
        )
        AsyncImage(
            url = stat.thumbnailUrl ?: "",
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(6.dp))
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
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
            color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun ArtistStatRow(rank: Int, artist: ArtistStat, colorScheme: ColorScheme) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "$rank",
            style = MaterialTheme.typography.labelMedium,
            color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.width(28.dp),
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(12.dp))
        Text(
            artist.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            "${artist.playCount} plays",
            style = MaterialTheme.typography.labelSmall,
            color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}
