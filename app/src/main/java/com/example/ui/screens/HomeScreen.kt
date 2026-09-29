package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.model.RoomEntity
import com.example.model.UserEntity
import com.example.ui.components.AvatarView
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    rooms: List<RoomEntity>,
    topHosts: List<UserEntity>,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit,
    onRoomClick: (RoomEntity) -> Unit,
    onHostClick: (UserEntity) -> Unit,
    onCreateRoomClick: () -> Unit,
    onEventBannerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf("All", "Party", "Music", "Chat", "Gaming", "Dating", "Official")

    val filteredRooms = if (selectedCategory == "All") rooms
    else rooms.filter { it.category.equals(selectedCategory, ignoreCase = true) }

    Box(modifier = modifier.fillMaxSize().background(StarKingBgDark)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
        ) {
            // Hero Event Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(StarKingCardDark, RoyalPurple.copy(alpha = 0.8f), StarKingCardDark)
                            )
                        )
                        .border(1.dp, StarGoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .clickable { onEventBannerClick() }
                        .padding(16.dp)
                        .testTag("hero_event_banner")
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(StarGoldPrimary)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "OFFICIAL EVENT",
                                    color = StarKingBgDark,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp
                                )
                            }
                            Text(text = "Prize Pool: 1,000,000 🪙", color = StarGoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "STAR KING TOP HOST GALA 🏆",
                            color = TextWhite,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Join voice rooms, host streams & win exclusive Diamond Crown frames!",
                            color = TextChampagne.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Popular Hosts Carousel
            if (topHosts.isNotEmpty()) {
                item {
                    Column {
                        Text(
                            text = "Top Star Hosts 🌟",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(topHosts, key = { it.userId }) { host ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable { onHostClick(host) }
                                        .testTag("top_host_${host.userId}")
                                ) {
                                    AvatarView(
                                        avatarUrl = host.avatarUrl,
                                        nickname = host.nickname,
                                        size = 52.dp,
                                        frameName = host.equippedFrame,
                                        vipTier = host.vipTier,
                                        level = host.level,
                                        isOfficial = host.isOfficialVerified
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = host.nickname,
                                        color = TextWhite,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.widthIn(max = 68.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Category Filter Chips
            item {
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
                                .clickable { onCategorySelect(cat) }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                                .testTag("cat_chip_$cat")
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
            }

            // Live Rooms Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(LiveGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Live Voice Parties (${filteredRooms.size})",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // Rooms List
            if (filteredRooms.isEmpty()) {
                item {
                    EmptyStateView(
                        iconEmoji = "🎙️",
                        title = "No Rooms in this Category",
                        description = "Be the first creator to start a voice party in $selectedCategory!",
                        actionButtonText = "Create Voice Room",
                        onActionClick = onCreateRoomClick
                    )
                }
            } else {
                items(filteredRooms, key = { it.roomId }) { room ->
                    RoomCard(room = room, onClick = { onRoomClick(room) })
                }
            }
        }

        // Floating Action Button: Create Room
        FloatingActionButton(
            onClick = onCreateRoomClick,
            containerColor = StarGoldPrimary,
            contentColor = StarKingBgDark,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 90.dp)
                .testTag("home_create_room_fab")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp)
            ) {
                Icon(Icons.Default.Mic, contentDescription = "Create Room")
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Go Live",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun RoomCard(
    room: RoomEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = StarKingSurfaceVariantDark),
        shape = RoundedCornerShape(18.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(StarKingCardBorder, StarKingSurfaceVariantDark))),
        modifier = modifier
            .fillMaxWidth()
            .testTag("room_card_${room.roomId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Room Cover / Category Icon
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(StarKingCardDark, RoyalPurple)
                        )
                    )
                    .border(1.dp, StarGoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = room.coverEmoji, fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Room Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = room.name,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (room.isPrivate) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "🔒", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Host: ${room.hostName}",
                        color = StarGoldLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ID: ${room.roomId}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(StarKingCardDark)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = room.category,
                            color = NeonCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Online Users Count
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = "Online",
                            tint = LiveGreen,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${room.onlineCount} online",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    // Mic Seats count
                    Text(
                        text = "• ${room.seatCount} seats",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
