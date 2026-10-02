package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CallState
import com.example.model.PrivateCallSessionEntity
import com.example.ui.components.AvatarView
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun PrivateCallScreen(
    session: PrivateCallSessionEntity,
    currentUserId: Long,
    onAcceptCall: () -> Unit,
    onDeclineCall: () -> Unit,
    onEndCall: () -> Unit
) {
    val isCaller = session.callerId == currentUserId
    val otherUserName = if (isCaller) session.receiverName else session.callerName
    val otherUserAvatar = if (isCaller) session.receiverAvatar else session.callerAvatar
    val otherUserId = if (isCaller) session.receiverId else session.callerId

    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(true) }
    var isCameraOn by remember { mutableStateOf(session.isVideo) }

    // Live call duration timer
    var elapsedSeconds by remember { mutableStateOf(0) }

    LaunchedEffect(session.status, session.connectedAt) {
        if (session.status == CallState.CONNECTED.name) {
            val initialSeconds = if (session.connectedAt > 0L) {
                ((System.currentTimeMillis() - session.connectedAt) / 1000).toInt()
            } else 0
            elapsedSeconds = maxOf(0, initialSeconds)
            while (true) {
                delay(1000)
                elapsedSeconds++
            }
        }
    }

    // Pulsing circle animation during calling/ringing
    val infiniteTransition = rememberInfiniteTransition(label = "call_wave")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F0B1E),
                        Color(0xFF1B1438),
                        Color(0xFF0A0714)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("private_call_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Privacy & Security Badge
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF1F173B))
                        .border(1.dp, Color(0xFF3E3166), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Encrypted",
                        tint = StarGoldPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "2-PERSON PRIVATE ENCRYPTED CALL",
                        color = StarGoldLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (session.isVideo) "STAR VOICE VIDEO CALL" else "STAR VOICE DIRECT CALL",
                    color = TextChampagne,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Center: Avatars & Status
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Animated pulse box around other participant
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(160.dp)
                ) {
                    if (session.status == CallState.RINGING.name || session.status == CallState.CALLING.name) {
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(Color(0xFFE91E63).copy(alpha = 0.2f))
                        )
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .scale(pulseScale * 0.9f)
                                .clip(CircleShape)
                                .background(StarGoldPrimary.copy(alpha = 0.25f))
                        )
                    }

                    AvatarView(
                        avatarUrl = otherUserAvatar,
                        nickname = otherUserName,
                        size = 100.dp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = otherUserName,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "ID: STAR-$otherUserId",
                    color = StarGoldPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Connection Status / Timer Banner
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when (session.status) {
                                CallState.CONNECTED.name -> LiveGreen.copy(alpha = 0.2f)
                                CallState.RINGING.name, CallState.CALLING.name -> Color(0xFFE91E63).copy(alpha = 0.2f)
                                CallState.BUSY.name -> Color(0xFFFF9800).copy(alpha = 0.2f)
                                else -> Color(0xFF281D4C)
                            }
                        )
                        .border(
                            1.dp,
                            when (session.status) {
                                CallState.CONNECTED.name -> LiveGreen
                                CallState.RINGING.name, CallState.CALLING.name -> Color(0xFFE91E63)
                                else -> Color(0xFF4A3E72)
                            },
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = when (session.status) {
                            CallState.CONNECTED.name -> {
                                val mins = elapsedSeconds / 60
                                val secs = elapsedSeconds % 60
                                String.format("CONNECTED  •  %02d:%02d", mins, secs)
                            }
                            CallState.RINGING.name -> if (isCaller) "RINGING..." else "INCOMING CALL..."
                            CallState.CALLING.name -> "CONNECTING..."
                            CallState.BUSY.name -> "USER IS CURRENTLY BUSY"
                            CallState.DECLINED.name -> "CALL DECLINED"
                            CallState.ENDED.name -> "CALL ENDED"
                            else -> session.status
                        },
                        color = when (session.status) {
                            CallState.CONNECTED.name -> LiveGreen
                            CallState.RINGING.name, CallState.CALLING.name -> Color(0xFFFF80AB)
                            CallState.BUSY.name -> Color(0xFFFFB74D)
                            else -> TextChampagne
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // If user is busy notification notice
                if (session.status == CallState.BUSY.name) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "User is currently in another private call.",
                        color = DangerRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Bottom Controls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // In-Call Tools (Mute, Speaker, Video)
                if (session.status == CallState.CONNECTED.name) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mute Mic
                        IconButton(
                            onClick = { isMuted = !isMuted },
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(if (isMuted) DangerRed else Color(0xFF281D4C))
                        ) {
                            Icon(
                                if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Mute",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Speaker
                        IconButton(
                            onClick = { isSpeakerOn = !isSpeakerOn },
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(if (isSpeakerOn) StarGoldPrimary else Color(0xFF281D4C))
                        ) {
                            Icon(
                                if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = "Speaker",
                                tint = if (isSpeakerOn) Color.Black else Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Camera toggle if video call
                        IconButton(
                            onClick = { isCameraOn = !isCameraOn },
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(if (isCameraOn) Color(0xFF00E5FF) else Color(0xFF281D4C))
                        ) {
                            Icon(
                                if (isCameraOn) Icons.Default.Videocam else Icons.Default.VideocamOff,
                                contentDescription = "Video",
                                tint = if (isCameraOn) Color.Black else Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Call Action Buttons (Accept vs Decline/End)
                if (!isCaller && session.status == CallState.RINGING.name) {
                    // Incoming call: show Accept (Green) and Decline (Red)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDeclineCall,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(DangerRed)
                        ) {
                            Icon(
                                Icons.Default.CallEnd,
                                contentDescription = "Decline",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        IconButton(
                            onClick = onAcceptCall,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(LiveGreen)
                        ) {
                            Icon(
                                Icons.Default.Call,
                                contentDescription = "Accept",
                                tint = Color.Black,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                } else {
                    // Caller or in-progress call: Big Red End Call Button
                    IconButton(
                        onClick = onEndCall,
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(DangerRed)
                            .testTag("end_private_call_btn")
                    ) {
                        Icon(
                            Icons.Default.CallEnd,
                            contentDescription = "End Call",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}
