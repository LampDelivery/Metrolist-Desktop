@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
package com.metrolist.desktop.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.core.RepeatMode as AnimationRepeatMode
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metrolist.desktop.state.AppState
import com.metrolist.desktop.constants.*
import com.metrolist.desktop.ui.theme.*
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun AnimatedGradientBackground(color: Color) {
    val infiniteTransition = rememberInfiniteTransition()

    // Rich palette: primary + complementary + analogous +/- + triadic
    val palette = remember(color) {
        val hsv = color.toHsv()
        val h = hsv[0]
        val s = hsv[1].coerceAtLeast(0.4f)
        val v = (hsv[2] * 0.85f).coerceIn(0.35f, 0.85f)
        listOf(
            Color.hsv(h, s, v),                                                                          // primary
            Color.hsv((h + 178f) % 360f, (s * 0.9f).coerceIn(0f, 1f), (v * 0.8f).coerceIn(0.2f, 0.85f)), // complementary
            Color.hsv((h + 30f) % 360f, s.coerceIn(0f, 1f), (v * 0.95f).coerceIn(0.3f, 0.85f)),          // warm analogous
            Color.hsv((h - 40f + 360f) % 360f, (s * 0.85f).coerceIn(0f, 1f), (v * 0.75f).coerceIn(0.2f, 0.8f)), // cool analogous
            Color.hsv((h + 120f) % 360f, (s * 0.7f).coerceIn(0f, 1f), (v * 0.9f).coerceIn(0.25f, 0.85f)), // triadic
        )
    }
    // Very dark hue-tinted base instead of flat black
    val baseColor = remember(color) {
        val hsv = color.toHsv()
        Color.hsv(hsv[0], (hsv[1] * 0.2f).coerceIn(0f, 1f), 0.07f)
    }

    // Blob 0 – primary, drifts from top-left toward top-center
    val b0x by infiniteTransition.animateFloat(-0.27f,  0.09f, infiniteRepeatable(tween(22000, easing = LinearEasing), AnimationRepeatMode.Reverse))
    val b0y by infiniteTransition.animateFloat(-0.28f,  0.06f, infiniteRepeatable(tween(19000, easing = LinearEasing), AnimationRepeatMode.Reverse))
    // Blob 1 – complementary, drifts from bottom-right toward center-right
    val b1x by infiniteTransition.animateFloat( 0.22f, -0.07f, infiniteRepeatable(tween(24000, easing = LinearEasing), AnimationRepeatMode.Reverse))
    val b1y by infiniteTransition.animateFloat( 0.25f, -0.08f, infiniteRepeatable(tween(20000, easing = LinearEasing), AnimationRepeatMode.Reverse))
    // Blob 2 – warm analogous, top-right
    val b2x by infiniteTransition.animateFloat( 0.26f, -0.06f, infiniteRepeatable(tween(17000, easing = LinearEasing), AnimationRepeatMode.Reverse))
    val b2y by infiniteTransition.animateFloat(-0.24f,  0.13f, infiniteRepeatable(tween(21000, easing = LinearEasing), AnimationRepeatMode.Reverse))
    // Blob 3 – cool analogous, bottom-left
    val b3x by infiniteTransition.animateFloat(-0.25f,  0.10f, infiniteRepeatable(tween(26000, easing = LinearEasing), AnimationRepeatMode.Reverse))
    val b3y by infiniteTransition.animateFloat( 0.22f, -0.07f, infiniteRepeatable(tween(18000, easing = LinearEasing), AnimationRepeatMode.Reverse))
    // Blob 4 – triadic accent, slow center drift
    val b4x by infiniteTransition.animateFloat(-0.10f,  0.16f, infiniteRepeatable(tween(23000, easing = LinearEasing), AnimationRepeatMode.Reverse))
    val b4y by infiniteTransition.animateFloat(-0.10f,  0.18f, infiniteRepeatable(tween(16000, easing = LinearEasing), AnimationRepeatMode.Reverse))

    Box(modifier = Modifier.fillMaxSize().background(baseColor)) {
        // Heavy-blur blob layer
        Canvas(modifier = Modifier.fillMaxSize().blur(160.dp)) {
            val cx = center.x; val cy = center.y; val d = size.maxDimension

            // primary – tall ellipse
            drawOval(palette[0],
                topLeft = androidx.compose.ui.geometry.Offset(cx + size.width * b0x - d, cy + size.height * b0y - d * 1.25f),
                size = androidx.compose.ui.geometry.Size(d * 2f, d * 2.5f), alpha = 0.75f)
            // complementary – wide ellipse
            drawOval(palette[1],
                topLeft = androidx.compose.ui.geometry.Offset(cx + size.width * b1x - d * 1.235f, cy + size.height * b1y - d * 0.95f),
                size = androidx.compose.ui.geometry.Size(d * 2.47f, d * 1.9f), alpha = 0.70f)
            // warm analogous – slightly wide
            drawOval(palette[2],
                topLeft = androidx.compose.ui.geometry.Offset(cx + size.width * b2x - d * 0.858f, cy + size.height * b2y - d * 0.702f),
                size = androidx.compose.ui.geometry.Size(d * 1.716f, d * 1.404f), alpha = 0.60f)
            // cool analogous – slightly tall
            drawOval(palette[3],
                topLeft = androidx.compose.ui.geometry.Offset(cx + size.width * b3x - d * 0.738f, cy + size.height * b3y - d * 0.943f),
                size = androidx.compose.ui.geometry.Size(d * 1.476f, d * 1.886f), alpha = 0.55f)
            // triadic – round accent
            drawOval(palette[4],
                topLeft = androidx.compose.ui.geometry.Offset(cx + size.width * b4x - d * 0.62f, cy + size.height * b4y - d * 0.62f),
                size = androidx.compose.ui.geometry.Size(d * 1.24f, d * 1.24f), alpha = 0.45f)
        }

        // Radial vignette – black edges, clear center (Apple Music signature)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.0f to Color.Transparent,
                        0.55f to Color.Transparent,
                        1.0f to Color.Black.copy(alpha = 0.60f)
                    ),
                    center = center,
                    radius = size.maxDimension * 0.72f
                )
            )
        }

        // Thin dark scrim for text readability
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.10f)))
    }
}

@Composable
fun FloatingBottomPlayerContent(colorScheme: ColorScheme) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val scale by animateFloatAsState(if (isHovered) 1.02f else 1f)

    val smoothProgress by animateFloatAsState(
        targetValue = AppState.progress,
        animationSpec = if (AppState.isPlaying) tween(durationMillis = 500, easing = LinearEasing) else spring()
    )

    BoxWithConstraints {
        val width = maxWidth
        val playerWidth = if (width < 600.dp) width - 32.dp else 520.dp
        
        Surface(
            modifier = Modifier
                .width(playerWidth)
                .height(80.dp)
                .scale(scale)
                .hoverable(interactionSource)
                .clickable { AppState.isExpanded = !AppState.isExpanded },
            color = colorScheme.surfaceContainer.copy(alpha = 0.98f),
            shape = CircleShape,
            shadowElevation = 16.dp,
            border = BorderStroke(1.dp, colorScheme.outline)
        ) {
            Row(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.size(64.dp)) {
                        drawArc(color = colorScheme.primary.copy(alpha = 0.15f), startAngle = -90f, sweepAngle = 360f, useCenter = false, style = Stroke(width = 3.dp.toPx()))
                        drawArc(color = colorScheme.primary, startAngle = -90f, sweepAngle = 360 * smoothProgress, useCenter = false, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                    }
                    AsyncImage(url = AppState.currentTrack?.thumbnail ?: "", modifier = Modifier.size(52.dp), shape = CircleShape, extractColor = true)
                    Box(modifier = Modifier.size(52.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.2f)).clickable { AppState.togglePlay() }, contentAlignment = Alignment.Center) {
                        Icon(if (AppState.isPlaying) MetrolistPauseIcon else Icons.Outlined.PlayArrow, null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(AppState.currentTrack?.title ?: "Not Playing", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    val artists = AppState.currentTrack?.artists ?: emptyList()
                    val album = AppState.currentTrack?.album
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        artists.forEachIndexed { idx, artist ->
                            Text(
                                text = artist.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .clickable(enabled = artist.id != null, indication = null, interactionSource = remember { MutableInteractionSource() }) {
                                        artist.id?.let { AppState.fetchArtistData(it) }
                                    }
                            )
                            if (idx < artists.lastIndex) {
                                Text(" / ", style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant)
                            }
                        }
                        if (album != null && album.name.isNotBlank()) {
                            Text(" • ", style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant)
                            Text(
                                text = album.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .clickable(enabled = album.id != null, indication = null, interactionSource = remember { MutableInteractionSource() }) {
                                        album.id?.let { AppState.fetchAlbumData(it) }
                                    }
                            )
                        }
                    }
                }
                if (width > 450.dp) {
                    val isMiniLiked = AppState.currentTrack?.id in AppState.likedSongIds
                    MetrolistIconButton(
                        if (isMiniLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        { AppState.currentTrack?.let { AppState.toggleLike(it) } },
                        contentColor = if (isMiniLiked) colorScheme.primary else colorScheme.onSurfaceVariant
                    )
                }
                
                val expandRotation by animateFloatAsState(if (AppState.isExpanded) 180f else 0f)
                IconButton(onClick = { AppState.isExpanded = !AppState.isExpanded }) {
                    Icon(
                        Icons.Outlined.KeyboardArrowUp, 
                        null, 
                        tint = colorScheme.onSurfaceVariant, 
                        modifier = Modifier.size(24.dp).rotate(expandRotation)
                    )
                }
                Spacer(Modifier.width(8.dp))
            }
        }
    }
}

@Composable
fun ExpandedPlayerView() {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val width = maxWidth
        val isNarrow = width < 850.dp
        
        if (AppState.animatedGradient) {
            Box(modifier = Modifier.fillMaxSize()) {
                AnimatedGradientBackground(AppState.seedColor)
            }
        } else {
            val gradientColors = remember(AppState.seedColor) {
                PlayerColorExtractor.getGradientColors(AppState.seedColor)
            }
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(gradientColors)))
        }
        
        if (isNarrow) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    val artSize = (width * 0.7f).coerceIn(200.dp, 400.dp)
                    AsyncImage(
                        url = AppState.currentTrack?.thumbnail ?: "",
                        modifier = Modifier.size(artSize).shadow(24.dp, RoundedCornerShape(ExpandedThumbnailCornerRadius)),
                        shape = RoundedCornerShape(ExpandedThumbnailCornerRadius),
                        extractColor = true
                    )
                }
                
                Surface(
                    modifier = Modifier.fillMaxWidth().weight(1.2f),
                    color = Color.Transparent
                ) {
                    ExpandedTabsContent()
                }
            }
        } else {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    val artSize = (width * 0.35f).coerceIn(300.dp, 480.dp)
                    AsyncImage(
                        url = AppState.currentTrack?.thumbnail ?: "",
                        modifier = Modifier.size(artSize).shadow(32.dp, RoundedCornerShape(ExpandedThumbnailCornerRadius)),
                        shape = RoundedCornerShape(ExpandedThumbnailCornerRadius),
                        extractColor = true
                    )
                }

                Surface(
                    modifier = Modifier.width(450.dp).fillMaxHeight(),
                    color = Color.Transparent
                ) {
                    ExpandedTabsContent()
                }
            }
        }
    }
}

@Composable
private fun ExpandedTabsContent() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        Pair("Up Next", Icons.AutoMirrored.Outlined.QueueMusic),
        Pair("Lyrics",  Icons.Outlined.Lyrics),
        Pair("Related", Icons.Outlined.Explore)
    )
    var showSleepDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        // Medium-screen nav bar: rounded container, icon + label side-by-side, pill indicator
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, (title, icon) ->
                    val isSelected = selectedTab == index
                    val indicatorColor by animateColorAsState(
                        if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                        animationSpec = tween(200),
                        label = "navIndicator$index"
                    )
                    val contentColor by animateColorAsState(
                        if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        animationSpec = tween(200),
                        label = "navContent$index"
                    )
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(indicatorColor)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { selectedTab = index }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = title,
                            tint = contentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            title,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = contentColor
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Box(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    val direction = if (targetState > initialState) 1 else -1
                    (slideInHorizontally { width -> direction * width } + fadeIn()).togetherWith(
                        slideOutHorizontally { width -> direction * -width } + fadeOut()
                    ).using(SizeTransform(clip = false))
                },
                label = "TabContentTransition"
            ) { targetIndex ->
                Box(Modifier.fillMaxSize()) {
                    when (targetIndex) {
                        0 -> UpNextContent()
                        1 -> DesktopLyricsView()
                        2 -> RelatedContent()
                    }
                }
            }
        }

        // Sleep timer row
        val sleepActive = AppState.sleepTimerEnabled
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { showSleepDialog = true }
                .padding(vertical = 12.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.Bedtime, null,
                    tint = if (sleepActive) Color.White else Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = when {
                        AppState.sleepTimerStopAfterCurrentSong -> "Stopping after current song"
                        sleepActive -> "Sleep: ${formatSleepTime(AppState.sleepTimerTimeLeftMs)}"
                        else -> "Sleep Timer"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (sleepActive) Color.White else Color.White.copy(alpha = 0.7f)
                )
            }
            if (sleepActive) {
                IconButton(onClick = { AppState.clearSleepTimer() }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.Close, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                }
            }
        }
    }

    if (showSleepDialog) {
        SleepTimerDialog(onDismiss = { showSleepDialog = false })
    }
}

@Composable
private fun UpNextContent() {
    val queueItems by AppState.queue.items.collectAsState()
    val currentIndex by AppState.queue.currentIndex.collectAsState()
    val upNext = if (currentIndex >= 0 && currentIndex < queueItems.size) queueItems.drop(currentIndex + 1) else queueItems

    if (upNext.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No songs in queue", color = Color.White.copy(alpha = 0.5f), style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        items(upNext) { song ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { AppState.playTrack(song, queueItems) }
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(url = song.thumbnail ?: "", modifier = Modifier.size(40.dp).clip(RoundedCornerShape(6.dp)))
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(song.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(song.artists.joinToString { it.name }, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun RelatedContent() {
    val videoId = AppState.currentTrack?.id
    LaunchedEffect(videoId) {
        if (videoId != null) AppState.fetchRelated(videoId)
    }

    when {
        AppState.isRelatedLoading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(32.dp))
            }
        }
        AppState.relatedSongs.isEmpty() -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No related songs", color = Color.White.copy(alpha = 0.5f), style = MaterialTheme.typography.bodyMedium)
            }
        }
        else -> {
            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                items(AppState.relatedSongs) { song ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { AppState.playTrack(song, AppState.relatedSongs) }
                            .padding(horizontal = 4.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(url = song.thumbnail ?: "", modifier = Modifier.size(40.dp).clip(RoundedCornerShape(6.dp)))
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(song.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(song.artists.joinToString { it.name }, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SleepTimerDialog(onDismiss: () -> Unit) {
    var minutes by remember { mutableIntStateOf(30) }
    var fadeOut by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.Bedtime, null) },
        title = { Text("Sleep Timer") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "$minutes min",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = minutes.toFloat(),
                    onValueChange = { minutes = it.roundToInt() },
                    valueRange = 5f..120f,
                    steps = 22
                )
                OutlinedButton(
                    onClick = {
                        AppState.startSleepTimer(0, stopAfterCurrentSong = true, fadeOut = false)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("End of current song")
                }
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { fadeOut = !fadeOut },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = fadeOut, onCheckedChange = { fadeOut = it })
                    Spacer(Modifier.width(8.dp))
                    Text("Fade out audio", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                AppState.startSleepTimer(minutes, stopAfterCurrentSong = false, fadeOut = fadeOut)
                onDismiss()
            }) { Text("Start") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private fun formatSleepTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return if (m > 0) "${m}m ${s}s" else "${s}s"
}

@Composable
fun ShareHeartPill(colorScheme: ColorScheme) {
    val shareShape = RoundedCornerShape(topStart = 50.dp, bottomStart = 50.dp, topEnd = 3.dp, bottomEnd = 3.dp)
    val favShape = RoundedCornerShape(topStart = 3.dp, bottomStart = 3.dp, topEnd = 50.dp, bottomEnd = 50.dp)
    val pillBg = colorScheme.onSurface.copy(alpha = 0.08f)

    val isLiked = AppState.currentTrack?.id in AppState.likedSongIds
    var copyFeedback by remember { mutableStateOf(false) }
    LaunchedEffect(copyFeedback) {
        if (copyFeedback) { delay(1500); copyFeedback = false }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Surface(
            modifier = Modifier.height(36.dp),
            shape = shareShape,
            color = pillBg,
            border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.05f)),
            onClick = {
                AppState.currentTrack?.id?.let {
                    AppState.copyToClipboard("https://music.youtube.com/watch?v=$it")
                    copyFeedback = true
                }
            }
        ) {
            Box(modifier = Modifier.padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
                AnimatedContent(copyFeedback, label = "shareFeedback") { copied ->
                    Icon(
                        if (copied) Icons.Outlined.Check else MetrolistShareIcon,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = if (copied) colorScheme.primary else colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Surface(
            modifier = Modifier.height(36.dp),
            shape = favShape,
            color = pillBg,
            border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.05f)),
            onClick = { AppState.currentTrack?.let { AppState.toggleLike(it) } }
        ) {
            Box(modifier = Modifier.padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
                Icon(
                    if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    null,
                    modifier = Modifier.size(16.dp),
                    tint = if (isLiked) colorScheme.primary else colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MetrolistPlayPauseButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    colorScheme: ColorScheme,
    modifier: Modifier = Modifier,
    isWide: Boolean = false,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.92f
            isHovered -> 1.08f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
    )

    val width by animateDpAsState(if (isWide) 120.dp else 56.dp)
    val height by animateDpAsState(if (isWide) 48.dp else 56.dp)
    val shapeRadius by animateDpAsState(if (isWide) 24.dp else 28.dp)

    Surface(
        modifier = modifier
            .size(width = width, height = height)
            .scale(scale)
            .hoverable(interactionSource),
        shape = RoundedCornerShape(shapeRadius),
        color = if (isHovered) colorScheme.primaryContainer.copy(alpha = 0.9f) else colorScheme.primaryContainer,
        onClick = onClick,
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedContent(
                targetState = isPlaying,
                transitionSpec = {
                    (scaleIn(animationSpec = tween(250, easing = EaseOutBack)) + fadeIn()).togetherWith(
                        scaleOut(animationSpec = tween(250, easing = EaseInBack)) + fadeOut()
                    )
                }
            ) { playing ->
                Icon(
                    imageVector = if (playing) MetrolistPauseIcon else Icons.Outlined.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(if (isWide) 24.dp else 32.dp),
                    tint = colorScheme.onPrimaryContainer
                )
            }
            if (isWide) {
                Spacer(Modifier.width(8.dp))
                AnimatedContent(
                    targetState = if (isPlaying) "Pause" else "Play",
                    transitionSpec = {
                        (slideInVertically { it / 2 } + fadeIn()).togetherWith(
                            slideOutVertically { -it / 2 } + fadeOut()
                        )
                    }
                ) { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedControlIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    contentColor: Color,
    iconSize: androidx.compose.ui.unit.Dp = 28.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.85f
            isHovered -> 1.15f
            else -> 1f
        }
    )
    
    IconButton(
        onClick = onClick,
        modifier = Modifier.scale(scale).hoverable(interactionSource),
        interactionSource = interactionSource
    ) {
        Icon(icon, null, modifier = Modifier.size(iconSize), tint = if (isHovered) contentColor.copy(alpha = 1f) else contentColor.copy(alpha = 0.8f))
    }
}

@Composable
fun PlayerSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit = {},
    style: SliderStyle,
    isPlaying: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    isFocused: Boolean = false
) {
    val sliderColors = SliderDefaults.colors(
        activeTrackColor = color, 
        thumbColor = color, 
        inactiveTrackColor = color.copy(alpha = 0.2f)
    )

    when (style) {
        SliderStyle.WAVY -> {
            WavySlider(
                value = value,
                onValueChange = onValueChange,
                onValueChangeFinished = onValueChangeFinished,
                isPlaying = isPlaying,
                colors = sliderColors,
                modifier = modifier
            )
        }
        SliderStyle.SQUIGGLY -> {
            SquigglySlider(
                value = value,
                onValueChange = onValueChange,
                onValueChangeFinished = onValueChangeFinished,
                isPlaying = isPlaying,
                colors = sliderColors,
                modifier = modifier
            )
        }
        SliderStyle.SLIM -> {
            Slider(
                value = value,
                onValueChange = onValueChange,
                onValueChangeFinished = onValueChangeFinished,
                thumb = {
                    if (isFocused) {
                        Box(Modifier.size(12.dp).background(color, CircleShape))
                    }
                },
                track = { sliderState ->
                    PlayerSliderTrack(
                        sliderState = sliderState,
                        colors = sliderColors,
                        trackHeight = if (isFocused) 6.dp else 4.dp
                    )
                },
                modifier = modifier.height(if (isFocused) 32.dp else 4.dp)
            )
        }
        else -> {
            // DEFAULT: equal-thickness tracks with vertical pill thumb and equal gaps
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = { offset ->
                                onValueChange((offset.x / size.width.toFloat()).coerceIn(0f, 1f))
                            },
                            onHorizontalDrag = { change, _ ->
                                onValueChange((change.position.x / size.width.toFloat()).coerceIn(0f, 1f))
                            },
                            onDragEnd = { onValueChangeFinished() },
                            onDragCancel = { onValueChangeFinished() }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            onValueChange((offset.x / size.width.toFloat()).coerceIn(0f, 1f))
                            onValueChangeFinished()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxWidth().fillMaxHeight().graphicsLayer { clip = false }) {
                    val fraction = value.coerceIn(0f, 1f)
                    val trackStroke = 5.dp.toPx()
                    val cy = size.height / 2f  // Track centered vertically
                    val thumbWidth = 5.dp.toPx()
                    val gap = 5.dp.toPx()
                    val thumbHalfH = 8.dp.toPx()  // Fixed thumb height allowing overflow
                    val thumbX = (size.width * fraction).coerceIn(thumbWidth / 2f, size.width - thumbWidth / 2f)

                    // Active track: left of thumb
                    val activeEnd = (thumbX - thumbWidth / 2f - gap).coerceAtLeast(0f)
                    if (activeEnd > 0f) {
                        drawLine(
                            color = color,
                            start = Offset(0f, cy),
                            end = Offset(activeEnd, cy),
                            strokeWidth = trackStroke,
                            cap = StrokeCap.Round
                        )
                    }

                    // Inactive track: right of thumb
                    val inactiveStart = (thumbX + thumbWidth / 2f + gap).coerceAtMost(size.width)
                    if (inactiveStart < size.width) {
                        drawLine(
                            color = color.copy(alpha = 0.25f),
                            start = Offset(inactiveStart, cy),
                            end = Offset(size.width, cy),
                            strokeWidth = trackStroke,
                            cap = StrokeCap.Round
                        )
                    }

                    // Vertical pill thumb
                    drawLine(
                        color = color,
                        start = Offset(thumbX, cy - thumbHalfH),
                        end = Offset(thumbX, cy + thumbHalfH),
                        strokeWidth = thumbWidth,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val seconds = (ms / 1000) % 60
    val minutes = (ms / (1000 * 60)) % 60
    val hours = (ms / (1000 * 60 * 60))
    
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%d:%02d".format(minutes, seconds)
    }
}

@Composable
fun StandardBottomPlayer(colorScheme: ColorScheme) {
    val playerPosition by AppState.player.currentPosition.collectAsState()
    val playerDuration by AppState.player.duration.collectAsState()

    var isDragging by remember { mutableStateOf(false) }
    var dragValue by remember { mutableFloatStateOf(0f) }

    // Real-time extrapolation: advance position at 60fps between player updates
    var extrapolatedProgress by remember { mutableStateOf(0f) }
    LaunchedEffect(playerPosition, playerDuration, AppState.isPlaying) {
        if (playerDuration <= 0L) { extrapolatedProgress = 0f; return@LaunchedEffect }
        if (!AppState.isPlaying) {
            extrapolatedProgress = playerPosition.toFloat() / playerDuration.toFloat()
            return@LaunchedEffect
        }
        val startPos = playerPosition
        val startTime = System.currentTimeMillis()
        while (isActive) {
            val elapsed = System.currentTimeMillis() - startTime
            extrapolatedProgress = ((startPos + elapsed).toFloat() / playerDuration.toFloat()).coerceIn(0f, 1f)
            delay(16L)
        }
    }

    val displayProgress = if (isDragging) dragValue else extrapolatedProgress

    BoxWithConstraints(modifier = Modifier.fillMaxWidth().background(colorScheme.surfaceContainer)) {
        val width = maxWidth
        val isWide = width > 900.dp
        val isNarrow = width < 650.dp

        Column(modifier = Modifier.fillMaxWidth()) {
            val sliderBoxHeight = 4.dp
            Box(modifier = Modifier.fillMaxWidth().height(sliderBoxHeight)) {
                PlayerSlider(
                    value = displayProgress,
                    onValueChange = { 
                        isDragging = true
                        dragValue = it 
                    },
                    onValueChangeFinished = { 
                        AppState.seekTo(dragValue)
                        isDragging = false
                    },
                    style = AppState.sliderStyleState,
                    isPlaying = AppState.isPlaying,
                    color = colorScheme.primary,
                    modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
                    isFocused = false
                )
            }
            
            val songInfo = @Composable {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.wrapContentWidth()) {
                    AsyncImage(
                        url = AppState.currentTrack?.thumbnail ?: "",
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)),
                        extractColor = true
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.widthIn(max = 240.dp)) {
                        Text(
                            AppState.currentTrack?.title ?: "Not Playing", 
                            style = if (isNarrow) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium, 
                            fontWeight = FontWeight.Medium, 
                            maxLines = 1, 
                            overflow = TextOverflow.Ellipsis
                        )
                        val artists = AppState.currentTrack?.artists ?: emptyList()
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            artists.forEachIndexed { idx, artist ->
                                Text(
                                    text = artist.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.clickable(enabled = artist.id != null, indication = null, interactionSource = remember { MutableInteractionSource() }) {
                                        artist.id?.let { AppState.fetchArtistData(it) }
                                    }
                                )
                                if (idx < artists.lastIndex) {
                                    Text(" / ", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                    if (width > 700.dp) {
                        Spacer(Modifier.width(16.dp))
                        ShareHeartPill(colorScheme)
                    }
                }
            }

            val playbackControls = @Composable {
                val backInteractionSource = remember { MutableInteractionSource() }
                val nextInteractionSource = remember { MutableInteractionSource() }
                val playPauseInteractionSource = remember { MutableInteractionSource() }

                val isBackPressed by backInteractionSource.collectIsPressedAsState()
                val isNextPressed by nextInteractionSource.collectIsPressedAsState()
                val isPlayPausePressed by playPauseInteractionSource.collectIsPressedAsState()

                val playPauseWeight by animateFloatAsState(
                    targetValue = if (isPlayPausePressed) 1.9f else if (isBackPressed || isNextPressed) 1.1f else 1.3f,
                    animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f)
                )
                val backButtonWeight by animateFloatAsState(
                    targetValue = if (isBackPressed) 0.65f else if (isPlayPausePressed) 0.35f else 0.45f,
                    animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f)
                )
                val nextButtonWeight by animateFloatAsState(
                    targetValue = if (isNextPressed) 0.65f else if (isPlayPausePressed) 0.35f else 0.45f,
                    animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f)
                )

                Row(
                    modifier = if (isWide) Modifier.width(360.dp) else Modifier.wrapContentWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    FilledIconButton(
                        onClick = { AppState.skipPrevious() },
                        shape = RoundedCornerShape(50),
                        interactionSource = backInteractionSource,
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = colorScheme.surfaceContainerHighest, contentColor = colorScheme.onSurface),
                        modifier = Modifier.height(if (isWide) 48.dp else 42.dp).then(if (isWide) Modifier.weight(backButtonWeight) else Modifier.size(42.dp))
                    ) { Icon(Icons.Outlined.SkipPrevious, null, modifier = Modifier.size(24.dp)) }
                    
                    Spacer(Modifier.width(if (isNarrow) 4.dp else 8.dp))
                    
                    MetrolistPlayPauseButton(
                        isPlaying = AppState.isPlaying,
                        onClick = { AppState.togglePlay() },
                        colorScheme = colorScheme,
                        isWide = isWide,
                        interactionSource = playPauseInteractionSource,
                        modifier = if (isWide) Modifier.weight(playPauseWeight) else Modifier
                    )
                    
                    Spacer(Modifier.width(if (isNarrow) 4.dp else 8.dp))
                    
                    FilledIconButton(
                        onClick = { AppState.skipNext() },
                        shape = RoundedCornerShape(50),
                        interactionSource = nextInteractionSource,
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = colorScheme.surfaceContainerHighest, contentColor = colorScheme.onSurface),
                        modifier = Modifier.height(if (isWide) 48.dp else 42.dp).then(if (isWide) Modifier.weight(nextButtonWeight) else Modifier.size(42.dp))
                    ) { Icon(Icons.Outlined.SkipNext, null, modifier = Modifier.size(24.dp)) }
                    
                    if (!isNarrow) {
                        Spacer(Modifier.width(16.dp))
                        Text("${formatTime(playerPosition)} / ${formatTime(playerDuration)}", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Fixed Layout: Left (Flexible), Middle (Fixed/Centered), Right (Flexible)
            Row(
                modifier = Modifier.fillMaxWidth().height(BottomPlayerHeight).padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Section 1: Left
                Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.CenterStart) {
                    if (AppState.swapPlayerControls) songInfo() else playbackControls()
                }

                // Section 2: Middle (Strictly Centered)
                Box(Modifier.wrapContentWidth().fillMaxHeight().padding(horizontal = 24.dp), contentAlignment = Alignment.Center) {
                    if (AppState.swapPlayerControls) playbackControls() else songInfo()
                }

                // Section 3: Right
                Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.CenterEnd) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isWide) {
                            var isVolumeHovered by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.onPointerEvent(PointerEventType.Enter) { isVolumeHovered = true }.onPointerEvent(PointerEventType.Exit) { isVolumeHovered = false }) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AnimatedVisibility(visible = isVolumeHovered, enter = fadeIn() + expandHorizontally(expandFrom = Alignment.End), exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.End)) {
                                        MetrolistVolumeSlider(value = AppState.volume, onValueChange = { AppState.setVolumeLevel(it) }, modifier = Modifier.width(180.dp).padding(horizontal = 8.dp), accentColor = colorScheme.primary, showIconInside = false)
                                    }
                                    Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                                        Icon(if (AppState.volume > 0.66f) MetrolistVolumeUpIcon else if (AppState.volume > 0.33f) MetrolistVolumeDownIcon else if (AppState.volume > 0f) MetrolistVolumeMuteIcon else MetrolistVolumeOffIcon, null, modifier = Modifier.size(20.dp), tint = if (isVolumeHovered) colorScheme.primary else colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                            // Miniplayer pop-out button
                            IconButton(onClick = { AppState.showMiniplayer = !AppState.showMiniplayer }) {
                                AnimatedContent(
                                    targetState = AppState.showMiniplayer,
                                    transitionSpec = {
                                        (scaleIn(animationSpec = tween(200)) + fadeIn()).togetherWith(
                                            scaleOut(animationSpec = tween(200)) + fadeOut()
                                        )
                                    },
                                    label = "miniplayerIcon"
                                ) { showing ->
                                    Icon(
                                        if (showing) Icons.Outlined.CloseFullscreen else Icons.AutoMirrored.Outlined.OpenInNew,
                                        contentDescription = if (showing) "Close Miniplayer" else "Popout Miniplayer",
                                        tint = if (showing) colorScheme.primary else colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                        }

                        if (!isNarrow) {
                            AnimatedControlIconButton(icon = when(AppState.repeatMode) { RepeatMode.OFF -> Icons.Outlined.Repeat; RepeatMode.ALL -> Icons.Outlined.Repeat; RepeatMode.ONE -> Icons.Outlined.RepeatOne }, onClick = { AppState.repeatMode = RepeatMode.entries[(AppState.repeatMode.ordinal + 1) % 3] }, contentColor = if (AppState.repeatMode != RepeatMode.OFF) colorScheme.primary else colorScheme.onSurfaceVariant, iconSize = 20.dp)
                            AnimatedControlIconButton(icon = Icons.Outlined.Shuffle, onClick = { AppState.shuffleEnabled = !AppState.shuffleEnabled }, contentColor = if (AppState.shuffleEnabled) colorScheme.primary else colorScheme.onSurfaceVariant, iconSize = 20.dp)
                        }
                        
                        val expandRotation by animateFloatAsState(targetValue = if (AppState.isExpanded) 180f else 0f)
                        IconButton(onClick = { AppState.isExpanded = !AppState.isExpanded }) {
                            Icon(Icons.Outlined.KeyboardArrowUp, null, tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(28.dp).rotate(expandRotation))
                        }
                    }
                }
            }
        }
    }
}
