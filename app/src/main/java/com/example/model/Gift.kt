package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gifts")
data class GiftEntity(
    @PrimaryKey val id: String,
    val name: String,
    val iconEmoji: String,
    val category: String, // Popular, Love, Star, Luxury, Festival, Special
    val coinPrice: Long,
    val animationType: String = "FLOAT", // FLOAT, BURST, CROWN, CAR, GALAXY
    val isActive: Boolean = true
)

@Entity(tableName = "gift_transactions")
data class GiftTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val roomId: Long,
    val senderUserId: Long,
    val senderName: String,
    val receiverUserId: Long,
    val receiverName: String,
    val giftId: String,
    val giftName: String,
    val giftIcon: String,
    val giftCount: Int,
    val totalCoins: Long,
    val timestamp: Long = System.currentTimeMillis()
)
