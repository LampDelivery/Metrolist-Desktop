package com.metrolist.desktop.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metrolist.desktop.state.AppState
import com.metrolist.shared.model.LyricsEntry
import com.metrolist.shared.model.WordTimestamp

private val lineRegex = Regex("""^\[(\d{2}):(\d{2})\.(\d{2,3})]\s?(.*)$""")
private val wordTagRegex = Regex("""<(\d{1,2}):(\d{2})\.(\d{2,3})>\s*([^<]*)""")
// BetterLyrics next-line format: <word:startSecs:endSecs|word2:...>
private val betterLyricsLineRegex = Regex("""^<([^>]+)>$""")

private fun parseTimeMs(min: Long, sec: Long, msStr: String): Long {
    val millis = if (msStr.length == 3) msStr.toLong() else msStr.toLong() * 10
    return min * 60_000 + sec * 1000 + millis
}

private fun decodeHtmlEntities(text: String): String = text
    .replace("&amp;", "&")
    .replace("&lt;", "<")
    .replace("&gt;", ">")
    .replace("&quot;", "\"")
    .replace("&apos;", "'")
    .replace("&#x27;", "'")
    .replace("&#39;", "'")
    .replace("&nbsp;", "\u00A0")
    .replace("&#x2019;", "\u2019")
    .replace("&#x2018;", "\u2018")
    .replace("&#x201C;", "\u201C")
    .replace("&#x201D;", "\u201D")

/** Parse the BetterLyrics `<word:startSecs:endSecs|...>` next-line word format. */
private fun parseBetterLyricsWords(data: String): List<WordTimestamp>? {
    val parts = data.split("|")
    val words = parts.mapNotNull { segment ->
        val tokens = segment.split(":")
        if (tokens.size == 3) {
            val text = decodeHtmlEntities(tokens[0].trim())
            val startMs = (tokens[1].toDoubleOrNull() ?: return@mapNotNull null) * 1000.0
            val endMs   = (tokens[2].toDoubleOrNull() ?: return@mapNotNull null) * 1000.0
            if (text.isNotBlank()) WordTimestamp(text, startMs.toLong(), endMs.toLong()) else null
        } else null
    }
    return words.ifEmpty { null }
}

fun parseLrc(lyrics: String): List<LyricsEntry> {
    if (!lyrics.trimStart().startsWith("[")) return emptyList()
    val lines = lyrics.lines()
    val result = mutableListOf<LyricsEntry>()
    var i = 0
    while (i < lines.size) {
        val line = lines[i].trim()
        val match = lineRegex.matchEntire(line)
        if (match != null) {
            val lineTime = parseTimeMs(
                match.groupValues[1].toLong(),
                match.groupValues[2].toLong(),
                match.groupValues[3]
            )
            val rawText = decodeHtmlEntities(match.groupValues[4])

            // Check for inline rich-sync word tags: [mm:ss.ms]<mm:ss.ms> word...
            if (rawText.contains("<")) {
                val wordMatches = wordTagRegex.findAll(rawText).toList()
                val words = wordMatches.mapIndexedNotNull { idx, wm ->
                    val wordText = decodeHtmlEntities(wm.groupValues[4].trim())
                    if (wordText.isBlank()) null
                    else {
                        val wordStart = parseTimeMs(
                            wm.groupValues[1].toLong(),
                            wm.groupValues[2].toLong(),
                            wm.groupValues[3]
                        )
                        val wordEnd = wordMatches.getOrNull(idx + 1)?.let {
                            parseTimeMs(it.groupValues[1].toLong(), it.groupValues[2].toLong(), it.groupValues[3])
                        } ?: (wordStart + 500L)
                        WordTimestamp(wordText, wordStart, wordEnd)
                    }
                }
                val plainText = rawText.replace(Regex("""<\d{1,2}:\d{2}\.\d{2,3}>\s*"""), "").trim()
                result.add(LyricsEntry(
                    lineTime,
                    plainText.ifBlank { words.joinToString(" ") { it.text } },
                    words.ifEmpty { null }
                ))
            } else {
                // Check if the NEXT line is a BetterLyrics word-data line: <word:start:end|...>
                val nextLine = lines.getOrNull(i + 1)?.trim() ?: ""
                val betterWordsMatch = betterLyricsLineRegex.matchEntire(nextLine)
                val words = if (betterWordsMatch != null && nextLine.contains(":")) {
                    parseBetterLyricsWords(betterWordsMatch.groupValues[1]).also { i++ }
                } else null

                result.add(LyricsEntry(lineTime, rawText.trim(), words))
            }
        }
        i++
    }
    return result.sortedBy { it.time }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DesktopLyricsView() {
    val lyricsRaw = AppState.currentLyrics
    val loading = AppState.isLyricsLoading
    val provider = AppState.currentLyricsProvider
    val lyrics = remember(lyricsRaw) { lyricsRaw?.let { parseLrc(it) } ?: emptyList() }
    val currentPosition by AppState.player.currentPosition.collectAsState(0L)
    val lazyListState = rememberLazyListState()
    val colorScheme = MaterialTheme.colorScheme

    val currentLineIndex = remember(currentPosition, lyrics) {
        lyrics.indexOfLast { it.time <= currentPosition }
    }

    var isAutoScrollEnabled by remember { mutableStateOf(true) }
    var isAutoScrolling by remember { mutableStateOf(false) }
    val resyncTrigger = remember { mutableStateOf(0) }

    // Detect user-initiated scroll → disable auto-scroll
    LaunchedEffect(lazyListState.isScrollInProgress) {
        if (lazyListState.isScrollInProgress && !isAutoScrolling) {
            isAutoScrollEnabled = false
        }
    }

    // Auto-scroll when current line changes (if enabled)
    LaunchedEffect(currentLineIndex) {
        if (currentLineIndex >= 0 && isAutoScrollEnabled) {
            isAutoScrolling = true
            lazyListState.animateScrollToItem(maxOf(0, currentLineIndex - 2), 0)
            isAutoScrolling = false
        }
    }

    // Manual resync: scroll to current line immediately
    LaunchedEffect(resyncTrigger.value) {
        if (resyncTrigger.value > 0 && currentLineIndex >= 0) {
            isAutoScrolling = true
            lazyListState.animateScrollToItem(maxOf(0, currentLineIndex - 2), 0)
            isAutoScrolling = false
        }
    }

    // Re-enable auto-scroll when track changes
    LaunchedEffect(lyricsRaw) { isAutoScrollEnabled = true }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            loading -> CircularProgressIndicator()
            lyrics.isEmpty() -> Text(
                "No lyrics found",
                color = colorScheme.onSurface.copy(alpha = 0.7f),
                fontSize = 18.sp
            )
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    state = lazyListState,
                    contentPadding = PaddingValues(vertical = 64.dp)
                ) {
                    if (provider != null) {
                        item {
                            Text(
                                text = "Lyrics from $provider",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                    }
                    itemsIndexed(lyrics) { i, entry ->
                        LyricsLine(
                            entry = entry,
                            isActive = i == currentLineIndex,
                            isPast = i < currentLineIndex,
                            currentPosition = currentPosition,
                            activeColor = colorScheme.primary,
                            onSeek = { AppState.player.seekTo(entry.time) }
                        )
                    }
                }

                // Resync button – appears when user scrolls away from current line
                AnimatedVisibility(
                    visible = !isAutoScrollEnabled,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
                    enter = fadeIn(tween(200)),
                    exit = fadeOut(tween(200))
                ) {
                    FilledTonalButton(
                        onClick = {
                            isAutoScrollEnabled = true
                            resyncTrigger.value++
                        }
                    ) {
                        Icon(
                            Icons.Rounded.Sync,
                            contentDescription = "Resync lyrics",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Resync", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LyricsLine(
    entry: LyricsEntry,
    isActive: Boolean,
    isPast: Boolean,
    currentPosition: Long,
    activeColor: Color,
    onSeek: () -> Unit
) {
    val words = entry.words
    if (words != null && (isActive || isPast)) {
        // Word-synced line: karaoke highlight with glow
        FlowRow(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth()
                .clickable { onSeek() },
            horizontalArrangement = Arrangement.Center
        ) {
            words.forEachIndexed { idx, word ->
                val isWordActive = isActive &&
                        currentPosition >= word.startTime &&
                        currentPosition < word.endTime
                val hasWordPassed = (isActive && currentPosition >= word.endTime) || isPast

                val targetAlpha = when {
                    hasWordPassed || isWordActive -> 1f
                    else -> 0.35f  // future word in current active line
                }
                val alpha by animateFloatAsState(
                    targetValue = targetAlpha,
                    animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing),
                    label = "wordAlpha"
                )
                val glowIntensity by animateFloatAsState(
                    targetValue = if (isWordActive) 1f else 0f,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "wordGlow"
                )
                val weight = when {
                    hasWordPassed -> FontWeight.Bold
                    isWordActive  -> FontWeight.ExtraBold
                    else          -> FontWeight.Medium
                }

                Text(
                    text = if (idx < words.lastIndex) "${word.text} " else word.text,
                    color = activeColor.copy(alpha = alpha),
                    fontWeight = weight,
                    fontSize = if (isActive) 22.sp else 18.sp,
                    lineHeight = if (isActive) 30.sp else 26.sp,
                    style = if (glowIntensity > 0.02f) {
                        LocalTextStyle.current.copy(
                            shadow = Shadow(
                                color = activeColor.copy(alpha = 0.8f * glowIntensity),
                                offset = Offset.Zero,
                                blurRadius = 18f * glowIntensity
                            )
                        )
                    } else LocalTextStyle.current,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        // Plain line or future line with no word data
        val targetAlpha = when {
            isActive -> 1f
            isPast   -> 0.6f
            else     -> 0.35f
        }
        val alpha by animateFloatAsState(
            targetValue = targetAlpha,
            animationSpec = tween(durationMillis = 200),
            label = "lineAlpha"
        )
        Text(
            text = entry.text,
            fontSize = if (isActive) 22.sp else 18.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            lineHeight = if (isActive) 30.sp else 26.sp,
            color = activeColor.copy(alpha = alpha),
            modifier = Modifier
                .padding(vertical = 2.dp)
                .fillMaxWidth()
                .clickable { onSeek() }
        )
    }
}
