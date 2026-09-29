package com.example.model

data class MomentEntity(
    val id: String,
    val userId: Long,
    val authorName: String,
    val authorAvatar: String,
    val authorGender: String = "Female", // "Female" or "Male"
    val vipTier: Int = 4,
    val badgeTag: String = "COOL",
    val timestampText: String = "Just now",
    val caption: String,
    val images: List<String> = emptyList(),
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val commentsCount: Int = 0,
    val isFollowing: Boolean = false,
    val hashtag: String = "# Mood________"
)

data class MomentTopic(
    val hashtag: String,
    val participationCount: String,
    val isHot: Boolean,
    val previewIcons: List<String>
)

data class MomentComment(
    val id: String,
    val momentId: String,
    val authorName: String,
    val authorAvatar: String,
    val text: String,
    val timeAgo: String
)
