package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserEntity
import com.example.model.UserRole
import com.example.model.WalletEntity
import com.example.ui.components.AvatarView
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
    user: UserEntity?,
    wallet: WalletEntity?,
    onRechargeClick: () -> Unit,
    onOpenStore: () -> Unit,
    onOpenSupport: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAdminDashboard: () -> Unit,
    onOpenAuth: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userRole = try { UserRole.valueOf(user?.role ?: "USER") } catch (_: Exception) { UserRole.USER }
    val level = user?.level ?: 1
    val exp = user?.exp ?: 0
    val targetExpForNextLevel = level * 1000
    val progress = (exp % 1000).toFloat() / 1000f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(StarKingBgDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
    ) {
        // Profile Header Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = StarKingSurfaceVariantDark),
                shape = RoundedCornerShape(20.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(StarKingCardBorder, RoyalPurple))),
                modifier = Modifier.fillMaxWidth().testTag("profile_header_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AvatarView(
                            avatarUrl = user?.avatarUrl ?: "avatar_user",
                            nickname = user?.nickname ?: "User",
                            size = 64.dp,
                            frameName = user?.equippedFrame ?: "",
                            vipTier = user?.vipTier ?: 0,
                            level = level,
                            isOfficial = user?.isOfficialVerified ?: false
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = user?.nickname ?: "Star King User",
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                                if (user?.isOfficialVerified == true) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "✓", color = StarGoldPrimary, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = "User ID: ${user?.userId ?: "504094"}",
                                color = StarGoldLight,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "${user?.gender ?: "Star"} • ${user?.country ?: "Global"} • ${user?.language ?: "English"}",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }

                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier.size(36.dp).testTag("profile_settings_btn")
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextWhite)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = user?.bio ?: "Living the Star King voice party life! 🌟",
                        color = TextChampagne,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Followers / Following / Gifts Count
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ProfileStatItem(label = "Followers", value = "${user?.followersCount ?: 0}")
                        ProfileStatItem(label = "Following", value = "${user?.followingCount ?: 0}")
                        ProfileStatItem(label = "Gifts Received", value = "${user?.giftsReceivedCount ?: 0}")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Level Progress Bar
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Level $level", color = StarGoldPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text(text = "Next: Level ${level + 1} (${exp % 1000}/1000 XP)", color = TextMuted, fontSize = 10.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            color = StarGoldPrimary,
                            trackColor = StarKingCardDark,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
        }

        // Wallet Balance Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = StarKingSurfaceVariantDark),
                shape = RoundedCornerShape(20.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(StarGoldPrimary.copy(alpha = 0.6f), StarKingCardBorder))),
                modifier = Modifier.fillMaxWidth().testTag("wallet_balance_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Star King Wallet 💼",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )

                        Button(
                            onClick = onRechargeClick,
                            colors = ButtonDefaults.buttonColors(containerColor = StarGoldPrimary, contentColor = StarKingBgDark),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("wallet_recharge_btn")
                        ) {
                            Text(text = "+ Recharge Coins", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Coin Balance", color = TextMuted, fontSize = 11.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🪙 ", fontSize = 16.sp)
                                Text(
                                    text = String.format("%,d", wallet?.coinBalance ?: 0L),
                                    color = StarGoldPrimary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp
                                )
                            }
                        }

                        Column {
                            Text(text = "Diamonds (Income)", color = TextMuted, fontSize = 11.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "💎 ", fontSize = 16.sp)
                                Text(
                                    text = String.format("%,d", wallet?.diamondBalance ?: 0L),
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Staff / Admin Management Hub (If user is staff or owner)
        if (userRole.canAccessAdminPanel()) {
            item {
                Card(
                    onClick = onOpenAdminDashboard,
                    colors = CardDefaults.cardColors(containerColor = StarKingCardDark),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(StarGoldPrimary, NeonCyan))),
                    modifier = Modifier.fillMaxWidth().testTag("profile_admin_hub_card")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(StarGoldPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin", tint = StarKingBgDark)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Admin & Staff Control Center",
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Role: ${userRole.name} • Manage users, tickets & reports",
                                color = StarGoldLight,
                                fontSize = 11.sp
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = "Open", tint = StarGoldPrimary)
                    }
                }
            }
        }

        // Customization Store & Features Row
        item {
            Text(text = "Personalization & Features", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(StarKingSurfaceVariantDark)
            ) {
                ProfileMenuRow(
                    icon = Icons.Default.AutoAwesome,
                    title = "Customization Store",
                    subtitle = "Avatar frames, badges, titles & entry effects",
                    onClick = onOpenStore
                )
                HorizontalDivider(color = StarKingCardBorder, thickness = 0.5.dp)
                ProfileMenuRow(
                    icon = Icons.Default.SupportAgent,
                    title = "Official Customer Support",
                    subtitle = "Ticket inquiries, recharge & room guidance",
                    onClick = onOpenSupport
                )
                HorizontalDivider(color = StarKingCardBorder, thickness = 0.5.dp)
                ProfileMenuRow(
                    icon = Icons.Default.AccountCircle,
                    title = "Switch / Register Account",
                    subtitle = "Login with Google, Phone OTP or Guest ID",
                    onClick = onOpenAuth
                )
            }
        }
    }
}

@Composable
private fun ProfileStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(text = label, color = TextMuted, fontSize = 11.sp)
    }
}

@Composable
private fun ProfileMenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, tint = StarGoldPrimary, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TextWhite, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(text = subtitle, color = TextMuted, fontSize = 11.sp)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = "Go", tint = TextSecondary, modifier = Modifier.size(18.dp))
    }
}
