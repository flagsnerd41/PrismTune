package com.metrolist.music.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

private fun prismWind(t: Float): Float {
    val base = 0.45f * sin(t * 0.35f) + 0.25f * sin(t * 0.83f + 1.7f) + 0.12f * sin(t * 2.1f + 0.4f)
    val gust = (sin(t * 0.17f + 2f) * 0.5f + 0.5f).pow(3f)
    return (0.25f + base * (0.4f + 0.9f * gust)).coerceIn(-1f, 1f)
}

private val leafPath = Path().apply {
    moveTo(0f, -1f)
    cubicTo(0.95f, -0.5f, 0.95f, 0.5f, 0f, 1f)
    cubicTo(-0.95f, 0.5f, -0.95f, -0.5f, 0f, -1f)
    close()
}

private val veinPath = Path().apply {
    moveTo(0f, -0.95f); lineTo(0f, 1f)
    for (y in listOf(-0.4f, 0.05f, 0.5f)) {
        moveTo(0f, y); lineTo(0.42f, y - 0.25f)
        moveTo(0f, y); lineTo(-0.42f, y - 0.25f)
    }
}

private val starPath = Path().apply {
    for (i in 0 until 10) {
        val r = if (i % 2 == 0) 1f else 0.45f
        val a = (-PI / 2 + i * PI / 5).toFloat()
        val px = cos(a) * r
        val py = sin(a) * r
        if (i == 0) moveTo(px, py) else lineTo(px, py)
    }
    close()
}

private val leafColors = listOf(
    Color(0xFFD2691E), Color(0xFFC0572B), Color(0xFFE0A526),
    Color(0xFF8D6E3F), Color(0xFF6B8E23), Color(0xFFB5651D),
)

private class Leaf(
    val size: Float, val fall: Float, val swayAmp: Float, val swayFreq: Float,
    val phase: Float, val spin: Float, val flipFreq: Float, val color: Color,
) {
    var x = 0f
    var y = 0f
    var angle = 0f
}

private class LeafSim {
    private val rnd = Random(7)
    val leaves = List(16) {
        Leaf(
            size = rnd.nextFloat() * 9f + 9f,
            fall = rnd.nextFloat() * 28f + 36f,
            swayAmp = rnd.nextFloat() * 22f + 10f,
            swayFreq = rnd.nextFloat() * 1.2f + 0.8f,
            phase = rnd.nextFloat() * 6.28f,
            spin = rnd.nextFloat() * 2f - 1f,
            flipFreq = rnd.nextFloat() * 2f + 1.5f,
            color = leafColors[rnd.nextInt(leafColors.size)],
        )
    }
    private var ready = false
    private var lastT = 0f

    fun step(t: Float, w: Float, h: Float, d: Float, wind: Float) {
        if (w <= 0f || h <= 0f) return
        if (!ready) {
            leaves.forEach { it.x = rnd.nextFloat() * w; it.y = rnd.nextFloat() * h; it.angle = rnd.nextFloat() * 360f }
            ready = true
            lastT = t
        }
        val dt = (t - lastT).coerceIn(0f, 0.05f)
        lastT = t
        for (l in leaves) {
            val sway = sin(t * l.swayFreq + l.phase)
            l.x += (wind * 70f * (0.6f + l.size / 20f) + sway * l.swayAmp) * d * dt
            l.y += (l.fall - abs(wind) * 10f + cos(t * l.swayFreq * 0.7f + l.phase) * 8f) * d * dt
            l.angle += (l.spin * 45f + wind * 70f + cos(t * l.swayFreq + l.phase) * 40f) * dt
            if (l.y > h + 30f * d) { l.y = -20f * d; l.x = rnd.nextFloat() * w }
            if (l.x > w + 30f * d) l.x = -20f * d else if (l.x < -30f * d) l.x = w + 20f * d
        }
    }
}

private fun DrawScope.drawLeaves(sim: LeafSim, t: Float) {
    val d = density
    for (l in sim.leaves) {
        val fade = (l.y / (60f * d)).coerceIn(0f, 1f)
        val flip = cos(t * l.flipFreq + l.phase)
        val sx = 0.25f + 0.75f * abs(flip)
        val px = l.size * d
        val c = if (flip < 0f) lerp(l.color, Color.Black, 0.25f) else l.color
        withTransform({
            translate(l.x, l.y)
            rotate(l.angle, Offset.Zero)
            scale(px * sx, px, Offset.Zero)
        }) {
            drawPath(leafPath, c.copy(alpha = 0.92f * fade))
            drawPath(veinPath, Color.Black.copy(alpha = 0.28f * fade), style = Stroke(width = 0.06f))
        }
    }
}

private fun DrawScope.drawStrand(t: Float, wind: Float, y0: Float, sag: Float, count: Int, seed: Int, colors: List<Color>) {
    val d = density
    val w = size.width
    val wire = Color(0xFF1E1A16).copy(alpha = 0.75f)
    fun pt(u: Float): Offset {
        val bell = sin(PI.toFloat() * u)
        val ox = (wind * 12f + sin(t * 1.2f + u * 5f + seed) * 2.2f * (0.5f + abs(wind))) * d * bell
        val oy = sin(t * 1.0f + u * 4f + seed) * 1.6f * d * abs(wind) * bell
        return Offset(-8f * d + (w + 16f * d) * u + ox, y0 * d + sag * d * 4f * u * (1f - u) + oy)
    }
    var prev = pt(0f)
    for (i in 1..28) {
        val p = pt(i / 28f)
        drawLine(wire, prev, p, strokeWidth = 1.3f * d)
        prev = p
    }
    for (k in 0 until count) {
        val p = pt((k + 0.5f) / count)
        val drop = (11f + (k % 3) * 3f) * d
        val ang = wind * 0.55f + sin(t * 1.8f + k * 1.3f + seed) * 0.14f * (0.4f + abs(wind))
        val b = Offset(p.x + sin(ang) * drop, p.y + cos(ang) * drop)
        drawLine(wire, p, b, strokeWidth = 1f * d)

        val n1 = sin(t * (3.1f + k * 0.37f) + k * 1.9f + seed)
        val n2 = sin(t * (7.3f + k * 0.11f) + k * 4.2f)
        val amt = 0.12f + 0.5f * abs(wind)
        var br = (0.82f + amt * (0.6f * n1 + 0.4f * n2)).coerceIn(0.25f, 1f)
        if (sin(t * (0.9f + k * 0.13f) + k) > 0.97f) br *= 0.4f

        val c = colors[(k + seed) % colors.size]
        val r = 5.5f * d
        drawCircle(c.copy(alpha = 0.10f * br), r * 3.4f, b)
        drawCircle(c.copy(alpha = 0.20f * br), r * 2.1f, b)
        withTransform({
            translate(b.x, b.y)
            rotate(ang * 57.3f + 18f * sin(t + k), Offset.Zero)
            scale(r, r, Offset.Zero)
        }) {
            drawPath(starPath, c.copy(alpha = (0.55f + 0.45f * br).coerceIn(0f, 1f)))
        }
        drawCircle(Color.White.copy(alpha = 0.55f * br), r * 0.28f, b)
    }
}

@Composable
fun PrismNature(
    active: Boolean,
    modifier: Modifier = Modifier,
    showLeaves: Boolean = true,
    showLights: Boolean = true,
) {
    if (!active) return
    val scheme = MaterialTheme.colorScheme
    val lightColors = remember(scheme.primary, scheme.tertiary) {
        listOf(
            Color(0xFFFFE9B0),
            lerp(scheme.primary, Color.White, 0.6f),
            Color(0xFFFFE9B0),
            lerp(scheme.tertiary, Color.White, 0.6f),
        )
    }
    var time by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        while (true) {
            withFrameNanos { time = (it - start) / 1_000_000_000f }
        }
    }
    val sim = remember { LeafSim() }
    Canvas(modifier = modifier) {
        val t = time
        val wind = prismWind(t)
        if (showLeaves) {
            sim.step(t, size.width, size.height, density, wind)
            drawLeaves(sim, t)
        }
        if (showLights) {
            drawStrand(t, wind, y0 = 30f, sag = 26f, count = 11, seed = 1, colors = lightColors)
            drawStrand(t, wind, y0 = 12f, sag = 34f, count = 9, seed = 2, colors = lightColors)
        }
    }
}
