package com.screen.autolocker.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

data class AppPalette(
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val top: Color,
    val background: Color,
    val bottom: Color,
    val surface: Color,
    val text: Color,
    val muted: Color,
    val buttonText: Color,
    val dialSurface: Color,
    val dialSurfaceAlt: Color
)

private data class ThemeSeed(
    val primary: Color,
    val secondary: Color,
    val accent: Color
)

private fun parseCustomColor(input: String): Color? {
    val value = input.trim()

    fun hexToColor(hex: String): Color? {
        val normalized = hex.removePrefix("#")
        val parsed = when (normalized.length) {
            6 -> normalized.toLongOrNull(16)?.let { 0xFF000000 or it }
            8 -> normalized.toLongOrNull(16)
            else -> null
        } ?: return null
        return Color(parsed)
    }

    if (value.startsWith("#")) {
        return hexToColor(value)
    }

    if (value.startsWith("rgb(", ignoreCase = true) && value.endsWith(")")) {
        val parts = value.substringAfter("(").substringBeforeLast(")")
            .split(",")
            .map { it.trim().toIntOrNull() }
        if (parts.size == 3 && parts.all { it != null && it in 0..255 }) {
            return Color(parts[0]!!, parts[1]!!, parts[2]!!)
        }
    }

    if (value.startsWith("rgba(", ignoreCase = true) && value.endsWith(")")) {
        val parts = value.substringAfter("(").substringBeforeLast(")")
            .split(",")
            .map { it.trim() }
        if (parts.size == 4) {
            val r = parts[0].toIntOrNull()
            val g = parts[1].toIntOrNull()
            val b = parts[2].toIntOrNull()
            val a = parts[3].toFloatOrNull()
            if (r in 0..255 && g in 0..255 && b in 0..255 && a != null && a in 0f..1f) {
                return Color(r!!, g!!, b!!, (a * 255).toInt())
            }
        }
    }

    if (value.startsWith("custom:", ignoreCase = true)) {
        return parseCustomColor(value.substringAfter(":"))
    }

    return null
}

private fun Color.mix(other: Color, ratio: Float): Color {
    val r = red * (1f - ratio) + other.red * ratio
    val g = green * (1f - ratio) + other.green * ratio
    val b = blue * (1f - ratio) + other.blue * ratio
    val a = alpha * (1f - ratio) + other.alpha * ratio
    return Color(r, g, b, a)
}

private fun contrastTextFor(color: Color): Color {
    return if (color.luminance() > 0.42f) Color(0xFF16111E) else Color(0xFFF8F6FB)
}

private fun buildPalette(seed: ThemeSeed, isDark: Boolean, amoledPolish: Boolean): AppPalette {
    return if (isDark) {
        val isNearBlackSeed = seed.primary.luminance() < 0.02f
        val background = if (isNearBlackSeed) {
            if (amoledPolish) Color(0xFF010101) else Color(0xFF040404)
        } else {
            seed.primary.mix(Color.Black, if (amoledPolish) 0.93f else 0.9f)
        }
        val surface = if (isNearBlackSeed) {
            if (amoledPolish) Color(0xFF0A0A0A) else Color(0xFF141414)
        } else {
            seed.primary.mix(seed.secondary, 0.12f).mix(Color.Black, 0.78f)
        }
        val primary = if (isNearBlackSeed) {
            Color(0xFFF2F0EB)
        } else {
            seed.primary.mix(Color.White, 0.2f)
        }
        val secondary = if (isNearBlackSeed) {
            Color(0xFF1A1A1A)
        } else {
            seed.secondary.mix(Color.Black, 0.45f)
        }
        val accent = if (isNearBlackSeed) {
            Color(0xFFD9D4CC)
        } else {
            seed.accent.mix(Color.White, 0.18f)
        }
        val dialSurface = surface.mix(Color.White, 0.05f)
        val dialSurfaceAlt = surface.mix(primary, 0.18f)
        val top = if (isNearBlackSeed) {
            Color(0xFF000000)
        } else {
            background.mix(Color.Black, if (amoledPolish) 0.24f else 0.18f)
        }
        val bottom = if (isNearBlackSeed) {
            if (amoledPolish) Color(0xFF121212) else Color(0xFF1F1F1F)
        } else {
            background.mix(primary, 0.08f)
        }
        AppPalette(
            primary = primary,
            secondary = secondary,
            accent = accent,
            top = top,
            background = background,
            bottom = bottom,
            surface = surface,
            text = Color(0xFFF8F6FB),
            muted = Color(0xFFC9C0D6),
            buttonText = contrastTextFor(primary),
            dialSurface = dialSurface,
            dialSurfaceAlt = dialSurfaceAlt
        )
    } else {
        val background = seed.primary.mix(Color.White, 0.90f)
        val primary = seed.primary
        AppPalette(
            primary = primary,
            secondary = seed.secondary.mix(Color.White, 0.35f),
            accent = seed.accent,
            top = Color.White,
            background = background,
            bottom = background.mix(seed.secondary, 0.18f),
            surface = Color.White,
            text = Color(0xFF18131F),
            muted = Color(0xFF746A84),
            buttonText = contrastTextFor(primary),
            dialSurface = Color.White,
            dialSurfaceAlt = background.mix(seed.secondary, 0.15f)
        )
    }
}

fun paletteFor(theme: String, isDark: Boolean, amoledPolish: Boolean = false): AppPalette {
    val custom = parseCustomColor(theme)
    if (custom != null) {
        return buildPalette(
            ThemeSeed(
                primary = custom,
                secondary = custom.mix(Color.White, 0.65f),
                accent = custom.mix(Color.Black, 0.18f)
            ),
            isDark,
            amoledPolish
        )
    }

    val seed = when (theme) {
        "Blue" -> ThemeSeed(
            primary = Color(0xFF5D7CFF),
            secondary = Color(0xFFD8E3FF),
            accent = Color(0xFF3557E0)
        )
        "Sunset" -> ThemeSeed(
            primary = Color(0xFFF28482),
            secondary = Color(0xFFFFD9C7),
            accent = Color(0xFFE76F51)
        )
        "Forest" -> ThemeSeed(
            primary = Color(0xFF40916C),
            secondary = Color(0xFFD8F3DC),
            accent = Color(0xFF2D6A4F)
        )
        else -> ThemeSeed(
            primary = Color(0xFFB57EDC),
            secondary = Color(0xFFE8D5FA),
            accent = Color(0xFF8E59C2)
        )
    }
    return buildPalette(seed, isDark, amoledPolish)
}

fun backgroundBrush(palette: AppPalette): Brush {
    return Brush.verticalGradient(
        colorStops = arrayOf(
            0.0f to palette.top,
            0.72f to palette.background,
            1.0f to palette.bottom
        )
    )
}
