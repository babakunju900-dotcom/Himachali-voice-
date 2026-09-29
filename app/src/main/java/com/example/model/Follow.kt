package com.example.model

import androidx.room.Entity

@Entity(tableName = "follows", primaryKeys = ["followerUserId", "followingUserId"])
data class FollowEntity(
    val followerUserId: Long,
    val followingUserId: Long,
    val timestamp: Long = System.currentTimeMillis()
)
