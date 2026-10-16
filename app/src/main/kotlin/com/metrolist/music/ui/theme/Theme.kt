/**
 * PrismTune Project
 * Licensed under GPL-3.0 | See git history for contributors
 *
 * Material 3 theme: deep purple surfaces + magenta accent
 * (ArchiveTune-inspired glass / soft-card look).
 */

package com.metrolist.music.ui.theme

import android.graphics.Bitmap
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme
import com.materialkolor.score.Score

// Magenta / pink accent (play buttons, chips, selected nav)
val DefaultThemeColor = Color(0xFFFF80AB)

// Fixed dark surfaces for a consistent PrismTune look
private val PrismBackground = Color(0xFF0B0710)
private val PrismSurface = Color(0xFF14101A)
private val PrismSurfaceContainer = Color(0xFF1C1524)
private val PrismSurfaceHigh = Color(0xFF241C2E)
private val PrismSurfaceHighest = Color(0xFF2C2338)

@Composable
fun MetrolistTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    pureBlack: Boolean = false,
    themeColor: Color = DefaultThemeColor,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current

    // Keep PrismTune identity; do not follow system dynamic color
    val useSystemDynamicColor = false

    val baseColorScheme =
        if (useSystemDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else {
            rememberDynamicColorScheme(
                seedColor = themeColor,
                isDark = darkTheme,
                specVersion = ColorSpec.SpecVersion.SPEC_2025,
                style = PaletteStyle.TonalSpot,
            )
        }

    val colorScheme =
        remember(baseColorScheme, pureBlack, darkTheme) {
            when {
                darkTheme && pureBlack -> baseColorScheme.pureBlack(true)
                darkTheme ->
                    baseColorScheme.copy(
                        background = PrismBackground,
                        surface = PrismSurface,
                        surfaceContainerLowest = PrismBackground,
                        surfaceContainerLow = PrismSurface,
                        surfaceContainer = PrismSurfaceContainer,
                        surfaceContainerHigh = PrismSurfaceHigh,
                        surfaceContainerHighest = PrismSurfaceHighest,
                        primary = DefaultThemeColor,
                        secondary = Color(0xFFE91E8C),
                        tertiary = Color(0xFFCE93D8),
                    )
                else -> baseColorScheme
            }
        }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}

fun Bitmap.extractThemeColor(): Color =
    Color(
        Palette
            .from(this)
            .maximumColorCount(8)
            .generate()
            .rankedColors(1, DefaultThemeColor.toArgb())
            .first(),
    )

internal fun Palette.rankedColors(
    desiredColorCount: Int,
    fallbackColor: Int,
): List<Int> =
    Score.score(
        swatches.associate { it.rgb to it.population },
        desiredColorCount,
        fallbackColor,
        true,
    )

fun ColorScheme.pureBlack(apply: Boolean) =
    if (apply) {
        copy(
            surface = Color.Black,
            background = Color.Black,
            surfaceContainer = Color(0xFF121212),
            surfaceContainerHigh = Color(0xFF1A1A1A),
            surfaceContainerHighest = Color(0xFF222222),
        )
    } else {
        this
    }

val ColorSaver =
    object : Saver<Color, Int> {
        override fun restore(value: Int): Color = Color(value)

        override fun SaverScope.save(value: Color): Int = value.toArgb()
    }
