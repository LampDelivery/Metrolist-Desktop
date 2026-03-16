package com.metrolist.desktop.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme
import com.metrolist.desktop.state.AppState
import com.metrolist.desktop.constants.DefaultThemeColor
import org.jetbrains.skia.*

@Composable
fun InterFontFamily(): FontFamily {
    return FontFamily(
        androidx.compose.ui.text.platform.Font("font/Inter-Regular.ttf", FontWeight.Normal),
        androidx.compose.ui.text.platform.Font("font/Inter-Bold.ttf", FontWeight.Bold)
    )
}

@Composable
fun AppTypography(): Typography {
    val inter = InterFontFamily()
    val defaultTypography = Typography()
    return Typography(
        displayLarge = defaultTypography.displayLarge.copy(fontFamily = inter, fontWeight = FontWeight.Medium),
        displayMedium = defaultTypography.displayMedium.copy(fontFamily = inter, fontWeight = FontWeight.Medium),
        displaySmall = defaultTypography.displaySmall.copy(fontFamily = inter, fontWeight = FontWeight.Medium),
        headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = inter, fontWeight = FontWeight.Medium),
        headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = inter, fontWeight = FontWeight.Medium),
        headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = inter, fontWeight = FontWeight.Medium),
        titleLarge = defaultTypography.titleLarge.copy(fontFamily = inter, fontWeight = FontWeight.Medium, fontSize = 22.sp),
        titleMedium = defaultTypography.titleMedium.copy(fontFamily = inter, fontWeight = FontWeight.Medium, fontSize = 18.sp),
        titleSmall = defaultTypography.titleSmall.copy(fontFamily = inter, fontWeight = FontWeight.Medium, fontSize = 14.sp),
        bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = inter, fontSize = 16.sp),
        bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = inter, fontSize = 14.sp),
        bodySmall = defaultTypography.bodySmall.copy(fontFamily = inter, fontSize = 12.sp),
        labelLarge = defaultTypography.labelLarge.copy(fontFamily = inter, fontWeight = FontWeight.Medium),
        labelMedium = defaultTypography.labelMedium.copy(fontFamily = inter, fontWeight = FontWeight.Medium),
        labelSmall = defaultTypography.labelSmall.copy(fontFamily = inter, fontWeight = FontWeight.Medium)
    )
}

fun Color.toHsv(): FloatArray {
    val r = red; val g = green; val b = blue
    val max = maxOf(r, maxOf(g, b)); val min = minOf(r, minOf(g, b))
    val d = max - min
    val h = when {
        max == min -> 0f
        max == r -> (g - b) / d + (if (g < b) 6 else 0)
        max == g -> (b - r) / d + 2
        else -> (r - g) / d + 4
    }
    val s = if (max == 0f) 0f else d / max
    val v = max
    
    val normalizedH = (h * 60f + 360f) % 360f
    return floatArrayOf(normalizedH, s, v)
}

@Composable
fun MetrolistTheme(
    seedColor: Color,
    pureBlack: Boolean = AppState.pureBlack,
    content: @Composable () -> Unit
) {
    val isDark = AppState.getCurrentThemeIsDark()

    val baseColorScheme = rememberDynamicColorScheme(
        seedColor = seedColor,
        isDark = isDark,
        style = PaletteStyle.TonalSpot
    )

    val colorScheme = remember(baseColorScheme, pureBlack, isDark) {
        if (pureBlack && isDark) baseColorScheme.pureBlack() else baseColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography(),
        content = content
    )
}

fun ColorScheme.pureBlack() = copy(
    surface = Color.Black,
    background = Color.Black,
    surfaceContainer = Color.Black,
    surfaceContainerHigh = Color.Black,
    surfaceContainerLow = Color.Black,
    surfaceContainerLowest = Color.Black,
    surfaceContainerHighest = Color.Black,
    surfaceVariant = Color.Black
)

object PlayerColorExtractor {
    fun extractSeedColor(image: Image): Color {
        val bitmap = Bitmap()
        val imageInfo = ImageInfo(image.width, image.height, ColorType.RGBA_8888, ColorAlphaType.PREMUL)
        bitmap.allocPixels(imageInfo)
        val canvas = Canvas(bitmap)
        canvas.drawImage(image, 0f, 0f)
        
        val pixels = bitmap.readPixels(imageInfo, bitmap.rowBytes, 0, 0) ?: return DefaultThemeColor
        
        val colorsToPopulation = mutableMapOf<Int, Int>()
        val step = (image.width * image.height / 2000).coerceAtLeast(1)
        for (i in 0 until (image.width * image.height) step step) {
            val offset = i * 4
            if (offset + 3 >= pixels.size) break
            val r = pixels[offset].toInt() and 0xFF
            val g = pixels[offset + 1].toInt() and 0xFF
            val b = pixels[offset + 2].toInt() and 0xFF
            val argb = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
            
            val hsv = Color(argb).toHsv()
            if (hsv[2] < 0.1f || hsv[2] > 0.9f || hsv[1] < 0.05f) continue
            
            colorsToPopulation[argb] = colorsToPopulation.getOrDefault(argb, 0) + 1
        }

        val bestColorArgb = colorsToPopulation.maxByOrNull { entry ->
            val hsv = Color(entry.key).toHsv()
            val score = entry.value.toFloat() * (hsv[1] + 0.1f)
            score
        }?.key ?: DefaultThemeColor.toArgb()
        
        return Color(bestColorArgb)
    }

    /** Returns true when the artwork is predominantly bright/light. */
    fun isArtworkBright(image: Image): Boolean {
        val bitmap = Bitmap()
        val imageInfo = ImageInfo(image.width, image.height, ColorType.RGBA_8888, ColorAlphaType.PREMUL)
        bitmap.allocPixels(imageInfo)
        Canvas(bitmap).drawImage(image, 0f, 0f)
        val pixels = bitmap.readPixels(imageInfo, bitmap.rowBytes, 0, 0) ?: return false
        val step = (image.width * image.height / 1000).coerceAtLeast(1)
        var total = 0.0
        var count = 0
        for (i in 0 until (image.width * image.height) step step) {
            val off = i * 4
            if (off + 2 >= pixels.size) break
            val r = (pixels[off].toInt() and 0xFF) / 255f
            val g = (pixels[off + 1].toInt() and 0xFF) / 255f
            val b = (pixels[off + 2].toInt() and 0xFF) / 255f
            total += 0.299 * r + 0.587 * g + 0.114 * b
            count++
        }
        return count > 0 && (total / count) > 0.4
    }

    fun getGradientColors(accentColor: Color): List<Color> {
        val hsv = accentColor.toHsv()
        val c1 = Color.hsv(hsv[0], hsv[1].coerceAtLeast(0.7f), 0.4f)
        val c2 = Color.hsv((hsv[0] + 30f) % 360f, hsv[1].coerceIn(0.4f, 0.6f), 0.2f)
        val c3 = Color.hsv((hsv[0] - 30f + 360f) % 360f, hsv[1].coerceIn(0.3f, 0.5f), 0.1f)
        return listOf(c1, c2, c3, Color.Black)
    }
}
