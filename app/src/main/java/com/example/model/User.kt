package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    OWNER,
    SUPER_ADMIN,
    ADMIN,
    MODERATOR,
    CUSTOMER_SUPPORT,
    EVENT_MANAGER,
    FINANCE,
    HOST,
    USER;

    fun canAccessAdminPanel(): Boolean = this != USER
    fun canBanUsers(): Boolean = this in listOf(OWNER, SUPER_ADMIN, ADMIN, MODERATOR)
    fun canManageRooms(): Boolean = this in listOf(OWNER, SUPER_ADMIN, ADMIN, MODERATOR)
    fun canManageSupport(): Boolean = this in listOf(OWNER, SUPER_ADMIN, CUSTOMER_SUPPORT)
    fun canManageEvents(): Boolean = this in listOf(OWNER, SUPER_ADMIN, EVENT_MANAGER)
    fun canManageFinance(): Boolean = this in listOf(OWNER, SUPER_ADMIN, FINANCE)
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: Long, // Unique 6-digit numeric User ID, never changes (e.g. 504094)
    val nickname: String,
    val avatarUrl: String = "avatar_1",
    val gender: String = "Star",
    val country: String = "United States",
    val dob: String = "2000-01-01",
    val language: String = "English",
    val bio: String = "Living the Star King party vibes! 🌟",
    val level: Int = 1,
    val exp: Int = 0,
    val vipTier: Int = 0, // 0: None, 1: VIP Bronze, 2: VIP Silver, 3: VIP Gold, 4: VIP Diamond
    val vipExpiry: Long = 0L,
    val isOfficialVerified: Boolean = false,
    val role: String = UserRole.USER.name,
    val equippedFrame: String = "",
    val equippedBadge: String = "",
    val equippedTitle: String = "",
    val isBanned: Boolean = false,
    val banReason: String = "",
    val isSuspended: Boolean = false,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val giftsReceivedCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val authProvider: String = "GUEST", // GOOGLE, PHONE, GUEST
    val authIdentifier: String = "",    // Email or Phone number
    val sessionId: String = "",
    val deviceInfo: String = "",
    val lastLoginAt: Long = System.currentTimeMillis()
)
