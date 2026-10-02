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
import com.example.model.*
import com.example.ui.components.AvatarView
import com.example.ui.theme.*
import com.example.util.CountryHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CpScreen(
    currentUserId: Long,
    allUsers: List<UserEntity>,
    connections: List<CpConnectionEntity>,
    onDismiss: () -> Unit,
    onSendRequest: (UserEntity) -> Unit,
    onAcceptRequest: (String) -> Unit,
    onDeclineRequest: (String) -> Unit,
    onEndConnection: (String) -> Unit,
    onBlockUser: (String) -> Unit,
    onStartCall: (UserEntity, Boolean) -> Unit,
    onReportUser: (UserEntity) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") } // All, Available, My CP, Requests
    var searchQuery by remember { mutableStateOf("") }

    val activeCpConnections = remember(connections) {
        connections.filter { it.status == CpStatus.ACCEPTED.name }
    }

    val pendingRequests = remember(connections, currentUserId) {
        connections.filter { it.status == CpStatus.REQUEST_SENT.name }
    }

    // Filter available candidates (excluding self, banned, suspended)
    val eligibleUsers = remember(allUsers, connections, currentUserId, searchQuery, selectedFilter) {
        allUsers.filter { u ->
            u.userId != currentUserId && !u.isBanned && !u.isSuspended &&
                (searchQuery.isBlank() || u.nickname.contains(searchQuery, ignoreCase = true) || u.userId.toString().contains(searchQuery))
        }
    }

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
                .fillMaxHeight(0.92f)
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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color(0xFFE91E63), Color(0xFFFF4081)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💖", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "STAR VOICE CP",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFFF4081))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("DIRECT", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(
                            text = "1-on-1 Direct Partner Connection & Calling",
                            color = TextChampagne,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFF281D4C))
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name or User ID...", color = TextMuted, fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = StarGoldPrimary, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                },
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

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val filters = listOf("All", "Available", "Requests (${pendingRequests.size})", "My CP (${activeCpConnections.size})")
                items(filters) { filter ->
                    val isSelected = selectedFilter.startsWith(filter.substringBefore(" ("))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected)
                                    Brush.horizontalGradient(listOf(Color(0xFFE91E63), Color(0xFFFF4081)))
                                else
                                    Brush.horizontalGradient(listOf(Color(0xFF1E1738), Color(0xFF1E1738)))
                            )
                            .border(1.dp, if (isSelected) Color(0xFFFF80AB) else Color(0xFF332A55), RoundedCornerShape(12.dp))
                            .clickable { selectedFilter = filter.substringBefore(" (") }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = filter,
                            color = if (isSelected) Color.White else TextMuted,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Content List
            when (selectedFilter) {
                "Requests" -> {
                    if (pendingRequests.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(top = 40.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("💌", fontSize = 42.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No pending CP requests", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("When someone asks to connect with you, it will appear here.", color = TextMuted, fontSize = 11.sp)
                            }
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(pendingRequests) { req ->
                                val isIncoming = req.requesterId != currentUserId
                                val partnerName = if (req.user1Id == currentUserId) req.user2Name else req.user1Name
                                val partnerAvatar = if (req.user1Id == currentUserId) req.user2Avatar else req.user1Avatar

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1F183C)),
                                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(0xFFE91E63), Color(0xFF673AB7)))),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AvatarView(avatarUrl = partnerAvatar, nickname = partnerName, size = 50.dp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(partnerName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(
                                                if (isIncoming) "Sent you a CP partner request 💞" else "Waiting for their response... ⏳",
                                                color = TextChampagne,
                                                fontSize = 11.sp
                                            )
                                        }

                                        if (isIncoming) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                FilledTonalButton(
                                                    onClick = { onDeclineRequest(req.connectionId) },
                                                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF352750), contentColor = Color(0xFFFF80AB)),
                                                    shape = RoundedCornerShape(10.dp),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                    modifier = Modifier.height(34.dp)
                                                ) {
                                                    Text("Decline", fontSize = 11.sp)
                                                }
                                                Button(
                                                    onClick = { onAcceptRequest(req.connectionId) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63), contentColor = Color.White),
                                                    shape = RoundedCornerShape(10.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                    modifier = Modifier.height(34.dp)
                                                ) {
                                                    Text("Accept 💖", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        } else {
                                            OutlinedButton(
                                                onClick = { onEndConnection(req.connectionId) },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.height(34.dp)
                                            ) {
                                                Text("Cancel", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "My CP" -> {
                    if (activeCpConnections.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(top = 40.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("💔", fontSize = 42.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No active CP partner yet", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Find an eligible partner from the Available list and tap Connect!", color = TextMuted, fontSize = 11.sp)
                            }
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(activeCpConnections) { cp ->
                                val partnerId = if (cp.user1Id == currentUserId) cp.user2Id else cp.user1Id
                                val partnerName = if (cp.user1Id == currentUserId) cp.user2Name else cp.user1Name
                                val partnerAvatar = if (cp.user1Id == currentUserId) cp.user2Avatar else cp.user1Avatar
                                val partnerUser = allUsers.find { it.userId == partnerId }

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF231846)),
                                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(StarGoldPrimary, Color(0xFFE91E63)))),
                                    shape = RoundedCornerShape(18.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            AvatarView(avatarUrl = partnerAvatar, nickname = partnerName, size = 56.dp)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(partnerName, color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("💍", fontSize = 14.sp)
                                                }
                                                Text("CP ID: STAR-$partnerId • Intimacy ${cp.intimacyScore} 🌟", color = StarGoldLight, fontSize = 11.sp)
                                                Text("Status: ACCEPTED & CONNECTED", color = LiveGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Action buttons for active CP
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    if (partnerUser != null) {
                                                        onStartCall(partnerUser, false)
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = LiveGreen, contentColor = Color.Black),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.weight(1f).height(38.dp)
                                            ) {
                                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(15.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Voice Call", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            }

                                            Button(
                                                onClick = {
                                                    if (partnerUser != null) {
                                                        onStartCall(partnerUser, true)
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.weight(1f).height(38.dp)
                                            ) {
                                                Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(15.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Video Call", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            }

                                            OutlinedButton(
                                                onClick = { onEndConnection(cp.connectionId) },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.height(38.dp)
                                            ) {
                                                Text("End CP", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                else -> {
                    // All / Available candidates
                    if (eligibleUsers.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(top = 40.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Text("No users found matching your search.", color = TextMuted, fontSize = 13.sp)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(eligibleUsers) { candidate ->
                                val existingConn = connections.find {
                                    (it.user1Id == currentUserId && it.user2Id == candidate.userId) ||
                                    (it.user2Id == currentUserId && it.user1Id == candidate.userId)
                                }

                                val status = existingConn?.status ?: CpStatus.AVAILABLE.name
                                val flag = CountryHelper.getFlag(candidate.country)

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1535)),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = Brush.linearGradient(
                                            if (status == CpStatus.ACCEPTED.name)
                                                listOf(StarGoldPrimary, Color(0xFFE91E63))
                                            else
                                                listOf(Color(0xFF332A55), Color(0xFF332A55))
                                        )
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("cp_user_card_${candidate.userId}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // User Avatar with Online Dot
                                        Box(contentAlignment = Alignment.BottomEnd) {
                                            AvatarView(
                                                avatarUrl = candidate.avatarUrl,
                                                nickname = candidate.nickname,
                                                size = 54.dp,
                                                vipTier = candidate.vipTier,
                                                isOfficial = candidate.isOfficialVerified
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clip(CircleShape)
                                                    .background(if (status == CpStatus.BUSY.name) Color(0xFFFF9800) else LiveGreen)
                                                    .border(2.dp, StarKingSurfaceDark, CircleShape)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = candidate.nickname,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (candidate.isOfficialVerified) {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("✓", color = StarGoldPrimary, fontWeight = FontWeight.Black, fontSize = 12.sp)
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(2.dp))

                                            Text(
                                                text = "ID: STAR-${candidate.userId} • $flag ${candidate.country}",
                                                color = TextMuted,
                                                fontSize = 11.sp
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))

                                            // Status Badge
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(
                                                        when (status) {
                                                            CpStatus.ACCEPTED.name -> Color(0xFFE91E63)
                                                            CpStatus.REQUEST_SENT.name -> Color(0xFF9C27B0)
                                                            CpStatus.BUSY.name -> Color(0xFFE65100)
                                                            else -> LiveGreen.copy(alpha = 0.2f)
                                                        }
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = status,
                                                    color = if (status == CpStatus.AVAILABLE.name) LiveGreen else Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        // Action Button
                                        Column(
                                            horizontalAlignment = Alignment.End,
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            when (status) {
                                                CpStatus.ACCEPTED.name -> {
                                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        IconButton(
                                                            onClick = { onStartCall(candidate, false) },
                                                            modifier = Modifier.size(36.dp).clip(CircleShape).background(LiveGreen)
                                                        ) {
                                                            Icon(Icons.Default.Call, contentDescription = "Call", tint = Color.Black, modifier = Modifier.size(18.dp))
                                                        }
                                                        IconButton(
                                                            onClick = { onStartCall(candidate, true) },
                                                            modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFF00E5FF))
                                                        ) {
                                                            Icon(Icons.Default.Videocam, contentDescription = "Video", tint = Color.Black, modifier = Modifier.size(18.dp))
                                                        }
                                                    }
                                                }

                                                CpStatus.REQUEST_SENT.name -> {
                                                    OutlinedButton(
                                                        onClick = {
                                                            if (existingConn != null) {
                                                                onEndConnection(existingConn.connectionId)
                                                            }
                                                        },
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF80AB)),
                                                        shape = RoundedCornerShape(10.dp),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                        modifier = Modifier.height(34.dp)
                                                    ) {
                                                        Text("Sent ⏳", fontSize = 11.sp)
                                                    }
                                                }

                                                else -> {
                                                    Button(
                                                        onClick = { onSendRequest(candidate) },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63), contentColor = Color.White),
                                                        shape = RoundedCornerShape(10.dp),
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                        modifier = Modifier.height(34.dp).testTag("connect_btn_${candidate.userId}")
                                                    ) {
                                                        Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(13.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Connect", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }

                                            // Options popup / direct Call icon
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                IconButton(
                                                    onClick = { onStartCall(candidate, false) },
                                                    modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0xFF281D4C))
                                                ) {
                                                    Icon(Icons.Default.PhoneInTalk, contentDescription = "Direct Call", tint = StarGoldLight, modifier = Modifier.size(14.dp))
                                                }
                                                IconButton(
                                                    onClick = { onReportUser(candidate) },
                                                    modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0xFF281D4C))
                                                ) {
                                                    Icon(Icons.Default.Report, contentDescription = "Report", tint = TextMuted, modifier = Modifier.size(14.dp))
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
        }
    }
}
