package com.example.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.moderation.ImageSafetyModerator
import com.example.moderation.ModerationResult
import com.example.ui.components.AvatarView
import com.example.ui.theme.*
import com.example.util.CountryHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun ProfileCreationWizardDialog(
    initialUserId: Long = 500000L + Random.nextLong(10000, 99999),
    onDismiss: () -> Unit,
    onSaveProfile: (
        userId: Long,
        displayName: String,
        age: Int,
        country: String,
        bio: String,
        avatarUrl: String
    ) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var currentStep by remember { mutableIntStateOf(1) } // 1: Photo, 2: Info, 3: Preview

    // State
    var avatarUriString by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var ageText by remember { mutableStateOf("22") }
    var selectedCountry by remember { mutableStateOf("United States") }
    var bioText by remember { mutableStateOf("Living the Star Voice party vibes! 🌟") }
    val generatedUserId = remember { initialUserId }

    // Country Dropdown
    var countryMenuExpanded by remember { mutableStateOf(false) }

    // Step 1: Photo & Moderation state
    var isUploadingPhoto by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableFloatStateOf(0f) }
    var photoModerationResult by remember { mutableStateOf<ModerationResult?>(null) }
    var cropZoom by remember { mutableFloatStateOf(1f) }

    fun processPhoto(uri: Uri) {
        isUploadingPhoto = true
        photoModerationResult = null
        uploadProgress = 0.2f
        coroutineScope.launch {
            delay(150)
            uploadProgress = 0.6f
            val result = ImageSafetyModerator.moderateImage(context, uri)
            uploadProgress = 1.0f
            delay(100)
            isUploadingPhoto = false
            photoModerationResult = result
            if (result.isApproved) {
                avatarUriString = uri.toString()
            }
        }
    }

    val galleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) processPhoto(uri)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val uri = CountryHelper.saveAndCompressBitmap(context, bitmap)
            processPhoto(uri)
        }
    }

    Surface(
        modifier = Modifier
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
            // Header: Step Progress Tracker
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "STAR VOICE CHAT",
                        color = StarGoldLight,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Create Your Profile 🌟",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF281D4C))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Step $currentStep of 3",
                        color = StarGoldPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Step Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (1..3).forEach { step ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                brush = if (step <= currentStep)
                                    Brush.horizontalGradient(listOf(StarGoldPrimary, Color(0xFFFF2B6D)))
                                else
                                    Brush.horizontalGradient(listOf(Color(0xFF2E264E), Color(0xFF2E264E)))
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Wizard Step Contents
            AnimatedContent(
                targetState = currentStep,
                label = "profile_step_anim",
                modifier = Modifier.weight(1f)
            ) { step ->
                when (step) {
                    // --- STEP 1: PROFILE PHOTO ---
                    1 -> {
                        LazyColumn(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                Text(
                                    text = "Step 1: Upload Profile Photo",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Select a clear photo of yourself. All uploads undergo automatic real-time safety inspection.",
                                    color = TextMuted,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }

                            item {
                                // Circular Profile Photo Preview Frame
                                Box(
                                    modifier = Modifier
                                        .size(150.dp)
                                        .clip(CircleShape)
                                        .background(StarKingSurfaceDark)
                                        .border(
                                            3.dp,
                                            when {
                                                isUploadingPhoto -> Brush.sweepGradient(listOf(StarGoldPrimary, NeonCyan, Color(0xFFFF2B6D)))
                                                photoModerationResult?.isApproved == true -> Brush.linearGradient(listOf(LiveGreen, Color(0xFF69F0AE)))
                                                photoModerationResult?.isApproved == false -> Brush.linearGradient(listOf(DangerRed, Color(0xFFFF5252)))
                                                else -> Brush.linearGradient(listOf(StarKingCardBorder, StarGoldPrimary.copy(alpha = 0.5f)))
                                            },
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (avatarUriString.isNotBlank()) {
                                        AsyncImage(
                                            model = avatarUriString,
                                            contentDescription = "Profile Preview",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .graphicsLayer(scaleX = cropZoom, scaleY = cropZoom)
                                        )
                                    } else {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = Icons.Default.AddPhotoAlternate,
                                                contentDescription = null,
                                                tint = StarGoldPrimary,
                                                modifier = Modifier.size(44.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("No Photo", color = TextMuted, fontSize = 11.sp)
                                        }
                                    }

                                    // Upload & Compression Progress Indicator
                                    if (isUploadingPhoto) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black.copy(alpha = 0.7f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                CircularProgressIndicator(
                                                    progress = { uploadProgress },
                                                    color = StarGoldPrimary,
                                                    strokeWidth = 3.dp,
                                                    modifier = Modifier.size(36.dp)
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = "Scanning ${(uploadProgress * 100).toInt()}%",
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Crop / Zoom Slider (if photo selected)
                            if (avatarUriString.isNotBlank() && photoModerationResult?.isApproved == true) {
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = StarKingSurfaceVariantDark),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Crop & Reposition Zoom", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Text("${(cropZoom * 100).toInt()}%", color = StarGoldLight, fontSize = 12.sp)
                                            }
                                            Slider(
                                                value = cropZoom,
                                                onValueChange = { cropZoom = it },
                                                valueRange = 1f..2.5f,
                                                colors = SliderDefaults.colors(thumbColor = StarGoldPrimary, activeTrackColor = StarGoldPrimary)
                                            )
                                        }
                                    }
                                }
                            }

                            // Actions: Gallery or Camera
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            galleryPicker.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StarGoldLight),
                                        border = ButtonDefaults.outlinedButtonBorder.copy(
                                            brush = Brush.linearGradient(listOf(StarGoldPrimary, Color(0xFFFF2B6D)))
                                        ),
                                        modifier = Modifier.weight(1f).height(46.dp)
                                    ) {
                                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Choose Gallery", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }

                                    OutlinedButton(
                                        onClick = { cameraLauncher.launch(null) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                                        border = ButtonDefaults.outlinedButtonBorder.copy(
                                            brush = Brush.linearGradient(listOf(NeonCyan, Color(0xFF673AB7)))
                                        ),
                                        modifier = Modifier.weight(1f).height(46.dp)
                                    ) {
                                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Take Photo", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }

                            // Moderation Feedback Banner
                            item {
                                photoModerationResult?.let { result ->
                                    if (result.isApproved) {
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = LiveGreen.copy(alpha = 0.15f)),
                                            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(LiveGreen, LiveGreen))),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = LiveGreen)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text("AI Safety Verified: PASSED", color = LiveGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    Text("Photo is family safe and ready to publish.", color = TextChampagne, fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    } else {
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.18f)),
                                            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(DangerRed, DangerRed))),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Dangerous, contentDescription = null, tint = DangerRed)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text("Photo Rejected: Image Rules Violated", color = DangerRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    Text(result.reason, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                                                    Text("Please upload another acceptable photo.", color = TextMuted, fontSize = 10.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // --- STEP 2: PERSONAL INFORMATION ---
                    2 -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                Text(
                                    text = "Step 2: Personal Information",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Your country flag is automatically set based on your selection.",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }

                            // Unique User ID (Auto Generated & Displayed)
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF20183B)),
                                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(StarGoldPrimary, NeonCyan))),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Permanent User ID", color = TextMuted, fontSize = 11.sp)
                                            Text("STAR-$generatedUserId", color = StarGoldPrimary, fontWeight = FontWeight.Black, fontSize = 17.sp)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(StarGoldPrimary)
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text("AUTO-GENERATED", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 9.sp)
                                        }
                                    }
                                }
                            }

                            // Display Name
                            item {
                                OutlinedTextField(
                                    value = displayName,
                                    onValueChange = { displayName = it },
                                    label = { Text("Display Name / Nickname", color = TextMuted) },
                                    placeholder = { Text("e.g. StarKing, MelodyQueen", color = TextSecondary) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = StarGoldPrimary,
                                        unfocusedBorderColor = StarKingCardBorder,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("wizard_name_input")
                                )
                            }

                            // Age
                            item {
                                OutlinedTextField(
                                    value = ageText,
                                    onValueChange = { if (it.all { ch -> ch.isDigit() } && it.length <= 2) ageText = it },
                                    label = { Text("Age", color = TextMuted) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = StarGoldPrimary,
                                        unfocusedBorderColor = StarKingCardBorder,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Country Selector with Automatic Flag Emoji
                            item {
                                Column {
                                    Text("Country & Region", color = TextMuted, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(StarKingSurfaceVariantDark)
                                            .border(1.dp, StarKingCardBorder, RoundedCornerShape(12.dp))
                                            .clickable { countryMenuExpanded = true }
                                            .padding(14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(CountryHelper.getFlag(selectedCountry), fontSize = 22.sp)
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(selectedCountry, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            }
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = StarGoldPrimary)
                                        }

                                        DropdownMenu(
                                            expanded = countryMenuExpanded,
                                            onDismissRequest = { countryMenuExpanded = false },
                                            modifier = Modifier.background(StarKingSurfaceDark)
                                        ) {
                                            CountryHelper.COUNTRIES.forEach { item ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(item.flagEmoji, fontSize = 18.sp)
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text(item.name, color = Color.White)
                                                        }
                                                    },
                                                    onClick = {
                                                        selectedCountry = item.name
                                                        countryMenuExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Bio
                            item {
                                OutlinedTextField(
                                    value = bioText,
                                    onValueChange = { bioText = it },
                                    label = { Text("Bio (Optional)", color = TextMuted) },
                                    placeholder = { Text("Tell everyone what you love to chat about 🌟", color = TextSecondary) },
                                    maxLines = 3,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = StarGoldPrimary,
                                        unfocusedBorderColor = StarKingCardBorder,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // --- STEP 3: PROFILE PREVIEW ---
                    3 -> {
                        LazyColumn(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                Text(
                                    text = "Step 3: Profile Preview",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Here is how other users in voice rooms will see your profile.",
                                    color = TextMuted,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }

                            item {
                                // High-End Glassmorphism Card
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E173E)),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = Brush.linearGradient(
                                            listOf(StarGoldPrimary, Color(0xFFFF2B6D), NeonCyan)
                                        )
                                    ),
                                    shape = RoundedCornerShape(24.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp)
                                    ) {
                                        // Profile Photo
                                        Box(
                                            modifier = Modifier
                                                .size(90.dp)
                                                .clip(CircleShape)
                                                .background(Brush.linearGradient(listOf(StarGoldPrimary, Color(0xFFFF2B6D))))
                                                .padding(3.dp)
                                                .clip(CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (avatarUriString.isNotBlank()) {
                                                AsyncImage(
                                                    model = avatarUriString,
                                                    contentDescription = "Avatar",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                                                )
                                            } else {
                                                AvatarView(
                                                    avatarUrl = "avatar_user",
                                                    nickname = displayName.ifBlank { "User" },
                                                    size = 84.dp
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Display Name & Country Flag
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = displayName.ifBlank { "Star Voice User" },
                                                color = Color.White,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 18.sp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(CountryHelper.getFlag(selectedCountry), fontSize = 18.sp)
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // User ID & Age Pill
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFF2F2459))
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text("ID: $generatedUserId", color = StarGoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFFE91E63).copy(alpha = 0.2f))
                                                    .border(1.dp, Color(0xFFE91E63), RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text("Age: $ageText", color = Color(0xFFFF80AB), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Bio Box
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(StarKingSurfaceDark)
                                                .padding(12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = bioText.ifBlank { "No bio added yet." },
                                                color = TextChampagne,
                                                fontSize = 12.sp,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Wizard Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (currentStep > 1) {
                    OutlinedButton(
                        onClick = { currentStep -= 1 },
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
                        if (currentStep < 3) {
                            currentStep += 1
                        } else {
                            // Final Save
                            val finalAge = ageText.toIntOrNull() ?: 22
                            onSaveProfile(
                                generatedUserId,
                                displayName.ifBlank { "Star Voice User" },
                                finalAge,
                                selectedCountry,
                                bioText,
                                avatarUriString.ifBlank { "avatar_user" }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StarGoldPrimary,
                        contentColor = StarKingBgDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(48.dp).testTag("wizard_continue_btn")
                ) {
                    Text(
                        text = if (currentStep < 3) "Next Step >" else "Save & Continue 🌟",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
