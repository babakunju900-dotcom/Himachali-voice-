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
    val activeRooms: Int,
    val totalGiftCoins: Long,
    val openTickets: Int,
    val pendingReports: Int
)

class StarKingRepository(private val dao: StarKingDao) {

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
            "BADGE" -> user.copy(equippedBadge = item.name)
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
        coverEmoji: String = "🎙️"
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

        PlatformStats(
            totalUsers = userCount,
            activeRooms = roomCount,
            totalGiftCoins = totalCoins,
            openTickets = openTickets,
            pendingReports = pendingReports
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
}
