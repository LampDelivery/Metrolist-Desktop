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
import androidx.compose.material.icons.automirrored.outlined.VolumeDown
import androidx.compose.material.icons.automirrored.outlined.VolumeMute
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.metrolist.shared.model.*
import com.metrolist.desktop.state.AppState
import com.metrolist.desktop.constants.*
import com.metrolist.desktop.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun AnimatedGradientBackground(color: Color) {
    val infiniteTransition = rememberInfiniteTransition()
    
    val hsv = color.toHsv()
    val baseColor = Color.hsv(hsv[0], hsv[1].coerceAtLeast(0.6f), 0.4f)
    val altColor1 = Color.hsv((hsv[0] + 40f + 360f) % 360f, hsv[1].coerceAtLeast(0.5f), 0.3f)
    val altColor2 = Color.hsv((hsv[0] - 40f + 360f) % 360f, hsv[1].coerceAtLeast(0.5f), 0.2f)

    val xOffset1 by infiniteTransition.animateFloat(
        initialValue = -0.5f, targetValue = 0.5f,
        animationSpec = infiniteRepeatable(tween(15000, easing = LinearEasing), AnimationRepeatMode.Reverse)
    )
    val yOffset1 by infiniteTransition.animateFloat(
        initialValue = -0.5f, targetValue = 0.5f,
        animationSpec = infiniteRepeatable(tween(18000, easing = LinearEasing), AnimationRepeatMode.Reverse)
    )
    
    val xOffset2 by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = -0.5f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing), AnimationRepeatMode.Reverse)
    )
    val yOffset2 by infiniteTransition.animateFloat(
        initialValue = -0.3f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing), AnimationRepeatMode.Reverse)
    )

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Canvas(modifier = Modifier.fillMaxSize().blur(120.dp)) {
            drawRect(color = Color.Black)
            drawCircle(
                color = baseColor,
                radius = size.maxDimension * 0.8f,
                center = center + androidx.compose.ui.geometry.Offset(size.width * xOffset1, size.height * yOffset1),
                alpha = 0.6f
            )
            drawCircle(
                color = altColor1,
                radius = size.maxDimension * 0.7f,
                center = center + androidx.compose.ui.geometry.Offset(size.width * xOffset2, size.height * yOffset2),
                alpha = 0.5f
            )
            drawCircle(
                color = altColor2,
                radius = size.maxDimension * 0.9f,
                center = center + androidx.compose.ui.geometry.Offset(size.width * -xOffset1, size.height * -yOffset2),
                alpha = 0.4f
            )
        }
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
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

    Surface(
        modifier = Modifier
            .width(520.dp)
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
                    Icon(if (AppState.isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow, null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(AppState.currentTrack?.title ?: "Not Playing", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(AppState.currentTrack?.artists?.joinToString { it.name } ?: "", style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            MetrolistIconButton(Icons.Outlined.FavoriteBorder, {}, contentColor = colorScheme.onSurfaceVariant)
            MetrolistIconButton(if (AppState.isExpanded) Icons.Outlined.CloseFullscreen else Icons.Outlined.OpenInFull, { AppState.isExpanded = !AppState.isExpanded }, contentColor = colorScheme.onSurfaceVariant, iconSize = 20.dp)
            Spacer(Modifier.width(8.dp))
        }
    }
}

@Composable
fun ExpandedPlayerView(colorScheme: ColorScheme) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (AppState.animatedGradient) {
            AnimatedGradientBackground(colorScheme.primary)
        } else {
            val gradientColors = remember(colorScheme.primary) { 
                PlayerColorExtractor.getGradientColors(colorScheme.primary) 
            }
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(gradientColors)))
        }
        
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                AsyncImage(
                    url = AppState.currentTrack?.thumbnail ?: "",
                    modifier = Modifier.size(480.dp).shadow(32.dp, RoundedCornerShape(ExpandedThumbnailCornerRadius)),
                    shape = RoundedCornerShape(ExpandedThumbnailCornerRadius),
                    extractColor = true
                )
            }

            Surface(
                modifier = Modifier.width(450.dp).fillMaxHeight(),
                color = Color.Transparent
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                    var selectedTab by remember { mutableIntStateOf(0) }
                    val tabs = listOf("UP NEXT", "LYRICS", "RELATED")

                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = Color.White,
                        indicator = { tabPositions ->
                            if (selectedTab < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = Color.White
                                )
                            }
                        },
                        divider = {}
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium, color = if (selectedTab == index) Color.White else Color.White.copy(alpha = 0.7f)) }
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Box(modifier = Modifier.weight(1f)) {
                        when (selectedTab) {
                            0 -> Text("Queue functionality coming soon", color = Color.White.copy(alpha = 0.7f))
                            1 -> Text("Lyrics coming soon...", color = Color.White.copy(alpha = 0.7f))
                            2 -> Text("Related tracks coming soon", color = Color.White.copy(alpha = 0.7f))
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { }.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Timer, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Sleep Timer", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.7f))
                    }
                }
            }
        }
    }
}

@Composable
fun ShareHeartPill(colorScheme: ColorScheme) {
    Surface(
        modifier = Modifier.height(40.dp),
        shape = CircleShape,
        color = colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.clickable { }.padding(horizontal = 12.dp)) {
                Icon(MetrolistShareIcon, null, modifier = Modifier.size(18.dp), tint = colorScheme.onSurfaceVariant)
            }
            VerticalDivider(modifier = Modifier.height(20.dp), thickness = 1.dp, color = colorScheme.outline.copy(alpha = 0.2f))
            Box(modifier = Modifier.clickable { }.padding(horizontal = 12.dp)) {
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
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
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

    Surface(
        modifier = modifier
            .size(56.dp)
            .scale(scale)
            .hoverable(interactionSource),
        shape = CircleShape,
        color = if (isHovered) colorScheme.primaryContainer.copy(alpha = 0.9f) else colorScheme.primaryContainer,
        onClick = onClick,
        interactionSource = interactionSource
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = colorScheme.onPrimaryContainer
            )
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
                    val fraction = (value - sliderState.valueRange.start) / (sliderState.valueRange.endInclusive - sliderState.valueRange.start)
                    val trackHeight = if (isFocused) 6.dp else 4.dp
                    Box(Modifier.fillMaxWidth().height(trackHeight).background(color.copy(alpha = 0.2f), CircleShape)) {
                        Box(Modifier.fillMaxWidth(fraction).fillMaxHeight().background(color, CircleShape))
                    }
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

    Column(modifier = Modifier.fillMaxWidth().background(colorScheme.surfaceContainer)) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AnimatedControlIconButton(
                        icon = Icons.Outlined.SkipPrevious, 
                        onClick = { }, 
                        contentColor = colorScheme.onSurface, 
                        iconSize = 28.dp
                    )
                    
                    Spacer(Modifier.width(8.dp))
                    
                    MetrolistPlayPauseButton(
                        isPlaying = AppState.isPlaying,
                        onClick = { AppState.togglePlay() },
                        colorScheme = colorScheme
                    )
                    
                    Spacer(Modifier.width(8.dp))
                    
                    AnimatedControlIconButton(
                        icon = Icons.Outlined.SkipNext, 
                        onClick = { }, 
                        contentColor = colorScheme.onSurface, 
                        iconSize = 28.dp
                    )
                    
                    Spacer(Modifier.width(16.dp))
                    
                    Text(
                        "${formatTime(playerPosition)} / ${formatTime(playerDuration)}", 
                        style = MaterialTheme.typography.labelSmall, 
                        color = colorScheme.onSurfaceVariant
                    )
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
                            style = MaterialTheme.typography.bodyMedium, 
                            fontWeight = FontWeight.Medium, 
                            maxLines = 1, 
                            overflow = TextOverflow.Ellipsis
                        )
                        val artistText = AppState.currentTrack?.artists?.joinToString { it.name } ?: "Unknown Artist"
                        val albumText = AppState.currentTrack?.album?.name ?: "Unknown Album"
                        Text(
                            "$artistText • $albumText", 
                            style = MaterialTheme.typography.bodySmall, 
                            color = colorScheme.onSurfaceVariant, 
                            maxLines = 1, 
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.width(24.dp))
                    ShareHeartPill(colorScheme)
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
                
                IconButton(onClick = { AppState.isExpanded = !AppState.isExpanded }) {
                    Icon(
                        if (AppState.isExpanded) Icons.Outlined.ArrowDropDown else Icons.Outlined.ArrowDropUp, 
                        null, 
                        tint = colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
