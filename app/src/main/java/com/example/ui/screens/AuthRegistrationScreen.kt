package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
fun AuthRegistrationScreen(
    onDismiss: () -> Unit,
    onRegister: (
        nickname: String,
        gender: String,
        country: String,
        dob: String,
        language: String,
        bio: String
    ) -> Unit,
    onQuickSwitchUser: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var mode by remember { mutableStateOf("LOGIN") } // LOGIN, REGISTER, PHONE_OTP
    var nickname by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Star") }
    var country by remember { mutableStateOf("United States") }
    var dob by remember { mutableStateOf("2000-01-01") }
    var language by remember { mutableStateOf("English") }
    var bio by remember { mutableStateOf("Ready to voice party on Star King! 🌟") }
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }

    val genderList = listOf("Male", "Female", "Star")
    val countryList = listOf("United States", "United Kingdom", "India", "Germany", "United Arab Emirates", "Indonesia")

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
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "⭐ STAR KING VOICE CHAT",
                    color = StarGoldPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp
                )
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("auth_close_btn")) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextWhite)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Connect • Talk • Party • Make Friends",
                color = TextChampagne,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (mode) {
                "LOGIN" -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Welcome to Star King",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Sign in or create your permanent numeric user ID.",
                            color = TextMuted,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Google Sign-In
                        Button(
                            onClick = { mode = "REGISTER" },
                            colors = ButtonDefaults.buttonColors(containerColor = StarKingSurfaceVariantDark),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("google_login_btn")
                        ) {
                            Text(text = "🌐 Continue with Google", color = TextWhite, fontWeight = FontWeight.Bold)
                        }

                        // Phone Number OTP Sign-In
                        Button(
                            onClick = { mode = "PHONE_OTP" },
                            colors = ButtonDefaults.buttonColors(containerColor = StarKingSurfaceVariantDark),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("phone_login_btn")
                        ) {
                            Text(text = "📱 Phone Number & OTP Verification", color = TextWhite, fontWeight = FontWeight.Bold)
                        }

                        // Guest Mode (Generates registered ID)
                        Button(
                            onClick = { mode = "REGISTER" },
                            colors = ButtonDefaults.buttonColors(containerColor = StarGoldPrimary, contentColor = StarKingBgDark),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("guest_mode_btn")
                        ) {
                            Text(text = "✨ Instant Guest Mode Registration", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(text = "Quick Switch Account (Testing Profiles):", color = TextMuted, fontSize = 12.sp)

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    onQuickSwitchUser(504094L)
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StarKingCardDark),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("StarRuler (Owner)", fontSize = 11.sp, color = StarGoldPrimary)
                            }
                            Button(
                                onClick = {
                                    onQuickSwitchUser(502110L)
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StarKingCardDark),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Aria (Host)", fontSize = 11.sp, color = TextWhite)
                            }
                            Button(
                                onClick = {
                                    onQuickSwitchUser(100001L)
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StarKingCardDark),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Official (99)", fontSize = 11.sp, color = TextWhite)
                            }
                        }
                    }
                }
                "PHONE_OTP" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = "Phone Number OTP Verification", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Phone Number (+1 / +91 / +971)", color = TextMuted) },
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

                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = { otpCode = it },
                            label = { Text("6-Digit OTP Code (Simulated: 123456)", color = TextMuted) },
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

                        Button(
                            onClick = { mode = "REGISTER" },
                            colors = ButtonDefaults.buttonColors(containerColor = StarGoldPrimary, contentColor = StarKingBgDark),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(46.dp)
                        ) {
                            Text("Verify OTP & Proceed to Profile Setup", fontWeight = FontWeight.Bold)
                        }

                        TextButton(onClick = { mode = "LOGIN" }) {
                            Text("Back to Login", color = TextMuted)
                        }
                    }
                }
                "REGISTER" -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Text(
                                text = "Profile Setup (Permanent User ID)",
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Your permanent 6-digit numeric User ID will be generated upon registration.",
                                color = StarGoldLight,
                                fontSize = 11.sp
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = nickname,
                                onValueChange = { nickname = it },
                                label = { Text("Nickname", color = TextMuted) },
                                placeholder = { Text("e.g. StarKingKing77", color = TextSecondary) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = StarGoldPrimary,
                                    unfocusedBorderColor = StarKingCardBorder,
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("auth_nickname_input")
                            )
                        }

                        item {
                            Text(text = "Gender", color = TextMuted, fontSize = 12.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                genderList.forEach { g ->
                                    val isSelected = g == gender
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) StarGoldPrimary else StarKingSurfaceVariantDark)
                                            .clickable { gender = g }
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text(text = g, color = if (isSelected) StarKingBgDark else TextWhite, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = dob,
                                onValueChange = { dob = it },
                                label = { Text("Date of Birth (YYYY-MM-DD)", color = TextMuted) },
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
                            OutlinedTextField(
                                value = bio,
                                onValueChange = { bio = it },
                                label = { Text("Bio", color = TextMuted) },
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
                            Button(
                                onClick = {
                                    onRegister(
                                        nickname.ifBlank { "StarUser" },
                                        gender,
                                        country,
                                        dob,
                                        language,
                                        bio
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StarGoldPrimary, contentColor = StarKingBgDark),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("submit_registration_btn")
                            ) {
                                Text("Complete Registration & Get 1,000 Coins 🪙", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        item {
                            TextButton(onClick = { mode = "LOGIN" }, modifier = Modifier.fillMaxWidth()) {
                                Text("Cancel", color = TextMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}
