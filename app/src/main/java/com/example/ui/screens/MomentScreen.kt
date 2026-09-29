package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MomentComment
import com.example.model.MomentEntity
import com.example.model.MomentTopic
import com.example.ui.components.AvatarView
import com.example.ui.theme.*

@Composable
fun MomentScreen(
    moments: List<MomentEntity>,
    topics: List<MomentTopic>,
    onLikeMoment: (String) -> Unit,
    onFollowUser: (Long) -> Unit,
    onPostComment: (String, String) -> Unit,
    onCreateMoment: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("Recommend") } // "Follow" or "Recommend"
    var showCreateDialog by remember { mutableStateOf(false) }
    var commentingMomentId by remember { mutableStateOf<String?>(null) }
    var selectedTopicFilter by remember { mutableStateOf<String?>(null) }

    val filteredMoments = remember(moments, selectedTab, selectedTopicFilter) {
        var list = if (selectedTab == "Follow") {
            moments.filter { it.isFollowing }
        } else {
            moments
        }
        if (selectedTopicFilter != null) {
            list = list.filter { it.hashtag.equals(selectedTopicFilter, ignoreCase = true) }
        }
        list
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF141221))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // --- TOP BAR: Follow | Recommend + Notifications & Camera ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Tabs: Follow | Recommend
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Text(
                            text = "Follow",
                            color = if (selectedTab == "Follow") TextWhite else TextMuted,
                            fontSize = 18.sp,
                            fontWeight = if (selectedTab == "Follow") FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier
                                .clickable { selectedTab = "Follow" }
                                .testTag("moment_tab_follow")
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { selectedTab = "Recommend" }
                                .testTag("moment_tab_recommend")
                        ) {
                            Text(
                                text = "Recommend",
                                color = if (selectedTab == "Recommend") TextWhite else TextMuted,
                                fontSize = 20.sp,
                                fontWeight = if (selectedTab == "Recommend") FontWeight.ExtraBold else FontWeight.Normal
                            )
                            if (selectedTab == "Recommend") {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .size(width = 24.dp, height = 3.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(StarGoldPrimary)
                                )
                            }
                        }
                    }

                    // Right Actions: Notification Bell + Camera (Post Moment)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        IconButton(
                            onClick = { },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = TextWhite,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Pink / Magenta Camera button to post moment
                        IconButton(
                            onClick = { showCreateDialog = true },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Color(0xFFFF2B6D), Color(0xFFFF5252))))
                                .testTag("moment_post_btn")
                        ) {
                            Icon(
                                Icons.Default.PhotoCamera,
                                contentDescription = "Post Moment",
                                tint = Color.White,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }

            // Mascot Header Sticker (as seen in Screenshot 2)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🥳 🌟 🎶",
                        fontSize = 24.sp
                    )
                }
            }

            // --- RECOMMENDED TOPICS / HASHTAG CAROUSEL ---
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Recommended",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )

                        Text(
                            text = "More >",
                            color = TextMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.clickable { selectedTopicFilter = null }
                        )
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(topics) { topic ->
                            val isSelected = selectedTopicFilter == topic.hashtag
                            TopicCard(
                                topic = topic,
                                isSelected = isSelected,
                                onClick = {
                                    selectedTopicFilter = if (isSelected) null else topic.hashtag
                                }
                            )
                        }
                    }
                }
            }

            // Divider line
            item {
                HorizontalDivider(
                    color = Color(0xFF221F36),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // --- MOMENTS FEED ---
            if (filteredMoments.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("✨", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (selectedTab == "Follow") "No moments from followed users yet" else "No moments found",
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Follow hosts or share your first voice party moment!",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { showCreateDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2B6D)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Post a Moment 📷", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(filteredMoments, key = { it.id }) { moment ->
                    MomentPostCard(
                        moment = moment,
                        onLike = { onLikeMoment(moment.id) },
                        onFollow = { onFollowUser(moment.userId) },
                        onComment = { commentingMomentId = moment.id }
                    )
                }
            }
        }
    }

    // Create Moment Dialog
    if (showCreateDialog) {
        CreateMomentDialog(
            topics = topics,
            onDismiss = { showCreateDialog = false },
            onSubmit = { caption, topic ->
                onCreateMoment(caption, topic)
                showCreateDialog = false
            }
        )
    }

    // Comment Sheet Dialog
    commentingMomentId?.let { momentId ->
        val moment = moments.find { it.id == momentId }
        MomentCommentSheet(
            moment = moment,
            onDismiss = { commentingMomentId = null },
            onSendComment = { text ->
                onPostComment(momentId, text)
                commentingMomentId = null
            }
        )
    }
}

@Composable
private fun TopicCard(
    topic: MomentTopic,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFF2E234A) else Color(0xFF1D1A30)),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = if (isSelected)
                Brush.linearGradient(listOf(Color(0xFFFF2B6D), StarGoldPrimary))
            else
                Brush.linearGradient(listOf(Color(0xFF2C2748), Color(0xFF241F3C)))
        ),
        modifier = Modifier.width(170.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // 2x2 Collage Grid of preview photos (recreating screenshot 2)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(1.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF4A148C), Color(0xFF311B92)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎙️", fontSize = 18.sp)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(1.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF880E4F), Color(0xFFC2185B)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✨", fontSize = 18.sp)
                    }
                }
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(1.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF004D40), Color(0xFF00796B)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👑", fontSize = 18.sp)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(1.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFFBF360C), Color(0xFFE65100)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💖", fontSize = 18.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Topic Hashtag
            Text(
                text = topic.hashtag,
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Participation count and Hot badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${topic.participationCount} participated",
                    color = TextMuted,
                    fontSize = 10.sp
                )

                if (topic.isHot) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFFF2B6D))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "hot",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MomentPostCard(
    moment: MomentEntity,
    onLike: () -> Unit,
    onFollow: () -> Unit,
    onComment: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Author Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with colorful live halo
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .border(2.dp, Brush.linearGradient(listOf(Color(0xFFFF2B6D), StarGoldPrimary)), CircleShape)
                    .padding(2.dp)
            ) {
                AvatarView(
                    avatarUrl = moment.authorAvatar,
                    nickname = moment.authorName,
                    size = 40.dp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Badge tag (e.g. COOL)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF26C6DA))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = moment.badgeTag,
                            color = Color.Black,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = moment.authorName,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    // Gender symbol
                    Text(
                        text = if (moment.authorGender == "Female") "♀" else "♂",
                        color = if (moment.authorGender == "Female") Color(0xFFFF69B4) else Color(0xFF64B5F6),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    // VIP badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF4A148C))
                            .border(0.5.dp, StarGoldPrimary, RoundedCornerShape(3.dp))
                            .padding(horizontal = 3.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "💎 VIP ${moment.vipTier}",
                            color = StarGoldLight,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = moment.timestampText,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            // Follow / Following Button
            Button(
                onClick = onFollow,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (moment.isFollowing) Color(0xFF262142) else Color(0xFFFF2B6D),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Text(
                    text = if (moment.isFollowing) "Following" else "+ Follow",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Caption Text
        if (moment.caption.isNotBlank()) {
            Text(
                text = moment.caption,
                color = TextWhite,
                fontSize = 14.sp,
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Room screenshots / Moment Images (2-column layout matching screenshot 2)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF7B1FA2), Color(0xFFE91E63))
                        )
                    )
                    .border(1.dp, Color(0xFFAB47BC).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .padding(10.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🎙️ Room Party", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Black.copy(alpha = 0.4f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("● LIVE", color = LiveGreen, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Guests on stage preview icons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(4) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👤", fontSize = 10.sp)
                            }
                        }
                    }

                    Text("Festival Night #8 🎶", color = StarGoldLight, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF1E88E5), Color(0xFF9C27B0))
                        )
                    )
                    .border(1.dp, Color(0xFF42A5F5).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .padding(10.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("👑 Gift Shower", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text("x88 🚀", color = StarGoldPrimary, fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💖 🎉 ✨", fontSize = 20.sp)
                    }

                    Text("Top Supporter Gala", color = TextChampagne, fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Interaction bar: Like | Comment | More (...)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Like Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onLike() }
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = if (moment.isLiked) Icons.Default.Favorite else Icons.Default.ThumbUp,
                        contentDescription = "Like",
                        tint = if (moment.isLiked) Color(0xFFFF2B6D) else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${moment.likesCount}",
                        color = if (moment.isLiked) Color(0xFFFF2B6D) else TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Comment Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onComment() }
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Comment",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${moment.commentsCount}",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // More Options (...)
            IconButton(
                onClick = { },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = "More",
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        HorizontalDivider(color = Color(0xFF1E1A33), thickness = 0.8.dp)
    }
}

@Composable
private fun CreateMomentDialog(
    topics: List<MomentTopic>,
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit
) {
    var caption by remember { mutableStateOf("") }
    var selectedTopic by remember { mutableStateOf(topics.firstOrNull()?.hashtag ?: "# Mood________") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Share Your Moment 📷",
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Post photos, voice thoughts, or updates to the community feed.",
                    color = TextMuted,
                    fontSize = 12.sp
                )

                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    placeholder = { Text("What's on your mind? 🥰🎶", color = TextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = StarGoldPrimary,
                        unfocusedBorderColor = Color(0xFF332D52)
                    )
                )

                Text(
                    text = "Select Topic:",
                    color = StarGoldLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(topics) { topic ->
                        val isChosen = selectedTopic == topic.hashtag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isChosen) Color(0xFFFF2B6D) else Color(0xFF221F38))
                                .clickable { selectedTopic = topic.hashtag }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = topic.hashtag,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (caption.isNotBlank()) {
                        onSubmit(caption, selectedTopic)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2B6D)),
                enabled = caption.isNotBlank()
            ) {
                Text("Publish Moment", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        },
        containerColor = Color(0xFF1D1836),
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun MomentCommentSheet(
    moment: MomentEntity?,
    onDismiss: () -> Unit,
    onSendComment: (String) -> Unit
) {
    var commentText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Comments (${moment?.commentsCount ?: 0})",
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Replying to ${moment?.authorName ?: "Host"}: \"${moment?.caption?.take(30) ?: ""}...\"",
                    color = TextMuted,
                    fontSize = 12.sp
                )

                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = { Text("Write a supportive comment... 💬", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = StarGoldPrimary,
                        unfocusedBorderColor = Color(0xFF332D52)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (commentText.isNotBlank()) {
                        onSendComment(commentText)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2B6D)),
                enabled = commentText.isNotBlank()
            ) {
                Text("Send Comment", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextMuted)
            }
        },
        containerColor = Color(0xFF1D1836),
        shape = RoundedCornerShape(20.dp)
    )
}
