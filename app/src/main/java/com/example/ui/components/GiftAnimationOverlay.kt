package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.R
import com.example.model.GiftTransactionEntity
import com.example.ui.theme.*

/**
 * Lottie-powered Virtual Gift Celebration Animation Overlay for Voice Rooms
 */
@Composable
fun GiftAnimationOverlay(
    giftTx: GiftTransactionEntity?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = giftTx != null,
        enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(animationSpec = tween(300)),
        exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(animationSpec = tween(400)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .testTag("lottie_gift_animation_overlay")
    ) {
        if (giftTx == null) return@AnimatedVisibility

        // Determine specific Lottie raw resource based on gift type
        val lottieRawRes = when {
            giftTx.giftName.contains("Rocket", ignoreCase = true) || giftTx.giftIcon == "🚀" ->
                R.raw.lottie_rocket_blast

            giftTx.giftName.contains("Heart", ignoreCase = true) ||
            giftTx.giftName.contains("Rose", ignoreCase = true) ||
            giftTx.giftIcon in listOf("🌹", "💖", "💍", "💐") ->
                R.raw.lottie_heart_explosion

            giftTx.giftName.contains("Crown", ignoreCase = true) ||
            giftTx.giftName.contains("Castle", ignoreCase = true) ||
            giftTx.giftName.contains("Yacht", ignoreCase = true) ||
            giftTx.giftIcon in listOf("👑", "🏰", "🏎️", "🛥️", "💎") ->
                R.raw.lottie_crown_sparkle

            else -> R.raw.lottie_gift_fireworks
        }

        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieRawRes))
        val lottieProgress by animateLottieCompositionAsState(
            composition = composition,
            iterations = 1,
            speed = 1.0f
        )

        // Continuous pulse for gift emblem
        val infiniteTransition = rememberInfiniteTransition(label = "gift_pulse")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.28f,
            animationSpec = infiniteRepeatable(
                animation = tween(380, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_scale"
        )

        val isMegaGift = giftTx.giftCount >= 10 || giftTx.giftIcon in listOf("🚀", "👑", "🏰", "🛥️")

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Background Lottie Fireworks / Blast Layer
            LottieAnimation(
                composition = composition,
                progress = { lottieProgress },
                modifier = Modifier
                    .size(if (isMegaGift) 220.dp else 160.dp)
                    .align(Alignment.Center)
            )

            // Luxury Glassmorphic Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(26.dp), spotColor = StarGoldPrimary)
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color(0xFF16132C).copy(alpha = 0.95f),
                                Color(0xFF221A45).copy(alpha = 0.92f),
                                Color(0xFF16132C).copy(alpha = 0.90f)
                            )
                        ),
                        shape = RoundedCornerShape(26.dp)
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.horizontalGradient(
                            listOf(
                                StarGoldPrimary,
                                NeonCyan,
                                StarGoldLight
                            )
                        ),
                        shape = RoundedCornerShape(26.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sender Avatar Badge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(StarGoldDark, StarGoldPrimary)))
                        .border(1.5.dp, Color.White, CircleShape)
                ) {
                    Text(
                        text = giftTx.senderName.take(2).uppercase(),
                        color = StarKingBgDark,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Sender to Receiver text
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = giftTx.senderName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                        Text(
                            text = " sent ",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                        Text(
                            text = giftTx.giftName,
                            color = StarGoldLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "to ",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                        Text(
                            text = giftTx.receiverName,
                            color = NeonCyan,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Pulsating Gift Emoji
                Text(
                    text = giftTx.giftIcon,
                    fontSize = 34.sp,
                    modifier = Modifier.scale(pulseScale)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Glowing Combo Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(
                                    Color(0xFFE11D48),
                                    StarGoldPrimary
                                )
                            )
                        )
                        .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "x${giftTx.giftCount}",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
