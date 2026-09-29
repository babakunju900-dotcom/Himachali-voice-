package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RoomSeatEntity
import com.example.ui.theme.*

@Composable
fun VoiceSeatGrid(
    seats: List<RoomSeatEntity>,
    hostUserId: Long,
    currentUserId: Long,
    onSeatClick: (RoomSeatEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        userScrollEnabled = false
    ) {
        items(seats, key = { it.seatIndex }) { seat ->
            SeatItem(
                seat = seat,
                isHostSeat = seat.seatIndex == 0,
                isMySeat = seat.userId == currentUserId,
                onClick = { onSeatClick(seat) }
            )
        }
    }
}

@Composable
private fun SeatItem(
    seat: RoomSeatEntity,
    isHostSeat: Boolean,
    isMySeat: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .testTag("seat_${seat.seatIndex}")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(62.dp)
        ) {
            when {
                // Occupied Seat
                seat.userId != null -> {
                    AvatarView(
                        avatarUrl = seat.userAvatar ?: "avatar_user",
                        nickname = seat.userName ?: "User",
                        size = 46.dp,
                        level = seat.userLevel,
                        vipTier = seat.userVip,
                        isSpeaking = seat.isSpeaking,
                        onClick = onClick
                    )

                    // Crown for host seat
                    if (isHostSeat) {
                        Text(
                            text = "👑",
                            fontSize = 14.sp,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = (-10).dp)
                        )
                    }

                    // Muted status indicator
                    if (seat.isMuted) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(DangerRed)
                                .border(1.dp, StarKingBgDark, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MicOff,
                                contentDescription = "Muted",
                                tint = TextWhite,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                }

                // Locked Seat
                seat.isLocked -> {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(SeatLockedColor)
                            .border(1.dp, StarKingCardBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Seat Locked",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Empty Seat (Ready to sit)
                else -> {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(SeatEmptyColor)
                            .border(1.dp, StarKingCardBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Empty Mic Seat",
                            tint = StarGoldSecondary.copy(alpha = 0.7f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Seat Label or User Name
        Text(
            text = when {
                seat.userId != null -> if (isMySeat) "You" else seat.userName ?: "User"
                isHostSeat -> "Host"
                else -> "${seat.seatIndex + 1}"
            },
            color = if (isMySeat) StarGoldPrimary else TextWhite,
            fontWeight = if (isMySeat || isHostSeat) FontWeight.Bold else FontWeight.Normal,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
