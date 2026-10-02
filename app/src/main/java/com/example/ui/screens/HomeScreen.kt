package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.model.RoomEntity
import com.example.model.UserEntity
import com.example.ui.components.AvatarView
import com.example.ui.components.StarVoiceChatBanner
import com.example.ui.theme.*
import kotlinx.coroutines.delay

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
    onStarVoiceBannerClick: () -> Unit = {},
    onCpClick: () -> Unit = {},
    onPrivateCallClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var topNavTab by remember { mutableStateOf("Room") } // "People", "Room", "Game", "Explore"
    val subCategories = listOf("Hot", "Joined", "Talent Show", "Game", "Dating")
    var currentSubCategory by remember { mutableStateOf("Hot") }

    val filteredRooms = remember(rooms, currentSubCategory) {
        when (currentSubCategory) {
            "Hot" -> rooms.sortedByDescending { it.onlineCount + (it.roomId % 50).toInt() }
            "Joined" -> rooms.take(4)
            "Talent Show" -> rooms.filter { it.category.contains("Music", ignoreCase = true) || it.category.contains("Party", ignoreCase = true) }
            "Game" -> rooms.filter { it.category.contains("Gaming", ignoreCase = true) }
            "Dating" -> rooms.filter { it.category.contains("Dating", ignoreCase = true) || it.category.contains("Chat", ignoreCase = true) }
            else -> rooms
        }.ifEmpty { rooms }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF141221))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // --- 1. TOP HEADER NAVIGATION (Matching Screenshot 1) ---
            // Spiral icon | People | Room (selected) | Game | Explo... | Search
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Spiral Icon
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(Color(0xFFFF2B6D), Color(0xFF7B1FA2)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🌀", fontSize = 16.sp)
                    }

                    // Top Bar Tabs
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "People",
                            color = if (topNavTab == "People") TextWhite else Color(0xFF9E9DB5),
                            fontWeight = if (topNavTab == "People") FontWeight.Bold else FontWeight.Medium,
                            fontSize = 15.sp,
                            modifier = Modifier.clickable { topNavTab = "People" }
                        )

                        // Room (Active)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { topNavTab = "Room" }
                        ) {
                            Text(
                                text = "Room",
                                color = TextWhite,
                                fontWeight = FontWeight.Black,
                                fontSize = 19.sp
                            )
                            Box(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(width = 16.dp, height = 3.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color.White)
                            )
                        }

                        // Game with red badge dot
                        Box(modifier = Modifier.clickable { topNavTab = "Game" }) {
                            Text(
                                text = "Game",
                                color = if (topNavTab == "Game") TextWhite else Color(0xFF9E9DB5),
                                fontWeight = if (topNavTab == "Game") FontWeight.Bold else FontWeight.Medium,
                                fontSize = 15.sp
                            )
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF2B6D))
                                    .align(Alignment.TopEnd)
                            )
                        }

                        Text(
                            text = "Explo...",
                            color = if (topNavTab == "Explore") TextWhite else Color(0xFF9E9DB5),
                            fontWeight = if (topNavTab == "Explore") FontWeight.Bold else FontWeight.Medium,
                            fontSize = 15.sp,
                            modifier = Modifier.clickable { topNavTab = "Explore" }
                        )
                    }

                    // Search Icon
                    IconButton(
                        onClick = { },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFFFF2B6D),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // --- 2. SUB-CATEGORY PILLS ROW ---
            // Hot (selected) | Joined | Talent Show | Game | Dating
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    subCategories.forEach { subCat ->
                        val isSelected = currentSubCategory == subCat
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    currentSubCategory = subCat
                                    onCategorySelect(subCat)
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = subCat,
                                color = if (isSelected) TextWhite else Color(0xFF8C8A9E),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .size(width = 12.dp, height = 2.dp)
                                        .clip(RoundedCornerShape(1.dp))
                                        .background(Color.White)
                                )
                            }
                        }
                    }
                }
            }

            // --- 3. WEALTH LEADERBOARD BANNER STRIP (Matching Screenshot 1) ---
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF8B2500), Color(0xFFB85000), Color(0xFF4A1000))
                            )
                        )
                        .border(1.dp, Color(0xFFFFB300).copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .clickable { onEventBannerClick() }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left: Golden Trophy / Crown & "Wealth"
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Brush.linearGradient(listOf(Color(0xFFFFD54F), Color(0xFFFFA000)))),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👑", fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Wealth",
                                    color = Color(0xFFFFECB3),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Weekly Top Donors 💎",
                                    color = Color(0xFFFFD54F).copy(alpha = 0.8f),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // Right: Laurel wreaths with #2, #1 (center, crowned), #3
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("🌿", fontSize = 14.sp)

                            // Rank 2
                            val host2 = topHosts.getOrNull(1)
                            AvatarView(
                                avatarUrl = host2?.avatarUrl ?: "avatar_2",
                                nickname = host2?.nickname ?: "Top 2",
                                size = 28.dp,
                                vipTier = 2
                            )

                            // Rank 1 (Center, King Crown)
                            val host1 = topHosts.firstOrNull()
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("👑", fontSize = 10.sp)
                                AvatarView(
                                    avatarUrl = host1?.avatarUrl ?: "avatar_1",
                                    nickname = host1?.nickname ?: "Top 1",
                                    size = 34.dp,
                                    vipTier = 5
                                )
                                Text("Week", color = Color(0xFFFFD54F), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }

                            // Rank 3
                            val host3 = topHosts.getOrNull(2)
                            AvatarView(
                                avatarUrl = host3?.avatarUrl ?: "avatar_3",
                                nickname = host3?.nickname ?: "Top 3",
                                size = 28.dp,
                                vipTier = 1
                            )

                            Text("🌿", fontSize = 14.sp)
                        }
                    }
                }
            }

            // --- 4. PROMOTIONAL SLIDER (STAR Voice Chat + Gala Banner) ---
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    val pagerState = rememberPagerState(pageCount = { 2 })

                    LaunchedEffect(pagerState) {
                        while (true) {
                            delay(6000)
                            val nextPage = (pagerState.currentPage + 1) % 2
                            pagerState.animateScrollToPage(nextPage)
                        }
                    }

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_banner_slider")
                    ) { page ->
                        when (page) {
                            0 -> {
                                StarVoiceChatBanner(
                                    onClick = onStarVoiceBannerClick,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            1 -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFF2A1B4E), Color(0xFF6A1B9A), Color(0xFF1A1035))
                                            )
                                        )
                                        .border(1.dp, StarGoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                                        .clickable { onEventBannerClick() }
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(StarGoldPrimary)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "FEATURED GALA 🏆",
                                                    color = Color.Black,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 9.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Star King Voice Superstars",
                                                color = Color.White,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 15.sp
                                            )
                                            Text(
                                                text = "Win exclusive 3D animated crowns & entrance gifts!",
                                                color = TextChampagne,
                                                fontSize = 11.sp
                                            )
                                        }
                                        Text("🌟 🚀", fontSize = 28.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- 4.5 DEDICATED OFFICIAL STAR VOICE SUPPORT ROOM (Matching Requirement 13) ---
            item {
                val officialRoom = rooms.find { it.category.equals("Official", ignoreCase = true) || it.name.contains("Customer Support", ignoreCase = true) }
                if (officialRoom != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Text("⭐", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "OFFICIAL VERIFIED DESK",
                                color = StarGoldPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            )
                        }

                        Card(
                            onClick = { onRoomClick(officialRoom) },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1535)),
                            shape = RoundedCornerShape(18.dp),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(
                                    listOf(StarGoldPrimary, Color(0xFF00E5FF), StarGoldLight)
                                ),
                                width = 1.5.dp
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("official_support_room_card")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Star Voice Customer Support Logo / Badge
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFFFFD54F), Color(0xFFFF6F00), Color(0xFFE91E63))
                                            )
                                        )
                                ) {
                                    Text("🌟", fontSize = 28.sp)
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(LiveGreen),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("✓", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = officialRoom.name,
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 15.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(StarGoldPrimary)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "OFFICIAL",
                                                color = Color.Black,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(3.dp))

                                    Text(
                                        text = officialRoom.description.ifBlank { "24/7 Verified Customer Support Desk • Real-time host & agency assistance" },
                                        color = TextChampagne,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(LiveGreen.copy(alpha = 0.2f))
                                                .border(1.dp, LiveGreen, RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("● LIVE AGENTS ACTIVE", color = LiveGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Text(
                                            text = "👥 ${officialRoom.onlineCount} online",
                                            color = StarGoldLight,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Default.HeadsetMic,
                                    contentDescription = "Support",
                                    tint = StarGoldPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }

            // --- 5. FEATURED ROOMS (Top-1 Large Card + Top 2 & 3 Stacked Cards) ---
            item {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                    val room1 = filteredRooms.firstOrNull()
                    val room2 = filteredRooms.getOrNull(1)
                    val room3 = filteredRooms.getOrNull(2)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Left: Large Top 1 Room Card
                        if (room1 != null) {
                            Box(
                                modifier = Modifier
                                    .weight(1.15f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(
                                        2.dp,
                                        Brush.linearGradient(listOf(Color(0xFFFFD54F), Color(0xFFFF6F00))),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFFB71C1C), Color(0xFFD32F2F), Color(0xFF7F0000))
                                        )
                                    )
                                    .clickable { onRoomClick(room1) }
                                    .padding(8.dp)
                                    .testTag("featured_room_top_1")
                            ) {
                                // Top row: Rank 1 badge & audience count (68)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Golden Crown Rank 1
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA000))))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("👑 1", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
                                    }

                                    // Red live count
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFD50000))
                                            .padding(horizontal = 6.dp, vertical = 1.dp)
                                    ) {
                                        Text("68", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    }
                                }

                                // Center Character Art / Avatar
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🧘‍♂️ ✨", fontSize = 48.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("● LIVE VOICE", color = Color(0xFFFFD54F), fontSize = 9.sp, fontWeight = FontWeight.Black)
                                }

                                // Bottom Title Row
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = room1.name.ifBlank { "ॐ प्यारे बाबा💚 का आश्रम" },
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(" 🏠", fontSize = 11.sp)
                                }
                            }
                        }

                        // Right: Stacked Top 2 and Top 3 Room Cards
                        Column(
                            modifier = Modifier
                                .weight(0.95f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Rank 2 Card
                            if (room2 != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .border(1.dp, Color(0xFF64B5F6).copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFF880E4F), Color(0xFFC2185B))
                                        )
                                    )
                                    .clickable { onRoomClick(room2) }
                                    .padding(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFF1976D2))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text("2", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                        }

                                        Text("62", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                    }

                                    Column(
                                        modifier = Modifier.align(Alignment.Center),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("👳‍♂️", fontSize = 24.sp)
                                    }

                                    Text(
                                        text = room2.name.ifBlank { "ॐ बाबा प्यारे..." },
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.align(Alignment.BottomStart)
                                    )
                                }
                            }

                            // Rank 3 Card
                            if (room3 != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .border(1.dp, Color(0xFFFFB74D).copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFF311B92), Color(0xFF4A148C))
                                        )
                                    )
                                    .clickable { onRoomClick(room3) }
                                    .padding(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFFE65100))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text("3", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                        }

                                        Text("49", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                    }

                                    Column(
                                        modifier = Modifier.align(Alignment.Center),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("💃", fontSize = 24.sp)
                                    }

                                    Text(
                                        text = room3.name.ifBlank { "💖 LADLA..." },
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.align(Alignment.BottomStart)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- 6. 3-COLUMN ROOM GRID (Matching Screenshot 1) ---
            item {
                val remainingRooms = filteredRooms.drop(3)
                val gridRooms = if (remainingRooms.isNotEmpty()) remainingRooms else filteredRooms

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val chunked = gridRooms.chunked(3)
                    chunked.forEach { rowRooms ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowRooms.forEach { room ->
                                CompactGridRoomCard(
                                    room = room,
                                    onClick = { onRoomClick(room) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Fill empty slots if last row has less than 3
                            repeat(3 - rowRooms.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // --- 7. BOTTOM QUICK ACTIONS CARDS (CP Dating | Private Call) ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // CP Dating Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFE91E63), Color(0xFFFF80AB))
                                )
                            )
                            .clickable { }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("CP Dating", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
                                Text("8846 are online", color = Color.White.copy(alpha = 0.85f), fontSize = 10.sp)
                            }
                            Text("💖 💓", fontSize = 20.sp)
                        }
                    }

                    // Private Call Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF673AB7), Color(0xFFB388FF))
                                )
                            )
                            .clickable { }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Private Call", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
                                Text("Let's chat", color = Color.White.copy(alpha = 0.85f), fontSize = 10.sp)
                            }
                            Text("📞 💬", fontSize = 20.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactGridRoomCard(
    room: RoomEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visitorCount = remember(room.roomId) {
        val base = 15 + ((room.roomId * 17) % 80).toInt()
        base.toString()
    }

    val cardGradient = remember(room.roomId) {
        when ((room.roomId % 4).toInt()) {
            0 -> listOf(Color(0xFFD50000), Color(0xFFB71C1C))
            1 -> listOf(Color(0xFF2E7D32), Color(0xFF1B5E20))
            2 -> listOf(Color(0xFF4A148C), Color(0xFF6A1B9A))
            else -> listOf(Color(0xFF006064), Color(0xFF00838F))
        }
    }

    Box(
        modifier = modifier
            .height(115.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFF332A55), RoundedCornerShape(14.dp))
            .background(Brush.verticalGradient(cardGradient))
            .clickable { onClick() }
            .padding(6.dp)
    ) {
        // Top row: visitor count on right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("💎", fontSize = 10.sp)
            Text(visitorCount, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp)
        }

        // Center Emoji Artwork
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val emoji = when ((room.roomId % 5).toInt()) {
                0 -> "👳‍♂️"
                1 -> "💑"
                2 -> "👸"
                3 -> "💃"
                else -> "🎧"
            }
            Text(emoji, fontSize = 26.sp)
        }

        // Bottom title & house icon
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = room.name,
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(" 🏠", fontSize = 8.sp)
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
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = if (room.category == "Official")
                Brush.linearGradient(listOf(StarGoldPrimary, LiveGreen))
            else
                Brush.linearGradient(listOf(StarKingCardBorder, StarKingCardBorder))
        ),
        modifier = modifier.fillMaxWidth().testTag("room_card_${room.roomId}")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(56.dp)
            ) {
                if (room.coverPhotoUrl.isNotBlank()) {
                    AsyncImage(
                        model = room.coverPhotoUrl,
                        contentDescription = "Room Cover",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 4.dp, y = 4.dp)
                    ) {
                        AvatarView(
                            avatarUrl = room.hostAvatar,
                            nickname = room.hostName,
                            size = 22.dp,
                            vipTier = if (room.category == "Official") 5 else 1,
                            isOfficial = room.category.equals("Official", ignoreCase = true)
                        )
                    }
                } else {
                    AvatarView(
                        avatarUrl = room.hostAvatar,
                        nickname = room.hostName,
                        size = 56.dp,
                        vipTier = if (room.category == "Official") 5 else 1,
                        isOfficial = room.category.equals("Official", ignoreCase = true)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = room.name,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (room.isPrivate) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "🔒", fontSize = 10.sp)
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Host: ${room.hostName} • ID: ${room.roomId}",
                    color = TextMuted,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (room.category == "Official") StarGoldPrimary else StarKingCardDark)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = room.category,
                            color = if (room.category == "Official") Color.Black else StarGoldPrimary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "👥 ${room.onlineCount} online", color = TextChampagne, fontSize = 10.sp)
                }
            }
            Icon(Icons.Default.Mic, contentDescription = "Live", tint = LiveGreen, modifier = Modifier.size(20.dp))
        }
    }
}

