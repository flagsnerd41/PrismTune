/**
 * PrismTune branding: welcome splash + glowing header
 */

package com.metrolist.music.ui.component

import androidx.compose.animation.core.Animatable
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt
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
    val strands = remember { buildPrismStrands() }
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
                strands,
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

private class PrismDot(
    val x: Float,
    val y: Float,
    val speed: Float,
    val size: Float,
    val amp: Float,
    val freq: Float,
    val phase: Float,
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

private const val PRISM_PI = 3.1415927f
private const val PRISM_LEAF_SEGMENTS = 6
private const val PRISM_ROPE_N = 26

private class PrismLeaf(
    val xFrac: Float,
    val yFrac0: Float,
    val sizeDp: Float,
    val color: Color,
    val depth: Float,
    val fall: Float,
    val glide: Float,
    val flutterFreq: Float,
    val tiltMax: Float,
    val yawSpeed: Float,
    val curl: Float,
    val widthK: Float,
    val seed: Float,
) {
    var inited = false
    var lastT = -1f
    var x = 0f
    var y = 0f
    var vx = 0f
    var vy = 0f
    var psi = seed * 6.28f
    var yaw = seed * 31f
    var pitch = 0f
    var roll = 0f
    val mx = FloatArray(PRISM_LEAF_SEGMENTS + 1)
    val my = FloatArray(PRISM_LEAF_SEGMENTS + 1)
    val lx = FloatArray(PRISM_LEAF_SEGMENTS + 1)
    val ly = FloatArray(PRISM_LEAF_SEGMENTS + 1)
    val rx = FloatArray(PRISM_LEAF_SEGMENTS + 1)
    val ry = FloatArray(PRISM_LEAF_SEGMENTS + 1)
    val stem = FloatArray(2)
    val pathL = Path()
    val pathR = Path()
}

private fun buildPrismLeaves(
    count: Int,
    minSize: Float,
    maxSize: Float,
    colors: List<Color>,
    speedMul: Float,
    widthK: Float = 1f,
    curlBase: Float = 0.45f,
): List<PrismLeaf> {
    val r = Random(11 + count)
    return List(count) {
        val depth = r.nextFloat()
        PrismLeaf(
            xFrac = r.nextFloat(),
            yFrac0 = r.nextFloat(),
            sizeDp = minSize + (maxSize - minSize) * depth,
            color = colors[r.nextInt(colors.size)],
            depth = depth,
            fall = (38f + 30f * r.nextFloat()) * speedMul,
            glide = 0.6f + 0.8f * r.nextFloat(),
            flutterFreq = 1.4f + 1.4f * r.nextFloat(),
            tiltMax = 0.6f + 0.5f * r.nextFloat(),
            yawSpeed = (r.nextFloat() - 0.5f) * 2.4f,
            curl = curlBase * (0.7f + 0.6f * r.nextFloat()),
            widthK = widthK * (0.85f + 0.3f * r.nextFloat()),
            seed = r.nextFloat(),
        )
    }.sortedBy { it.depth }
}

private fun PrismLeaf.step(
    t: Float,
    w: Float,
    h: Float,
    d: Float,
    wind: Float,
) {
    val margin = 40f * d
    if (!inited) {
        x = xFrac * w
        y = yFrac0 * (h + 2f * margin) - margin
        inited = true
        lastT = t
        return
    }
    var dt = t - lastT
    lastT = t
    if (dt <= 0f) return
    if (dt > 0.05f) dt = 0.05f
    psi += flutterFreq * dt
    yaw += yawSpeed * dt
    val s = sin(psi)
    val tilt = tiltMax * s
    val localWind = wind * (0.8f + 0.4f * sin(t * 0.9f + (x / (w + 1f)) * 4f + seed * 6.28f))
    val parallax = 0.8f + 0.5f * depth
    val targetVx = glide * sin(tilt) * 90f * d + localWind * 70f * d * (0.5f + 0.5f * depth)
    val targetVy = fall * d * parallax * (0.55f + 0.9f * s * s)
    val k = 1f - exp(-dt / 0.55f)
    vx += (targetVx - vx) * k
    vy += (targetVy - vy) * k
    x += vx * dt
    y += vy * dt
    pitch = tilt
    roll = 0.6f * tilt * cos(psi * 0.5f) + localWind * 0.25f
    if (y > h + margin) {
        y = -margin
        x = (((seed * 811f + t * 37f) % 1f + 1f) % 1f) * w
        vy = 0f
    }
    val span = w + 120f * d
    var xx = (x + 60f * d) % span
    if (xx < 0f) xx += span
    x = xx - 60f * d
}

private fun prismLeafShade(
    base: Color,
    tint: Color,
    nx: Float,
    ny: Float,
    nz: Float,
    alpha: Float,
): Color {
    val sgn = if (nz >= 0f) 1f else -1f
    val dd = (nx * -0.35f + ny * -0.6f + nz * 0.72f) * sgn
    val diffuse = max(0f, dd)
    val backlit = max(0f, -dd)
    var c = lerp(base, tint, 0.12f)
    if (sgn < 0f) c = lerp(c, Color(0xFFD8C79A), 0.30f)
    var out = lerp(Color.Black, c, 0.30f + 0.70f * diffuse)
    out = lerp(out, lerp(c, Color.White, 0.45f), (backlit * 0.7f).coerceIn(0f, 0.7f))
    out = lerp(out, Color.White, diffuse * diffuse * diffuse * diffuse * 0.20f)
    return out.copy(alpha = alpha)
}

private fun DrawScope.drawPrismLeaf3D(
    leaf: PrismLeaf,
    d: Float,
    tint: Color,
    alphaMul: Float,
    veins: Boolean,
) {
    val size = leaf.sizeDp * d
    val f = 260f * d
    val angX = leaf.pitch + 0.35f
    val angY = leaf.yaw
    val angZ = leaf.roll
    val cx = cos(angX)
    val sx = sin(angX)
    val cy = cos(angY)
    val sy = sin(angY)
    val cz = cos(angZ)
    val sz = sin(angZ)
    val r00 = cz * cy - sz * sx * sy
    val r01 = -sz * cx
    val r02 = cz * sy + sz * sx * cy
    val r10 = sz * cy + cz * sx * sy
    val r11 = cz * cx
    val r12 = sz * sy - cz * sx * cy
    val r20 = -cx * sy
    val r21 = sx
    val r22 = cx * cy

    fun put(
        ax: FloatArray,
        ay: FloatArray,
        i: Int,
        lx: Float,
        ly: Float,
        lz: Float,
    ) {
        val px = (r00 * lx + r01 * ly + r02 * lz) * size
        val py = (r10 * lx + r11 * ly + r12 * lz) * size
        val pz = (r20 * lx + r21 * ly + r22 * lz) * size
        val k = f / (f - pz)
        ax[i] = leaf.x + px * k
        ay[i] = leaf.y + py * k
    }

    val n = PRISM_LEAF_SEGMENTS
    for (i in 0..n) {
        val s = i.toFloat() / n
        val yy = 1f - 2f * s
        val wd = leaf.widthK * sin(PRISM_PI * s.pow(0.8f)) * 0.55f
        val zm = -0.35f * s * s * leaf.curl
        val ze = zm + leaf.curl * 4f * wd * wd
        put(leaf.mx, leaf.my, i, 0f, yy, zm)
        put(leaf.lx, leaf.ly, i, -wd, yy, ze)
        put(leaf.rx, leaf.ry, i, wd, yy, ze)
    }
    put(leaf.stem, leaf.stem, 0, 0f, 1.28f, 0f)

    val c = 2.2f * leaf.curl
    val inv = 1f / sqrt(1f + c * c)
    val nlx = c * inv
    val nrx = -c * inv
    val nz = inv
    val alpha = (0.62f + 0.38f * leaf.depth) * alphaMul
    val colL = prismLeafShade(leaf.color, tint, r00 * nlx + r02 * nz, r10 * nlx + r12 * nz, r20 * nlx + r22 * nz, alpha)
    val colR = prismLeafShade(leaf.color, tint, r00 * nrx + r02 * nz, r10 * nrx + r12 * nz, r20 * nrx + r22 * nz, alpha)

    leaf.pathL.rewind()
    leaf.pathL.moveTo(leaf.mx[0], leaf.my[0])
    for (i in 1..n) leaf.pathL.lineTo(leaf.mx[i], leaf.my[i])
    for (i in n downTo 0) leaf.pathL.lineTo(leaf.lx[i], leaf.ly[i])
    leaf.pathL.close()
    leaf.pathR.rewind()
    leaf.pathR.moveTo(leaf.mx[0], leaf.my[0])
    for (i in 1..n) leaf.pathR.lineTo(leaf.mx[i], leaf.my[i])
    for (i in n downTo 0) leaf.pathR.lineTo(leaf.rx[i], leaf.ry[i])
    leaf.pathR.close()
    drawPath(leaf.pathL, color = colL)
    drawPath(leaf.pathR, color = colR)

    val line = Color.Black.copy(alpha = 0.30f * alphaMul)
    val outline = Stroke(width = 0.6f * d)
    drawPath(leaf.pathL, color = line, style = outline)
    drawPath(leaf.pathR, color = line, style = outline)
    if (veins) {
        drawLine(
            color = Color(0xFF3B2A14).copy(alpha = 0.55f * alphaMul),
            start = Offset(leaf.mx[0], leaf.my[0]),
            end = Offset(leaf.stem[0], leaf.stem[1]),
            strokeWidth = 1.1f * d,
        )
        drawLine(
            color = line,
            start = Offset(leaf.mx[0], leaf.my[0]),
            end = Offset(leaf.mx[n], leaf.my[n]),
            strokeWidth = 0.9f * d,
        )
        for (i in 1 until n) {
            drawLine(
                color = line,
                start = Offset(leaf.mx[i], leaf.my[i]),
                end = Offset(leaf.mx[i] + (leaf.lx[i] - leaf.mx[i]) * 0.8f, leaf.my[i] + (leaf.ly[i] - leaf.my[i]) * 0.8f),
                strokeWidth = 0.5f * d,
            )
            drawLine(
                color = line,
                start = Offset(leaf.mx[i], leaf.my[i]),
                end = Offset(leaf.mx[i] + (leaf.rx[i] - leaf.mx[i]) * 0.8f, leaf.my[i] + (leaf.ry[i] - leaf.my[i]) * 0.8f),
                strokeWidth = 0.5f * d,
            )
        }
    }
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
    for (leaf in leaves) {
        leaf.step(t, w, h, d, wind)
        drawPrismLeaf3D(leaf, d, tint, alphaMul, veins)
    }
}

private class PrismStrand(
    val y0: Float,
    val y1: Float,
    val sag: Float,
    val count: Int,
    val seed: Float,
    val scale: Float,
) {
    val px = FloatArray(PRISM_ROPE_N + 1)
    val py = FloatArray(PRISM_ROPE_N + 1)
    val ox = FloatArray(PRISM_ROPE_N + 1)
    val oy = FloatArray(PRISM_ROPE_N + 1)
    val theta = FloatArray(count)
    val omega = FloatArray(count)
    val wire = Path()
    var rest = 0f
    var simW = -1f
    var simH = -1f
    var lastT = -1f
}

private fun buildPrismStrands(): List<PrismStrand> =
    listOf(
        PrismStrand(0.040f, 0.050f, 0.060f, 12, 0f, 1f),
        PrismStrand(0.022f, 0.030f, 0.035f, 9, 1f, 0.8f),
    )

private fun buildPrismHomeStrands(): List<PrismStrand> =
    listOf(
        PrismStrand(0.05f, 0.08f, 0.22f, 10, 0f, 1f),
        PrismStrand(0.02f, 0.03f, 0.12f, 7, 1f, 0.8f),
    )

private fun PrismStrand.step(
    t: Float,
    w: Float,
    h: Float,
    d: Float,
    wind: Float,
    yOffset: Float,
) {
    val n = PRISM_ROPE_N
    val x0 = -0.04f * w
    val x2 = 1.04f * w
    val ya = y0 * h + yOffset
    val yb = y1 * h + yOffset
    if (simW != w || simH != h || lastT < 0f) {
        simW = w
        simH = h
        var len = 0f
        var prevX = x0
        var prevY = ya
        for (i in 0..n) {
            val u = i.toFloat() / n
            val x = x0 + (x2 - x0) * u
            val y = ya + (yb - ya) * u + 4f * sag * h * u * (1f - u)
            px[i] = x
            py[i] = y
            ox[i] = x
            oy[i] = y
            if (i > 0) len += sqrt((x - prevX) * (x - prevX) + (y - prevY) * (y - prevY))
            prevX = x
            prevY = y
        }
        rest = len / n
        lastT = t
        return
    }
    var dt = t - lastT
    lastT = t
    if (dt <= 0f) return
    if (dt > 0.05f) dt = 0.05f
    val sub = 2
    val sdt = dt / sub
    val g = 900f * d
    for (step in 0 until sub) {
        px[0] = x0
        py[0] = ya
        px[n] = x2
        py[n] = yb
        for (i in 1 until n) {
            val u = i.toFloat() / n
            val local = wind * (0.8f + 0.4f * sin(t * 0.8f + u * 3.2f + seed * 2f)) + 0.12f * sin(t * 3.1f + i * 0.9f)
            val ax = local * 420f * d
            val ay = g - local * 40f * d
            val vx = (px[i] - ox[i]) * 0.985f
            val vy = (py[i] - oy[i]) * 0.985f
            ox[i] = px[i]
            oy[i] = py[i]
            px[i] += vx + ax * sdt * sdt
            py[i] += vy + ay * sdt * sdt
        }
        for (iter in 0 until 8) {
            for (i in 0 until n) {
                val dx = px[i + 1] - px[i]
                val dy = py[i + 1] - py[i]
                val dist = sqrt(dx * dx + dy * dy)
                if (dist > 0.0001f) {
                    val diff = (dist - rest) / dist
                    val cx = dx * 0.5f * diff
                    val cy = dy * 0.5f * diff
                    if (i == 0) {
                        px[i + 1] -= cx * 2f
                        py[i + 1] -= cy * 2f
                    } else if (i + 1 == n) {
                        px[i] += cx * 2f
                        py[i] += cy * 2f
                    } else {
                        px[i] += cx
                        py[i] += cy
                        px[i + 1] -= cx
                        py[i + 1] -= cy
                    }
                }
            }
        }
    }
    for (i in 0 until count) {
        val k = (((i + 0.5f) / count) * n).toInt().coerceIn(1, n - 1)
        val nodeVx = (px[k] - ox[k]) / sdt
        val phase = i * 1.7f + seed * 3.1f
        val acc =
            -26f * sin(theta[i]) - 2.2f * omega[i] +
                wind * 8f * (0.6f + 0.4f * sin(t * 2.1f + phase)) - nodeVx / (40f * d)
        omega[i] += acc * dt
        theta[i] = (theta[i] + omega[i] * dt).coerceIn(-1.1f, 1.1f)
    }
}

private fun DrawScope.drawPrismBulb(
    c: Offset,
    r: Float,
    bulb: Color,
    b: Float,
    d: Float,
) {
    val glowBig = 34f * d * (0.7f + 0.3f * b)
    drawCircle(
        brush = Brush.radialGradient(colors = listOf(bulb.copy(alpha = 0.22f * b), Color.Transparent), center = c, radius = glowBig),
        radius = glowBig,
        center = c,
    )
    val glowMid = 17f * d
    drawCircle(
        brush = Brush.radialGradient(colors = listOf(bulb.copy(alpha = 0.50f * b), Color.Transparent), center = c, radius = glowMid),
        radius = glowMid,
        center = c,
    )
    val unlit = Color(0xFF2A2420)
    val core = lerp(unlit, lerp(bulb, Color.White, 0.55f), b)
    val mid = lerp(unlit, bulb, b)
    val edge = lerp(Color.Black, mid, 0.55f)
    drawCircle(
        brush =
            Brush.radialGradient(
                colors = listOf(core.copy(alpha = 0.95f), mid.copy(alpha = 0.95f), edge.copy(alpha = 0.95f)),
                center = c + Offset(-0.28f * r, -0.30f * r),
                radius = 1.35f * r,
            ),
        radius = r,
        center = c,
    )
    drawCircle(color = Color.White.copy(alpha = 0.9f * b), radius = 0.32f * r, center = c)
    drawCircle(color = Color.White.copy(alpha = 0.55f), radius = 0.2f * r, center = c + Offset(-0.38f * r, -0.42f * r))
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
    val wireColor = Color(0xFF2B2118)
    val palette =
        listOf(
            warm,
            lerp(primary, Color.White, 0.45f),
            warm,
            lerp(tertiary, Color.White, 0.45f),
            lerp(secondary, Color.White, 0.40f),
        )
    val n = PRISM_ROPE_N
    for (s in strands.asReversed()) {
        s.step(t, w, h, d, wind, yOffset)
        if (s.rest <= 0f) continue
        s.wire.rewind()
        s.wire.moveTo(s.px[0], s.py[0])
        for (i in 1..n) s.wire.lineTo(s.px[i], s.py[i])
        withTransform({ translate(0f, 1.6f * d) }) {
            drawPath(s.wire, color = Color.Black.copy(alpha = 0.22f), style = Stroke(width = 2.2f * d * s.scale, cap = StrokeCap.Round))
        }
        drawPath(s.wire, color = wireColor.copy(alpha = 0.9f), style = Stroke(width = 1.7f * d * s.scale, cap = StrokeCap.Round))
        drawPath(s.wire, color = Color.White.copy(alpha = 0.10f), style = Stroke(width = 0.6f * d, cap = StrokeCap.Round))

        for (i in 0 until s.count) {
            val k = (((i + 0.5f) / s.count) * n).toInt().coerceIn(1, n - 1)
            val bx0 = s.px[k]
            val by0 = s.py[k]
            val th = s.theta[i]
            val r = 5.2f * d * s.scale
            val cord = 6f * d * s.scale
            val dirX = sin(th)
            val dirY = cos(th)
            val topX = bx0 + dirX * cord
            val topY = by0 + dirY * cord
            drawLine(color = wireColor.copy(alpha = 0.9f), start = Offset(bx0, by0), end = Offset(topX, topY), strokeWidth = 1.2f * d * s.scale)
            drawLine(
                color = Color(0xFF4A3B2A),
                start = Offset(topX, topY),
                end = Offset(topX + dirX * r * 0.7f, topY + dirY * r * 0.7f),
                strokeWidth = r * 0.85f,
                cap = StrokeCap.Round,
            )
            val center = Offset(bx0 + dirX * (cord + r * 0.9f), by0 + dirY * (cord + r * 0.9f))

            val phase = i * 1.7f + s.seed * 3.1f
            val gust = max(0f, wind - 0.55f)
            val flick = 0.84f + 0.10f * sin(t * (2.1f + (i % 3) * 0.8f) + phase) + 0.06f * sin(t * 7.3f + phase * 1.7f)
            val dip = 1f - 0.5f * gust * (0.5f + 0.5f * sin(t * 14f + phase * 2.3f))
            val b = (flick * dip).coerceIn(0.3f, 1f) * level
            drawPrismBulb(center, r, palette[(i + s.seed.toInt()) % palette.size], b, d)
        }
    }
}

private class PrismField {
    val leaves = buildPrismLeaves(16, 10f, 22f, PrismLeafColors, 1f)
    val petals = buildPrismLeaves(26, 5f, 9f, PrismPetalColors, 0.7f, 0.9f, 0.8f)
    val snow = buildPrismDots(21, 70, 1.2f, 3.6f, 0.04f, 0.09f, 12f)
    val rain = buildPrismDots(22, 80, 10f, 18f, 0.7f, 1.2f, 0f)
    val flies = buildPrismDots(23, 22, 1f, 2f, 0f, 0f, 14f)
    val butterflies = buildPrismDots(24, 3, 0f, 0f, 0f, 0f, 0f)
    val sparkles = buildPrismDots(25, 36, 0.8f, 2.0f, 0.01f, 0.03f, 0f)
    val stars = buildPrismDots(26, 40, 0.8f, 1.8f, 0f, 0f, 0f)
    val strands = buildPrismStrands()
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
        drawPrismFairyLights(field.strands, t, w, h, d, wind, primary, secondary, tertiary, level)
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
    val strands = remember { buildPrismHomeStrands() }

    Canvas(modifier = modifier) {
        if (!enabled) return@Canvas
        val t = timeNanos.longValue / 1_000_000_000f
        val d = 1.dp.toPx()
        val wind = prismWind(t) * windStrength
        drawPrismFairyLights(strands, t, size.width, size.height, d, wind, primary, secondary, tertiary)
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
