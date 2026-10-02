package com.example.data

import androidx.room.*
import com.example.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StarKingDao {

    // --- Users ---
    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    fun getUserById(userId: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun getUserByIdSync(userId: Long): UserEntity?

    @Query("SELECT * FROM users WHERE isBanned = 0 ORDER BY level DESC, exp DESC")
    fun getAllActiveUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE nickname LIKE '%' || :query || '%' OR CAST(userId AS TEXT) LIKE '%' || :query || '%'")
    fun searchUsers(query: String): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE isOfficialVerified = 1")
    fun getOfficialUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users ORDER BY userId ASC")
    fun getAllUsersAdmin(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE role = 'HOST' OR level >= 5 ORDER BY giftsReceivedCount DESC LIMIT :limit")
    fun getTopHosts(limit: Int = 10): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET avatarUrl = :avatarUrl WHERE userId = :userId")
    suspend fun updateUserAvatar(userId: Long, avatarUrl: String)

    @Query("UPDATE rooms SET coverPhotoUrl = :coverPhotoUrl WHERE roomId = :roomId")
    suspend fun updateRoomCoverPhoto(roomId: Long, coverPhotoUrl: String)

    @Query("SELECT * FROM users WHERE authIdentifier = :identifier LIMIT 1")
    suspend fun getUserByAuthIdentifier(identifier: String): UserEntity?

    @Query("DELETE FROM users WHERE userId = :userId")
    suspend fun deleteUser(userId: Long)

    @Query("DELETE FROM wallets WHERE userId = :userId")
    suspend fun deleteWallet(userId: Long)

    @Query("DELETE FROM wallet_transactions WHERE userId = :userId")
    suspend fun deleteWalletTransactions(userId: Long)

    @Query("DELETE FROM follows WHERE followerUserId = :userId OR followingUserId = :userId")
    suspend fun deleteFollowsForUser(userId: Long)

    @Query("DELETE FROM user_customizations WHERE userId = :userId")
    suspend fun deleteUserCustomizations(userId: Long)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    // --- Wallets & Transactions ---
    @Query("SELECT * FROM wallets WHERE userId = :userId LIMIT 1")
    fun getWallet(userId: Long): Flow<WalletEntity?>

    @Query("SELECT * FROM wallets WHERE userId = :userId LIMIT 1")
    suspend fun getWalletSync(userId: Long): WalletEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: WalletEntity)

    @Update
    suspend fun updateWallet(wallet: WalletEntity)

    @Query("SELECT * FROM wallet_transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getTransactions(userId: Long): Flow<List<WalletTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: WalletTransactionEntity)

    // --- Rooms ---
    @Query("SELECT * FROM rooms WHERE isLive = 1 ORDER BY totalGiftsValue DESC, onlineCount DESC")
    fun getLiveRooms(): Flow<List<RoomEntity>>

    @Query("SELECT * FROM rooms WHERE roomId = :roomId LIMIT 1")
    fun getRoomById(roomId: Long): Flow<RoomEntity?>

    @Query("SELECT * FROM rooms WHERE roomId = :roomId LIMIT 1")
    suspend fun getRoomByIdSync(roomId: Long): RoomEntity?

    @Query("SELECT * FROM rooms WHERE name LIKE '%' || :query || '%' OR CAST(roomId AS TEXT) LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'")
    fun searchRooms(query: String): Flow<List<RoomEntity>>

    @Query("SELECT * FROM rooms WHERE hostUserId = :userId")
    fun getRoomsByHost(userId: Long): Flow<List<RoomEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: RoomEntity)

    @Update
    suspend fun updateRoom(room: RoomEntity)

    @Query("DELETE FROM rooms WHERE roomId = :roomId")
    suspend fun deleteRoom(roomId: Long)

    @Query("SELECT COUNT(*) FROM rooms WHERE isLive = 1")
    suspend fun getActiveRoomCount(): Int

    // --- Room Seats ---
    @Query("SELECT * FROM room_seats WHERE roomId = :roomId ORDER BY seatIndex ASC")
    fun getSeatsForRoom(roomId: Long): Flow<List<RoomSeatEntity>>

    @Query("SELECT * FROM room_seats WHERE roomId = :roomId ORDER BY seatIndex ASC")
    suspend fun getSeatsForRoomSync(roomId: Long): List<RoomSeatEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeats(seats: List<RoomSeatEntity>)

    @Update
    suspend fun updateSeat(seat: RoomSeatEntity)

    @Query("UPDATE room_seats SET userId = NULL, userName = NULL, userAvatar = NULL, isSpeaking = 0 WHERE roomId = :roomId AND userId = :userId")
    suspend fun clearUserFromSeats(roomId: Long, userId: Long)

    @Query("UPDATE room_seats SET userId = NULL, userName = NULL, userAvatar = NULL, isSpeaking = 0 WHERE userId = :userId")
    suspend fun clearUserFromAllSeats(userId: Long)

    @Query("UPDATE room_seats SET isSpeaking = :isSpeaking WHERE roomId = :roomId AND userId = :userId")
    suspend fun updateSeatSpeakingState(roomId: Long, userId: Long, isSpeaking: Boolean)

    // --- Chat Messages ---
    @Query("SELECT * FROM chat_messages WHERE roomId = :roomId ORDER BY timestamp ASC")
    fun getRoomMessages(roomId: Long): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE roomId IS NULL AND ((senderUserId = :u1 AND recipientUserId = :u2) OR (senderUserId = :u2 AND recipientUserId = :u1)) ORDER BY timestamp ASC")
    fun getPrivateMessages(u1: Long, u2: Long): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE roomId IS NULL AND (senderUserId = :userId OR recipientUserId = :userId) ORDER BY timestamp DESC")
    fun getRecentPrivateMessages(userId: Long): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(msg: ChatMessageEntity): Long

    @Query("UPDATE chat_messages SET isRead = 1 WHERE roomId IS NULL AND senderUserId = :senderId AND recipientUserId = :myUserId")
    suspend fun markMessagesAsRead(senderId: Long, myUserId: Long)

    // --- Gifts & Transactions ---
    @Query("SELECT * FROM gifts WHERE isActive = 1 ORDER BY coinPrice ASC")
    fun getAllGifts(): Flow<List<GiftEntity>>

    @Query("SELECT * FROM gifts WHERE id = :giftId LIMIT 1")
    suspend fun getGiftById(giftId: String): GiftEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGifts(gifts: List<GiftEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGiftTransaction(transaction: GiftTransactionEntity)

    @Query("SELECT * FROM gift_transactions WHERE roomId = :roomId ORDER BY timestamp DESC LIMIT 20")
    fun getRecentRoomGifts(roomId: Long): Flow<List<GiftTransactionEntity>>

    @Query("SELECT SUM(totalCoins) FROM gift_transactions WHERE receiverUserId = :userId")
    fun getTotalCoinsReceived(userId: Long): Flow<Long?>

    @Query("SELECT SUM(totalCoins) FROM gift_transactions")
    suspend fun getTotalPlatformGiftCoins(): Long?

    // --- Hand Raises ---
    @Query("SELECT * FROM room_hand_raises WHERE roomId = :roomId ORDER BY timestamp ASC")
    fun getHandRaisesForRoom(roomId: Long): Flow<List<RoomHandRaiseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHandRaise(raise: RoomHandRaiseEntity)

    @Query("DELETE FROM room_hand_raises WHERE roomId = :roomId AND userId = :userId")
    suspend fun deleteHandRaise(roomId: Long, userId: Long)

    @Query("DELETE FROM room_hand_raises WHERE roomId = :roomId")
    suspend fun clearHandRaisesForRoom(roomId: Long)

    // --- Follows ---
    @Query("SELECT COUNT(*) FROM follows WHERE followerUserId = :followerId AND followingUserId = :followingId")
    fun isFollowing(followerId: Long, followingId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM follows WHERE followerUserId = :followerId AND followingUserId = :followingId")
    suspend fun isFollowingSync(followerId: Long, followingId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollow(follow: FollowEntity)

    @Query("DELETE FROM follows WHERE followerUserId = :followerId AND followingUserId = :followingId")
    suspend fun deleteFollow(followerId: Long, followingId: Long)

    @Query("SELECT followingUserId FROM follows WHERE followerUserId = :userId")
    fun getFollowingIds(userId: Long): Flow<List<Long>>

    @Query("SELECT u.* FROM users u INNER JOIN follows f ON u.userId = f.followerUserId WHERE f.followingUserId = :userId")
    fun getFollowersUsers(userId: Long): Flow<List<UserEntity>>

    @Query("SELECT u.* FROM users u INNER JOIN follows f ON u.userId = f.followingUserId WHERE f.followerUserId = :userId")
    fun getFollowingUsers(userId: Long): Flow<List<UserEntity>>

    // --- Store & VIP ---
    @Query("SELECT * FROM coin_packages WHERE isActive = 1 ORDER BY coins ASC")
    fun getCoinPackages(): Flow<List<CoinPackageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoinPackages(packages: List<CoinPackageEntity>)

    @Query("SELECT * FROM vip_plans WHERE isActive = 1 ORDER BY tier ASC")
    fun getVipPlans(): Flow<List<VipPlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVipPlans(plans: List<VipPlanEntity>)

    @Query("SELECT * FROM store_customizations WHERE isActive = 1")
    fun getStoreCustomizations(): Flow<List<StoreCustomizationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStoreCustomizations(items: List<StoreCustomizationEntity>)

    @Query("SELECT * FROM store_customizations WHERE id = :id LIMIT 1")
    suspend fun getStoreCustomizationById(id: String): StoreCustomizationEntity?

    @Query("SELECT * FROM user_customizations WHERE userId = :userId")
    fun getUserCustomizations(userId: Long): Flow<List<UserCustomizationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserCustomization(customization: UserCustomizationEntity)

    @Query("UPDATE user_customizations SET isEquipped = 0 WHERE userId = :userId")
    suspend fun unequipAllUserCustomizations(userId: Long)

    // --- Events & Agencies ---
    @Query("SELECT * FROM events WHERE isActive = 1 ORDER BY endDate ASC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)

    @Query("SELECT * FROM agencies")
    fun getAllAgencies(): Flow<List<AgencyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgencies(agencies: List<AgencyEntity>)

    // --- Moderation & Support ---
    @Query("SELECT * FROM support_tickets WHERE userId = :userId ORDER BY updatedAt DESC")
    fun getTicketsForUser(userId: Long): Flow<List<SupportTicketEntity>>

    @Query("SELECT * FROM support_tickets ORDER BY updatedAt DESC")
    fun getAllTickets(): Flow<List<SupportTicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: SupportTicketEntity)

    @Update
    suspend fun updateTicket(ticket: SupportTicketEntity)

    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    fun getAllReports(): Flow<List<ReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity)

    @Update
    suspend fun updateReport(report: ReportEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModerationAction(action: ModerationActionEntity)

    @Query("SELECT * FROM moderation_actions ORDER BY timestamp DESC")
    fun getAllModerationActions(): Flow<List<ModerationActionEntity>>

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotifications(userId: Long): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllNotificationsRead(userId: Long)

    @Query("SELECT * FROM app_settings")
    fun getAppSettings(): Flow<List<AppSettingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppSetting(setting: AppSettingEntity)

    // --- CP Connections ---
    @Query("SELECT * FROM cp_connections ORDER BY updatedAt DESC")
    fun getAllCpConnections(): Flow<List<CpConnectionEntity>>

    @Query("SELECT * FROM cp_connections WHERE user1Id = :userId OR user2Id = :userId ORDER BY updatedAt DESC")
    fun getCpConnectionsForUser(userId: Long): Flow<List<CpConnectionEntity>>

    @Query("SELECT * FROM cp_connections WHERE (user1Id = :u1 AND user2Id = :u2) OR (user1Id = :u2 AND user2Id = :u1) LIMIT 1")
    fun getCpConnection(u1: Long, u2: Long): Flow<CpConnectionEntity?>

    @Query("SELECT * FROM cp_connections WHERE (user1Id = :u1 AND user2Id = :u2) OR (user1Id = :u2 AND user2Id = :u1) LIMIT 1")
    suspend fun getCpConnectionSync(u1: Long, u2: Long): CpConnectionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCpConnection(cp: CpConnectionEntity)

    @Update
    suspend fun updateCpConnection(cp: CpConnectionEntity)

    @Query("DELETE FROM cp_connections WHERE connectionId = :connectionId")
    suspend fun deleteCpConnection(connectionId: String)

    @Query("SELECT COUNT(*) FROM cp_connections WHERE status = 'ACCEPTED'")
    fun getActiveCpConnectionsCount(): Flow<Int>

    // --- Private Call Sessions (2 Persons Only) ---
    @Query("SELECT * FROM private_call_sessions WHERE (callerId = :userId OR receiverId = :userId) AND status IN ('CALLING', 'RINGING', 'CONNECTING', 'CONNECTED') LIMIT 1")
    fun getActiveCallSessionForUser(userId: Long): Flow<PrivateCallSessionEntity?>

    @Query("SELECT * FROM private_call_sessions WHERE (callerId = :userId OR receiverId = :userId) AND status IN ('CALLING', 'RINGING', 'CONNECTING', 'CONNECTED') LIMIT 1")
    suspend fun getActiveCallSessionForUserSync(userId: Long): PrivateCallSessionEntity?

    @Query("SELECT * FROM private_call_sessions WHERE callId = :callId LIMIT 1")
    fun getCallSessionById(callId: String): Flow<PrivateCallSessionEntity?>

    @Query("SELECT * FROM private_call_sessions WHERE callId = :callId LIMIT 1")
    suspend fun getCallSessionByIdSync(callId: String): PrivateCallSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallSession(session: PrivateCallSessionEntity)

    @Update
    suspend fun updateCallSession(session: PrivateCallSessionEntity)

    @Query("DELETE FROM private_call_sessions WHERE callId = :callId")
    suspend fun deleteCallSession(callId: String)

    @Query("SELECT COUNT(*) FROM private_call_sessions WHERE status = 'CONNECTED'")
    fun getActiveCallSessionsCount(): Flow<Int>

    // --- Private Call History ---
    @Query("SELECT * FROM private_call_history WHERE userId = :userId ORDER BY timestamp DESC")
    fun getCallHistoryForUser(userId: Long): Flow<List<PrivateCallHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallHistory(history: PrivateCallHistoryEntity)

    @Query("DELETE FROM private_call_history WHERE userId = :userId")
    suspend fun clearCallHistoryForUser(userId: Long)

    // --- Additional Admin & Event Management ---
    @Query("SELECT * FROM events ORDER BY isFeatured DESC, endDate ASC")
    fun getAllEventsAdmin(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    suspend fun getEventByIdSync(id: String): EventEntity?

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteEvent(id: String)

    @Query("SELECT * FROM rooms ORDER BY isLive DESC, onlineCount DESC")
    fun getAllRoomsAdmin(): Flow<List<RoomEntity>>

    @Query("SELECT COUNT(*) FROM users WHERE isBanned = 1")
    fun getBannedUsersCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM users")
    fun getTotalUsersCountFlow(): Flow<Int>
}
