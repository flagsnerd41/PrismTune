/**
 * PrismTune / Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.screens.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.metrolist.music.LocalPlayerAwareWindowInsets
import com.metrolist.music.LocalPlayerConnection
import com.metrolist.music.R
import com.metrolist.music.ui.component.IconButton
import com.metrolist.music.ui.component.Material3SettingsGroup
import com.metrolist.music.ui.component.Material3SettingsItem
import com.metrolist.music.ui.utils.backToMain
import kotlinx.coroutines.CoroutineScope

private data class Contributor(
    val name: String,
    val roleRes: Int,
    val githubHandle: String,
    val avatarUrl: String = "https://github.com/$githubHandle.png",
    val githubUrl: String = "https://github.com/$githubHandle",
    val sponsorUrl: String? = null,
    val polygon: Any? = null,
    val favoriteSongVideoId: String? = null,
)

private data class CommunityLink(
    val labelRes: Int,
    val iconRes: Int,
    val url: String,
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private val leadDeveloper = Contributor(
    name = "Mo Agamy",
    roleRes = R.string.credits_lead_developer,
    githubHandle = "mostafaalagamy",
    polygon = MaterialShapes.Cookie9Sided,
    favoriteSongVideoId = "Mh2JWGWvy_Y",
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private val collaborators = listOf(
    Contributor(
        name = "Adriel O'Connel",
        roleRes = R.string.credits_collaborator,
        githubHandle = "adrielGGmotion",
        sponsorUrl = "https://github.com/sponsors/adrielGGmotion",
        polygon = MaterialShapes.Cookie4Sided,
        favoriteSongVideoId = "m2zUrruKjDQ",
    ),
    Contributor(
        name = "Nyx",
        roleRes = R.string.credits_collaborator,
        githubHandle = "nyxiereal",
        sponsorUrl = "https://github.com/sponsors/nyxiereal",
        polygon = MaterialShapes.Cookie12Sided,
        favoriteSongVideoId = "zselaN6zPXw",
    ),
)

private val communityLinks = listOf(
    CommunityLink(R.string.credits_discord, R.drawable.discord, "https://discord.com/invite/zrdbeRG2Mt"),
    CommunityLink(R.string.credits_telegram, R.drawable.telegram, "https://t.me/metrolistapp"),
    CommunityLink(R.string.credits_view_repo, R.drawable.github, "https://github.com/flagsnerd41/PrismTune2"),
    CommunityLink(R.string.credits_license_name, R.drawable.info, "https://github.com/flagsnerd41/PrismTune2/blob/main/LICENSE"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val uriHandler = LocalUriHandler.current
    val windowInsets = LocalPlayerAwareWindowInsets.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val playerConnection = LocalPlayerConnection.current

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(windowInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier = Modifier.windowInsetsPadding(windowInsets.only(WindowInsetsSides.Top)))
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.about),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Material3SettingsGroup(
            title = stringResource(R.string.community_and_info),
            items =
                communityLinks.map { link ->
                    Material3SettingsItem(
                        icon = painterResource(link.iconRes),
                        title = { Text(stringResource(link.labelRes), fontWeight = FontWeight.SemiBold) },
                        description =
                            if (link.labelRes == R.string.credits_license_name) {
                                { Text(stringResource(R.string.credits_license_desc)) }
                            } else {
                                null
                            },
                        onClick = { uriHandler.openUri(link.url) },
                    )
                },
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = stringResource(R.string.stands_with_palestine),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(48.dp))
    }

    TopAppBar(
        title = { Text(stringResource(R.string.about)) },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain,
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow_back),
                    contentDescription = stringResource(R.string.cd_back),
                )
            }
        },
    )

    Box(Modifier.fillMaxSize()) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(windowInsets.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)),
        )
    }
}
