package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timeline
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.engine.KaizenProgressionEngine
import com.example.data.model.QuestCompletion
import com.example.data.model.Rank
import com.example.data.model.StatType
import com.example.data.model.StatValue
import com.example.data.model.UserProfile
import com.example.data.model.XpEvent
import com.example.ui.components.AstralCanvasBackground
import com.example.ui.components.AstralCard
import com.example.ui.components.RankBadge
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
import java.time.LocalDate

@Composable
fun JourneyScreen(
    profile: UserProfile?,
    stats: List<StatValue>,
    xpEvents: List<XpEvent>,
    completions: List<QuestCompletion>,
    modifier: Modifier = Modifier
) {
    val level = profile?.level ?: 1
    val totalXp = profile?.totalXp ?: 0
    val rank = KaizenProgressionEngine.rankForLevel(level)
    val currentRhythm = profile?.currentRhythm ?: 0
    val longestRhythm = profile?.longestRhythm ?: 0

    val vitality = stats.firstOrNull { it.statName == StatType.VITALITY.name }?.value ?: 0
    val focus = stats.firstOrNull { it.statName == StatType.FOCUS.name }?.value ?: 0
    val knowledge = stats.firstOrNull { it.statName == StatType.KNOWLEDGE.name }?.value ?: 0
    val resolve = stats.firstOrNull { it.statName == StatType.RESOLVE.name }?.value ?: 0
    val totalStatPoints = vitality + focus + knowledge + resolve

    // Compute last 7 days XP velocity
    val today = LocalDate.now()
    val last7Days = (6 downTo 0).map { today.minusDays(it.toLong()).toString() }
    val xpByDay = last7Days.associateWith { date ->
        xpEvents.filter { it.localDate == date }.sumOf { it.xpDelta }
    }
    val maxXpInWindow = maxOf(100, xpByDay.values.maxOrNull() ?: 100)

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
                        text = "JOURNEY & PROGRESSION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuroraTeal,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Astral Field Chart",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AstralTextPrimary
                    )
                }
            }

            // Gentle Philosophy Banner
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = AstralDarkCard),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(AuroraTeal.copy(alpha = 0.4f), AstralDarkSurfaceBorder))
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = AuroraTeal, modifier = Modifier.size(16.dp))
                            Text(text = "UNBREAKABLE PROGRESSION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AuroraTeal)
                        }
                        Text(
                            text = "Kaizen never deducts XP or stats after a missed day. Momentum belongs to you forever. Welcome back whenever you are ready.",
                            fontSize = 12.sp,
                            color = AstralTextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Overview Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricBox(
                        title = "Current Rhythm",
                        value = "$currentRhythm days",
                        subtitle = "Longest: $longestRhythm d",
                        accentColor = AuroraTeal,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "Astral Rank",
                        value = rank.title,
                        subtitle = "Level $level",
                        accentColor = CosmicViolet,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 7-Day XP Momentum Accessible Chart
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AstralDarkCard),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(AstralIndigo.copy(alpha = 0.5f), AstralDarkSurfaceBorder))
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("journey_xp_chart")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "7-Day Momentum Velocity (XP)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AstralTextPrimary
                            )
                            Text(
                                text = "Max $maxXpInWindow XP",
                                fontSize = 11.sp,
                                color = AstralTextMuted
                            )
                        }

                        // Bar/Line Canvas Chart
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                val barWidth = w / 7f - 12.dp.toPx()

                                // Base zero line
                                drawLine(
                                    color = AstralDarkSurfaceBorder,
                                    start = Offset(0f, h - 1),
                                    end = Offset(w, h - 1),
                                    strokeWidth = 1f
                                )

                                for (i in 0 until 7) {
                                    val dateStr = last7Days[i]
                                    val dayXp = xpByDay[dateStr] ?: 0
                                    val barHeight = (dayXp.toFloat() / maxXpInWindow.toFloat()) * (h - 10.dp.toPx())
                                    val left = i * (w / 7f) + 6.dp.toPx()
                                    val top = h - barHeight

                                    // Bar fill
                                    drawRect(
                                        brush = Brush.verticalGradient(
                                            listOf(AuroraTeal, AstralIndigo)
                                        ),
                                        topLeft = Offset(left, top),
                                        size = Size(barWidth, barHeight)
                                    )
                                }
                            }
                        }

                        // Day Labels
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (dateStr in last7Days) {
                                val label = try {
                                    LocalDate.parse(dateStr).dayOfWeek.name.take(2)
                                } catch (_: Exception) {
                                    "-"
                                }
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    color = AstralTextMuted,
                                    modifier = Modifier.weight(1f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Stat Distribution Growth
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AstralDarkCard),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(CosmicViolet.copy(alpha = 0.4f), AstralDarkSurfaceBorder))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Stat Constellation Breakdown",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AstralTextPrimary
                        )
                        Text(
                            text = "Total discipline points earned: $totalStatPoints",
                            fontSize = 11.sp,
                            color = AstralTextSecondary
                        )

                        StatGrowthBar("Vitality", vitality, totalStatPoints, AuroraTeal, Icons.Default.FitnessCenter)
                        StatGrowthBar("Focus", focus, totalStatPoints, AstralIndigo, Icons.Default.Psychology)
                        StatGrowthBar("Knowledge", knowledge, totalStatPoints, CosmicViolet, Icons.Default.MenuBook)
                        StatGrowthBar("Resolve", resolve, totalStatPoints, SolarAmber, Icons.Default.Shield)
                    }
                }
            }

            // Milestones & Ranks
            item {
                Text(
                    text = "ASTRAL MILESTONES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AstralTextSecondary,
                    letterSpacing = 1.sp
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MilestoneCard("Initiate Wayfarer", "Reach Level 1 and set your daily quests", level >= 1)
                    MilestoneCard("Rhythm of Three", "Maintain 3 consecutive active days", longestRhythm >= 3)
                    MilestoneCard("Wayfinder Rank", "Attain Level 5 in Kaizen", level >= 5)
                    MilestoneCard("Deep Stride", "Complete 10 total quests or focus chambers", completions.size >= 10)
                    MilestoneCard("Vanguard Horizon", "Attain Level 10 and steady resolve", level >= 10)
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun MetricBox(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AstralDarkSurfaceVariant),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(accentColor.copy(alpha = 0.4f), AstralDarkSurfaceBorder))
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = title, fontSize = 11.sp, color = AstralTextSecondary)
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = accentColor)
            Text(text = subtitle, fontSize = 11.sp, color = AstralTextMuted)
        }
    }
}

@Composable
private fun StatGrowthBar(
    name: String,
    value: Int,
    total: Int,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    val fraction = if (total > 0) (value.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
                Text(text = name, fontSize = 12.sp, color = AstralTextPrimary, fontWeight = FontWeight.Medium)
            }
            Text(text = "$value pts", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = AstralDarkSurfaceVariant
        )
    }
}

@Composable
private fun MilestoneCard(title: String, desc: String, isUnlocked: Boolean) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = AstralDarkCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                if (isUnlocked) listOf(AuroraTeal.copy(alpha = 0.5f), AstralDarkSurfaceBorder)
                else listOf(AstralDarkSurfaceBorder, AstralDarkSurfaceBorder)
            )
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isUnlocked) AstralTextPrimary else AstralTextSecondary
                )
                Text(
                    text = desc,
                    fontSize = 11.sp,
                    color = AstralTextMuted
                )
            }

            Surface(
                shape = CircleShape,
                color = if (isUnlocked) AuroraTeal.copy(alpha = 0.2f) else AstralDarkSurfaceVariant,
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isUnlocked) Icons.Default.Check else Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (isUnlocked) AuroraTeal else AstralTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
