package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GiftTransactionEntity
import com.example.ui.theme.*

@Composable
fun GiftAnimationOverlay(
    giftTx: GiftTransactionEntity?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = giftTx != null,
        enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
        exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        if (giftTx == null) return@AnimatedVisibility

        val infiniteTransition = rememberInfiniteTransition(label = "gift_bounce")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.25f,
            animationSpec = infiniteRepeatable(
                animation = tween(400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "gift_scale"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(
                            StarKingCardDark.copy(alpha = 0.95f),
                            StarKingSurfaceVariantDark.copy(alpha = 0.9f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.horizontalGradient(listOf(StarGoldPrimary, NeonCyan, Color.Transparent)),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated Gift Emoji
            Text(
                text = giftTx.giftIcon,
                fontSize = 32.sp,
                modifier = Modifier.scale(scale)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${giftTx.senderName} sent ${giftTx.giftName}",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "to ${giftTx.receiverName}",
                    color = StarGoldLight,
                    fontSize = 11.sp
                )
            }

            // Combo Count Badge
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.linearGradient(listOf(StarGoldDark, StarGoldPrimary)),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "x${giftTx.giftCount}",
                    color = StarKingBgDark,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
            }
        }
    }
}
