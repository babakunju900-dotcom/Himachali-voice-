package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.PhotoUploadModerationDialog
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
        password: String,
        coverPhotoUrl: String
    ) -> Unit
) {
    var roomName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Party") }
    var seatCount by remember { mutableIntStateOf(8) }
    var welcomeMsg by remember { mutableStateOf("Welcome to Star Voice! Respect everyone and enjoy the party! 🌟") }
    var isPrivate by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var coverPhotoUrl by remember { mutableStateOf("") }
    var showCoverUploadDialog by remember { mutableStateOf(false) }

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

            // Room Cover Photo Picker (Real-Time Moderated)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Room Cover Photo", color = TextMuted, fontSize = 12.sp)
                    if (coverPhotoUrl.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = LiveGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Safety Verified", color = LiveGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(95.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(StarKingCardDark)
                        .border(1.dp, if (coverPhotoUrl.isNotBlank()) LiveGreen else StarKingCardBorder, RoundedCornerShape(14.dp))
                        .clickable { showCoverUploadDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (coverPhotoUrl.isNotBlank()) {
                        AsyncImage(
                            model = coverPhotoUrl,
                            contentDescription = "Room Cover Preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.7f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Change Cover 🖼️", color = TextWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = StarGoldPrimary)
                            Column {
                                Text("Upload Room Cover from Gallery", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Real-time safety scan protects against explicit images", color = TextMuted, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

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
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                .testTag("cat_option_$cat")
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

            // Seat Count Selection
            Column {
                Text(text = "Voice Seats", color = TextMuted, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    seatOptions.forEach { seats ->
                        val isSelected = seats == seatCount
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) StarGoldPrimary else StarKingSurfaceVariantDark)
                                .clickable { seatCount = seats }
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$seats Seats",
                                color = if (isSelected) StarKingBgDark else TextWhite,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Privacy Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Private Room 🔒", color = TextWhite, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    Text(text = "Requires password to join", color = TextMuted, fontSize = 10.sp)
                }
                Switch(
                    checked = isPrivate,
                    onCheckedChange = { isPrivate = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = StarGoldPrimary)
                )
            }

            if (isPrivate) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("4-Digit Password", color = TextMuted) },
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
                        roomName.ifBlank { "Star Voice Party Stage" },
                        description.ifBlank { "Join my voice party and let's talk!" },
                        selectedCategory,
                        "English",
                        seatCount,
                        welcomeMsg,
                        isPrivate,
                        password,
                        coverPhotoUrl
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

    // Modal for selecting and moderating Room Cover Photo
    if (showCoverUploadDialog) {
        PhotoUploadModerationDialog(
            title = "Room Cover Photo",
            subtitle = "Upload an attractive room cover image. Real-time AI moderation checks against adult/explicit content.",
            isCircularPreview = false,
            currentPhotoUrl = coverPhotoUrl,
            onDismiss = { showCoverUploadDialog = false },
            onPhotoApprovedAndSaved = { approvedUri ->
                coverPhotoUrl = approvedUri
                showCoverUploadDialog = false
            }
        )
    }
}
