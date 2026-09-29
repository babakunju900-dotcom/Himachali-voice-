package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceRoomScreen(
    room: RoomEntity,
    seats: List<RoomSeatEntity>,
    messages: List<ChatMessageEntity>,
    activeGiftTx: GiftTransactionEntity?,
    handRaises: List<RoomHandRaiseEntity>,
    currentUserId: Long,
    isMicEnabled: Boolean,
    isMuted: Boolean,
    isSpeaking: Boolean,
    isSpeakerOn: Boolean,
    audioWaveLevels: List<Float>,
    onExitRoom: () -> Unit,
    onSeatClick: (RoomSeatEntity) -> Unit,
    onLeaveSeat: () -> Unit,
    onToggleMic: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onOpenGiftSheet: () -> Unit,
    onSendMessage: (String) -> Unit,
    onFollowHost: (Long) -> Unit,
    onReportRoom: () -> Unit,
    onRaiseHand: () -> Unit,
    onCancelHandRaise: () -> Unit,
    onAcceptHandRaise: (UserEntity, Int) -> Unit,
    onLockSeat: (Int) -> Unit,
    onMuteSeat: (Int) -> Unit,
    onKickSeat: (Int) -> Unit,
    onCloseRoom: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onExitRoom() }

    var chatInput by remember { mutableStateOf("") }
    var showHostControlSheet by remember { mutableStateOf(false) }
    var showHandRaiseQueueSheet by remember { mutableStateOf(false) }
    var selectedSeatForHostAction by remember { mutableStateOf<RoomSeatEntity?>(null) }
    var showSeatActionModal by remember { mutableStateOf<RoomSeatEntity?>(null) }

    val isHost = room.hostUserId == currentUserId
    val mySeat = seats.find { it.userId == currentUserId }
    val isSeated = mySeat != null

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(StarKingBgDark, StarKingSurfaceDark, StarKingSurfaceVariantDark)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 8.dp)
        ) {
            // --- TOP BAR ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Host pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(StarKingSurfaceVariantDark.copy(alpha = 0.8f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("room_host_pill")
                ) {
                    AvatarView(
                        avatarUrl = room.hostAvatar,
                        nickname = room.hostName,
                        size = 28.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = room.name,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 120.dp)
                        )
                        Text(
                            text = "ID: ${room.roomId}",
                            color = StarGoldLight,
                            fontSize = 10.sp
                        )
                    }
                    if (!isHost) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(StarGoldPrimary)
                                .clickable { onFollowHost(room.hostUserId) }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                .testTag("room_follow_host_btn")
                        ) {
                            Text(
                                text = "+Follow",
                                color = StarKingBgDark,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Right Actions: Online Count, Share, Report, Exit
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Online Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(StarKingCardDark.copy(alpha = 0.7f))
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = "Online",
                            tint = LiveGreen,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = "${room.onlineCount}", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Report Room
                    IconButton(
                        onClick = onReportRoom,
                        modifier = Modifier.size(32.dp).testTag("room_report_btn")
                    ) {
                        Icon(Icons.Default.Report, contentDescription = "Report", tint = TextMuted, modifier = Modifier.size(18.dp))
                    }

                    // Host Hand-Raise Queue Badge (if host)
                    if (isHost && handRaises.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(StarGoldPrimary)
                                .clickable { showHandRaiseQueueSheet = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("room_hand_queue_btn")
                        ) {
                            Text(
                                text = "✋ ${handRaises.size}",
                                color = StarKingBgDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Host Management shortcut if host
                    if (isHost) {
                        IconButton(
                            onClick = { showHostControlSheet = true },
                            modifier = Modifier.size(32.dp).testTag("room_host_menu_btn")
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = "Host Controls", tint = StarGoldPrimary, modifier = Modifier.size(18.dp))
                        }
                    }

                    // Exit Button
                    IconButton(
                        onClick = onExitRoom,
                        modifier = Modifier.size(32.dp).testTag("room_exit_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Exit Room", tint = DangerRed, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Live Microphone Audio Visualizer Bar when speaking on seat
            if (isSeated && isSpeaking) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🎙️ You are speaking: ", color = StarGoldPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    audioWaveLevels.forEach { wave ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .width(3.dp)
                                .height((wave * 18).coerceIn(4f, 20f).dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(NeonCyan)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- CENTER: VOICE SEAT GRID ---
            VoiceSeatGrid(
                seats = seats,
                hostUserId = room.hostUserId,
                currentUserId = currentUserId,
                onSeatClick = { seat ->
                    if (isHost) {
                        selectedSeatForHostAction = seat
                    } else {
                        showSeatActionModal = seat
                    }
                },
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            // --- ANIMATED GIFT OVERLAY ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                contentAlignment = Alignment.Center
            ) {
                GiftAnimationOverlay(giftTx = activeGiftTx)
            }

            // --- LOWER CENTER: REAL-TIME ROOM CHAT FEED ---
            RoomChatFeed(
                messages = messages,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            // --- BOTTOM CONTROLS BAR ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Chat input field
                OutlinedTextField(
                    value = chatInput,
                    onValueChange = { chatInput = it },
                    placeholder = { Text("Say something...", color = TextSecondary, fontSize = 12.sp) },
                    trailingIcon = {
                        if (chatInput.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    onSendMessage(chatInput)
                                    chatInput = ""
                                },
                                modifier = Modifier.size(32.dp).testTag("room_send_chat_btn")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = StarGoldPrimary, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = StarKingCardDark.copy(alpha = 0.8f),
                        unfocusedContainerColor = StarKingCardDark.copy(alpha = 0.8f),
                        focusedBorderColor = StarGoldPrimary,
                        unfocusedBorderColor = StarKingCardBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("room_chat_input")
                )

                // Leave Seat Button (if seated)
                if (isSeated) {
                    IconButton(
                        onClick = onLeaveSeat,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(StarKingSurfaceVariantDark)
                            .border(1.dp, DangerRed.copy(alpha = 0.5f), CircleShape)
                            .testTag("room_leave_seat_btn")
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Leave Seat", tint = DangerRed, modifier = Modifier.size(20.dp))
                    }
                } else {
                    // Raise Hand Toggle for listeners
                    val hasRaisedHand = handRaises.any { it.userId == currentUserId }
                    IconButton(
                        onClick = { if (hasRaisedHand) onCancelHandRaise() else onRaiseHand() },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (hasRaisedHand) StarGoldPrimary else StarKingSurfaceVariantDark)
                            .border(1.dp, if (hasRaisedHand) StarGoldLight else StarKingCardBorder, CircleShape)
                            .testTag("room_raise_hand_btn")
                    ) {
                        Text(text = "✋", fontSize = 18.sp)
                    }
                }

                // Microphone Toggle Button (if seated)
                if (isSeated) {
                    IconButton(
                        onClick = onToggleMic,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isMicEnabled && !isMuted) StarGoldPrimary else StarKingSurfaceVariantDark)
                            .border(1.dp, if (isMicEnabled && !isMuted) StarGoldLight else StarKingCardBorder, CircleShape)
                            .testTag("room_mic_toggle_btn")
                    ) {
                        Icon(
                            imageVector = if (isMicEnabled && !isMuted) Icons.Default.Mic else Icons.Default.MicOff,
                            contentDescription = "Mic Toggle",
                            tint = if (isMicEnabled && !isMuted) StarKingBgDark else DangerRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Speaker audio toggle
                IconButton(
                    onClick = onToggleSpeaker,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(StarKingSurfaceVariantDark)
                        .border(1.dp, StarKingCardBorder, CircleShape)
                        .testTag("room_speaker_toggle_btn")
                ) {
                    Icon(
                        imageVector = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                        contentDescription = "Speaker Toggle",
                        tint = if (isSpeakerOn) NeonCyan else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Gift Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(StarGoldDark, StarGoldPrimary))
                        )
                        .clickable { onOpenGiftSheet() }
                        .testTag("room_open_gift_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🎁", fontSize = 22.sp)
                }
            }
        }

        // --- GUEST SEAT ACTION SHEET ---
        if (showSeatActionModal != null) {
            val seat = showSeatActionModal!!
            AlertDialog(
                onDismissRequest = { showSeatActionModal = null },
                containerColor = StarKingSurfaceDark,
                title = {
                    Text(
                        text = "Seat #${seat.seatIndex + 1}",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        if (seat.userId != null) {
                            Text(
                                text = "Occupied by ${seat.userName} (Lv.${seat.userLevel})",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        } else if (seat.isLocked) {
                            Text(text = "This seat is locked by the host.", color = DangerRed, fontSize = 13.sp)
                        } else {
                            Text(
                                text = "Take this microphone seat and start speaking to everyone in the room!",
                                color = TextChampagne,
                                fontSize = 13.sp
                            )
                        }
                    }
                },
                confirmButton = {
                    if (seat.userId == null && !seat.isLocked) {
                        Button(
                            onClick = {
                                onSeatClick(seat)
                                showSeatActionModal = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StarGoldPrimary, contentColor = StarKingBgDark),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Take Mic Seat 🎙️", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSeatActionModal = null }) {
                        Text("Close", color = TextMuted)
                    }
                }
            )
        }

        // --- HOST CONTROLS DIALOG / SHEET ---
        if (selectedSeatForHostAction != null) {
            val seat = selectedSeatForHostAction!!
            AlertDialog(
                onDismissRequest = { selectedSeatForHostAction = null },
                containerColor = StarKingSurfaceDark,
                title = {
                    Text("Host Action on Seat #${seat.seatIndex + 1}", color = TextWhite, fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (seat.userId != null) "User: ${seat.userName}" else "Status: Empty",
                            color = StarGoldLight,
                            fontSize = 13.sp
                        )

                        // Lock / Unlock Seat
                        Button(
                            onClick = {
                                onLockSeat(seat.seatIndex)
                                selectedSeatForHostAction = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StarKingSurfaceVariantDark),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (seat.isLocked) "🔓 Unlock Seat" else "🔒 Lock Seat", color = TextWhite)
                        }

                        // Mute / Unmute Seat (if occupied)
                        if (seat.userId != null) {
                            Button(
                                onClick = {
                                    onMuteSeat(seat.seatIndex)
                                    selectedSeatForHostAction = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StarKingSurfaceVariantDark),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (seat.isMuted) "🔊 Unmute User" else "🔇 Mute User", color = TextWhite)
                            }

                            // Kick from seat
                            Button(
                                onClick = {
                                    onKickSeat(seat.seatIndex)
                                    selectedSeatForHostAction = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DangerRed.copy(alpha = 0.8f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("❌ Remove User From Seat", color = TextWhite, fontWeight = FontWeight.Bold)
                            }
                        } else if (!seat.isLocked) {
                            Button(
                                onClick = {
                                    onSeatClick(seat)
                                    selectedSeatForHostAction = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StarGoldPrimary, contentColor = StarKingBgDark),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Take This Seat", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { selectedSeatForHostAction = null }) {
                        Text("Cancel", color = TextMuted)
                    }
                }
            )
        }

        // Host Menu Sheet
        if (showHostControlSheet) {
            ModalBottomSheet(
                onDismissRequest = { showHostControlSheet = false },
                containerColor = StarKingSurfaceDark
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Host Control Center 👑", color = StarGoldPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Manage your room's voice settings and status.", color = TextMuted, fontSize = 12.sp)

                    Button(
                        onClick = {
                            showHostControlSheet = false
                            onCloseRoom()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("End Voice Party Room", fontWeight = FontWeight.Bold, color = TextWhite)
                    }
                }
            }
        }

        // Host Hand-Raise Queue Sheet
        if (showHandRaiseQueueSheet) {
            ModalBottomSheet(
                onDismissRequest = { showHandRaiseQueueSheet = false },
                containerColor = StarKingSurfaceDark
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "✋ Hand-Raise Queue (${handRaises.size})",
                        color = StarGoldPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Listeners waiting to speak. Select an available seat to invite them.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )

                    val emptySeats = seats.filter { it.userId == null && !it.isLocked }

                    if (handRaises.isEmpty()) {
                        Text("No listeners waiting in queue.", color = TextMuted, fontSize = 13.sp)
                    } else {
                        handRaises.forEach { raise ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(StarKingSurfaceVariantDark)
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AvatarView(
                                        avatarUrl = raise.userAvatar,
                                        nickname = raise.userName,
                                        size = 36.dp,
                                        level = raise.userLevel,
                                        vipTier = raise.userVip
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(text = raise.userName, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(text = "Lv.${raise.userLevel}", color = StarGoldLight, fontSize = 11.sp)
                                    }
                                }

                                if (emptySeats.isNotEmpty()) {
                                    val firstEmpty = emptySeats.first()
                                    Button(
                                        onClick = {
                                            val targetUser = UserEntity(
                                                userId = raise.userId,
                                                nickname = raise.userName,
                                                avatarUrl = raise.userAvatar,
                                                level = raise.userLevel,
                                                vipTier = raise.userVip
                                            )
                                            onAcceptHandRaise(targetUser, firstEmpty.seatIndex)
                                            showHandRaiseQueueSheet = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = StarGoldPrimary, contentColor = StarKingBgDark),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Invite to Seat #${firstEmpty.seatIndex + 1}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Text("Seats Full", color = DangerRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
