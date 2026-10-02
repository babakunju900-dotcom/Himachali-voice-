package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val bannerEmoji: String = "🏆",
    val description: String,
    val startDate: Long,
    val endDate: Long,
    val rules: String,
    val prizeDescription: String,
    val targetPoints: Long,
    val category: String, // DAILY, WEEKLY, MONTHLY, SPECIAL
    val isActive: Boolean = true,
    val bannerUrl: String = "",
    val isPublished: Boolean = true,
    val isFeatured: Boolean = false,
    val startTimeText: String = "18:00 UTC",
    val endTimeText: String = "23:00 UTC",
    val rewardInfo: String = "5,000 Coins + 3D Golden Crown"
)

@Entity(tableName = "agencies")
data class AgencyEntity(
    @PrimaryKey val agencyId: String,
    val name: String,
    val ownerUserId: Long,
    val description: String,
    val totalHosts: Int = 0,
    val commissionRate: Double = 0.10,
    val createdAt: Long = System.currentTimeMillis()
)
