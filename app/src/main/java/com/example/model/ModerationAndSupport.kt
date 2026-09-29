package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
    @PrimaryKey val ticketId: String,
    val userId: Long,
    val userNickname: String,
    val category: String, // Payment, Account, Room Issue, Harassment, Other
    val subject: String,
    val message: String,
    val status: String = "OPEN", // OPEN, IN_PROGRESS, RESOLVED, CLOSED
    val assignedStaffId: Long? = null,
    val staffReply: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val reporterUserId: Long,
    val reporterName: String,
    val reportedUserId: Long? = null,
    val reportedUserName: String? = null,
    val reportedRoomId: Long? = null,
    val reportedRoomName: String? = null,
    val reasonCategory: String, // Harassment, Inappropriate Audio, Spam, Scams, Fake Identity
    val details: String,
    val status: String = "PENDING", // PENDING, ACTION_TAKEN, DISMISSED
    val moderatorNotes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "moderation_actions")
data class ModerationActionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val moderatorUserId: Long,
    val targetUserId: Long,
    val actionType: String, // WARN, MUTE, KICK, TEMP_BAN, PERM_BAN, UNBAN
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: Long,
    val title: String,
    val message: String,
    val type: String, // FOLLOW, GIFT, ROOM_INVITE, PRIVATE_MSG, EVENT, SYSTEM, RECHARGE, VIP
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSettingEntity(
    @PrimaryKey val key: String,
    val value: String
)
