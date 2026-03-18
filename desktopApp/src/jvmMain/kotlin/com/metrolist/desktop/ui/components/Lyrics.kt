package com.metrolist.desktop.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

// ── LRC parsing regexes ────────────────────────────────────────────────────

private val lineRegex        = Regex("""^\[(\d{2}):(\d{2})\.(\d{2,3})]\s?(.*)$""")
private val wordTagRegex     = Regex("""<(\d{1,2}):(\d{2})\.(\d{2,3})>\s*([^<]*)""")
private val betterWordRegex  = Regex("""^<([^>]+)>$""")
private val agentRegex       = Regex("""\{agent:([^}]+)\}""")

private fun parseTimeMs(min: Long, sec: Long, msStr: String): Long {
    val millis = if (msStr.length == 3) msStr.toLong() else msStr.toLong() * 10
    return min * 60_000 + sec * 1000 + millis
}

private fun decodeHtmlEntities(text: String): String = text
    .replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">")
    .replace("&quot;", "\"").replace("&apos;", "'").replace("&#x27;", "'")
    .replace("&#39;", "'").replace("&nbsp;", "\u00A0")
    .replace("&#x2019;", "\u2019").replace("&#x2018;", "\u2018")
    .replace("&#x201C;", "\u201C").replace("&#x201D;", "\u201D")

/** Parse BetterLyrics `<word:startSecs:endSecs|word2:start:end|...>` word-data line. */
private fun parseBetterLyricsWords(data: String): List<WordTimestamp>? {
    val words = data.split("|").mapNotNull { segment ->
        val tokens = segment.split(":")
        if (tokens.size == 3) {
            val text    = decodeHtmlEntities(tokens[0].trim())
            val startMs = (tokens[1].toDoubleOrNull() ?: return@mapNotNull null) * 1000.0
            val endMs   = (tokens[2].toDoubleOrNull() ?: return@mapNotNull null) * 1000.0
            if (text.isNotBlank()) WordTimestamp(text, startMs.toLong(), endMs.toLong()) else null
        } else null
    }
    return words.ifEmpty { null }
}

/**
 * Parse an LRC string (with optional BetterLyrics word-data lines and
 * {agent:v1/v2} / {bg} prefixes) into a list of [LyricsEntry].
 */
fun parseLrc(lyrics: String): List<LyricsEntry> {
    if (!lyrics.trimStart().startsWith("[")) return emptyList()
    val lines  = lyrics.lines()
    val result = mutableListOf<LyricsEntry>()
    var i = 0
    while (i < lines.size) {
        val line  = lines[i].trim()
        val match = lineRegex.matchEntire(line)
        if (match != null) {
            val lineTime = parseTimeMs(
                match.groupValues[1].toLong(),
                match.groupValues[2].toLong(),
                match.groupValues[3]
            )
            var rawText = decodeHtmlEntities(match.groupValues[4])

            // Extract agent prefix: {agent:v1}, {agent:v2}, {agent:v1000}
            val agentMatch = agentRegex.find(rawText)
            val agent      = agentMatch?.groupValues?.get(1)
            if (agentMatch != null) rawText = rawText.replace(agentMatch.value, "")

            // Extract background-vocal prefix: {bg}
            val isBackground = rawText.startsWith("{bg}")
            if (isBackground) rawText = rawText.removePrefix("{bg}")

            rawText = rawText.trim()

            if (wordTagRegex.containsMatchIn(rawText)) {
                // Inline rich-sync: [mm:ss.ms]<mm:ss.ms> word <mm:ss.ms> word …
                val wordMatches = wordTagRegex.findAll(rawText).toList()
                val words = wordMatches.mapIndexedNotNull { idx, wm ->
                    val wordText = decodeHtmlEntities(wm.groupValues[4].trim())
                    if (wordText.isBlank()) null
                    else {
                        val wordStart = parseTimeMs(
                            wm.groupValues[1].toLong(), wm.groupValues[2].toLong(), wm.groupValues[3]
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
                    words.ifEmpty { null },
                    agent = agent,
                    isBackground = isBackground
                ))
            } else {
                // Check if the NEXT line is a BetterLyrics word-data line: <word:start:end|…>
                val nextLine        = lines.getOrNull(i + 1)?.trim() ?: ""
                val betterWordMatch = betterWordRegex.matchEntire(nextLine)
                val words = if (betterWordMatch != null) {
                    parseBetterLyricsWords(betterWordMatch.groupValues[1]).also { i++ }
                } else null

                result.add(LyricsEntry(lineTime, rawText, words, agent = agent, isBackground = isBackground))
            }
        }
        i++
    }
    return result.sortedBy { it.time }
}

// ── Main composable ────────────────────────────────────────────────────────

@Composable
fun DesktopLyricsView() {
    val lyricsRaw    = AppState.currentLyrics
    val loading      = AppState.isLyricsLoading
    val provider     = AppState.currentLyricsProvider
    val lyrics       = remember(lyricsRaw) { lyricsRaw?.let { parseLrc(it) } ?: emptyList() }
    val playerPos    by AppState.player.currentPosition.collectAsState(0L)
    val lazyListState = rememberLazyListState()
    val colorScheme  = MaterialTheme.colorScheme

    // High-frequency (8 ms) extrapolated position — necessary for per-word transitions.
    // Android Lyrics.kt uses exactly the same 8 ms poll interval.
    var effectivePosition by remember { mutableLongStateOf(0L) }
    LaunchedEffect(playerPos, AppState.isPlaying) {
        val basePos   = playerPos
        val baseTime  = System.currentTimeMillis()
        while (isActive) {
            effectivePosition = if (AppState.isPlaying) basePos + (System.currentTimeMillis() - baseTime)
                                else basePos
            delay(8L)
        }
    }

    val currentLineIndex = remember(effectivePosition, lyrics) {
        lyrics.indexOfLast { it.time <= effectivePosition }
    }

    var isAutoScrollEnabled by remember { mutableStateOf(true) }
    var isAutoScrolling     by remember { mutableStateOf(false) }
    val resyncTrigger = remember { mutableStateOf(0) }

    LaunchedEffect(lazyListState.isScrollInProgress) {
        if (lazyListState.isScrollInProgress && !isAutoScrolling) isAutoScrollEnabled = false
    }
    LaunchedEffect(currentLineIndex) {
        if (currentLineIndex >= 0 && isAutoScrollEnabled) {
            isAutoScrolling = true
            lazyListState.animateScrollToItem(maxOf(0, currentLineIndex - 2))
            isAutoScrolling = false
        }
    }
    LaunchedEffect(resyncTrigger.value) {
        if (resyncTrigger.value > 0 && currentLineIndex >= 0) {
            isAutoScrolling = true
            lazyListState.animateScrollToItem(maxOf(0, currentLineIndex - 2))
            isAutoScrolling = false
        }
    }
    LaunchedEffect(lyricsRaw) { isAutoScrollEnabled = true }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            loading       -> CircularProgressIndicator()
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
                    itemsIndexed(lyrics) { idx, entry ->
                        LyricsLine(
                            entry            = entry,
                            isActive         = idx == currentLineIndex,
                            isPast           = idx < currentLineIndex,
                            effectivePosition = effectivePosition,
                            activeColor      = colorScheme.primary,
                            onSeek           = { AppState.player.seekTo(entry.time) }
                        )
                    }
                }

                AnimatedVisibility(
                    visible  = !isAutoScrollEnabled,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
                    enter    = fadeIn(tween(200)),
                    exit     = fadeOut(tween(200))
                ) {
                    FilledTonalButton(onClick = { isAutoScrollEnabled = true; resyncTrigger.value++ }) {
                        Icon(Icons.Rounded.Sync, contentDescription = "Resync", modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Resync", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

// ── Line renderer ──────────────────────────────────────────────────────────

/**
 * Map agent string → horizontal text alignment.
 *   v1    = leading singer  → start-aligned
 *   v2    = secondary singer → end-aligned
 *   other / null / v1000 → centred (background vocals also centred)
 */
private fun agentTextAlign(agent: String?, isBackground: Boolean): TextAlign = when {
    isBackground     -> TextAlign.Center
    agent == "v1"    -> TextAlign.Start
    agent == "v2"    -> TextAlign.End
    else             -> TextAlign.Center
}

private fun agentWordRowAlign(agent: String?, isBackground: Boolean): Arrangement.Horizontal = when {
    isBackground     -> Arrangement.Center
    agent == "v1"    -> Arrangement.Start
    agent == "v2"    -> Arrangement.End
    else             -> Arrangement.Center
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LyricsLine(
    entry: LyricsEntry,
    isActive: Boolean,
    isPast: Boolean,
    effectivePosition: Long,
    activeColor: Color,
    onSeek: () -> Unit
) {
    val words        = entry.words
    val agent        = entry.agent
    val isBackground = entry.isBackground

    // Background vocals are rendered slightly smaller and more transparent
    val bgScale     = if (isBackground) 0.85f else 1f
    val activeFontSp = if (isBackground) 18.sp else 22.sp
    val idleFontSp   = if (isBackground) 15.sp else 18.sp
    val activeLineHSp = if (isBackground) 25.sp else 30.sp
    val idleLineHSp   = if (isBackground) 22.sp else 26.sp
    val bgAlphaMul   = if (isBackground) 0.8f else 1f

    val textAlign   = agentTextAlign(agent, isBackground)
    val wordAlign   = agentWordRowAlign(agent, isBackground)
    val fillModifier = Modifier.fillMaxWidth()

    if (words != null) {
        // ── Word-synced line ──────────────────────────────────────────────
        FlowRow(
            modifier = fillModifier
                .padding(vertical = 4.dp)
                .clickable { onSeek() },
            horizontalArrangement = wordAlign
        ) {
            words.forEachIndexed { idx, word ->
                val isWordActive = isActive &&
                        effectivePosition >= word.startTime &&
                        effectivePosition < word.endTime
                val hasWordPassed = (isActive && effectivePosition >= word.endTime) || isPast

                val targetAlpha = when {
                    hasWordPassed || isWordActive -> 1f * bgAlphaMul
                    isActive                      -> 0.35f * bgAlphaMul   // upcoming word in active line
                    isPast                        -> 0.6f * bgAlphaMul    // past line words
                    else                          -> 0.35f * bgAlphaMul   // future line words
                }
                val alpha by animateFloatAsState(
                    targetValue    = targetAlpha,
                    animationSpec  = tween(durationMillis = 80, easing = FastOutSlowInEasing),
                    label          = "wordAlpha"
                )
                val glowIntensity by animateFloatAsState(
                    targetValue   = if (isWordActive) 1f else 0f,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label         = "wordGlow"
                )
                val weight = when {
                    hasWordPassed -> FontWeight.Bold
                    isWordActive  -> FontWeight.ExtraBold
                    else          -> FontWeight.Medium
                }

                Text(
                    text       = if (idx < words.lastIndex) "${word.text} " else word.text,
                    color      = activeColor.copy(alpha = alpha),
                    fontWeight = weight,
                    fontSize   = if (isActive) activeFontSp else idleFontSp,
                    lineHeight = if (isActive) activeLineHSp else idleLineHSp,
                    style      = if (glowIntensity > 0.02f) LocalTextStyle.current.copy(
                        shadow = Shadow(
                            color      = activeColor.copy(alpha = 0.8f * glowIntensity),
                            offset     = Offset.Zero,
                            blurRadius = 18f * glowIntensity
                        )
                    ) else LocalTextStyle.current,
                    textAlign  = textAlign
                )
            }
        }
    } else {
        // ── Plain / future line ───────────────────────────────────────────
        val targetAlpha = when {
            isActive -> 1f    * bgAlphaMul
            isPast   -> 0.6f  * bgAlphaMul
            else     -> 0.35f * bgAlphaMul
        }
        val alpha by animateFloatAsState(
            targetValue   = targetAlpha,
            animationSpec = tween(durationMillis = 200),
            label         = "lineAlpha"
        )
        Text(
            text       = entry.text,
            fontSize   = if (isActive) activeFontSp else idleFontSp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            textAlign  = textAlign,
            lineHeight = if (isActive) activeLineHSp else idleLineHSp,
            color      = activeColor.copy(alpha = alpha),
            modifier   = fillModifier
                .padding(vertical = 2.dp)
                .clickable { onSeek() }
        )
    }
}
