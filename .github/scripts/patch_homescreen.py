#!/usr/bin/env python3
"""Hide Speed Dial; Quick Picks = big HorizontalMultiBrowseCarousel (same as Daily Discover)."""
from pathlib import Path
import subprocess
import sys

HS = Path("app/src/main/kotlin/com/metrolist/music/ui/screens/HomeScreen.kt")
GOOD = "1c18498f9ad70b914e460ee8ae95dbba06ab8bc6"

raw = subprocess.check_output(["git", "show", f"{GOOD}:{HS}"], text=True)
HS.write_text(raw)
text = HS.read_text()
if len(text) < 100000:
    sys.exit(f"HomeScreen too small after restore: {len(text)}")

old = "            if (!chipActive && speedDialItems.isNotEmpty()) list.add(HomeSection.SpeedDial)\n"
new = (
    "            // Speed Dial removed — ArchiveTune-style big Quick Picks\n"
    "            // if (!chipActive && speedDialItems.isNotEmpty()) list.add(HomeSection.SpeedDial)\n"
)
if old not in text:
    sys.exit("speed dial line not found")
text = text.replace(old, new, 1)

start = text.find('                                item(key = "quick_picks_list") {')
if start < 0:
    sys.exit("quick_picks_list not found")
end = text.find("                        HomeSection.FromTheCommunity -> {", start)
if end < 0:
    sys.exit("FromTheCommunity not found")

before, after = text[:start], text[end:]
replacement = r'''                                item(key = "quick_picks_list") {
                                    // Big Quick Picks tiles (ArchiveTune-style), same carousel API as Daily Discover
                                    val distinctQuickPicks = quickPicks.distinctBy { it.id }
                                    Box(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .height(340.dp)
                                                .padding(horizontal = 16.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        val carouselState = rememberCarouselState { distinctQuickPicks.size }
                                        HorizontalMultiBrowseCarousel(
                                            state = carouselState,
                                            preferredItemWidth = 280.dp,
                                            itemSpacing = 14.dp,
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .height(320.dp),
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
out = HS.read_text()
print("patched", len(out))
assert "Speed Dial removed" in out
assert "HorizontalMultiBrowseCarousel" in out[out.find("quick_picks_list") : out.find("FromTheCommunity")]
assert "LazyHorizontalGrid" not in out[out.find("quick_picks_list") : out.find("FromTheCommunity")]
print("ok")
