/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 *
 * Performance optimized MiniPlayer - prevents unnecessary recomposition
 */

package com.metrolist.music.ui.player

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.Stable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import coil3.compose.AsyncImage
import com.metrolist.music.LocalDatabase
import com.metrolist.music.LocalListenTogetherManager
import com.metrolist.music.LocalPlayerConnection
import com.metrolist.music.R
import com.metrolist.music.constants.CropAlbumArtKey
import com.metrolist.music.constants.DarkModeKey
import com.metrolist.music.constants.MiniPlayerHeight
import com.metrolist.music.constants.PureBlackMiniPlayerKey
import com.metrolist.music.constants.SwipeSensitivityKey
import com.metrolist.music.constants.SwipeThumbnailKey
import com.metrolist.music.constants.ThumbnailCornerRadius
import com.metrolist.music.constants.UseNewMiniPlayerDesignKey
import com.metrolist.music.db.entities.ArtistEntity
import com.metrolist.music.listentogether.ListenTogetherManager
import com.metrolist.music.models.MediaMetadata
import com.metrolist.music.playback.CastConnectionHandler
import com.metrolist.music.playback.PlayerConnection
import com.metrolist.music.ui.screens.settings.DarkMode
import com.metrolist.music.ui.utils.resize
import com.metrolist.music.utils.joinToArtistString
import com.metrolist.music.utils.rememberEnumPreference
import com.metrolist.music.utils.rememberPreference
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import com.metrolist.music.ui.component.Icon as MIcon
import androidx.compose.ui.draw.blur
import com.metrolist.music.constants.MiniPlayerBackgroundStyle
import com.metrolist.music.constants.MiniPlayerBackgroundStyleKey
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.metrolist.music.ui.theme.PlayerColorExtractor

/**
 * Stable wrapper for progress state - reads values only during draw phase
 * This prevents recomposition when position/duration change
 */
@Stable
class ProgressState(
    private val positionState: MutableLongState,
    private val durationState: MutableLongState,
) {
    val progress: Float
        get() {
            val duration = durationState.longValue
            return if (duration > 0) (positionState.longValue.toFloat() / duration).coerceIn(0f, 1f) else 0f
        }
}

@Composable
fun MiniPlayer(
    positionState: MutableLongState,
    durationState: MutableLongState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val useNewMiniPlayerDesign by rememberPreference(UseNewMiniPlayerDesignKey, true)

    // Create stable progress state - doesn't cause recomposition on position changes
    val progressState = remember { ProgressState(positionState, durationState) }

    if (useNewMiniPlayerDesign) {
        NewMiniPlayer(
            progressState = progressState,
            modifier = modifier,
            onClick = onClick,
        )
    } else {
        Box(modifier = modifier.fillMaxWidth()) {
            LegacyMiniPlayer(
                progressState = progressState,
                modifier = Modifier.align(Alignment.Center),
                onClick = onClick,
            )
        }
    }
}

// ============================================================================
// NEW MINI PLAYER DESIGN
// ============================================================================

@Composable
private fun NewMiniPlayer(
    progressState: ProgressState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val playerConnection = LocalPlayerConnection.current ?: return

    // Theme settings - these rarely change
    val miniPlayerBackground by rememberEnumPreference(
        MiniPlayerBackgroundStyleKey,
        defaultValue = MiniPlayerBackgroundStyle.BLUR,
    )
    val context = LocalContext.current
    var gradientColors by remember { mutableStateOf<List<Color>>(emptyList()) }
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val darkTheme by rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.AUTO)
    val useDarkTheme =
        remember(darkTheme, isSystemInDarkTheme) {
            if (darkTheme == DarkMode.AUTO) isSystemInDarkTheme else darkTheme == DarkMode.ON
        }

    // Player states - only collect what's needed at this level
    val playbackState by playerConnection.playbackState.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val canSkipNext by playerConnection.canSkipNext.collectAsStateWithLifecycle()
    val canSkipPrevious by playerConnection.canSkipPrevious.collectAsStateWithLifecycle()

    // Cast state - safely access castConnectionHandler to prevent crashes during service lifecycle changes
    val castHandler =
        remember(playerConnection) {
            try {
                playerConnection.service.castConnectionHandler
            } catch (e: Exception) {
                null
            }
        }
    val isCasting by castHandler?.isCasting?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(false) }

    // Swipe settings
    val swipeSensitivity by rememberPreference(SwipeSensitivityKey, 0.73f)
    val swipeThumbnailPref by rememberPreference(SwipeThumbnailKey, true)

    // Disable swipe for Listen Together guests
    val listenTogetherManager = LocalListenTogetherManager.current
    val isListenTogetherGuest = listenTogetherManager?.let { it.isInRoom && !it.isHost } ?: false
    val swipeThumbnail = swipeThumbnailPref && !isListenTogetherGuest

    val layoutDirection = LocalLayoutDirection.current
    val coroutineScope = rememberCoroutineScope()

    val windowInfo = LocalWindowInfo.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val isTabletLandscape =
        remember(windowInfo.containerSize.width, configuration.orientation) {
            (windowInfo.containerSize.width / density.density) >= 600f && configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        }

    // Swipe animation state
    val offsetXAnimatable = remember { Animatable(0f) }
    var dragStartTime by remember { mutableLongStateOf(0L) }
    var totalDragDistance by remember { mutableFloatStateOf(0f) }

    val animationSpec =
        remember {
            spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)
        }

    val autoSwipeThreshold =
        remember(swipeSensitivity) {
            (600 / (1f + kotlin.math.exp(-(-11.44748 * swipeSensitivity + 9.04945)))).roundToInt()
        }

    var songThemeColor by remember { mutableStateOf(Color(0xFFFFC66D)) }

    LaunchedEffect(mediaMetadata?.id, miniPlayerBackground) {
        gradientColors = emptyList()
        if (miniPlayerBackground == MiniPlayerBackgroundStyle.GRADIENT ||
            miniPlayerBackground == MiniPlayerBackgroundStyle.FAIRY_LIGHTS
        ) {
            val url = mediaMetadata?.thumbnailUrl
            if (url != null) {
                withContext(Dispatchers.IO) {
                    val request = ImageRequest.Builder(context)
                        .data(url)
                        .size(100, 100)
                        .allowHardware(false)
                        .build()
                    val result = runCatching { context.imageLoader.execute(request) }.getOrNull()
                    val bitmap = result?.image?.toBitmap()
                    if (bitmap != null) {
                        val palette = withContext(Dispatchers.Default) {
                            Palette.from(bitmap).maximumColorCount(8).resizeBitmapArea(100 * 100).generate()
                        }
                        val extracted = PlayerColorExtractor.extractGradientColors(
                            palette = palette,
                            fallbackColor = 0xFF000000.toInt(),
                        )
                        val themeRgb = palette.vibrantSwatch?.rgb
                            ?: palette.lightVibrantSwatch?.rgb
                            ?: palette.dominantSwatch?.rgb
                            ?: 0xFFFFC66D.toInt()
                        withContext(Dispatchers.Main) {
                            gradientColors = extracted
                            songThemeColor = Color(themeRgb)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            gradientColors = emptyList()
                            songThemeColor = Color(0xFFFFC66D)
                        }
                    }
                }
            } else {
                songThemeColor = Color(0xFFFFC66D)
            }
        } else {
            gradientColors = emptyList()
        }
    }

    // Memoize colors
    val backgroundColor = when (miniPlayerBackground) {
        MiniPlayerBackgroundStyle.DEFAULT    -> MaterialTheme.colorScheme.surfaceContainer
        MiniPlayerBackgroundStyle.TRANSPARENT -> Color.Black.copy(alpha = 0.25f)
        MiniPlayerBackgroundStyle.BLUR       -> MaterialTheme.colorScheme.surfaceContainer
        MiniPlayerBackgroundStyle.GRADIENT   -> MaterialTheme.colorScheme.surfaceContainer
        MiniPlayerBackgroundStyle.PURE_BLACK -> Color.Black
        MiniPlayerBackgroundStyle.FAIRY_LIGHTS -> Color.Black
        MiniPlayerBackgroundStyle.LEAVES -> Color.Black
    }
    val forceLightColors = !useDarkTheme && (miniPlayerBackground == MiniPlayerBackgroundStyle.PURE_BLACK ||
            miniPlayerBackground == MiniPlayerBackgroundStyle.BLUR ||
            miniPlayerBackground == MiniPlayerBackgroundStyle.GRADIENT ||
            miniPlayerBackground == MiniPlayerBackgroundStyle.FAIRY_LIGHTS ||
            miniPlayerBackground == MiniPlayerBackgroundStyle.LEAVES)

    val primaryColor = if (forceLightColors) Color.White else MaterialTheme.colorScheme.primary
    val outlineColor = if (forceLightColors) Color.White else MaterialTheme.colorScheme.outline
    val onSurfaceColor = if (forceLightColors) Color.White else MaterialTheme.colorScheme.onSurface
    val errorColor = if (forceLightColors) Color(0xFFFF6B6B) else MaterialTheme.colorScheme.error

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(MiniPlayerHeight)
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
                .padding(horizontal = 12.dp)
                .let { baseModifier ->
                    if (swipeThumbnail) {
                        baseModifier.pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragStart = {
                                    dragStartTime = System.currentTimeMillis()
                                    totalDragDistance = 0f
                                },
                                onDragCancel = {
                                    coroutineScope.launch {
                                        offsetXAnimatable.animateTo(0f, animationSpec)
                                    }
                                },
                                onHorizontalDrag = { _, dragAmount ->
                                    val adjustedDragAmount =
                                        if (layoutDirection == LayoutDirection.Rtl) -dragAmount else dragAmount
                                    val canSkipPrevious = playerConnection.player.previousMediaItemIndex != -1
                                    val canSkipNext = playerConnection.player.nextMediaItemIndex != -1
                                    val tryingToSwipeRight = adjustedDragAmount > 0
                                    val tryingToSwipeLeft = adjustedDragAmount < 0
                                    val allowLeft = tryingToSwipeLeft && canSkipNext
                                    val allowRight = tryingToSwipeRight && canSkipPrevious

                                    val canReturnToCenter =
                                        (tryingToSwipeRight && !canSkipPrevious && offsetXAnimatable.value < 0) ||
                                            (tryingToSwipeLeft && !canSkipNext && offsetXAnimatable.value > 0)

                                    if (allowLeft || allowRight || canReturnToCenter) {
                                        totalDragDistance += kotlin.math.abs(adjustedDragAmount)
                                        coroutineScope.launch {
                                            offsetXAnimatable.snapTo(offsetXAnimatable.value + adjustedDragAmount)
                                        }
                                    }
                                },
                                onDragEnd = {
                                    val dragDuration = System.currentTimeMillis() - dragStartTime
                                    val velocity = if (dragDuration > 0) totalDragDistance / dragDuration else 0f
                                    val currentOffset = offsetXAnimatable.value
                                    val minDistanceThreshold = 50f
                                    val velocityThreshold = (swipeSensitivity * -8.25f) + 8.5f

                                    val shouldChangeSong =
                                        (kotlin.math.abs(currentOffset) > minDistanceThreshold && velocity > velocityThreshold) ||
                                            (kotlin.math.abs(currentOffset) > autoSwipeThreshold)

                                    if (shouldChangeSong) {
                                        if (currentOffset > 0 && canSkipPrevious) {
                                            playerConnection.player.seekToPreviousMediaItem()
                                        } else if (currentOffset <= 0 && canSkipNext) {
                                            playerConnection.player.seekToNext()
                                        }
                                    }
                                    coroutineScope.launch {
                                        offsetXAnimatable.animateTo(0f, animationSpec)
                                    }
                                },
                            )
                        }
                    } else {
                        baseModifier
                    }
                },
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        Box(
            modifier =
                Modifier
                    .then(if (isTabletLandscape) Modifier.width(500.dp).align(Alignment.Center) else Modifier.fillMaxWidth())
                    .height(64.dp)
                    .offset { IntOffset(offsetXAnimatable.value.roundToInt(), 0) }
                    .clip(RoundedCornerShape(32.dp))
                    .background(color = backgroundColor)
                    .border(1.dp, outlineColor.copy(alpha = 0.3f), RoundedCornerShape(32.dp))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = LocalIndication.current,
                        onClick = onClick
                    ),
        ) {
            when (miniPlayerBackground) {
                MiniPlayerBackgroundStyle.BLUR -> {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        mediaMetadata?.thumbnailUrl?.let { url ->
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .blur(60.dp),
                            )
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.45f)),
                            )
                        }
                    }
                }
                MiniPlayerBackgroundStyle.FAIRY_LIGHTS -> {
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.18f)))
                }
                MiniPlayerBackgroundStyle.LEAVES -> {
                    Box(Modifier.fillMaxSize().background(Color(0xFF152017).copy(alpha = 0.20f)))
                }
                MiniPlayerBackgroundStyle.GRADIENT -> {
                    val colors = if (gradientColors.isNotEmpty()) gradientColors
                    else listOf(
                        MaterialTheme.colorScheme.surfaceContainer,
                        MaterialTheme.colorScheme.surfaceContainer,
                    )
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(colors)
                            )
                            .background(Color.Black.copy(alpha = 0.15f)),
                    )
                }
                else -> {}
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 8.dp),
            ) {
                // Play button with progress - isolated composable
                NewMiniPlayerPlayButton(
                    progressState = progressState,
                    playbackState = playbackState,
                    isCasting = isCasting,
                    castHandler = castHandler,
                    playerConnection = playerConnection,
                    mediaMetadata = mediaMetadata,
                    primaryColor = primaryColor,
                    outlineColor = outlineColor,
                    listenTogetherManager = listenTogetherManager,
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Song info - isolated composable
                NewMiniPlayerSongInfo(
                    mediaMetadata = mediaMetadata,
                    onSurfaceColor = onSurfaceColor,
                    errorColor = errorColor,
                    modifier = Modifier.weight(1f),
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Cast indicator
                if (isCasting) {
                    Icon(
                        painter = painterResource(R.drawable.cast_connected),
                        contentDescription = "Casting",
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }

// Subscribe button - isolated composable
                mediaMetadata?.artists?.firstOrNull()?.id?.let { artistId ->
                    SubscribeButton(
                        artistId = artistId,
                        metadata = mediaMetadata!!,
                        primaryColor = primaryColor,
                        outlineColor = outlineColor,
                        onSurfaceColor = onSurfaceColor,
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

// Favorite button - isolated composable
                mediaMetadata?.let { FavoriteButton(
                    songId = it.id,
                    errorColor = errorColor,
                    outlineColor = outlineColor,
                    onSurfaceColor = onSurfaceColor,
                )
                }
            }
        }
    }
}

/**
 * Atmospheric, hand-drawn decorations for the expanded Now Playing player.
 * Fairy bulbs have sockets, glass highlights and layered light bloom; leaves have
 * a tapered silhouette, midrib and side veins instead of flat ovals.
 */
@Composable
internal fun PlayerAtmosphere(
    style: MiniPlayerBackgroundStyle,
    trackKey: String?,
    themeColor: Color,
    modifier: Modifier = Modifier,
) {
    var songChangePulse by remember { mutableStateOf(false) }
    LaunchedEffect(trackKey, style) {
        if (trackKey != null) {
            songChangePulse = true
            kotlinx.coroutines.delay(5_000)
            songChangePulse = false
        }
    }
    val motion = rememberInfiniteTransition(label = "prismAtmosphere")
    val wind by motion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(8_000, easing = LinearEasing), RepeatMode.Restart),
        label = "slowBreeze",
    )
    val shimmer by motion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1_700, easing = LinearEasing), RepeatMode.Restart),
        label = "fairyLightTwinkle",
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val breeze = kotlin.math.sin(wind * Math.PI * 2.0).toFloat()
        val px = density
        val warmGold = Color(0xFFFFD99A)
        val warmAmber = Color(0xFFFFB85C)

        if (style == MiniPlayerBackgroundStyle.FAIRY_LIGHTS) {
            // A softly illuminated top edge keeps the effect atmospheric, not a flat drawing.
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFC46B).copy(alpha = 0.12f), Color.Transparent),
                    startY = 0f,
                    endY = h * 0.48f,
                ),
                size = size,
            )

            // Fine twisted cord, with a separate highlight to give it depth.
            val cord = Path().apply {
                moveTo(-w * 0.04f, h * (0.115f + breeze * 0.012f))
                cubicTo(
                    w * 0.18f, h * (0.235f + breeze * 0.018f),
                    w * 0.39f, h * (0.045f - breeze * 0.014f),
                    w * 0.58f, h * (0.135f + breeze * 0.012f),
                )
                cubicTo(
                    w * 0.75f, h * (0.225f - breeze * 0.012f),
                    w * 0.91f, h * (0.055f + breeze * 0.014f),
                    w * 1.04f, h * (0.13f - breeze * 0.01f),
                )
            }
            drawPath(cord, Color(0xFF16110D).copy(alpha = 0.95f), style = Stroke(width = 2.2f * px, cap = StrokeCap.Round))
            drawPath(cord, Color(0xFFB18A55).copy(alpha = 0.78f), style = Stroke(width = 0.7f * px, cap = StrokeCap.Round))

            // Individual sockets and dangling stems follow the cord's gentle wave.
            val bulbCount = 9
            for (i in 0 until bulbCount) {
                val t = i / (bulbCount - 1f)
                val x = w * (0.035f + t * 0.93f)
                val cordY = h * (
                    0.115f + 0.095f * kotlin.math.sin(t * Math.PI * 2.0).toFloat() +
                        breeze * 0.012f * kotlin.math.cos(t * Math.PI * 2.0).toFloat()
                )
                val stemLength = (10f + (i % 3) * 3f) * px
                val sway = breeze * (2.4f + i % 3) * px
                val bulbX = x + sway
                val bulbY = cordY + stemLength + 8f * px
                val twinkle = (kotlin.math.sin(shimmer * Math.PI * 2.0 + i * 1.63) * 0.5 + 0.5).toFloat()
                val pulse = if (songChangePulse) {
                    0.35f + 0.65f * ((kotlin.math.sin(shimmer * Math.PI * 8.0 + i) * 0.5 + 0.5).toFloat())
                } else {
                    0.72f + 0.28f * twinkle
                }
                val bulbTint = if (songChangePulse) androidx.compose.ui.graphics.lerp(warmGold, themeColor, twinkle * 0.82f) else warmGold

                drawLine(Color(0xFF32251A), Offset(x, cordY), Offset(bulbX, bulbY - 3f * px), strokeWidth = 1.15f * px, cap = StrokeCap.Round)
                drawRoundRect(
                    color = Color(0xFF59432A),
                    topLeft = Offset(bulbX - 3.2f * px, bulbY - 5.8f * px),
                    size = Size(6.4f * px, 5f * px),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5f * px),
                )
                // Broad bloom, inner glow, shaded glass and a tiny specular glint.
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(bulbTint.copy(alpha = 0.32f * pulse), bulbTint.copy(alpha = 0.10f * pulse), Color.Transparent),
                        center = Offset(bulbX, bulbY),
                        radius = 31f * px,
                    ),
                    radius = 31f * px,
                    center = Offset(bulbX, bulbY),
                )
                drawCircle(bulbTint.copy(alpha = 0.19f * pulse), 8.5f * px, Offset(bulbX, bulbY))
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFFF8D8), bulbTint, warmAmber.copy(alpha = 0.96f)),
                        center = Offset(bulbX - 2f * px, bulbY - 2f * px),
                        radius = 8f * px,
                    ),
                    radius = 5.4f * px,
                    center = Offset(bulbX, bulbY),
                )
                drawCircle(Color.White.copy(alpha = 0.92f * pulse), 1.45f * px, Offset(bulbX - 1.7f * px, bulbY - 2.1f * px))
                drawCircle(Color(0xFFFFF0C2).copy(alpha = 0.42f * pulse), 2.1f * px, Offset(bulbX + 1.5f * px, bulbY + 1.5f * px))
            }
        } else if (style == MiniPlayerBackgroundStyle.LEAVES) {
            // Low-contrast forest-green haze gives the drifting leaves somewhere to live.
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF35563A).copy(alpha = 0.18f), Color.Transparent),
                    center = Offset(w * 0.52f, h * 0.50f),
                    radius = maxOf(w, h) * 0.76f,
                ),
                size = size,
            )

            val greens = listOf(Color(0xFF789C54), Color(0xFF426D3F), Color(0xFFB4C987), Color(0xFF5E8748), Color(0xFF9AAE63))
            // Draw a natural leaf silhouette with a tapered tip and curved sides.
            fun drawLeaf(cx: Float, cy: Float, leafW: Float, leafH: Float, angle: Float, color: Color, alpha: Float) {
                rotate(angle, pivot = Offset(cx, cy)) {
                    val leaf = Path().apply {
                        moveTo(cx, cy - leafH * 0.58f)
                        cubicTo(cx + leafW * 0.48f, cy - leafH * 0.34f, cx + leafW * 0.55f, cy + leafH * 0.18f, cx, cy + leafH * 0.55f)
                        cubicTo(cx - leafW * 0.48f, cy + leafH * 0.20f, cx - leafW * 0.52f, cy - leafH * 0.28f, cx, cy - leafH * 0.58f)
                        close()
                    }
                    drawPath(
                        leaf,
                        brush = Brush.linearGradient(
                            colors = listOf(color.copy(alpha = alpha), color.copy(alpha = alpha * 0.78f), Color(0xFF243D27).copy(alpha = alpha * 0.72f)),
                            start = Offset(cx - leafW * 0.4f, cy - leafH * 0.4f),
                            end = Offset(cx + leafW * 0.4f, cy + leafH * 0.5f),
                        ),
                    )
                    // Central vein and fine diagonal veins make each leaf read as a real leaf.
                    drawLine(Color(0xFFE4EBC5).copy(alpha = alpha * 0.84f), Offset(cx, cy - leafH * 0.46f), Offset(cx, cy + leafH * 0.43f), strokeWidth = 0.9f * px, cap = StrokeCap.Round)
                    for (vein in 0..2) {
                        val yy = cy - leafH * 0.18f + vein * leafH * 0.18f
                        val side = if (vein % 2 == 0) 1f else -1f
                        drawLine(
                            Color(0xFFD9E6B8).copy(alpha = alpha * 0.48f),
                            Offset(cx, yy),
                            Offset(cx + side * leafW * 0.29f, yy - leafH * 0.10f),
                            strokeWidth = 0.55f * px,
                            cap = StrokeCap.Round,
                        )
                    }
                    drawPath(leaf, color.copy(alpha = alpha * 0.18f), style = Stroke(width = 0.65f * px))
                }
            }

            // Larger foreground leaves travel diagonally and tumble gently in the breeze.
            for (i in 0 until 8) {
                val speed = 0.16f + (i % 4) * 0.035f
                val progress = (wind * speed + i * 0.137f) % 1f
                val x = w * progress
                val verticalWave = kotlin.math.sin((wind * 2.0 + i * 0.91) * Math.PI).toFloat()
                val y = h * (0.20f + (i % 5) * 0.135f + verticalWave * 0.065f)
                val leafH = (30f + (i % 4) * 8f) * px
                val leafW = leafH * (0.43f + (i % 3) * 0.04f)
                val rotation = kotlin.math.sin((wind * 1.6 + i * 0.7) * Math.PI * 2.0).toFloat() * 48f + 22f
                drawLeaf(x, y, leafW, leafH, rotation, greens[i % greens.size], 0.90f)
            }

            // These leaves are intentionally stationary: they have settled on the player's edges.
            drawLeaf(w * 0.075f, h * 0.13f, 18f * px, 39f * px, -42f + breeze * 3f, greens[2], 0.94f)
            drawLeaf(w * 0.91f, h * 0.20f, 16f * px, 35f * px, 48f + breeze * 2f, greens[0], 0.88f)
            drawLeaf(w * 0.13f, h * 0.83f, 17f * px, 37f * px, 53f + breeze * 2f, greens[3], 0.90f)
            drawLeaf(w * 0.84f, h * 0.91f, 19f * px, 41f * px, -46f + breeze * 3f, greens[1], 0.92f)
            drawLeaf(w * 0.51f, h * 0.96f, 14f * px, 30f * px, 72f + breeze * 2f, greens[4], 0.78f)
        }
    }
}

/**
 * Play button with circular progress indicator
 * Uses drawWithContent to update progress without recomposition
 */
@Composable
private fun NewMiniPlayerPlayButton(
    progressState: ProgressState,
    playbackState: Int,
    isCasting: Boolean,
    castHandler: CastConnectionHandler?,
    playerConnection: PlayerConnection,
    mediaMetadata: MediaMetadata?,
    primaryColor: Color,
    outlineColor: Color,
    listenTogetherManager: ListenTogetherManager?,
) {
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val castIsPlaying by castHandler?.castIsPlaying?.collectAsState() ?: remember { mutableStateOf(false) }
    val effectiveIsPlaying = if (isCasting) castIsPlaying else isPlaying
    val isListenTogetherGuest = listenTogetherManager?.let { it.isInRoom && !it.isHost } ?: false
    val isMuted by playerConnection.isMuted.collectAsStateWithLifecycle()

    val trackColor = outlineColor.copy(alpha = 0.2f)
    val strokeWidth = 3.dp

    Box(
        contentAlignment = Alignment.Center,
        modifier =
            Modifier
                .size(48.dp)
                .drawWithContent {
                    drawContent()
                    // Draw progress arc - this reads progressState.progress during draw phase only
                    val progress = progressState.progress
                    val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                    val startAngle = -90f
                    val sweepAngle = 360f * progress
                    val diameter = size.minDimension
                    val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)

                    // Draw track
                    drawArc(
                        color = trackColor,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(diameter, diameter),
                        style = stroke,
                    )
                    // Draw progress
                    drawArc(
                        color = primaryColor,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(diameter, diameter),
                        style = stroke,
                    )
                },
    ) {
        // Thumbnail with play/pause overlay
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(1.dp, outlineColor.copy(alpha = 0.3f), CircleShape)
                    .clickable {
                        if (isListenTogetherGuest) {
                            playerConnection.toggleMute()
                            return@clickable
                        }
                        if (isCasting) {
                            if (castIsPlaying) castHandler?.pause() else castHandler?.play()
                        } else if (playbackState == Player.STATE_ENDED) {
                            playerConnection.player.seekTo(0, 0)
                            playerConnection.player.playWhenReady = true
                        } else {
                            playerConnection.togglePlayPause()
                        }
                    },
        ) {
            mediaMetadata?.let { metadata ->
                val thumbnailUrl =
                    remember(metadata.thumbnailUrl) {
                        metadata.thumbnailUrl?.resize(120, 120)
                    }
                AsyncImage(
                    model = thumbnailUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                )
            }

            // Overlay for paused state or muted (guest)
            if (isListenTogetherGuest && isMuted ||
                (!isListenTogetherGuest && (!effectiveIsPlaying || playbackState == Player.STATE_ENDED))
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape),
                )
                Icon(
                    painter =
                        painterResource(
                            if (isListenTogetherGuest) {
                                if (isMuted) R.drawable.volume_off else R.drawable.volume_up
                            } else if (playbackState == Player.STATE_ENDED) {
                                R.drawable.replay
                            } else {
                                R.drawable.play
                            },
                        ),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

/**
 * Song info display - title and artist
 */
@Composable
private fun NewMiniPlayerSongInfo(
    mediaMetadata: MediaMetadata?,
    onSurfaceColor: Color,
    errorColor: Color,
    modifier: Modifier = Modifier,
) {
    val error by LocalPlayerConnection.current?.error?.collectAsState() ?: remember { mutableStateOf(null) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
    ) {
        mediaMetadata?.let { metadata ->
            Text(
                text = metadata.title,
                color = onSurfaceColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Clip,
                modifier = Modifier.basicMarquee(iterations = 1, initialDelayMillis = 3000, velocity = 30.dp),
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (metadata.explicit) MIcon.Explicit()
                 if (metadata.artists.any { it.name.isNotBlank() }) {
                     Text(
                         text = metadata.artists.joinToArtistString(" ${stringResource(R.string.and)} ") { it.name },
                         color = onSurfaceColor.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        modifier = Modifier.basicMarquee(iterations = 1, initialDelayMillis = 3000, velocity = 30.dp),
                    )
                }
            }

            AnimatedVisibility(visible = error != null, enter = fadeIn(), exit = fadeOut()) {
                Text(
                    text = stringResource(R.string.error_playing),
                    color = errorColor,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

// ============================================================================
// LEGACY MINI PLAYER DESIGN
// ============================================================================

@Composable
private fun LegacyMiniPlayer(
    progressState: ProgressState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val pureBlack by rememberPreference(PureBlackMiniPlayerKey, defaultValue = false)

    val playbackState by playerConnection.playbackState.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val canSkipNext by playerConnection.canSkipNext.collectAsStateWithLifecycle()
    val canSkipPrevious by playerConnection.canSkipPrevious.collectAsStateWithLifecycle()

    val castHandler =
        remember(playerConnection) {
            try {
                playerConnection.service.castConnectionHandler
            } catch (e: Exception) {
                null
            }
        }
    val isCasting by castHandler?.isCasting?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(false) }

    val swipeSensitivity by rememberPreference(SwipeSensitivityKey, 0.73f)
    val swipeThumbnailPref by rememberPreference(SwipeThumbnailKey, true)

    // Disable swipe for Listen Together guests
    val listenTogetherManager = LocalListenTogetherManager.current
    val isListenTogetherGuest = listenTogetherManager?.let { it.isInRoom && !it.isHost } ?: false
    val swipeThumbnail = swipeThumbnailPref && !isListenTogetherGuest

    val layoutDirection = LocalLayoutDirection.current
    val coroutineScope = rememberCoroutineScope()

    val windowInfo = LocalWindowInfo.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val isTabletLandscape =
        remember(windowInfo.containerSize.width, configuration.orientation) {
            (windowInfo.containerSize.width / density.density) >= 600f && configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        }

    val offsetXAnimatable = remember { Animatable(0f) }
    var dragStartTime by remember { mutableLongStateOf(0L) }
    var totalDragDistance by remember { mutableFloatStateOf(0f) }

    val animationSpec =
        remember {
            spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)
        }

    val autoSwipeThreshold =
        remember(swipeSensitivity) {
            (600 / (1f + kotlin.math.exp(-(-11.44748 * swipeSensitivity + 9.04945)))).roundToInt()
        }

    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier =
            modifier
                .then(if (isTabletLandscape) Modifier.width(500.dp) else Modifier.fillMaxWidth())
                .height(MiniPlayerHeight)
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(
                    if (pureBlack && isSystemInDarkTheme()) {
                        Color.Black
                    } else {
                        MaterialTheme.colorScheme.surfaceContainer
                    },
                ).clickable(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    onClick = onClick
                ).let { baseModifier ->
                    if (swipeThumbnail) {
                        baseModifier.pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragStart = {
                                    dragStartTime = System.currentTimeMillis()
                                    totalDragDistance = 0f
                                },
                                onDragCancel = {
                                    coroutineScope.launch { offsetXAnimatable.animateTo(0f, animationSpec) }
                                },
                                onHorizontalDrag = { _, dragAmount ->
                                    val adjustedDragAmount =
                                        if (layoutDirection == LayoutDirection.Rtl) -dragAmount else dragAmount
                                    val canSkipPrevious = playerConnection.player.previousMediaItemIndex != -1
                                    val canSkipNext = playerConnection.player.nextMediaItemIndex != -1
                                    val tryingToSwipeRight = adjustedDragAmount > 0
                                    val tryingToSwipeLeft = adjustedDragAmount < 0
                                    val allowLeft = tryingToSwipeLeft && canSkipNext
                                    val allowRight = tryingToSwipeRight && canSkipPrevious

                                    val canReturnToCenter =
                                        (tryingToSwipeRight && !canSkipPrevious && offsetXAnimatable.value < 0) ||
                                            (tryingToSwipeLeft && !canSkipNext && offsetXAnimatable.value > 0)

                                    if (allowLeft || allowRight || canReturnToCenter) {
                                        totalDragDistance += kotlin.math.abs(adjustedDragAmount)
                                        coroutineScope.launch {
                                            offsetXAnimatable.snapTo(offsetXAnimatable.value + adjustedDragAmount)
                                        }
                                    }
                                },
                                onDragEnd = {
                                    val dragDuration = System.currentTimeMillis() - dragStartTime
                                    val velocity = if (dragDuration > 0) totalDragDistance / dragDuration else 0f
                                    val currentOffset = offsetXAnimatable.value
                                    val minDistanceThreshold = 50f
                                    val velocityThreshold = (swipeSensitivity * -8.25f) + 8.5f

                                    val shouldChangeSong =
                                        (kotlin.math.abs(currentOffset) > minDistanceThreshold && velocity > velocityThreshold) ||
                                            (kotlin.math.abs(currentOffset) > autoSwipeThreshold)

                                    if (shouldChangeSong) {
                                        if (currentOffset > 0 && canSkipPrevious) {
                                            playerConnection.player.seekToPreviousMediaItem()
                                        } else if (currentOffset <= 0 && canSkipNext) {
                                            playerConnection.player.seekToNext()
                                        }
                                    }
                                    coroutineScope.launch { offsetXAnimatable.animateTo(0f, animationSpec) }
                                },
                            )
                        }
                    } else {
                        baseModifier
                    }
                },
    ) {
        // Progress bar - uses drawWithContent to avoid recomposition
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.BottomCenter)
                    .drawWithContent {
                        val progress = progressState.progress
                        drawRect(trackColor)
                        drawRect(primaryColor, size = Size(size.width * progress, size.height))
                    },
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .fillMaxSize()
                    .offset { IntOffset(offsetXAnimatable.value.roundToInt(), 0) }
                    .padding(end = 12.dp),
        ) {
            Box(Modifier.weight(1f)) {
                mediaMetadata?.let {
                    LegacyMiniMediaInfo(
                        mediaMetadata = it,
                        pureBlack = pureBlack,
                        modifier = Modifier.padding(horizontal = 6.dp),
                    )
                }
            }

            LegacyPlayPauseButton(
                playbackState = playbackState,
                isCasting = isCasting,
                castHandler = castHandler,
                playerConnection = playerConnection,
                listenTogetherManager = listenTogetherManager,
            )

            IconButton(
                enabled = canSkipNext && !isListenTogetherGuest,
                onClick = if (isListenTogetherGuest) ({}) else ({ playerConnection.seekToNext() }),
            ) {
                Icon(painter = painterResource(R.drawable.skip_next), contentDescription = null)
            }
        }

        // Swipe indicator
        if (offsetXAnimatable.value.absoluteValue > 50f) {
            Box(
                modifier =
                    Modifier
                        .align(if (offsetXAnimatable.value > 0) Alignment.CenterStart else Alignment.CenterEnd)
                        .padding(horizontal = 16.dp),
            ) {
                Icon(
                    painter =
                        painterResource(
                            if (offsetXAnimatable.value > 0) R.drawable.skip_previous else R.drawable.skip_next,
                        ),
                    contentDescription = null,
                    tint =
                        primaryColor.copy(
                            alpha = (offsetXAnimatable.value.absoluteValue / autoSwipeThreshold).coerceIn(0f, 1f),
                        ),
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Composable
private fun LegacyPlayPauseButton(
    playbackState: Int,
    isCasting: Boolean,
    castHandler: CastConnectionHandler?,
    playerConnection: PlayerConnection,
    listenTogetherManager: ListenTogetherManager?,
) {
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val castIsPlaying by castHandler?.castIsPlaying?.collectAsState() ?: remember { mutableStateOf(false) }
    val effectiveIsPlaying = if (isCasting) castIsPlaying else isPlaying
    val isListenTogetherGuest = listenTogetherManager?.let { it.isInRoom && !it.isHost } ?: false
    val isMuted by playerConnection.isMuted.collectAsStateWithLifecycle()

    IconButton(
        onClick = {
            if (isListenTogetherGuest) {
                playerConnection.toggleMute()
                return@IconButton
            }
            if (isCasting) {
                if (castIsPlaying) castHandler?.pause() else castHandler?.play()
            } else if (playbackState == Player.STATE_ENDED) {
                playerConnection.player.seekTo(0, 0)
                playerConnection.player.playWhenReady = true
            } else {
                playerConnection.togglePlayPause()
            }
        },
    ) {
        Icon(
            painter =
                painterResource(
                    when {
                        isListenTogetherGuest -> if (isMuted) R.drawable.volume_off else R.drawable.volume_up
                        playbackState == Player.STATE_ENDED -> R.drawable.replay
                        effectiveIsPlaying -> R.drawable.pause
                        else -> R.drawable.play
                    },
                ),
            contentDescription = null,
        )
    }
}

@Composable
private fun LegacyMiniMediaInfo(
    mediaMetadata: MediaMetadata,
    pureBlack: Boolean,
    modifier: Modifier = Modifier,
) {
    val error by LocalPlayerConnection.current?.error?.collectAsState() ?: remember { mutableStateOf(null) }
    val cropAlbumArt by rememberPreference(CropAlbumArtKey, false)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Box(
            modifier =
                Modifier
                    .padding(6.dp)
                    .size(48.dp)
                    .clip(RoundedCornerShape(ThumbnailCornerRadius)),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
            )

            val thumbnailUrl =
                remember(mediaMetadata.thumbnailUrl) {
                    mediaMetadata.thumbnailUrl?.resize(144, 144)
                }
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = null,
                contentScale = if (cropAlbumArt) ContentScale.Crop else ContentScale.Fit,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(ThumbnailCornerRadius)),
            )

            androidx.compose.animation.AnimatedVisibility(visible = error != null, enter = fadeIn(), exit = fadeOut()) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            color = if (pureBlack) Color.Black else Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(ThumbnailCornerRadius),
                        ),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.info),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }
        }

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp),
        ) {
            Text(
                text = mediaMetadata.title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.basicMarquee(),
            )

             if (mediaMetadata.artists.any { it.name.isNotBlank() }) {
                 Text(
                     text = mediaMetadata.artists.joinToArtistString(" ${stringResource(R.string.and)} ") { it.name },
                     color = MaterialTheme.colorScheme.secondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

// ============================================================================
// ISOLATED BUTTON COMPOSABLES - Prevent parent recomposition
// ============================================================================

@Composable
private fun SubscribeButton(
    artistId: String,
    metadata: MediaMetadata,
    primaryColor: Color,
    outlineColor: Color,
    onSurfaceColor: Color,
) {
    val database = LocalDatabase.current
    val libraryArtist by database.artist(artistId).collectAsStateWithLifecycle(initialValue = null)
    val isSubscribed = libraryArtist?.artist?.bookmarkedAt != null


    Box(
        contentAlignment = Alignment.Center,
        modifier =
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(
                    width = 1.dp,
                    color = if (isSubscribed) primaryColor.copy(alpha = 0.5f) else outlineColor.copy(alpha = 0.3f),
                    shape = CircleShape,
                ).background(
                    color = if (isSubscribed) primaryColor.copy(alpha = 0.1f) else Color.Transparent,
                    shape = CircleShape,
                ).clickable {
                    database.transaction {
                        val artist = libraryArtist?.artist
                        if (artist != null) {
                            update(artist.toggleLike())
                        } else {
                            metadata.artists.firstOrNull()?.let { artistInfo ->
                                insert(
                                    ArtistEntity(
                                        id = artistInfo.id ?: "",
                                        name = artistInfo.name,
                                        channelId = null,
                                        thumbnailUrl = null,
                                    ).toggleLike(),
                                )
                            }
                        }
                    }
                },
    ) {
        Icon(
            painter = painterResource(if (isSubscribed) R.drawable.subscribed else R.drawable.subscribe),
            contentDescription = null,
            tint = if (isSubscribed) primaryColor else onSurfaceColor.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun FavoriteButton(
    songId: String,
    errorColor: Color,
    outlineColor: Color,
    onSurfaceColor: Color,
) {
    val database = LocalDatabase.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val librarySong by database.song(songId).collectAsStateWithLifecycle(initialValue = null)
    // For episodes, show saved state (inLibrary); for songs, show liked state
    val isEpisode = librarySong?.song?.isEpisode == true
    val isLiked = if (isEpisode) librarySong?.song?.inLibrary != null else librarySong?.song?.liked == true

    Box(
        contentAlignment = Alignment.Center,
        modifier =
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(
                    width = 1.dp,
                    color = if (isLiked) errorColor.copy(alpha = 0.5f) else outlineColor.copy(alpha = 0.3f),
                    shape = CircleShape,
                ).background(
                    color = if (isLiked) errorColor.copy(alpha = 0.1f) else Color.Transparent,
                    shape = CircleShape,
                ).clickable { playerConnection.service.toggleLike() },
    ) {
        Icon(
            painter = painterResource(if (isLiked) R.drawable.favorite else R.drawable.favorite_border),
            contentDescription = null,
            tint = if (isLiked) errorColor else onSurfaceColor.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp),
        )
    }
}
