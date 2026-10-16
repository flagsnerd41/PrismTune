#!/usr/bin/env python3
"""Port ArchiveTune-style Quick Picks hero carousel; hide Speed Dial."""
from pathlib import Path
import subprocess
import sys

HS = Path("app/src/main/kotlin/com/metrolist/music/ui/screens/HomeScreen.kt")
GOOD = "1c18498f9ad70b914e460ee8ae95dbba06ab8bc6"

raw = subprocess.check_output(["git", "show", f"{GOOD}:{HS}"], text=True)
HS.write_text(raw)
text = HS.read_text()

# Hide Speed Dial section registration
old = "            if (!chipActive && speedDialItems.isNotEmpty()) list.add(HomeSection.SpeedDial)\n"
new = (
    "            // Speed Dial removed — ArchiveTune Quick Picks hero carousel instead\n"
    "            // if (!chipActive && speedDialItems.isNotEmpty()) list.add(HomeSection.SpeedDial)\n"
)
if old not in text:
    sys.exit("speed dial add line not found")
text = text.replace(old, new, 1)

# Ensure carousel imports exist (Material3 experimental carousel)
if "HorizontalCenteredHeroCarousel" not in text:
    # Add imports near other foundation/material3 imports
    anchor = "import androidx.compose.foundation.lazy.LazyRow\n"
    inject = (
        anchor
        + "import androidx.compose.material3.carousel.HorizontalCenteredHeroCarousel\n"
        + "import androidx.compose.material3.carousel.rememberCarouselState\n"
        + "import androidx.compose.material3.ExperimentalMaterial3Api\n"
    )
    if anchor in text:
        text = text.replace(anchor, inject, 1)
    else:
        sys.exit("LazyRow import not found for carousel inject")

start = text.find('                                item(key = "quick_picks_list") {')
if start < 0:
    sys.exit("quick_picks_list not found")
end = text.find("                        HomeSection.FromTheCommunity -> {", start)
if end < 0:
    sys.exit("FromTheCommunity not found")

before, after = text[:start], text[end:]

# Adapted from ArchiveTune HomeScreenComponents.QuickPicksSection CARD mode
# (rukamori/ArchiveTune, GPL-3.0) — hero carousel with gradient + title overlay
replacement = r'''                                item(key = "quick_picks_list") {
                                    // ArchiveTune-style Quick Picks hero carousel
                                    val distinctQuickPicks = quickPicks.distinctBy { it.id }
                                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                        val heroHeight =
                                            when {
                                                maxWidth >= 840.dp -> 380.dp
                                                maxWidth >= 600.dp -> 356.dp
                                                else -> 332.dp
                                            }
                                        val heroMaxWidth =
                                            (maxWidth - 48.dp)
                                                .coerceAtLeast(232.dp)
                                                .coerceAtMost(440.dp)

                                        HorizontalCenteredHeroCarousel(
                                            state = rememberCarouselState { distinctQuickPicks.size },
                                            maxItemWidth = heroMaxWidth,
                                            itemSpacing = 10.dp,
                                            contentPadding = PaddingValues(horizontal = 16.dp),
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .height(heroHeight),
                                        ) { index ->
                                            val originalSong = distinctQuickPicks[index]
                                            val song by database
                                                .song(originalSong.id)
                                                .collectAsStateWithLifecycle(initialValue = originalSong)
                                            val display = song ?: originalSong

                                            Box(
                                                modifier =
                                                    Modifier
                                                        .fillMaxSize()
                                                        .clip(RoundedCornerShape(24.dp))
                                                        .combinedClickable(
                                                            onClick = {
                                                                if (!isListenTogetherGuest) {
                                                                    playerConnection.playQueue(
                                                                        if (autoRadioQueue) {
                                                                            YouTubeQueue.radio(display.toMediaMetadata())
                                                                        } else {
                                                                            ListQueue(
                                                                                title = display.title,
                                                                                items = listOf(display.toMediaItem()),
                                                                            )
                                                                        },
                                                                    )
                                                                }
                                                            },
                                                            onLongClick = {
                                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                menuState.show {
                                                                    SongMenu(
                                                                        originalSong = display,
                                                                        onDismiss = menuState::dismiss,
                                                                    )
                                                                }
                                                            },
                                                        ),
                                            ) {
                                                AsyncImage(
                                                    model = display.thumbnailUrl?.resize(800, 800),
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize(),
                                                )
                                                Box(
                                                    modifier =
                                                        Modifier
                                                            .fillMaxSize()
                                                            .background(
                                                                Brush.verticalGradient(
                                                                    colorStops =
                                                                        arrayOf(
                                                                            0.35f to Color.Transparent,
                                                                            1f to Color.Black.copy(alpha = 0.84f),
                                                                        ),
                                                                ),
                                                            ),
                                                )
                                                Column(
                                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                                    modifier =
                                                        Modifier
                                                            .align(Alignment.BottomStart)
                                                            .padding(20.dp),
                                                ) {
                                                    Text(
                                                        text = display.title,
                                                        style = MaterialTheme.typography.titleLarge,
                                                        color = Color.White,
                                                        maxLines = 2,
                                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                                    )
                                                    Text(
                                                        text = display.artists.joinToArtistString(" • ") { it.name },
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        color = Color.White.copy(alpha = 0.78f),
                                                        maxLines = 1,
                                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

'''

HS.write_text(before + replacement + after)
print("patched", HS.stat().st_size)
assert "Speed Dial removed" in HS.read_text()
assert "HorizontalCenteredHeroCarousel" in HS.read_text()
