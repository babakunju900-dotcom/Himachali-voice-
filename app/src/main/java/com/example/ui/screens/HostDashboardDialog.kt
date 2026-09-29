package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserEntity
import com.example.model.WalletEntity
import com.example.ui.components.AvatarView
import com.example.ui.theme.*

@Composable
fun HostDashboardDialog(
    user: UserEntity?,
    wallet: WalletEntity?,
    onDismiss: () -> Unit,
    onConvertDiamonds: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var convertAmount by remember { mutableStateOf("500") }
    var showConvertSuccess by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        color = StarKingBgDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Star Host Center 🎙️",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "Streaming performance & gift revenue analytics",
                        color = StarGoldLight,
                        fontSize = 11.sp
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("host_dash_close_btn")) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextWhite)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Host ID & Rating Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StarKingSurfaceVariantDark),
                        shape = RoundedCornerShape(16.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(StarGoldPrimary, RoyalPurple))),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AvatarView(
                                avatarUrl = user?.avatarUrl ?: "avatar_user",
                                nickname = user?.nickname ?: "Host",
                                size = 52.dp,
                                level = user?.level ?: 1,
                                vipTier = user?.vipTier ?: 0,
                                isOfficial = user?.isOfficialVerified ?: false
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = user?.nickname ?: "Host",
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Star King Creator ID: ${user?.userId} • Level ${user?.level}",
                                    color = StarGoldLight,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "Status: Eligible for Creator Rewards ✓",
                                    color = LiveGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Performance Metrics Grid
                item {
                    Text(text = "Monthly Performance", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricBox(label = "Voice Hours", value = "42.5 hrs", icon = "⏱️", modifier = Modifier.weight(1f))
                        MetricBox(label = "Room Visits", value = "1,840", icon = "👥", modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricBox(label = "Gifts Received", value = "${user?.giftsReceivedCount ?: 0}", icon = "🎁", modifier = Modifier.weight(1f))
                        MetricBox(label = "Diamond Income", value = "${wallet?.diamondBalance ?: 0} 💎", icon = "💎", modifier = Modifier.weight(1f))
                    }
                }

                // Diamond Cash-Out / Coin Conversion Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StarKingCardDark),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Convert Diamonds to Star Coins 💰",
                                color = StarGoldPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Available Diamonds: ${String.format("%,d", wallet?.diamondBalance ?: 0L)} 💎 (1 💎 = 1 🪙 Coin)",
                                color = TextChampagne,
                                fontSize = 12.sp
                            )

                            OutlinedTextField(
                                value = convertAmount,
                                onValueChange = { convertAmount = it },
                                label = { Text("Amount of Diamonds to Convert", color = TextMuted) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = StarGoldPrimary,
                                    unfocusedBorderColor = StarKingCardBorder,
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    val amount = convertAmount.toLongOrNull() ?: 0L
                                    if (amount > 0 && amount <= (wallet?.diamondBalance ?: 0L)) {
                                        onConvertDiamonds(amount)
                                        showConvertSuccess = true
                                    }
                                },
                                enabled = (wallet?.diamondBalance ?: 0L) > 0,
                                colors = ButtonDefaults.buttonColors(containerColor = StarGoldPrimary, contentColor = StarKingBgDark),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(44.dp)
                            ) {
                                Text("Convert to Coins Instantly", fontWeight = FontWeight.Bold)
                            }

                            if (showConvertSuccess) {
                                Text(
                                    text = "Successfully converted diamonds to wallet coins! 🌟",
                                    color = LiveGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricBox(label: String, value: String, icon: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(StarKingSurfaceVariantDark)
            .padding(12.dp)
    ) {
        Column {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(text = label, color = TextMuted, fontSize = 11.sp)
        }
    }
}
