package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessageEntity
import com.example.model.NotificationEntity
import com.example.model.UserEntity
import com.example.ui.components.AvatarView
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.*

@Composable
fun MessagesScreen(
    recentConversations: List<ChatMessageEntity>,
    notifications: List<NotificationEntity>,
    onSelectChatUser: (UserEntity) -> Unit,
    onOpenSupportTicket: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Chats, 1: Notifications

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StarKingBgDark)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Tabs: Chats vs Notifications
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(StarKingSurfaceVariantDark)
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selectedTab == 0) StarGoldPrimary else Color.Transparent)
                    .clickable { selectedTab = 0 }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "💬 Chats",
                    color = if (selectedTab == 0) StarKingBgDark else TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selectedTab == 1) StarGoldPrimary else Color.Transparent)
                    .clickable { selectedTab = 1 }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🔔 Notifications",
                        color = if (selectedTab == 1) StarKingBgDark else TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    val unread = notifications.count { !it.isRead }
                    if (unread > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(DangerRed)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedTab == 0) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Official Customer Support Channel Pinned Card
                item {
                    val supportStaff = UserEntity(
                        userId = 100002L,
                        nickname = "Customer Support 24/7",
                        avatarUrl = "avatar_support",
                        isOfficialVerified = true,
                        role = "CUSTOMER_SUPPORT"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(StarKingSurfaceVariantDark)
                            .clickable { onSelectChatUser(supportStaff) }
                            .padding(12.dp)
                            .testTag("pinned_support_chat"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AvatarView(
                            avatarUrl = "avatar_support",
                            nickname = "Support",
                            size = 46.dp,
                            isOfficial = true
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Star King Customer Support 24/7",
                                    color = StarGoldLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Official help desk for coins, rooms & account verification.",
                                color = TextMuted,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(StarKingCardDark)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "Official", color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Sample Friend chat (Aria Luna)
                item {
                    val friendAria = UserEntity(
                        userId = 502110L,
                        nickname = "Aria Luna 🎙️",
                        avatarUrl = "avatar_aria",
                        isOfficialVerified = true,
                        level = 18,
                        vipTier = 3,
                        role = "HOST"
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(StarKingSurfaceVariantDark)
                            .clickable { onSelectChatUser(friendAria) }
                            .padding(12.dp)
                            .testTag("friend_aria_chat"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AvatarView(
                            avatarUrl = friendAria.avatarUrl,
                            nickname = friendAria.nickname,
                            size = 46.dp,
                            level = friendAria.level,
                            vipTier = friendAria.vipTier,
                            isOfficial = friendAria.isOfficialVerified
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = friendAria.nickname,
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Thanks for the stars! See you in tonight's acoustic session 🎸",
                                color = TextMuted,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(text = "Just now", color = TextSecondary, fontSize = 10.sp)
                    }
                }

                // Support Ticket Action Card
                item {
                    Card(
                        onClick = onOpenSupportTicket,
                        colors = CardDefaults.cardColors(containerColor = StarKingCardDark),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().testTag("open_support_ticket_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🎫", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Need Technical Help or Ticket Resolution?",
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Submit a formal customer support ticket with staff response.",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            Text(text = "Submit >", color = StarGoldPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            // Notifications List
            if (notifications.isEmpty()) {
                EmptyStateView(
                    iconEmoji = "🔔",
                    title = "No Notifications",
                    description = "System announcements, gift alerts and recharge confirmations will appear here."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(notifications, key = { it.id }) { notif ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(StarKingSurfaceVariantDark)
                                .padding(12.dp)
                                .testTag("notification_item_${notif.id}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val icon = when (notif.type) {
                                "RECHARGE" -> "💰"
                                "GIFT" -> "🎁"
                                "FOLLOW" -> "🌟"
                                "VIP" -> "👑"
                                "ROOM_INVITE" -> "🎙️"
                                else -> "📢"
                            }
                            Text(text = icon, fontSize = 26.sp)

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = notif.title,
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = notif.message,
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
