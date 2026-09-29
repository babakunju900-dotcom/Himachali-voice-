package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun AvatarView(
    avatarUrl: String,
    nickname: String,
    size: Dp = 48.dp,
    frameName: String = "",
    vipTier: Int = 0,
    level: Int = 1,
    isOfficial: Boolean = false,
    isSpeaking: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Pulse animation when user is speaking
    val infiniteTransition = rememberInfiniteTransition(label = "speaking_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isSpeaking) 1.15f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val frameBorder = when {
        frameName.contains("Crown", ignoreCase = true) -> Brush.linearGradient(listOf(StarGoldLight, StarGoldPrimary, StarGoldDark))
        frameName.contains("Galaxy", ignoreCase = true) -> Brush.linearGradient(listOf(NeonCyan, BrightMagenta, RoyalPurple))
        frameName.contains("Cyber", ignoreCase = true) -> Brush.linearGradient(listOf(NeonCyan, LiveGreen, RoyalPurple))
        frameName.contains("Gold", ignoreCase = true) -> Brush.linearGradient(listOf(StarGoldPrimary, StarGoldSecondary))
        vipTier >= 3 -> Brush.linearGradient(listOf(StarGoldPrimary, StarGoldLight))
        vipTier >= 1 -> Brush.linearGradient(listOf(RoyalPurple, NeonPurple))
        else -> Brush.linearGradient(listOf(StarKingCardBorder, StarKingCardBorder))
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size + 14.dp)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .testTag("avatar_${nickname.take(6)}")
    ) {
        // Outer speaking glow ring
        if (isSpeaking) {
            Box(
                modifier = Modifier
                    .size(size + 12.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                StarGoldPrimary.copy(alpha = 0.8f),
                                NeonCyan.copy(alpha = 0.5f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // Frame wrapper
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size + 6.dp)
                .clip(CircleShape)
                .background(frameBorder)
                .padding(2.dp)
        ) {
            // Inner Avatar
            val initials = if (nickname.isNotBlank()) nickname.take(2).uppercase() else "SK"
            val avatarBg = when {
                avatarUrl.contains("crown") -> Brush.linearGradient(listOf(StarGoldDark, StarGoldPrimary))
                avatarUrl.contains("aria") -> Brush.linearGradient(listOf(RoyalPurple, BrightMagenta))
                avatarUrl.contains("viktor") -> Brush.linearGradient(listOf(Color(0xFFE65100), Color(0xFFFF9800)))
                avatarUrl.contains("layla") -> Brush.linearGradient(listOf(Color(0xFF4A148C), Color(0xFF880E4F)))
                avatarUrl.contains("support") -> Brush.linearGradient(listOf(Color(0xFF00695C), NeonCyan))
                else -> Brush.linearGradient(listOf(Color(0xFF283593), RoyalPurple))
            }

            val isCustomImage = avatarUrl.startsWith("content://") || avatarUrl.startsWith("file://") || avatarUrl.startsWith("http")
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(avatarBg)
            ) {
                if (isCustomImage) {
                    coil.compose.AsyncImage(
                        model = avatarUrl,
                        contentDescription = nickname,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = initials,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = (size.value * 0.38f).sp
                    )
                }
            }
        }

        // VIP / Level Tag at bottom
        if (level > 0 && size >= 40.dp) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 4.dp)
                    .background(
                        if (vipTier > 0) Brush.horizontalGradient(listOf(StarGoldDark, StarGoldPrimary))
                        else Brush.horizontalGradient(listOf(RoyalPurple, NeonPurple)),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = if (vipTier > 0) "V$vipTier Lv$level" else "Lv.$level",
                    color = if (vipTier > 0) StarKingBgDark else TextWhite,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 8.sp
                )
            }
        }

        // Official verification checkmark
        if (isOfficial) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(StarGoldPrimary)
                    .border(1.dp, StarKingBgDark, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Official Verified",
                    tint = StarKingBgDark,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
