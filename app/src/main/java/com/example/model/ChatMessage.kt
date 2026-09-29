package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val roomId: Long? = null, // If non-null, in-room message. If null, 1-on-1 private chat
    val recipientUserId: Long? = null, // If non-null, private message recipient
    val senderUserId: Long,
    val senderName: String,
    val senderAvatar: String,
    val senderLevel: Int = 1,
    val senderVip: Int = 0,
    val messageText: String,
    val type: String = "TEXT", // TEXT, GIFT, SYSTEM, SEAT_ACTION
    val giftName: String? = null,
    val giftIcon: String? = null,
    val giftCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
