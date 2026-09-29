package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    val languages = listOf("English", "Hindi", "Bengali", "Nepali", "Urdu", "Indonesian", "Arabic")
    var micSensitivity by remember { mutableFloatStateOf(0.7f) }
    var soundEffects by remember { mutableStateOf(true) }

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
                    text = "Settings ⚙️",
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

                // Account Actions
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onLogout,
                            colors = ButtonDefaults.buttonColors(containerColor = StarKingSurfaceVariantDark),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Log Out", color = TextWhite, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onDeleteAccount,
                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed.copy(alpha = 0.8f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Delete Account & Clear Data", color = TextWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
