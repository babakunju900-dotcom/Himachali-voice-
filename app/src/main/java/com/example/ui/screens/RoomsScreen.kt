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
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RoomEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.*

@Composable
fun RoomsScreen(
    rooms: List<RoomEntity>,
    searchQuery: String,
    selectedCategory: String,
    onSearchChange: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onRoomClick: (RoomEntity) -> Unit,
    onCreateRoomClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf("All", "Party", "Music", "Chat", "Gaming", "Dating", "Official")

    // Permanent Official Support Room
    val officialRoom = remember(rooms) {
        rooms.find { it.roomId == 708101L || it.name.contains("Customer Support", ignoreCase = true) }
    }

    val communityRooms = remember(rooms) {
        rooms.filterNot { it.roomId == 708101L || it.name.contains("Customer Support", ignoreCase = true) }
    }

    val filtered = communityRooms.filter { room ->
        val matchesCat = selectedCategory == "All" || room.category.equals(selectedCategory, ignoreCase = true)
        val matchesQuery = searchQuery.isBlank() ||
                room.name.contains(searchQuery, ignoreCase = true) ||
                room.roomId.toString().contains(searchQuery) ||
                room.hostName.contains(searchQuery, ignoreCase = true)
        matchesCat && matchesQuery
    }

    Box(modifier = modifier.fillMaxSize().background(StarKingBgDark)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp)
        ) {
            // Search TextField
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search by Room Name, ID or Host...", color = TextMuted, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = StarGoldPrimary)
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = StarKingSurfaceVariantDark,
                        unfocusedContainerColor = StarKingSurfaceVariantDark,
                        focusedBorderColor = StarGoldPrimary,
                        unfocusedBorderColor = StarKingCardBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rooms_search_input")
                )
            }

            // --- 1. DEDICATED OFFICIAL ROOM SECTION ---
            if (officialRoom != null && (selectedCategory == "All" || selectedCategory == "Official")) {
                item {
                    Text(
                        text = "Official Platform Room 👑",
                        color = StarGoldLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        onClick = { onRoomClick(officialRoom) },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2B27)),
                        shape = RoundedCornerShape(18.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(
                                listOf(StarGoldPrimary, LiveGreen, StarGoldDark)
                            )
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("rooms_official_support_card")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(Brush.radialGradient(listOf(StarGoldPrimary, Color(0xFF004D40)))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("⭐", fontSize = 26.sp)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = officialRoom.name,
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 15.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(Icons.Default.Verified, contentDescription = "Verified", tint = StarGoldPrimary, modifier = Modifier.size(16.dp))
                                    }

                                    Spacer(modifier = Modifier.height(3.dp))

                                    Text(
                                        text = "Official STAR VOICE 24/7 Verified Support & Inquiries",
                                        color = TextChampagne,
                                        fontSize = 11.sp
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(StarGoldPrimary)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("OFFICIAL", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Black)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("● ${officialRoom.onlineCount} online", color = LiveGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Button(
                                onClick = { onRoomClick(officialRoom) },
                                colors = ButtonDefaults.buttonColors(containerColor = LiveGreen, contentColor = Color.Black),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.HeadsetMic, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Enter", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Categories horizontal bar
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        val isSelected = cat.equals(selectedCategory, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) StarGoldPrimary else StarKingSurfaceVariantDark)
                                .clickable { onCategorySelect(cat) }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                .testTag("rooms_cat_$cat")
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

            // Community Rooms count
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Community Voice Rooms (${filtered.size})",
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Real-time updates ⚡",
                        color = StarGoldLight,
                        fontSize = 11.sp
                    )
                }
            }

            if (filtered.isEmpty()) {
                item {
                    EmptyStateView(
                        iconEmoji = "🎙️",
                        title = "No Voice Rooms in this Category",
                        description = if (searchQuery.isNotBlank()) "No rooms match '$searchQuery'. Create your own room or search another category!" else "Be the first to start a live voice party in $selectedCategory!",
                        actionButtonText = "Create Live Voice Room 🎙️",
                        onActionClick = onCreateRoomClick
                    )
                }
            } else {
                items(filtered, key = { it.roomId }) { room ->
                    RoomCard(room = room, onClick = { onRoomClick(room) })
                }
            }
        }

        // Create Room FAB
        FloatingActionButton(
            onClick = onCreateRoomClick,
            containerColor = StarGoldPrimary,
            contentColor = StarKingBgDark,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 90.dp)
                .testTag("rooms_create_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Create Room")
        }
    }
}
