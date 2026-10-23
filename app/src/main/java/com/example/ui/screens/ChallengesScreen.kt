package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.engine.KaizenProgressionEngine
import com.example.data.model.ChallengeRecord
import com.example.data.model.ChallengeType
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
import com.example.ui.theme.SuccessGreen

@Composable
fun ChallengesScreen(
    challenges: List<ChallengeRecord>,
    userTimezone: String,
    modifier: Modifier = Modifier
) {
    val localDate = KaizenProgressionEngine.getLocalDate(userTimezone)
    val localWeek = KaizenProgressionEngine.getLocalWeekKey(userTimezone)

    val dailyChallenges = challenges.filter { it.periodKey == localDate || it.challengeType.contains("DAILY") }
    val weeklyChallenges = challenges.filter { it.periodKey == localWeek || it.challengeType.contains("WEEKLY") }

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
                Column {
                    Text(
                        text = "ASTRAL TRIALS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CosmicViolet,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Challenges & Rhythms",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AstralTextPrimary
                    )
                }
            }

            item {
                Text(
                    text = "Challenges award Momentum XP and Sparks that bypass the daily XP cap. All trials are evaluated idempotently.",
                    fontSize = 12.sp,
                    color = AstralTextSecondary,
                    lineHeight = 16.sp
                )
            }

            // Daily Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DAILY CONCORD",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AstralTextSecondary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Resets Daily ($localDate)",
                        fontSize = 11.sp,
                        color = AuroraTeal
                    )
                }
            }

            items(dailyChallenges) { ch ->
                ChallengeCard(challenge = ch)
            }

            // Weekly Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "WEEKLY RESIDENCY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AstralTextSecondary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Cycle $localWeek",
                        fontSize = 11.sp,
                        color = CosmicViolet
                    )
                }
            }

            items(weeklyChallenges) { ch ->
                ChallengeCard(challenge = ch)
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun ChallengeCard(challenge: ChallengeRecord) {
    val progress = if (challenge.targetValue > 0) {
        (challenge.currentValue.toFloat() / challenge.targetValue.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val accentColor = when (challenge.challengeType) {
        ChallengeType.DAILY_ACTIONS.name -> AuroraTeal
        ChallengeType.WEEKLY_VITALITY.name -> AuroraTeal
        ChallengeType.WEEKLY_FOCUS_DURATION.name -> AstralIndigo
        ChallengeType.WEEKLY_ACTIVE_DAYS.name -> CosmicViolet
        else -> SolarAmber
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AstralDarkCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                if (challenge.isCompleted) listOf(SuccessGreen, AstralDarkSurfaceBorder)
                else listOf(accentColor.copy(alpha = 0.5f), AstralDarkSurfaceBorder)
            )
        ),
        modifier = Modifier.fillMaxWidth().testTag("challenge_card_${challenge.challengeType}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = if (challenge.isCompleted) Icons.Default.CheckCircle else Icons.Default.Stars,
                        contentDescription = null,
                        tint = if (challenge.isCompleted) SuccessGreen else accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = challenge.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AstralTextPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AstralDarkSurfaceVariant,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(SolarAmber.copy(alpha = 0.4f), AstralDarkSurfaceBorder))
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "+${challenge.xpReward} XP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AuroraTeal
                        )
                        Text(text = "•", fontSize = 11.sp, color = AstralTextMuted)
                        Text(
                            text = "+${challenge.sparkReward} Sparks",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SolarAmber
                        )
                    }
                }
            }

            Text(
                text = challenge.description,
                fontSize = 12.sp,
                color = AstralTextSecondary,
                lineHeight = 16.sp
            )

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (challenge.isCompleted) SuccessGreen else accentColor,
                trackColor = AstralDarkSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${challenge.currentValue} / ${challenge.targetValue} progress",
                    fontSize = 11.sp,
                    color = AstralTextMuted
                )
                Text(
                    text = if (challenge.isCompleted) "Completed & Awarded" else "${(progress * 100).toInt()}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (challenge.isCompleted) SuccessGreen else AstralTextSecondary
                )
            }
        }
    }
}
