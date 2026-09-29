package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CoinPackageEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletRechargeDialog(
    packages: List<CoinPackageEntity>,
    currentBalance: Long,
    onDismiss: () -> Unit,
    onRechargeSelected: (String) -> Unit
) {
    var selectedPackageId by remember { mutableStateOf(packages.firstOrNull()?.id ?: "pkg_100k") }
    var isProcessing by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = StarKingSurfaceDark,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Recharge Star Coins 🪙",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Text(
                    text = "Balance: ${String.format("%,d", currentBalance)}",
                    color = StarGoldPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Instant credit via Secure Payment Gateway. Ledger transactions recorded.",
                color = TextMuted,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.heightIn(max = 340.dp)
            ) {
                items(packages, key = { it.id }) { pkg ->
                    val isSelected = pkg.id == selectedPackageId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) StarKingCardDark else StarKingSurfaceVariantDark)
                            .border(
                                1.5.dp,
                                if (isSelected) StarGoldPrimary else StarKingCardBorder,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedPackageId = pkg.id }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🪙", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${String.format("%,d", pkg.coins)} Coins",
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                if (pkg.bonusCoins > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "+${String.format("%,d", pkg.bonusCoins)} Bonus",
                                        color = StarGoldLight,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            if (pkg.popularTag.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = pkg.popularTag,
                                    color = NeonCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Button(
                            onClick = { selectedPackageId = pkg.id },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) StarGoldPrimary else StarKingSurfaceVariantDark,
                                contentColor = if (isSelected) StarKingBgDark else TextWhite
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "$${pkg.priceUsd}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    isProcessing = true
                    onRechargeSelected(selectedPackageId)
                },
                enabled = !isProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = StarGoldPrimary, contentColor = StarKingBgDark),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("confirm_recharge_btn")
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = StarKingBgDark, modifier = Modifier.size(20.dp))
                } else {
                    Text(
                        text = "Pay & Credit Coins Securely",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
