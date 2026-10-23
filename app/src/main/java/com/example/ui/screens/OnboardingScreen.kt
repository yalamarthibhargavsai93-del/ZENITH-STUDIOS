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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.model.Quest
import com.example.data.model.QuestCadence
import com.example.data.model.QuestCategory
import com.example.ui.components.AstralCanvasBackground
import com.example.ui.theme.AstralDarkBg
import com.example.ui.theme.AstralDarkCard
import com.example.ui.theme.AstralDarkSurfaceBorder
import com.example.ui.theme.AstralDarkSurfaceVariant
import com.example.ui.theme.AstralIndigo
import com.example.ui.theme.AstralTextMuted
import com.example.ui.theme.AstralTextPrimary
import com.example.ui.theme.AstralTextSecondary
import com.example.ui.theme.AuroraTeal
import com.example.ui.theme.CosmicViolet
import com.example.ui.theme.SolarAmber
import java.util.UUID

@Composable
fun OnboardingScreen(
    onComplete: (displayName: String, timezone: String, mainGoal: String, starterQuests: List<Quest>) -> Unit,
    modifier: Modifier = Modifier
) {
    var displayName by remember { mutableStateOf("Wayfinder") }
    var timezone by remember { mutableStateOf(java.time.ZoneId.systemDefault().id) }
    var mainGoal by remember { mutableStateOf("Cultivate daily discipline and mental clarity") }

    var movementQuestTitle by remember { mutableStateOf("Morning Mobility & 20-min Walk") }
    var studyQuestTitle by remember { mutableStateOf("Read 15 Pages or Deep Work Sprint") }
    var habitQuestTitle by remember { mutableStateOf("Evening Hydration & Gratitude Journal") }

    val scrollState = rememberScrollState()

    Box(modifier = modifier.fillMaxSize().background(AstralDarkBg)) {
        AstralCanvasBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Astral Emblem & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AstralDarkSurfaceVariant,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(AuroraTeal, CosmicViolet))
                    ),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Emblem",
                            tint = AuroraTeal,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "KAIZEN",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AstralTextPrimary,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Astral Field Journal of Earned Growth",
                        fontSize = 12.sp,
                        color = AuroraTeal,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Core Philosophy Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AstralDarkCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(AstralIndigo.copy(alpha = 0.5f), AstralDarkSurfaceBorder))
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "PRINCIPLE OF PERSONAL GROWTH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AstralIndigo,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Momentum XP and Astral Ranks represent progress inside Kaizen, never personal human worth.",
                        fontSize = 13.sp,
                        color = AstralTextPrimary,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Missed days are never punished. Compounding discipline is earned one intentional choice at a time.",
                        fontSize = 12.sp,
                        color = AstralTextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }

            // Step 1: Identity & Context
            Text(
                text = "1. Identity & Trajectory",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = AstralTextPrimary
            )

            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Display Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_name_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AstralTextPrimary,
                    unfocusedTextColor = AstralTextPrimary,
                    focusedBorderColor = AuroraTeal,
                    unfocusedBorderColor = AstralDarkSurfaceBorder,
                    focusedLabelColor = AuroraTeal,
                    unfocusedLabelColor = AstralTextSecondary
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = mainGoal,
                onValueChange = { mainGoal = it },
                label = { Text("Primary Focus / Goal") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_goal_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AstralTextPrimary,
                    unfocusedTextColor = AstralTextPrimary,
                    focusedBorderColor = AuroraTeal,
                    unfocusedBorderColor = AstralDarkSurfaceBorder,
                    focusedLabelColor = AuroraTeal,
                    unfocusedLabelColor = AstralTextSecondary
                )
            )

            OutlinedTextField(
                value = timezone,
                onValueChange = { timezone = it },
                label = { Text("Timezone (used for daily resets & cadence)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_timezone_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AstralTextPrimary,
                    unfocusedTextColor = AstralTextPrimary,
                    focusedBorderColor = AuroraTeal,
                    unfocusedBorderColor = AstralDarkSurfaceBorder,
                    focusedLabelColor = AuroraTeal,
                    unfocusedLabelColor = AstralTextSecondary
                ),
                singleLine = true
            )

            // Step 2: Forge 3 Starter Quests
            Text(
                text = "2. Forge Starter Quests",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = AstralTextPrimary
            )
            Text(
                text = "Three initial daily keystones to anchor your progression loop:",
                fontSize = 12.sp,
                color = AstralTextSecondary
            )

            // Movement Starter
            StarterQuestCard(
                categoryLabel = "MOVEMENT / WORKOUT",
                statAward = "+2 Vitality • 30 XP",
                color = AuroraTeal,
                icon = Icons.Default.FitnessCenter,
                title = movementQuestTitle,
                onTitleChange = { movementQuestTitle = it }
            )

            // Study Starter
            StarterQuestCard(
                categoryLabel = "STUDY / DEEP FOCUS",
                statAward = "+2 Knowledge • 25 XP",
                color = CosmicViolet,
                icon = Icons.Default.MenuBook,
                title = studyQuestTitle,
                onTitleChange = { studyQuestTitle = it }
            )

            // Habit Starter
            StarterQuestCard(
                categoryLabel = "DAILY HABIT / RESOLVE",
                statAward = "+1 Resolve • 15 XP",
                color = SolarAmber,
                icon = Icons.Default.Shield,
                title = habitQuestTitle,
                onTitleChange = { habitQuestTitle = it }
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    val starters = listOf(
                        Quest(
                            id = "qst_starter_1_${UUID.randomUUID()}",
                            userId = "user_default",
                            title = movementQuestTitle.ifBlank { "Morning Movement" },
                            category = QuestCategory.WORKOUT.name,
                            cadence = QuestCadence.DAILY.name,
                            xpReward = 30,
                            targetDurationMinutes = 20
                        ),
                        Quest(
                            id = "qst_starter_2_${UUID.randomUUID()}",
                            userId = "user_default",
                            title = studyQuestTitle.ifBlank { "Deep Study Sprint" },
                            category = QuestCategory.STUDY.name,
                            cadence = QuestCadence.DAILY.name,
                            xpReward = 25,
                            targetDurationMinutes = 25
                        ),
                        Quest(
                            id = "qst_starter_3_${UUID.randomUUID()}",
                            userId = "user_default",
                            title = habitQuestTitle.ifBlank { "Hydration & Reflection" },
                            category = QuestCategory.HABIT.name,
                            cadence = QuestCadence.DAILY.name,
                            xpReward = 15,
                            targetDurationMinutes = 5
                        )
                    )
                    onComplete(displayName, timezone, mainGoal, starters)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("onboarding_submit_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AuroraTeal,
                    contentColor = Color(0xFF042F2E)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Initiate Astral Journal",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StarterQuestCard(
    categoryLabel: String,
    statAward: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onTitleChange: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AstralDarkSurfaceVariant),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(color.copy(alpha = 0.5f), AstralDarkSurfaceBorder))
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                    Text(text = categoryLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
                }
                Text(text = statAward, fontSize = 11.sp, color = AstralTextMuted)
            }
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AstralTextPrimary,
                    unfocusedTextColor = AstralTextPrimary,
                    focusedBorderColor = color,
                    unfocusedBorderColor = AstralDarkSurfaceBorder
                )
            )
        }
    }
}
