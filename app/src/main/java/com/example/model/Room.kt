package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rooms")
data class RoomEntity(
    @PrimaryKey val roomId: Long, // Unique 6-digit room ID e.g. 708210
    val name: String,
    val description: String,
    val coverEmoji: String = "🎙️",
    val category: String = "Party", // Party, Music, Chat, Gaming, Dating, Poetry, Official
    val language: String = "English",
    val country: String = "Global",
    val isPrivate: Boolean = false,
    val password: String = "",
    val seatCount: Int = 8, // 8, 10, or 12 seats
    val backgroundTheme: String = "royal_stars",
    val welcomeMessage: String = "Welcome to Star King! Respect everyone and enjoy the party! 🌟",
    val hostUserId: Long,
    val hostName: String,
    val hostAvatar: String,
    val isLive: Boolean = true,
    val onlineCount: Int = 1,
    val totalGiftsValue: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "room_seats", primaryKeys = ["roomId", "seatIndex"])
data class RoomSeatEntity(
    val roomId: Long,
    val seatIndex: Int, // 0 is host seat, 1..N-1 are guest seats
    val userId: Long? = null,
    val userName: String? = null,
    val userAvatar: String? = null,
    val userLevel: Int = 1,
    val userVip: Int = 0,
    val isLocked: Boolean = false,
    val isMuted: Boolean = false,
    val isSpeaking: Boolean = false,
    val joinedSeatTime: Long = 0L
)

@Entity(tableName = "room_members", primaryKeys = ["roomId", "userId"])
data class RoomMemberEntity(
    val roomId: Long,
    val userId: Long,
    val role: String = "LISTENER", // HOST, MODERATOR, SPEAKER, LISTENER
    val joinedAt: Long = System.currentTimeMillis()
)
