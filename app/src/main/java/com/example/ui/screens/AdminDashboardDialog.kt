package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlatformStats
import com.example.model.ReportEntity
import com.example.model.SupportTicketEntity
import com.example.model.UserEntity
import com.example.ui.components.AvatarView
import com.example.ui.theme.*

@Composable
fun AdminDashboardDialog(
    stats: PlatformStats?,
    users: List<UserEntity>,
    reports: List<ReportEntity>,
    tickets: List<SupportTicketEntity>,
    onDismiss: () -> Unit,
    onBanUser: (Long, String) -> Unit,
    onUnbanUser: (Long) -> Unit,
    onVerifyUser: (Long, Boolean) -> Unit,
    onReplyTicket: (String, String, String) -> Unit,
    onReviewReport: (Long, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Stats, 1: Users, 2: Reports, 3: Tickets
    var userSearchQuery by remember { mutableStateOf("") }
    var selectedTicketForReply by remember { mutableStateOf<SupportTicketEntity?>(null) }
    var replyText by remember { mutableStateOf("") }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        color = StarKingBgDark
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Star King Admin Control 🛡️",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "Real-time Platform Management & Moderation",
                        color = StarGoldLight,
                        fontSize = 11.sp
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("admin_close_btn")) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextWhite)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Navigation Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(StarKingSurfaceVariantDark)
                    .padding(3.dp)
            ) {
                listOf("Overview", "Users", "Reports", "Tickets").forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) StarGoldPrimary else Color.Transparent)
                            .clickable { selectedTab = index }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = if (isSelected) StarKingBgDark else TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (selectedTab) {
                0 -> {
                    // Overview Stats Cards
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            StatCard(
                                title = "Total Registered Users",
                                value = "${stats?.totalUsers ?: users.size}",
                                icon = "👥",
                                sub = "All permanent unique IDs"
                            )
                        }
                        item {
                            StatCard(
                                title = "Active Voice Rooms",
                                value = "${stats?.activeRooms ?: 5}",
                                icon = "🎙️",
                                sub = "Currently streaming parties"
                            )
                        }
                        item {
                            StatCard(
                                title = "Platform Gift Coin Volume",
                                value = "${String.format("%,d", stats?.totalGiftCoins ?: 950000L)} 🪙",
                                icon = "💰",
                                sub = "Total coins exchanged in gifts"
                            )
                        }
                        item {
                            StatCard(
                                title = "Open Support Tickets",
                                value = "${stats?.openTickets ?: tickets.count { it.status == "OPEN" }}",
                                icon = "🎫",
                                sub = "Awaiting customer support response"
                            )
                        }
                        item {
                            StatCard(
                                title = "Pending Moderation Reports",
                                value = "${stats?.pendingReports ?: reports.count { it.status == "PENDING" }}",
                                icon = "🚨",
                                sub = "User and room safety violation reports"
                            )
                        }
                    }
                }
                1 -> {
                    // Users Management
                    Column(modifier = Modifier.fillMaxSize()) {
                        OutlinedTextField(
                            value = userSearchQuery,
                            onValueChange = { userSearchQuery = it },
                            placeholder = { Text("Search by Nickname or numeric ID...", color = TextMuted, fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = StarGoldPrimary) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = StarGoldPrimary,
                                unfocusedBorderColor = StarKingCardBorder,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        val filteredUsers = users.filter {
                            userSearchQuery.isBlank() ||
                                    it.nickname.contains(userSearchQuery, ignoreCase = true) ||
                                    it.userId.toString().contains(userSearchQuery)
                        }

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredUsers, key = { it.userId }) { u ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = StarKingSurfaceVariantDark),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("admin_user_card_${u.userId}")
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            AvatarView(
                                                avatarUrl = u.avatarUrl,
                                                nickname = u.nickname,
                                                size = 38.dp,
                                                isOfficial = u.isOfficialVerified,
                                                level = u.level
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(text = u.nickname, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text(text = "ID: ${u.userId} • Role: ${u.role}", color = StarGoldLight, fontSize = 11.sp)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            // Toggle Official Verification
                                            Button(
                                                onClick = { onVerifyUser(u.userId, !u.isOfficialVerified) },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (u.isOfficialVerified) StarKingCardDark else StarGoldPrimary,
                                                    contentColor = if (u.isOfficialVerified) StarGoldLight else StarKingBgDark
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f).height(34.dp)
                                            ) {
                                                Text(
                                                    text = if (u.isOfficialVerified) "Remove Official" else "✓ Verify Official",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            // Ban / Unban
                                            Button(
                                                onClick = {
                                                    if (u.isBanned) onUnbanUser(u.userId)
                                                    else onBanUser(u.userId, "Violation of Star King community terms")
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (u.isBanned) LiveGreen else DangerRed,
                                                    contentColor = TextWhite
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f).height(34.dp)
                                            ) {
                                                Text(
                                                    text = if (u.isBanned) "Unban User" else "Ban Account",
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
                2 -> {
                    // Reports Desk
                    if (reports.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No violation reports pending. Platform is healthy! 🌟", color = TextMuted, fontSize = 13.sp)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(reports, key = { it.id }) { report ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = StarKingSurfaceVariantDark),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("admin_report_${report.id}")
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = "🚨 Reason: ${report.reasonCategory}", color = DangerRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(text = "Status: ${report.status}", color = StarGoldLight, fontSize = 11.sp)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "Reported by: ${report.reporterName} (ID: ${report.reporterUserId})", color = TextMuted, fontSize = 11.sp)
                                        if (report.reportedUserName != null) {
                                            Text(text = "Target User: ${report.reportedUserName} (ID: ${report.reportedUserId})", color = TextWhite, fontSize = 11.sp)
                                        }
                                        Text(text = "Details: ${report.details}", color = TextChampagne, fontSize = 12.sp)

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = { onReviewReport(report.id, "ACTION_TAKEN", "Violator cautioned / banned") },
                                                colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f).height(34.dp)
                                            ) {
                                                Text("Take Action", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Button(
                                                onClick = { onReviewReport(report.id, "DISMISSED", "No violation found") },
                                                colors = ButtonDefaults.buttonColors(containerColor = StarKingCardDark),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f).height(34.dp)
                                            ) {
                                                Text("Dismiss", fontSize = 11.sp, color = TextMuted)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // Tickets Desk
                    if (tickets.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No open support tickets.", color = TextMuted, fontSize = 13.sp)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(tickets, key = { it.ticketId }) { ticket ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = StarKingSurfaceVariantDark),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("admin_ticket_${ticket.ticketId}")
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = "[${ticket.category}] ${ticket.subject}", color = StarGoldLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(text = ticket.status, color = if (ticket.status == "RESOLVED") LiveGreen else StarGoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "From: ${ticket.userNickname} (ID: ${ticket.userId})", color = TextMuted, fontSize = 11.sp)
                                        Text(text = "Message: ${ticket.message}", color = TextWhite, fontSize = 12.sp)

                                        if (ticket.staffReply.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(text = "Staff Reply: ${ticket.staffReply}", color = NeonCyan, fontSize = 11.sp)
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Button(
                                            onClick = { selectedTicketForReply = ticket },
                                            colors = ButtonDefaults.buttonColors(containerColor = StarGoldPrimary, contentColor = StarKingBgDark),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth().height(34.dp)
                                        ) {
                                            Text("Reply & Update Ticket", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Reply Dialog
        if (selectedTicketForReply != null) {
            val tck = selectedTicketForReply!!
            AlertDialog(
                onDismissRequest = { selectedTicketForReply = null },
                containerColor = StarKingSurfaceDark,
                title = { Text("Reply to Ticket ${tck.ticketId}", color = TextWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "User inquiry: ${tck.message}", color = TextMuted, fontSize = 12.sp)
                        OutlinedTextField(
                            value = replyText,
                            onValueChange = { replyText = it },
                            placeholder = { Text("Type official resolution reply...", color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = StarGoldPrimary,
                                unfocusedBorderColor = StarKingCardBorder,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            ),
                            shape = RoundedCornerShape(10.dp),
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
                                replyText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StarGoldPrimary, contentColor = StarKingBgDark)
                    ) {
                        Text("Send & Resolve Ticket", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedTicketForReply = null }) {
                        Text("Cancel", color = TextMuted)
                    }
                }
            )
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, icon: String, sub: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = StarKingSurfaceVariantDark),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(StarGoldPrimary.copy(alpha = 0.4f), StarKingCardBorder))),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 32.sp)
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(text = title, color = TextMuted, fontSize = 12.sp)
                Text(text = value, color = TextWhite, fontWeight = FontWeight.Black, fontSize = 20.sp)
                Text(text = sub, color = TextSecondary, fontSize = 10.sp)
            }
        }
    }
}
