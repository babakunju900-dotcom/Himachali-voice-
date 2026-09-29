package com.example

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.BottomNavTab
import com.example.viewmodel.StarKingViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                StarKingApp()
            }
        }
    }
}

@Composable
fun StarKingApp(viewModel: StarKingViewModel = viewModel()) {
    val context = LocalContext.current

    // Runtime Permission for Microphone Audio capture
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.showToast("Microphone permission granted 🎙️")
        } else {
            viewModel.showToast("Microphone permission denied. Live audio is disabled.")
        }
    }

    LaunchedEffect(Unit) {
        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    // State Collection
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val currentWallet by viewModel.currentWallet.collectAsStateWithLifecycle()
    val currentNotifications by viewModel.currentNotifications.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    val liveRooms by viewModel.liveRooms.collectAsStateWithLifecycle()
    val topHosts by viewModel.topHosts.collectAsStateWithLifecycle()
    val allGifts by viewModel.allGifts.collectAsStateWithLifecycle()
    val coinPackages by viewModel.coinPackages.collectAsStateWithLifecycle()
    val vipPlans by viewModel.vipPlans.collectAsStateWithLifecycle()
    val storeCustomizations by viewModel.storeCustomizations.collectAsStateWithLifecycle()
    val allEvents by viewModel.allEvents.collectAsStateWithLifecycle()

    val activeRoomId by viewModel.activeRoomId.collectAsStateWithLifecycle()
    val activeRoom by viewModel.activeRoom.collectAsStateWithLifecycle()
    val activeSeats by viewModel.activeSeats.collectAsStateWithLifecycle()
    val activeRoomMessages by viewModel.activeRoomMessages.collectAsStateWithLifecycle()
    val activeGiftAnimation by viewModel.activeGiftAnimation.collectAsStateWithLifecycle()

    val isMicEnabled by viewModel.voiceEngine.isMicEnabled.collectAsStateWithLifecycle()
    val isMuted by viewModel.voiceEngine.isMuted.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.voiceEngine.isSpeaking.collectAsStateWithLifecycle()
    val isSpeakerOn by viewModel.voiceEngine.isSpeakerOn.collectAsStateWithLifecycle()
    val audioWaveLevels by viewModel.voiceEngine.audioWaveLevels.collectAsStateWithLifecycle()

    val selectedPrivateChatUser by viewModel.selectedPrivateChatUser.collectAsStateWithLifecycle()
    val privateChatMessages by viewModel.privateChatMessages.collectAsStateWithLifecycle()
    val recentConversations by viewModel.recentPrivateConversations.collectAsStateWithLifecycle()

    val mySupportTickets by viewModel.mySupportTickets.collectAsStateWithLifecycle()
    val allSupportTickets by viewModel.allSupportTickets.collectAsStateWithLifecycle()
    val allReports by viewModel.allReports.collectAsStateWithLifecycle()
    val platformStats by viewModel.platformStats.collectAsStateWithLifecycle()

    val showCreateRoomModal by viewModel.showCreateRoomModal.collectAsStateWithLifecycle()
    val showRechargeModal by viewModel.showRechargeModal.collectAsStateWithLifecycle()
    val showGiftDrawer by viewModel.showGiftDrawer.collectAsStateWithLifecycle()
    val showAdminDashboard by viewModel.showAdminDashboard.collectAsStateWithLifecycle()
    val showSupportModal by viewModel.showSupportModal.collectAsStateWithLifecycle()
    val showStoreModal by viewModel.showStoreModal.collectAsStateWithLifecycle()
    val showSettingsModal by viewModel.showSettingsModal.collectAsStateWithLifecycle()
    val showAuthModal by viewModel.showAuthModal.collectAsStateWithLifecycle()
    val inspectingUser by viewModel.inspectingUser.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    // Toast listener
    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    val unreadNotifs = currentNotifications.count { !it.isRead }

    Box(modifier = Modifier.fillMaxSize().background(StarKingBgDark)) {
        when {
            // Active Voice Room Screen (Deep immersive full-screen)
            activeRoom != null -> {
                VoiceRoomScreen(
                    room = activeRoom!!,
                    seats = activeSeats,
                    messages = activeRoomMessages,
                    activeGiftTx = activeGiftAnimation,
                    currentUserId = currentUser?.userId ?: 504094L,
                    isMicEnabled = isMicEnabled,
                    isMuted = isMuted,
                    isSpeaking = isSpeaking,
                    isSpeakerOn = isSpeakerOn,
                    audioWaveLevels = audioWaveLevels,
                    onExitRoom = { viewModel.exitRoom() },
                    onSeatClick = { seat -> viewModel.takeSeat(seat.seatIndex) },
                    onLeaveSeat = { viewModel.leaveSeat() },
                    onToggleMic = { viewModel.toggleMic() },
                    onToggleSpeaker = { viewModel.toggleSpeaker() },
                    onOpenGiftSheet = { viewModel.openGiftDrawer() },
                    onSendMessage = { text -> viewModel.sendRoomMessage(text) },
                    onFollowHost = { hostId -> viewModel.toggleFollow(hostId) },
                    onReportRoom = {
                        val r = activeRoom ?: return@VoiceRoomScreen
                        viewModel.submitReport(
                            reportedUserId = r.hostUserId,
                            reportedUserName = r.hostName,
                            reportedRoomId = r.roomId,
                            reportedRoomName = r.name,
                            category = "Room Violation",
                            details = "Reported by room listener"
                        )
                    },
                    onLockSeat = { seatIdx -> viewModel.lockSeat(seatIdx) },
                    onMuteSeat = { seatIdx -> viewModel.muteSeat(seatIdx) },
                    onKickSeat = { seatIdx -> viewModel.kickUserFromSeat(seatIdx) },
                    onCloseRoom = { viewModel.closeActiveRoom() }
                )
            }

            // 1-on-1 Private Chat Screen
            selectedPrivateChatUser != null -> {
                PrivateChatScreen(
                    targetUser = selectedPrivateChatUser!!,
                    messages = privateChatMessages,
                    currentUserId = currentUser?.userId ?: 504094L,
                    onBack = { viewModel.closePrivateChat() },
                    onSendMessage = { text -> viewModel.sendPrivateMessage(text) }
                )
            }

            // Normal Tabbed App Navigation
            else -> {
                Scaffold(
                    topBar = {
                        StarKingHeader(
                            user = currentUser,
                            coinBalance = currentWallet?.coinBalance ?: 0L,
                            unreadNotifications = unreadNotifs,
                            onAvatarClick = { viewModel.setTab(BottomNavTab.PROFILE) },
                            onCoinClick = { viewModel.openRecharge() },
                            onSearchClick = { viewModel.setTab(BottomNavTab.ROOMS) },
                            onNotificationClick = {
                                viewModel.markNotificationsRead()
                                viewModel.setTab(BottomNavTab.MESSAGES)
                            },
                            onAdminClick = { viewModel.openAdminDashboard() }
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = StarKingSurfaceDark,
                            tonalElevation = 8.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .testTag("main_bottom_nav")
                        ) {
                            NavigationBarItem(
                                selected = currentTab == BottomNavTab.HOME,
                                onClick = { viewModel.setTab(BottomNavTab.HOME) },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                label = { Text("HOME", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = StarKingBgDark,
                                    selectedTextColor = StarGoldPrimary,
                                    indicatorColor = StarGoldPrimary,
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted
                                ),
                                modifier = Modifier.testTag("tab_home")
                            )

                            NavigationBarItem(
                                selected = currentTab == BottomNavTab.ROOMS,
                                onClick = { viewModel.setTab(BottomNavTab.ROOMS) },
                                icon = { Icon(Icons.Default.Mic, contentDescription = "Rooms") },
                                label = { Text("ROOMS", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = StarKingBgDark,
                                    selectedTextColor = StarGoldPrimary,
                                    indicatorColor = StarGoldPrimary,
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted
                                ),
                                modifier = Modifier.testTag("tab_rooms")
                            )

                            NavigationBarItem(
                                selected = currentTab == BottomNavTab.EVENTS,
                                onClick = { viewModel.setTab(BottomNavTab.EVENTS) },
                                icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "Events") },
                                label = { Text("EVENTS", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = StarKingBgDark,
                                    selectedTextColor = StarGoldPrimary,
                                    indicatorColor = StarGoldPrimary,
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted
                                ),
                                modifier = Modifier.testTag("tab_events")
                            )

                            NavigationBarItem(
                                selected = currentTab == BottomNavTab.MESSAGES,
                                onClick = { viewModel.setTab(BottomNavTab.MESSAGES) },
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            if (unreadNotifs > 0) {
                                                Badge(containerColor = DangerRed) {
                                                    Text(text = "$unreadNotifs", color = TextWhite)
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.ChatBubble, contentDescription = "Messages")
                                    }
                                },
                                label = { Text("MESSAGES", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = StarKingBgDark,
                                    selectedTextColor = StarGoldPrimary,
                                    indicatorColor = StarGoldPrimary,
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted
                                ),
                                modifier = Modifier.testTag("tab_messages")
                            )

                            NavigationBarItem(
                                selected = currentTab == BottomNavTab.PROFILE,
                                onClick = { viewModel.setTab(BottomNavTab.PROFILE) },
                                icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                                label = { Text("PROFILE", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = StarKingBgDark,
                                    selectedTextColor = StarGoldPrimary,
                                    indicatorColor = StarGoldPrimary,
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted
                                ),
                                modifier = Modifier.testTag("tab_profile")
                            )
                        }
                    },
                    containerColor = StarKingBgDark
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        when (currentTab) {
                            BottomNavTab.HOME -> {
                                HomeScreen(
                                    rooms = liveRooms,
                                    topHosts = topHosts,
                                    selectedCategory = selectedCategory,
                                    onCategorySelect = { viewModel.setSelectedCategory(it) },
                                    onRoomClick = { room -> viewModel.enterRoom(room.roomId) },
                                    onHostClick = { host -> viewModel.inspectUser(host) },
                                    onCreateRoomClick = { viewModel.openCreateRoom() },
                                    onEventBannerClick = { viewModel.setTab(BottomNavTab.EVENTS) }
                                )
                            }
                            BottomNavTab.ROOMS -> {
                                RoomsScreen(
                                    rooms = liveRooms,
                                    searchQuery = searchQuery,
                                    selectedCategory = selectedCategory,
                                    onSearchChange = { viewModel.setSearchQuery(it) },
                                    onCategorySelect = { viewModel.setSelectedCategory(it) },
                                    onRoomClick = { room -> viewModel.enterRoom(room.roomId) },
                                    onCreateRoomClick = { viewModel.openCreateRoom() }
                                )
                            }
                            BottomNavTab.EVENTS -> {
                                EventsLeaderboardScreen(
                                    topHosts = topHosts,
                                    events = allEvents,
                                    onHostClick = { host -> viewModel.inspectUser(host) }
                                )
                            }
                            BottomNavTab.MESSAGES -> {
                                MessagesScreen(
                                    recentConversations = recentConversations,
                                    notifications = currentNotifications,
                                    onSelectChatUser = { user -> viewModel.startPrivateChat(user) },
                                    onOpenSupportTicket = { viewModel.openSupport() }
                                )
                            }
                            BottomNavTab.PROFILE -> {
                                ProfileScreen(
                                    user = currentUser,
                                    wallet = currentWallet,
                                    onRechargeClick = { viewModel.openRecharge() },
                                    onOpenStore = { viewModel.openStore() },
                                    onOpenSupport = { viewModel.openSupport() },
                                    onOpenSettings = { viewModel.openSettings() },
                                    onOpenAdminDashboard = { viewModel.openAdminDashboard() },
                                    onOpenAuth = { viewModel.openAuth() }
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- MODALS & BOTTOM SHEETS ---

        // In-Room Gift Bottom Sheet
        if (showGiftDrawer && activeRoom != null) {
            GiftBottomSheet(
                gifts = allGifts,
                userCoinBalance = currentWallet?.coinBalance ?: 0L,
                seats = activeSeats,
                hostUserId = activeRoom!!.hostUserId,
                hostName = activeRoom!!.hostName,
                onDismiss = { viewModel.closeGiftDrawer() },
                onSendGift = { receiverId, receiverName, giftId, count ->
                    viewModel.sendGift(receiverId, receiverName, giftId, count)
                },
                onRechargeClick = { viewModel.openRecharge() }
            )
        }

        // Create Room Dialog
        if (showCreateRoomModal) {
            CreateRoomDialog(
                onDismiss = { viewModel.closeCreateRoom() },
                onCreateRoom = { name, desc, cat, lang, seats, welcome, isPrivate, pass ->
                    viewModel.createRoom(name, desc, cat, lang, seats, welcome, isPrivate, pass)
                }
            )
        }

        // Wallet Recharge Dialog
        if (showRechargeModal) {
            WalletRechargeDialog(
                packages = coinPackages,
                currentBalance = currentWallet?.coinBalance ?: 0L,
                onDismiss = { viewModel.closeRecharge() },
                onRechargeSelected = { pkgId -> viewModel.rechargeCoins(pkgId) }
            )
        }

        // Customization Store Sheet
        if (showStoreModal) {
            CustomizationStoreScreen(
                items = storeCustomizations,
                vipPlans = vipPlans,
                userCoinBalance = currentWallet?.coinBalance ?: 0L,
                onDismiss = { viewModel.closeStore() },
                onPurchaseItem = { itemId -> viewModel.purchaseStoreItem(itemId) },
                onPurchaseVip = { planId -> viewModel.purchaseVip(planId) }
            )
        }

        // Customer Support Dialog
        if (showSupportModal) {
            CustomerSupportDialog(
                tickets = mySupportTickets,
                onDismiss = { viewModel.closeSupport() },
                onSubmitTicket = { cat, sub, msg -> viewModel.submitSupportTicket(cat, sub, msg) }
            )
        }

        // Settings Screen
        if (showSettingsModal) {
            SettingsScreen(
                currentLanguage = currentUser?.language ?: "English",
                onLanguageChange = { lang ->
                    currentUser?.let { u ->
                        viewModel.updateMyProfile(u.nickname, u.bio, u.gender, u.country, u.avatarUrl)
                    }
                    viewModel.showToast("Language changed to $lang")
                },
                onDismiss = { viewModel.closeSettings() },
                onLogout = {
                    viewModel.closeSettings()
                    viewModel.openAuth()
                },
                onDeleteAccount = {
                    viewModel.closeSettings()
                    viewModel.showToast("Account deletion requested. Data cleared.")
                    viewModel.openAuth()
                }
            )
        }

        // Admin & Staff Dashboard
        if (showAdminDashboard) {
            AdminDashboardDialog(
                stats = platformStats,
                users = topHosts,
                reports = allReports,
                tickets = allSupportTickets,
                onDismiss = { viewModel.closeAdminDashboard() },
                onBanUser = { targetId, reason -> viewModel.adminBanUser(targetId, reason) },
                onUnbanUser = { targetId -> viewModel.adminUnbanUser(targetId) },
                onVerifyUser = { targetId, verified -> viewModel.adminVerifyUser(targetId, verified) },
                onReplyTicket = { ticketId, reply, status -> viewModel.adminReplyTicket(ticketId, reply, status) },
                onReviewReport = { reportId, status, notes -> viewModel.adminReviewReport(reportId, status, notes) }
            )
        }

        // Auth & Registration Modal
        if (showAuthModal) {
            AuthRegistrationScreen(
                onDismiss = { viewModel.closeAuth() },
                onRegister = { nick, gender, country, dob, lang, bio ->
                    viewModel.registerNewUser(nick, gender, country, dob, lang, bio)
                },
                onQuickSwitchUser = { userId -> viewModel.switchUser(userId) }
            )
        }

        // User Profile Inspection Dialog (when tapping on other user's avatar)
        if (inspectingUser != null) {
            val user = inspectingUser!!
            AlertDialog(
                onDismissRequest = { viewModel.inspectUser(null) },
                containerColor = StarKingSurfaceDark,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AvatarView(
                            avatarUrl = user.avatarUrl,
                            nickname = user.nickname,
                            size = 48.dp,
                            frameName = user.equippedFrame,
                            vipTier = user.vipTier,
                            level = user.level,
                            isOfficial = user.isOfficialVerified
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = user.nickname, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(text = "ID: ${user.userId}", color = StarGoldLight, fontSize = 11.sp)
                            if (user.isOfficialVerified) {
                                Text(text = "✓ OFFICIAL VERIFIED", color = StarGoldPrimary, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = user.bio, color = TextChampagne, fontSize = 12.sp)
                        Text(text = "Country: ${user.country} • Gender: ${user.gender}", color = TextMuted, fontSize = 11.sp)
                        Text(text = "Followers: ${user.followersCount} • Gifts: ${user.giftsReceivedCount}", color = TextMuted, fontSize = 11.sp)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.toggleFollow(user.userId)
                            viewModel.inspectUser(null)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StarGoldPrimary, contentColor = StarKingBgDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Follow / Unfollow 🌟", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                },
                dismissButton = {
                    Row {
                        TextButton(
                            onClick = {
                                viewModel.startPrivateChat(user)
                                viewModel.inspectUser(null)
                            }
                        ) {
                            Text("Message 💬", color = NeonCyan, fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = { viewModel.inspectUser(null) }) {
                            Text("Close", color = TextMuted)
                        }
                    }
                }
            )
        }
    }
}
