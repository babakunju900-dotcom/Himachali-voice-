package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserEntity
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
    user: UserEntity? = null,
    deviceInfo: String = "",
    modifier: Modifier = Modifier
) {
    var micSensitivity by remember { mutableFloatStateOf(0.7f) }
    var soundEffects by remember { mutableStateOf(true) }

    var isPrivateProfile by remember { mutableStateOf(false) }
    var hideAge by remember { mutableStateOf(false) }
    var hideCountry by remember { mutableStateOf(false) }
    var allowMessages by remember { mutableStateOf(true) }
    var allowProfileSharing by remember { mutableStateOf(true) }
    var defaultRoomPrivacy by remember { mutableStateOf("Public") }
    val languages = listOf("English", "Hindi", "Bengali", "Nepali", "Urdu", "Indonesian", "Arabic")

    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteAccountConfirmDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        color = StarKingBgDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Settings & Security ⚙️",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("settings_close_btn")) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextWhite)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Account & Authentication Section
                if (user != null) {
                    item {
                        Text(
                            text = "Account & Authentication",
                            color = StarGoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = StarKingSurfaceVariantDark),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Permanent User ID", color = TextMuted, fontSize = 12.sp)
                                    Text(
                                        text = "${user.userId}",
                                        color = StarGoldPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                HorizontalDivider(color = StarKingCardBorder.copy(alpha = 0.5f))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Auth Provider", color = TextMuted, fontSize = 12.sp)
                                    Text(
                                        text = user.authProvider.ifBlank { "GUEST" },
                                        color = TextWhite,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                }
                                if (user.authIdentifier.isNotBlank()) {
                                    HorizontalDivider(color = StarKingCardBorder.copy(alpha = 0.5f))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "Linked Credential", color = TextMuted, fontSize = 12.sp)
                                        Text(
                                            text = user.authIdentifier,
                                            color = TextChampagne,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                HorizontalDivider(color = StarKingCardBorder.copy(alpha = 0.5f))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Account Role", color = TextMuted, fontSize = 12.sp)
                                    Text(
                                        text = user.role,
                                        color = if (user.role == "USER") TextWhite else StarGoldLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    // Device & Active Session Management
                    item {
                        Text(
                            text = "Active Session & Device Security",
                            color = StarGoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = StarKingSurfaceVariantDark),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(LiveGreen)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Current Active Session", color = LiveGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Text(
                                    text = if (deviceInfo.isNotBlank()) deviceInfo else user.deviceInfo.ifBlank { "Android Mobile Client" },
                                    color = TextWhite,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Session Token: ${user.sessionId.ifBlank { "SK-SES-SECURE" }}",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                // Multilingual selector
                item {
                    Text(text = "Language / भाषा / لغة", color = StarGoldPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(StarKingSurfaceVariantDark)
                    ) {
                        languages.forEach { lang ->
                            val isSelected = lang.equals(currentLanguage, ignoreCase = true)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onLanguageChange(lang) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = lang,
                                    color = if (isSelected) StarGoldPrimary else TextWhite,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                                if (isSelected) {
                                    Text(text = "✓", color = StarGoldPrimary, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }

                // Voice & Sound
                item {
                    Text(text = "Voice & Sound", color = StarGoldPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StarKingSurfaceVariantDark),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "In-room Sound Effects", color = TextWhite, fontSize = 13.sp)
                                Switch(
                                    checked = soundEffects,
                                    onCheckedChange = { soundEffects = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = StarGoldPrimary, checkedTrackColor = StarKingCardDark)
                                )
                            }
                            Text(text = "Microphone Sensitivity: ${(micSensitivity * 100).toInt()}%", color = TextMuted, fontSize = 11.sp)
                            Slider(
                                value = micSensitivity,
                                onValueChange = { micSensitivity = it },
                                colors = SliderDefaults.colors(thumbColor = StarGoldPrimary, activeTrackColor = StarGoldPrimary)
                            )
                        }
                    }
                }

                // Profile & Room Privacy Controls (Requirement 11)
                item {
                    Text(text = "Profile & Room Privacy 🔒", color = StarGoldPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StarKingSurfaceVariantDark),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Public Profile", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    Text(text = "Allow others to discover and view your profile", color = TextMuted, fontSize = 10.sp)
                                }
                                Switch(
                                    checked = !isPrivateProfile,
                                    onCheckedChange = { isPrivateProfile = !it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = StarGoldPrimary)
                                )
                            }

                            HorizontalDivider(color = StarKingCardBorder.copy(alpha = 0.5f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Hide Age", color = TextWhite, fontSize = 13.sp)
                                Switch(
                                    checked = hideAge,
                                    onCheckedChange = { hideAge = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = StarGoldPrimary)
                                )
                            }

                            HorizontalDivider(color = StarKingCardBorder.copy(alpha = 0.5f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Hide Country & Flag", color = TextWhite, fontSize = 13.sp)
                                Switch(
                                    checked = hideCountry,
                                    onCheckedChange = { hideCountry = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = StarGoldPrimary)
                                )
                            }

                            HorizontalDivider(color = StarKingCardBorder.copy(alpha = 0.5f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Allow Private Messages", color = TextWhite, fontSize = 13.sp)
                                    Text(text = "Receive direct messages from community members", color = TextMuted, fontSize = 10.sp)
                                }
                                Switch(
                                    checked = allowMessages,
                                    onCheckedChange = { allowMessages = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = StarGoldPrimary)
                                )
                            }

                            HorizontalDivider(color = StarKingCardBorder.copy(alpha = 0.5f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Allow Profile Sharing", color = TextWhite, fontSize = 13.sp)
                                Switch(
                                    checked = allowProfileSharing,
                                    onCheckedChange = { allowProfileSharing = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = StarGoldPrimary)
                                )
                            }

                            HorizontalDivider(color = StarKingCardBorder.copy(alpha = 0.5f))

                            Column {
                                Text(text = "Default Room Privacy", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("Public", "Private", "Password", "Approved Only").forEach { opt ->
                                        val isSel = opt == defaultRoomPrivacy
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) StarGoldPrimary else Color(0xFF231B45))
                                                .clickable { defaultRoomPrivacy = opt }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = opt,
                                                color = if (isSel) StarKingBgDark else TextWhite,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Legal & Safety
                item {
                    Text(text = "Safety & Legal", color = StarGoldPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StarKingSurfaceVariantDark),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "• Terms of Service (App Store & Safe Harbor Compliant)", color = TextChampagne, fontSize = 12.sp)
                            Text(text = "• Privacy Policy (Zero unauthorized third-party disclosure)", color = TextChampagne, fontSize = 12.sp)
                            Text(text = "• Community Guidelines (Zero tolerance for harassment)", color = TextChampagne, fontSize = 12.sp)
                            Text(text = "• Age Requirement: 13+ Community Platform", color = TextMuted, fontSize = 11.sp)
                        }
                    }
                }

                // Account Actions: Logout & Delete Account
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { showLogoutConfirmDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = StarKingSurfaceVariantDark),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("logout_btn")
                        ) {
                            Text(text = "Log Out from Device", color = TextWhite, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showDeleteAccountConfirmDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed.copy(alpha = 0.85f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("delete_account_btn")
                        ) {
                            Text(text = "Delete Account & Permanently Erase Data", color = TextWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Confirmation Dialog: Logout
        if (showLogoutConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutConfirmDialog = false },
                title = { Text("Log Out?", color = TextWhite, fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to log out of Star King Voice Chat? Your session will end.", color = TextMuted) },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutConfirmDialog = false
                            onLogout()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StarGoldPrimary, contentColor = StarKingBgDark)
                    ) {
                        Text("Log Out", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutConfirmDialog = false }) {
                        Text("Cancel", color = TextMuted)
                    }
                },
                containerColor = StarKingSurfaceDark,
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Confirmation Dialog: Delete Account
        if (showDeleteAccountConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteAccountConfirmDialog = false },
                icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = DangerRed) },
                title = { Text("Delete Account Permanently?", color = DangerRed, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        text = "This action CANNOT be undone. Your permanent User ID (${user?.userId}), wallet coin balance, virtual gifts received, and profile history will be permanently deleted from Star King.",
                        color = TextMuted
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteAccountConfirmDialog = false
                            onDeleteAccount()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                    ) {
                        Text("Permanently Delete", color = TextWhite, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteAccountConfirmDialog = false }) {
                        Text("Keep My Account", color = TextWhite)
                    }
                },
                containerColor = StarKingSurfaceDark,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
