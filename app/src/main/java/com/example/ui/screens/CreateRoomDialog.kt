package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoomDialog(
    onDismiss: () -> Unit,
    onCreateRoom: (
        name: String,
        description: String,
        category: String,
        language: String,
        seatCount: Int,
        welcomeMsg: String,
        isPrivate: Boolean,
        password: String
    ) -> Unit
) {
    var roomName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Party") }
    var seatCount by remember { mutableIntStateOf(8) }
    var welcomeMsg by remember { mutableStateOf("Welcome to Star King! Respect everyone and enjoy the party! 🌟") }
    var isPrivate by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }

    val categories = listOf("Party", "Music", "Chat", "Gaming", "Dating", "Poetry", "Official")
    val seatOptions = listOf(8, 10, 12)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = StarKingSurfaceDark,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Create Voice Party Room 🎙️",
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )

            // Room Name
            OutlinedTextField(
                value = roomName,
                onValueChange = { roomName = it },
                label = { Text("Room Name", color = TextMuted) },
                placeholder = { Text("e.g. Royal Acoustic Night Party", color = TextSecondary) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StarGoldPrimary,
                    unfocusedBorderColor = StarKingCardBorder,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("create_room_name_input")
            )

            // Category Picker
            Column {
                Text(text = "Category", color = TextMuted, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        val isSelected = cat == selectedCategory
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) StarGoldPrimary else StarKingSurfaceVariantDark)
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) StarKingBgDark else TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Mic Seats Selection (8, 10, 12 seats)
            Column {
                Text(text = "Mic Seats Configuration", color = TextMuted, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    seatOptions.forEach { count ->
                        val isSelected = seatCount == count
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) StarGoldPrimary else StarKingSurfaceVariantDark)
                                .clickable { seatCount = count }
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$count Seats",
                                color = if (isSelected) StarKingBgDark else TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Welcome Message
            OutlinedTextField(
                value = welcomeMsg,
                onValueChange = { welcomeMsg = it },
                label = { Text("Welcome Announcement", color = TextMuted) },
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

            // Private toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Private Room (Password Protected)", color = TextWhite, fontSize = 13.sp)
                Switch(
                    checked = isPrivate,
                    onCheckedChange = { isPrivate = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = StarGoldPrimary, checkedTrackColor = StarKingCardDark)
                )
            }

            if (isPrivate) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Room Password", color = TextMuted) },
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
            }

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = {
                    onCreateRoom(
                        roomName.ifBlank { "Star King Party Stage" },
                        description.ifBlank { "Join my voice party and let's talk!" },
                        selectedCategory,
                        "English",
                        seatCount,
                        welcomeMsg,
                        isPrivate,
                        password
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = StarGoldPrimary, contentColor = StarKingBgDark),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("submit_create_room_btn")
            ) {
                Text("Start Live Voice Room 🎙️", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
