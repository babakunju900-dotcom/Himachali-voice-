package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey val userId: Long,
    val coinBalance: Long = 1000L, // Initial bonus welcome coins for new users
    val diamondBalance: Long = 0L, // Earnings from gifts received
    val totalRecharged: Long = 0L,
    val totalSpent: Long = 0L
)

@Entity(tableName = "wallet_transactions")
data class WalletTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: Long,
    val amount: Long, // Positive for credit, negative for debit
    val type: String, // RECHARGE, GIFT_SENT, GIFT_RECEIVED, VIP_PURCHASE, STORE_PURCHASE, REWARD
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val referenceId: String = ""
)
