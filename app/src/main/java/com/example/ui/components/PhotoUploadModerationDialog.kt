package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.moderation.ImageSafetyModerator
import com.example.moderation.ModerationResult
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun PhotoUploadModerationDialog(
    title: String = "Update Profile Photo",
    subtitle: String = "Select a photo from your gallery. All uploads undergo automatic real-time safety inspection.",
    isCircularPreview: Boolean = true,
    currentPhotoUrl: String = "",
    onDismiss: () -> Unit,
    onPhotoApprovedAndSaved: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var isScanning by remember { mutableStateOf(false) }
    var moderationResult by remember { mutableStateOf<ModerationResult?>(null) }

    // Android Zero-Permission Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            isScanning = true
            moderationResult = null
            coroutineScope.launch {
                // Real-time scan
                val result = ImageSafetyModerator.moderateImage(context, uri)
                isScanning = false
                moderationResult = result
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(StarGoldPrimary, Color(0xFFFF2B6D)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCircularPreview) Icons.Default.AccountCircle else Icons.Default.Image,
                        contentDescription = null,
                        tint = StarKingBgDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "Real-Time Moderation Protected 🛡️",
                        color = StarGoldLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                // Preview Area
                Box(
                    modifier = Modifier
                        .size(if (isCircularPreview) 130.dp else 180.dp, if (isCircularPreview) 130.dp else 110.dp)
                        .clip(if (isCircularPreview) CircleShape else RoundedCornerShape(16.dp))
                        .background(StarKingSurfaceDark)
                        .border(
                            2.dp,
                            when {
                                isScanning -> Brush.sweepGradient(listOf(StarGoldPrimary, NeonCyan, Color(0xFFFF2B6D)))
                                moderationResult?.isApproved == true -> Brush.linearGradient(listOf(LiveGreen, Color(0xFF69F0AE)))
                                moderationResult?.isApproved == false -> Brush.linearGradient(listOf(DangerRed, Color(0xFFFF5252)))
                                else -> Brush.linearGradient(listOf(StarKingCardBorder, StarKingCardBorder))
                            },
                            if (isCircularPreview) CircleShape else RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedUri != null) {
                        AsyncImage(
                            model = selectedUri,
                            contentDescription = "Selected Photo Preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (currentPhotoUrl.isNotBlank()) {
                        AsyncImage(
                            model = currentPhotoUrl,
                            contentDescription = "Current Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Add Photo",
                                tint = StarGoldPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("No Photo Selected", color = TextMuted, fontSize = 11.sp)
                        }
                    }

                    // Scanning Overlay Indicator
                    if (isScanning) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.65f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    color = StarGoldPrimary,
                                    modifier = Modifier.size(28.dp),
                                    strokeWidth = 2.5.dp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Scanning Safety...",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Pick / Change Photo Button
                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StarGoldLight),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.linearGradient(listOf(StarGoldPrimary, Color(0xFFFF2B6D)))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Choose from gallery",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedUri != null) "Choose Different Photo" else "Select From Device Gallery",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // Moderation Status Banner
                AnimatedVisibility(visible = moderationResult != null) {
                    moderationResult?.let { result ->
                        if (result.isApproved) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(LiveGreen.copy(alpha = 0.15f))
                                    .border(1.dp, LiveGreen.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                    .padding(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Safe",
                                        tint = LiveGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "✅ Image Safety Verified: PASSED",
                                            color = LiveGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = result.details,
                                            color = TextChampagne,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DangerRed.copy(alpha = 0.18f))
                                    .border(1.dp, DangerRed, RoundedCornerShape(10.dp))
                                    .padding(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Dangerous,
                                        contentDescription = "Rejected",
                                        tint = DangerRed,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "⛔ Upload Rejected: Inappropriate Content",
                                            color = DangerRed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = result.reason,
                                            color = TextWhite,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = "Sexual, explicit, or policy-violating imagery is strictly prohibited to maintain community safety.",
                                            color = TextMuted,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            val isReadyToSave = selectedUri != null && moderationResult?.isApproved == true && !isScanning
            Button(
                onClick = {
                    selectedUri?.let { uri ->
                        onPhotoApprovedAndSaved(uri.toString())
                    }
                },
                enabled = isReadyToSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF2B6D),
                    disabledContainerColor = Color(0xFF3A3450)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Save & Apply Photo",
                    fontWeight = FontWeight.Bold,
                    color = if (isReadyToSave) Color.White else TextMuted
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        },
        containerColor = Color(0xFF1B1630),
        shape = RoundedCornerShape(22.dp)
    )
}
