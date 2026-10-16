#!/usr/bin/env python3
"""Hide Speed Dial and switch Quick Picks to large ArchiveTune-style cards."""
from pathlib import Path
import subprocess
import sys

HS = Path("app/src/main/kotlin/com/metrolist/music/ui/screens/HomeScreen.kt")
GOOD = "1c18498f9ad70b914e460ee8ae95dbba06ab8bc6"

# Restore from last known-good commit
raw = subprocess.check_output(
    ["git", "show", f"{GOOD}:{HS}"],
    text=True,
)
HS.write_text(raw)
text = HS.read_text()

old = "            if (!chipActive && speedDialItems.isNotEmpty()) list.add(HomeSection.SpeedDial)\n"
new = (
    "            // Speed Dial removed — ArchiveTune-style Quick Picks only\n"
    "            // if (!chipActive && speedDialItems.isNotEmpty()) list.add(HomeSection.SpeedDial)\n"
)
if old not in text:
    sys.exit("speed dial add line not found")
text = text.replace(old, new, 1)

start = text.find('                                item(key = "quick_picks_list") {')
if start < 0:
    sys.exit("quick_picks_list not found")
end = text.find("                        HomeSection.FromTheCommunity -> {", start)
if end < 0:
    sys.exit("FromTheCommunity not found")

before, after = text[:start], text[end:]
replacement = r'''                                item(key = "quick_picks_list") {
                                    // ArchiveTune-style large horizontal Quick Picks cards
                                    LazyRow(
                                        contentPadding = PaddingValues(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        items(
                                            items = quickPicks.distinctBy { it.id },
                                            key = { "home_quickpick_${it.id}" },
                                        ) { originalSong ->
                                            val song by database
                                                .song(originalSong.id)
                                                .collectAsStateWithLifecycle(initialValue = originalSong)
                                            val display = song ?: originalSong

                                            Box(
                                                modifier =
                                                    Modifier
                                                        .width(280.dp)
                                                        .height(320.dp)
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
                                                                    colors =
                                                                        listOf(
                                                                            Color.Transparent,
                                                                            Color.Black.copy(alpha = 0.75f),
                                                                        ),
                                                                ),
                                                            ),
                                                )
                                                Column(
                                                    modifier =
                                                        Modifier
                                                            .align(Alignment.BottomStart)
                                                            .padding(16.dp),
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
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = Color.White.copy(alpha = 0.85f),
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
assert "ArchiveTune-style large horizontal Quick Picks" in HS.read_text()
