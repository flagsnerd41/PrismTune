#!/usr/bin/env python3
"""
PrismTune wave12
- Heavier real Haze blur (nav + mini player)
- New retro TV header icon
- Attempt to clean Now Playing bottom overlap / glass bug
- Small header polish
"""
import os, re, shutil

B = 'app/src/main/kotlin/com/metrolist/music/'

def load(p):
    return open(p, encoding='utf-8').read()

def save(p, s):
    open(p, 'w', encoding='utf-8').write(s)

def need(s, old, where):
    if old not in s:
        print(f'WARNING: MISSING in {where}: {old[:80]}...')
        return False
    return True

def rep(s, old, new, where, count=1):
    if not need(s, old, where):
        return s
    return s.replace(old, new, count)

print('=== wave12 starting ===')

# ---------- 0. place new icon ----------
os.makedirs('app/src/main/res/drawable', exist_ok=True)
if os.path.exists('prism_header_icon.png'):
    shutil.copy('prism_header_icon.png', 'app/src/main/res/drawable/prism_header_icon.png')
    print('icon placed')
else:
    print('WARNING: prism_header_icon.png not found next to script')

# ---------- 1. Heavier blur in MainActivity ----------
p = B + 'MainActivity.kt'
if os.path.exists(p):
    s = load(p)
    # bump blur radius
    s = rep(s,
            'blurRadius = 24.dp',
            'blurRadius = 40.dp',
            p)
    s = rep(s,
            'blurRadius = 28.dp',
            'blurRadius = 40.dp',
            p)
    s = rep(s,
            'blurRadius = 32.dp',
            'blurRadius = 40.dp',
            p)
    # also make the tint a bit more transparent so heavier blur is visible
    s = rep(s,
            'Color.Black.copy(alpha = 0.65f)',
            'Color.Black.copy(alpha = 0.55f)',
            p)
    save(p, s)
    print('MainActivity blur updated')
else:
    print('MainActivity.kt not found')

# ---------- 2. Heavier blur + cleaner mini player ----------
p = B + 'ui/player/MiniPlayer.kt'
if os.path.exists(p):
    s = load(p)
    s = rep(s,
            'blurRadius = 24.dp',
            'blurRadius = 40.dp',
            p)
    s = rep(s,
            'blurRadius = 28.dp',
            'blurRadius = 40.dp',
            p)
    # slightly stronger glass
    s = rep(s,
            'backgroundColor.copy(alpha = 0.6f)',
            'backgroundColor.copy(alpha = 0.5f)',
            p)
    s = rep(s,
            'backgroundColor.copy(alpha = 0.65f)',
            'backgroundColor.copy(alpha = 0.5f)',
            p)
    save(p, s)
    print('MiniPlayer blur updated')
else:
    print('MiniPlayer.kt not found')

# ---------- 3. AppNavigation tint (so blur shows better) ----------
p = B + 'ui/component/AppNavigation.kt'
if os.path.exists(p):
    s = load(p)
    s = rep(s,
            'Color.Black.copy(alpha = 0.65f)',
            'Color.Black.copy(alpha = 0.5f)',
            p)
    s = rep(s,
            'surfaceContainer.copy(alpha = 0.6f)',
            'surfaceContainer.copy(alpha = 0.45f)',
            p)
    s = rep(s,
            'surfaceContainer.copy(alpha = 0.78f)',
            'surfaceContainer.copy(alpha = 0.45f)',
            p)
    save(p, s)
    print('AppNavigation tint updated')

# ---------- 4. Header icon + PrismTitleBanner polish ----------
# Try common locations for the title banner / branding
candidates = [
    B + 'ui/component/PrismBranding.kt',
    B + 'ui/component/PrismTitleBanner.kt',
    B + 'ui/home/PrismTitleBanner.kt',
    B + 'ui/component/HomeHeader.kt',
]

for p in candidates:
    if not os.path.exists(p):
        continue
    s = load(p)
    changed = False

    # force use of our new icon if painterResource is used for header
    if 'prism_header_icon' not in s and 'R.drawable' in s:
        # try to swap any existing header icon reference
        s2 = re.sub(
            r'painterResource\(R\.drawable\.[a-zA-Z0-9_]+\)',
            'painterResource(R.drawable.prism_header_icon)',
            s,
            count=1
        )
        if s2 != s:
            s = s2
            changed = True
            print(f'header icon swapped in {p}')

    # make sure Image import exists if we touch it
    if changed and 'import androidx.compose.foundation.Image' not in s:
        s = s.replace(
            'import androidx.compose.foundation.',
            'import androidx.compose.foundation.Image\nimport androidx.compose.foundation.',
            1
        )

    if changed:
        save(p, s)
        print(f'updated {p}')
    else:
        print(f'no header change needed / pattern not found in {p}')

# ---------- 5. Now Playing bottom bug ----------
# The circled area looks like mini-player glass is leaking under the full player controls.
# We try to make the full player hide the mini player more cleanly and reduce bottom haze.

player_candidates = [
    B + 'ui/player/PlayerScreen.kt',
    B + 'ui/player/FullPlayer.kt',
    B + 'ui/player/NowPlayingScreen.kt',
    B + 'ui/player/Player.kt',
    B + 'ui/player/Queue.kt',
]

for p in player_candidates:
    if not os.path.exists(p):
        continue
    s = load(p)
    original = s

    # remove or weaken any hazeEffect near the bottom controls if present
    s = re.sub(
        r'\.hazeEffect\(state\s*=\s*[^)]+\)\s*\{\s*blurRadius\s*=\s*\d+\.dp[^}]*\}',
        '',
        s,
        count=2
    )

    # common pattern: make sure mini player is not drawn when player is expanded
    # (this is defensive – only if the pattern exists)
    s = rep(s,
            'if (!playerBottomSheetState.isVisible)',
            'if (!playerBottomSheetState.isVisible && !isExpanded)',
            p, count=1)

    if s != original:
        save(p, s)
        print(f'Player screen cleaned: {p}')
    else:
        print(f'Player pattern not matched in {p}')

# Also check MiniPlayer for any expansion-related visibility
p = B + 'ui/player/MiniPlayer.kt'
if os.path.exists(p):
    s = load(p)
    # make the mini player background a bit cleaner
    s = rep(s,
            '.background(color = backgroundColor.copy(alpha = 0.5f))',
            '.background(color = backgroundColor.copy(alpha = 0.42f))',
            p)
    save(p, s)

# ---------- 6. Library / Playlist small polish (safe) ----------
# Only light touch – make sure tiles don't have heavy solid backgrounds
lib_candidates = [
    B + 'ui/screens/library/LibraryMixScreen.kt',
    B + 'ui/screens/library/LibraryScreen.kt',
    B + 'ui/screens/playlist/LocalPlaylistScreen.kt',
    B + 'ui/screens/playlist/PlaylistScreen.kt',
]

for p in lib_candidates:
    if not os.path.exists(p):
        continue
    s = load(p)
    # slightly reduce heavy solid backgrounds if present
    s = rep(s,
            'surfaceContainer.copy(alpha = 0.85f)',
            'surfaceContainer.copy(alpha = 0.7f)',
            p)
    s = rep(s,
            'surfaceContainerHigh.copy(alpha = 0.85f)',
            'surfaceContainerHigh.copy(alpha = 0.7f)',
            p)
    save(p, s)
    print(f'light polish on {p}')

print('=== wave12 applied OK ===')
print('Rebuild the APK and check:')
print('  1. Heavier blur on nav + mini player')
print('  2. New retro icon in header (if the banner uses drawable)')
print('  3. Now Playing bottom area should be cleaner')
