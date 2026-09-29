package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

/**
 * STAR Voice Chat Promotion & Welcome Opportunity Dialog
 * Opened when user taps the STAR Voice Chat banner on the Home Screen
 */
@Composable
fun StarVoiceChatPromotionDialog(
    onDismiss: () -> Unit,
    onEnterVoiceRooms: () -> Unit,
    onClaimRewards: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF090F24)),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(StarGoldPrimary, NeonCyan, StarGoldLight)),
                width = 2.dp
            ),
            modifier = modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .testTag("star_voice_chat_promotion_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Header Row with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("👑", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "NEXUS • CEO WELCOME",
                                color = StarGoldPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Official STAR Voice Chat Opportunity",
                                color = NeonCyan,
                                fontSize = 10.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextWhite, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Hero Banner Inside Dialog
                StarVoiceChatBanner(
                    onClick = { /* already in dialog */ },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // CEO Greeting Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141E3C)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(StarGoldDark, StarGoldPrimary.copy(alpha = 0.4f)))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💬", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "\"Voice Brings People Together\"",
                                color = StarGoldLight,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Welcome to STAR Voice Chat! Our mission is to provide you with the most immersive, high-quality, and interactive live audio community. Connect with hosts globally, send spectacular Lottie gifts, and make lifelong friends in voice rooms.",
                            color = TextChampagne,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "— NEXUS, CEO for STAR Voice Chat",
                            color = StarGoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4 Core Opportunities Showcase
                Text(
                    text = "Key Opportunities & Features 🌟",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                OpportunityRow(
                    icon = Icons.Default.Mic,
                    title = "Voice Chat Party",
                    desc = "Real-time crystal-clear audio with up to 12 speaker seats and live host controls."
                )

                OpportunityRow(
                    icon = Icons.Default.Groups,
                    title = "Global Community",
                    desc = "Meet friends across different languages and countries in dedicated regional rooms."
                )

                OpportunityRow(
                    icon = Icons.Default.Security,
                    title = "Safe & Secure",
                    desc = "Strict community standards, automated moderation, and verified host safety."
                )

                OpportunityRow(
                    icon = Icons.Default.EmojiEvents,
                    title = "More Fun Together",
                    desc = "Exciting Lottie gift celebrations, combo animations, and host gala prizes."
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Welcome Promotional Reward Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1638)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(StarGoldPrimary, Color(0xFFE11D48)))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🎁", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "New Member Welcome Gift",
                                color = StarGoldLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Free welcome bonus for all STAR Voice Chat members!",
                                color = TextMuted,
                                fontSize = 10.5.sp
                            )
                        }
                        Button(
                            onClick = {
                                onClaimRewards()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StarGoldPrimary,
                                contentColor = StarKingBgDark
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("Claim 🪙", fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Enter Voice Rooms Button
                Button(
                    onClick = {
                        onEnterVoiceRooms()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = StarKingBgDark
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("enter_voice_rooms_from_promo_btn")
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "EXPLORE STAR VOICE ROOMS 🎙️",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun OpportunityRow(
    icon: ImageVector,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E293B))
                .border(1.dp, StarGoldPrimary.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, tint = StarGoldPrimary, modifier = Modifier.size(16.dp))
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Text(
                text = desc,
                color = TextChampagne.copy(alpha = 0.85f),
                fontSize = 10.5.sp,
                lineHeight = 14.sp
            )
        }
    }
}
