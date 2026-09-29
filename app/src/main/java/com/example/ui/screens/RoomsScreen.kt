package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    val filtered = rooms.filter { room ->
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
            contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp)
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

            // Rooms count
            item {
                Text(
                    text = "Active Voice Rooms (${filtered.size})",
                    color = TextMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (filtered.isEmpty()) {
                item {
                    EmptyStateView(
                        iconEmoji = "🔍",
                        title = "No Voice Rooms Found",
                        description = if (searchQuery.isNotBlank()) "No rooms match '$searchQuery'. Try another term or create your own!" else "No live rooms currently in this category.",
                        actionButtonText = "Create Voice Room",
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
