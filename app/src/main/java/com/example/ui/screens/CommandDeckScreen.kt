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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.example.data.model.Quest
import com.example.data.model.QuestCompletion
import com.example.data.model.Rank
import com.example.data.model.StatType
import com.example.data.model.StatValue
import com.example.data.model.UserProfile
import com.example.data.model.XpEvent
import com.example.ui.KaizenScreen
import com.example.ui.components.AstralCanvasBackground
import com.example.ui.components.AstralCard
import com.example.ui.components.AstralDivider
import com.example.ui.components.CompactStatCard
import com.example.ui.components.MomentumXpBar
import com.example.ui.components.RankBadge
import com.example.ui.components.RhythmBadge
import com.example.ui.components.SparkBadge
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
import com.example.ui.theme.StarlightCyan
import com.example.ui.theme.SuccessGreen

@Composable
fun CommandDeckScreen(
    profile: UserProfile?,
    stats: List<StatValue>,
    quests: List<Quest>,
    completions: List<QuestCompletion>,
    challenges: List<ChallengeRecord>,
    recentEvents: List<XpEvent>,
    onCompleteQuest: (String) -> Unit,
    onNavigate: (KaizenScreen) -> Unit,
    onStartFocus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val level = profile?.level ?: 1
    val totalXp = profile?.totalXp ?: 0
    val xpInLevel = profile?.xpInCurrentLevel ?: 0
    val rank = KaizenProgressionEngine.rankForLevel(level)
    val sparks = profile?.sparks ?: 0
    val rhythm = profile?.currentRhythm ?: 0
    val timezone = profile?.timezone ?: "UTC"
    val todayDate = KaizenProgressionEngine.getLocalDate(timezone)

    val todayCompletionQuestIds = completions
        .filter { it.localDate == todayDate }
        .map { it.questId }
        .toSet()

    // Daily active quests
    val todayQuests = quests.filter { !it.isArchived && it.cadence != "ONCE" || !todayCompletionQuestIds.contains(it.id) }

    val dailyChallenge = challenges.firstOrNull { it.challengeType.contains("DAILY") }

    val vitalityStat = stats.firstOrNull { it.statName == StatType.VITALITY.name }?.value ?: 0
    val focusStat = stats.firstOrNull { it.statName == StatType.FOCUS.name }?.value ?: 0
    val knowledgeStat = stats.firstOrNull { it.statName == StatType.KNOWLEDGE.name }?.value ?: 0
    val resolveStat = stats.firstOrNull { it.statName == StatType.RESOLVE.name }?.value ?: 0

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
                // Top Header: Greeting & Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "COMMAND DECK",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AuroraTeal,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "Greetings, ${profile?.displayName ?: "Wayfinder"}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AstralTextPrimary
                        )
                    }
                    RankBadge(rank = rank)
                }
            }

            // Rhythm & Sparks Banner
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RhythmBadge(rhythmDays = rhythm, modifier = Modifier.weight(1f))
                    SparkBadge(sparks = sparks, modifier = Modifier.weight(1f))
                }
            }

            // Momentum XP Progression Card
            item {
                AstralCard(
                    borderColor = AuroraTeal.copy(alpha = 0.3f)
                ) {
                    MomentumXpBar(
                        totalXp = totalXp,
                        level = level,
                        xpInLevel = xpInLevel
                    )
                }
            }

            // Daily Challenge Card
            if (dailyChallenge != null) {
                item {
                    val progress = (dailyChallenge.currentValue.toFloat() / dailyChallenge.targetValue.toFloat()).coerceIn(0f, 1f)
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = AstralDarkCard),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(
                                if (dailyChallenge.isCompleted) listOf(SuccessGreen, AstralDarkSurfaceBorder)
                                else listOf(CosmicViolet.copy(alpha = 0.6f), AstralDarkSurfaceBorder)
                            )
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigate(KaizenScreen.CHALLENGES) }
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = CosmicViolet,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "DAILY TRIAL: ${dailyChallenge.title}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CosmicViolet
                                    )
                                }
                                if (dailyChallenge.isCompleted) {
                                    Surface(
                                        color = SuccessGreen.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(SuccessGreen, SuccessGreen)))
                                    ) {
                                        Text(
                                            text = "CONCORD MET",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SuccessGreen,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "+${dailyChallenge.xpReward} XP • +${dailyChallenge.sparkReward} Sparks",
                                        fontSize = 11.sp,
                                        color = SolarAmber,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Text(
                                text = dailyChallenge.description,
                                fontSize = 12.sp,
                                color = AstralTextSecondary
                            )
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (dailyChallenge.isCompleted) SuccessGreen else CosmicViolet,
                                trackColor = AstralDarkSurfaceVariant
                            )
                            Text(
                                text = "${dailyChallenge.currentValue} / ${dailyChallenge.targetValue} completed today",
                                fontSize = 11.sp,
                                color = AstralTextMuted
                            )
                        }
                    }
                }
            }

            // Quick Focus Session Launcher Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AstralDarkSurfaceVariant),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(AstralIndigo.copy(alpha = 0.5f), AstralDarkSurfaceBorder))
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onStartFocus() }
                        .testTag("quick_focus_card")
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = AstralIndigo.copy(alpha = 0.2f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = "Focus",
                                        tint = AstralIndigo,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Astral Focus Chamber",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AstralTextPrimary
                                )
                                Text(
                                    text = "25m deep work • +25 XP, +2 Focus, +1 Spark",
                                    fontSize = 11.sp,
                                    color = AstralTextSecondary
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Start",
                            tint = AuroraTeal,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Four Core Stat Cards
            item {
                Text(
                    text = "ASTRAL DISCIPLINES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AstralTextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CompactStatCard(statType = StatType.VITALITY, value = vitalityStat, modifier = Modifier.weight(1f))
                    CompactStatCard(statType = StatType.FOCUS, value = focusStat, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CompactStatCard(statType = StatType.KNOWLEDGE, value = knowledgeStat, modifier = Modifier.weight(1f))
                    CompactStatCard(statType = StatType.RESOLVE, value = resolveStat, modifier = Modifier.weight(1f))
                }
            }

            // Today's Quests Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TODAY'S QUESTS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AstralTextSecondary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "View All Quests",
                        fontSize = 12.sp,
                        color = AuroraTeal,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onNavigate(KaizenScreen.QUESTS) }
                    )
                }
            }

            if (todayQuests.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = AstralDarkSurfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "All charted quests for today completed!",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AuroraTeal
                            )
                            Text(
                                text = "Take a restful breath or forge a new quest for additional discipline.",
                                fontSize = 11.sp,
                                color = AstralTextMuted
                            )
                        }
                    }
                }
            } else {
                items(todayQuests.take(5)) { quest ->
                    val isDoneToday = todayCompletionQuestIds.contains(quest.id)
                    QuestDeckItem(
                        quest = quest,
                        isDone = isDoneToday,
                        onComplete = { onCompleteQuest(quest.id) }
                    )
                }
            }

            // Recent Activity Feed
            item {
                Text(
                    text = "FIELD JOURNAL LOG",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AstralTextSecondary,
                    letterSpacing = 1.sp
                )
            }

            if (recentEvents.isEmpty()) {
                item {
                    Text(
                        text = "No events logged yet. Complete a quest or focus block to begin.",
                        fontSize = 12.sp,
                        color = AstralTextMuted,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(recentEvents.take(4)) { event ->
                    RecentEventItem(event = event)
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun QuestDeckItem(
    quest: Quest,
    isDone: Boolean,
    onComplete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AstralDarkCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                if (isDone) listOf(SuccessGreen.copy(alpha = 0.4f), AstralDarkSurfaceBorder)
                else listOf(AstralDarkSurfaceBorder, AstralDarkSurfaceBorder)
            )
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = quest.category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuroraTeal
                    )
                    Text(
                        text = "• ${quest.xpReward} XP",
                        fontSize = 10.sp,
                        color = AstralTextMuted
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = quest.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AstralTextPrimary
                )
            }

            IconButton(
                onClick = onComplete,
                enabled = !isDone,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("complete_quest_btn_${quest.id}")
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isDone) SuccessGreen.copy(alpha = 0.2f) else AstralDarkSurfaceVariant,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(
                            if (isDone) listOf(SuccessGreen, SuccessGreen)
                            else listOf(AuroraTeal.copy(alpha = 0.5f), AstralDarkSurfaceBorder)
                        )
                    ),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Complete Quest",
                            tint = if (isDone) SuccessGreen else AstralTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentEventItem(event: XpEvent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.reason,
                fontSize = 12.sp,
                color = AstralTextPrimary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${event.eventType.replace('_', ' ')} • ${event.localDate}",
                fontSize = 10.sp,
                color = AstralTextMuted
            )
        }
        Text(
            text = "+${event.xpDelta} XP",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = AuroraTeal
        )
    }
}
