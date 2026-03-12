@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
package com.metrolist.desktop.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.core.RepeatMode as AnimationRepeatMode
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

@Composable
fun AnimatedGradientBackground(color: Color) {
    // Apple Music-style mesh gradient using animated blurred circles
    val infiniteTransition = rememberInfiniteTransition()
    
    val meshColors = remember(color) {
        val hsv = color.toHsv()
        listOf(
            color,
            Color.hsv((hsv[0] + 25f) % 360f, (hsv[1] * 0.85f).coerceIn(0f, 1f), (hsv[2] * 0.8f).coerceIn(0f, 1f)),
            Color.hsv((hsv[0] - 35f + 360f) % 360f, (hsv[1] * 0.75f).coerceIn(0f, 1f), (hsv[2] * 0.7f).coerceIn(0f, 1f)),
            Color.hsv((hsv[0] + 15f) % 360f, (hsv[1] * 0.65f).coerceIn(0f, 1f), (hsv[2] * 0.6f).coerceIn(0f, 1f))
        )
    }

    val params = List(meshColors.size) { i ->
        val x by infiniteTransition.animateFloat(
            initialValue = -0.35f + i * 0.25f,
            targetValue = 0.35f - i * 0.18f,
            animationSpec = infiniteRepeatable(
                tween(16000 + i * 2000, easing = LinearEasing),
                AnimationRepeatMode.Reverse
            )
        )
        val y by infiniteTransition.animateFloat(
            initialValue = -0.32f - i * 0.18f,
            targetValue = 0.32f + i * 0.22f,
            animationSpec = infiniteRepeatable(
                tween(17000 + i * 1800, easing = LinearEasing),
                AnimationRepeatMode.Reverse
            )
        )
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.95f + i * 0.13f,
            targetValue = 1.18f + i * 0.15f,
            animationSpec = infiniteRepeatable(
                tween(15000 + i * 2100, easing = LinearEasing),
                AnimationRepeatMode.Reverse
            )
        )
        Triple(x, y, scale)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Canvas(modifier = Modifier.fillMaxSize().blur(120.dp)) {
            drawRect(color = Color.Black)
            for (i in meshColors.indices) {
                val (x, y, scale) = params[i]
                val meshColor = meshColors[i]
                drawCircle(
                    color = meshColor,
                    radius = size.maxDimension * (0.52f + i * 0.17f) * scale,
                    center = center + androidx.compose.ui.geometry.Offset(size.width * x, size.height * y),
                    alpha = 0.38f - i * 0.07f
                )
            }
        }
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.18f)))
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
                    MetrolistIconButton(Icons.Outlined.FavoriteBorder, {}, contentColor = colorScheme.onSurfaceVariant)
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
fun ExpandedPlayerView(colorScheme: ColorScheme) {
    // Only apply the gradient background to the main player area, not the sidebar
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val width = maxWidth
        val isNarrow = width < 850.dp
        
        if (AppState.animatedGradient) {
            // Place the gradient only behind the main player area, not the sidebar
            Box(modifier = Modifier.fillMaxSize()) {
                AnimatedGradientBackground(colorScheme.primary)
            }
        } else {
            val gradientColors = remember(colorScheme.primary) { 
                PlayerColorExtractor.getGradientColors(colorScheme.primary) 
            }
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(gradientColors)))
        }
        
        if (isNarrow) {
            Column(modifier = Modifier.fillMaxSize().padding(top = TopBarHeight)) {
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
            Row(modifier = Modifier.fillMaxSize().padding(top = TopBarHeight)) {
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
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        var selectedTab by remember { mutableIntStateOf(0) }
        val tabs = listOf("UP NEXT", "LYRICS", "RELATED")

        SecondaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = Color.White,
            indicator = { 
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(selectedTab),
                    color = Color.White,
                    height = 2.dp
                )
            },
            divider = {
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            }
        ) {
            tabs.forEachIndexed { index, title ->
                val selected = selectedTab == index
                val scale by animateFloatAsState(if (selected) 1.05f else 1f)
                
                Tab(
                    selected = selected,
                    onClick = { selectedTab = index },
                    modifier = Modifier.scale(scale),
                    text = { 
                        Text(
                            text = title, 
                            style = MaterialTheme.typography.labelLarge, 
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, 
                            color = if (selected) Color.White else Color.White.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        ) 
                    }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

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
                        0 -> Text("Queue functionality coming soon", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyLarge)
                        1 -> DesktopLyricsView()
                        2 -> Text("Related tracks coming soon", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { }
                .padding(vertical = 12.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Timer, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text("Sleep Timer", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun ShareHeartPill(colorScheme: ColorScheme) {
    val shareShape = RoundedCornerShape(topStart = 50.dp, bottomStart = 50.dp, topEnd = 3.dp, bottomEnd = 3.dp)
    val favShape = RoundedCornerShape(topStart = 3.dp, bottomStart = 3.dp, topEnd = 50.dp, bottomEnd = 50.dp)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            modifier = Modifier.height(40.dp),
            shape = shareShape,
            color = colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.1f)),
            onClick = { /* TODO: Share logic */ }
        ) {
            Box(modifier = Modifier.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
                Icon(MetrolistShareIcon, null, modifier = Modifier.size(18.dp), tint = colorScheme.onSurfaceVariant)
            }
        }
        Surface(
            modifier = Modifier.height(40.dp),
            shape = favShape,
            color = colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.1f)),
            onClick = { /* TODO: Favorite logic */ }
        ) {
            Box(modifier = Modifier.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.FavoriteBorder, null, modifier = Modifier.size(18.dp), tint = colorScheme.onSurfaceVariant)
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
            Slider(
                value = value,
                onValueChange = onValueChange,
                onValueChangeFinished = onValueChangeFinished,
                colors = sliderColors,
                modifier = modifier
            )
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

    val smoothProgress by animateFloatAsState(
        targetValue = AppState.progress,
        animationSpec = if (AppState.isPlaying && !isDragging) {
            tween(durationMillis = 500, easing = LinearEasing)
        } else {
            snap()
        }
    )

    val displayProgress = if (isDragging) dragValue else smoothProgress

    BoxWithConstraints(modifier = Modifier.fillMaxWidth().background(colorScheme.surfaceContainer)) {
        val width = maxWidth
        val isWide = width > 900.dp
        val isNarrow = width < 600.dp
        
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth().height(4.dp)) {
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
            
            Box(
                modifier = Modifier.fillMaxWidth().height(BottomPlayerHeight).padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                val playbackControls = @Composable {
                    val backInteractionSource = remember { MutableInteractionSource() }
                    val nextInteractionSource = remember { MutableInteractionSource() }
                    val playPauseInteractionSource = remember { MutableInteractionSource() }

                    val isBackPressed by backInteractionSource.collectIsPressedAsState()
                    val isNextPressed by nextInteractionSource.collectIsPressedAsState()
                    val isPlayPausePressed by playPauseInteractionSource.collectIsPressedAsState()

                    val playPauseWeight by animateFloatAsState(
                        targetValue = if (isPlayPausePressed) {
                            1.9f
                        } else if (isBackPressed || isNextPressed) {
                            1.1f
                        } else {
                            1.3f
                        },
                        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f)
                    )
                    val backButtonWeight by animateFloatAsState(
                        targetValue = if (isBackPressed) {
                            0.65f
                        } else if (isPlayPausePressed) {
                            0.35f
                        } else {
                            0.45f
                        },
                        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f)
                    )
                    val nextButtonWeight by animateFloatAsState(
                        targetValue = if (isNextPressed) {
                            0.65f
                        } else if (isPlayPausePressed) {
                            0.35f
                        } else {
                            0.45f
                        },
                        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f)
                    )

                    Row(
                        modifier = if (isWide) Modifier.width(380.dp) else Modifier.wrapContentWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        FilledIconButton(
                            onClick = { },
                            shape = RoundedCornerShape(50),
                            interactionSource = backInteractionSource,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = colorScheme.surfaceContainerHighest,
                                contentColor = colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .height(if (isWide) 48.dp else 42.dp)
                                .then(if (isWide) Modifier.weight(backButtonWeight) else Modifier.size(42.dp))
                        ) {
                            Icon(Icons.Outlined.SkipPrevious, null, modifier = Modifier.size(24.dp))
                        }
                        
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
                            onClick = { },
                            shape = RoundedCornerShape(50),
                            interactionSource = nextInteractionSource,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = colorScheme.surfaceContainerHighest,
                                contentColor = colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .height(if (isWide) 48.dp else 42.dp)
                                .then(if (isWide) Modifier.weight(nextButtonWeight) else Modifier.size(42.dp))
                        ) {
                            Icon(Icons.Outlined.SkipNext, null, modifier = Modifier.size(24.dp))
                        }
                        
                        if (!isNarrow) {
                            Spacer(Modifier.width(16.dp))
                            Text(
                                "${formatTime(playerPosition)} / ${formatTime(playerDuration)}", 
                                style = MaterialTheme.typography.labelSmall, 
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                val songInfo = @Composable {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            url = AppState.currentTrack?.thumbnail ?: "",
                            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)),
                            extractColor = true
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                AppState.currentTrack?.title ?: "Not Playing", 
                                style = if (isNarrow) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium, 
                                fontWeight = FontWeight.Medium, 
                                maxLines = 1, 
                                overflow = TextOverflow.Ellipsis
                            )
                            val artists = AppState.currentTrack?.artists ?: emptyList()
                            val album = AppState.currentTrack?.album
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                if (isNarrow) {
                                    artists.forEachIndexed { idx, artist ->
                                        Text(
                                            text = artist.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier
                                                .clickable(enabled = artist.id != null, indication = null, interactionSource = remember { MutableInteractionSource() }) {
                                                    artist.id?.let { AppState.fetchArtistData(it) }
                                                }
                                        )
                                        if (idx < artists.lastIndex) {
                                            Text(" / ", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                                        }
                                    }
                                } else {
                                    artists.forEachIndexed { idx, artist ->
                                        Text(
                                            text = artist.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier
                                                .clickable(enabled = artist.id != null, indication = null, interactionSource = remember { MutableInteractionSource() }) {
                                                    artist.id?.let { AppState.fetchArtistData(it) }
                                                }
                                        )
                                        if (idx < artists.lastIndex) {
                                            Text(" / ", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    if (album != null && album.name.isNotBlank()) {
                                        Text(" • ", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                                        Text(
                                            text = album.name,
                                            style = MaterialTheme.typography.bodySmall,
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
                        }
                        if (isWide) {
                            Spacer(Modifier.width(24.dp))
                            ShareHeartPill(colorScheme)
                        }
                    }
                }

                if (AppState.swapPlayerControls) {
                    Box(Modifier.align(Alignment.CenterStart)) { songInfo() }
                    Box(Modifier.align(Alignment.Center)) { playbackControls() }
                } else {
                    Box(Modifier.align(Alignment.CenterStart)) { playbackControls() }
                    Box(Modifier.align(Alignment.Center)) { songInfo() }
                }

                Row(modifier = Modifier.align(Alignment.CenterEnd), verticalAlignment = Alignment.CenterVertically) {
                    if (isWide) {
                        var isVolumeHovered by remember { mutableStateOf(false) }
                        
                        Box(
                            modifier = Modifier
                                .onPointerEvent(PointerEventType.Enter) { isVolumeHovered = true }
                                .onPointerEvent(PointerEventType.Exit) { isVolumeHovered = false }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AnimatedVisibility(
                                    visible = isVolumeHovered, 
                                    enter = fadeIn() + expandHorizontally(expandFrom = Alignment.End), 
                                    exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.End)
                                ) {
                                    MetrolistVolumeSlider(
                                        value = AppState.volume,
                                        onValueChange = { AppState.setVolumeLevel(it) },
                                        modifier = Modifier.width(180.dp).padding(horizontal = 8.dp),
                                        accentColor = colorScheme.primary,
                                        showIconInside = false
                                    )
                                }
                                
                                Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                                    Icon(
                                        if (AppState.volume > 0.66f) MetrolistVolumeUpIcon 
                                        else if (AppState.volume > 0.33f) MetrolistVolumeDownIcon 
                                        else if (AppState.volume > 0f) MetrolistVolumeMuteIcon
                                        else MetrolistVolumeOffIcon,
                                        null, 
                                        modifier = Modifier.size(20.dp), 
                                        tint = if (isVolumeHovered) colorScheme.primary else colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        
                        Spacer(Modifier.width(8.dp))
                    }

                    if (!isNarrow) {
                        AnimatedControlIconButton(
                            icon = when(AppState.repeatMode) {
                                RepeatMode.OFF -> Icons.Outlined.Repeat
                                RepeatMode.ALL -> Icons.Outlined.Repeat
                                RepeatMode.ONE -> Icons.Outlined.RepeatOne
                            }, 
                            onClick = { AppState.repeatMode = RepeatMode.entries[(AppState.repeatMode.ordinal + 1) % 3] }, 
                            contentColor = if (AppState.repeatMode != RepeatMode.OFF) colorScheme.primary else colorScheme.onSurfaceVariant, 
                            iconSize = 20.dp
                        )
                        
                        AnimatedControlIconButton(
                            icon = Icons.Outlined.Shuffle, 
                            onClick = { AppState.shuffleEnabled = !AppState.shuffleEnabled }, 
                            contentColor = if (AppState.shuffleEnabled) colorScheme.primary else colorScheme.onSurfaceVariant, 
                            iconSize = 20.dp
                        )
                    }
                    
                    val expandInteractionSource = remember { MutableInteractionSource() }
                    val isExpandPressed by expandInteractionSource.collectIsPressedAsState()
                    val isExpandHovered by expandInteractionSource.collectIsHoveredAsState()
                    
                    val expandRotation by animateFloatAsState(
                        targetValue = if (AppState.isExpanded) 180f else 0f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
                    )
                    
                    val expandScale by animateFloatAsState(
                        targetValue = when {
                            isExpandPressed -> 0.9f
                            isExpandHovered -> 1.15f
                            else -> 1f
                        },
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                    )

                    Surface(
                        onClick = { AppState.isExpanded = !AppState.isExpanded },
                        modifier = Modifier.size(40.dp).scale(expandScale),
                        shape = CircleShape,
                        color = if (isExpandHovered) colorScheme.surfaceVariant else Color.Transparent,
                        interactionSource = expandInteractionSource
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.KeyboardArrowUp, 
                                contentDescription = "Toggle Player", 
                                tint = if (isExpandHovered) colorScheme.primary else colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(28.dp).rotate(expandRotation)
                            )
                        }
                    }
                }
            }
        }
    }
}
