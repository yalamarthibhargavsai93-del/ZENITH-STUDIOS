package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.RewardItem
import com.example.data.model.RewardRedemption
import com.example.data.model.UserProfile
import com.example.ui.components.AstralCanvasBackground
import com.example.ui.components.SparkBadge
import com.example.ui.theme.AstralDarkBg
import com.example.ui.theme.AstralDarkCard
import com.example.ui.theme.AstralDarkSurfaceBorder
import com.example.ui.theme.AstralDarkSurfaceVariant
import com.example.ui.theme.AstralTextMuted
import com.example.ui.theme.AstralTextPrimary
import com.example.ui.theme.AstralTextSecondary
import com.example.ui.theme.AuroraTeal
import com.example.ui.theme.CosmicViolet
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.SolarAmberDeep
import com.example.ui.theme.SuccessGreen

@Composable
fun VaultScreen(
    profile: UserProfile?,
    rewards: List<RewardItem>,
    redemptions: List<RewardRedemption>,
    onCreateReward: (title: String, desc: String, cost: Int) -> Unit,
    onArchiveReward: (id: String, isArchived: Boolean) -> Unit,
    onRedeemReward: (id: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sparks = profile?.sparks ?: 0
    var showCreateDialog by remember { mutableStateOf(false) }

    val activeRewards = rewards.filter { !it.isArchived }

    Box(modifier = modifier.fillMaxSize().background(AstralDarkBg)) {
        AstralCanvasBackground()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ASTRAL VAULT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SolarAmber,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "Earned Rewards",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AstralTextPrimary
                        )
                    }

                    Button(
                        onClick = { showCreateDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SolarAmber,
                            contentColor = Color(0xFF451A03)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("create_reward_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "New Reward", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Sparks Balance Header Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AstralDarkCard),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(SolarAmber.copy(alpha = 0.5f), AstralDarkSurfaceBorder))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Available Sparks", fontSize = 12.sp, color = AstralTextSecondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = "Sparks",
                                    tint = SolarAmber,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "$sparks Sparks",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SolarAmber
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AstralDarkSurfaceVariant,
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.linearGradient(listOf(AuroraTeal.copy(alpha = 0.3f), AstralDarkSurfaceBorder))
                            )
                        ) {
                            Text(
                                text = "Zero real-money or loot boxes",
                                fontSize = 10.sp,
                                color = AuroraTeal,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Description
            item {
                Text(
                    text = "Sparks are earned through deep focus sessions and completing challenges. Spend them freely on real-life moments of joy without guilt.",
                    fontSize = 12.sp,
                    color = AstralTextSecondary,
                    lineHeight = 16.sp
                )
            }

            // Available Rewards Header
            item {
                Text(
                    text = "FORGED REAL-LIFE REWARDS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AstralTextSecondary,
                    letterSpacing = 1.sp
                )
            }

            if (activeRewards.isEmpty()) {
                item {
                    Text(
                        text = "Your Vault is currently empty. Add your favorite treats like 'Watch a movie', 'Buy an espresso', or '1h of Gaming'.",
                        fontSize = 12.sp,
                        color = AstralTextMuted,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(activeRewards, key = { it.id }) { rew ->
                    RewardCard(
                        reward = rew,
                        currentSparks = sparks,
                        onRedeem = { onRedeemReward(rew.id) },
                        onArchive = { onArchiveReward(rew.id, true) }
                    )
                }
            }

            // Redemption History Log
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(imageVector = Icons.Default.History, contentDescription = null, tint = AstralTextSecondary, modifier = Modifier.size(16.dp))
                    Text(
                        text = "REDEMPTION HISTORY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AstralTextSecondary,
                        letterSpacing = 1.sp
                    )
                }
            }

            if (redemptions.isEmpty()) {
                item {
                    Text(
                        text = "No redemptions logged yet. Complete tasks and redeem your first reward.",
                        fontSize = 12.sp,
                        color = AstralTextMuted
                    )
                }
            } else {
                items(redemptions.take(6)) { red ->
                    RedemptionLogItem(redemption = red)
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        if (showCreateDialog) {
            CreateRewardDialog(
                onDismiss = { showCreateDialog = false },
                onConfirm = { title, desc, cost ->
                    onCreateReward(title, desc, cost)
                    showCreateDialog = false
                }
            )
        }
    }
}

@Composable
private fun RewardCard(
    reward: RewardItem,
    currentSparks: Int,
    onRedeem: () -> Unit,
    onArchive: () -> Unit
) {
    val canAfford = currentSparks >= reward.sparkCost

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AstralDarkCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                if (canAfford) listOf(SolarAmber.copy(alpha = 0.5f), AstralDarkSurfaceBorder)
                else listOf(AstralDarkSurfaceBorder, AstralDarkSurfaceBorder)
            )
        ),
        modifier = Modifier.fillMaxWidth().testTag("reward_card_${reward.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = reward.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = AstralTextPrimary
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AstralDarkSurfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = SolarAmber, modifier = Modifier.size(14.dp))
                        Text(text = "${reward.sparkCost} Sparks", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SolarAmber)
                    }
                }
            }

            if (reward.description.isNotBlank()) {
                Text(
                    text = reward.description,
                    fontSize = 12.sp,
                    color = AstralTextSecondary,
                    lineHeight = 16.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onArchive, modifier = Modifier.size(36.dp)) {
                    Icon(imageVector = Icons.Default.Archive, contentDescription = "Archive", tint = AstralTextMuted, modifier = Modifier.size(18.dp))
                }

                Button(
                    onClick = onRedeem,
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SolarAmber,
                        contentColor = Color(0xFF451A03),
                        disabledContainerColor = AstralDarkSurfaceVariant,
                        disabledContentColor = AstralTextMuted
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("claim_reward_button_${reward.id}")
                ) {
                    Icon(
                        imageVector = if (canAfford) Icons.Default.LockOpen else Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (canAfford) "Claim Reward" else "Need ${reward.sparkCost} Sparks",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun RedemptionLogItem(redemption: RewardRedemption) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = redemption.rewardTitle, fontSize = 12.sp, color = AstralTextPrimary, fontWeight = FontWeight.SemiBold)
            Text(text = "Redeemed for ${redemption.sparkCost} Sparks", fontSize = 10.sp, color = AstralTextMuted)
        }
        Text(text = "-${redemption.sparkCost} Sparks", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SolarAmber)
    }
}

@Composable
private fun CreateRewardDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, desc: String, cost: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var costStr by remember { mutableStateOf("3") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = AstralDarkCard,
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(SolarAmber.copy(alpha = 0.5f), AstralDarkSurfaceBorder))
            ),
            modifier = Modifier.padding(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Forge Vault Reward",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AstralTextPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = AstralTextSecondary)
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Reward Name") },
                    placeholder = { Text("e.g. Cinema Night, Favorite Coffee") },
                    modifier = Modifier.fillMaxWidth().testTag("reward_title_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AstralTextPrimary,
                        unfocusedTextColor = AstralTextPrimary,
                        focusedBorderColor = SolarAmber,
                        unfocusedBorderColor = AstralDarkSurfaceBorder
                    )
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description / Condition") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AstralTextPrimary,
                        unfocusedTextColor = AstralTextPrimary,
                        focusedBorderColor = SolarAmber,
                        unfocusedBorderColor = AstralDarkSurfaceBorder
                    )
                )

                OutlinedTextField(
                    value = costStr,
                    onValueChange = { costStr = it },
                    label = { Text("Spark Cost") },
                    modifier = Modifier.fillMaxWidth().testTag("reward_cost_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AstralTextPrimary,
                        unfocusedTextColor = AstralTextPrimary,
                        focusedBorderColor = SolarAmber,
                        unfocusedBorderColor = AstralDarkSurfaceBorder
                    )
                )

                Button(
                    onClick = {
                        val cost = costStr.toIntOrNull()?.coerceAtLeast(1) ?: 1
                        onConfirm(title.ifBlank { "Unspecified Reward" }, desc, cost)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_reward_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SolarAmber,
                        contentColor = Color(0xFF451A03)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Seal Reward into Vault", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
