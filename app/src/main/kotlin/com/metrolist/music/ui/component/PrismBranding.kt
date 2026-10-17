/**
 * PrismTune branding: welcome splash + glowing header
 */

package com.metrolist.music.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.geometry.Size
import androidx.datastore.preferences.core.intPreferencesKey
import java.util.Calendar
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Column
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import com.metrolist.music.utils.rememberPreference
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object WelcomeSplashState {
    var shown: Boolean = false
}

@Composable
fun WelcomeSplash(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val timeNanos = remember { mutableLongStateOf(0L) }
    val overlayAlpha = remember { Animatable(1f) }
    val greeting = remember { prismGreeting() }
    val leaves = remember { buildPrismLeaves(14, 9f, 18f, PrismLeafColors, 1f) }
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val gradient = listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.primaryContainer)

    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it } - start
            timeNanos.longValue = now
            if (now >= 3_300_000_000L) break
        }
        overlayAlpha.animateTo(0f, tween(500))
        onFinished()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .fillMaxSize()
                .graphicsLayer { alpha = overlayAlpha.value }
                .background(Brush.verticalGradient(gradient))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val t = timeNanos.longValue / 1_000_000_000f
            val d = 1.dp.toPx()
            val wind = prismWind(t)
            val drop = (t / 1.2f).coerceIn(0f, 1f)
            val eased = 1f - (1f - drop) * (1f - drop)
            val level = ((t - 0.9f) / 0.8f).coerceIn(0f, 1f)
            val leafFade = ((t - 0.5f) / 0.8f).coerceIn(0f, 1f)
            drawPrismLeaves(leaves, t, size.width, size.height, d, wind, primary, leafFade)
            drawPrismFairyLights(
                PrismStrands,
                t,
                size.width,
                size.height,
                d,
                wind,
                primary,
                secondary,
                tertiary,
                level,
                -(1f - eased) * size.height * 0.3f,
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = greeting,
                color = onSurface.copy(alpha = 0.85f),
                style = MaterialTheme.typography.titleLarge,
                modifier =
                    Modifier.graphicsLayer {
                        alpha = ((timeNanos.longValue / 1_000_000_000f - 0.2f) / 0.6f).coerceIn(0f, 1f)
                    },
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                "PrismTune".forEachIndexed { i, c ->
                    PrismSplashLetter(
                        ch = c,
                        index = i,
                        timeOf = { timeNanos.longValue / 1_000_000_000f },
                        glow = primary,
                        textColor = onSurface,
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Let the music glow",
                color = onSurface.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyLarge,
                modifier =
                    Modifier.graphicsLayer {
                        alpha = ((timeNanos.longValue / 1_000_000_000f - 2.1f) / 0.6f).coerceIn(0f, 1f)
                    },
            )
        }
    }
}

@Composable
private fun PrismSplashLetter(
    ch: Char,
    index: Int,
    timeOf: () -> Float,
    glow: Color,
    textColor: Color,
) {
    val t = timeOf()
    val a = ((t - (1.0f + index * 0.09f)) / 0.25f).coerceIn(0f, 1f)
    val sweep = (t - 1.3f) * 7f
    val g = (1f - abs(sweep - index) / 2.2f).coerceIn(0f, 1f)
    Text(
        text = ch.toString(),
        color = textColor,
        style =
            MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Bold,
                shadow =
                    Shadow(
                        color = glow.copy(alpha = 0.35f + 0.65f * g),
                        offset = Offset.Zero,
                        blurRadius = 18f + 40f * g,
                    ),
            ),
        modifier =
            Modifier.graphicsLayer {
                alpha = a
                translationY = (1f - a) * 24f
            },
    )
}

private fun prismGreeting(): String {
    val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        h < 5 -> "Good night"
        h < 12 -> "Good morning"
        h < 17 -> "Good afternoon"
        h < 22 -> "Good evening"
        else -> "Good night"
    }
}

@Composable
fun PrismHeader(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val pulse by rememberInfiniteTransition(label = "headerGlow").animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "headerGlowPulse",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .height(120.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            primary.copy(alpha = 0.60f),
                            secondary.copy(alpha = 0.45f),
                            tertiary.copy(alpha = 0.60f),
                        ),
                    ),
                ).drawBehind {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.16f * pulse),
                        radius = size.height * 0.9f,
                        center = Offset(size.width * 0.82f, size.height * 0.1f),
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.10f * pulse),
                        radius = size.height * 0.6f,
                        center = Offset(size.width * 0.12f, size.height * 0.95f),
                    )
                },
    ) {
        Text(
            text = "PrismTune",
            color = Color.White,
            style =
                MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    shadow =
                        Shadow(
                            color = Color.White.copy(alpha = pulse),
                            offset = Offset.Zero,
                            blurRadius = 30f,
                        ),
                ),
        )
    }
}

@Composable
fun PrismTitleBanner(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val pulse by rememberInfiniteTransition(label = "titleGlow").animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "titleGlowPulse",
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .height(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        listOf(primary.copy(alpha = 0.60f), secondary.copy(alpha = 0.45f), tertiary.copy(alpha = 0.60f)),
                    ),
                ).drawBehind {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.16f * pulse),
                        radius = size.height * 0.9f,
                        center = Offset(size.width * 0.85f, size.height * 0.1f),
                    )
                }.padding(horizontal = 16.dp),
    ) {
        Text(
            text = "PrismTune",
            color = Color.White,
            style =
                MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    shadow = Shadow(color = Color.White.copy(alpha = pulse), offset = Offset.Zero, blurRadius = 24f),
                ),
        )
    }
}

val PrismNatureKey = booleanPreferencesKey("prismNatureEnabled")
val PrismWindStrengthKey = floatPreferencesKey("prismWindStrength")
val PrismSeasonKey = intPreferencesKey("prismSeason")

private class PrismLeaf(
    val xFrac: Float,
    val yPhase: Float,
    val speed: Float,
    val sizeDp: Float,
    val swayAmpDp: Float,
    val swayFreq: Float,
    val swayPhase: Float,
    val spin: Float,
    val spinPhase: Float,
    val flipFreq: Float,
    val flipPhase: Float,
    val color: Color,
    val depth: Float,
)

private class PrismDot(
    val x: Float,
    val y: Float,
    val speed: Float,
    val size: Float,
    val amp: Float,
    val freq: Float,
    val phase: Float,
)

private class PrismStrand(
    val y0: Float,
    val y1: Float,
    val sag: Float,
    val count: Int,
    val seed: Float,
)

private val PrismLeafColors =
    listOf(
        Color(0xFFC2611F),
        Color(0xFFD98324),
        Color(0xFFB8860B),
        Color(0xFF8C4A1E),
        Color(0xFFA63D1B),
        Color(0xFFE0A526),
        Color(0xFF7A5C1E),
    )

private val PrismPetalColors =
    listOf(
        Color(0xFFF8BBD0),
        Color(0xFFF48FB1),
        Color(0xFFFFCDD2),
        Color(0xFFF06292),
    )

private val PrismStrands =
    listOf(
        PrismStrand(0.040f, 0.050f, 0.060f, 12, 0f),
        PrismStrand(0.022f, 0.030f, 0.035f, 9, 1f),
    )

private val PrismHomeStrands =
    listOf(
        PrismStrand(0.05f, 0.08f, 0.22f, 10, 0f),
        PrismStrand(0.02f, 0.03f, 0.12f, 7, 1f),
    )

private fun buildPrismLeaves(
    count: Int,
    minSize: Float,
    maxSize: Float,
    colors: List<Color>,
    speedMul: Float,
): List<PrismLeaf> {
    val r = Random(11 + count)
    return List(count) {
        val depth = r.nextFloat()
        PrismLeaf(
            xFrac = r.nextFloat(),
            yPhase = r.nextFloat(),
            speed = (0.045f + 0.05f * r.nextFloat()) * speedMul,
            sizeDp = minSize + (maxSize - minSize) * depth,
            swayAmpDp = 14f + 26f * r.nextFloat(),
            swayFreq = 0.6f + 0.9f * r.nextFloat(),
            swayPhase = 6.28f * r.nextFloat(),
            spin = (r.nextFloat() - 0.5f) * 90f,
            spinPhase = 360f * r.nextFloat(),
            flipFreq = 0.8f + 1.6f * r.nextFloat(),
            flipPhase = 6.28f * r.nextFloat(),
            color = colors[r.nextInt(colors.size)],
            depth = depth,
        )
    }
}

private fun buildPrismDots(
    seed: Int,
    count: Int,
    minSize: Float,
    maxSize: Float,
    minSpeed: Float,
    maxSpeed: Float,
    maxAmp: Float,
): List<PrismDot> {
    val r = Random(seed)
    return List(count) {
        PrismDot(
            x = r.nextFloat(),
            y = r.nextFloat(),
            speed = minSpeed + (maxSpeed - minSpeed) * r.nextFloat(),
            size = minSize + (maxSize - minSize) * r.nextFloat(),
            amp = 2f + maxAmp * r.nextFloat(),
            freq = 0.5f + 1.5f * r.nextFloat(),
            phase = 6.28f * r.nextFloat(),
        )
    }
}

private class PrismField {
    val leaves = buildPrismLeaves(18, 9f, 20f, PrismLeafColors, 1f)
    val petals = buildPrismLeaves(26, 5f, 9f, PrismPetalColors, 0.7f)
    val snow = buildPrismDots(21, 70, 1.2f, 3.6f, 0.04f, 0.09f, 12f)
    val rain = buildPrismDots(22, 80, 10f, 18f, 0.7f, 1.2f, 0f)
    val flies = buildPrismDots(23, 22, 1f, 2f, 0f, 0f, 14f)
    val butterflies = buildPrismDots(24, 3, 0f, 0f, 0f, 0f, 0f)
    val sparkles = buildPrismDots(25, 36, 0.8f, 2.0f, 0.01f, 0.03f, 0f)
    val stars = buildPrismDots(26, 40, 0.8f, 1.8f, 0f, 0f, 0f)
}

private val PrismLeafPath =
    Path().apply {
        moveTo(0f, -1f)
        cubicTo(0.9f, -0.55f, 0.85f, 0.55f, 0f, 1f)
        cubicTo(-0.85f, 0.55f, -0.9f, -0.55f, 0f, -1f)
        close()
    }

private fun prismWind(t: Float): Float {
    val gust = (sin(t * 0.37f) + 0.6f * sin(t * 0.83f + 1.7f) + 0.8f * sin(t * 0.19f + 4f)) / 2.4f
    return 0.3f + 0.7f * gust
}

private fun prismResolveSeason(pref: Int): Int {
    if (pref in 1..5) return pref
    return when (Calendar.getInstance().get(Calendar.MONTH)) {
        11, 0, 1 -> 2
        2, 3, 4 -> 3
        5, 6, 7 -> 4
        else -> 1
    }
}

private fun prismIsNight(): Boolean {
    val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return h >= 22 || h < 5
}

private fun prismWrap(
    x: Float,
    span: Float,
    pad: Float,
): Float {
    var v = (x + pad) % span
    if (v < 0f) v += span
    return v - pad
}

private fun DrawScope.drawPrismLeaves(
    leaves: List<PrismLeaf>,
    t: Float,
    w: Float,
    h: Float,
    d: Float,
    wind: Float,
    tint: Color,
    alphaMul: Float = 1f,
    veins: Boolean = true,
) {
    val margin = 40f * d
    val travel = h + margin * 2f
    for (leaf in leaves) {
        val raw = (leaf.yPhase + t * leaf.speed) % 1f
        val y = raw * travel - margin
        val swayWave = sin(t * leaf.swayFreq + leaf.swayPhase)
        val sway = swayWave * leaf.swayAmpDp * d
        val drift = wind * (24f + 40f * leaf.depth) * d * (0.4f + raw)
        val x = prismWrap(leaf.xFrac * w + sway + drift, w + 120f * d, 60f * d)
        val rot = leaf.spinPhase + t * leaf.spin + wind * 25f + swayWave * 30f
        val flip = cos(t * leaf.flipFreq + leaf.flipPhase)
        val sx = max(0.22f, abs(flip))
        val shade = (0.78f + 0.22f * flip).coerceIn(0.55f, 1f)
        val shaded = lerp(Color.Black, leaf.color, shade)
        val body = lerp(shaded, tint, 0.15f).copy(alpha = (0.55f + 0.4f * leaf.depth) * alphaMul)
        val vein = Color.Black.copy(alpha = 0.28f * alphaMul)
        val sizePx = leaf.sizeDp * d
        withTransform({
            translate(x, y)
            rotate(rot, Offset.Zero)
            scale(sx * sizePx, sizePx, Offset.Zero)
        }) {
            drawPath(PrismLeafPath, color = body)
            if (veins) {
                drawLine(color = vein, start = Offset(0f, -0.8f), end = Offset(0f, 1.3f), strokeWidth = 0.06f)
            }
        }
    }
}

private fun DrawScope.drawPrismSnow(
    dots: List<PrismDot>,
    t: Float,
    w: Float,
    h: Float,
    d: Float,
    wind: Float,
) {
    for (p in dots) {
        val raw = (p.y + t * p.speed) % 1f
        val y = raw * (h + 20f * d) - 10f * d
        val x = prismWrap(p.x * w + sin(t * p.freq + p.phase) * p.amp * d + wind * 30f * d * (0.3f + raw), w + 40f * d, 20f * d)
        drawCircle(color = Color.White.copy(alpha = 0.55f + 0.1f * p.size), radius = p.size * d, center = Offset(x, y))
    }
}

private fun DrawScope.drawPrismRain(
    dots: List<PrismDot>,
    t: Float,
    w: Float,
    h: Float,
    d: Float,
    wind: Float,
) {
    val slant = (3f + wind * 8f) * d
    for (p in dots) {
        val raw = (p.y + t * p.speed) % 1f
        val y = raw * (h + 40f * d) - 20f * d
        val x = prismWrap(p.x * w + raw * wind * 40f * d, w + 60f * d, 30f * d)
        drawLine(
            color = Color.White.copy(alpha = 0.28f),
            start = Offset(x, y),
            end = Offset(x - slant, y + p.size * d),
            strokeWidth = 1.2f * d,
        )
    }
}

private fun DrawScope.drawPrismFireflies(
    dots: List<PrismDot>,
    t: Float,
    w: Float,
    h: Float,
    d: Float,
) {
    val fly = Color(0xFFE6F27A)
    for (p in dots) {
        val cx = p.x * w + sin(t * p.freq * 0.6f + p.phase) * p.amp * 3f * d
        val cy = p.y * h + cos(t * p.freq * 0.5f + p.phase * 1.3f) * p.amp * 2f * d
        val glow = 0.5f + 0.5f * sin(t * 1.7f + p.phase * 2f)
        val center = Offset(cx, cy)
        drawCircle(
            brush =
                Brush.radialGradient(
                    colors = listOf(fly.copy(alpha = 0.55f * glow), Color.Transparent),
                    center = center,
                    radius = 14f * d,
                ),
            radius = 14f * d,
            center = center,
        )
        drawCircle(color = fly.copy(alpha = 0.4f + 0.6f * glow), radius = 2f * d, center = center)
    }
}

private fun DrawScope.drawPrismButterflies(
    dots: List<PrismDot>,
    t: Float,
    w: Float,
    h: Float,
    d: Float,
    c1: Color,
    c2: Color,
) {
    for ((i, p) in dots.withIndex()) {
        val x = w * (0.15f + 0.7f * (0.5f + 0.5f * sin(t * 0.11f * (1f + i * 0.3f) + p.phase)))
        val y = h * (0.25f + 0.5f * (0.5f + 0.5f * sin(t * 0.17f * (1f + i * 0.2f) + p.phase * 2f))) + sin(t * 2.3f + p.phase) * 10f * d
        val flap = abs(cos(t * 9f + p.phase))
        val wingW = (2f + 7f * flap) * d
        val color = lerp(if (i % 2 == 0) c1 else c2, Color.White, 0.35f).copy(alpha = 0.85f)
        withTransform({
            translate(x, y)
            rotate(sin(t * 0.5f + p.phase) * 25f, Offset.Zero)
        }) {
            drawOval(color = color, topLeft = Offset(-wingW, -5f * d), size = Size(wingW, 10f * d))
            drawOval(color = color, topLeft = Offset(0f, -5f * d), size = Size(wingW, 10f * d))
            drawLine(color = Color.Black.copy(alpha = 0.6f), start = Offset(0f, -5f * d), end = Offset(0f, 5f * d), strokeWidth = 1.4f * d)
        }
    }
}

private fun DrawScope.drawPrismSparkles(
    dots: List<PrismDot>,
    t: Float,
    w: Float,
    h: Float,
    d: Float,
    color: Color,
) {
    for (p in dots) {
        val tw = sin(t * (0.8f + p.freq) + p.phase)
        val a = (tw * tw * tw).coerceAtLeast(0f)
        if (a > 0.02f) {
            var yy = (p.y - t * p.speed) % 1f
            if (yy < 0f) yy += 1f
            val x = p.x * w + sin(t * 0.2f + p.phase) * 6f * d
            drawCircle(color = color.copy(alpha = a * 0.8f), radius = p.size * d, center = Offset(x, yy * h))
        }
    }
}

private fun DrawScope.drawPrismStars(
    dots: List<PrismDot>,
    t: Float,
    w: Float,
    h: Float,
    d: Float,
) {
    for (p in dots) {
        val a = 0.35f + 0.65f * (0.5f + 0.5f * sin(t * p.freq + p.phase))
        drawCircle(color = Color.White.copy(alpha = a), radius = p.size * d, center = Offset(p.x * w, p.y * h * 0.45f))
    }
}

private fun DrawScope.drawPrismFairyLights(
    strands: List<PrismStrand>,
    t: Float,
    w: Float,
    h: Float,
    d: Float,
    wind: Float,
    primary: Color,
    secondary: Color,
    tertiary: Color,
    level: Float = 1f,
    yOffset: Float = 0f,
) {
    val warm = Color(0xFFFFE7B0)
    val wireColor = Color(0xFF2B2118).copy(alpha = 0.75f)
    val palette =
        listOf(
            warm,
            lerp(primary, Color.White, 0.45f),
            warm,
            lerp(tertiary, Color.White, 0.45f),
            lerp(secondary, Color.White, 0.40f),
        )
    for (s in strands) {
        val x0 = -0.04f * w
        val x2 = 1.04f * w
        val y0 = s.y0 * h + yOffset
        val y2 = s.y1 * h + yOffset
        val sway = wind * 0.025f * w + sin(t * 0.9f + s.seed) * 0.008f * w
        val cx = (x0 + x2) / 2f + sway
        val cy = (y0 + y2) / 2f + 2f * s.sag * h * (1f + 0.10f * sin(t * 1.3f + s.seed * 2f) + 0.08f * wind)

        val wire = Path()
        wire.moveTo(x0, y0)
        wire.cubicTo(
            x0 + 2f / 3f * (cx - x0),
            y0 + 2f / 3f * (cy - y0),
            x2 + 2f / 3f * (cx - x2),
            y2 + 2f / 3f * (cy - y2),
            x2,
            y2,
        )
        drawPath(wire, color = wireColor, style = Stroke(width = 1.6f * d, cap = StrokeCap.Round))

        for (i in 0 until s.count) {
            val u = (i + 0.5f) / s.count
            val omu = 1f - u
            val px = omu * omu * x0 + 2f * omu * u * cx + u * u * x2
            val py = omu * omu * y0 + 2f * omu * u * cy + u * u * y2
            val phase = i * 1.7f + s.seed * 3.1f
            val swing = wind * 5f * d + sin(t * 1.7f + phase) * 2.2f * d
            val bx = px + swing
            val by = py + 9f * d
            drawLine(color = wireColor, start = Offset(px, py), end = Offset(bx, by), strokeWidth = 1.4f * d)

            val gust = max(0f, wind - 0.55f)
            val flick = 0.82f + 0.18f * sin(t * (2.1f + (i % 3) * 0.8f) + phase)
            val dip = 1f - 0.5f * gust * (0.5f + 0.5f * sin(t * 14f + phase * 2.3f))
            val b = (flick * dip).coerceIn(0.3f, 1f) * level
            val bulb = palette[(i + s.seed.toInt()) % palette.size]
            val center = Offset(bx, by)
            drawCircle(
                brush =
                    Brush.radialGradient(
                        colors = listOf(bulb.copy(alpha = 0.55f * b), Color.Transparent),
                        center = center,
                        radius = 22f * d,
                    ),
                radius = 22f * d,
                center = center,
            )
            drawCircle(color = bulb.copy(alpha = b), radius = 4.2f * d, center = center)
            drawCircle(color = Color.White.copy(alpha = 0.85f * b), radius = 1.8f * d, center = center)
        }
    }
}

@Composable
fun PrismNature(
    active: Boolean,
    modifier: Modifier = Modifier,
    playing: Boolean = true,
) {
    val enabled by rememberPreference(PrismNatureKey, defaultValue = true)
    val windStrength by rememberPreference(PrismWindStrengthKey, defaultValue = 1f)
    val seasonPref by rememberPreference(PrismSeasonKey, defaultValue = 0)
    val season = remember(seasonPref) { prismResolveSeason(seasonPref) }
    val isNight = remember { prismIsNight() }
    val running = active && enabled
    val timeNanos = remember { mutableLongStateOf(0L) }
    LaunchedEffect(running) {
        if (running) {
            val start = withFrameNanos { it }
            while (true) {
                withFrameNanos { timeNanos.longValue = it - start }
            }
        }
    }
    val primary by animateColorAsState(MaterialTheme.colorScheme.primary, tween(900), label = "natureP")
    val secondary by animateColorAsState(MaterialTheme.colorScheme.secondary, tween(900), label = "natureS")
    val tertiary by animateColorAsState(MaterialTheme.colorScheme.tertiary, tween(900), label = "natureT")
    val level by animateFloatAsState(if (playing) 1f else 0.18f, tween(900), label = "lightLevel")
    val field = remember { PrismField() }

    Canvas(modifier = modifier) {
        if (!running) return@Canvas
        val t = timeNanos.longValue / 1_000_000_000f
        val d = 1.dp.toPx()
        val wind = prismWind(t) * windStrength
        val w = size.width
        val h = size.height
        if (isNight) drawPrismStars(field.stars, t, w, h, d)
        when (season) {
            1 -> drawPrismLeaves(field.leaves, t, w, h, d, wind, primary)
            2 -> drawPrismSnow(field.snow, t, w, h, d, wind)
            3 -> {
                drawPrismLeaves(field.petals, t, w, h, d, wind, primary, 1f, false)
                drawPrismButterflies(field.butterflies, t, w, h, d, primary, tertiary)
            }
            4 -> {
                drawPrismFireflies(field.flies, t, w, h, d)
                drawPrismButterflies(field.butterflies, t, w, h, d, primary, tertiary)
            }
            else -> drawPrismRain(field.rain, t, w, h, d, wind)
        }
        drawPrismSparkles(field.sparkles, t, w, h, d, lerp(primary, Color.White, 0.6f))
        drawPrismFairyLights(PrismStrands, t, w, h, d, wind, primary, secondary, tertiary, level)
    }
}

@Composable
fun PrismHomeLights(modifier: Modifier = Modifier) {
    val enabled by rememberPreference(PrismNatureKey, defaultValue = true)
    val windStrength by rememberPreference(PrismWindStrengthKey, defaultValue = 1f)
    val timeNanos = remember { mutableLongStateOf(0L) }
    LaunchedEffect(enabled) {
        if (enabled) {
            val start = withFrameNanos { it }
            while (true) {
                withFrameNanos { timeNanos.longValue = it - start }
            }
        }
    }
    val primary by animateColorAsState(MaterialTheme.colorScheme.primary, tween(900), label = "homeP")
    val secondary by animateColorAsState(MaterialTheme.colorScheme.secondary, tween(900), label = "homeS")
    val tertiary by animateColorAsState(MaterialTheme.colorScheme.tertiary, tween(900), label = "homeT")

    Canvas(modifier = modifier) {
        if (!enabled) return@Canvas
        val t = timeNanos.longValue / 1_000_000_000f
        val d = 1.dp.toPx()
        val wind = prismWind(t) * windStrength
        drawPrismFairyLights(PrismHomeStrands, t, size.width, size.height, d, wind, primary, secondary, tertiary)
    }
}

@Composable
fun PrismLibraryHeader(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val pulse by rememberInfiniteTransition(label = "libGlow").animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "libGlowPulse",
    )
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .height(84.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            primary.copy(alpha = 0.50f),
                            secondary.copy(alpha = 0.35f),
                            tertiary.copy(alpha = 0.50f),
                        ),
                    ),
                ).drawBehind {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.14f * pulse),
                        radius = size.height * 1.1f,
                        center = Offset(size.width * 0.9f, 0f),
                    )
                }.padding(horizontal = 20.dp),
    ) {
        Column {
            Text(
                text = "Your Library",
                color = Color.White,
                style =
                    MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        shadow = Shadow(color = Color.White.copy(alpha = 0.8f * pulse), offset = Offset.Zero, blurRadius = 22f),
                    ),
            )
            Text(
                text = "Playlists | Songs | Albums | Artists",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
