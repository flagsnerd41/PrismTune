/**
 * PrismTune branding: welcome splash + glowing header
 */

package com.metrolist.music.ui.component

import androidx.compose.animation.core.Animatable
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
    val textAlpha = remember { Animatable(0f) }
    val textScale = remember { Animatable(0.75f) }
    val overlayAlpha = remember { Animatable(1f) }
    val pulse by rememberInfiniteTransition(label = "splashGlow").animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "splashGlowPulse",
    )

    LaunchedEffect(Unit) {
        coroutineScope {
            launch { textAlpha.animateTo(1f, tween(700)) }
            launch {
                textScale.animateTo(
                    1f,
                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                )
            }
        }
        delay(900)
        overlayAlpha.animateTo(0f, tween(500))
        onFinished()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .fillMaxSize()
                .graphicsLayer { alpha = overlayAlpha.value }
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.primaryContainer,
                        ),
                    ),
                ).clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { },
    ) {
        Text(
            text = "PrismTune",
            color = MaterialTheme.colorScheme.onSurface,
            style =
                MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    shadow =
                        Shadow(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = pulse),
                            offset = Offset.Zero,
                            blurRadius = 48f,
                        ),
                ),
            modifier =
                Modifier.graphicsLayer {
                    alpha = textAlpha.value
                    scaleX = textScale.value
                    scaleY = textScale.value
                },
        )
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

private val PrismStrands =
    listOf(
        PrismStrand(0.040f, 0.050f, 0.060f, 12, 0f),
        PrismStrand(0.022f, 0.030f, 0.035f, 9, 1f),
    )

private fun buildPrismLeaves(): List<PrismLeaf> {
    val r = Random(11)
    return List(18) {
        val depth = r.nextFloat()
        PrismLeaf(
            xFrac = r.nextFloat(),
            yPhase = r.nextFloat(),
            speed = 0.045f + 0.05f * r.nextFloat(),
            sizeDp = 9f + 11f * depth,
            swayAmpDp = 14f + 26f * r.nextFloat(),
            swayFreq = 0.6f + 0.9f * r.nextFloat(),
            swayPhase = 6.28f * r.nextFloat(),
            spin = (r.nextFloat() - 0.5f) * 90f,
            spinPhase = 360f * r.nextFloat(),
            flipFreq = 0.8f + 1.6f * r.nextFloat(),
            flipPhase = 6.28f * r.nextFloat(),
            color = PrismLeafColors[r.nextInt(PrismLeafColors.size)],
            depth = depth,
        )
    }
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

private fun DrawScope.drawPrismLeaves(
    leaves: List<PrismLeaf>,
    t: Float,
    w: Float,
    h: Float,
    d: Float,
    wind: Float,
) {
    val margin = 40f * d
    val travel = h + margin * 2f
    val span = w + 120f * d
    for (leaf in leaves) {
        val raw = (leaf.yPhase + t * leaf.speed) % 1f
        val y = raw * travel - margin
        val swayWave = sin(t * leaf.swayFreq + leaf.swayPhase)
        val sway = swayWave * leaf.swayAmpDp * d
        val drift = wind * (24f + 40f * leaf.depth) * d * (0.4f + raw)
        var x = (leaf.xFrac * w + sway + drift + 60f * d) % span
        if (x < 0f) x += span
        x -= 60f * d
        val rot = leaf.spinPhase + t * leaf.spin + wind * 25f + swayWave * 30f
        val flip = cos(t * leaf.flipFreq + leaf.flipPhase)
        val sx = max(0.22f, abs(flip))
        val shade = (0.78f + 0.22f * flip).coerceIn(0.55f, 1f)
        val body = lerp(Color.Black, leaf.color, shade).copy(alpha = 0.55f + 0.4f * leaf.depth)
        val vein = Color.Black.copy(alpha = 0.28f)
        val sizePx = leaf.sizeDp * d
        withTransform({
            translate(x, y)
            rotate(rot, Offset.Zero)
            scale(sx * sizePx, sizePx, Offset.Zero)
        }) {
            drawPath(PrismLeafPath, color = body)
            drawLine(color = vein, start = Offset(0f, -0.8f), end = Offset(0f, 1.3f), strokeWidth = 0.06f)
        }
    }
}

private fun DrawScope.drawPrismFairyLights(
    t: Float,
    w: Float,
    h: Float,
    d: Float,
    wind: Float,
    tint: Color,
) {
    val warm = Color(0xFFFFE7B0)
    val wireColor = Color(0xFF2B2118).copy(alpha = 0.75f)
    for (s in PrismStrands) {
        val x0 = -0.04f * w
        val x2 = 1.04f * w
        val y0 = s.y0 * h
        val y2 = s.y1 * h
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
            val b = (flick * dip).coerceIn(0.3f, 1f)
            val bulb = if (i % 2 == 0) warm else lerp(warm, tint, 0.4f)
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
) {
    val timeNanos = remember { mutableLongStateOf(0L) }
    LaunchedEffect(active) {
        if (active) {
            val start = withFrameNanos { it }
            while (true) {
                withFrameNanos { timeNanos.longValue = it - start }
            }
        }
    }
    val tint = MaterialTheme.colorScheme.primary
    val leaves = remember { buildPrismLeaves() }

    Canvas(modifier = modifier) {
        if (!active) return@Canvas
        val t = timeNanos.longValue / 1_000_000_000f
        val d = 1.dp.toPx()
        val wind = prismWind(t)
        drawPrismLeaves(leaves, t, size.width, size.height, d, wind)
        drawPrismFairyLights(t, size.width, size.height, d, wind, tint)
    }
}
