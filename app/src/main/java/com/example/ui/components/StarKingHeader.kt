package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserEntity
import com.example.model.UserRole
import com.example.ui.theme.*

@Composable
fun StarKingHeader(
    user: UserEntity?,
    coinBalance: Long,
    unreadNotifications: Int,
    onAvatarClick: () -> Unit,
    onCoinClick: () -> Unit,
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onAdminClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // User Info Section
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f, fill = false)
                .clickable { onAvatarClick() }
                .testTag("header_user_profile")
        ) {
            AvatarView(
                avatarUrl = user?.avatarUrl ?: "avatar_user",
                nickname = user?.nickname ?: "Guest",
                size = 38.dp,
                frameName = user?.equippedFrame ?: "",
                vipTier = user?.vipTier ?: 0,
                level = user?.level ?: 1,
                isOfficial = user?.isOfficialVerified ?: false
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = user?.nickname ?: "Star User",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = androidx.compose.ui.res.stringResource(com.example.R.string.user_id_label, (user?.userId ?: 504094L).toString()),
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Right side: Wallet Coin pill & Actions
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Coin Balance Pill with '+' recharge trigger
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(StarKingSurfaceVariantDark)
                    .border(1.dp, StarKingCardBorder, RoundedCornerShape(16.dp))
                    .clickable { onCoinClick() }
                    .padding(horizontal = 8.dp, vertical = 5.dp)
                    .testTag("header_wallet_pill")
            ) {
                Text(text = "🪙", fontSize = 13.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = String.format("%,d", coinBalance),
                    color = StarGoldPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(StarGoldPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        color = StarKingBgDark,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }
            }

            // Search Button
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier.size(40.dp).testTag("header_search_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = androidx.compose.ui.res.stringResource(com.example.R.string.search_rooms_users_desc),
                    tint = TextWhite,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Notifications
            Box(contentAlignment = Alignment.TopEnd) {
                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier.size(40.dp).testTag("header_notifications_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = androidx.compose.ui.res.stringResource(com.example.R.string.notifications_desc),
                        tint = TextWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
                if (unreadNotifications > 0) {
                    Box(
                        modifier = Modifier
                            .offset(x = (-2).dp, y = 2.dp)
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(DangerRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (unreadNotifications > 9) "9+" else unreadNotifications.toString(),
                            color = TextWhite,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Staff / Admin Dashboard shortcut if user has staff role
            val userRole = try { UserRole.valueOf(user?.role ?: "USER") } catch (_: Exception) { UserRole.USER }
            if (userRole.canAccessAdminPanel()) {
                IconButton(
                    onClick = onAdminClick,
                    modifier = Modifier.size(40.dp).testTag("header_admin_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = androidx.compose.ui.res.stringResource(com.example.R.string.admin_management_panel),
                        tint = StarGoldPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
