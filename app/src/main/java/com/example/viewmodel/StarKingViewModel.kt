package com.example.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.*
import com.example.data.*
import com.example.model.*
import com.example.voice.StarKingVoiceEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class BottomNavTab {
    HOME,
    MOMENTS,
    ROOMS,
    MESSAGES,
    PROFILE
}

class StarKingViewModel(application: Application) : AndroidViewModel(application) {

    val firestoreService = FirestoreProfileService(application)
    private val database = StarKingDatabase.getDatabase(application, viewModelScope)
    private val repository = StarKingRepository(database.starKingDao(), firestoreService)
    val voiceEngine = StarKingVoiceEngine(application)
    val authManager = StarKingAuthManager(application)
    val phoneOtpState: StateFlow<PhoneOtpState> = authManager.phoneOtpState

    // Current logged in user (defaults to 504094L)
    private val _currentUserId = MutableStateFlow(504094L)
    val currentUserId: StateFlow<Long> = _currentUserId.asStateFlow()

    val currentUser: StateFlow<UserEntity?> = _currentUserId
        .flatMapLatest { id -> repository.getUser(id) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val currentWallet: StateFlow<WalletEntity?> = _currentUserId
        .flatMapLatest { id -> repository.getWallet(id) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val currentNotifications: StateFlow<List<NotificationEntity>> = _currentUserId
        .flatMapLatest { id -> repository.getNotifications(id) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Bottom Navigation
    private val _currentTab = MutableStateFlow(BottomNavTab.HOME)
    val currentTab: StateFlow<BottomNavTab> = _currentTab.asStateFlow()

    // Active Room State
    private val _activeRoomId = MutableStateFlow<Long?>(null)
    val activeRoomId: StateFlow<Long?> = _activeRoomId.asStateFlow()

    val activeRoom: StateFlow<RoomEntity?> = _activeRoomId
        .flatMapLatest { roomId ->
            if (roomId == null) flowOf(null) else repository.getRoom(roomId)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val activeSeats: StateFlow<List<RoomSeatEntity>> = _activeRoomId
        .flatMapLatest { roomId ->
            if (roomId == null) flowOf(emptyList()) else repository.getSeatsForRoom(roomId)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val activeRoomMessages: StateFlow<List<ChatMessageEntity>> = _activeRoomId
        .flatMapLatest { roomId ->
            if (roomId == null) flowOf(emptyList()) else repository.getRoomMessages(roomId)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val activeRoomRecentGifts: StateFlow<List<GiftTransactionEntity>> = _activeRoomId
        .flatMapLatest { roomId ->
            if (roomId == null) flowOf(emptyList()) else repository.getRecentRoomGifts(roomId)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Gift animation banner overlay
    private val _activeGiftAnimation = MutableStateFlow<GiftTransactionEntity?>(null)
    val activeGiftAnimation: StateFlow<GiftTransactionEntity?> = _activeGiftAnimation.asStateFlow()
    private var giftAnimationJob: Job? = null

    // Room Listing & Discovery
    val liveRooms: StateFlow<List<RoomEntity>> = repository.getLiveRooms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topHosts: StateFlow<List<UserEntity>> = repository.getTopHosts(10)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGifts: StateFlow<List<GiftEntity>> = repository.getAllGifts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val coinPackages: StateFlow<List<CoinPackageEntity>> = repository.getCoinPackages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vipPlans: StateFlow<List<VipPlanEntity>> = repository.getVipPlans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val storeCustomizations: StateFlow<List<StoreCustomizationEntity>> = repository.getStoreCustomizations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userCustomizations: StateFlow<List<UserCustomizationEntity>> = _currentUserId
        .flatMapLatest { id -> repository.getUserCustomizations(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Firestore live user profile & accumulated medals
    val firestoreUserProfile: StateFlow<FirestoreUserProfile?> = _currentUserId
        .flatMapLatest { id -> firestoreService.observeProfile(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isFirestoreSyncing = MutableStateFlow(false)
    val isFirestoreSyncing: StateFlow<Boolean> = _isFirestoreSyncing.asStateFlow()

    val allEvents: StateFlow<List<EventEntity>> = repository.getAllEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAgencies: StateFlow<List<AgencyEntity>> = repository.getAllAgencies()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search and Category Filter
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // 1-on-1 Private Chat
    private val _selectedPrivateChatUser = MutableStateFlow<UserEntity?>(null)
    val selectedPrivateChatUser: StateFlow<UserEntity?> = _selectedPrivateChatUser.asStateFlow()

    val privateChatMessages: StateFlow<List<ChatMessageEntity>> = combine(
        _currentUserId,
        _selectedPrivateChatUser
    ) { myId, recipient ->
        if (recipient == null) flowOf(emptyList())
        else repository.getPrivateMessages(myId, recipient.userId)
    }.flatMapLatest { it }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val recentPrivateConversations: StateFlow<List<ChatMessageEntity>> = _currentUserId
        .flatMapLatest { myId -> repository.getRecentPrivateConversations(myId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Moment Feed & Recommended Topics ---
    private val _momentTopics = MutableStateFlow(
        listOf(
            MomentTopic(
                hashtag = "# selfie",
                participationCount = "443.3k",
                isHot = true,
                previewIcons = listOf("📸", "✨", "🥰", "🤳")
            ),
            MomentTopic(
                hashtag = "# Mood________",
                participationCount = "520.6k",
                isHot = true,
                previewIcons = listOf("🎶", "☕", "🌧️", "💭")
            ),
            MomentTopic(
                hashtag = "# VoiceChat",
                participationCount = "310.2k",
                isHot = true,
                previewIcons = listOf("🎙️", "👑", "🚀", "💎")
            ),
            MomentTopic(
                hashtag = "# Anime",
                participationCount = "293.8k",
                isHot = false,
                previewIcons = listOf("🦊", "⚔️", "🌸", "⭐")
            )
        )
    )
    val momentTopics: StateFlow<List<MomentTopic>> = _momentTopics.asStateFlow()

    private val _moments = MutableStateFlow(
        listOf(
            MomentEntity(
                id = "moment_1",
                userId = 100001L,
                authorName = "COOL + Nafaa ✨",
                authorAvatar = "avatar_1",
                authorGender = "Female",
                vipTier = 4,
                badgeTag = "COOL",
                timestampText = "Just now",
                caption = "🥰🥰🥰🥰🥰 Join our festival night voice party in Room #68! Special gift shower for top fans 🚀👑",
                images = listOf("room_festival_1", "room_gift_shower"),
                likesCount = 128,
                isLiked = false,
                commentsCount = 24,
                isFollowing = false,
                hashtag = "# Mood________"
            ),
            MomentEntity(
                id = "moment_2",
                userId = 100002L,
                authorName = "💔🍁goodby🍁",
                authorAvatar = "avatar_2",
                authorGender = "Male",
                vipTier = 2,
                badgeTag = "HOT",
                timestampText = "5m ago",
                caption = "Late night chill & acoustic music vibes with the team 🎸🎤 Come say hi!",
                images = listOf("room_chill_music"),
                likesCount = 94,
                isLiked = false,
                commentsCount = 13,
                isFollowing = true,
                hashtag = "# selfie"
            ),
            MomentEntity(
                id = "moment_3",
                userId = 100003L,
                authorName = "Princess Yashi 🌸",
                authorAvatar = "avatar_3",
                authorGender = "Female",
                vipTier = 5,
                badgeTag = "VIP",
                timestampText = "12m ago",
                caption = "Thank you so much to all my gifters tonight for the Diamond Agency crown! Love you all 💖✨",
                images = listOf("room_stage_yashi"),
                likesCount = 312,
                isLiked = true,
                commentsCount = 48,
                isFollowing = false,
                hashtag = "# VoiceChat"
            )
        )
    )
    val moments: StateFlow<List<MomentEntity>> = _moments.asStateFlow()

    fun likeMoment(momentId: String) {
        _moments.value = _moments.value.map { m ->
            if (m.id == momentId) {
                val newLiked = !m.isLiked
                m.copy(
                    isLiked = newLiked,
                    likesCount = if (newLiked) m.likesCount + 1 else maxOf(0, m.likesCount - 1)
                )
            } else m
        }
    }

    fun followUserFromMoment(targetUserId: Long) {
        _moments.value = _moments.value.map { m ->
            if (m.userId == targetUserId) {
                val newFollowing = !m.isFollowing
                m.copy(isFollowing = newFollowing)
            } else m
        }
        toggleFollow(targetUserId)
    }

    fun postMomentComment(momentId: String, text: String) {
        _moments.value = _moments.value.map { m ->
            if (m.id == momentId) {
                m.copy(commentsCount = m.commentsCount + 1)
            } else m
        }
        showToast("Comment posted! 💬")
    }

    fun createMoment(caption: String, hashtag: String, imageUri: String = "") {
        val user = currentUser.value
        val newMoment = MomentEntity(
            id = "moment_${System.currentTimeMillis()}",
            userId = user?.userId ?: 504094L,
            authorName = user?.nickname ?: "Star Voice User",
            authorAvatar = user?.avatarUrl ?: "avatar_user",
            authorGender = user?.gender ?: "Female",
            vipTier = user?.vipTier ?: 1,
            badgeTag = "NEW",
            timestampText = "Just now",
            caption = caption,
            images = if (imageUri.isNotBlank()) listOf(imageUri) else listOf("room_festival_1"),
            likesCount = 0,
            isLiked = false,
            commentsCount = 0,
            isFollowing = false,
            hashtag = hashtag
        )
        _moments.value = listOf(newMoment) + _moments.value
        showToast("Moment published to feed! 📸")
    }

    fun deleteMoment(momentId: String) {
        _moments.value = _moments.value.filterNot { it.id == momentId }
        showToast("Moment deleted from feed")
    }

    // Moderation & Support
    val mySupportTickets: StateFlow<List<SupportTicketEntity>> = _currentUserId
        .flatMapLatest { id -> repository.getTicketsForUser(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSupportTickets: StateFlow<List<SupportTicketEntity>> = repository.getAllTickets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReports: StateFlow<List<ReportEntity>> = repository.getAllReports()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeHandRaises: StateFlow<List<RoomHandRaiseEntity>> = _activeRoomId
        .flatMapLatest { roomId ->
            if (roomId == null) flowOf(emptyList()) else repository.getHandRaises(roomId)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val walletTransactions: StateFlow<List<WalletTransactionEntity>> = _currentUserId
        .flatMapLatest { id -> repository.getTransactions(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val followersList: StateFlow<List<UserEntity>> = _currentUserId
        .flatMapLatest { id -> repository.getFollowersUsers(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val followingList: StateFlow<List<UserEntity>> = _currentUserId
        .flatMapLatest { id -> repository.getFollowingUsers(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Dialog & Sheet states
    private val _showCreateRoomModal = MutableStateFlow(false)
    val showCreateRoomModal: StateFlow<Boolean> = _showCreateRoomModal.asStateFlow()

    private val _showRechargeModal = MutableStateFlow(false)
    val showRechargeModal: StateFlow<Boolean> = _showRechargeModal.asStateFlow()

    private val _showGiftDrawer = MutableStateFlow(false)
    val showGiftDrawer: StateFlow<Boolean> = _showGiftDrawer.asStateFlow()

    private val _showAdminDashboard = MutableStateFlow(false)
    val showAdminDashboard: StateFlow<Boolean> = _showAdminDashboard.asStateFlow()

    private val _showSupportModal = MutableStateFlow(false)
    val showSupportModal: StateFlow<Boolean> = _showSupportModal.asStateFlow()

    private val _showStoreModal = MutableStateFlow(false)
    val showStoreModal: StateFlow<Boolean> = _showStoreModal.asStateFlow()

    private val _showSettingsModal = MutableStateFlow(false)
    val showSettingsModal: StateFlow<Boolean> = _showSettingsModal.asStateFlow()

    private val _showAuthModal = MutableStateFlow(false)
    val showAuthModal: StateFlow<Boolean> = _showAuthModal.asStateFlow()

    private val _showHostDashboard = MutableStateFlow(false)
    val showHostDashboard: StateFlow<Boolean> = _showHostDashboard.asStateFlow()

    private val _showAgencyDashboard = MutableStateFlow(false)
    val showAgencyDashboard: StateFlow<Boolean> = _showAgencyDashboard.asStateFlow()

    private val _showLedgerDialog = MutableStateFlow(false)
    val showLedgerDialog: StateFlow<Boolean> = _showLedgerDialog.asStateFlow()

    private val _showFollowListDialog = MutableStateFlow(false)
    val showFollowListDialog: StateFlow<Boolean> = _showFollowListDialog.asStateFlow()

    private val _followListTitle = MutableStateFlow("Followers")
    val followListTitle: StateFlow<String> = _followListTitle.asStateFlow()

    private val _showEditProfileModal = MutableStateFlow(false)
    val showEditProfileModal: StateFlow<Boolean> = _showEditProfileModal.asStateFlow()

    private val _showProfileCreationWizard = MutableStateFlow(false)
    val showProfileCreationWizard: StateFlow<Boolean> = _showProfileCreationWizard.asStateFlow()

    fun openProfileCreationWizard() { _showProfileCreationWizard.value = true }
    fun closeProfileCreationWizard() { _showProfileCreationWizard.value = false }

    fun saveProfileFromWizard(
        userId: Long,
        displayName: String,
        age: Int,
        country: String,
        bio: String,
        avatarUrl: String
    ) {
        viewModelScope.launch {
            val user = currentUser.value
            if (user != null) {
                repository.updateUserProfile(
                    userId = user.userId,
                    nickname = displayName,
                    bio = bio,
                    gender = user.gender,
                    country = country,
                    avatarUrl = avatarUrl
                )
            }
            closeProfileCreationWizard()
            showToast("Profile created successfully! Welcome to Star Voice 🌟")
        }
    }

    // Privacy & Visibility Settings
    private val _isProfilePrivate = MutableStateFlow(false)
    val isProfilePrivate: StateFlow<Boolean> = _isProfilePrivate.asStateFlow()

    private val _hideAge = MutableStateFlow(false)
    val hideAge: StateFlow<Boolean> = _hideAge.asStateFlow()

    private val _hideCountry = MutableStateFlow(false)
    val hideCountry: StateFlow<Boolean> = _hideCountry.asStateFlow()

    private val _allowMessages = MutableStateFlow(true)
    val allowMessages: StateFlow<Boolean> = _allowMessages.asStateFlow()

    private val _allowProfileSharing = MutableStateFlow(true)
    val allowProfileSharing: StateFlow<Boolean> = _allowProfileSharing.asStateFlow()

    fun toggleProfilePrivacy() { _isProfilePrivate.value = !_isProfilePrivate.value }
    fun toggleHideAge() { _hideAge.value = !_hideAge.value }
    fun toggleHideCountry() { _hideCountry.value = !_hideCountry.value }
    fun toggleAllowMessages() { _allowMessages.value = !_allowMessages.value }
    fun toggleAllowProfileSharing() { _allowProfileSharing.value = !_allowProfileSharing.value }
    fun shareProfile() {
        val user = currentUser.value
        val nick = user?.nickname ?: "Star Voice User"
        val uid = user?.userId ?: 504094L
        showToast("Profile link copied for $nick (ID: $uid) 📋")
    }

    private val _showProfilePhotoUpload = MutableStateFlow(false)
    val showProfilePhotoUpload: StateFlow<Boolean> = _showProfilePhotoUpload.asStateFlow()

    private val _showRoomCoverUpload = MutableStateFlow(false)
    val showRoomCoverUpload: StateFlow<Boolean> = _showRoomCoverUpload.asStateFlow()

    fun openProfilePhotoUpload() { _showProfilePhotoUpload.value = true }
    fun closeProfilePhotoUpload() { _showProfilePhotoUpload.value = false }

    fun openRoomCoverUpload() { _showRoomCoverUpload.value = true }
    fun closeRoomCoverUpload() { _showRoomCoverUpload.value = false }

    private val _inspectingUser = MutableStateFlow<UserEntity?>(null)
    val inspectingUser: StateFlow<UserEntity?> = _inspectingUser.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Platform Statistics (for Admin)
    private val _platformStats = MutableStateFlow<PlatformStats?>(null)
    val platformStats: StateFlow<PlatformStats?> = _platformStats.asStateFlow()

    init {
        // Wire Voice Engine speaking callback to seat update in Room
        voiceEngine.onSpeakingStateChanged = { speaking ->
            val roomId = _activeRoomId.value
            val userId = _currentUserId.value
            if (roomId != null) {
                viewModelScope.launch {
                    repository.updateSeatSpeaking(roomId, userId, speaking)
                }
            }
        }
        refreshStats()
    }

    fun setTab(tab: BottomNavTab) {
        _currentTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun showToast(message: String) {
        _toastMessage.value = message
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun openCreateRoom() { _showCreateRoomModal.value = true }
    fun closeCreateRoom() { _showCreateRoomModal.value = false }

    fun openRecharge() { _showRechargeModal.value = true }
    fun closeRecharge() { _showRechargeModal.value = false }

    fun openGiftDrawer() { _showGiftDrawer.value = true }
    fun closeGiftDrawer() { _showGiftDrawer.value = false }

    fun openAdminDashboard() {
        refreshStats()
        _showAdminDashboard.value = true
    }
    fun closeAdminDashboard() { _showAdminDashboard.value = false }

    fun openSupport() { _showSupportModal.value = true }
    fun closeSupport() { _showSupportModal.value = false }

    fun openStore() { _showStoreModal.value = true }
    fun closeStore() { _showStoreModal.value = false }

    fun openSettings() { _showSettingsModal.value = true }
    fun closeSettings() { _showSettingsModal.value = false }

    fun openAuth() { _showAuthModal.value = true }
    fun closeAuth() { _showAuthModal.value = false }

    fun openHostDashboard() { _showHostDashboard.value = true }
    fun closeHostDashboard() { _showHostDashboard.value = false }

    fun openAgencyDashboard() { _showAgencyDashboard.value = true }
    fun closeAgencyDashboard() { _showAgencyDashboard.value = false }

    fun openLedger() { _showLedgerDialog.value = true }
    fun closeLedger() { _showLedgerDialog.value = false }

    fun openFollowersList() {
        _followListTitle.value = "Followers"
        _showFollowListDialog.value = true
    }
    fun openFollowingList() {
        _followListTitle.value = "Following"
        _showFollowListDialog.value = true
    }
    fun closeFollowList() { _showFollowListDialog.value = false }

    fun openEditProfile() { _showEditProfileModal.value = true }
    fun closeEditProfile() { _showEditProfileModal.value = false }

    fun raiseHand() {
        val roomId = _activeRoomId.value ?: return
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.raiseHand(roomId, user)
            showToast("Raised hand to speak ✋")
        }
    }

    fun cancelHandRaise() {
        val roomId = _activeRoomId.value ?: return
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.cancelHandRaise(roomId, user.userId)
            showToast("Hand raise cancelled")
        }
    }

    fun acceptHandRaise(targetUser: UserEntity, seatIndex: Int) {
        val roomId = _activeRoomId.value ?: return
        viewModelScope.launch {
            val result = repository.acceptHandRaise(roomId, targetUser, seatIndex)
            if (result.isSuccess) {
                showToast("Invited ${targetUser.nickname} to seat #${seatIndex + 1} 🎙️")
            } else {
                showToast(result.exceptionOrNull()?.message ?: "Could not assign seat")
            }
        }
    }

    fun convertDiamondsToCoins(amount: Long) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val res = repository.convertDiamondsToCoins(user.userId, amount)
            if (res.isSuccess) {
                showToast("Converted $amount diamonds into ${res.getOrNull()} coins! 🪙")
            } else {
                showToast(res.exceptionOrNull()?.message ?: "Conversion failed")
            }
        }
    }

    fun inspectUser(user: UserEntity?) { _inspectingUser.value = user }

    fun refreshStats() {
        viewModelScope.launch {
            _platformStats.value = repository.getPlatformStats()
        }
    }

    // --- Room Actions ---
    fun enterRoom(roomId: Long) {
        _activeRoomId.value = roomId
    }

    fun exitRoom() {
        val roomId = _activeRoomId.value
        val user = currentUser.value
        if (roomId != null && user != null) {
            viewModelScope.launch {
                repository.leaveSeat(roomId, user.userId)
            }
        }
        voiceEngine.stopMicrophone()
        _activeRoomId.value = null
        _showGiftDrawer.value = false
    }

    fun takeSeat(seatIndex: Int) {
        val roomId = _activeRoomId.value ?: return
        val user = currentUser.value ?: return

        viewModelScope.launch {
            val result = repository.takeSeat(roomId, seatIndex, user)
            if (result.isSuccess) {
                showToast("Took Seat #${seatIndex + 1}! 🎙️")
                // Start microphone if not muted
                voiceEngine.startMicrophone()
            } else {
                showToast(result.exceptionOrNull()?.message ?: "Failed to take seat")
            }
        }
    }

    fun leaveSeat() {
        val roomId = _activeRoomId.value ?: return
        val user = currentUser.value ?: return

        viewModelScope.launch {
            repository.leaveSeat(roomId, user.userId)
            voiceEngine.stopMicrophone()
            showToast("Left voice seat")
        }
    }

    fun toggleMic() {
        if (!voiceEngine.isMicEnabled.value) {
            val started = voiceEngine.startMicrophone()
            if (!started) {
                showToast("Microphone permission required to speak.")
            }
        } else {
            voiceEngine.toggleMute()
        }
    }

    fun toggleSpeaker() {
        voiceEngine.toggleSpeakerphone()
        showToast(if (voiceEngine.isSpeakerOn.value) "Speaker On" else "Earpiece Audio")
    }

    fun lockSeat(seatIndex: Int) {
        val roomId = _activeRoomId.value ?: return
        viewModelScope.launch {
            repository.toggleLockSeat(roomId, seatIndex)
        }
    }

    fun muteSeat(seatIndex: Int) {
        val roomId = _activeRoomId.value ?: return
        viewModelScope.launch {
            repository.toggleMuteSeat(roomId, seatIndex)
        }
    }

    fun kickUserFromSeat(seatIndex: Int) {
        val roomId = _activeRoomId.value ?: return
        viewModelScope.launch {
            repository.kickUserFromSeat(roomId, seatIndex)
            showToast("User removed from seat")
        }
    }

    fun sendRoomMessage(text: String) {
        val roomId = _activeRoomId.value ?: return
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.sendRoomMessage(roomId, user, text)
        }
    }

    fun sendGift(receiverUserId: Long, receiverName: String, giftId: String, count: Int) {
        val roomId = _activeRoomId.value ?: return
        val user = currentUser.value ?: return

        viewModelScope.launch {
            val result = repository.sendGift(roomId, user, receiverUserId, receiverName, giftId, count)
            if (result.isSuccess) {
                val giftTx = result.getOrNull()
                _activeGiftAnimation.value = giftTx
                giftAnimationJob?.cancel()
                giftAnimationJob = launch {
                    delay(3500)
                    _activeGiftAnimation.value = null
                }
                showToast("Sent ${count}x gift to $receiverName! 🎁")
            } else {
                showToast(result.exceptionOrNull()?.message ?: "Gift failed")
            }
        }
    }

    fun createRoom(
        name: String,
        description: String,
        category: String,
        language: String,
        seatCount: Int,
        welcomeMsg: String,
        isPrivate: Boolean,
        password: String,
        coverPhotoUrl: String = ""
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val created = repository.createRoom(
                hostUser = user,
                name = name,
                description = description,
                category = category,
                language = language,
                seatCount = seatCount,
                welcomeMsg = welcomeMsg,
                isPrivate = isPrivate,
                password = password,
                coverPhotoUrl = coverPhotoUrl
            )
            closeCreateRoom()
            enterRoom(created.roomId)
            showToast("Room created! ID: ${created.roomId} 🌟")
        }
    }

    fun updateRoomCoverPhoto(roomId: Long, newCoverUrl: String) {
        viewModelScope.launch {
            repository.updateRoomCoverPhoto(roomId, newCoverUrl)
            showToast("Room cover updated successfully! 🖼️")
        }
    }

    fun updateUserProfilePhoto(newAvatarUrl: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.updateUserProfilePhoto(user.userId, newAvatarUrl)
            showToast("Profile photo updated successfully! 📸")
        }
    }

    fun removeFakeOrInvalidRoom(roomId: Long) {
        if (roomId == 708101L) {
            showToast("Cannot remove ⭐ Star Voice Customer Support official room!")
            return
        }
        viewModelScope.launch {
            repository.deleteRoomPermanently(roomId)
            showToast("Invalid/fake room removed from community.")
        }
    }

    fun closeActiveRoom() {
        val roomId = _activeRoomId.value ?: return
        viewModelScope.launch {
            repository.closeRoom(roomId)
            exitRoom()
            showToast("Room closed")
        }
    }

    // --- Wallet & Recharge ---
    fun rechargeCoins(packageId: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.rechargeCoins(user.userId, packageId)
            if (result.isSuccess) {
                closeRecharge()
                showToast("Payment verified! Credited ${result.getOrNull()} coins 💰")
                refreshStats()
            } else {
                showToast(result.exceptionOrNull()?.message ?: "Payment failed")
            }
        }
    }

    // --- VIP & Store ---
    fun purchaseVip(planId: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.purchaseVip(user.userId, planId)
            if (result.isSuccess) {
                showToast("VIP activated successfully! 👑")
            } else {
                showToast(result.exceptionOrNull()?.message ?: "VIP activation failed")
            }
        }
    }

    fun purchaseStoreItem(itemId: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.purchaseCustomization(user.userId, itemId)
            if (result.isSuccess) {
                showToast("Customization equipped! 🌟")
            } else {
                showToast(result.exceptionOrNull()?.message ?: "Purchase failed")
            }
        }
    }

    fun equipStoreItem(itemId: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.equipCustomization(user.userId, itemId)
            if (result.isSuccess) {
                showToast("Medal / Customization equipped! 🎖️")
                syncProfileWithFirestore()
            } else {
                showToast(result.exceptionOrNull()?.message ?: "Equip failed")
            }
        }
    }

    fun syncProfileWithFirestore() {
        val user = currentUser.value ?: return
        val medals = userCustomizations.value
            .filter { it.itemId.startsWith("medal_") }
            .map { it.itemId }
        viewModelScope.launch {
            _isFirestoreSyncing.value = true
            val success = firestoreService.syncProfileToFirestore(user, medals)
            _isFirestoreSyncing.value = false
            if (success) {
                showToast("Profile & Medals synced with Cloud Firestore ☁️")
            } else {
                showToast("Profile updated locally. Cloud sync pending connectivity.")
            }
        }
    }

    // --- Follow System ---
    fun toggleFollow(targetUserId: Long) {
        val myId = _currentUserId.value
        viewModelScope.launch {
            val following = repository.toggleFollow(myId, targetUserId)
            showToast(if (following) "Followed user! 🌟" else "Unfollowed")
        }
    }

    // --- Private Chat ---
    fun startPrivateChat(targetUser: UserEntity) {
        _selectedPrivateChatUser.value = targetUser
        _currentTab.value = BottomNavTab.MESSAGES
    }

    fun closePrivateChat() {
        _selectedPrivateChatUser.value = null
    }

    fun sendPrivateMessage(text: String) {
        val sender = currentUser.value ?: return
        val recipient = _selectedPrivateChatUser.value ?: return
        viewModelScope.launch {
            repository.sendPrivateMessage(sender, recipient.userId, text)
        }
    }

    // --- Support & Reports ---
    fun submitSupportTicket(category: String, subject: String, message: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val tckId = repository.createSupportTicket(user.userId, user.nickname, category, subject, message)
            closeSupport()
            showToast("Support Ticket $tckId submitted. Our team will respond shortly.")
        }
    }

    fun submitReport(
        reportedUserId: Long?,
        reportedUserName: String?,
        reportedRoomId: Long?,
        reportedRoomName: String?,
        category: String,
        details: String
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.submitReport(
                reporterId = user.userId,
                reporterName = user.nickname,
                reportedUserId = reportedUserId,
                reportedUserName = reportedUserName,
                reportedRoomId = reportedRoomId,
                reportedRoomName = reportedRoomName,
                category = category,
                details = details
            )
            showToast("Report submitted to Star King Moderation team. Thank you!")
        }
    }

    // --- Admin / Staff Panel Controls ---
    fun adminBanUser(targetUserId: Long, reason: String) {
        val user = currentUser.value ?: return
        if (!UserRole.valueOf(user.role).canBanUsers()) {
            showToast("Unauthorized: Insufficient staff permissions")
            return
        }
        viewModelScope.launch {
            repository.setUserBanned(targetUserId, true, reason)
            repository.logModerationAction(user.userId, targetUserId, "PERM_BAN", reason)
            showToast("User $targetUserId has been banned.")
            refreshStats()
        }
    }

    fun adminUnbanUser(targetUserId: Long) {
        val user = currentUser.value ?: return
        if (!UserRole.valueOf(user.role).canBanUsers()) return
        viewModelScope.launch {
            repository.setUserBanned(targetUserId, false, "")
            repository.logModerationAction(user.userId, targetUserId, "UNBAN", "Admin pardon")
            showToast("User $targetUserId unbanned.")
            refreshStats()
        }
    }

    fun adminVerifyUser(targetUserId: Long, verified: Boolean) {
        val user = currentUser.value ?: return
        if (user.role != UserRole.OWNER.name && user.role != UserRole.SUPER_ADMIN.name) return
        viewModelScope.launch {
            repository.assignOfficialVerification(targetUserId, verified)
            showToast("Official status updated for User $targetUserId")
        }
    }

    fun adminReplyTicket(ticketId: String, reply: String, status: String) {
        viewModelScope.launch {
            repository.updateSupportTicket(ticketId, reply, status)
            showToast("Ticket $ticketId updated")
            refreshStats()
        }
    }

    fun adminReviewReport(reportId: Long, status: String, notes: String) {
        viewModelScope.launch {
            repository.reviewReport(reportId, status, notes)
            showToast("Report reviewed ($status)")
            refreshStats()
        }
    }

    // --- Authentication & User Switching ---
    fun registerNewUser(
        nickname: String,
        gender: String,
        country: String,
        dob: String,
        language: String,
        bio: String
    ) {
        viewModelScope.launch {
            val newUser = repository.registerUser(
                nickname = nickname,
                gender = gender,
                country = country,
                dob = dob,
                language = language,
                bio = bio,
                deviceInfo = authManager.getDeviceInfo(),
                sessionId = authManager.generateSessionId()
            )
            _currentUserId.value = newUser.userId
            closeAuth()
            showToast("Registered! Permanent Star King ID: ${newUser.userId} 🌟")
        }
    }

    fun loginWithGoogle(
        activity: Activity,
        onRequireProfileSetup: (AuthResult) -> Unit
    ) {
        viewModelScope.launch {
            val result = authManager.signInWithGoogle(activity)
            if (result.success) {
                val existing = repository.findUserByAuthIdentifier(result.identifier)
                if (existing != null) {
                    _currentUserId.value = existing.userId
                    closeAuth()
                    showToast("Welcome back, ${existing.nickname}! (ID: ${existing.userId}) 🌟")
                } else {
                    onRequireProfileSetup(result)
                }
            } else {
                showToast(result.errorMessage ?: "Google Sign-In failed")
            }
        }
    }

    fun loginWithGoogleEmail(
        email: String,
        name: String,
        onRequireProfileSetup: (AuthResult) -> Unit
    ) {
        viewModelScope.launch {
            val result = authManager.signInWithGoogleEmail(email, name)
            val existing = repository.findUserByAuthIdentifier(result.identifier)
            if (existing != null) {
                _currentUserId.value = existing.userId
                closeAuth()
                showToast("Welcome back, ${existing.nickname}! (ID: ${existing.userId}) 🌟")
            } else {
                onRequireProfileSetup(result)
            }
        }
    }

    fun sendPhoneOtp(activity: Activity, phoneNumber: String) {
        authManager.sendPhoneOtp(activity, phoneNumber)
    }

    fun verifyPhoneOtp(
        otpCode: String,
        onRequireProfileSetup: (AuthResult) -> Unit
    ) {
        viewModelScope.launch {
            val result = authManager.verifyOtp(otpCode)
            if (result.success) {
                val existing = repository.findUserByAuthIdentifier(result.identifier)
                if (existing != null) {
                    _currentUserId.value = existing.userId
                    closeAuth()
                    showToast("Welcome back, ${existing.nickname}! (ID: ${existing.userId}) 🌟")
                } else {
                    onRequireProfileSetup(result)
                }
            } else {
                showToast(result.errorMessage ?: "Invalid OTP verification code")
            }
        }
    }

    fun loginAsGuest(onRequireProfileSetup: (AuthResult) -> Unit) {
        val guestAuth = authManager.signInAsGuest()
        onRequireProfileSetup(guestAuth)
    }

    fun completeRegistrationWithProfile(
        authResult: AuthResult?,
        nickname: String,
        gender: String,
        country: String,
        dob: String,
        language: String,
        bio: String,
        avatarUrl: String
    ) {
        viewModelScope.launch {
            val provider = authResult?.provider ?: "GUEST"
            val identifier = authResult?.identifier ?: ""
            val deviceInfo = authManager.getDeviceInfo()
            val sessionId = authManager.generateSessionId()

            val newUser = repository.registerUser(
                nickname = nickname,
                gender = gender,
                country = country,
                dob = dob,
                language = language,
                bio = bio,
                avatarUrl = avatarUrl,
                authProvider = provider,
                authIdentifier = identifier,
                deviceInfo = deviceInfo,
                sessionId = sessionId
            )
            _currentUserId.value = newUser.userId
            closeAuth()
            showToast("Welcome to Star King! Your permanent User ID is ${newUser.userId} 🌟 (1,000 Coins Credited)")
        }
    }

    fun logoutCurrentUser() {
        viewModelScope.launch {
            val uid = _currentUserId.value
            exitRoom()
            repository.logoutUser(uid)
            authManager.logout()
            showToast("Logged out successfully.")
            openAuth()
        }
    }

    fun deleteCurrentUserAccount() {
        viewModelScope.launch {
            val uid = _currentUserId.value
            exitRoom()
            repository.deleteAccount(uid)
            authManager.logout()
            showToast("Account deleted. All personal data removed.")
            openAuth()
        }
    }

    fun switchUser(userId: Long) {
        _currentUserId.value = userId
        showToast("Switched active user to ID: $userId")
    }

    fun updateMyProfile(nickname: String, bio: String, gender: String, country: String, avatarUrl: String) {
        val userId = _currentUserId.value
        viewModelScope.launch {
            repository.updateUserProfile(userId, nickname, bio, gender, country, avatarUrl)
            showToast("Profile updated! 🌟")
        }
    }

    fun markNotificationsRead() {
        val userId = _currentUserId.value
        viewModelScope.launch {
            repository.markAllNotificationsRead(userId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceEngine.release()
    }
}
