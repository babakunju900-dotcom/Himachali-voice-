package com.example.data

import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.random.Random

data class PlatformStats(
    val totalUsers: Int,
    val onlineUsers: Int,
    val activeRooms: Int,
    val activePrivateCalls: Int,
    val cpConnections: Int,
    val totalEvents: Int,
    val openTickets: Int,
    val pendingReports: Int,
    val bannedUsers: Int,
    val totalRevenueCoins: Long,
    val dailyActiveUsers: Int,
    val newRegistrationsToday: Int
)

class StarKingRepository(
    private val dao: StarKingDao,
    val firestoreService: FirestoreProfileService? = null
) {

    // --- Users ---
    fun getUser(userId: Long): Flow<UserEntity?> = dao.getUserById(userId)
    fun getAllActiveUsers(): Flow<List<UserEntity>> = dao.getAllActiveUsers()
    fun searchUsers(query: String): Flow<List<UserEntity>> = dao.searchUsers(query)
    fun getOfficialUsers(): Flow<List<UserEntity>> = dao.getOfficialUsers()
    fun getTopHosts(limit: Int = 10): Flow<List<UserEntity>> = dao.getTopHosts(limit)

    suspend fun findUserByAuthIdentifier(identifier: String): UserEntity? = withContext(Dispatchers.IO) {
        if (identifier.isBlank()) null else dao.getUserByAuthIdentifier(identifier)
    }

    suspend fun registerUser(
        nickname: String,
        gender: String,
        country: String,
        dob: String,
        language: String,
        bio: String,
        avatarUrl: String = "avatar_1",
        authProvider: String = "GUEST",
        authIdentifier: String = "",
        deviceInfo: String = "",
        sessionId: String = ""
    ): UserEntity = withContext(Dispatchers.IO) {
        // If an account with this authIdentifier already exists, update session and return it
        if (authIdentifier.isNotBlank()) {
            val existing = dao.getUserByAuthIdentifier(authIdentifier)
            if (existing != null) {
                val updated = existing.copy(
                    lastLoginAt = System.currentTimeMillis(),
                    deviceInfo = deviceInfo.ifBlank { existing.deviceInfo },
                    sessionId = sessionId.ifBlank { existing.sessionId }
                )
                dao.updateUser(updated)
                return@withContext updated
            }
        }

        // Generate a permanent 6-digit numeric User ID (between 500000 and 999999)
        var newUserId = 500000L + Random.nextLong(100000, 499999)
        while (dao.getUserByIdSync(newUserId) != null) {
            newUserId = 500000L + Random.nextLong(100000, 499999)
        }

        val newUser = UserEntity(
            userId = newUserId,
            nickname = nickname.trim().ifEmpty { "StarUser$newUserId" },
            avatarUrl = avatarUrl,
            gender = gender,
            country = country,
            dob = dob,
            language = language,
            bio = bio.ifEmpty { "Hello from Star King! 🌟" },
            level = 1,
            exp = 0,
            vipTier = 0,
            role = UserRole.USER.name,
            createdAt = System.currentTimeMillis(),
            authProvider = authProvider,
            authIdentifier = authIdentifier,
            deviceInfo = deviceInfo,
            sessionId = sessionId,
            lastLoginAt = System.currentTimeMillis()
        )

        dao.insertUser(newUser)

        // Create welcome wallet with 1,000 welcome coins
        dao.insertWallet(WalletEntity(userId = newUserId, coinBalance = 1000L))
        dao.insertTransaction(
            WalletTransactionEntity(
                userId = newUserId,
                amount = 1000L,
                type = "REWARD",
                description = "New user registration welcome bonus"
            )
        )
        dao.insertNotification(
            NotificationEntity(
                userId = newUserId,
                title = "Welcome to Star King Voice Chat!",
                message = "Your permanent User ID is $newUserId. 1,000 welcome coins have been credited to your wallet.",
                type = "SYSTEM"
            )
        )

        newUser
    }

    suspend fun deleteAccount(userId: Long) = withContext(Dispatchers.IO) {
        dao.clearUserFromAllSeats(userId)
        dao.deleteFollowsForUser(userId)
        dao.deleteUserCustomizations(userId)
        dao.deleteWalletTransactions(userId)
        dao.deleteWallet(userId)
        dao.deleteUser(userId)
    }

    suspend fun logoutUser(userId: Long) = withContext(Dispatchers.IO) {
        val existing = dao.getUserByIdSync(userId) ?: return@withContext
        dao.updateUser(existing.copy(sessionId = ""))
    }

    suspend fun updateUserProfile(
        userId: Long,
        nickname: String,
        bio: String,
        gender: String,
        country: String,
        avatarUrl: String
    ) = withContext(Dispatchers.IO) {
        val existing = dao.getUserByIdSync(userId) ?: return@withContext
        val updated = existing.copy(
            nickname = nickname.trim(),
            bio = bio.trim(),
            gender = gender,
            country = country,
            avatarUrl = avatarUrl
        )
        dao.updateUser(updated)
    }

    suspend fun assignOfficialVerification(userId: Long, isVerified: Boolean) = withContext(Dispatchers.IO) {
        val existing = dao.getUserByIdSync(userId) ?: return@withContext
        dao.updateUser(
            existing.copy(
                isOfficialVerified = isVerified,
                equippedBadge = if (isVerified) "✓ OFFICIAL" else ""
            )
        )
    }

    suspend fun assignStaffRole(userId: Long, role: String) = withContext(Dispatchers.IO) {
        val existing = dao.getUserByIdSync(userId) ?: return@withContext
        dao.updateUser(existing.copy(role = role))
    }

    suspend fun setUserBanned(userId: Long, isBanned: Boolean, reason: String) = withContext(Dispatchers.IO) {
        val existing = dao.getUserByIdSync(userId) ?: return@withContext
        dao.updateUser(existing.copy(isBanned = isBanned, banReason = reason))
    }

    // --- Wallet & Coins ---
    fun getWallet(userId: Long): Flow<WalletEntity?> = dao.getWallet(userId)
    fun getTransactions(userId: Long): Flow<List<WalletTransactionEntity>> = dao.getTransactions(userId)
    fun getCoinPackages(): Flow<List<CoinPackageEntity>> = dao.getCoinPackages()

    suspend fun rechargeCoins(userId: Long, packageId: String): Result<Long> = withContext(Dispatchers.IO) {
        val packages = dao.getCoinPackages().firstOrNull() ?: emptyList()
        val pkg = packages.find { it.id == packageId } ?: return@withContext Result.failure(Exception("Invalid package"))

        val totalCoinsToAdd = pkg.coins + pkg.bonusCoins
        val currentWallet = dao.getWalletSync(userId) ?: WalletEntity(userId = userId)

        val updatedWallet = currentWallet.copy(
            coinBalance = currentWallet.coinBalance + totalCoinsToAdd,
            totalRecharged = currentWallet.totalRecharged + totalCoinsToAdd
        )
        dao.updateWallet(updatedWallet)

        dao.insertTransaction(
            WalletTransactionEntity(
                userId = userId,
                amount = totalCoinsToAdd,
                type = "RECHARGE",
                description = "Recharge ${pkg.coins} coins (+${pkg.bonusCoins} bonus) via Secure Gateway",
                referenceId = "PAY-${System.currentTimeMillis()}-${Random.nextInt(1000, 9999)}"
            )
        )

        dao.insertNotification(
            NotificationEntity(
                userId = userId,
                title = "Coins Recharged Successfully!",
                message = "Successfully credited $totalCoinsToAdd coins to your wallet.",
                type = "RECHARGE"
            )
        )

        Result.success(totalCoinsToAdd)
    }

    // --- Virtual Gifts ---
    fun getAllGifts(): Flow<List<GiftEntity>> = dao.getAllGifts()
    fun getRecentRoomGifts(roomId: Long): Flow<List<GiftTransactionEntity>> = dao.getRecentRoomGifts(roomId)

    suspend fun sendGift(
        roomId: Long,
        senderUser: UserEntity,
        receiverUserId: Long,
        receiverName: String,
        giftId: String,
        count: Int
    ): Result<GiftTransactionEntity> = withContext(Dispatchers.IO) {
        val gift = dao.getGiftById(giftId) ?: return@withContext Result.failure(Exception("Gift not found"))
        val totalCost = gift.coinPrice * count

        val senderWallet = dao.getWalletSync(senderUser.userId)
            ?: return@withContext Result.failure(Exception("Wallet not found"))

        if (senderWallet.coinBalance < totalCost) {
            return@withContext Result.failure(Exception("Insufficient coins! Please recharge."))
        }

        // Deduct from sender wallet
        dao.updateWallet(
            senderWallet.copy(
                coinBalance = senderWallet.coinBalance - totalCost,
                totalSpent = senderWallet.totalSpent + totalCost
            )
        )

        // 50% commission credited as diamonds to receiver
        val diamondCredit = (totalCost * 0.5).toLong()
        val receiverWallet = dao.getWalletSync(receiverUserId) ?: WalletEntity(userId = receiverUserId)
        dao.updateWallet(
            receiverWallet.copy(
                diamondBalance = receiverWallet.diamondBalance + diamondCredit
            )
        )

        // Update receiver's gifts received count
        val receiverUser = dao.getUserByIdSync(receiverUserId)
        if (receiverUser != null) {
            dao.updateUser(receiverUser.copy(giftsReceivedCount = receiverUser.giftsReceivedCount + count))
        }

        // Increase sender Exp & Level (1 coin = 1 exp; level up at level * 1000)
        val newExp = senderUser.exp + totalCost.toInt()
        val calculatedLevel = 1 + (newExp / 1000)
        if (calculatedLevel > senderUser.level || newExp != senderUser.exp) {
            dao.updateUser(senderUser.copy(exp = newExp, level = calculatedLevel))
        }

        // Update Room total gifts value
        val room = dao.getRoomByIdSync(roomId)
        if (room != null) {
            dao.updateRoom(room.copy(totalGiftsValue = room.totalGiftsValue + totalCost))
        }

        // Insert Gift Transaction
        val giftTx = GiftTransactionEntity(
            roomId = roomId,
            senderUserId = senderUser.userId,
            senderName = senderUser.nickname,
            receiverUserId = receiverUserId,
            receiverName = receiverName,
            giftId = gift.id,
            giftName = gift.name,
            giftIcon = gift.iconEmoji,
            giftCount = count,
            totalCoins = totalCost,
            timestamp = System.currentTimeMillis()
        )
        dao.insertGiftTransaction(giftTx)

        // Add ledger transactions
        dao.insertTransaction(
            WalletTransactionEntity(
                userId = senderUser.userId,
                amount = -totalCost,
                type = "GIFT_SENT",
                description = "Sent ${count}x ${gift.name} ${gift.iconEmoji} to $receiverName"
            )
        )
        dao.insertTransaction(
            WalletTransactionEntity(
                userId = receiverUserId,
                amount = diamondCredit,
                type = "GIFT_RECEIVED",
                description = "Received ${count}x ${gift.name} from ${senderUser.nickname} (+$diamondCredit 💎)"
            )
        )

        // In-room chat announcement
        dao.insertMessage(
            ChatMessageEntity(
                roomId = roomId,
                senderUserId = senderUser.userId,
                senderName = senderUser.nickname,
                senderAvatar = senderUser.avatarUrl,
                senderLevel = senderUser.level,
                senderVip = senderUser.vipTier,
                messageText = "sent ${count}x ${gift.name} ${gift.iconEmoji} to $receiverName!",
                type = "GIFT",
                giftName = gift.name,
                giftIcon = gift.iconEmoji,
                giftCount = count
            )
        )

        // Notification to receiver
        dao.insertNotification(
            NotificationEntity(
                userId = receiverUserId,
                title = "Gift Received! 🎁",
                message = "${senderUser.nickname} sent you ${count}x ${gift.name} ${gift.iconEmoji} in the voice room!",
                type = "GIFT"
            )
        )

        Result.success(giftTx)
    }

    // --- VIP & Customization Store ---
    fun getVipPlans(): Flow<List<VipPlanEntity>> = dao.getVipPlans()
    fun getStoreCustomizations(): Flow<List<StoreCustomizationEntity>> = dao.getStoreCustomizations()
    fun getUserCustomizations(userId: Long): Flow<List<UserCustomizationEntity>> = dao.getUserCustomizations(userId)

    suspend fun purchaseVip(userId: Long, planId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val plans = dao.getVipPlans().firstOrNull() ?: emptyList()
        val plan = plans.find { it.id == planId } ?: return@withContext Result.failure(Exception("Plan not found"))

        val wallet = dao.getWalletSync(userId) ?: return@withContext Result.failure(Exception("Wallet not found"))
        if (wallet.coinBalance < plan.priceCoins) {
            return@withContext Result.failure(Exception("Insufficient coins for VIP!"))
        }

        dao.updateWallet(wallet.copy(coinBalance = wallet.coinBalance - plan.priceCoins))
        val user = dao.getUserByIdSync(userId) ?: return@withContext Result.failure(Exception("User not found"))
        val expiryTime = System.currentTimeMillis() + (plan.durationDays * 86400000L)

        dao.updateUser(
            user.copy(
                vipTier = plan.tier,
                vipExpiry = expiryTime,
                equippedBadge = plan.badgeTitle
            )
        )

        dao.insertTransaction(
            WalletTransactionEntity(
                userId = userId,
                amount = -plan.priceCoins,
                type = "VIP_PURCHASE",
                description = "Subscribed to ${plan.name} (${plan.durationDays} days)"
            )
        )

        dao.insertNotification(
            NotificationEntity(
                userId = userId,
                title = "VIP Activated! 👑",
                message = "Congratulations! You are now a ${plan.name} member with exclusive privileges.",
                type = "VIP"
            )
        )

        Result.success(true)
    }

    suspend fun purchaseCustomization(userId: Long, itemId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val items = dao.getStoreCustomizations().firstOrNull() ?: emptyList()
        val item = items.find { it.id == itemId } ?: return@withContext Result.failure(Exception("Item not found"))

        val wallet = dao.getWalletSync(userId) ?: return@withContext Result.failure(Exception("Wallet not found"))
        if (wallet.coinBalance < item.priceCoins) {
            return@withContext Result.failure(Exception("Insufficient coins for this customization!"))
        }

        dao.updateWallet(wallet.copy(coinBalance = wallet.coinBalance - item.priceCoins))
        val user = dao.getUserByIdSync(userId) ?: return@withContext Result.failure(Exception("User not found"))

        val updatedUser = when (item.type) {
            "FRAME" -> user.copy(equippedFrame = item.name)
            "BADGE", "MEDAL" -> user.copy(equippedBadge = item.name)
            "TITLE" -> user.copy(equippedTitle = item.name)
            else -> user
        }
        dao.updateUser(updatedUser)

        dao.insertUserCustomization(
            UserCustomizationEntity(
                userId = userId,
                itemId = itemId,
                isEquipped = true,
                expiresAt = System.currentTimeMillis() + (item.durationDays * 86400000L)
            )
        )

        dao.insertTransaction(
            WalletTransactionEntity(
                userId = userId,
                amount = -item.priceCoins,
                type = "STORE_PURCHASE",
                description = "Purchased ${item.name} (${item.type})"
            )
        )

        Result.success(true)
    }

    suspend fun equipCustomization(userId: Long, itemId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val item = dao.getStoreCustomizationById(itemId)
            ?: return@withContext Result.failure(Exception("Item not found"))
        val user = dao.getUserByIdSync(userId)
            ?: return@withContext Result.failure(Exception("User not found"))

        val updatedUser = when (item.type) {
            "FRAME" -> user.copy(equippedFrame = item.name)
            "BADGE", "MEDAL" -> user.copy(equippedBadge = item.name)
            "TITLE" -> user.copy(equippedTitle = item.name)
            else -> user
        }
        dao.updateUser(updatedUser)

        dao.insertUserCustomization(
            UserCustomizationEntity(
                userId = userId,
                itemId = itemId,
                isEquipped = true,
                expiresAt = System.currentTimeMillis() + (365L * 86400000L)
            )
        )

        Result.success(true)
    }

    // --- Rooms ---
    fun getLiveRooms(): Flow<List<RoomEntity>> = dao.getLiveRooms()
    fun getRoom(roomId: Long): Flow<RoomEntity?> = dao.getRoomById(roomId)
    fun searchRooms(query: String): Flow<List<RoomEntity>> = dao.searchRooms(query)
    fun getRoomsByHost(userId: Long): Flow<List<RoomEntity>> = dao.getRoomsByHost(userId)

    suspend fun createRoom(
        hostUser: UserEntity,
        name: String,
        description: String,
        category: String,
        language: String,
        seatCount: Int,
        welcomeMsg: String,
        isPrivate: Boolean,
        password: String,
        coverEmoji: String = "🎙️",
        coverPhotoUrl: String = ""
    ): RoomEntity = withContext(Dispatchers.IO) {
        var roomId = 700000L + Random.nextLong(10000, 99999)
        while (dao.getRoomByIdSync(roomId) != null) {
            roomId = 700000L + Random.nextLong(10000, 99999)
        }

        val room = RoomEntity(
            roomId = roomId,
            name = name.trim().ifEmpty { "${hostUser.nickname}'s Voice Party" },
            description = description.trim().ifEmpty { "Join my voice room and party!" },
            coverEmoji = coverEmoji,
            coverPhotoUrl = coverPhotoUrl,
            category = category,
            language = language,
            country = hostUser.country,
            isPrivate = isPrivate,
            password = password,
            seatCount = seatCount,
            welcomeMessage = welcomeMsg.ifEmpty { "Welcome to Star King! Respect everyone and enjoy! 🌟" },
            hostUserId = hostUser.userId,
            hostName = hostUser.nickname,
            hostAvatar = hostUser.avatarUrl,
            isLive = true,
            onlineCount = 1
        )
        dao.insertRoom(room)

        // Seed seats: seat 0 is Host seat
        val seats = (0 until seatCount).map { idx ->
            if (idx == 0) {
                RoomSeatEntity(
                    roomId = roomId,
                    seatIndex = 0,
                    userId = hostUser.userId,
                    userName = hostUser.nickname,
                    userAvatar = hostUser.avatarUrl,
                    userLevel = hostUser.level,
                    userVip = hostUser.vipTier,
                    isSpeaking = false
                )
            } else {
                RoomSeatEntity(roomId = roomId, seatIndex = idx, userId = null, isLocked = false)
            }
        }
        dao.insertSeats(seats)

        // Welcome system message in chat
        dao.insertMessage(
            ChatMessageEntity(
                roomId = roomId,
                senderUserId = 100001L,
                senderName = "System",
                senderAvatar = "avatar_crown",
                messageText = room.welcomeMessage,
                type = "SYSTEM"
            )
        )

        room
    }

    suspend fun updateRoomSettings(roomId: Long, name: String, description: String, welcomeMsg: String) =
        withContext(Dispatchers.IO) {
            val room = dao.getRoomByIdSync(roomId) ?: return@withContext
            dao.updateRoom(
                room.copy(
                    name = name.trim(),
                    description = description.trim(),
                    welcomeMessage = welcomeMsg.trim()
                )
            )
        }

    suspend fun updateRoomCoverPhoto(roomId: Long, coverPhotoUrl: String) = withContext(Dispatchers.IO) {
        dao.updateRoomCoverPhoto(roomId, coverPhotoUrl)
    }

    suspend fun updateUser(user: UserEntity) = withContext(Dispatchers.IO) {
        dao.updateUser(user)
    }

    suspend fun updateUserProfilePhoto(userId: Long, newAvatarUrl: String) = withContext(Dispatchers.IO) {
        dao.updateUserAvatar(userId, newAvatarUrl)
    }

    suspend fun deleteRoomPermanently(roomId: Long) = withContext(Dispatchers.IO) {
        dao.deleteRoom(roomId)
    }

    suspend fun closeRoom(roomId: Long) = withContext(Dispatchers.IO) {
        val room = dao.getRoomByIdSync(roomId) ?: return@withContext
        dao.updateRoom(room.copy(isLive = false))
        dao.deleteRoom(roomId)
    }

    // --- Seats ---
    fun getSeatsForRoom(roomId: Long): Flow<List<RoomSeatEntity>> = dao.getSeatsForRoom(roomId)

    suspend fun takeSeat(roomId: Long, seatIndex: Int, user: UserEntity): Result<Boolean> = withContext(Dispatchers.IO) {
        val seats = dao.getSeatsForRoomSync(roomId)
        val targetSeat = seats.find { it.seatIndex == seatIndex }
            ?: return@withContext Result.failure(Exception("Seat does not exist"))

        if (targetSeat.isLocked) {
            return@withContext Result.failure(Exception("This seat is locked by host"))
        }
        if (targetSeat.userId != null && targetSeat.userId != user.userId) {
            return@withContext Result.failure(Exception("Seat is already taken"))
        }

        // Leave any prior seat in this room
        dao.clearUserFromSeats(roomId, user.userId)

        // Take target seat
        dao.updateSeat(
            targetSeat.copy(
                userId = user.userId,
                userName = user.nickname,
                userAvatar = user.avatarUrl,
                userLevel = user.level,
                userVip = user.vipTier,
                isSpeaking = false,
                joinedSeatTime = System.currentTimeMillis()
            )
        )

        dao.insertMessage(
            ChatMessageEntity(
                roomId = roomId,
                senderUserId = user.userId,
                senderName = user.nickname,
                senderAvatar = user.avatarUrl,
                senderLevel = user.level,
                senderVip = user.vipTier,
                messageText = "took seat #${seatIndex + 1} 🎙️",
                type = "SEAT_ACTION"
            )
        )

        Result.success(true)
    }

    suspend fun leaveSeat(roomId: Long, userId: Long) = withContext(Dispatchers.IO) {
        dao.clearUserFromSeats(roomId, userId)
    }

    suspend fun toggleLockSeat(roomId: Long, seatIndex: Int) = withContext(Dispatchers.IO) {
        val seats = dao.getSeatsForRoomSync(roomId)
        val targetSeat = seats.find { it.seatIndex == seatIndex } ?: return@withContext
        dao.updateSeat(targetSeat.copy(isLocked = !targetSeat.isLocked))
    }

    suspend fun toggleMuteSeat(roomId: Long, seatIndex: Int) = withContext(Dispatchers.IO) {
        val seats = dao.getSeatsForRoomSync(roomId)
        val targetSeat = seats.find { it.seatIndex == seatIndex } ?: return@withContext
        dao.updateSeat(targetSeat.copy(isMuted = !targetSeat.isMuted))
    }

    suspend fun kickUserFromSeat(roomId: Long, seatIndex: Int) = withContext(Dispatchers.IO) {
        val seats = dao.getSeatsForRoomSync(roomId)
        val targetSeat = seats.find { it.seatIndex == seatIndex } ?: return@withContext
        dao.updateSeat(targetSeat.copy(userId = null, userName = null, userAvatar = null, isSpeaking = false))
    }

    suspend fun updateSeatSpeaking(roomId: Long, userId: Long, isSpeaking: Boolean) = withContext(Dispatchers.IO) {
        dao.updateSeatSpeakingState(roomId, userId, isSpeaking)
    }

    // --- Chat Messages ---
    fun getRoomMessages(roomId: Long): Flow<List<ChatMessageEntity>> = dao.getRoomMessages(roomId)

    suspend fun sendRoomMessage(roomId: Long, sender: UserEntity, text: String) = withContext(Dispatchers.IO) {
        if (text.isBlank()) return@withContext
        dao.insertMessage(
            ChatMessageEntity(
                roomId = roomId,
                senderUserId = sender.userId,
                senderName = sender.nickname,
                senderAvatar = sender.avatarUrl,
                senderLevel = sender.level,
                senderVip = sender.vipTier,
                messageText = text.trim(),
                type = "TEXT"
            )
        )
    }

    fun getPrivateMessages(u1: Long, u2: Long): Flow<List<ChatMessageEntity>> = dao.getPrivateMessages(u1, u2)
    fun getRecentPrivateConversations(userId: Long): Flow<List<ChatMessageEntity>> = dao.getRecentPrivateMessages(userId)

    suspend fun sendPrivateMessage(sender: UserEntity, recipientId: Long, text: String) = withContext(Dispatchers.IO) {
        if (text.isBlank()) return@withContext
        dao.insertMessage(
            ChatMessageEntity(
                recipientUserId = recipientId,
                senderUserId = sender.userId,
                senderName = sender.nickname,
                senderAvatar = sender.avatarUrl,
                senderLevel = sender.level,
                senderVip = sender.vipTier,
                messageText = text.trim(),
                type = "TEXT"
            )
        )
    }

    suspend fun markMessagesAsRead(senderId: Long, myUserId: Long) = withContext(Dispatchers.IO) {
        dao.markMessagesAsRead(senderId, myUserId)
    }

    // --- Follows ---
    fun isFollowing(followerId: Long, followingId: Long): Flow<Boolean> =
        dao.isFollowing(followerId, followingId).map { it > 0 }

    suspend fun toggleFollow(followerId: Long, followingId: Long): Boolean = withContext(Dispatchers.IO) {
        val isFollowing = dao.isFollowingSync(followerId, followingId) > 0
        if (isFollowing) {
            dao.deleteFollow(followerId, followingId)
            val followingUser = dao.getUserByIdSync(followingId)
            if (followingUser != null) {
                dao.updateUser(followingUser.copy(followersCount = maxOf(0, followingUser.followersCount - 1)))
            }
            val followerUser = dao.getUserByIdSync(followerId)
            if (followerUser != null) {
                dao.updateUser(followerUser.copy(followingCount = maxOf(0, followerUser.followingCount - 1)))
            }
            false
        } else {
            dao.insertFollow(FollowEntity(followerUserId = followerId, followingUserId = followingId))
            val followingUser = dao.getUserByIdSync(followingId)
            if (followingUser != null) {
                dao.updateUser(followingUser.copy(followersCount = followingUser.followersCount + 1))
            }
            val followerUser = dao.getUserByIdSync(followerId)
            if (followerUser != null) {
                dao.updateUser(followerUser.copy(followingCount = followerUser.followingCount + 1))
            }
            dao.insertNotification(
                NotificationEntity(
                    userId = followingId,
                    title = "New Follower! 🌟",
                    message = "${followerUser?.nickname ?: "A user"} started following you.",
                    type = "FOLLOW"
                )
            )
            true
        }
    }

    // --- Support & Moderation ---
    fun getTicketsForUser(userId: Long): Flow<List<SupportTicketEntity>> = dao.getTicketsForUser(userId)
    fun getAllTickets(): Flow<List<SupportTicketEntity>> = dao.getAllTickets()

    suspend fun createSupportTicket(
        userId: Long,
        nickname: String,
        category: String,
        subject: String,
        message: String
    ): String = withContext(Dispatchers.IO) {
        val ticketId = "TCK-${Random.nextInt(1000, 9999)}"
        dao.insertTicket(
            SupportTicketEntity(
                ticketId = ticketId,
                userId = userId,
                userNickname = nickname,
                category = category,
                subject = subject,
                message = message,
                status = "OPEN"
            )
        )
        ticketId
    }

    suspend fun updateSupportTicket(ticketId: String, staffReply: String, status: String) =
        withContext(Dispatchers.IO) {
            val tickets = dao.getAllTickets().firstOrNull() ?: emptyList()
            val ticket = tickets.find { it.ticketId == ticketId } ?: return@withContext
            dao.updateTicket(
                ticket.copy(
                    staffReply = staffReply,
                    status = status,
                    updatedAt = System.currentTimeMillis()
                )
            )
            dao.insertNotification(
                NotificationEntity(
                    userId = ticket.userId,
                    title = "Support Ticket Update: $ticketId",
                    message = "Official staff responded: '$staffReply' (Status: $status)",
                    type = "SYSTEM"
                )
            )
        }

    fun getAllReports(): Flow<List<ReportEntity>> = dao.getAllReports()

    suspend fun submitReport(
        reporterId: Long,
        reporterName: String,
        reportedUserId: Long?,
        reportedUserName: String?,
        reportedRoomId: Long?,
        reportedRoomName: String?,
        category: String,
        details: String
    ) = withContext(Dispatchers.IO) {
        dao.insertReport(
            ReportEntity(
                reporterUserId = reporterId,
                reporterName = reporterName,
                reportedUserId = reportedUserId,
                reportedUserName = reportedUserName,
                reportedRoomId = reportedRoomId,
                reportedRoomName = reportedRoomName,
                reasonCategory = category,
                details = details,
                status = "PENDING"
            )
        )
    }

    suspend fun reviewReport(reportId: Long, newStatus: String, notes: String) = withContext(Dispatchers.IO) {
        val reports = dao.getAllReports().firstOrNull() ?: emptyList()
        val report = reports.find { it.id == reportId } ?: return@withContext
        dao.updateReport(report.copy(status = newStatus, moderatorNotes = notes))
    }

    fun getAllModerationActions(): Flow<List<ModerationActionEntity>> = dao.getAllModerationActions()

    suspend fun logModerationAction(modId: Long, targetId: Long, actionType: String, reason: String) =
        withContext(Dispatchers.IO) {
            dao.insertModerationAction(
                ModerationActionEntity(
                    moderatorUserId = modId,
                    targetUserId = targetId,
                    actionType = actionType,
                    reason = reason
                )
            )
        }

    // --- Events & Agencies ---
    fun getAllEvents(): Flow<List<EventEntity>> = dao.getAllEvents()
    fun getAllAgencies(): Flow<List<AgencyEntity>> = dao.getAllAgencies()

    // --- Notifications ---
    fun getNotifications(userId: Long): Flow<List<NotificationEntity>> = dao.getNotifications(userId)
    suspend fun markAllNotificationsRead(userId: Long) = withContext(Dispatchers.IO) {
        dao.markAllNotificationsRead(userId)
    }

    // --- Platform Stats for Admin Dashboard ---
    suspend fun getPlatformStats(): PlatformStats = withContext(Dispatchers.IO) {
        val userCount = dao.getUserCount()
        val roomCount = dao.getActiveRoomCount()
        val totalCoins = dao.getTotalPlatformGiftCoins() ?: 950000L
        val tickets = dao.getAllTickets().firstOrNull() ?: emptyList()
        val openTickets = tickets.count { it.status == "OPEN" || it.status == "IN_PROGRESS" }
        val reports = dao.getAllReports().firstOrNull() ?: emptyList()
        val pendingReports = reports.count { it.status == "PENDING" }
        val bannedCount = dao.getBannedUsersCount().firstOrNull() ?: 0
        val cpCount = dao.getActiveCpConnectionsCount().firstOrNull() ?: 3
        val callCount = dao.getActiveCallSessionsCount().firstOrNull() ?: 1
        val eventCount = dao.getAllEvents().firstOrNull()?.size ?: 4

        PlatformStats(
            totalUsers = userCount,
            onlineUsers = maxOf(42, userCount * 3 / 4),
            activeRooms = roomCount,
            activePrivateCalls = callCount,
            cpConnections = cpCount,
            totalEvents = eventCount,
            openTickets = openTickets,
            pendingReports = pendingReports,
            bannedUsers = bannedCount,
            totalRevenueCoins = totalCoins,
            dailyActiveUsers = maxOf(35, userCount * 2 / 3),
            newRegistrationsToday = maxOf(12, userCount / 5)
        )
    }

    // --- Hand Raises ---
    fun getHandRaises(roomId: Long): Flow<List<RoomHandRaiseEntity>> = dao.getHandRaisesForRoom(roomId)

    suspend fun raiseHand(roomId: Long, user: UserEntity) = withContext(Dispatchers.IO) {
        dao.insertHandRaise(
            RoomHandRaiseEntity(
                roomId = roomId,
                userId = user.userId,
                userName = user.nickname,
                userAvatar = user.avatarUrl,
                userLevel = user.level,
                userVip = user.vipTier
            )
        )
        dao.insertMessage(
            ChatMessageEntity(
                roomId = roomId,
                senderUserId = user.userId,
                senderName = user.nickname,
                senderAvatar = user.avatarUrl,
                messageText = "raised hand to speak ✋",
                type = "SEAT_ACTION"
            )
        )
    }

    suspend fun cancelHandRaise(roomId: Long, userId: Long) = withContext(Dispatchers.IO) {
        dao.deleteHandRaise(roomId, userId)
    }

    suspend fun acceptHandRaise(roomId: Long, user: UserEntity, seatIndex: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        dao.deleteHandRaise(roomId, user.userId)
        takeSeat(roomId, seatIndex, user)
    }

    // --- Followers / Following Users ---
    fun getFollowersUsers(userId: Long): Flow<List<UserEntity>> = dao.getFollowersUsers(userId)
    fun getFollowingUsers(userId: Long): Flow<List<UserEntity>> = dao.getFollowingUsers(userId)

    // --- Diamond to Coin Conversion (Host Earnings) ---
    suspend fun convertDiamondsToCoins(userId: Long, diamonds: Long): Result<Long> = withContext(Dispatchers.IO) {
        val wallet = dao.getWalletSync(userId) ?: return@withContext Result.failure(Exception("Wallet not found"))
        if (wallet.diamondBalance < diamonds) {
            return@withContext Result.failure(Exception("Insufficient diamonds"))
        }
        val coinsToAdd = diamonds // 1 diamond = 1 coin conversion
        dao.updateWallet(
            wallet.copy(
                diamondBalance = wallet.diamondBalance - diamonds,
                coinBalance = wallet.coinBalance + coinsToAdd
            )
        )
        dao.insertTransaction(
            WalletTransactionEntity(
                userId = userId,
                amount = coinsToAdd,
                type = "REWARD",
                description = "Converted $diamonds 💎 diamonds to $coinsToAdd 🪙 coins"
            )
        )
        Result.success(coinsToAdd)
    }

    // =========================================================================
    // CP (Direct Connection System) Methods
    // =========================================================================
    fun getCpConnectionsForUser(userId: Long): Flow<List<CpConnectionEntity>> =
        dao.getCpConnectionsForUser(userId)

    fun getAllCpConnections(): Flow<List<CpConnectionEntity>> =
        dao.getAllCpConnections()

    fun getActiveCpConnectionsCount(): Flow<Int> =
        dao.getActiveCpConnectionsCount()

    suspend fun sendCpRequest(requester: UserEntity, target: UserEntity): Result<CpConnectionEntity> = withContext(Dispatchers.IO) {
        if (target.isBanned || target.isSuspended) {
            return@withContext Result.failure(Exception("Cannot connect with this user at this moment."))
        }
        val minId = minOf(requester.userId, target.userId)
        val maxId = maxOf(requester.userId, target.userId)
        val connId = "cp_${minId}_${maxId}"

        val existing = dao.getCpConnectionSync(requester.userId, target.userId)
        if (existing != null) {
            if (existing.status == CpStatus.BLOCKED.name) {
                return@withContext Result.failure(Exception("Connection request cannot be delivered."))
            }
            if (existing.status == CpStatus.ACCEPTED.name) {
                return@withContext Result.failure(Exception("You are already connected with ${target.nickname}!"))
            }
            if (existing.status == CpStatus.REQUEST_SENT.name && existing.requesterId == requester.userId) {
                return@withContext Result.failure(Exception("Connection request already sent. Waiting for response."))
            }
        }

        val conn = CpConnectionEntity(
            connectionId = connId,
            user1Id = minId,
            user1Name = if (minId == requester.userId) requester.nickname else target.nickname,
            user1Avatar = if (minId == requester.userId) requester.avatarUrl else target.avatarUrl,
            user2Id = maxId,
            user2Name = if (maxId == requester.userId) requester.nickname else target.nickname,
            user2Avatar = if (maxId == requester.userId) requester.avatarUrl else target.avatarUrl,
            requesterId = requester.userId,
            status = CpStatus.REQUEST_SENT.name,
            updatedAt = System.currentTimeMillis()
        )
        dao.insertCpConnection(conn)

        dao.insertNotification(
            NotificationEntity(
                userId = target.userId,
                title = "💞 New CP Connection Request",
                message = "${requester.nickname} wants to connect with you on STAR Voice CP!",
                type = "CP_REQUEST"
            )
        )

        Result.success(conn)
    }

    suspend fun acceptCpRequest(connectionId: String, currentUserId: Long): Result<Boolean> = withContext(Dispatchers.IO) {
        val parts = connectionId.removePrefix("cp_").split("_")
        if (parts.size != 2) return@withContext Result.failure(Exception("Invalid connection ID"))
        val u1 = parts[0].toLongOrNull() ?: return@withContext Result.failure(Exception("Invalid ID"))
        val u2 = parts[1].toLongOrNull() ?: return@withContext Result.failure(Exception("Invalid ID"))
        val conn = dao.getCpConnectionSync(u1, u2) ?: return@withContext Result.failure(Exception("Request not found"))

        val updated = conn.copy(
            status = CpStatus.ACCEPTED.name,
            updatedAt = System.currentTimeMillis()
        )
        dao.updateCpConnection(updated)

        val otherUserId = if (conn.user1Id == currentUserId) conn.user2Id else conn.user1Id
        val currentUser = dao.getUserByIdSync(currentUserId)
        dao.insertNotification(
            NotificationEntity(
                userId = otherUserId,
                title = "💖 CP Request Accepted!",
                message = "${currentUser?.nickname ?: "Your partner"} accepted your CP connection! 🌟",
                type = "CP_ACCEPTED"
            )
        )
        Result.success(true)
    }

    suspend fun declineCpRequest(connectionId: String, currentUserId: Long): Result<Boolean> = withContext(Dispatchers.IO) {
        val parts = connectionId.removePrefix("cp_").split("_")
        if (parts.size != 2) return@withContext Result.failure(Exception("Invalid connection ID"))
        val u1 = parts[0].toLongOrNull() ?: return@withContext Result.failure(Exception("Invalid ID"))
        val u2 = parts[1].toLongOrNull() ?: return@withContext Result.failure(Exception("Invalid ID"))
        val conn = dao.getCpConnectionSync(u1, u2) ?: return@withContext Result.failure(Exception("Request not found"))

        val updated = conn.copy(
            status = CpStatus.DECLINED.name,
            updatedAt = System.currentTimeMillis()
        )
        dao.updateCpConnection(updated)
        Result.success(true)
    }

    suspend fun endCpConnection(connectionId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        dao.deleteCpConnection(connectionId)
        Result.success(true)
    }

    suspend fun blockCpConnection(connectionId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val parts = connectionId.removePrefix("cp_").split("_")
        if (parts.size != 2) return@withContext Result.failure(Exception("Invalid connection ID"))
        val u1 = parts[0].toLongOrNull() ?: return@withContext Result.failure(Exception("Invalid ID"))
        val u2 = parts[1].toLongOrNull() ?: return@withContext Result.failure(Exception("Invalid ID"))
        val conn = dao.getCpConnectionSync(u1, u2) ?: return@withContext Result.failure(Exception("Request not found"))

        val updated = conn.copy(
            status = CpStatus.BLOCKED.name,
            updatedAt = System.currentTimeMillis()
        )
        dao.updateCpConnection(updated)
        Result.success(true)
    }

    // =========================================================================
    // Private Call System (Strictly 2 Persons Only)
    // =========================================================================
    fun getActiveCallSessionForUser(userId: Long): Flow<PrivateCallSessionEntity?> =
        dao.getActiveCallSessionForUser(userId)

    fun getCallHistoryForUser(userId: Long): Flow<List<PrivateCallHistoryEntity>> =
        dao.getCallHistoryForUser(userId)

    fun getActiveCallSessionsCount(): Flow<Int> =
        dao.getActiveCallSessionsCount()

    suspend fun startPrivateCall(
        caller: UserEntity,
        receiver: UserEntity,
        isVideo: Boolean
    ): Result<PrivateCallSessionEntity> = withContext(Dispatchers.IO) {
        if (receiver.isBanned || receiver.isSuspended) {
            return@withContext Result.failure(Exception("${receiver.nickname} is currently unavailable."))
        }

        val receiverActiveCall = dao.getActiveCallSessionForUserSync(receiver.userId)
        if (receiverActiveCall != null) {
            dao.insertCallHistory(
                PrivateCallHistoryEntity(
                    historyId = "hist_${System.currentTimeMillis()}_${caller.userId}",
                    userId = caller.userId,
                    otherUserId = receiver.userId,
                    otherUserName = receiver.nickname,
                    otherUserAvatar = receiver.avatarUrl,
                    isOutgoing = true,
                    isVideo = isVideo,
                    status = "Busy",
                    durationSeconds = 0
                )
            )
            return@withContext Result.failure(Exception("User is currently busy."))
        }

        val callerActiveCall = dao.getActiveCallSessionForUserSync(caller.userId)
        if (callerActiveCall != null) {
            return@withContext Result.failure(Exception("You are already in an active private call."))
        }

        val callId = "call_${System.currentTimeMillis()}_${caller.userId}"
        val session = PrivateCallSessionEntity(
            callId = callId,
            callerId = caller.userId,
            callerName = caller.nickname,
            callerAvatar = caller.avatarUrl,
            receiverId = receiver.userId,
            receiverName = receiver.nickname,
            receiverAvatar = receiver.avatarUrl,
            status = CallState.RINGING.name,
            isVideo = isVideo,
            startedAt = System.currentTimeMillis(),
            activeParticipantsCount = 2
        )
        dao.insertCallSession(session)

        dao.insertNotification(
            NotificationEntity(
                userId = receiver.userId,
                title = if (isVideo) "📹 Incoming Video Call" else "📞 Incoming Voice Call",
                message = "${caller.nickname} is calling you...",
                type = "PRIVATE_CALL"
            )
        )

        Result.success(session)
    }

    suspend fun acceptPrivateCall(callId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val session = dao.getCallSessionByIdSync(callId)
            ?: return@withContext Result.failure(Exception("Call session expired"))

        val updated = session.copy(
            status = CallState.CONNECTED.name,
            connectedAt = System.currentTimeMillis()
        )
        dao.updateCallSession(updated)
        Result.success(true)
    }

    suspend fun declinePrivateCall(callId: String, currentUserId: Long): Result<Boolean> = withContext(Dispatchers.IO) {
        val session = dao.getCallSessionByIdSync(callId)
            ?: return@withContext Result.failure(Exception("Call session not found"))

        dao.insertCallHistory(
            PrivateCallHistoryEntity(
                historyId = "hist_${System.currentTimeMillis()}_${session.callerId}",
                userId = session.callerId,
                otherUserId = session.receiverId,
                otherUserName = session.receiverName,
                otherUserAvatar = session.receiverAvatar,
                isOutgoing = true,
                isVideo = session.isVideo,
                status = "Declined",
                durationSeconds = 0
            )
        )
        dao.insertCallHistory(
            PrivateCallHistoryEntity(
                historyId = "hist_${System.currentTimeMillis()}_${session.receiverId}",
                userId = session.receiverId,
                otherUserId = session.callerId,
                otherUserName = session.callerName,
                otherUserAvatar = session.callerAvatar,
                isOutgoing = false,
                isVideo = session.isVideo,
                status = "Declined",
                durationSeconds = 0
            )
        )

        dao.deleteCallSession(callId)
        Result.success(true)
    }

    suspend fun endPrivateCall(callId: String, currentUserId: Long, endReason: String = "Call Ended"): Result<Boolean> = withContext(Dispatchers.IO) {
        val session = dao.getCallSessionByIdSync(callId) ?: return@withContext Result.success(true)

        val duration = if (session.connectedAt > 0L) {
            ((System.currentTimeMillis() - session.connectedAt) / 1000).toInt()
        } else 0

        val callStatus = if (duration > 0) "Completed" else if (session.status == CallState.RINGING.name) "Missed" else "Ended"

        dao.insertCallHistory(
            PrivateCallHistoryEntity(
                historyId = "hist_${System.currentTimeMillis()}_${session.callerId}",
                userId = session.callerId,
                otherUserId = session.receiverId,
                otherUserName = session.receiverName,
                otherUserAvatar = session.receiverAvatar,
                isOutgoing = true,
                isVideo = session.isVideo,
                status = callStatus,
                durationSeconds = duration
            )
        )

        dao.insertCallHistory(
            PrivateCallHistoryEntity(
                historyId = "hist_${System.currentTimeMillis()}_${session.receiverId}",
                userId = session.receiverId,
                otherUserId = session.callerId,
                otherUserName = session.callerName,
                otherUserAvatar = session.callerAvatar,
                isOutgoing = false,
                isVideo = session.isVideo,
                status = callStatus,
                durationSeconds = duration
            )
        )

        dao.deleteCallSession(callId)
        Result.success(true)
    }

    suspend fun clearCallHistory(userId: Long) = withContext(Dispatchers.IO) {
        dao.clearCallHistoryForUser(userId)
    }

    // =========================================================================
    // Admin Operations (Room, Event, User Moderation & Control)
    // =========================================================================
    fun getAllEventsAdmin(): Flow<List<EventEntity>> = dao.getAllEventsAdmin()
    fun getAllRoomsAdmin(): Flow<List<RoomEntity>> = dao.getAllRoomsAdmin()
    fun getAllUsersAdmin(): Flow<List<UserEntity>> = dao.getAllUsersAdmin()
    fun getBannedUsersCount(): Flow<Int> = dao.getBannedUsersCount()
    fun getTotalUsersCountFlow(): Flow<Int> = dao.getTotalUsersCountFlow()

    suspend fun adminCreateRoom(
        name: String,
        description: String,
        category: String,
        isPrivate: Boolean,
        password: String,
        coverPhotoUrl: String,
        ownerId: Long,
        ownerName: String,
        ownerAvatar: String
    ): RoomEntity = withContext(Dispatchers.IO) {
        val roomId = 700000L + kotlin.random.Random.nextLong(10000, 99999)
        val room = RoomEntity(
            roomId = roomId,
            name = name.trim(),
            description = description.trim(),
            category = category,
            isPrivate = isPrivate,
            password = password,
            coverPhotoUrl = coverPhotoUrl,
            hostUserId = ownerId,
            hostName = ownerName,
            hostAvatar = ownerAvatar,
            isLive = true,
            onlineCount = 1
        )
        dao.insertRoom(room)

        val seats = (0 until 8).map { idx ->
            RoomSeatEntity(
                roomId = roomId,
                seatIndex = idx,
                userId = if (idx == 0) ownerId else null,
                userName = if (idx == 0) ownerName else null,
                userAvatar = if (idx == 0) ownerAvatar else null,
                isMuted = false,
                isLocked = false,
                isSpeaking = false
            )
        }
        dao.insertSeats(seats)
        room
    }

    suspend fun adminDeleteRoom(roomId: Long) = withContext(Dispatchers.IO) {
        dao.deleteRoom(roomId)
    }

    suspend fun adminCreateEvent(
        title: String,
        description: String,
        category: String,
        bannerEmoji: String,
        bannerUrl: String,
        rules: String,
        prizeDescription: String,
        targetPoints: Long,
        isFeatured: Boolean
    ): EventEntity = withContext(Dispatchers.IO) {
        val eventId = "evt_${System.currentTimeMillis()}"
        val event = EventEntity(
            id = eventId,
            title = title,
            description = description,
            category = category,
            bannerEmoji = bannerEmoji.ifBlank { "🏆" },
            bannerUrl = bannerUrl,
            rules = rules,
            prizeDescription = prizeDescription,
            targetPoints = targetPoints,
            startDate = System.currentTimeMillis(),
            endDate = System.currentTimeMillis() + 7 * 86400000L,
            isActive = true,
            isPublished = true,
            isFeatured = isFeatured
        )
        dao.insertEvents(listOf(event))
        event
    }

    suspend fun adminUpdateEvent(event: EventEntity) = withContext(Dispatchers.IO) {
        dao.updateEvent(event)
    }

    suspend fun adminDeleteEvent(eventId: String) = withContext(Dispatchers.IO) {
        dao.deleteEvent(eventId)
    }

    suspend fun adminSuspendUser(userId: Long, suspend: Boolean) = withContext(Dispatchers.IO) {
        val user = dao.getUserByIdSync(userId) ?: return@withContext
        dao.updateUser(user.copy(isSuspended = suspend))
    }

    suspend fun adminBanUser(userId: Long, reason: String) = withContext(Dispatchers.IO) {
        val user = dao.getUserByIdSync(userId) ?: return@withContext
        dao.updateUser(user.copy(isBanned = true, banReason = reason))
        dao.clearUserFromAllSeats(userId)
    }

    suspend fun adminUnbanUser(userId: Long) = withContext(Dispatchers.IO) {
        val user = dao.getUserByIdSync(userId) ?: return@withContext
        dao.updateUser(user.copy(isBanned = false, banReason = ""))
    }

    suspend fun adminVerifyUser(userId: Long, isVerified: Boolean) = withContext(Dispatchers.IO) {
        val user = dao.getUserByIdSync(userId) ?: return@withContext
        dao.updateUser(user.copy(isOfficialVerified = isVerified))
    }

    suspend fun adminRemoveUserPhoto(userId: Long) = withContext(Dispatchers.IO) {
        dao.updateUserAvatar(userId, "avatar_1")
    }

    suspend fun adminSendWarning(userId: Long, warning: String) = withContext(Dispatchers.IO) {
        dao.insertNotification(
            NotificationEntity(
                userId = userId,
                title = "⚠️ Official Warning from STAR VOICE Moderation",
                message = warning,
                type = "SYSTEM"
            )
        )
    }
}
