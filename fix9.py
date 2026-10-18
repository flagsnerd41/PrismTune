import os, shutil
P = 'app/src/main/kotlin/com/metrolist/music/ui/component/PrismBranding.kt'
s = open(P).read()

def rep(old, new):
    global s
    assert old in s, 'MISSING: ' + old[:60]
    s = s.replace(old, new)

def region(start, end, new):
    global s
    a = s.index(start); b = s.index(end)
    s = s[:a] + new + s[b:]

rep('import androidx.compose.animation.core.Animatable\n',
    'import androidx.compose.animation.core.Animatable\nimport androidx.compose.animation.core.LinearEasing\nimport androidx.compose.foundation.Image\nimport androidx.compose.foundation.layout.size\nimport androidx.compose.foundation.layout.width\nimport androidx.compose.ui.res.painterResource\nimport com.metrolist.music.R\n')

# calmer rope + bulbs
rep('val ax = local * 420f * d', 'val ax = local * 140f * d')
rep('val ay = g - local * 40f * d', 'val ay = g - local * 12f * d')
rep('0.12f * sin(t * 3.1f + i * 0.9f)', '0.04f * sin(t * 3.1f + i * 0.9f)')
rep('* 0.985f', '* 0.96f')
rep('val sub = 2', 'val sub = 1')
rep('for (iter in 0 until 8)', 'for (iter in 0 until 5)')
rep('- 2.2f * omega[i]', '- 4.5f * omega[i]')
rep('wind * 8f * (0.6f + 0.4f * sin(t * 2.1f + phase))', 'wind * 3f * (0.6f + 0.4f * sin(t * 2.1f + phase))')
rep('0.10f * sin(t * (2.1f + (i % 3) * 0.8f) + phase) + 0.06f * sin(t * 7.3f + phase * 1.7f)',
    '0.05f * sin(t * (1.2f + (i % 3) * 0.4f) + phase) + 0.02f * sin(t * 4f + phase * 1.7f)')
rep('sin(t * 14f + phase * 2.3f)', 'sin(t * 6f + phase * 2.3f)')

# cheaper bulb (no gradient brushes per frame)
region('private fun DrawScope.drawPrismBulb(', 'private fun DrawScope.drawPrismFairyLights(', '''private fun DrawScope.drawPrismBulb(
    c: Offset,
    r: Float,
    bulb: Color,
    b: Float,
    d: Float,
) {
    drawCircle(color = bulb.copy(alpha = 0.10f * b), radius = 18f * d, center = c)
    drawCircle(color = bulb.copy(alpha = 0.22f * b), radius = 10f * d, center = c)
    drawCircle(color = lerp(Color(0xFF3A322B), bulb, b), radius = r, center = c)
    drawCircle(color = Color.White.copy(alpha = 0.75f * b), radius = 0.45f * r, center = c)
}

''')

# light welcome splash
region('@Composable\nfun WelcomeSplash(', 'private fun prismGreeting()', '''private fun DrawScope.drawSplashLights(t: Float, d: Float, level: Float, colors: List<Color>) {
    val w = size.width
    val wire = Color(0xFF2B2118).copy(alpha = 0.85f * level)
    for (s in 0..1) {
        val y0 = (if (s == 0) 36f else 18f) * d
        val sag = (if (s == 0) 34f else 22f) * d
        val path = Path()
        val n = 14
        for (i in 0..n) {
            val u = i / n.toFloat()
            val x = -8f * d + (w + 16f * d) * u
            val y = y0 + sag * 4f * u * (1f - u)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path = path, color = wire, style = Stroke(width = 1.5f * d))
        val count = if (s == 0) 9 else 7
        for (k in 0 until count) {
            val u = (k + 0.5f) / count
            val x = -8f * d + (w + 16f * d) * u
            val y = y0 + sag * 4f * u * (1f - u) + 8f * d
            val br = (0.8f + 0.2f * sin(t * 2.4f + k * 1.7f + s * 2f)) * level
            val c = colors[(k + s) % colors.size]
            drawCircle(color = c.copy(alpha = 0.18f * br), radius = 12f * d, center = Offset(x, y))
            drawCircle(color = c.copy(alpha = 0.9f * br), radius = 4.5f * d, center = Offset(x, y))
        }
    }
}

@Composable
fun WelcomeSplash(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress = remember { Animatable(0f) }
    val overlayAlpha = remember { Animatable(1f) }
    val greeting = remember { prismGreeting() }
    val leaves = remember { buildPrismLeaves(7, 9f, 18f, PrismLeafColors, 1f) }
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val gradient = listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.primaryContainer)
    val bulbColors = remember(primary, tertiary) {
        listOf(Color(0xFFFFE7B0), lerp(primary, Color.White, 0.45f), Color(0xFFFFE7B0), lerp(tertiary, Color.White, 0.45f))
    }

    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(2300, easing = LinearEasing))
        overlayAlpha.animateTo(0f, tween(350))
        onFinished()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .fillMaxSize()
                .graphicsLayer { alpha = overlayAlpha.value }
                .background(Brush.verticalGradient(gradient))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val t = progress.value * 2.3f
            val d = 1.dp.toPx()
            val leafFade = ((t - 0.4f) / 0.6f).coerceIn(0f, 1f)
            drawPrismLeaves(leaves, t, size.width, size.height, d, 0.35f, primary, leafFade, false)
            drawSplashLights(t, d, ((t - 0.3f) / 0.6f).coerceIn(0f, 1f), bulbColors)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = greeting,
                color = onSurface.copy(alpha = 0.85f),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.graphicsLayer { alpha = ((progress.value * 2.3f - 0.2f) / 0.6f).coerceIn(0f, 1f) },
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                "PrismTune".forEachIndexed { i, ch ->
                    Text(
                        text = ch.toString(),
                        color = onSurface,
                        style =
                            MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold,
                                shadow = Shadow(color = primary.copy(alpha = 0.8f), offset = Offset.Zero, blurRadius = 24f),
                            ),
                        modifier =
                            Modifier.graphicsLayer {
                                val a = ((progress.value * 2.3f - (0.6f + i * 0.09f)) / 0.25f).coerceIn(0f, 1f)
                                alpha = a
                                translationY = (1f - a) * 24f
                            },
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Let the music glow",
                color = onSurface.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.graphicsLayer { alpha = ((progress.value * 2.3f - 1.6f) / 0.5f).coerceIn(0f, 1f) },
            )
        }
    }
}

''')

# header: icon + text like ArchiveTune
region('@Composable\nfun PrismTitleBanner(', 'val PrismNatureKey', '''@Composable
fun PrismTitleBanner(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Image(
            painter = painterResource(R.drawable.prism_header_icon),
            contentDescription = null,
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "PrismTune",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
        )
    }
}

''')

open(P, 'w').write(s)
os.makedirs('app/src/main/res/drawable-nodpi', exist_ok=True)
shutil.move('prism_header_icon.png', 'app/src/main/res/drawable-nodpi/prism_header_icon.png')
os.remove(__file__)
print('fix9 applied OK')
