/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette

object PlayerColorExtractor {
    fun extractGradientColors(
        palette: Palette,
        fallbackColor: Int,
    ): List<Color> {
        val rankedColors = palette.rankedColors(3, fallbackColor)
        val primaryColor = Color(rankedColors.first())
        val accentColor = Color(
            palette.vibrantSwatch?.rgb
                ?: palette.lightVibrantSwatch?.rgb
                ?: rankedColors.getOrElse(1) { fallbackColor },
        )
        val deepColor = primaryColor.copy(
            red = primaryColor.red * 0.42f,
            green = primaryColor.green * 0.42f,
            blue = primaryColor.blue * 0.42f,
        )
        return listOf(primaryColor, accentColor, deepColor, Color.Black)
    }
}
