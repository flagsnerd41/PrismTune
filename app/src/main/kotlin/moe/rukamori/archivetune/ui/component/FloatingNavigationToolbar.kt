/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package moe.rukamori.archivetune.ui.component

import android.os.SystemClock
import android.view.ViewConfiguration
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarArrangement
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import moe.rukamori.archivetune.constants.FloatingBarJunctionCornerRadius
import moe.rukamori.archivetune.constants.FloatingBarOuterCornerRadius
import moe.rukamori.archivetune.constants.FloatingBarStandaloneCornerRadius
import moe.rukamori.archivetune.constants.NavigationBarHeight
import moe.rukamori.archivetune.constants.NavigationBarMaxWidth
import moe.rukamori.archivetune.ui.screens.Screens

private val NavigationItemsMaxWidth = 360.dp
private val NavigationItemVerticalPadding = 8.dp

@Composable
fun FloatingNavigationToolbar(
    items: List<Screens>,
    pureBlack: Boolean,
    modifier: Modifier = Modifier,
    miniPlayerProximityProvider: () -> Float = { 0f },
    isSelected: (Screens) -> Boolean,
    onItemClick: (Screens, Boolean) -> Unit,
    onSearchItemDoubleClick: (() -> Unit)? = null,
) {
    val miniPlayerProximity = miniPlayerProximityProvider()
    val navigationShape =
        RoundedCornerShape(
            topStart = lerp(FloatingBarStandaloneCornerRadius.value, FloatingBarJunctionCornerRadius.value, miniPlayerProximity).dp,
            topEnd = lerp(FloatingBarStandaloneCornerRadius.value, FloatingBarJunctionCornerRadius.value, miniPlayerProximity).dp,
            bottomStart = lerp(FloatingBarStandaloneCornerRadius.value, FloatingBarOuterCornerRadius.value, miniPlayerProximity).dp,
            bottomEnd = lerp(FloatingBarStandaloneCornerRadius.value, FloatingBarOuterCornerRadius.value, miniPlayerProximity).dp,
        )
    val navigationContainerColor =
        if (pureBlack) Color.Black else MaterialTheme.colorScheme.surfaceContainer
    val motionScheme = MaterialTheme.motionScheme
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)),
        contentAlignment = Alignment.Center,
    ) {
        // A softly diffused underlay adds depth to the floating glass bar.
        Box(
            modifier =
                Modifier
                    .widthIn(max = NavigationBarMaxWidth)
                    .fillMaxWidth()
                    .height(NavigationBarHeight)
                    .clip(navigationShape)
                    .background(navigationContainerColor.copy(alpha = 0.46f))
                    .blur(18.dp),
        )
        Surface(
            modifier =
                Modifier
                    .widthIn(max = NavigationBarMaxWidth)
                    .fillMaxWidth()
                    .height(NavigationBarHeight)
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.12f),
                        shape = navigationShape,
                    ),
            shape = navigationShape,
            color = navigationContainerColor.copy(alpha = 0.78f),
            tonalElevation = 0.dp,
            shadowElevation = NavigationBarDefaults.Elevation,
        ) {
            ShortNavigationBar(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                contentColor = if (pureBlack) Color.White else MaterialTheme.colorScheme.onSurface,
                windowInsets = WindowInsets(0, 0, 0, 0),
                arrangement = ShortNavigationBarArrangement.EqualWeight,
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        modifier =
                            Modifier
                                .widthIn(max = NavigationItemsMaxWidth)
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .padding(vertical = NavigationItemVerticalPadding),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        items.forEach { screen ->
                            val selected = isSelected(screen)
                            val onDoubleClick =
                                remember(screen, onSearchItemDoubleClick) {
                                    if (screen == Screens.Search) onSearchItemDoubleClick else null
                                }
                            val lastClickTime = remember(screen) { mutableLongStateOf(0L) }
                            val onClick =
                                remember(screen, selected, onItemClick, onDoubleClick) {
                                    {
                                        val currentTime = SystemClock.uptimeMillis()
                                        val isDoubleClick =
                                            onDoubleClick != null &&
                                                currentTime - lastClickTime.longValue <= ViewConfiguration.getDoubleTapTimeout()
                                        lastClickTime.longValue = if (isDoubleClick) 0L else currentTime
                                        if (isDoubleClick) {
                                            onDoubleClick?.invoke()
                                            Unit
                                        } else {
                                            onItemClick(screen, selected)
                                        }
                                    }
                                }

                            ShortNavigationBarItem(
                                selected = selected,
                                onClick = onClick,
                                modifier = Modifier.weight(1f),
                                icon = {
                                    Crossfade(
                                        targetState = selected,
                                        animationSpec = motionScheme.fastEffectsSpec(),
                                        label = "navigationItemIcon",
                                    ) { isSelected ->
                                        Icon(
                                            painter =
                                                painterResource(
                                                    if (isSelected) screen.iconIdActive else screen.iconIdInactive,
                                                ),
                                            contentDescription = null,
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = stringResource(screen.titleId),
                                        maxLines = 1,
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
