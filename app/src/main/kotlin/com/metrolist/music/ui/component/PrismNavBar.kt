package com.metrolist.music.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.metrolist.music.ui.screens.Screens

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PrismNavBar(
    navigationItems: List<Screens>,
    currentRoute: String?,
    onItemClick: (Screens, Boolean) -> Unit,
    pureBlack: Boolean,
    slimNav: Boolean,
    onSearchLongClick: () -> Unit,
    onHomeLongHold: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = if (pureBlack) Color.Black else MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier,
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            navigationItems.forEach { screen ->
                val selected = currentRoute?.startsWith(screen.route) == true
                val tint =
                    if (selected) MaterialTheme.colorScheme.onSecondaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(20.dp))
                            .combinedClickable(
                                onClick = { onItemClick(screen, selected) },
                                onLongClick =
                                    when (screen.route) {
                                        Screens.Home.route -> onHomeLongHold
                                        Screens.Search.route -> onSearchLongClick
                                        else -> null
                                    },
                            ),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier =
                            Modifier
                                .width(60.dp)
                                .height(30.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selected) MaterialTheme.colorScheme.secondaryContainer
                                    else Color.Transparent,
                                ),
                    ) {
                        Icon(
                            painter = painterResource(if (selected) screen.iconIdActive else screen.iconIdInactive),
                            contentDescription = null,
                            tint = tint,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    if (!slimNav) {
                        Text(
                            text = stringResource(screen.titleId),
                            style = MaterialTheme.typography.labelMedium,
                            color = tint,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}
