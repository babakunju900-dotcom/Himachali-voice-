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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserEntity
import com.example.ui.components.AvatarView
import com.example.ui.components.PhotoUploadModerationDialog
import com.example.ui.theme.*

@Composable
fun EditProfileDialog(
    user: UserEntity?,
    onDismiss: () -> Unit,
    onSave: (nickname: String, bio: String, gender: String, country: String, avatarUrl: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var nickname by remember { mutableStateOf(user?.nickname ?: "") }
    var bio by remember { mutableStateOf(user?.bio ?: "") }
    var gender by remember { mutableStateOf(user?.gender ?: "Star") }
    var country by remember { mutableStateOf(user?.country ?: "United States") }
    var selectedAvatar by remember { mutableStateOf(user?.avatarUrl ?: "avatar_user") }
    var showPhotoUploadDialog by remember { mutableStateOf(false) }

    val avatars = listOf("avatar_user", "avatar_crown", "avatar_aria", "avatar_viktor", "avatar_layla", "avatar_support")
    val genders = listOf("Male", "Female", "Star")

    Surface(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        color = StarKingBgDark
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Edit Profile ✏️",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("edit_profile_close_btn")) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextWhite)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Direct Device / Gallery Photo Upload Card
                item {
                    Text(text = "Profile Photo (Direct Upload)", color = TextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(StarKingSurfaceVariantDark)
                            .border(1.dp, StarGoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .clickable { showPhotoUploadDialog = true }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            AvatarView(
                                avatarUrl = selectedAvatar,
                                nickname = nickname.ifEmpty { "User" },
                                size = 64.dp
                            )
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(StarGoldPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = "Upload",
                                    tint = StarKingBgDark,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Upload Photo from Gallery 📸", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("AI safety inspection automatically verifies photo in real time", color = TextChampagne, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Tap to select photo >", color = StarGoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Avatar Presets Picker
                item {
                    Text(text = "Or Choose Preset Avatar", color = TextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(avatars) { av ->
                            val isSelected = av == selectedAvatar
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .border(2.dp, if (isSelected) StarGoldPrimary else StarKingCardBorder, CircleShape)
                                    .clickable { selectedAvatar = av }
                                    .padding(4.dp)
                            ) {
                                AvatarView(
                                    avatarUrl = av,
                                    nickname = nickname.ifEmpty { "User" },
                                    size = 46.dp
                                )
                            }
                        }
                    }
                }

                // Nickname
                item {
                    OutlinedTextField(
                        value = nickname,
                        onValueChange = { nickname = it },
                        label = { Text("Nickname", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StarGoldPrimary,
                            unfocusedBorderColor = StarKingCardBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("edit_nickname_input")
                    )
                }

                // Bio
                item {
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Bio", color = TextMuted) },
                        placeholder = { Text("Tell everyone what you love to talk about 🌟", color = TextSecondary) },
                        maxLines = 3,
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

                // Gender Selection
                item {
                    Text(text = "Gender", color = TextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        genders.forEach { g ->
                            val isSelected = g == gender
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) StarGoldPrimary else StarKingSurfaceVariantDark)
                                    .clickable { gender = g }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = g,
                                    color = if (isSelected) StarKingBgDark else TextWhite,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // Country
                item {
                    OutlinedTextField(
                        value = country,
                        onValueChange = { country = it },
                        label = { Text("Country", color = TextMuted) },
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

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            onSave(nickname.ifEmpty { "StarUser" }, bio, gender, country, selectedAvatar)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StarGoldPrimary, contentColor = StarKingBgDark),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_profile_btn")
                    ) {
                        Text("Save Profile Changes 💾", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Direct Photo Upload Dialog with Automatic Safety Moderation
    if (showPhotoUploadDialog) {
        PhotoUploadModerationDialog(
            title = "Upload Profile Photo",
            subtitle = "Select your profile photo from your device. Real-time automatic safety moderation checks against inappropriate or explicit imagery.",
            isCircularPreview = true,
            currentPhotoUrl = selectedAvatar,
            onDismiss = { showPhotoUploadDialog = false },
            onPhotoApprovedAndSaved = { approvedUri ->
                selectedAvatar = approvedUri
                showPhotoUploadDialog = false
            }
        )
    }
}
