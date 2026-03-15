package com.metrolist.desktop.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import com.metrolist.desktop.state.AppState
import com.metrolist.desktop.ui.theme.MetrolistTheme
import com.metrolist.desktop.ui.theme.MetrolistPauseIcon
import com.metrolist.desktop.constants.SliderStyle

@Composable
fun MiniplayerWindow() {
    if (!AppState.showMiniplayer) return

    val isFocusMode = AppState.miniplayerFocusMode
    val windowSize = if (isFocusMode) DpSize(300.dp, 300.dp) else DpSize(380.dp, 120.dp)
    val windowState = remember { WindowState(size = windowSize) }

    // Sync window size when mode changes
    LaunchedEffect(isFocusMode) {
        windowState.size = windowSize
    }

    val seedColor = AppState.seedColor
    val isNightMode = AppState.miniplayerNightMode

    Window(
        onCloseRequest = { AppState.showMiniplayer = false },
        title = "Miniplayer",
        resizable = false,
        visible = true,
        undecorated = true,
        transparent = true,
        state = windowState,
        alwaysOnTop = true
    ) {
        MetrolistTheme(seedColor = seedColor, pureBlack = isNightMode) {
            val colorScheme = MaterialTheme.colorScheme

            WindowDraggableArea {
                if (isFocusMode) {
                    FocusModeMiniplayer(colorScheme, isNightMode)
                } else {
                    StandardMiniplayer(colorScheme, isNightMode)
                }
            }
        }
    }
}

@Composable
private fun StandardMiniplayer(colorScheme: ColorScheme, isNightMode: Boolean) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.3f)),
        tonalElevation = 0.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Blurred Background - Uses album art for a rich blur effect
            AsyncImage(
                url = AppState.currentTrack?.thumbnail ?: "",
                modifier = Modifier.fillMaxSize().blur(250.dp),
                shape = RoundedCornerShape(0.dp)
            )
            
            // Tint/Opacity layer - Increased alpha for lower transparency
            // Remove overlay in expanded mode (focus mode) so album art is not hidden
            if (!AppState.miniplayerFocusMode) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (isNightMode) Color.Black.copy(alpha = 0.99f)
                            else colorScheme.surfaceContainerHigh.copy(alpha = 0.98f)
                        )
                )
            }

            // Main content shifted slightly up to remove gap and make room for slider
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Album Art
                AsyncImage(
                    url = AppState.currentTrack?.thumbnail ?: "",
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = AppState.currentTrack?.title ?: "Not Playing",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val artists = AppState.currentTrack?.artists ?: emptyList()
                        Text(
                            text = artists.joinToString { it.name },
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        val album = AppState.currentTrack?.album
                        if (album != null && album.name.isNotBlank()) {
                            Text(" • ", style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant)
                            Text(
                                text = album.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    PlaybackControls(colorScheme)
                }
            }

            // Window Controls Overlay
            Row(
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { AppState.toggleMiniplayerFocusMode() },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Outlined.Fullscreen, null, modifier = Modifier.size(16.dp), tint = colorScheme.onSurfaceVariant)
                }
                IconButton(
                    onClick = { AppState.showMiniplayer = false },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Outlined.Close, null, modifier = Modifier.size(16.dp), tint = colorScheme.onSurfaceVariant)
                }
            }

            // Playbar at the bottom with enough height to prevent wavy/squiggles from clipping
            Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(12.dp)) {
                PlayerSlider(
                    value = AppState.progress,
                    onValueChange = { AppState.seekTo(it) },
                    style = AppState.sliderStyleState,
                    isPlaying = AppState.isPlaying,
                    color = colorScheme.primary,
                    modifier = Modifier.fillMaxSize(),
                    isFocused = false
                )
            }
        }
    }
}

@Composable
private fun FocusModeMiniplayer(colorScheme: ColorScheme, isNightMode: Boolean) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Surface(
        modifier = Modifier.fillMaxSize().hoverable(interactionSource),
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                url = AppState.currentTrack?.thumbnail ?: "",
                modifier = Modifier.fillMaxSize().then(
                    if (isHovered) Modifier.blur(150.dp) else Modifier
                ),
                shape = RoundedCornerShape(0.dp)
            )

            // Overlay only when hovered
            if (isHovered) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (isNightMode) Color.Black.copy(alpha = 0.99f)
                            else Color.Black.copy(alpha = 0.94f)
                        )
                )
            }

            AnimatedVisibility(
                visible = isHovered,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { AppState.toggleMiniplayerFocusMode() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Outlined.FullscreenExit, null, modifier = Modifier.size(18.dp), tint = Color.White)
                        }
                        IconButton(
                            onClick = { AppState.showMiniplayer = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Outlined.Close, null, modifier = Modifier.size(18.dp), tint = Color.White)
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = AppState.currentTrack?.title ?: "Not Playing",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = AppState.currentTrack?.artists?.joinToString { it.name } ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(Modifier.height(24.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            IconButton(onClick = { AppState.skipPrevious() }, modifier = Modifier.size(40.dp)) {
                                Icon(Icons.Outlined.SkipPrevious, null, modifier = Modifier.size(28.dp), tint = Color.White)
                            }

                            Surface(
                                onClick = { AppState.togglePlay() },
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        if (AppState.isPlaying) MetrolistPauseIcon else Icons.Outlined.PlayArrow,
                                        null,
                                        modifier = Modifier.size(32.dp),
                                        tint = Color.White
                                    )
                                }
                            }

                            IconButton(onClick = { AppState.skipNext() }, modifier = Modifier.size(40.dp)) {
                                Icon(Icons.Outlined.SkipNext, null, modifier = Modifier.size(28.dp), tint = Color.White)
                            }
                        }
                    }

                    PlayerSlider(
                        value = AppState.progress,
                        onValueChange = { AppState.seekTo(it) },
                        style = AppState.sliderStyleState,
                        isPlaying = AppState.isPlaying,
                        color = colorScheme.primary,
                        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(12.dp),
                        isFocused = false
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaybackControls(colorScheme: ColorScheme) {
    val backInteractionSource = remember { MutableInteractionSource() }
    val nextInteractionSource = remember { MutableInteractionSource() }
    val playPauseInteractionSource = remember { MutableInteractionSource() }

    val isBackPressed by backInteractionSource.collectIsPressedAsState()
    val isNextPressed by nextInteractionSource.collectIsPressedAsState()
    val isPlayPausePressed by playPauseInteractionSource.collectIsPressedAsState()

    val playPauseWeight by animateFloatAsState(
        targetValue = if (isPlayPausePressed) 1.8f else if (isBackPressed || isNextPressed) 1.1f else 1.3f,
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
        modifier = Modifier.width(180.dp).height(40.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledIconButton(
            onClick = { AppState.skipPrevious() },
            shape = RoundedCornerShape(50),
            interactionSource = backInteractionSource,
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = colorScheme.surfaceContainerHighest.copy(alpha = 1.0f), contentColor = colorScheme.onSurface),
            modifier = Modifier.fillMaxHeight().weight(backButtonWeight)
        ) { Icon(Icons.Outlined.SkipPrevious, null, modifier = Modifier.size(20.dp)) }

        Spacer(Modifier.width(8.dp))

        MetrolistPlayPauseButton(
            isPlaying = AppState.isPlaying,
            onClick = { AppState.togglePlay() },
            colorScheme = colorScheme,
            isWide = false,
            interactionSource = playPauseInteractionSource,
            modifier = Modifier.fillMaxHeight().weight(playPauseWeight)
        )

        Spacer(Modifier.width(8.dp))

        FilledIconButton(
            onClick = { AppState.skipNext() },
            shape = RoundedCornerShape(50),
            interactionSource = nextInteractionSource,
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = colorScheme.surfaceContainerHighest.copy(alpha = 1.0f), contentColor = colorScheme.onSurface),
            modifier = Modifier.fillMaxHeight().weight(nextButtonWeight)
        ) { Icon(Icons.Outlined.SkipNext, null, modifier = Modifier.size(20.dp)) }
    }
}
