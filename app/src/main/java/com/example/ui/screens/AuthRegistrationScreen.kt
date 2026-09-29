package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Check
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
import com.example.auth.AuthResult
import com.example.auth.PhoneOtpState
import com.example.ui.components.AvatarView
import com.example.ui.theme.*
import kotlin.random.Random

@Composable
fun AuthRegistrationScreen(
    onDismiss: () -> Unit,
    onGoogleSignIn: (onProfileNeeded: (AuthResult) -> Unit) -> Unit,
    onSendPhoneOtp: (phoneNumber: String) -> Unit,
    onVerifyPhoneOtp: (otpCode: String, onProfileNeeded: (AuthResult) -> Unit) -> Unit,
    phoneOtpState: PhoneOtpState,
    onGuestSignIn: (onProfileNeeded: (AuthResult) -> Unit) -> Unit,
    onCompleteRegistration: (
        authResult: AuthResult?,
        nickname: String,
        gender: String,
        country: String,
        dob: String,
        language: String,
        bio: String,
        avatarUrl: String
    ) -> Unit,
    onQuickSwitchUser: (Long) -> Unit,
    onGoogleEmailSignIn: ((email: String, name: String, onProfileNeeded: (AuthResult) -> Unit) -> Unit)? = null,
    deviceInfo: String = "Android Device",
    modifier: Modifier = Modifier
) {
    var mode by remember { mutableStateOf("LOGIN") } // "LOGIN", "PHONE_OTP", "PROFILE_SETUP"
    var currentPendingAuth by remember { mutableStateOf<AuthResult?>(null) }
    var showGoogleDialog by remember { mutableStateOf(false) }
    var googleEmailInput by remember { mutableStateOf("babakunju900@gmail.com") }

    // Registration Profile Fields
    var nickname by remember { mutableStateOf("StarKing" + Random.nextInt(100, 999)) }
    var selectedAvatar by remember { mutableStateOf("avatar_1") }
    var gender by remember { mutableStateOf("Star") }
    var country by remember { mutableStateOf("United States") }
    var dob by remember { mutableStateOf("2000-01-01") }
    var language by remember { mutableStateOf("English") }
    var bio by remember { mutableStateOf("Ready to connect, talk and party on Star King! 🌟") }

    // Phone OTP fields
    var countryCode by remember { mutableStateOf("+1") }
    var phoneBody by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var otpTimer by remember { mutableIntStateOf(60) }

    val countryCodes = listOf("+1 (US)", "+44 (UK)", "+91 (IN)", "+971 (UAE)", "+62 (ID)", "+49 (DE)", "+81 (JP)", "+880 (BD)")
    val genderList = listOf("Male", "Female", "Star")
    val countryList = listOf("United States", "United Kingdom", "India", "Germany", "United Arab Emirates", "Indonesia", "Japan", "Bangladesh")
    val languageList = listOf("English", "Hindi", "Arabic", "Bengali", "Urdu", "Indonesian", "German", "Japanese")
    val avatarPresets = listOf(
        "avatar_crown" to "Crown King 👑",
        "avatar_1" to "Gold Star ⭐",
        "avatar_2" to "Party Vibe 🎧",
        "avatar_aria" to "Vocalist 🎙️",
        "avatar_viktor" to "DJ Viktor 🔥",
        "avatar_support" to "Royal Guard 🛡️"
    )

    // Countdown effect when code is sent
    LaunchedEffect(phoneOtpState) {
        if (phoneOtpState is PhoneOtpState.CodeSent) {
            otpTimer = phoneOtpState.secondsRemaining
            while (otpTimer > 0) {
                kotlinx.coroutines.delay(1000)
                otpTimer--
            }
        }
    }

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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(StarGoldPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👑", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "STAR KING",
                            color = StarGoldPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Voice Chat & Party",
                            color = TextChampagne,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("auth_close_btn")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextWhite)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Body depending on mode
            when (mode) {
                "LOGIN" -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = StarKingSurfaceVariantDark),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Welcome to Star King 🌟",
                                        color = TextWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 19.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Connect • Talk • Party • Make Friends",
                                        color = StarGoldPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Sign in to access live 3D voice rooms, send luxury gifts, earn VIP status, and chat with creators.",
                                        color = TextMuted,
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }

                        // Google Sign-In button
                        item {
                            Button(
                                onClick = { showGoogleDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = StarKingSurfaceVariantDark,
                                    contentColor = TextWhite
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .border(1.dp, StarKingCardBorder, RoundedCornerShape(14.dp))
                                    .testTag("google_login_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text("🌐", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Continue with Google",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        // Phone Number OTP Sign-In
                        item {
                            Button(
                                onClick = { mode = "PHONE_OTP" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = StarKingSurfaceVariantDark,
                                    contentColor = TextWhite
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .border(1.dp, StarKingCardBorder, RoundedCornerShape(14.dp))
                                    .testTag("phone_login_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text("📱", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Phone Number & OTP Verification",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        // Guest Mode (Generates unique 6-digit User ID)
                        item {
                            Button(
                                onClick = {
                                    onGuestSignIn { authRes ->
                                        currentPendingAuth = authRes
                                        mode = "PROFILE_SETUP"
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = StarGoldPrimary,
                                    contentColor = StarKingBgDark
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("guest_mode_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text("✨", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Instant Guest Mode (+1,000 Coins 🪙)",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        // Security & Device Info Banner
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = StarKingCardDark.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🔒", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Secure Authentication Active",
                                            color = StarGoldLight,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = "Device: $deviceInfo",
                                            color = TextMuted,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Quick Switch for Testing
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Switch Testing Role (Quick Access):",
                                color = TextChampagne,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        onQuickSwitchUser(504094L)
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = StarKingCardDark),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("👑 Owner", fontSize = 11.sp, color = StarGoldPrimary, fontWeight = FontWeight.Bold)
                                        Text("ID: 504094", fontSize = 9.sp, color = TextMuted)
                                    }
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
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("🎙️ Aria", fontSize = 11.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                                        Text("Host Star", fontSize = 9.sp, color = TextMuted)
                                    }
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
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("🛡️ Official", fontSize = 11.sp, color = StarGoldLight, fontWeight = FontWeight.Bold)
                                        Text("Staff 99", fontSize = 9.sp, color = TextMuted)
                                    }
                                }
                            }
                        }
                    }
                }

                "PHONE_OTP" -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Phone Number & OTP Login 📱",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Enter your mobile phone number to receive a secure 6-digit verification code.",
                            color = TextMuted,
                            fontSize = 12.sp
                        )

                        // Country Code Selector
                        Text(text = "Select Country Code:", color = TextChampagne, fontSize = 11.sp)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(countryCodes) { codeItem ->
                                val code = codeItem.substringBefore(" ")
                                val isSelected = countryCode == code
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) StarGoldPrimary else StarKingSurfaceVariantDark)
                                        .clickable { countryCode = code }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = codeItem,
                                        color = if (isSelected) StarKingBgDark else TextWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        // Phone Number Input
                        OutlinedTextField(
                            value = phoneBody,
                            onValueChange = { phoneBody = it.filter { char -> char.isDigit() } },
                            label = { Text("Phone Number", color = TextMuted) },
                            placeholder = { Text("e.g. 5550192834", color = TextSecondary) },
                            prefix = { Text("$countryCode ", color = StarGoldPrimary, fontWeight = FontWeight.Bold) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = StarGoldPrimary,
                                unfocusedBorderColor = StarKingCardBorder,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("phone_number_input")
                        )

                        // Send OTP Button
                        Button(
                            onClick = {
                                val fullNumber = "$countryCode$phoneBody"
                                onSendPhoneOtp(fullNumber)
                            },
                            enabled = phoneBody.length >= 6,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StarGoldPrimary,
                                contentColor = StarKingBgDark
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("send_otp_btn")
                        ) {
                            Text(
                                text = if (phoneOtpState is PhoneOtpState.Sending) "Sending Code..." else "Send 6-Digit OTP Code",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Verification Input (visible when code is sent or ready)
                        AnimatedVisibility(visible = phoneOtpState is PhoneOtpState.CodeSent || phoneOtpState is PhoneOtpState.Verifying) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = StarKingSurfaceVariantDark),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "Verification Code Sent! 📩",
                                            color = StarGoldLight,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Enter the 6-digit code sent to $countryCode$phoneBody (Development Test Code: 123456)",
                                            color = TextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = otpInput,
                                        onValueChange = { otpInput = it.take(6).filter { char -> char.isDigit() } },
                                        label = { Text("6-Digit OTP", color = TextMuted) },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = StarGoldPrimary,
                                            unfocusedBorderColor = StarKingCardBorder,
                                            focusedTextColor = TextWhite,
                                            unfocusedTextColor = TextWhite
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("otp_code_input")
                                    )

                                    // Quick Autofill Test OTP button
                                    Button(
                                        onClick = { otpInput = "123456" },
                                        colors = ButtonDefaults.buttonColors(containerColor = StarKingCardDark),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Auto 123456", fontSize = 11.sp, color = StarGoldPrimary)
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (otpTimer > 0) "Resend in ${otpTimer}s" else "Didn't receive code?",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                    if (otpTimer == 0) {
                                        TextButton(onClick = { onSendPhoneOtp("$countryCode$phoneBody") }) {
                                            Text("Resend Code", color = StarGoldPrimary, fontSize = 12.sp)
                                        }
                                    }
                                }

                                Button(
                                    onClick = {
                                        onVerifyPhoneOtp(otpInput) { authRes ->
                                            currentPendingAuth = authRes
                                            mode = "PROFILE_SETUP"
                                        }
                                    },
                                    enabled = otpInput.length == 6,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = StarGoldPrimary,
                                        contentColor = StarKingBgDark
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("verify_otp_btn")
                                ) {
                                    Text("Verify & Continue to Profile Setup", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        TextButton(
                            onClick = { mode = "LOGIN" },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("← Back to Other Sign-In Options", color = TextMuted)
                        }
                    }
                }

                "PROFILE_SETUP" -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = StarKingSurfaceVariantDark),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Complete Profile Setup 👑",
                                        color = TextWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "A unique 6-digit numeric User ID (e.g., 504094) will be generated for you. Your ID never changes.",
                                        color = StarGoldLight,
                                        fontSize = 11.sp
                                    )
                                    if (currentPendingAuth != null) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Linked ${currentPendingAuth?.provider}: ${currentPendingAuth?.identifier}",
                                            color = TextChampagne,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Avatar Selection
                        item {
                            Text(
                                text = "Choose Your Avatar Profile:",
                                color = TextChampagne,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(avatarPresets) { (key, label) ->
                                    val isSelected = selectedAvatar == key
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) StarGoldPrimary.copy(alpha = 0.25f) else StarKingSurfaceVariantDark)
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) StarGoldPrimary else StarKingCardBorder,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable { selectedAvatar = key }
                                            .padding(8.dp)
                                    ) {
                                        AvatarView(
                                            avatarUrl = key,
                                            nickname = label,
                                            size = 48.dp,
                                            isSpeaking = isSelected
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = label,
                                            color = if (isSelected) StarGoldPrimary else TextMuted,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Nickname Field with Random Dice
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
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
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("auth_nickname_input")
                                )
                                Button(
                                    onClick = {
                                        val prefixes = listOf("StarKing", "SolarKnight", "RoyalQueen", "CrownPrince", "AudioNova", "GalaxyBeats")
                                        nickname = prefixes.random() + Random.nextInt(10, 999)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = StarKingSurfaceVariantDark),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("🎲", fontSize = 16.sp)
                                }
                            }
                        }

                        // Gender Selection
                        item {
                            Text(text = "Gender", color = TextMuted, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
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
                                        Text(
                                            text = g,
                                            color = if (isSelected) StarKingBgDark else TextWhite,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Country Selection
                        item {
                            Text(text = "Country / Region", color = TextMuted, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(countryList) { c ->
                                    val isSelected = c == country
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) StarGoldPrimary else StarKingSurfaceVariantDark)
                                            .clickable { country = c }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = c,
                                            color = if (isSelected) StarKingBgDark else TextWhite,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Language Selection
                        item {
                            Text(text = "Primary Language", color = TextMuted, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(languageList) { l ->
                                    val isSelected = l == language
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) StarGoldPrimary else StarKingSurfaceVariantDark)
                                            .clickable { language = l }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = l,
                                            color = if (isSelected) StarKingBgDark else TextWhite,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Date of Birth
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

                        // Bio Field
                        item {
                            OutlinedTextField(
                                value = bio,
                                onValueChange = { bio = it },
                                label = { Text("Personal Bio", color = TextMuted) },
                                singleLine = false,
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

                        // Submit Registration Button
                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = {
                                    onCompleteRegistration(
                                        currentPendingAuth,
                                        nickname.ifBlank { "StarUser" },
                                        gender,
                                        country,
                                        dob,
                                        language,
                                        bio,
                                        selectedAvatar
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = StarGoldPrimary,
                                    contentColor = StarKingBgDark
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("submit_registration_btn")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = StarKingBgDark)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Complete Registration & Get 1,000 Coins 🪙",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        item {
                            TextButton(
                                onClick = { mode = "LOGIN" },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Cancel", color = TextMuted)
                            }
                        }
                    }
                }
            }

            // Google Sign-In Chooser Dialog
            if (showGoogleDialog) {
                AlertDialog(
                    onDismissRequest = { showGoogleDialog = false },
                    containerColor = StarKingSurfaceDark,
                    shape = RoundedCornerShape(18.dp),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🌐", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Google Sign-In", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        }
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Sign in using your Google account to connect with Star King voice rooms and sync your numeric User ID.",
                                color = TextMuted,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )

                            OutlinedTextField(
                                value = googleEmailInput,
                                onValueChange = { googleEmailInput = it },
                                label = { Text("Google Account Email", color = TextMuted) },
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
                                onClick = {
                                    showGoogleDialog = false
                                    if (onGoogleEmailSignIn != null) {
                                        onGoogleEmailSignIn.invoke(googleEmailInput, googleEmailInput.substringBefore("@")) { authRes ->
                                            currentPendingAuth = authRes
                                            nickname = authRes.displayName.ifBlank { "StarUser" }
                                            mode = "PROFILE_SETUP"
                                        }
                                    } else {
                                        onGoogleSignIn { authRes ->
                                            currentPendingAuth = authRes
                                            nickname = authRes.displayName.ifBlank { "StarUser" }
                                            mode = "PROFILE_SETUP"
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StarGoldPrimary, contentColor = StarKingBgDark),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(46.dp)
                            ) {
                                Text("Continue with Google Email", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    showGoogleDialog = false
                                    onGoogleSignIn { authRes ->
                                        currentPendingAuth = authRes
                                        nickname = authRes.displayName.ifBlank { "StarUser" }
                                        mode = "PROFILE_SETUP"
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StarKingSurfaceVariantDark),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(42.dp)
                            ) {
                                Text("⚡ One-Tap Fast Sign-In", color = TextWhite, fontSize = 12.sp)
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { showGoogleDialog = false }) {
                            Text("Cancel", color = TextMuted)
                        }
                    }
                )
            }
        }
    }
}
