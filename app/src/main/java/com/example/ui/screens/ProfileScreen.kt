package com.example.ui.screens

import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FirestoreUserProfile
import com.example.model.StoreCustomizationEntity
import com.example.model.UserCustomizationEntity
import com.example.model.UserEntity
import com.example.model.UserRole
import com.example.model.WalletEntity
import com.example.ui.components.AvatarView
import com.example.ui.components.MedalDetailBottomSheet
import com.example.ui.components.MedalGraphic
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
    user: UserEntity?,
    wallet: WalletEntity?,
    firestoreProfile: FirestoreUserProfile? = null,
    isFirestoreSyncing: Boolean = false,
    userCustomizations: List<UserCustomizationEntity> = emptyList(),
    storeMedals: List<StoreCustomizationEntity> = emptyList(),
    onSyncFirestore: () -> Unit = {},
    onEquipMedal: (String) -> Unit = {},
    onRechargeClick: () -> Unit,
    onOpenStore: () -> Unit,
    onOpenSupport: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAdminDashboard: () -> Unit,
    onOpenAuth: () -> Unit,
    onOpenFollowers: () -> Unit,
    onOpenFollowing: () -> Unit,
    onOpenEditProfile: () -> Unit,
    onOpenLedger: () -> Unit,
    onOpenHostDashboard: () -> Unit,
    onOpenAgencyDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userRole = try { UserRole.valueOf(user?.role ?: "USER") } catch (_: Exception) { UserRole.USER }

    // Resolve Display Name & Profile Picture from Firestore (with local fallback)
    val displayName = remember(firestoreProfile, user) {
        firestoreProfile?.displayName?.ifBlank { user?.nickname }
            ?: user?.nickname ?: "Star King User"
    }

    val profilePicture = remember(firestoreProfile, user) {
        firestoreProfile?.profilePicture?.ifBlank { user?.avatarUrl }
            ?: user?.avatarUrl ?: "avatar_user"
    }

    val equippedMedal = remember(firestoreProfile, user) {
        firestoreProfile?.equippedMedal?.ifBlank { user?.equippedBadge }
            ?: user?.equippedBadge ?: ""
    }

    // Resolve Accumulated Medals pulled from Firestore
    val accumulatedMedalIds = remember(firestoreProfile, userCustomizations) {
        val fromFirestore = firestoreProfile?.accumulatedMedals ?: emptyList()
        if (fromFirestore.isNotEmpty()) {
            fromFirestore
        } else {
            // Fallback to local owned medals
            userCustomizations.filter { it.itemId.startsWith("medal_") }.map { it.itemId }
        }
    }

    // Inspect Medal Dialog State
    var selectedMedalToInspect by remember { mutableStateOf<StoreCustomizationEntity?>(null) }

    val level = firestoreProfile?.level ?: user?.level ?: 1
    val exp = firestoreProfile?.exp ?: user?.exp ?: 0
    val progress = (exp % 1000).toFloat() / 1000f

    // Rotation animation for sync spinner
    val infiniteTransition = rememberInfiniteTransition(label = "sync_spin")
    val spinRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isFirestoreSyncing) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_angle"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(StarKingBgDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
    ) {
        // --- 1. CLOUD FIRESTORE PROFILE HEADER CARD ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = StarKingSurfaceVariantDark),
                shape = RoundedCornerShape(22.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(StarKingCardBorder, StarGoldPrimary.copy(alpha = 0.5f)))
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_header_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Top Bar: Firestore Cloud Sync Status & Action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E173E))
                                .border(1.dp, Color(0xFF3B2F70), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("☁️", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (firestoreProfile != null) "Firestore Live Profile" else "Firestore Ready",
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        // Sync button
                        FilledTonalButton(
                            onClick = onSyncFirestore,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0xFF2B2154),
                                contentColor = StarGoldPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp).testTag("firestore_sync_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Sync Firestore",
                                modifier = Modifier
                                    .size(15.dp)
                                    .rotate(spinRotation)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isFirestoreSyncing) "Syncing..." else "Sync Cloud",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Main User Info Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Profile Picture pulled from Firestore
                        AvatarView(
                            avatarUrl = profilePicture,
                            nickname = displayName,
                            size = 68.dp,
                            frameName = user?.equippedFrame ?: "",
                            vipTier = firestoreProfile?.vipTier ?: user?.vipTier ?: 0,
                            level = level,
                            isOfficial = firestoreProfile?.isOfficialVerified ?: user?.isOfficialVerified ?: false
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            // Display Name pulled from Firestore
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = displayName,
                                    color = TextWhite,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                )
                                if (firestoreProfile?.isOfficialVerified == true || user?.isOfficialVerified == true) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "✓", color = StarGoldPrimary, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = "User ID: ${user?.userId ?: "504094"}",
                                color = StarGoldLight,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "${user?.gender ?: "Star"} • ${user?.country ?: "Global"} • ${user?.language ?: "English"}",
                                color = TextMuted,
                                fontSize = 11.sp
                            )

                            if (equippedMedal.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Brush.horizontalGradient(listOf(Color(0xFF8B0000), Color(0xFFD97706))))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text("👑 ", fontSize = 11.sp)
                                    Text(
                                        text = equippedMedal,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        // Edit Profile Icon
                        IconButton(
                            onClick = onOpenEditProfile,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(StarKingCardDark)
                                .testTag("profile_edit_btn")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = StarGoldPrimary, modifier = Modifier.size(16.dp))
                        }
                    }

                    if (!user?.bio.isNullOrBlank() || !firestoreProfile?.bio.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "\"${firestoreProfile?.bio?.ifBlank { user?.bio } ?: user?.bio}\"",
                            color = TextChampagne,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Followers / Following / Gifts Count
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ProfileStatItem(
                            label = "Followers",
                            value = "${firestoreProfile?.followersCount ?: user?.followersCount ?: 0}",
                            onClick = onOpenFollowers
                        )
                        ProfileStatItem(
                            label = "Following",
                            value = "${firestoreProfile?.followingCount ?: user?.followingCount ?: 0}",
                            onClick = onOpenFollowing
                        )
                        ProfileStatItem(
                            label = "Gifts Received",
                            value = "${firestoreProfile?.giftsReceivedCount ?: user?.giftsReceivedCount ?: 0}"
                        )
                        ProfileStatItem(
                            label = "Medals",
                            value = "${accumulatedMedalIds.size}",
                            onClick = onOpenStore
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Level Progress Bar
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Level $level", color = StarGoldPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text(text = "Next: Level ${level + 1} (${exp % 1000}/1000 XP)", color = TextMuted, fontSize = 10.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            color = StarGoldPrimary,
                            trackColor = StarKingCardDark,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
        }

        // --- 2. ACCUMULATED MEDALS WALL (PULLED FROM FIRESTORE) ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1838)),
                shape = RoundedCornerShape(22.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(StarGoldPrimary.copy(alpha = 0.6f), Color(0xFF2C2456)))
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("accumulated_medals_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🎖️", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Accumulated Medals Wall (${accumulatedMedalIds.size})",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Synced from Cloud Firestore collection",
                                    color = NeonCyan,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        TextButton(onClick = onOpenStore) {
                            Text("Store >", color = StarGoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (accumulatedMedalIds.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF15112B))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("No Medals Accumulated Yet", color = TextMuted, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = onOpenStore,
                                    colors = ButtonDefaults.buttonColors(containerColor = StarGoldPrimary, contentColor = StarKingBgDark),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Explore 22 Honor Medals in Store 🌟", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    } else {
                        // Horizontal scrolling showcase of accumulated medals
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(accumulatedMedalIds) { medalId ->
                                val medalItem = storeMedals.find { it.id == medalId }
                                val medalName = medalItem?.name ?: medalId.replace("medal_", "").replace("_", " ").capitalizeWords()
                                val isCurrentlyEquipped = equippedMedal.equals(medalName, ignoreCase = true)

                                Card(
                                    onClick = {
                                        if (medalItem != null) {
                                            selectedMedalToInspect = medalItem
                                        } else {
                                            onEquipMedal(medalId)
                                        }
                                    },
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF15112B)),
                                    shape = RoundedCornerShape(16.dp),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = if (isCurrentlyEquipped)
                                            Brush.linearGradient(listOf(StarGoldLight, StarGoldPrimary, StarGoldDark))
                                        else
                                            Brush.linearGradient(listOf(Color(0xFF2C2456), Color(0xFF221A45)))
                                    ),
                                    modifier = Modifier.width(115.dp)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(10.dp)
                                    ) {
                                        if (isCurrentlyEquipped) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Brush.horizontalGradient(listOf(StarGoldDark, StarGoldPrimary)))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("👑 Active", color = StarKingBgDark, fontWeight = FontWeight.ExtraBold, fontSize = 8.sp)
                                            }
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFF221A45))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("Wear", color = LiveGreen, fontWeight = FontWeight.Bold, fontSize = 8.sp)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // 3D Medal Graphic Artwork
                                        MedalGraphic(medalId = medalId, medalName = medalName, size = 64.dp)

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // 5 Stars: ★★★★★
                                        Row {
                                            repeat(5) {
                                                Text("★", color = Color(0xFFFFD700), fontSize = 10.sp)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = medalName,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 3. WALLET BALANCE CARD ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = StarKingSurfaceVariantDark),
                shape = RoundedCornerShape(20.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(StarGoldPrimary.copy(alpha = 0.6f), StarKingCardBorder))),
                modifier = Modifier.fillMaxWidth().testTag("wallet_balance_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Star King Wallet 💼",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )

                        Button(
                            onClick = onRechargeClick,
                            colors = ButtonDefaults.buttonColors(containerColor = StarGoldPrimary, contentColor = StarKingBgDark),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("wallet_recharge_btn")
                        ) {
                            Text(text = "+ Recharge Coins", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Gold Coins", color = TextMuted, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🪙 ", fontSize = 16.sp)
                                Text(
                                    text = String.format("%,d", wallet?.coinBalance ?: 0L),
                                    color = StarGoldPrimary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp
                                )
                            }
                        }

                        Box(modifier = Modifier.width(1.dp).height(36.dp).background(StarKingCardBorder))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Diamonds (Gifts)", color = TextMuted, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "💎 ", fontSize = 16.sp)
                                Text(
                                    text = String.format("%,d", wallet?.diamondBalance ?: 0L),
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 4. STAFF / ADMIN PANEL HUB ---
        if (userRole.canAccessAdminPanel()) {
            item {
                Card(
                    onClick = onOpenAdminDashboard,
                    colors = CardDefaults.cardColors(containerColor = StarKingCardDark),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(StarGoldPrimary, NeonCyan))),
                    modifier = Modifier.fillMaxWidth().testTag("profile_admin_hub_card")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(StarGoldPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin", tint = StarKingBgDark)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Admin & Staff Control Center",
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Role: ${userRole.name} • Manage users, tickets & reports",
                                color = StarGoldLight,
                                fontSize = 11.sp
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = "Open", tint = StarGoldPrimary)
                    }
                }
            }
        }

        // --- 5. ACTION HUBS & SETTINGS ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = StarKingSurfaceVariantDark),
                shape = RoundedCornerShape(20.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(StarKingCardBorder, StarKingCardBorder))),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    ProfileMenuRow(
                        icon = Icons.Default.Storefront,
                        title = "Customization Store & Medals",
                        subtitle = "Unlock 22 honor badges, frames & VIP",
                        onClick = onOpenStore,
                        badge = "HOT 🔥"
                    )

                    HorizontalDivider(color = StarKingCardBorder.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 8.dp))

                    ProfileMenuRow(
                        icon = Icons.Default.ReceiptLong,
                        title = "Wallet Ledger & Transactions",
                        subtitle = "View full history of coin recharges & gifts",
                        onClick = onOpenLedger
                    )

                    HorizontalDivider(color = StarKingCardBorder.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 8.dp))

                    ProfileMenuRow(
                        icon = Icons.Default.HeadsetMic,
                        title = "24/7 VIP Customer Support",
                        subtitle = "Submit tickets & live assistance",
                        onClick = onOpenSupport
                    )

                    HorizontalDivider(color = StarKingCardBorder.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 8.dp))

                    ProfileMenuRow(
                        icon = Icons.Default.Settings,
                        title = "App Settings & Account",
                        subtitle = "Language, security & session management",
                        onClick = onOpenSettings
                    )
                }
            }
        }
    }

    // Inspect Medal Dialog
    selectedMedalToInspect?.let { medal ->
        MedalDetailBottomSheet(
            medal = medal,
            isOwned = true,
            isEquipped = equippedMedal.equals(medal.name, ignoreCase = true),
            userCoins = wallet?.coinBalance ?: 0L,
            onDismiss = { selectedMedalToInspect = null },
            onEquip = { medalId ->
                onEquipMedal(medalId)
                selectedMedalToInspect = null
            },
            onPurchase = { medalId ->
                onEquipMedal(medalId)
                selectedMedalToInspect = null
            }
        )
    }
}

@Composable
private fun ProfileStatItem(
    label: String,
    value: String,
    onClick: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(4.dp)
    ) {
        Text(text = value, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(text = label, color = TextMuted, fontSize = 11.sp)
    }
}

@Composable
private fun ProfileMenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    badge: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(StarKingCardDark),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, tint = StarGoldPrimary, modifier = Modifier.size(18.dp))
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = title, color = TextWhite, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                if (badge.isNotBlank()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = badge,
                        color = DangerRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }
            Text(text = subtitle, color = TextMuted, fontSize = 11.sp)
        }

        Icon(Icons.Default.ChevronRight, contentDescription = "Open", tint = TextMuted, modifier = Modifier.size(18.dp))
    }
}

private fun String.capitalizeWords(): String =
    split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
