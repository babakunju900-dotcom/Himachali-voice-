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
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Avatar Picker
                item {
                    Text(text = "Choose Avatar", color = TextMuted, fontSize = 12.sp)
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
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Bio
                item {
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Bio", color = TextMuted) },
                        minLines = 2,
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

                // Gender
                item {
                    Text(text = "Gender", color = TextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        genders.forEach { g ->
                            val isSelected = g == gender
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) StarGoldPrimary else StarKingSurfaceVariantDark)
                                    .clickable { gender = g }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(text = g, color = if (isSelected) StarKingBgDark else TextWhite, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Country
                item {
                    OutlinedTextField(
                        value = country,
                        onValueChange = { country = it },
                        label = { Text("Country / Region", color = TextMuted) },
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

                // Save Button
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            onSave(nickname, bio, gender, country, selectedAvatar)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StarGoldPrimary, contentColor = StarKingBgDark),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Save Profile Changes", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
