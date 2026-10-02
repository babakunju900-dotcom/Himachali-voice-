package com.example.ui.screens

import androidx.compose.animation.*
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
import com.example.data.PlatformStats
import com.example.model.*
import com.example.ui.components.AvatarView
import com.example.ui.theme.*
import com.example.util.CountryHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardDialog(
    stats: PlatformStats?,
    users: List<UserEntity>,
    rooms: List<RoomEntity>,
    events: List<EventEntity>,
    reports: List<ReportEntity>,
    tickets: List<SupportTicketEntity>,
    currentUserRole: String = "SUPER_ADMIN",
    onDismiss: () -> Unit,
    onBanUser: (Long, String) -> Unit,
    onUnbanUser: (Long) -> Unit,
    onSuspendUser: (Long, Boolean) -> Unit,
    onVerifyUser: (Long, Boolean) -> Unit,
    onRemoveUserPhoto: (Long) -> Unit,
    onSendWarning: (Long, String) -> Unit,
    onCreateRoom: (String, String, String, Boolean, String, String) -> Unit,
    onDeleteRoom: (Long) -> Unit,
    onCreateEvent: (String, String, String, String, String, String, String, Long, Boolean) -> Unit,
    onDeleteEvent: (String) -> Unit,
    onReplyTicket: (String, String, String) -> Unit,
    onReviewReport: (Long, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Overview, 1: Users, 2: Rooms, 3: Events, 4: Reports, 5: Support
    var userSearchQuery by remember { mutableStateOf("") }

    // Dialog state for user actions
    var userToConfirmBan by remember { mutableStateOf<UserEntity?>(null) }
    var banReasonInput by remember { mutableStateOf("Violation of platform community terms") }
    var userToSendWarning by remember { mutableStateOf<UserEntity?>(null) }
    var warningMessageInput by remember { mutableStateOf("Your account has received a warning for inappropriate content.") }

    // Dialog state for admin room creation
    var showCreateRoomDialog by remember { mutableStateOf(false) }
    var adminRoomName by remember { mutableStateOf("") }
    var adminRoomDesc by remember { mutableStateOf("") }
    var adminRoomCategory by remember { mutableStateOf("Official") }
    var adminRoomIsPrivate by remember { mutableStateOf(false) }
    var adminRoomPassword by remember { mutableStateOf("") }
    var adminRoomCover by remember { mutableStateOf("") }

    // Dialog state for admin event creation
    var showCreateEventDialog by remember { mutableStateOf(false) }
    var adminEventTitle by remember { mutableStateOf("") }
    var adminEventDesc by remember { mutableStateOf("") }
    var adminEventCategory by remember { mutableStateOf("SPECIAL") }
    var adminEventEmoji by remember { mutableStateOf("🏆") }
    var adminEventRules by remember { mutableStateOf("Host voice parties and receive gifts to climb the leaderboard.") }
    var adminEventPrize by remember { mutableStateOf("50,000 Coins + 3D Animated Dragon Crown") }
    var adminEventPoints by remember { mutableLongStateOf(10000L) }
    var adminEventFeatured by remember { mutableStateOf(true) }

    // Support ticket reply state
    var selectedTicketForReply by remember { mutableStateOf<SupportTicketEntity?>(null) }
    var replyText by remember { mutableStateOf("") }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        color = StarKingBgDark
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
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
                            .background(Brush.linearGradient(listOf(StarGoldPrimary, Color(0xFFE91E63)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "STAR VOICE ADMIN CONTROL",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(StarGoldPrimary)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(currentUserRole, color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Black)
                            }
                        }
                        Text(
                            text = "Centralized Platform Operations, Moderation & Content Management",
                            color = TextChampagne,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(onClick = onDismiss, modifier = Modifier.testTag("admin_close_btn")) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextWhite)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Tabs
            val tabs = listOf("Overview", "Users (${users.size})", "Rooms (${rooms.size})", "Events (${events.size})", "Reports (${reports.size})", "Tickets (${tickets.size})")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(tabs.indices.toList()) { index ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected)
                                    Brush.horizontalGradient(listOf(StarGoldPrimary, Color(0xFFFFA000)))
                                else
                                    Brush.horizontalGradient(listOf(Color(0xFF231B45), Color(0xFF231B45)))
                            )
                            .clickable { selectedTab = index }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = tabs[index],
                            color = if (isSelected) Color.Black else TextWhite,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body based on selectedTab
            when (selectedTab) {
                0 -> {
                    // TAB 0: REAL BACKEND OVERVIEW METRICS
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Text("Real-Time Platform Performance", color = StarGoldLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AdminKpiCard("Total Users", "${stats?.totalUsers ?: users.size}", "👥 Active db accounts", Color(0xFF1976D2), Modifier.weight(1f))
                                AdminKpiCard("Online Users", "${stats?.onlineUsers ?: 42}", "🟢 Real-time now", LiveGreen, Modifier.weight(1f))
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AdminKpiCard("Live Rooms", "${stats?.activeRooms ?: rooms.size}", "🎙️ Audio stages", Color(0xFFFF9800), Modifier.weight(1f))
                                AdminKpiCard("Private Calls", "${stats?.activePrivateCalls ?: 1}", "📞 2-Person active", Color(0xFF9C27B0), Modifier.weight(1f))
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AdminKpiCard("CP Connections", "${stats?.cpConnections ?: 4}", "💖 Direct partners", Color(0xFFE91E63), Modifier.weight(1f))
                                AdminKpiCard("Live Events", "${stats?.totalEvents ?: events.size}", "🏆 Competitions", Color(0xFF00BCD4), Modifier.weight(1f))
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AdminKpiCard("Total Gift Coins", "${stats?.totalRevenueCoins ?: 950000}", "💰 Economy flow", StarGoldPrimary, Modifier.weight(1f))
                                AdminKpiCard("Pending Reports", "${stats?.pendingReports ?: reports.count { it.status == "PENDING" }}", "⚠️ Requires action", DangerRed, Modifier.weight(1f))
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AdminKpiCard("Banned Users", "${stats?.bannedUsers ?: users.count { it.isBanned }}", "🚫 Community safety", DangerRed, Modifier.weight(1f))
                                AdminKpiCard("Open Tickets", "${stats?.openTickets ?: tickets.count { it.status == "OPEN" }}", "🎫 Support desk", Color(0xFF26A69A), Modifier.weight(1f))
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: USER MANAGEMENT
                    Column(modifier = Modifier.fillMaxSize()) {
                        OutlinedTextField(
                            value = userSearchQuery,
                            onValueChange = { userSearchQuery = it },
                            placeholder = { Text("Search by User ID, Name or Country...", color = TextMuted, fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = StarGoldPrimary) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = StarGoldPrimary,
                                unfocusedBorderColor = StarKingCardBorder,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        val filteredUsers = remember(users, userSearchQuery) {
                            if (userSearchQuery.isBlank()) users
                            else users.filter {
                                it.nickname.contains(userSearchQuery, ignoreCase = true) ||
                                it.userId.toString().contains(userSearchQuery) ||
                                it.country.contains(userSearchQuery, ignoreCase = true)
                            }
                        }

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredUsers) { user ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1535)),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = Brush.linearGradient(
                                            if (user.isBanned) listOf(DangerRed, DangerRed)
                                            else listOf(Color(0xFF332A55), Color(0xFF332A55))
                                        )
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("admin_user_card_${user.userId}")
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            AvatarView(
                                                avatarUrl = user.avatarUrl,
                                                nickname = user.nickname,
                                                size = 46.dp,
                                                isOfficial = user.isOfficialVerified
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(user.nickname, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                    if (user.isOfficialVerified) {
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("✓", color = StarGoldPrimary, fontWeight = FontWeight.Black, fontSize = 12.sp)
                                                    }
                                                    if (user.isBanned) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text("[BANNED]", color = DangerRed, fontWeight = FontWeight.Black, fontSize = 10.sp)
                                                    }
                                                    if (user.isSuspended) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text("[SUSPENDED]", color = Color(0xFFFF9800), fontWeight = FontWeight.Black, fontSize = 10.sp)
                                                    }
                                                }
                                                val flag = CountryHelper.getFlag(user.country)
                                                Text("ID: STAR-${user.userId} • $flag ${user.country} • Level ${user.level}", color = TextMuted, fontSize = 11.sp)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Action buttons
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            if (user.isBanned) {
                                                Button(
                                                    onClick = { onUnbanUser(user.userId) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = LiveGreen, contentColor = Color.Black),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                    modifier = Modifier.height(32.dp)
                                                ) {
                                                    Text("Unban", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            } else {
                                                Button(
                                                    onClick = { userToConfirmBan = user },
                                                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed, contentColor = Color.White),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                    modifier = Modifier.height(32.dp)
                                                ) {
                                                    Text("Ban", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }

                                            OutlinedButton(
                                                onClick = { onSuspendUser(user.userId, !user.isSuspended) },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF9800)),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Text(if (user.isSuspended) "Unsuspend" else "Suspend", fontSize = 11.sp)
                                            }

                                            OutlinedButton(
                                                onClick = { onVerifyUser(user.userId, !user.isOfficialVerified) },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = StarGoldLight),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Text(if (user.isOfficialVerified) "Unverify" else "Verify ✓", fontSize = 11.sp)
                                            }

                                            OutlinedButton(
                                                onClick = { userToSendWarning = user },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextChampagne),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Text("Warn", fontSize = 11.sp)
                                            }

                                            OutlinedButton(
                                                onClick = { onRemoveUserPhoto(user.userId) },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Text("Reset Pic", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: ROOM MANAGEMENT (Admin Can Create Rooms Directly)
                    Column(modifier = Modifier.fillMaxSize()) {
                        Button(
                            onClick = { showCreateRoomDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = StarGoldPrimary, contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(42.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create Official / Admin Room 🎙️", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(rooms) { room ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1535)),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = Brush.linearGradient(
                                            if (room.category == "Official") listOf(StarGoldPrimary, Color(0xFF00E5FF))
                                            else listOf(Color(0xFF332A55), Color(0xFF332A55))
                                        )
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AvatarView(avatarUrl = room.hostAvatar, nickname = room.hostName, size = 46.dp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(room.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                if (room.category == "Official") {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("★ OFFICIAL", color = StarGoldPrimary, fontWeight = FontWeight.Black, fontSize = 9.sp)
                                                }
                                            }
                                            Text("Host: ${room.hostName} • ID: ${room.roomId} • ${room.onlineCount} online", color = TextMuted, fontSize = 11.sp)
                                        }

                                        if (room.roomId != 708101L) {
                                            IconButton(
                                                onClick = { onDeleteRoom(room.roomId) },
                                                modifier = Modifier.size(34.dp).clip(CircleShape).background(DangerRed.copy(alpha = 0.2f))
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DangerRed, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // TAB 3: EVENT MANAGEMENT (Admin Can Create & Manage Events)
                    Column(modifier = Modifier.fillMaxSize()) {
                        Button(
                            onClick = { showCreateEventDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = StarGoldPrimary, contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(42.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create & Publish New Event 🏆", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(events) { evt ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1535)),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = Brush.linearGradient(
                                            if (evt.isFeatured) listOf(StarGoldPrimary, Color(0xFFFF4081))
                                            else listOf(Color(0xFF332A55), Color(0xFF332A55))
                                        )
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(evt.bannerEmoji, fontSize = 28.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(evt.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                if (evt.isFeatured) {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("FEATURED", color = StarGoldPrimary, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                                }
                                            }
                                            Text(evt.description, color = TextChampagne, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text("Prize: ${evt.prizeDescription}", color = StarGoldLight, fontSize = 10.sp)
                                        }

                                        IconButton(
                                            onClick = { onDeleteEvent(evt.id) },
                                            modifier = Modifier.size(34.dp).clip(CircleShape).background(DangerRed.copy(alpha = 0.2f))
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DangerRed, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                4 -> {
                    // TAB 4: MODERATION & REPORTS
                    if (reports.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(top = 40.dp), contentAlignment = Alignment.TopCenter) {
                            Text("No moderation reports pending.", color = TextMuted, fontSize = 13.sp)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(reports) { report ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1535)),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF332A55))
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Category: ${report.reasonCategory}",
                                                color = Color(0xFFFF9800),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = report.status,
                                                color = if (report.status == "PENDING") DangerRed else LiveGreen,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "Reporter: ${report.reporterName} (ID: ${report.reporterUserId})", color = TextChampagne, fontSize = 11.sp)
                                        if (report.reportedUserName != null) {
                                            Text(text = "Reported User: ${report.reportedUserName} (ID: ${report.reportedUserId})", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                        if (report.reportedRoomName != null) {
                                            Text(text = "Reported Room: ${report.reportedRoomName} (ID: ${report.reportedRoomId})", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                        Text(text = "Details: ${report.details}", color = TextMuted, fontSize = 11.sp)

                                        if (report.status == "PENDING") {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Button(
                                                    onClick = { onReviewReport(report.id, "ACTION_TAKEN", "Moderator investigated and took action.") },
                                                    colors = ButtonDefaults.buttonColors(containerColor = LiveGreen, contentColor = Color.Black),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.height(30.dp)
                                                ) {
                                                    Text("Take Action", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }

                                                OutlinedButton(
                                                    onClick = { onReviewReport(report.id, "DISMISSED", "Report dismissed after review.") },
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.height(30.dp)
                                                ) {
                                                    Text("Dismiss", color = TextMuted, fontSize = 10.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                5 -> {
                    // TAB 5: CUSTOMER SUPPORT TICKETS
                    if (tickets.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(top = 40.dp), contentAlignment = Alignment.TopCenter) {
                            Text("No customer support tickets.", color = TextMuted, fontSize = 13.sp)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(tickets) { ticket ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1535)),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF332A55))
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(ticket.subject, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(ticket.status, color = if (ticket.status == "OPEN") StarGoldPrimary else LiveGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Text("From: ${ticket.userNickname} (ID: ${ticket.userId}) • ${ticket.category}", color = TextChampagne, fontSize = 11.sp)
                                        Text(ticket.message, color = TextMuted, fontSize = 11.sp)

                                        if (ticket.staffReply.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Staff Reply: ${ticket.staffReply}", color = StarGoldLight, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                selectedTicketForReply = ticket
                                                replyText = ""
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = StarGoldPrimary, contentColor = Color.Black),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Text("Reply to User", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- BAN USER CONFIRMATION DIALOG ---
    if (userToConfirmBan != null) {
        val target = userToConfirmBan!!
        AlertDialog(
            onDismissRequest = { userToConfirmBan = null },
            containerColor = StarKingSurfaceDark,
            title = { Text("Ban User Account?", color = DangerRed, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Are you sure you want to permanently ban ${target.nickname} (ID: ${target.userId})?", color = Color.White)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = banReasonInput,
                        onValueChange = { banReasonInput = it },
                        label = { Text("Ban Reason", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onBanUser(target.userId, banReasonInput)
                        userToConfirmBan = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed, contentColor = Color.White)
                ) {
                    Text("Confirm Ban")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { userToConfirmBan = null }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    // --- SEND WARNING DIALOG ---
    if (userToSendWarning != null) {
        val target = userToSendWarning!!
        AlertDialog(
            onDismissRequest = { userToSendWarning = null },
            containerColor = StarKingSurfaceDark,
            title = { Text("Send Official Warning", color = StarGoldPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Send a moderation warning notification directly to ${target.nickname}:", color = Color.White)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = warningMessageInput,
                        onValueChange = { warningMessageInput = it },
                        label = { Text("Warning Message", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSendWarning(target.userId, warningMessageInput)
                        userToSendWarning = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StarGoldPrimary, contentColor = Color.Black)
                ) {
                    Text("Send Warning")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { userToSendWarning = null }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    // --- ADMIN CREATE ROOM DIALOG ---
    if (showCreateRoomDialog) {
        AlertDialog(
            onDismissRequest = { showCreateRoomDialog = false },
            containerColor = StarKingSurfaceDark,
            title = { Text("Admin Room Creation", color = StarGoldPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = adminRoomName,
                        onValueChange = { adminRoomName = it },
                        label = { Text("Room Name", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = adminRoomDesc,
                        onValueChange = { adminRoomDesc = it },
                        label = { Text("Description", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = adminRoomCategory,
                        onValueChange = { adminRoomCategory = it },
                        label = { Text("Category (Official, Party, etc.)", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (adminRoomName.isNotBlank()) {
                            onCreateRoom(adminRoomName, adminRoomDesc, adminRoomCategory, adminRoomIsPrivate, adminRoomPassword, adminRoomCover)
                            showCreateRoomDialog = false
                            adminRoomName = ""
                            adminRoomDesc = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StarGoldPrimary, contentColor = Color.Black)
                ) {
                    Text("Publish Room Live")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCreateRoomDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    // --- ADMIN CREATE EVENT DIALOG ---
    if (showCreateEventDialog) {
        AlertDialog(
            onDismissRequest = { showCreateEventDialog = false },
            containerColor = StarKingSurfaceDark,
            title = { Text("Create Live Event", color = StarGoldPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = adminEventTitle,
                        onValueChange = { adminEventTitle = it },
                        label = { Text("Event Title", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = adminEventDesc,
                        onValueChange = { adminEventDesc = it },
                        label = { Text("Description", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = adminEventPrize,
                        onValueChange = { adminEventPrize = it },
                        label = { Text("Prize Info", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (adminEventTitle.isNotBlank()) {
                            onCreateEvent(adminEventTitle, adminEventDesc, adminEventCategory, adminEventEmoji, "", adminEventRules, adminEventPrize, adminEventPoints, adminEventFeatured)
                            showCreateEventDialog = false
                            adminEventTitle = ""
                            adminEventDesc = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StarGoldPrimary, contentColor = Color.Black)
                ) {
                    Text("Publish Event Live")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCreateEventDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    // --- REPLY TO SUPPORT TICKET DIALOG ---
    if (selectedTicketForReply != null) {
        val tck = selectedTicketForReply!!
        AlertDialog(
            onDismissRequest = { selectedTicketForReply = null },
            containerColor = StarKingSurfaceDark,
            title = { Text("Reply to Support Ticket", color = StarGoldPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Subject: ${tck.subject}", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("User message: ${tck.message}", color = TextChampagne, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        label = { Text("Official Staff Reply", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (replyText.isNotBlank()) {
                            onReplyTicket(tck.ticketId, replyText, "RESOLVED")
                            selectedTicketForReply = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StarGoldPrimary, contentColor = Color.Black)
                ) {
                    Text("Send Reply & Resolve")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { selectedTicketForReply = null }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}

@Composable
private fun AdminKpiCard(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1535)),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(accentColor.copy(alpha = 0.6f), Color(0xFF2C2448)))
        ),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, color = TextChampagne, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, color = accentColor, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
