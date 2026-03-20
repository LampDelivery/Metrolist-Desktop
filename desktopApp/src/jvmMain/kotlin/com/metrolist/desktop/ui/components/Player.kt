@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
package com.metrolist.desktop.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.EaseInBack
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.LinearEasing
// Removed incorrect Spring import
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CloseFullscreen
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Lyrics
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.RepeatOne
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.metrolist.desktop.constants.BottomPlayerHeight
import com.metrolist.desktop.constants.ExpandedThumbnailCornerRadius
import com.metrolist.desktop.constants.RepeatMode
import com.metrolist.desktop.constants.SliderStyle
import com.metrolist.desktop.state.AppState
import com.metrolist.desktop.ui.theme.MetrolistPauseIcon
import com.metrolist.desktop.ui.theme.MetrolistShareIcon
import com.metrolist.desktop.ui.theme.MetrolistVolumeDownIcon
import com.metrolist.desktop.ui.theme.MetrolistVolumeMuteIcon
import com.metrolist.desktop.ui.theme.MetrolistVolumeOffIcon
import com.metrolist.desktop.ui.theme.MetrolistVolumeUpIcon
import com.metrolist.desktop.ui.theme.PlayerColorExtractor
import com.metrolist.desktop.ui.theme.toHsv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.net.URI
import kotlin.math.roundToInt
import androidx.compose.animation.core.RepeatMode as AnimationRepeatMode
import org.jetbrains.skia.Image as SkiaImage
import com.metrolist.shared.api.innertube.models.response.PlayerResponse
import com.metrolist.shared.model.SongItem
import com.metrolist.shared.state.GlobalYouTubeRepository

@Composable
fun AnimatedGradientBackground(color: Color) {
    val url = AppState.currentTrack?.thumbnail ?: ""

    // Load album art — state resets to null automatically when url key changes
    var bitmap by remember(url) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(url) {
        if (url.isBlank()) return@LaunchedEffect
        withContext(Dispatchers.IO) {
            runCatching {
                val conn = URI(url).toURL().openConnection()
                conn.setRequestProperty("User-Agent", "Mozilla/5.0")
                bitmap = SkiaImage.makeFromEncoded(conn.getInputStream().readAllBytes()).toComposeImageBitmap()
            }
        }
    }

    // Three independent rotations: two full sweeps (opp. directions) + one slow oscillation.
    // Animate blur radius and layer alpha for a more dynamic effect.
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val rot1 by infiniteTransition.animateFloat(0f, 360f,
        infiniteRepeatable(tween(90000, easing = LinearEasing), AnimationRepeatMode.Restart), label = "rot1")
    val rot2 by infiniteTransition.animateFloat(0f, -360f,
        infiniteRepeatable(tween(75000, easing = LinearEasing), AnimationRepeatMode.Restart), label = "rot2")
    val rot3 by infiniteTransition.animateFloat(-18f, 18f,
        infiniteRepeatable(tween(55000, easing = CubicBezierEasing(0.37f, 0f, 0.63f, 1f)), AnimationRepeatMode.Reverse), label = "rot3")

    // Animate blur radius between 80dp and 120dp
    val blurRadius by infiniteTransition.animateFloat(80f, 120f,
        infiniteRepeatable(tween(6000, easing = CubicBezierEasing(0.37f, 0f, 0.63f, 1f)), AnimationRepeatMode.Reverse), label = "blurRadius")

    // Animate layer alpha for depth
    val layerAlpha1 by infiniteTransition.animateFloat(0.85f, 1f,
        infiniteRepeatable(tween(7000, easing = CubicBezierEasing(0.37f, 0f, 0.63f, 1f)), AnimationRepeatMode.Reverse), label = "layerAlpha1")
    val layerAlpha2 by infiniteTransition.animateFloat(0.65f, 0.85f,
        infiniteRepeatable(tween(9000, easing = CubicBezierEasing(0.37f, 0f, 0.63f, 1f)), AnimationRepeatMode.Reverse), label = "layerAlpha2")
    val layerAlpha3 by infiniteTransition.animateFloat(0.45f, 0.65f,
        infiniteRepeatable(tween(11000, easing = CubicBezierEasing(0.37f, 0f, 0.63f, 1f)), AnimationRepeatMode.Reverse), label = "layerAlpha3")

    // Fade art in once loaded; fall back to near-black tinted base while loading
    val artAlpha by animateFloatAsState(if (bitmap != null) 1f else 0f, tween(700), label = "artAlpha")
    val baseColor = remember(color) {
        val hsv = color.toHsv()
        Color.hsv(hsv[0], (hsv[1] * 0.10f).coerceIn(0f, 0.20f), 0.05f)
    }

    Box(Modifier.fillMaxSize().clip(RectangleShape).background(baseColor)) {
        // Art layer — 3 scaled + rotated copies of the album art, blurred together.
        val art = bitmap
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(blurRadius.dp, BlurredEdgeTreatment.Unbounded)
                .alpha(artAlpha)
        ) {
            if (art != null) {
                // Layer 1 — slow clockwise, large scale
                Image(art, null, contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().graphicsLayer { scaleX = 3.0f; scaleY = 3.0f; rotationZ = rot1; alpha = layerAlpha1 })
                // Layer 2 — counter-clockwise, slightly different scale + lower opacity
                Image(art, null, contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().graphicsLayer { scaleX = 2.5f; scaleY = 2.5f; rotationZ = rot2; alpha = layerAlpha2 })
                // Layer 3 — slow oscillation, creates color interference with the other two
                Image(art, null, contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().graphicsLayer { scaleX = 2.8f; scaleY = 2.8f; rotationZ = rot3; alpha = layerAlpha3 })
            }
        }

        // Radial vignette — dark edges, lit center
        Canvas(Modifier.fillMaxSize()) {
            drawRect(Brush.radialGradient(
                colorStops = arrayOf(0f to Color.Transparent, 0.40f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.82f)),
                center = center, radius = size.maxDimension * 0.65f
            ))
        }
        // Bottom-to-dark gradient for player UI legibility
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
            0f to Color.Transparent, 0.60f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.55f)
        )))
        // Uniform dark scrim for text contrast
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.20f)))
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
        
        val frosted = AppState.fullGradientBackground
        val playerBgColor = if (frosted) Color.White.copy(alpha = 0.10f) else colorScheme.surfaceContainer.copy(alpha = 0.98f)
        Surface(
            modifier = Modifier
                .width(playerWidth)
                .height(80.dp)
                .scale(scale)
                .hoverable(interactionSource)
                .clickable { AppState.isExpanded = !AppState.isExpanded }
                .then(if (frosted) Modifier.blur(32.dp) else Modifier),
            color = playerBgColor,
            shape = CircleShape,
            shadowElevation = if (frosted) 0.dp else 16.dp,
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
        
        // Always use animated gradient as default
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedGradientBackground(AppState.seedColor)
            // Apply frosted blur in front of gradient only in expanded mode
            if (AppState.fullGradientBackground) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(32.dp, BlurredEdgeTreatment.Unbounded)
                        .background(Color.White.copy(alpha = 0.10f))
                ) {}
            }
        }
        
        if (isNarrow) {
            Column(modifier = Modifier.fillMaxSize().padding(top = 48.dp)) {
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
            val panelSide = AppState.playerPanelSide
            Row(modifier = Modifier.fillMaxSize().padding(top = 48.dp)) {
                val sidePanelBg = if (AppState.fullGradientBackground) Color.Black.copy(alpha = 0.25f) else Color.Transparent
                if (panelSide == "left") {
                    Surface(modifier = Modifier.width(450.dp).fillMaxHeight(), color = sidePanelBg) {
                        ExpandedTabsContent()
                    }
                }
                Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    val artSize = (width * 0.35f).coerceIn(300.dp, 480.dp)
                    AsyncImage(
                        url = AppState.currentTrack?.thumbnail ?: "",
                        modifier = Modifier.size(artSize).shadow(32.dp, RoundedCornerShape(ExpandedThumbnailCornerRadius)),
                        shape = RoundedCornerShape(ExpandedThumbnailCornerRadius),
                        extractColor = true
                    )
                }
                if (panelSide != "left") {
                    Surface(modifier = Modifier.width(450.dp).fillMaxHeight(), color = sidePanelBg) {
                        ExpandedTabsContent()
                    }
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
        animationSpec = spring(dampingRatio = 0.75f)
    )

    val width by animateDpAsState(if (isWide) 120.dp else 56.dp)
    val height = 48.dp
    val shapeRadius = 24.dp

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
    var showPlayerMenu by remember { mutableStateOf(false) }
    var showSleepDialog by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }

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
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            MetrolistVolumeSlider(value = AppState.volume, onValueChange = { AppState.setVolumeLevel(it) }, modifier = Modifier.width(150.dp).padding(horizontal = 8.dp), accentColor = colorScheme.primary, showIconInside = false)
                                            Text(
                                                "${(AppState.volume * 100).roundToInt()}%",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = colorScheme.primary,
                                                modifier = Modifier.widthIn(min = 28.dp)
                                            )
                                        }
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

                            // Three-dot menu button
                            IconButton(onClick = { showPlayerMenu = !showPlayerMenu }) {
                                Icon(Icons.Outlined.MoreVert, "More options", tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                            }
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

    // Player Menu Panel
    if (showPlayerMenu) {
        PlayerMenuPanel(
            colorScheme = colorScheme,
            onDismiss = { showPlayerMenu = false },
            onShowAddToPlaylistDialog = {
                AppState.currentTrack?.let { song ->
                    AppState.showAddToPlaylistSong = song
                    AppState.fetchUserPlaylists()
                }
                showPlayerMenu = false
            },
            onShowDetailsDialog = {
                showDetailsDialog = true
                showPlayerMenu = false
            },
            onShowSleepTimerDialog = {
                showSleepDialog = true
                showPlayerMenu = false
            }
        )
    }

    // Sleep Timer Dialog
    if (showSleepDialog) {
        SleepTimerDialog(onDismiss = { showSleepDialog = false })
    }

    // Media Details Dialog
    if (showDetailsDialog) {
        MediaDetailsDialog(
            song = AppState.currentTrack,
            onDismiss = { showDetailsDialog = false }
        )
    }
}

@Composable
private fun MediaDetailsDialog(
    song: SongItem?,
    onDismiss: () -> Unit
) {
    var playerResponse by remember { mutableStateOf<PlayerResponse?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(song?.id) {
        if (song != null) {
            isLoading = true
            try {
                // Try to get detailed information via getStreamUrl which internally uses PlayerResponse
                GlobalYouTubeRepository.instance.getStreamUrl(song.id)
                // For now, we'll display basic info. Later we can expose PlayerResponse from repository
            } catch (_: Exception) {
                // Handle error silently
            } finally {
                isLoading = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Media Details",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Loading details...")
                    }
                } else if (song != null) {
                    // Basic Information
                    DetailSection("Basic Information") {
                        DetailRow("Title", song.title)
                        DetailRow("Artist", song.artists.joinToString(", ") { it.name })
                        song.album?.let { album ->
                            DetailRow("Album", album.name)
                        }
                        song.duration?.let { duration ->
                            val minutes = (duration / 1000) / 60
                            val seconds = (duration / 1000) % 60
                            DetailRow("Duration", "${minutes}:${seconds.toString().padStart(2, '0')}")
                        }
                        DetailRow("Video ID", song.id)
                        if (song.isExplicit) {
                            DetailRow("Content", "Explicit")
                        }
                    }

                    // Library Status
                    DetailSection("Library Status") {
                        DetailRow("In Library", if (song.libraryAddToken != null || song.libraryRemoveToken != null) "Yes" else "No")
                        if (song.libraryAddToken != null) {
                            DetailRow("Add Token", "Available")
                        }
                        if (song.libraryRemoveToken != null) {
                            DetailRow("Remove Token", "Available")
                        }
                    }

                    // Technical Information
                    DetailSection("Technical Information") {
                        song.endpointParams?.let { params ->
                            DetailRow("Endpoint Params", if (params.isNotEmpty()) "Available" else "None")
                        }
                        song.thumbnail?.let { thumbnail ->
                            DetailRow("Thumbnail URL", thumbnail)
                        }
                    }
                } else {
                    Text(
                        "No media information available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun DetailSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(2f),
            textAlign = TextAlign.End
        )
    }
}
