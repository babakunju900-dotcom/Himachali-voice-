package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GiftEntity
import com.example.model.RoomSeatEntity
import com.example.ui.theme.*

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
    var selectedCategory by remember { mutableStateOf("Popular") }
    var selectedGift by remember { mutableStateOf<GiftEntity?>(gifts.firstOrNull()) }
    var selectedCount by remember { mutableIntStateOf(1) }

    // Eligible receivers: Seated users and Host
    val seatedUsers = remember(seats, hostUserId, hostName) {
        val list = mutableListOf<Pair<Long, String>>()
        list.add(hostUserId to hostName)
        seats.forEach { seat ->
            if (seat.userId != null && list.none { it.first == seat.userId }) {
                list.add(seat.userId to (seat.userName ?: "Speaker"))
            }
        }
        list
    }
    var selectedReceiver by remember(seatedUsers) { mutableStateOf(seatedUsers.firstOrNull()) }

    val categories = listOf("Popular", "Love", "Star", "Luxury", "Festival", "Special")
    val countPresets = listOf(1, 10, 99, 520, 1314)

    val filteredGifts = remember(gifts, selectedCategory) {
        gifts.filter { it.category.equals(selectedCategory, ignoreCase = true) }
            .ifEmpty { gifts }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = StarKingSurfaceDark,
        dragHandle = { BottomSheetDefaults.DragHandle(color = TextMuted) },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
        ) {
            // Header: Receiver selector & Coin balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Receiver Dropdown/Picker
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Send to: ",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(StarKingSurfaceVariantDark)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = selectedReceiver?.second ?: hostName,
                            color = StarGoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                // Balance with recharge shortcut
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onRechargeClick() }
                        .clip(RoundedCornerShape(12.dp))
                        .background(StarKingSurfaceVariantDark)
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text(text = "🪙", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format("%,d", userCoinBalance),
                        color = StarGoldPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Top-up >",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Tabs
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = cat.equals(selectedCategory, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) StarGoldPrimary else StarKingSurfaceVariantDark)
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) StarKingBgDark else TextWhite,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Gifts Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredGifts, key = { it.id }) { gift ->
                    val isSelected = selectedGift?.id == gift.id
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) StarKingCardDark else Color.Transparent)
                            .border(
                                width = if (isSelected) 1.5.dp else 0.5.dp,
                                color = if (isSelected) StarGoldPrimary else StarKingCardBorder,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedGift = gift }
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                            .testTag("gift_${gift.id}")
                    ) {
                        Text(text = gift.iconEmoji, fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = gift.name,
                            color = TextWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🪙", fontSize = 10.sp)
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = String.format("%,d", gift.coinPrice),
                                color = StarGoldSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Bar: Presets & Send Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Combo Presets
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    countPresets.forEach { count ->
                        val isSelected = selectedCount == count
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) StarKingCardDark else StarKingSurfaceVariantDark)
                                .border(
                                    1.dp,
                                    if (isSelected) StarGoldPrimary else Color.Transparent,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedCount = count }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${count}x",
                                color = if (isSelected) StarGoldPrimary else TextWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Send Button
                val totalCost = (selectedGift?.coinPrice ?: 0L) * selectedCount
                Button(
                    onClick = {
                        val gift = selectedGift ?: return@Button
                        val receiver = selectedReceiver ?: return@Button
                        onSendGift(receiver.first, receiver.second, gift.id, selectedCount)
                        onDismiss()
                    },
                    enabled = selectedGift != null && userCoinBalance >= totalCost,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StarGoldPrimary,
                        contentColor = StarKingBgDark,
                        disabledContainerColor = StarKingSurfaceVariantDark,
                        disabledContentColor = TextMuted
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("send_gift_btn")
                ) {
                    Text(
                        text = "Send (${String.format("%,d", totalCost)} 🪙)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
