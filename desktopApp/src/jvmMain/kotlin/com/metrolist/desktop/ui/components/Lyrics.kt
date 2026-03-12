package com.metrolist.desktop.ui.components

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.clickable
// import androidx.compose.runtime.getValue
// import androidx.compose.animation.core.animateFloatAsState
// import androidx.compose.animation.core.tween
// import androidx.compose.ui.draw.graphicsLayer
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metrolist.desktop.state.AppState
import com.metrolist.desktop.state.GlobalYouTubeRepository
import com.metrolist.shared.model.LyricsEntry
import com.metrolist.shared.playback.MusicPlayer
import kotlinx.coroutines.launch

@Composable
fun DesktopLyricsView() {
    val track = AppState.currentTrack
    val scope = rememberCoroutineScope()
    var lyricsRaw by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Lyrics parsing logic (ported from Android)
    fun parseLrc(lyrics: String): List<LyricsEntry> {
        val regex = Regex("\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})] ?(.+)")
        return lyrics.lines().mapNotNull { line ->
            val match = regex.matchEntire(line)
            if (match != null) {
                val min = match.groupValues[1].toLong()
                val sec = match.groupValues[2].toLong()
                val ms = match.groupValues[3].padEnd(3, '0').toLong()
                val time = min * 60_000 + sec * 1000 + ms
                LyricsEntry(time, match.groupValues[4])
            } else null
        }.sortedBy { it.time }
    }

    LaunchedEffect(track?.id) {
        if (track != null) {
            loading = true
            error = null
            try {
                lyricsRaw = GlobalYouTubeRepository.instance.getLyrics(
                    title = track.title,
                    artist = track.artists.joinToString { it.name },
                    duration = track.duration,
                    album = track.album?.name
                )
            } catch (e: Exception) {
                error = "Failed to load lyrics"
            } finally {
                loading = false
            }
        } else {
            lyricsRaw = null
        }
    }

    val lyrics = remember(lyricsRaw) { lyricsRaw?.let { parseLrc(it) } ?: emptyList() }
    val player = AppState.player
    val currentPosition by player.currentPosition.collectAsState(0L)
    val lazyListState = rememberLazyListState()

    // Find the current line index
    val currentLineIndex = remember(currentPosition, lyrics) {
        lyrics.indexOfLast { it.time <= currentPosition }
    }

    // Fallback: Use animateScrollToItem (no smooth center scroll)
    LaunchedEffect(currentLineIndex) {
        if (currentLineIndex >= 0) {
            lazyListState.animateScrollToItem(currentLineIndex)
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            loading -> {
                CircularProgressIndicator()
            }
            error != null -> {
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }
            lyrics.isEmpty() -> {
                Text("No lyrics found", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 18.sp)
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    state = lazyListState
                ) {
                    itemsIndexed(lyrics) { i, entry ->
                        val isActive = i == currentLineIndex
                        Text(
                            text = entry.text,
                            fontSize = if (isActive) 22.sp else 18.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .padding(vertical = 2.dp)
                                .clickable { player.seekTo(entry.time) }
                        )
                    }
                }
            }
        }
    }
}
