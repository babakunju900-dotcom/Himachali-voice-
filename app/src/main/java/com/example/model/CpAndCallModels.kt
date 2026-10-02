package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CpStatus {
    AVAILABLE,
    REQUEST_SENT,
    ACCEPTED,
    BUSY,
    OFFLINE,
    DECLINED,
    BLOCKED
}

@Entity(tableName = "cp_connections")
data class CpConnectionEntity(
    @PrimaryKey val connectionId: String, // e.g. "cp_504094_500102"
    val user1Id: Long,
    val user1Name: String,
    val user1Avatar: String,
    val user2Id: Long,
    val user2Name: String,
    val user2Avatar: String,
    val requesterId: Long,
    val status: String = CpStatus.AVAILABLE.name,
    val intimacyScore: Int = 100,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class CallState {
    CALLING,
    RINGING,
    CONNECTING,
    CONNECTED,
    ENDED,
    MISSED,
    DECLINED,
    BUSY,
    FAILED
}

@Entity(tableName = "private_call_sessions")
data class PrivateCallSessionEntity(
    @PrimaryKey val callId: String, // e.g. "call_17200000_504094"
    val callerId: Long,
    val callerName: String,
    val callerAvatar: String,
    val receiverId: Long,
    val receiverName: String,
    val receiverAvatar: String,
    val status: String = CallState.CALLING.name,
    val isVideo: Boolean = false,
    val isCallerMuted: Boolean = false,
    val isReceiverMuted: Boolean = false,
    val isSpeakerOn: Boolean = true,
    val isVideoEnabled: Boolean = false,
    val startedAt: Long = System.currentTimeMillis(),
    val connectedAt: Long = 0L,
    val durationSeconds: Int = 0,
    val endedAt: Long = 0L,
    val endReason: String = "",
    val activeParticipantsCount: Int = 2 // Strictly 2 participants max
)

@Entity(tableName = "private_call_history")
data class PrivateCallHistoryEntity(
    @PrimaryKey val historyId: String,
    val userId: Long, // Owning user (for clearing own history)
    val otherUserId: Long,
    val otherUserName: String,
    val otherUserAvatar: String,
    val isOutgoing: Boolean,
    val isVideo: Boolean = false,
    val status: String, // Completed, Missed, Declined, Failed, Busy
    val durationSeconds: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
