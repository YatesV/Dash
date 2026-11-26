package com.yates.dash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.input.pointer.consume
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Void = Color(0xFF030506)
private val Panel = Color(0xD9070C0A)
private val Panel2 = Color(0xFF0A1715)
private val Neon = Color(0xFF00FF72)
private val Mint = Color(0xFF9DFFCD)
private val Dim = Color(0xFF718C82)
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
    var started by remember { mutableStateOf(false) }
    var active by remember { mutableStateOf("HOME") }
    if (!started) StartScreen { started = true }
    else MainDashboard(active = active, onNavigate = { active = it })
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
        for (i in 0..34) {
            val depth = 0.25f + (i % 5) * 0.16f
            val sx = ((i * 79f + drift * (if (i % 2 == 0) 1 else -1) * depth + parallaxX * depth * 2f) % this.size.width + this.size.width) % this.size.width
            val sy = ((i * 137f + parallaxY * depth * 2f) % this.size.height)
            val a = if (i % 3 == 0) 0.72f else 0.28f
            drawRect(Neon.copy(alpha = a), Offset(sx, sy), Size(if (i % 4 == 0) 3.5f else 2f, if (i % 4 == 0) 3.5f else 2f))
        }
        // Sweeping scan beam and soft ambient horizon glow.
        val beamY = size.height * scan
        drawRect(Brush.verticalGradient(listOf(Color.Transparent, Neon.copy(alpha = 0.055f), Color.Transparent), startY = beamY - 35f, endY = beamY + 35f), Offset(0f, beamY - 35f), Size(size.width, 70f))
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
private fun MainDashboard(active: String, onNavigate: (String) -> Unit) {
    var dragX by remember { mutableFloatStateOf(0f) }
    var dragY by remember { mutableFloatStateOf(0f) }
    val px by animateFloatAsState(dragX, spring(dampingRatio = 0.72f, stiffness = 110f), label = "parallaxX")
    val py by animateFloatAsState(dragY, spring(dampingRatio = 0.72f, stiffness = 110f), label = "parallaxY")
    Box(
        Modifier.fillMaxSize().background(Void)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = { dragX = 0f; dragY = 0f },
                    onDragCancel = { dragX = 0f; dragY = 0f }
                ) { change, amount ->
                    change.consume()
                    dragX = (dragX + amount.x * 0.045f).coerceIn(-18f, 18f)
                    dragY = (dragY + amount.y * 0.045f).coerceIn(-18f, 18f)
                }
            }
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
                    "FOCUS" -> SimplePage("FOCUS", "PLAN YOUR DAY", listOf("INBOX", "TODAY'S PRIORITIES", "POMODORO TIMER", "UPCOMING TASKS"))
                    "TRACK" -> SimplePage("TRACKER", "BUILD YOUR ROUTINES", listOf("HABITS", "WORKOUTS", "SLEEP", "PERSONAL GOALS"))
                    "VAULT" -> SimplePage("VAULT", "YOUR PERSONAL LIBRARY", listOf("NOTES", "PROJECTS", "IDEAS", "FILES & LINKS"))
                    "MONEY" -> SimplePage("MONEY", "KEEP AN EYE ON YOUR FINANCES", listOf("BALANCE OVERVIEW", "BUDGET", "SAVINGS GOALS", "TRANSACTIONS"))
                    "SETTINGS" -> SimplePage("SETTINGS", "MAKE DASH YOURS", listOf("APPEARANCE", "WIDGETS", "NOTIFICATIONS", "DATA & BACKUP"))
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
        .graphicsLayer { translationY = floatY; translationZ = 2f }
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
        Text("MODULE PREVIEW  //  MORE FEATURES COMING", color = Dim, fontFamily = FontFamily.Monospace, fontSize = 8.sp)
    }
}
