package com.yates.dash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class DashPalette(
    val name: String, val background: Color, val panel: Color, val panel2: Color,
    val neon: Color, val mint: Color, val dim: Color,
    val particles: Boolean = true, val scanlines: Boolean = true, val parallax: Boolean = true, val glow: Float = 1f
)
private val DefaultPalette = DashPalette("CYBER GREEN", Color(0xFF030506), Color(0xD9070C0A), Color(0xFF0A1715), Color(0xFF00FF72), Color(0xFF9DFFCD), Color(0xFF718C82))
private val LocalPalette = androidx.compose.runtime.staticCompositionLocalOf { DefaultPalette }
private val Void: Color @Composable get() = LocalPalette.current.background
private val Panel: Color @Composable get() = LocalPalette.current.panel
private val Panel2: Color @Composable get() = LocalPalette.current.panel2
private val Neon: Color @Composable get() = LocalPalette.current.neon
private val Mint: Color @Composable get() = LocalPalette.current.mint
private val Dim: Color @Composable get() = LocalPalette.current.dim
private val Pixel = FontFamily(Font(R.font.pixelite))

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { Surface(color = Void) { DashApp() } } }
    }
}

private data class Module(val title: String, val subtitle: String, val symbol: String, val tab: String)

@Composable
private fun DashApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("dash_appearance", android.content.Context.MODE_PRIVATE) }
    var started by remember { mutableStateOf(false) }
    var active by remember { mutableStateOf("HOME") }
    var palette by remember {
        mutableStateOf(DefaultPalette.copy(
            name = prefs.getString("theme_name", DefaultPalette.name) ?: DefaultPalette.name,
            background = Color(prefs.getInt("background", DefaultPalette.background.value.toLong().toInt())),
            panel = Color(prefs.getInt("panel", DefaultPalette.panel.value.toLong().toInt())),
            panel2 = Color(prefs.getInt("panel2", DefaultPalette.panel2.value.toLong().toInt())),
            neon = Color(prefs.getInt("neon", DefaultPalette.neon.value.toLong().toInt())),
            mint = Color(prefs.getInt("mint", DefaultPalette.mint.value.toLong().toInt())),
            dim = Color(prefs.getInt("dim", DefaultPalette.dim.value.toLong().toInt())),
            particles = prefs.getBoolean("particles", true), scanlines = prefs.getBoolean("scanlines", true),
            parallax = prefs.getBoolean("parallax", true), glow = prefs.getFloat("glow", 1f)
        ))
    }
    fun updatePalette(next: DashPalette) {
        palette = next
        prefs.edit().putString("theme_name", next.name)
            .putInt("background", next.background.value.toLong().toInt())
            .putInt("panel", next.panel.value.toLong().toInt()).putInt("panel2", next.panel2.value.toLong().toInt())
            .putInt("neon", next.neon.value.toLong().toInt()).putInt("mint", next.mint.value.toLong().toInt())
            .putInt("dim", next.dim.value.toLong().toInt()).putBoolean("particles", next.particles)
            .putBoolean("scanlines", next.scanlines).putBoolean("parallax", next.parallax).putFloat("glow", next.glow).apply()
    }
    BackHandler(enabled = started || active != "HOME") {
        if (active != "HOME") active = "HOME" else started = false
    }
    androidx.compose.runtime.CompositionLocalProvider(LocalPalette provides palette) {
        if (!started) StartScreen { started = true }
        else MainDashboard(active = active, onNavigate = { active = it }, onPaletteChange = ::updatePalette)
    }
}

@Composable
private fun PixelText(value: String, color: Color = Neon, size: Int = 10, modifier: Modifier = Modifier, align: TextAlign = TextAlign.Start) {
    Text(value, modifier = modifier, color = color, fontFamily = Pixel, fontSize = size.sp, lineHeight = (size * 1.8f).sp, textAlign = align)
}

@Composable
private fun Backdrop(modifier: Modifier = Modifier, parallaxX: Float = 0f, parallaxY: Float = 0f) {
    val transition = rememberInfiniteTransition(label = "ambient")
    val drift by transition.animateFloat(-28f, 28f, infiniteRepeatable(tween(6500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "drift")
    val scan by transition.animateFloat(-80f, 1.2f, infiniteRepeatable(tween(5200, easing = LinearEasing), RepeatMode.Restart), label = "scan")
    val breathe by transition.animateFloat(0.10f, 0.24f, infiniteRepeatable(tween(2600), RepeatMode.Reverse), label = "breathe")
    val palette = LocalPalette.current
    Canvas(modifier.fillMaxSize()) {
        val step = 34f
        var x = 0f
        while (x < this.size.width) {
            var y = 0f
            while (y < this.size.height) {
                drawRect(Color(0xFF102010).copy(alpha = 0.18f), Offset(x, y), Size(1f, 1f))
                y += step
            }
            x += step
        }
        // Deep parallax layer: slow drifting pixel stars.
        if (palette.particles) for (i in 0..58) {
            val depth = 0.25f + (i % 5) * 0.16f
            val sx = ((i * 79f + drift * (if (i % 2 == 0) 1 else -1) * depth + parallaxX * depth * 2f) % this.size.width + this.size.width) % this.size.width
            val sy = ((i * 137f + parallaxY * depth * 2f) % this.size.height)
            val a = if (i % 3 == 0) 0.72f else 0.28f
            drawRect(Neon.copy(alpha = a), Offset(sx, sy), Size(if (i % 4 == 0) 3.5f else 2f, if (i % 4 == 0) 3.5f else 2f))
        }
        // Sweeping scan beam and soft ambient horizon glow.
        val beamY = size.height * scan
        if (palette.scanlines) drawRect(Brush.verticalGradient(listOf(Color.Transparent, Neon.copy(alpha = 0.10f), Color.Transparent), startY = beamY - 48f, endY = beamY + 48f), Offset(0f, beamY - 48f), Size(size.width, 96f))
        drawRect(Neon.copy(alpha = breathe), Offset(0f, size.height * 0.72f + parallaxY), Size(size.width, 1f))
    }
}

@Composable
private fun StartScreen(onStart: () -> Unit) {
    val t = rememberInfiniteTransition(label = "startGlow")
    val pulse by t.animateFloat(8f, 34f, infiniteRepeatable(tween(1050, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pulse")
    val shimmer by t.animateFloat(0f, 1f, infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart), label = "shimmer")
    Box(Modifier.fillMaxSize().background(Void)) {
        Backdrop()
        Column(Modifier.align(Alignment.Center).fillMaxWidth().padding(horizontal = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            PixelText("DASH", Color.White, 30)
            Spacer(Modifier.height(22.dp))
            PixelText("YOUR LIFE. IN ONE PLACE.", Mint, 7, align = TextAlign.Center)
            Spacer(Modifier.height(66.dp))
            Box(
                Modifier.fillMaxWidth().height(76.dp)
                    .shadow(pulse.dp, RoundedCornerShape(3.dp), ambientColor = Neon, spotColor = Neon)
                    .background(Brush.horizontalGradient(listOf(Color(0xFF071507), Color(0xFF153414), Color(0xFF071507))), RoundedCornerShape(3.dp))
                    .border(2.dp, Neon.copy(alpha = 0.75f + shimmer * 0.25f), RoundedCornerShape(3.dp))
                    .clickable(onClick = onStart),
                contentAlignment = Alignment.Center
            ) { PixelText("START", Color.White, 14) }
            Spacer(Modifier.height(24.dp))
            PixelText("PRESS START TO ENTER", Dim, 6, align = TextAlign.Center)
        }
        PixelText("SYS.V 01.0", Dim, 5, Modifier.align(Alignment.BottomCenter).padding(bottom = 30.dp))
    }
}

@Composable
private fun MainDashboard(active: String, onNavigate: (String) -> Unit, onPaletteChange: (DashPalette) -> Unit) {
    val palette = LocalPalette.current
    var dragX by remember { mutableFloatStateOf(0f) }
    var dragY by remember { mutableFloatStateOf(0f) }
    val px by animateFloatAsState(dragX, spring(dampingRatio = 0.72f, stiffness = 110f), label = "parallaxX")
    val py by animateFloatAsState(dragY, spring(dampingRatio = 0.72f, stiffness = 110f), label = "parallaxY")
    Box(
        Modifier.fillMaxSize().background(Void)
            .then(if (palette.parallax) Modifier.pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = { dragX = 0f; dragY = 0f },
                    onDragCancel = { dragX = 0f; dragY = 0f }
                ) { change, amount ->
                    dragX = (dragX + amount.x * 0.045f).coerceIn(-18f, 18f)
                    dragY = (dragY + amount.y * 0.045f).coerceIn(-18f, 18f)
                }
            } else Modifier)
    ) {
        Backdrop(Modifier.fillMaxSize(), px, py)
        Column(Modifier.fillMaxSize().graphicsLayer { translationX = -px * 0.22f; translationY = -py * 0.22f }) {
            Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    PixelText("DASH", Color.White, 15)
                    Spacer(Modifier.height(7.dp))
                    Text("PERSONAL COMMAND CENTER", color = Dim, fontFamily = FontFamily.Monospace, fontSize = 9.sp, letterSpacing = 1.5.sp)
                }
                Box(Modifier.size(38.dp).border(1.dp, Neon.copy(alpha = 0.7f), RoundedCornerShape(3.dp)).background(Panel), contentAlignment = Alignment.Center) {
                    PixelText("Y", Neon, 12)
                }
            }
            Box(Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = active,
                    transitionSpec = {
                        (fadeIn(tween(260)) + scaleIn(initialScale = 0.975f, animationSpec = tween(320))) togetherWith
                            (fadeOut(tween(170)) + scaleOut(targetScale = 1.025f, animationSpec = tween(170)))
                    },
                    label = "dashboardPageTransition"
                ) { page ->
                when (page) {
                    "HOME" -> HomePage(onNavigate)
                    "FOCUS" -> WorkspacePage("FOCUS", "YOUR TASK LIST", "dash_tasks", true, "ADD TASK")
                    "TRACK" -> WorkspacePage("TRACKER", "DAILY HABITS", "dash_habits", true, "ADD HABIT")
                    "VAULT" -> WorkspacePage("VAULT", "QUICK NOTES", "dash_notes", false, "SAVE NOTE")
                    "MONEY" -> SimplePage("MONEY", "KEEP AN EYE ON YOUR FINANCES", listOf("BALANCE OVERVIEW", "BUDGET", "SAVINGS GOALS", "TRANSACTIONS"))
                    "SETTINGS" -> SettingsPage(palette, onPaletteChange)
                    else -> SimplePage(page, "DASH MODULE", listOf("COMING SOON", "THIS SPACE IS READY FOR YOUR TOOLS"))
                }
                }
            }
            Row(Modifier.fillMaxWidth().background(Color(0xFF050905)).border(width = 1.dp, color = Color(0xFF193019)).padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                NavItem("HOME", "⌂", active, onNavigate)
                NavItem("FOCUS", "▣", active, onNavigate)
                NavItem("TRACK", "✳", active, onNavigate)
                NavItem("VAULT", "▤", active, onNavigate)
            }
        }
    }
}

@Composable
private fun NavItem(tab: String, glyph: String, active: String, onNavigate: (String) -> Unit) {
    val selected = active == tab
    Column(Modifier.clickable { onNavigate(tab) }.padding(horizontal = 9.dp, vertical = 3.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(glyph, color = if (selected) Neon else Dim, fontSize = 20.sp)
        Spacer(Modifier.height(3.dp))
        Text(tab, color = if (selected) Neon else Dim, fontFamily = Pixel, fontSize = 6.sp)
    }
}

@Composable
private fun HomePage(onNavigate: (String) -> Unit) {
    val modules = listOf(
        Module("FOCUS", "Tasks & daily plan", "◈", "FOCUS"),
        Module("HABITS", "Routines & streaks", "✳", "TRACK"),
        Module("FITNESS", "Training & progress", "▰", "TRACK"),
        Module("MONEY", "Budget & savings", "$", "MONEY"),
        Module("NOTES", "Thoughts & ideas", "▤", "VAULT"),
        Module("PROJECTS", "Things you're building", "⌘", "VAULT"),
        Module("LIBRARY", "Files, links & resources", "▧", "VAULT"),
        Module("SETTINGS", "Customize your space", "⚙", "SETTINGS")
    )
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)) {
        Spacer(Modifier.height(8.dp))
        NeonPanel(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    Text("THURSDAY // YOUR OVERVIEW", color = Dim, fontFamily = FontFamily.Monospace, fontSize = 9.sp, letterSpacing = 1.sp)
                    Spacer(Modifier.height(12.dp))
                    PixelText("LEVEL UP", Color.White, 12)
                    Spacer(Modifier.height(8.dp))
                    Text("One place to move your life forward.", color = Mint, fontSize = 12.sp)
                }
                PixelText("01", Neon, 16)
            }
            Spacer(Modifier.height(18.dp))
            Box(Modifier.fillMaxWidth().height(5.dp).background(Color(0xFF1A2B19))) {
                Box(Modifier.fillMaxWidth(0.38f).fillMaxHeight().background(Neon))
            }
            Spacer(Modifier.height(7.dp))
            Text("DAILY SYSTEMS // READY TO BUILD", color = Dim, fontFamily = FontFamily.Monospace, fontSize = 8.sp)
        }
        Spacer(Modifier.height(18.dp))
        SectionTitle("QUICK ACCESS", "08 MODULES")
        Spacer(Modifier.height(10.dp))
        modules.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                pair.forEach { module ->
                    ModuleCard(module, Modifier.weight(1f)) { onNavigate(module.tab) }
                }
            }
            Spacer(Modifier.height(10.dp))
        }
        Spacer(Modifier.height(8.dp))
        SectionTitle("TODAY", "YOUR DAILY HUB")
        Spacer(Modifier.height(10.dp))
        NeonPanel(Modifier.fillMaxWidth()) {
            PixelText("DAILY CHECK-IN", Mint, 8)
            Spacer(Modifier.height(12.dp))
            Text("Your dashboard is ready. Add tasks, habits, goals and notes as we build out each module.", color = Color(0xFFB6C5B1), fontSize = 12.sp, lineHeight = 18.sp)
            Spacer(Modifier.height(14.dp))
            Text("NEXT UP  →  SET YOUR FIRST GOAL", color = Neon, fontFamily = FontFamily.Monospace, fontSize = 9.sp, modifier = Modifier.clickable { onNavigate("FOCUS") })
        }
        Spacer(Modifier.height(22.dp))
    }
}

@Composable
private fun SectionTitle(title: String, detail: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        PixelText(title, Neon, 8)
        Text(detail, color = Dim, fontFamily = FontFamily.Monospace, fontSize = 8.sp)
    }
}

@Composable
private fun NeonPanel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.shadow(13.dp, RoundedCornerShape(4.dp), ambientColor = Neon.copy(alpha = 0.13f), spotColor = Neon.copy(alpha = 0.12f)).background(Panel, RoundedCornerShape(4.dp)).border(1.dp, Color(0xFF244424), RoundedCornerShape(4.dp)).padding(14.dp), content = content)
}

@Composable
private fun ModuleCard(module: Module, modifier: Modifier = Modifier, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val t = rememberInfiniteTransition(label = "cardGlow")
    val alpha by t.animateFloat(0.22f, 0.78f, infiniteRepeatable(tween(1450), RepeatMode.Reverse), label = "alpha")
    val floatY by t.animateFloat(0f, -4f, infiniteRepeatable(tween(2100 + module.title.length * 85, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "float")
    Column(modifier.height(106.dp)
        .graphicsLayer { translationY = floatY }
        .shadow(12.dp, RoundedCornerShape(3.dp), ambientColor = Neon.copy(alpha = alpha * 0.34f), spotColor = Neon.copy(alpha = alpha * 0.28f))
        .background(Brush.linearGradient(listOf(Color(0xFF112311), Panel2, Color(0xFF071007))), RoundedCornerShape(3.dp))
        .border(1.dp, Neon.copy(alpha = if (pressed) 1f else alpha), RoundedCornerShape(3.dp))
        .clickable { pressed = !pressed; onClick() }.padding(11.dp), verticalArrangement = Arrangement.SpaceBetween) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(module.symbol, color = Neon, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("↗", color = Dim, fontSize = 12.sp)
        }
        Column {
            PixelText(module.title, Color.White, 7)
            Spacer(Modifier.height(5.dp))
            Text(module.subtitle, color = Dim, fontSize = 9.sp, maxLines = 1)
        }
    }
}

@Composable
private fun WorkspacePage(title: String, subtitle: String, storageKey: String, checkable: Boolean, addLabel: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("dash_workspace", android.content.Context.MODE_PRIVATE) }
    val entries = remember(storageKey) {
        androidx.compose.runtime.mutableStateListOf<String>().apply {
            addAll(prefs.getStringSet(storageKey, emptySet()).orEmpty().sorted())
        }
    }
    var draft by remember { mutableStateOf("") }
    var showAdd by remember { mutableStateOf(false) }
    fun save() { prefs.edit().putStringSet(storageKey, entries.toSet()).apply() }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        PixelText(title, Color.White, 15)
        Spacer(Modifier.height(10.dp))
        Text(subtitle, color = Neon, fontFamily = FontFamily.Monospace, fontSize = 10.sp, letterSpacing = 1.sp)
        Spacer(Modifier.height(22.dp))
        NeonPanel(Modifier.fillMaxWidth()) {
            val done = entries.count { it.startsWith("1::") }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                PixelText(if (checkable) "PROGRESS" else "SAVED ITEMS", Mint, 7)
                PixelText("${entries.size} ITEMS", Neon, 7)
            }
            if (checkable) {
                Spacer(Modifier.height(10.dp))
                Box(Modifier.fillMaxWidth().height(5.dp).background(Color(0xFF1A2B19))) {
                    Box(Modifier.fillMaxWidth(if (entries.isEmpty()) 0f else done.toFloat() / entries.size).fillMaxHeight().background(Neon))
                }
                Spacer(Modifier.height(7.dp))
                Text("$done OF ${entries.size} COMPLETED", color = Dim, fontFamily = FontFamily.Monospace, fontSize = 8.sp)
            } else {
                Spacer(Modifier.height(8.dp))
                Text("Your notes are saved on this device.", color = Dim, fontSize = 11.sp)
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = { showAdd = true }, modifier = Modifier.fillMaxWidth()) {
            PixelText(addLabel, Color.Black, 8)
        }
        Spacer(Modifier.height(16.dp))
        if (entries.isEmpty()) {
            NeonPanel(Modifier.fillMaxWidth()) {
                PixelText("NOTHING HERE YET", Mint, 8)
                Spacer(Modifier.height(8.dp))
                Text("Tap $addLabel to create your first item.", color = Dim, fontSize = 11.sp)
            }
        } else {
            entries.toList().forEach { entry ->
                val complete = entry.startsWith("1::")
                val label = if (checkable) entry.substringAfter("::") else entry
                NeonPanel(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (checkable) {
                            Checkbox(checked = complete, onCheckedChange = { checked ->
                                val position = entries.indexOf(entry)
                                if (position >= 0) entries[position] = (if (checked) "1::" else "0::") + label
                                save()
                            })
                        }
                        Text(label, color = if (complete) Dim else Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        TextButton(onClick = { entries.remove(entry); save() }) {
                            Text("×", color = Neon, fontSize = 18.sp)
                        }
                    }
                }
                Spacer(Modifier.height(9.dp))
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("LOCAL DATA // STORED ON THIS DEVICE", color = Dim, fontFamily = FontFamily.Monospace, fontSize = 8.sp)
    }
    if (showAdd) {
        AlertDialog(
            onDismissRequest = { showAdd = false; draft = "" },
            title = { PixelText(addLabel, Neon, 9) },
            text = {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    placeholder = { Text(if (checkable) "What do you want to do?" else "Write a quick note...") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val value = draft.trim()
                    if (value.isNotEmpty()) {
                        entries.add(if (checkable) "0::" + value else value)
                        save()
                    }
                    draft = ""
                    showAdd = false
                }) { Text("SAVE", color = Neon) }
            },
            dismissButton = { TextButton(onClick = { showAdd = false; draft = "" }) { Text("CANCEL") } }
        )
    }
}

@Composable
private fun SimplePage(title: String, subtitle: String, items: List<String>) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        PixelText(title, Color.White, 15)
        Spacer(Modifier.height(10.dp))
        Text(subtitle, color = Neon, fontFamily = FontFamily.Monospace, fontSize = 10.sp, letterSpacing = 1.sp)
        Spacer(Modifier.height(24.dp))
        items.forEachIndexed { index, item ->
            NeonPanel(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PixelText("0${index + 1}", Neon, 8)
                    Spacer(Modifier.width(15.dp))
                    Text(item, color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Text("＋", color = Neon, fontSize = 18.sp)
                }
            }
            Spacer(Modifier.height(11.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text("MODULE PREVIEW // MORE FEATURES COMING", color = Dim, fontFamily = FontFamily.Monospace, fontSize = 8.sp)
    }
}
 
@Composable
private fun SettingsPage(palette: DashPalette, onChange: (DashPalette) -> Unit) {
    val presets = listOf(
        DefaultPalette,
        DashPalette("ICE BLUE", Color(0xFF030611), Color(0xD9071020), Color(0xFF0A1728), Color(0xFF35C8FF), Color(0xFFB4ECFF), Color(0xFF718AA5)),
        DashPalette("VIOLET", Color(0xFF08030D), Color(0xD910071B), Color(0xFF190B29), Color(0xFFBD65FF), Color(0xFFE6C4FF), Color(0xFF9B82AD)),
        DashPalette("AMBER", Color(0xFF090603), Color(0xD9140D05), Color(0xFF211507), Color(0xFFFFB52E), Color(0xFFFFE0A0), Color(0xFFAA936D)),
        DashPalette("CRIMSON", Color(0xFF090305), Color(0xD916070A), Color(0xFF230B12), Color(0xFFFF426D), Color(0xFFFFB4C5), Color(0xFFAA7886))
    )
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp)) {
        PixelText("SETTINGS", Color.White, 15)
        Spacer(Modifier.height(8.dp))
        Text("TUNE EVERY PART OF YOUR DASH EXPERIENCE", color = Neon, fontFamily = FontFamily.Monospace, fontSize = 9.sp)
        Spacer(Modifier.height(20.dp))
        NeonPanel(Modifier.fillMaxWidth()) {
            PixelText("PRESET THEMES", Mint, 8)
            Spacer(Modifier.height(12.dp))
            presets.forEach { preset ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onChange(preset.copy(particles = palette.particles, scanlines = palette.scanlines, parallax = palette.parallax, glow = palette.glow)) }, verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(24.dp).background(preset.neon, RoundedCornerShape(4.dp)).border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(4.dp)))
                    Spacer(Modifier.width(12.dp))
                    Text(preset.name, color = if (palette.name == preset.name) Neon else Color.White, fontFamily = FontFamily.Monospace, fontSize = 11.sp, modifier = Modifier.weight(1f))
                    if (palette.name == preset.name) Text("✓ ACTIVE", color = Neon, fontSize = 8.sp)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        NeonPanel(Modifier.fillMaxWidth()) {
            PixelText("LIVE EFFECTS", Mint, 8)
            Spacer(Modifier.height(12.dp))
            SettingSwitch("PARTICLE FIELD", "Floating pixel stars", palette.particles) { onChange(palette.copy(particles = it)) }
            SettingSwitch("SCAN BEAM", "Animated light sweep", palette.scanlines) { onChange(palette.copy(scanlines = it)) }
            SettingSwitch("TOUCH PARALLAX", "Depth movement while dragging", palette.parallax) { onChange(palette.copy(parallax = it)) }
            Spacer(Modifier.height(8.dp))
            Text("GLOW INTENSITY  \${(palette.glow * 100).toInt()}%", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 9.sp)
            Slider(value = palette.glow, onValueChange = { onChange(palette.copy(glow = it)) }, valueRange = 0f..2f)
        }
        Spacer(Modifier.height(12.dp))
        NeonPanel(Modifier.fillMaxWidth()) {
            PixelText("CUSTOM ACCENT", Mint, 8)
            Spacer(Modifier.height(10.dp))
            Text("Choose a custom glow color without changing your background preset.", color = Dim, fontSize = 10.sp)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(Color(0xFF00FF72), Color(0xFF35C8FF), Color(0xFFBD65FF), Color(0xFFFFB52E), Color(0xFFFF426D), Color.White).forEach { swatch ->
                    Box(Modifier.size(34.dp).background(swatch, RoundedCornerShape(4.dp)).border(if (palette.neon == swatch) 2.dp else 1.dp, Color.White.copy(alpha = 0.75f), RoundedCornerShape(4.dp)).clickable { onChange(palette.copy(name = "CUSTOM", neon = swatch, mint = swatch.copy(alpha = 0.72f))) })
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        NeonPanel(Modifier.fillMaxWidth()) {
            PixelText("DISPLAY", Mint, 8)
            Spacer(Modifier.height(8.dp))
            Text("The selected appearance is saved automatically on this device.", color = Dim, fontSize = 10.sp)
            Text("DASH // PERSONAL COMMAND CENTER", color = Neon, fontFamily = FontFamily.Monospace, fontSize = 8.sp)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SettingSwitch(title: String, description: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 9.sp)
            Text(description, color = Dim, fontSize = 9.sp)
        }
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}
