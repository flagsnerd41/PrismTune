package moe.rukamori.archivetune.ui.player

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FairyLights(
    modifier: Modifier = Modifier,
    lightColor: Color = Color(0xFFFFE2A8),
) {
    val transition = rememberInfiniteTransition(label = "fairy")
    val twinkleA by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Reverse),
        label = "twinkleA",
    )
    val twinkleB by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Reverse),
        label = "twinkleB",
    )
    Canvas(modifier = modifier.fillMaxWidth().height(110.dp)) {
        val w = size.width
        val sag = size.height * 0.5f
        val count = 9
        val string = Path().apply {
            moveTo(0f, 0f)
            quadraticBezierTo(w / 2f, sag * 2f, w, 0f)
        }
        drawPath(
            string,
            Color(0x66FFFFFF),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
        )
        for (i in 0 until count) {
            val t = (i + 0.5f) / count
            val x = t * w
            val y = 4f * sag * t * (1f - t)
            val a = if (i % 2 == 0) twinkleA else twinkleB
            val drop = 10.dp.toPx()
            drawLine(
                Color(0x66FFFFFF),
                Offset(x, y),
                Offset(x, y + drop),
                strokeWidth = 1.5.dp.toPx(),
            )
            drawStar(Offset(x, y + drop + 6.dp.toPx()), 7.dp.toPx(), lightColor, a)
        }
    }
}

private fun DrawScope.drawStar(center: Offset, radius: Float, color: Color, alpha: Float) {
    drawCircle(color.copy(alpha = 0.18f * alpha), radius * 2.4f, center)
    drawCircle(color.copy(alpha = 0.28f * alpha), radius * 1.5f, center)
    val path = Path()
    val inner = radius * 0.45f
    for (k in 0 until 10) {
        val r = if (k % 2 == 0) radius else inner
        val ang = -PI / 2 + k * PI / 5
        val px = center.x + (r * cos(ang)).toFloat()
        val py = center.y + (r * sin(ang)).toFloat()
        if (k == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }
    path.close()
    drawPath(path, color.copy(alpha = alpha))
}

@Composable
fun FairyLightsOverlay(modifier: Modifier = Modifier) {
    FairyLights(
        modifier = modifier.layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            layout(placeable.width, 0) { placeable.place(0, 0) }
        },
    )
}
