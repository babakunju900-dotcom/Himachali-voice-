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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.UserEntity
import com.example.ui.components.AvatarView
import com.example.ui.components.PhotoUploadModerationDialog
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoomDialog(
    hostUser: UserEntity? = null,
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
    // Current step: 1 = Room Fields Config, 2 = Room Preview
    var currentStep by remember { mutableIntStateOf(1) }

    var roomName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Party") }
    var seatCount by remember { mutableIntStateOf(8) }
    var welcomeMsg by remember { mutableStateOf("Welcome to Star Voice! Respect everyone and enjoy the party! 🌟") }
    var isPrivate by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var coverPhotoUrl by remember { mutableStateOf("") }
    var micPermission by remember { mutableStateOf("Open Mic") } // Open Mic, Host Approval, VIP Only
    var roomEntrySetting by remember { mutableStateOf("Free Entry") } // Free Entry, Level 3+, Approved Only

    var showCoverUploadDialog by remember { mutableStateOf(false) }

    val categories = listOf("Party", "Music", "Chat", "Gaming", "Dating", "Poetry", "Official")
    val micOptions = listOf("Open Mic", "Host Approval", "VIP Only")
    val entryOptions = listOf("Free Entry", "Level 3+", "Approved Only")
    val seatOptions = listOf(8, 10, 12)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = StarKingSurfaceDark,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with Step Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (currentStep == 1) "Create Voice Room 🎙️" else "Room Preview ✨",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = if (currentStep == 1) "Configure your voice stage settings" else "Review room appearance before launching",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF281D4C))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (currentStep == 1) "Step 1 of 2" else "Step 2 of 2",
                        color = StarGoldPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            AnimatedContent(
                targetState = currentStep,
                label = "room_create_anim",
                modifier = Modifier.weight(1f, fill = false)
            ) { step ->
                if (step == 1) {
                    // --- STEP 1: ROOM CONFIGURATION ---
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Room Name
                        item {
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
                        }

                        // Room Description
                        item {
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = { Text("Room Description", color = TextMuted) },
                                placeholder = { Text("Tell users what this party room is about 🎵", color = TextSecondary) },
                                maxLines = 2,
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

                        // Room Cover Photo Picker (Camera or Gallery with real-time moderation)
                        item {
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
                                            Text("AI Verified", color = LiveGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(105.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(StarKingCardDark)
                                        .border(1.5.dp, if (coverPhotoUrl.isNotBlank()) LiveGreen else StarKingCardBorder, RoundedCornerShape(14.dp))
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
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF281D4C)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = StarGoldPrimary)
                                            }
                                            Column {
                                                Text("Upload Room Cover (Gallery or Camera)", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Text("Automatic AI safety moderation checks all images", color = TextMuted, fontSize = 10.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Category Picker
                        item {
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
                        }

                        // Mic Permissions Setting
                        item {
                            Column {
                                Text(text = "Mic Permissions", color = TextMuted, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    micOptions.forEach { opt ->
                                        val isSelected = opt == micPermission
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSelected) Color(0xFFFF2B6D) else StarKingSurfaceVariantDark)
                                                .clickable { micPermission = opt }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = opt,
                                                color = Color.White,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Room Entry Setting
                        item {
                            Column {
                                Text(text = "Room Entry Setting", color = TextMuted, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    entryOptions.forEach { opt ->
                                        val isSelected = opt == roomEntrySetting
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSelected) NeonCyan else StarKingSurfaceVariantDark)
                                                .clickable { roomEntrySetting = opt }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = opt,
                                                color = if (isSelected) StarKingBgDark else Color.White,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Voice Seats
                        item {
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
                        }

                        // Public / Private Switch
                        item {
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
                        }

                        if (isPrivate) {
                            item {
                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = { Text("4-Digit Room Password", color = TextMuted) },
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
                        }
                    }
                } else {
                    // --- STEP 2: ROOM PREVIEW (Matching User Requirement 4) ---
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1535)),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.linearGradient(listOf(StarGoldPrimary, Color(0xFFFF2B6D)))
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Room Cover Image
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                                        .background(Brush.verticalGradient(listOf(Color(0xFF281D4C), Color(0xFF120E24)))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (coverPhotoUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = coverPhotoUrl,
                                            contentDescription = "Cover",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("🎙️ 🌟", fontSize = 36.sp)
                                            Text("Star Voice Live Room", color = StarGoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // Live Tag overlay
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(10.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(LiveGreen)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("● LIVE STAGE", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                    }
                                }

                                // Details content
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = roomName.ifBlank { "${hostUser?.nickname ?: "Host"}'s Voice Stage" },
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (isPrivate) {
                                            Text("🔒 Private", color = Color(0xFFFF80AB), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        } else {
                                            Text("🌐 Public", color = LiveGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = description.ifBlank { "Join my live voice party and connect with new friends!" },
                                        color = TextChampagne,
                                        fontSize = 12.sp
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Host Information Row
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(StarKingSurfaceDark)
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AvatarView(
                                            avatarUrl = hostUser?.avatarUrl ?: "avatar_user",
                                            nickname = hostUser?.nickname ?: "Host",
                                            size = 42.dp
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Host: ${hostUser?.nickname ?: "Star Voice Host"}",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = "Category: $selectedCategory • $seatCount Seats • $micPermission",
                                                color = TextMuted,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons: Back vs Continue / Create
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (currentStep == 2) {
                    OutlinedButton(
                        onClick = { currentStep = 1 },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(listOf(StarKingCardBorder, StarKingCardBorder))
                        ),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("Back / Edit", fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = {
                        if (currentStep == 1) {
                            currentStep = 2
                        } else {
                            onCreateRoom(
                                roomName.ifBlank { "${hostUser?.nickname ?: "Star"}'s Voice Stage" },
                                description.ifBlank { "Join my voice party and let's talk!" },
                                selectedCategory,
                                "English",
                                seatCount,
                                welcomeMsg,
                                isPrivate,
                                password,
                                coverPhotoUrl
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StarGoldPrimary, contentColor = StarKingBgDark),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("submit_create_room_btn")
                ) {
                    Text(
                        text = if (currentStep == 1) "Review Room >" else "Create Room 🎙️",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
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
