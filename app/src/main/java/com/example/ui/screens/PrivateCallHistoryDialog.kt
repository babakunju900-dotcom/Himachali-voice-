package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PrivateCallHistoryEntity
import com.example.model.UserEntity
import com.example.ui.components.AvatarView
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivateCallHistoryDialog(
    history: List<PrivateCallHistoryEntity>,
    allUsers: List<UserEntity>,
    onDismiss: () -> Unit,
    onClearHistory: () -> Unit,
    onCallBack: (UserEntity) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    var showConfirmClear by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = StarKingSurfaceDark,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(44.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF4A3E72))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF281D4C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PhoneCallback, contentDescription = null, tint = StarGoldPrimary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Private Call History",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "${history.size} recorded call session${if (history.size == 1) "" else "s"}",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (history.isNotEmpty()) {
                        TextButton(
                            onClick = { showConfirmClear = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = DangerRed)
                        ) {
                            Text("Clear All", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(34.dp).clip(CircleShape).background(Color(0xFF281D4C))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (history.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(top = 50.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📞", fontSize = 42.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No call history yet", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Your 2-person private calls and missed rings will appear here.", color = TextMuted, fontSize = 11.sp)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(history) { entry ->
                        val targetUser = allUsers.find { it.userId == entry.otherUserId }
                        val isMissed = entry.status == "Missed" || entry.status == "Declined"

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1535)),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF2C2448))
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().testTag("call_history_item_${entry.historyId}")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AvatarView(
                                    avatarUrl = entry.otherUserAvatar,
                                    nickname = entry.otherUserName,
                                    size = 46.dp
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = entry.otherUserName,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = if (entry.isOutgoing) Icons.AutoMirrored.Filled.CallMade else Icons.AutoMirrored.Filled.CallReceived,
                                            contentDescription = null,
                                            tint = if (isMissed) DangerRed else LiveGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = "${dateFormat.format(Date(entry.timestamp))} at ${timeFormat.format(Date(entry.timestamp))}",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Status badge
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    when (entry.status) {
                                                        "Completed" -> LiveGreen.copy(alpha = 0.2f)
                                                        "Missed" -> DangerRed.copy(alpha = 0.2f)
                                                        "Declined" -> Color(0xFFFF9800).copy(alpha = 0.2f)
                                                        else -> Color(0xFF352750)
                                                    }
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = entry.status,
                                                color = when (entry.status) {
                                                    "Completed" -> LiveGreen
                                                    "Missed" -> DangerRed
                                                    "Declined" -> Color(0xFFFFB74D)
                                                    else -> TextChampagne
                                                },
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        if (entry.durationSeconds > 0) {
                                            val mins = entry.durationSeconds / 60
                                            val secs = entry.durationSeconds % 60
                                            Text(
                                                text = String.format("⏱️ %02d:%02d", mins, secs),
                                                color = StarGoldLight,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }

                                if (targetUser != null) {
                                    IconButton(
                                        onClick = { onCallBack(targetUser) },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF281D4C))
                                    ) {
                                        Icon(
                                            imageVector = if (entry.isVideo) Icons.Default.Videocam else Icons.Default.Call,
                                            contentDescription = "Call Back",
                                            tint = StarGoldPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showConfirmClear) {
        AlertDialog(
            onDismissRequest = { showConfirmClear = false },
            containerColor = StarKingSurfaceDark,
            title = { Text("Clear Call History?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to remove all your private call history logs? This cannot be undone.", color = TextChampagne) },
            confirmButton = {
                Button(
                    onClick = {
                        onClearHistory()
                        showConfirmClear = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed, contentColor = Color.White)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmClear = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}
