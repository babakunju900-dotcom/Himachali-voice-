package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.StoreCustomizationEntity
import com.example.ui.theme.*

/**
 * 3D-styled Luxury Medal Graphic representing each badge from the Star King Medal Wall
 */
@Composable
fun MedalGraphic(
    medalId: String,
    medalName: String,
    size: Dp = 76.dp,
    modifier: Modifier = Modifier
) {
    val cleanId = medalId.lowercase()
    val cleanName = medalName.lowercase()

    // Determine color theme & primary icon/insignia for the medal
    val (primaryGradients, secondaryColor, centerIcon, crownEmoji, bannerText) = when {
        cleanId.contains("king") || cleanName == "king" -> MedalVisual(
            gradient = listOf(Color(0xFF8B0000), Color(0xFFDC2626), Color(0xFFB45309)),
            rimColor = Color(0xFFFFD700),
            icon = "🦅",
            crown = "👑",
            banner = "KING"
        )
        cleanId.contains("official") && cleanId.contains("team") || cleanName.contains("official team") -> MedalVisual(
            gradient = listOf(Color(0xFF991B1B), Color(0xFFE11D48), Color(0xFFD97706)),
            rimColor = Color(0xFFFFD700),
            icon = "👔",
            crown = "👑",
            banner = "OFFICIAL"
        )
        cleanId.contains("official") || cleanName == "official" -> MedalVisual(
            gradient = listOf(Color(0xFF1D4ED8), Color(0xFF2563EB), Color(0xFF60A5FA)),
            rimColor = Color(0xFF93C5FD),
            icon = "✔️",
            crown = "⭐",
            banner = ""
        )
        cleanId.contains("wedding") || cleanName.contains("wedding") -> MedalVisual(
            gradient = listOf(Color(0xFFD97706), Color(0xFFF59E0B), Color(0xFFEF4444)),
            rimColor = Color(0xFFFFD700),
            icon = "💛",
            crown = "👑",
            banner = "WEDDING"
        )
        cleanId.contains("assistant") || cleanName == "assistant" -> MedalVisual(
            gradient = listOf(Color(0xFF581C87), Color(0xFF7E22CE), Color(0xFF9333EA)),
            rimColor = Color(0xFFC084FC),
            icon = "👤",
            crown = "🛡️",
            banner = "ASSISTANT"
        )
        cleanId.contains("service") || cleanName.contains("service") -> MedalVisual(
            gradient = listOf(Color(0xFFB45309), Color(0xFFD97706), Color(0xFF10B981)),
            rimColor = Color(0xFFFDE047),
            icon = "🎧",
            crown = "🛡️",
            banner = "SERVICE"
        )
        cleanId.contains("love") || cleanName.contains("love") -> MedalVisual(
            gradient = listOf(Color(0xFFBE185D), Color(0xFFEC4899), Color(0xFFFDA4AF)),
            rimColor = Color(0xFFFFE4E6),
            icon = "💖",
            crown = "👑",
            banner = "LOVE"
        )
        cleanId.contains("friend") && cleanId.contains("best") || cleanName.contains("best friend") -> MedalVisual(
            gradient = listOf(Color(0xFFB91C1C), Color(0xFFF59E0B), Color(0xFF991B1B)),
            rimColor = Color(0xFFFFD700),
            icon = "🤝",
            crown = "👑",
            banner = "FRIENDS"
        )
        cleanId.contains("friendship") || cleanName.contains("friendship maker") -> MedalVisual(
            gradient = listOf(Color(0xFF334155), Color(0xFF64748B), Color(0xFF94A3B8)),
            rimColor = Color(0xFFE2E8F0),
            icon = "🤝",
            crown = "⭐",
            banner = "MAKER"
        )
        cleanId.contains("rocket") || cleanName.contains("rocket") -> MedalVisual(
            gradient = listOf(Color(0xFF1E3A8A), Color(0xFF2563EB), Color(0xFF0284C7)),
            rimColor = Color(0xFFFFD700),
            icon = "🚀",
            crown = "👑",
            banner = "ROCKET"
        )
        cleanId.contains("bd") || cleanName == "bd" -> MedalVisual(
            gradient = listOf(Color(0xFF78350F), Color(0xFF92400E), Color(0xFFB45309)),
            rimColor = Color(0xFFFDE68A),
            icon = "🛡️",
            crown = "⭐",
            banner = "BD"
        )
        cleanId.contains("coin") || cleanName.contains("coin seller") -> MedalVisual(
            gradient = listOf(Color(0xFFB45309), Color(0xFFD97706), Color(0xFFF59E0B)),
            rimColor = Color(0xFFFFD700),
            icon = "💲",
            crown = "🪙",
            banner = "SELLER"
        )
        cleanId.contains("tiger") || cleanName.contains("tiger") -> MedalVisual(
            gradient = listOf(Color(0xFF18181B), Color(0xFFEA580C), Color(0xFFF97316)),
            rimColor = Color(0xFFFED7AA),
            icon = "🐯",
            crown = "⚡",
            banner = "TIGER"
        )
        cleanId.contains("cs") || cleanName.contains("cs admin") -> MedalVisual(
            gradient = listOf(Color(0xFF1E3A8A), Color(0xFF1D4ED8), Color(0xFF3B82F6)),
            rimColor = Color(0xFFFFD700),
            icon = "👩‍💼",
            crown = "🛡️",
            banner = "CS ADMIN"
        )
        cleanId.contains("first") || cleanName.contains("first recharge") -> MedalVisual(
            gradient = listOf(Color(0xFF047857), Color(0xFF059669), Color(0xFFF59E0B)),
            rimColor = Color(0xFFFFD700),
            icon = "💵",
            crown = "👑",
            banner = "RECHARGE"
        )
        cleanId.contains("winner") || cleanName.contains("winner") -> MedalVisual(
            gradient = listOf(Color(0xFF991B1B), Color(0xFFDC2626), Color(0xFFF59E0B)),
            rimColor = Color(0xFFFFD700),
            icon = "🏆",
            crown = "⭐",
            banner = "WINNER"
        )
        cleanId.contains("100m") || cleanName.contains("100m") -> MedalVisual(
            gradient = listOf(Color(0xFFB45309), Color(0xFFF59E0B), Color(0xFFFDE047)),
            rimColor = Color(0xFFFFFFFF),
            icon = "🦁",
            crown = "👑",
            banner = "100M"
        )
        cleanId.contains("millionaire") || cleanName.contains("millionaire") -> MedalVisual(
            gradient = listOf(Color(0xFF475569), Color(0xFF059669), Color(0xFFF59E0B)),
            rimColor = Color(0xFFE2E8F0),
            icon = "💎",
            crown = "👑",
            banner = "MILLION"
        )
        cleanId.contains("snack") || cleanName.contains("snack") -> MedalVisual(
            gradient = listOf(Color(0xFFB45309), Color(0xFFEA580C), Color(0xFFDC2626)),
            rimColor = Color(0xFFFFD700),
            icon = "🐉",
            crown = "🔥",
            banner = "SNACK"
        )
        cleanId.contains("30") || cleanName.contains("monthly") -> MedalVisual(
            gradient = listOf(Color(0xFF78350F), Color(0xFFD97706), Color(0xFFF59E0B)),
            rimColor = Color(0xFFFFD700),
            icon = "🏆",
            crown = "👑",
            banner = "30 DAY"
        )
        cleanId.contains("777") || cleanName.contains("lucky") -> MedalVisual(
            gradient = listOf(Color(0xFF334155), Color(0xFFDC2626), Color(0xFF64748B)),
            rimColor = Color(0xFFFDE047),
            icon = "🎰",
            crown = "7️⃣",
            banner = "777 PRO"
        )
        else -> MedalVisual(
            gradient = listOf(Color(0xFF1E3A8A), Color(0xFF6D28D9), Color(0xFFD97706)),
            rimColor = Color(0xFFFFD700),
            icon = "🎖️",
            crown = "👑",
            banner = "STAR"
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .shadow(6.dp, CircleShape, spotColor = primaryGradients.first())
    ) {
        // Outer aura halo
        Box(
            modifier = Modifier
                .size(size * 0.95f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            primaryGradients[1].copy(alpha = 0.5f),
                            primaryGradients[0].copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Winged decorative side accents (for heraldic medals)
        Row(
            modifier = Modifier.width(size * 1.05f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🪽", fontSize = (size.value * 0.28f).sp, modifier = Modifier.scale(scaleX = -1f, scaleY = 1f))
            Text("🪽", fontSize = (size.value * 0.28f).sp)
        }

        // Center Medallion Base (Ornate Shield/Disc)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size * 0.72f)
                .clip(CircleShape)
                .border(2.5.dp, Brush.sweepGradient(listOf(secondaryColor, Color.White, secondaryColor)), CircleShape)
                .background(Brush.radialGradient(primaryGradients))
        ) {
            // Inner Core Icon
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = centerIcon,
                    fontSize = (size.value * 0.32f).sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Top Crown / Tiara / Star Accent
        if (crownEmoji.isNotBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-4).dp)
            ) {
                Text(
                    text = crownEmoji,
                    fontSize = (size.value * 0.24f).sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Bottom Ribbon Banner (if present)
        if (bannerText.isNotBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 2.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF78350F), secondaryColor, Color(0xFF78350F))
                        )
                    )
                    .padding(horizontal = 6.dp, vertical = 1.dp)
            ) {
                Text(
                    text = bannerText,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = (size.value * 0.10f).sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

private data class MedalVisual(
    val gradient: List<Color>,
    val rimColor: Color,
    val icon: String,
    val crown: String,
    val banner: String
)

/**
 * 2-Column Medal Card matching the user's uploaded screenshots
 */
@Composable
fun MedalCard(
    medal: StoreCustomizationEntity,
    isOwned: Boolean,
    isEquipped: Boolean,
    userCoins: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1838)),
        shape = RoundedCornerShape(18.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = if (isEquipped) Brush.linearGradient(listOf(StarGoldLight, StarGoldPrimary, StarGoldDark))
            else Brush.linearGradient(listOf(Color(0xFF2C2456), Color(0xFF231C48)))
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("medal_card_${medal.id}")
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Equipped / In Use glowing badge in top right
            if (isEquipped) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Brush.horizontalGradient(listOf(StarGoldDark, StarGoldPrimary)))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "👑 In Use",
                        color = StarKingBgDark,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 9.sp
                    )
                }
            } else if (isOwned) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(LiveGreen.copy(alpha = 0.2f))
                        .border(1.dp, LiveGreen.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Owned",
                        color = LiveGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // 3D Medal Graphic Artwork
                MedalGraphic(
                    medalId = medal.id,
                    medalName = medal.name,
                    size = 78.dp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 5 Golden Stars: ★★★★★
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) {
                        Text(
                            text = "★",
                            color = Color(0xFFFFD700),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 1.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Medal Title (matching screenshots, e.g. "Dream wedding", "Assistant", "King")
                Text(
                    text = medal.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Price or Status tag
                Text(
                    text = when {
                        isEquipped -> "Equipped on Seat"
                        isOwned -> "Tap to Wear"
                        else -> "${String.format("%,d", medal.priceCoins)} 🪙"
                    },
                    color = if (isOwned) LiveGreen else StarGoldPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Interactive Medal Detail BottomSheet / Dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedalDetailBottomSheet(
    medal: StoreCustomizationEntity,
    isOwned: Boolean,
    isEquipped: Boolean,
    userCoins: Long,
    onDismiss: () -> Unit,
    onEquip: (String) -> Unit,
    onPurchase: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF16132C),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Drag Handle accent
            Box(
                modifier = Modifier
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF3B3363))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Large Hero Medal Graphic
            MedalGraphic(
                medalId = medal.id,
                medalName = medal.name,
                size = 110.dp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 5 Glowing Stars
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(5) {
                    Text(
                        text = "★",
                        color = Color(0xFFFFD700),
                        fontSize = 18.sp,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = medal.name,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Official Star King Voice Room Honor Medal",
                color = Color(0xFF9CA3AF),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Privileges & Perks Box
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1A3E)),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(0xFF2C2456), Color(0xFF352C67)))),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🎖️ Medal Privileges", color = StarGoldPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎙️", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Displays on your Voice Room mic seat & live avatar", color = Color(0xFFE2E8F0), fontSize = 12.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🌟", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Permanent showcase on your Profile Medal Wall", color = Color(0xFFE2E8F0), fontSize = 12.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💬", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Special animated badge next to your nickname in room chat", color = Color(0xFFE2E8F0), fontSize = 12.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⏱️", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Duration: ${medal.durationDays} Days of Honor Access", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            when {
                isEquipped -> {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2352)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("👑 Currently Equipped & Active", color = StarGoldPrimary, fontWeight = FontWeight.Bold)
                    }
                }
                isOwned -> {
                    Button(
                        onClick = {
                            onEquip(medal.id)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StarGoldPrimary,
                            contentColor = StarKingBgDark
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Wear / Equip Medal 🎖️", fontWeight = FontWeight.Black, fontSize = 15.sp)
                    }
                }
                else -> {
                    Button(
                        onClick = {
                            onPurchase(medal.id)
                            onDismiss()
                        },
                        enabled = userCoins >= medal.priceCoins,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StarGoldPrimary,
                            contentColor = StarKingBgDark
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(
                            text = if (userCoins >= medal.priceCoins) "Unlock Medal (${String.format("%,d", medal.priceCoins)} 🪙)"
                            else "Insufficient Coins (${String.format("%,d", medal.priceCoins)} 🪙)",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}
