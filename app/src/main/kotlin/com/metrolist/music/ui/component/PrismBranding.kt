/**
 * PrismTune branding: welcome splash + glowing header
 */

package com.metrolist.music.ui.component

import androidx.compose.animation.core.Animatable
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
