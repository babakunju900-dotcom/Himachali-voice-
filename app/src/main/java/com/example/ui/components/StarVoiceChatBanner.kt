package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * STAR Voice Chat Promotional Banner component
 * Faithfully presents the attached STAR Voice Chat banner with NEXUS CEO branding,
 * gold star microphone logo, welcome typography, feature badges, and scenic lake ambiance.
 */
@Composable
fun StarVoiceChatBanner(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Subtle animated gold shimmer for the outer border
    val infiniteTransition = rememberInfiniteTransition(label = "banner_shimmer")
    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_pos"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF070E22)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                colors = listOf(
                    StarGoldPrimary,
                    StarGoldLight,
                    Color(0xFF0284C7),
                    StarGoldPrimary
                ),
                start = Offset(shimmerTranslate, 0f),
                end = Offset(shimmerTranslate + 400f, 400f)
            ),
            width = 1.8.dp
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("star_voice_chat_banner")
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF030818),
                            Color(0xFF061536),
                            Color(0xFF0B2E58),
                            Color(0xFF0284C7).copy(alpha = 0.85f),
                            Color(0xFF0369A1)
                        )
                    )
                )
        ) {
            val isWideScreen = maxWidth > 500.dp

            // Decorative background lake wave highlights & sun flares
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height

                // Angled gold corner ribbon at top-left
                val ribbonPath = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(w * 0.18f, 0f)
                    lineTo(0f, h * 0.38f)
                    close()
                }
                drawPath(
                    path = ribbonPath,
                    brush = Brush.linearGradient(
                        colors = listOf(StarGoldLight.copy(alpha = 0.7f), StarGoldPrimary.copy(alpha = 0.15f))
                    )
                )

                // Sunny lake wave shimmer on right half
                val lakeWave = Path().apply {
                    moveTo(w * 0.45f, 0f)
                    cubicTo(w * 0.6f, h * 0.3f, w * 0.5f, h * 0.7f, w * 0.55f, h)
                    lineTo(w, h)
                    lineTo(w, 0f)
                    close()
                }
                drawPath(
                    path = lakeWave,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0284C7).copy(alpha = 0.25f),
                            Color(0xFF38BDF8).copy(alpha = 0.45f),
                            Color(0xFF0F766E).copy(alpha = 0.35f)
                        )
                    )
                )

                // Bottom gold flare
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(StarGoldPrimary.copy(alpha = 0.4f), Color.Transparent),
                        center = Offset(w * 0.9f, h * 0.9f),
                        radius = h * 0.6f
                    ),
                    radius = h * 0.6f,
                    center = Offset(w * 0.9f, h * 0.9f)
                )
            }

            // Foreground Content
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column: Brand, CEO badge, Welcome title & Features
                Column(
                    modifier = Modifier
                        .weight(if (isWideScreen) 1.4f else 1.3f)
                        .padding(end = 8.dp)
                ) {
                    // Top Logo & NEXUS Badge Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // 3D Star & Mic Emblem
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(StarGoldLight, StarGoldPrimary, StarGoldDark)
                                    )
                                )
                                .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Star Mic",
                                tint = StarKingBgDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "STAR",
                                    color = StarGoldLight,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "VOICE CHAT",
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp
                                )
                            }
                            Text(
                                text = "CONNECT • TALK • MAKE FRIENDS",
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp,
                                letterSpacing = 0.3.sp
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // CEO NEXUS Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F172A).copy(alpha = 0.9f))
                                .border(1.dp, StarGoldPrimary, RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("👑 ", fontSize = 9.sp)
                                    Text(
                                        text = "NEXUS",
                                        color = StarGoldPrimary,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(StarGoldPrimary)
                                        .padding(horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = "CEO FOR APP",
                                        color = StarKingBgDark,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 7.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // "WELCOME TO STAR VOICE CHAT"
                    Column {
                        Text(
                            text = "WELCOME TO",
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "STAR VOICE CHAT",
                            style = MaterialTheme.typography.titleMedium.copy(
                                brush = Brush.horizontalGradient(
                                    listOf(StarGoldLight, StarGoldPrimary, Color.White)
                                ),
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 4 Feature Pillars (Party, Global, Safe, Fun)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FeatureMiniPill(icon = Icons.Default.Mic, label = "Party")
                        FeatureMiniPill(icon = Icons.Default.Groups, label = "Global")
                        FeatureMiniPill(icon = Icons.Default.Security, label = "Secure")
                        FeatureMiniPill(icon = Icons.Default.Star, label = "More Fun")
                    }
                }

                // Right Column: CEO Portrait, Boat lake atmosphere & Calligraphy quote
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(0.7f)
                ) {
                    // Calligraphy Tag
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A).copy(alpha = 0.7f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("👑", fontSize = 8.sp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Voice Brings\nPeople Together",
                            color = StarGoldLight,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp,
                            lineHeight = 10.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // CEO Avatar Artwork (Sunglasses, Denim Vest, Boat Ambiance)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(76.dp)
                            .shadow(8.dp, CircleShape, spotColor = StarGoldPrimary)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF38BDF8),
                                        Color(0xFF0284C7),
                                        Color(0xFF1E293B)
                                    )
                                )
                            )
                            .border(2.dp, StarGoldPrimary, CircleShape)
                    ) {
                        // Stylized avatar representing the CEO in sunglasses & black vest
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text("🕶️", fontSize = 28.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF0F172A))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "NEXUS",
                                    color = StarGoldLight,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 8.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Explore Opportunity Action Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.horizontalGradient(listOf(StarGoldDark, StarGoldPrimary))
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ENTER NOW",
                                color = StarKingBgDark,
                                fontWeight = FontWeight.Black,
                                fontSize = 9.sp
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = StarKingBgDark,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureMiniPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0F172A).copy(alpha = 0.8f))
            .border(0.8.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
            .padding(horizontal = 5.dp, vertical = 3.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = StarGoldPrimary,
            modifier = Modifier.size(10.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.SemiBold,
            fontSize = 8.5.sp
        )
    }
}
