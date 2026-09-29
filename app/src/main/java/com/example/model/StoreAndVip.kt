package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coin_packages")
data class CoinPackageEntity(
    @PrimaryKey val id: String,
    val coins: Long,
    val bonusCoins: Long = 0L,
    val priceUsd: Double,
    val popularTag: String = "",
    val isActive: Boolean = true
)

@Entity(tableName = "vip_plans")
data class VipPlanEntity(
    @PrimaryKey val id: String,
    val name: String,
    val tier: Int, // 1 to 4
    val durationDays: Int,
    val priceCoins: Long,
    val badgeTitle: String,
    val benefitsDescription: String,
    val isActive: Boolean = true
)

@Entity(tableName = "store_customizations")
data class StoreCustomizationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String, // FRAME, TITLE, BADGE, VEHICLE
    val iconOrEmoji: String,
    val priceCoins: Long,
    val durationDays: Int = 30,
    val isActive: Boolean = true
)

@Entity(tableName = "user_customizations", primaryKeys = ["userId", "itemId"])
data class UserCustomizationEntity(
    val userId: Long,
    val itemId: String,
    val isEquipped: Boolean = false,
    val expiresAt: Long = 0L
)
