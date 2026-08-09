package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    // Animation States using Animatable
    val bgAlpha = remember { Animatable(1f) }
    val iconScale = remember { Animatable(0.92f) }
    val iconAlpha = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val titleOffsetY = remember { Animatable(16f) }
    val subtitleAlpha = remember { Animatable(0f) }
    val brandingAlpha = remember { Animatable(0f) }

    // Soft Radial Glow Pulse Loop
    val infiniteTransition = rememberInfiniteTransition(label = "glow_pulse")
    val pulseGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.22f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val pulseGlowScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )

    LaunchedEffect(Unit) {
        // Step 1: Icon scale & fade in smoothly
        launch {
            delay(80)
            iconAlpha.animateTo(1f, animationSpec = tween(450))
        }
        launch {
            delay(80)
            iconScale.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(550, easing = FastOutSlowInEasing)
            )
        }

        // Step 2: App Title fades in
        launch {
            delay(300)
            titleAlpha.animateTo(1f, animationSpec = tween(400))
        }
        launch {
            delay(300)
            titleOffsetY.animateTo(0f, animationSpec = tween(400, easing = FastOutSlowInEasing))
        }

        // Step 3: Subtitle fades in
        launch {
            delay(420)
            subtitleAlpha.animateTo(1f, animationSpec = tween(400))
        }

        // Step 4: Bottom Branding "A Product of DomainoTech" fades in last
        launch {
            delay(580)
            brandingAlpha.animateTo(1f, animationSpec = tween(400))
        }

        // Optimized duration for a brisk, premium launch experience (~1.7s)
        delay(1700)
        
        // GPU-driven smooth fade out into the main UI
        bgAlpha.animateTo(0f, animationSpec = tween(350, easing = FastOutSlowInEasing))
        
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("splash_screen_root")
            .graphicsLayer {
                alpha = bgAlpha.value
            }
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF042B1F), // Rich Emerald Center
                        Color(0xFF021B13), // Deep Emerald Black Mid
                        Color(0xFF010E0A), // Dark Teal Base
                        Color(0xFF000503)  // Pitch Black Edge
                    ),
                    radius = 1800f
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onSplashFinished()
            }
    ) {
        // Faint Abstract Flowing Wave Pattern (5-8% opacity) representing cash flow
        val wavePath1 = remember { Path() }
        val wavePath2 = remember { Path() }
        val wavePath3 = remember { Path() }

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val w = size.width
            val h = size.height

            wavePath1.reset()
            wavePath1.moveTo(-w * 0.2f, h * 0.35f)
            wavePath1.cubicTo(w * 0.25f, h * 0.22f, w * 0.65f, h * 0.48f, w * 1.2f, h * 0.30f)

            wavePath2.reset()
            wavePath2.moveTo(-w * 0.1f, h * 0.65f)
            wavePath2.cubicTo(w * 0.35f, h * 0.78f, w * 0.75f, h * 0.52f, w * 1.15f, h * 0.70f)

            wavePath3.reset()
            wavePath3.moveTo(-w * 0.15f, h * 0.50f)
            wavePath3.cubicTo(w * 0.30f, h * 0.40f, w * 0.70f, h * 0.60f, w * 1.25f, h * 0.45f)

            // Draw abstract cash flow waves with 5-8% opacity
            drawPath(
                path = wavePath1,
                color = Color(0xFF00E676).copy(alpha = 0.07f),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
            drawPath(
                path = wavePath2,
                color = Color(0xFF10B981).copy(alpha = 0.06f),
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )
            drawPath(
                path = wavePath3,
                color = Color(0xFF34D399).copy(alpha = 0.05f),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )

            // Subtle Vignette Overlay along screen edges
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.55f)
                    ),
                    center = Offset(w / 2f, h / 2f),
                    radius = maxOf(w, h) * 0.72f
                )
            )
        }

        // Safe Area Compliant Wrapper
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Soft Radial Emerald Glow behind Central Icon (GPU Layered)
            Canvas(
                modifier = Modifier
                    .size(340.dp)
                    .align(Alignment.Center)
                    .graphicsLayer {
                        scaleX = pulseGlowScale
                        scaleY = pulseGlowScale
                        alpha = iconAlpha.value * pulseGlowAlpha
                    }
            ) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00E676),
                            Color(0xFF10B981).copy(alpha = 0.50f),
                            Color(0xFF047857).copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
            }

            // Center Content: App Icon + CashFlow + Subtitle
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Translucent Glass Panel Container
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = iconScale.value
                            scaleY = iconScale.value
                            alpha = iconAlpha.value
                        }
                        .size(156.dp)
                        .shadow(
                            elevation = 28.dp,
                            shape = CircleShape,
                            ambientColor = Color(0xFF00E676).copy(alpha = 0.35f),
                            spotColor = Color(0xFF10B981).copy(alpha = 0.65f)
                        )
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF063B28).copy(alpha = 0.88f),
                                    Color(0xFF021E14).copy(alpha = 0.96f)
                                )
                            )
                        )
                        .border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF00E676).copy(alpha = 0.65f),
                                    Color(0xFF10B981).copy(alpha = 0.25f),
                                    Color(0xFF34D399).copy(alpha = 0.45f)
                                )
                            ),
                            shape = CircleShape
                        )
                        .testTag("cashflow_app_icon"),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_cashflow_logo),
                        contentDescription = "CashFlow App Icon",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(112.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // App Title: CashFlow
                Text(
                    text = "CashFlow",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.6.sp,
                    modifier = Modifier
                        .graphicsLayer {
                            alpha = titleAlpha.value
                            translationY = titleOffsetY.value
                        }
                        .testTag("cashflow_app_name")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle: Smart Personal Finance Manager
                Text(
                    text = "Smart Personal Finance Manager",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF9CA3AF),
                    letterSpacing = 0.5.sp,
                    modifier = Modifier
                        .graphicsLayer {
                            alpha = subtitleAlpha.value
                        }
                        .testTag("cashflow_subtitle")
                )
            }

            // Bottom Branding Text: A Product of DomainoTech
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp)
                    .graphicsLayer {
                        alpha = brandingAlpha.value
                    }
                    .testTag("bottom_branding_section"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "A Product of DomainoTech",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.70f),
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.testTag("domainotech_brand_text")
                )
            }
        }
    }
}


