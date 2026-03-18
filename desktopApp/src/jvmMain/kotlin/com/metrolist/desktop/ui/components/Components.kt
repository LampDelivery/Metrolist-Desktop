package com.metrolist.desktop.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.metrolist.desktop.constants.ThumbnailCornerRadius
import com.metrolist.desktop.state.AppState
import com.metrolist.desktop.ui.theme.MetrolistVolumeDownIcon
import com.metrolist.desktop.ui.theme.MetrolistVolumeMuteIcon
import com.metrolist.desktop.ui.theme.MetrolistVolumeOffIcon
import com.metrolist.desktop.ui.theme.MetrolistVolumeUpIcon
import com.metrolist.desktop.ui.theme.PlayerColorExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI
import org.jetbrains.skia.Image as SkiaImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetrolistVolumeSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    showIconInside: Boolean = false
) {
    val volumeOffIcon = rememberVectorPainter(MetrolistVolumeOffIcon)
    val volumeMuteIcon = rememberVectorPainter(MetrolistVolumeMuteIcon)
    val volumeDownIcon = rememberVectorPainter(MetrolistVolumeDownIcon)
    val volumeUpIcon = rememberVectorPainter(MetrolistVolumeUpIcon)

    val currentIcon = when {
        value <= 0f -> volumeOffIcon
        value < 0.33f -> volumeMuteIcon
        value < 0.66f -> volumeDownIcon
        else -> volumeUpIcon
    }

    val iconColor = if (accentColor.luminance() > 0.5f) Color.Black else Color.White

    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.height(40.dp),
        enabled = enabled,
        thumb = {
            Box(
                Modifier
                    .width(4.dp)
                    .height(32.dp)
                    .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(2.dp))
            )
        },
        track = { sliderState ->
            val fraction = (sliderState.value - sliderState.valueRange.start) / (sliderState.valueRange.endInclusive - sliderState.valueRange.start)
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .fillMaxHeight()
                        .background(accentColor)
                ) {
                    if (showIconInside) {
                        Icon(
                            painter = currentIcon,
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 12.dp)
                                .size(20.dp),
                            tint = iconColor
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun AsyncImage(
    url: String, 
    modifier: Modifier = Modifier, 
    extractColor: Boolean = false, 
    shape: Shape = RoundedCornerShape(ThumbnailCornerRadius)
) {
    if (url.isBlank()) { 
        Box(modifier.background(MaterialTheme.colorScheme.surfaceVariant, shape), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.MusicNote, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
        }
        return 
    }
    var bitmap by remember(url) { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember(url) { mutableStateOf(true) }
    
    LaunchedEffect(url) {
        isLoading = true
        withContext(Dispatchers.IO) {
            try {
                val connection = URI(url).toURL().openConnection()
                connection.setRequestProperty("User-Agent", "Mozilla/5.0")
                val bytes = connection.getInputStream().readAllBytes()
                val skiaImage = SkiaImage.makeFromEncoded(bytes)
                val loaded = skiaImage.toComposeImageBitmap()
                bitmap = loaded
                if (extractColor) {
                    val sampledColor = PlayerColorExtractor.extractSeedColor(skiaImage)
                    val bright = PlayerColorExtractor.isArtworkBright(skiaImage)
                    withContext(Dispatchers.Main) {
                        AppState.seedColor = sampledColor
                        AppState.applyAutoNightMode(bright)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
    
    Box(modifier.clip(shape), contentAlignment = Alignment.Center) {
        if (bitmap != null) {
            Image(bitmap!!, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                } else {
                    Icon(Icons.Default.BrokenImage, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                }
            }
        }
    }
}

@Composable
fun MetrolistIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Transparent,
    contentColor: Color = LocalContentColor.current,
    iconSize: Dp = 24.dp,
    buttonSize: Dp = 48.dp,
    enabled: Boolean = true
) {
    Surface(
        onClick = if (enabled) onClick else ({}),
        modifier = modifier.size(buttonSize),
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor.copy(alpha = if (enabled) 1f else 0.38f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(iconSize))
        }
    }
}
