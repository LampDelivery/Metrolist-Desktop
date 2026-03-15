package com.metrolist.desktop.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.metrolist.desktop.state.AppState
import com.metrolist.desktop.ui.components.AsyncImage
import com.metrolist.desktop.utils.HistoryEntry
import com.metrolist.desktop.utils.HistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen(colorScheme: ColorScheme) {
    var entries by remember { mutableStateOf(AppState.historyEntries) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val fresh = withContext(Dispatchers.IO) { HistoryRepository.getRecentHistory(500) }
        entries = fresh
        isLoading = false
    }

    val grouped = remember(entries) { groupHistoryByDate(entries) }

    Box(Modifier.fillMaxSize()) {
        when {
            isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            entries.isEmpty() -> Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Outlined.History, null, modifier = Modifier.size(64.dp),
                    tint = colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                Spacer(Modifier.height(16.dp))
                Text("No play history yet", color = colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleMedium)
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                grouped.forEach { (header, sectionEntries) ->
                    item(key = header) {
                        Text(
                            header,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 16.dp, bottom = 4.dp)
                        )
                    }
                    itemsIndexed(
                        sectionEntries,
                        key = { _, e -> "${e.songId}_${e.playedAt}" }
                    ) { _, entry ->
                        HistoryEntryRow(entry, colorScheme)
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryEntryRow(entry: HistoryEntry, colorScheme: ColorScheme) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { AppState.playFromId(entry.songId) }
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            url = entry.thumbnailUrl ?: "",
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(6.dp))
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                entry.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                entry.artists,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            historyRelativeTime(entry.playedAt),
            style = MaterialTheme.typography.labelSmall,
            color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

private fun groupHistoryByDate(entries: List<HistoryEntry>): Map<String, List<HistoryEntry>> {
    val now = LocalDate.now()
    val tz = ZoneId.systemDefault()
    val monthFmt = DateTimeFormatter.ofPattern("MMMM yyyy")
    val result = LinkedHashMap<String, List<HistoryEntry>>()
    for (entry in entries) {
        val date = Instant.ofEpochMilli(entry.playedAt).atZone(tz).toLocalDate()
        val bucket = when {
            date == now                     -> "Today"
            date == now.minusDays(1)        -> "Yesterday"
            date.isAfter(now.minusDays(7))  -> "This Week"
            date.isAfter(now.minusDays(14)) -> "Last Week"
            else                            -> date.format(monthFmt)
        }
        @Suppress("UNCHECKED_CAST")
        (result.getOrPut(bucket) { mutableListOf<HistoryEntry>() } as MutableList<HistoryEntry>).add(entry)
    }
    return result
}

private fun historyRelativeTime(ms: Long): String {
    val diff = System.currentTimeMillis() - ms
    return when {
        diff < 60_000L     -> "just now"
        diff < 3_600_000L  -> "${diff / 60_000}m ago"
        diff < 86_400_000L -> "${diff / 3_600_000}h ago"
        else               -> "${diff / 86_400_000}d ago"
    }
}
