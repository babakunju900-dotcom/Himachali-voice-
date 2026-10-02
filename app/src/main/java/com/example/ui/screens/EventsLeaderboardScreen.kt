package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.EventEntity
import com.example.model.UserEntity
import com.example.ui.components.AvatarView
import com.example.ui.theme.*

@Composable
fun EventsLeaderboardScreen(
    topHosts: List<UserEntity>,
    events: List<EventEntity>,
    onHostClick: (UserEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMainTab by remember { mutableIntStateOf(0) } // 0: Leaderboard, 1: Events
    var selectedTimeFrame by remember { mutableStateOf("Weekly") } // Daily, Weekly, Monthly

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StarKingBgDark)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Main Tab Bar: Leaderboards vs Events
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(StarKingSurfaceVariantDark)
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selectedMainTab == 0) StarGoldPrimary else Color.Transparent)
                    .clickable { selectedMainTab = 0 }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.tab_leaderboards),
                    color = if (selectedMainTab == 0) StarKingBgDark else TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selectedMainTab == 1) StarGoldPrimary else Color.Transparent)
                    .clickable { selectedMainTab = 1 }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.tab_official_events),
                    color = if (selectedMainTab == 1) StarKingBgDark else TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedMainTab == 0) {
            // Timeframe filters
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    "Daily" to stringResource(R.string.timeframe_daily),
                    "Weekly" to stringResource(R.string.timeframe_weekly),
                    "Monthly" to stringResource(R.string.timeframe_monthly)
                ).forEach { (timeframeKey, localizedTimeframe) ->
                    val isSelected = selectedTimeFrame == timeframeKey
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) StarKingCardDark else StarKingSurfaceVariantDark)
                            .border(1.dp, if (isSelected) StarGoldPrimary else Color.Transparent, RoundedCornerShape(14.dp))
                            .clickable { selectedTimeFrame = timeframeKey }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = localizedTimeframe,
                            color = if (isSelected) StarGoldPrimary else TextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Podium Section for Top 3
                if (topHosts.isNotEmpty()) {
                    item {
                        PodiumSection(
                            top3 = topHosts.take(3),
                            onUserClick = onHostClick
                        )
                    }
                }

                // Remaining Ranks 4+
                val rest = if (topHosts.size > 3) topHosts.drop(3) else emptyList()
                itemsIndexed(rest) { index, host ->
                    val rank = index + 4
                    RankRowItem(
                        rank = rank,
                        user = host,
                        onClick = { onHostClick(host) }
                    )
                }
            }
        } else {
            // Events List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(events, key = { it.id }) { event ->
                    EventCard(event = event)
                }
            }
        }
    }
}

@Composable
private fun PodiumSection(
    top3: List<UserEntity>,
    onUserClick: (UserEntity) -> Unit
) {
    val goldScore = stringResource(R.string.rank_gold_score)
    val silverScore = stringResource(R.string.rank_silver_score)
    val bronzeScore = stringResource(R.string.rank_bronze_score)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // Rank 2 (Silver)
        if (top3.size > 1) {
            PodiumItem(
                user = top3[1],
                rank = 2,
                crownEmoji = "🥈",
                badgeColor = Color(0xFFC0C0C0),
                score = silverScore,
                height = 100.dp,
                onClick = { onUserClick(top3[1]) },
                modifier = Modifier.weight(1f)
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        // Rank 1 (Gold)
        if (top3.isNotEmpty()) {
            PodiumItem(
                user = top3[0],
                rank = 1,
                crownEmoji = "👑",
                badgeColor = StarGoldPrimary,
                score = goldScore,
                height = 125.dp,
                onClick = { onUserClick(top3[0]) },
                modifier = Modifier.weight(1f)
            )
        }

        // Rank 3 (Bronze)
        if (top3.size > 2) {
            PodiumItem(
                user = top3[2],
                rank = 3,
                crownEmoji = "🥉",
                badgeColor = Color(0xFFCD7F32),
                score = bronzeScore,
                height = 85.dp,
                onClick = { onUserClick(top3[2]) },
                modifier = Modifier.weight(1f)
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun PodiumItem(
    user: UserEntity,
    rank: Int,
    crownEmoji: String,
    badgeColor: Color,
    score: String,
    height: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("podium_rank_$rank")
    ) {
        Text(text = crownEmoji, fontSize = 20.sp)

        AvatarView(
            avatarUrl = user.avatarUrl,
            nickname = user.nickname,
            size = if (rank == 1) 56.dp else 46.dp,
            frameName = user.equippedFrame,
            vipTier = user.vipTier,
            level = user.level,
            isOfficial = user.isOfficialVerified
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = user.nickname,
            color = TextWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)
        )

        Text(
            text = score,
            color = StarGoldLight,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Podium Block
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(StarKingCardDark, StarKingSurfaceVariantDark)
                    )
                )
                .border(1.dp, badgeColor.copy(alpha = 0.6f), RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "#$rank",
                color = badgeColor,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp
            )
        }
    }
}

@Composable
private fun RankRowItem(
    rank: Int,
    user: UserEntity,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(StarKingSurfaceVariantDark)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("rank_row_$rank"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$rank",
            color = TextMuted,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.width(26.dp)
        )

        AvatarView(
            avatarUrl = user.avatarUrl,
            nickname = user.nickname,
            size = 40.dp,
            vipTier = user.vipTier,
            level = user.level,
            isOfficial = user.isOfficialVerified
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.nickname,
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.user_id_num, user.userId),
                color = TextSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = stringResource(R.string.rank_score_format, user.giftsReceivedCount * 250),
            color = StarGoldPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun EventCard(event: EventEntity) {
    Card(
        colors = CardDefaults.cardColors(containerColor = StarKingSurfaceVariantDark),
        shape = RoundedCornerShape(18.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(StarGoldPrimary.copy(alpha = 0.5f), StarKingCardBorder))),
        modifier = Modifier.fillMaxWidth().testTag("event_card_${event.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(StarGoldPrimary)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = event.category,
                        color = StarKingBgDark,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = stringResource(R.string.status_active),
                    color = LiveGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = event.bannerEmoji, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = event.title,
                    color = TextWhite,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = event.description,
                color = TextChampagne,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(StarKingCardDark)
                    .padding(10.dp)
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.prize_details_heading),
                        color = StarGoldPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = event.prizeDescription,
                        color = TextWhite,
                        fontSize = 11.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
