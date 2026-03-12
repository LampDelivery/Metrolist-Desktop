package com.metrolist.desktop.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt

// Reference: https://github.com/material-foundation/material-color-utilities

object MaterialYouColorScheme {
    // Converts ARGB int to Compose Color
    private fun argbToColor(argb: Int): Color = Color(
        red = ((argb shr 16) and 0xFF) / 255f,
        green = ((argb shr 8) and 0xFF) / 255f,
        blue = (argb and 0xFF) / 255f,
        alpha = ((argb ushr 24) and 0xFF) / 255f
    )

    // Converts Compose Color to ARGB int
    private fun colorToArgb(color: Color): Int {
        val a = (color.alpha * 255).roundToInt()
        val r = (color.red * 255).roundToInt()
        val g = (color.green * 255).roundToInt()
        val b = (color.blue * 255).roundToInt()
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }

    // Helper: Convert Compose Color to HCT
    private fun colorToHct(color: Color): FloatArray {
        val r = color.red
        val g = color.green
        val b = color.blue
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val delta = max - min
        val l = (max + min) / 2f
        val s = if (delta == 0f) 0f else delta / (1f - Math.abs(2 * l - 1f))
        val h = when {
            delta == 0f -> 0f
            max == r -> ((g - b) / delta) % 6f
            max == g -> ((b - r) / delta) + 2f
            else -> ((r - g) / delta) + 4f
        } * 60f
        return floatArrayOf((h + 360f) % 360f, s * 100f, l * 100f)
    }

    // Convert HCT to Compose Color
    private fun hctToColor(hue: Float, chroma: Float, tone: Float): Color {
        val c = (1f - Math.abs(2 * (tone / 100f) - 1f)) * (chroma / 100f)
        val x = c * (1f - Math.abs((hue / 60f) % 2 - 1f))
        val m = (tone / 100f) - c / 2f
        val (r1, g1, b1) = when {
            hue < 60f -> Triple(c, x, 0f)
            hue < 120f -> Triple(x, c, 0f)
            hue < 180f -> Triple(0f, c, x)
            hue < 240f -> Triple(0f, x, c)
            hue < 300f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }
        return Color((r1 + m).coerceIn(0f, 1f), (g1 + m).coerceIn(0f, 1f), (b1 + m).coerceIn(0f, 1f), 1f)
    }

    // Generate from a seed color
    fun darkScheme(seed: Color): ColorScheme {
        val hct = colorToHct(seed)
        val hue = hct[0]
        val chroma = hct[1].coerceIn(16f, 48f) 

        val primary = hctToColor(hue, chroma, 80f)
        val onPrimary = hctToColor(hue, 8f, 20f)
        val primaryContainer = hctToColor(hue, chroma, 30f)
        val onPrimaryContainer = Color.White
        val secondary = hctToColor((hue + 60f) % 360f, chroma * 0.5f, 80f)
        val onSecondary = Color.Black
        val secondaryContainer = hctToColor((hue + 60f) % 360f, chroma * 0.5f, 30f)
        val onSecondaryContainer = Color.White
        val background = hctToColor(hue, chroma * 0.1f, 6f)
        val onBackground = Color(0xFFE6E1E5)
        val surface = hctToColor(hue, chroma * 0.1f, 10f)
        val onSurface = Color(0xFFE6E1E5)
        val surfaceVariant = hctToColor(hue, chroma * 0.1f, 17f)
        val onSurfaceVariant = Color(0xFFCAC4D0)
        val surfaceContainer = hctToColor(hue, chroma * 0.1f, 12f)
        val outline = primary.copy(alpha = 0.25f)
        val outlineVariant = hctToColor(hue, 15f, 30f)
        val error = Color(0xFFF2B8B5)

        return darkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            surfaceContainer = surfaceContainer,
            outline = outline,
            outlineVariant = outlineVariant,
            error = error
        )
    }

    // Light mode, unused
    fun lightScheme(seed: Color): ColorScheme {
        val hct = colorToHct(seed)
        val hue = hct[0]
        val chroma = hct[1].coerceIn(16f, 48f)

        val primary = hctToColor(hue, chroma, 40f)
        val onPrimary = Color.White
        val primaryContainer = hctToColor(hue, chroma, 90f)
        val onPrimaryContainer = Color.Black
        val secondary = hctToColor((hue + 60f) % 360f, chroma * 0.5f, 40f)
        val onSecondary = Color.Black
        val secondaryContainer = hctToColor((hue + 60f) % 360f, chroma * 0.5f, 90f)
        val onSecondaryContainer = Color.Black
        val background = hctToColor(hue, chroma * 0.1f, 98f)
        val onBackground = Color.Black
        val surface = hctToColor(hue, chroma * 0.1f, 98f)
        val onSurface = Color.Black
        val surfaceVariant = hctToColor(hue, chroma * 0.1f, 90f)
        val onSurfaceVariant = Color(0xFF49454F)
        val surfaceContainer = hctToColor(hue, chroma * 0.1f, 94f)
        val outline = primary.copy(alpha = 0.25f)
        val outlineVariant = hctToColor(hue, 15f, 80f)
        val error = Color(0xFFB3261E)

        return lightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            surfaceContainer = surfaceContainer,
            outline = outline,
            outlineVariant = outlineVariant,
            error = error
        )
    }
}
