package com.yates.dash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Black = Color(0xFF050605)
private val Neon = Color(0xFF39FF14)
private val Muted = Color(0xFF8B9688)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { Surface(color = Black) { DashApp() } } }
    }
}

@Composable
private fun DashApp() {
    var started by remember { mutableStateOf(false) }
    if (started) DashboardPlaceholder() else StartScreen { started = true }
}

@Composable
private fun StartScreen(onStart: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "neon")
    val glow by transition.animateFloat(12f, 25f, infiniteRepeatable(tween(1150), RepeatMode.Reverse), label = "glow")
    Box(Modifier.fillMaxSize().background(Black).padding(horizontal = 28.dp), contentAlignment = Alignment.Center) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("DASH", color = Color.White, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 38.sp, letterSpacing = 7.sp)
            Spacer(Modifier.height(13.dp))
            Text("SYSTEM READY", color = Neon, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 2.5.sp)
            Spacer(Modifier.height(58.dp))
            Box(
                Modifier.fillMaxWidth().height(68.dp)
                    .shadow(glow.dp, RoundedCornerShape(8.dp), ambientColor = Neon, spotColor = Neon)
                    .background(Color(0xFF0B1609), RoundedCornerShape(8.dp))
                    .border(1.5.dp, Neon, RoundedCornerShape(8.dp))
                    .clickable(onClick = onStart),
                contentAlignment = Alignment.Center
            ) {
                Text("START", color = Neon, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 18.sp, letterSpacing = 4.sp)
            }
            Spacer(Modifier.height(22.dp))
            Text("WELCOME TO DASH.\nPRESS START TO CONTINUE.", color = Muted, fontFamily = FontFamily.Monospace, fontSize = 9.sp, lineHeight = 17.sp, letterSpacing = 1.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun DashboardPlaceholder() {
    Box(Modifier.fillMaxSize().background(Black).padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("DASHBOARD", color = Neon, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 22.sp, letterSpacing = 3.sp)
            Spacer(Modifier.height(14.dp))
            Text("PLACEHOLDER", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 11.sp, letterSpacing = 2.sp)
        }
    }
}
