package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.StoreCustomizationEntity
import com.example.model.UserCustomizationEntity
import com.example.model.VipPlanEntity
import com.example.ui.components.MedalCard
import com.example.ui.components.MedalDetailBottomSheet
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizationStoreScreen(
    items: List<StoreCustomizationEntity>,
    vipPlans: List<VipPlanEntity>,
    userCustomizations: List<UserCustomizationEntity>,
    userCoinBalance: Long,
    currentUserBadge: String = "",
    onDismiss: () -> Unit,
    onPurchaseItem: (String) -> Unit,
    onEquipItem: (String) -> Unit,
    onPurchaseVip: (String) -> Unit,
    initialTab: Int = 0
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) } // 0: Medals, 1: Frames & Badges, 2: VIP
    var selectedMedalFilter by remember { mutableStateOf("All") } // "All", "Owned", "Legendary"
    var inspectMedal by remember { mutableStateOf<StoreCustomizationEntity?>(null) }

    val ownedItemIds = remember(userCustomizations) {
        userCustomizations.map { it.itemId }.toSet()
    }

    val medals = remember(items) {
        items.filter { it.type == "MEDAL" }
    }

    val framesAndBadges = remember(items) {
        items.filter { it.type != "MEDAL" }
    }

    val filteredMedals = remember(medals, selectedMedalFilter, ownedItemIds) {
        when (selectedMedalFilter) {
            "Owned" -> medals.filter { ownedItemIds.contains(it.id) }
            "Legendary" -> medals.filter { it.priceCoins >= 15000L }
            else -> medals
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF140F28),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .size(width = 44.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF3B3363))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎖️", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Honor Store & Medals",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF231C48))
                            .border(1.dp, StarGoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "${String.format("%,d", userCoinBalance)} 🪙",
                            color = StarGoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Tabs Row (Medals, Frames, VIP)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF1D173D))
                    .padding(4.dp)
            ) {
                val tabTitles = listOf("🎖️ Medals (${medals.size})", "🖼️ Frames", "👑 VIP")
                tabTitles.forEachIndexed { index, title ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(11.dp))
                            .then(
                                if (selectedTab == index) Modifier.background(Brush.horizontalGradient(listOf(StarGoldDark, StarGoldPrimary)))
                                else Modifier.background(Color.Transparent)
                            )
                            .clickable { selectedTab = index }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = if (selectedTab == index) StarKingBgDark else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTab) {
                // Tab 0: Medals Showcase (Exact 2-Column grid from user screenshots)
                0 -> {
                    // Filter Pills
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                    ) {
                        val filters = listOf(
                            "All" to "All Medals (${medals.size})",
                            "Owned" to "My Unlocked (${medals.count { ownedItemIds.contains(it.id) }})",
                            "Legendary" to "Legendary (15k+ 🪙)"
                        )
                        items(filters) { (key, label) ->
                            val isSelected = selectedMedalFilter == key
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) StarGoldPrimary else Color(0xFF221B45))
                                    .clickable { selectedMedalFilter = key }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) StarKingBgDark else Color(0xFFD1D5DB),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // 2-Column Medal Grid matching user's screenshots
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(440.dp)
                            .testTag("medals_grid"),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(filteredMedals, key = { it.id }) { medal ->
                            val isOwned = ownedItemIds.contains(medal.id)
                            val isEquipped = isOwned && (currentUserBadge.equals(medal.name, ignoreCase = true) ||
                                    userCustomizations.any { it.itemId == medal.id && it.isEquipped })

                            MedalCard(
                                medal = medal,
                                isOwned = isOwned,
                                isEquipped = isEquipped,
                                userCoins = userCoinBalance,
                                onClick = { inspectMedal = medal }
                            )
                        }
                    }
                }

                // Tab 1: Frames & Customization
                1 -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(440.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(framesAndBadges, key = { it.id }) { item ->
                            val isOwned = ownedItemIds.contains(item.id)
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1838)),
                                shape = RoundedCornerShape(16.dp),
                                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(0xFF2C2456), StarGoldPrimary.copy(alpha = 0.3f))))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = item.iconOrEmoji, fontSize = 34.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${item.durationDays} Days",
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Button(
                                        onClick = {
                                            if (isOwned) onEquipItem(item.id)
                                            else onPurchaseItem(item.id)
                                        },
                                        enabled = isOwned || userCoinBalance >= item.priceCoins,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isOwned) LiveGreen else StarGoldPrimary,
                                            contentColor = StarKingBgDark
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().height(36.dp)
                                    ) {
                                        Text(
                                            text = if (isOwned) "Wear Item" else "${String.format("%,d", item.priceCoins)} 🪙",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Tab 2: VIP Privileges
                2 -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.height(440.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(vipPlans, key = { it.id }) { plan ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1838)),
                                shape = RoundedCornerShape(16.dp),
                                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(StarGoldPrimary, RoyalPurple)))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "👑 ${plan.name} (${plan.durationDays} Days)",
                                            color = StarGoldPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = plan.benefitsDescription,
                                            color = TextChampagne,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = { onPurchaseVip(plan.id) },
                                        enabled = userCoinBalance >= plan.priceCoins,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = StarGoldPrimary,
                                            contentColor = StarKingBgDark
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            text = "${String.format("%,d", plan.priceCoins)} 🪙",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Detailed Medal Inspection BottomSheet
        inspectMedal?.let { medal ->
            val isOwned = ownedItemIds.contains(medal.id)
            val isEquipped = isOwned && (currentUserBadge.equals(medal.name, ignoreCase = true) ||
                    userCustomizations.any { it.itemId == medal.id && it.isEquipped })

            MedalDetailBottomSheet(
                medal = medal,
                isOwned = isOwned,
                isEquipped = isEquipped,
                userCoins = userCoinBalance,
                onDismiss = { inspectMedal = null },
                onEquip = { medalId ->
                    onEquipItem(medalId)
                    inspectMedal = null
                },
                onPurchase = { medalId ->
                    onPurchaseItem(medalId)
                    inspectMedal = null
                }
            )
        }
    }
}
