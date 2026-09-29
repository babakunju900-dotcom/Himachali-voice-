package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GiftEntity
import com.example.model.RoomSeatEntity
import com.example.ui.theme.*

/**
 * Speaker recipient item representation
 */
data class SpeakerRecipient(
    val userId: Long,
    val name: String,
    val seatLabel: String,
    val avatarUrl: String = "avatar_user",
    val level: Int = 1,
    val isHost: Boolean = false
)

/**
 * Premium Gift Shop Bottom Sheet Component
 * Allows selecting and sending virtual gifts with Lottie animation triggers to any speaker in the voice room
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GiftBottomSheet(
    gifts: List<GiftEntity>,
    userCoinBalance: Long,
    seats: List<RoomSeatEntity>,
    hostUserId: Long,
    hostName: String,
    onDismiss: () -> Unit,
    onSendGift: (receiverUserId: Long, receiverName: String, giftId: String, count: Int) -> Unit,
    onRechargeClick: () -> Unit
) {
    // Build list of active speakers / seated participants in the room
    val speakers = remember(seats, hostUserId, hostName) {
        val list = mutableListOf<SpeakerRecipient>()
        // Host speaker
        list.add(
            SpeakerRecipient(
                userId = hostUserId,
                name = hostName,
                seatLabel = "Host Seat",
                avatarUrl = "avatar_host",
                level = 5,
                isHost = true
            )
        )
        // Seated speakers
        seats.forEach { seat ->
            if (seat.userId != null && seat.userId != hostUserId) {
                list.add(
                    SpeakerRecipient(
                        userId = seat.userId,
                        name = seat.userName ?: "Speaker #${seat.seatIndex + 1}",
                        seatLabel = "Seat #${seat.seatIndex + 1}",
                        avatarUrl = seat.userAvatar ?: "avatar_user",
                        level = seat.userLevel,
                        isHost = false
                    )
                )
            }
        }
        list
    }

    var selectedSpeaker by remember(speakers) { mutableStateOf(speakers.firstOrNull()) }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedGift by remember { mutableStateOf<GiftEntity?>(gifts.firstOrNull()) }
    var selectedCount by remember { mutableIntStateOf(1) }

    val categories = listOf("All", "Popular", "Love", "Star", "Luxury", "Special")
    val countPresets = listOf(
        1 to "1x",
        10 to "10x",
        66 to "66x (Smooth)",
        99 to "99x (Love)",
        520 to "520x (Forever)",
        1314 to "1314x (Lifetime)"
    )

    val filteredGifts = remember(gifts, selectedCategory) {
        if (selectedCategory == "All") gifts
        else gifts.filter { it.category.equals(selectedCategory, ignoreCase = true) }.ifEmpty { gifts }
    }

    val totalCost = (selectedGift?.coinPrice ?: 0L) * selectedCount
    val canAfford = userCoinBalance >= totalCost

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF140F28),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .size(width = 44.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF3B3363))
            )
        },
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
        ) {
            // Top Bar: Gift Shop Title, Live Coins & Top-up Shortcut
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎁", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Gift Shop",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Powered by Lottie Voice Room FX ✨",
                            color = NeonCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Coin Balance and Recharge button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF221A45))
                        .border(1.dp, StarGoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .clickable { onRechargeClick() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("gift_shop_recharge_btn")
                ) {
                    Text(text = "🪙", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format("%,d", userCoinBalance),
                        color = StarGoldPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "+ Top-up",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Speaker Recipient Selector (Horizontal scrolling chips of mic seats)
            Text(
                text = "SELECT SPEAKER RECIPIENT:",
                color = TextMuted,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().testTag("speaker_recipient_row")
            ) {
                items(speakers) { speaker ->
                    val isSelected = selectedSpeaker?.userId == speaker.userId
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .then(
                                if (isSelected) Modifier.background(Brush.horizontalGradient(listOf(Color(0xFF78350F), Color(0xFFD97706))))
                                else Modifier.background(Color(0xFF1E173D))
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) StarGoldPrimary else Color(0xFF2E2458),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedSpeaker = speaker }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        AvatarView(
                            avatarUrl = speaker.avatarUrl,
                            nickname = speaker.name,
                            size = 28.dp,
                            level = speaker.level,
                            isOfficial = speaker.isHost
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = speaker.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (speaker.isHost) {
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("👑", fontSize = 10.sp)
                                }
                            }
                            Text(
                                text = speaker.seatLabel,
                                color = if (isSelected) Color.White.copy(alpha = 0.8f) else TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Tabs
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = cat.equals(selectedCategory, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) StarGoldPrimary else Color(0xFF1E183D))
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) StarKingBgDark else Color(0xFFD1D5DB),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Virtual Gifts Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .testTag("gift_shop_grid"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredGifts, key = { it.id }) { gift ->
                    val isSelected = selectedGift?.id == gift.id

                    // Determine Lottie preview tag
                    val lottieEffectTag = when {
                        gift.name.contains("Rocket", ignoreCase = true) || gift.iconEmoji == "🚀" -> "🚀 Blast"
                        gift.name.contains("Heart", ignoreCase = true) ||
                        gift.name.contains("Rose", ignoreCase = true) ||
                        gift.iconEmoji in listOf("🌹", "💖", "💍", "💐", "🎈") -> "💖 Burst"
                        gift.name.contains("Crown", ignoreCase = true) ||
                        gift.iconEmoji in listOf("👑", "🏰", "🏎️", "🛥️", "🪄") -> "👑 Royal"
                        else -> "🎆 Spark"
                    }

                    Card(
                        onClick = { selectedGift = gift },
                        colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFF261D4C) else Color(0xFF191333)),
                        shape = RoundedCornerShape(16.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = if (isSelected) Brush.linearGradient(listOf(StarGoldLight, StarGoldPrimary, StarGoldDark))
                            else Brush.linearGradient(listOf(Color(0xFF2B2154), Color(0xFF1C163A)))
                        ),
                        modifier = Modifier.testTag("gift_item_${gift.id}")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 4.dp)
                        ) {
                            // Lottie Effect Mini Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF110D24))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = lottieEffectTag,
                                    color = StarGoldLight,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            // Large Gift Emoji
                            Text(text = gift.iconEmoji, fontSize = 28.sp)

                            Spacer(modifier = Modifier.height(2.dp))

                            // Gift Name
                            Text(
                                text = gift.name,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Price in Coins
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🪙", fontSize = 9.sp)
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = String.format("%,d", gift.coinPrice),
                                    color = StarGoldPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Multiplier & Combo Presets Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(countPresets) { (count, label) ->
                    val isSelected = selectedCount == count
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) StarGoldPrimary else Color(0xFF221A45))
                            .clickable { selectedCount = count }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) StarKingBgDark else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Actions Row: Lottie indicator + Send Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Info preview of destination & Lottie FX
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "To: ${selectedSpeaker?.name ?: hostName}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                    Text(
                        text = "✨ Screen Lottie fireworks & audio FX",
                        color = NeonCyan,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Send Button
                Button(
                    onClick = {
                        val gift = selectedGift ?: return@Button
                        val speaker = selectedSpeaker ?: return@Button
                        onSendGift(speaker.userId, speaker.name, gift.id, selectedCount)
                        onDismiss()
                    },
                    enabled = selectedGift != null && selectedSpeaker != null && canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StarGoldPrimary,
                        contentColor = StarKingBgDark,
                        disabledContainerColor = Color(0xFF2E2458),
                        disabledContentColor = TextMuted
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.height(44.dp).testTag("gift_shop_send_btn")
                ) {
                    if (canAfford) {
                        Text(
                            text = "Send (${String.format("%,d", totalCost)} 🪙)",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    } else {
                        Text(
                            text = "Need ${String.format("%,d", totalCost - userCoinBalance)} 🪙",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
