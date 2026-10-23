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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.engine.KaizenProgressionEngine
import com.example.data.model.Quest
import com.example.data.model.QuestCadence
import com.example.data.model.QuestCategory
import com.example.data.model.QuestCompletion
import com.example.ui.QuestFilter
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
fun QuestsScreen(
    quests: List<Quest>,
    completions: List<QuestCompletion>,
    currentFilter: QuestFilter,
    userTimezone: String,
    onFilterChange: (QuestFilter) -> Unit,
    onCompleteQuest: (String) -> Unit,
    onCreateQuest: (title: String, desc: String, category: QuestCategory, cadence: QuestCadence, xp: Int, duration: Int) -> Unit,
    onArchiveQuest: (questId: String, isArchived: Boolean) -> Unit,
    onDeleteQuest: (questId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    val todayDate = KaizenProgressionEngine.getLocalDate(userTimezone)
    val todayCompletedQuestIds = completions
        .filter { it.localDate == todayDate }
        .map { it.questId }
        .toSet()

    val filteredQuests = when (currentFilter) {
        QuestFilter.TODAY -> quests.filter { !it.isArchived }
        QuestFilter.ACTIVE -> quests.filter { !it.isArchived && !todayCompletedQuestIds.contains(it.id) }
        QuestFilter.COMPLETED -> quests.filter { todayCompletedQuestIds.contains(it.id) }
        QuestFilter.ARCHIVED -> quests.filter { it.isArchived }
    }

    Box(modifier = modifier.fillMaxSize().background(AstralDarkBg)) {
        AstralCanvasBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header & Create Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "QUEST LOG",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuroraTeal,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Realms of Discipline",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AstralTextPrimary
                    )
                }

                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AuroraTeal,
                        contentColor = Color(0xFF042F2E)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("forge_quest_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Forge Quest", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuestFilterChip(
                    label = "Today",
                    isSelected = currentFilter == QuestFilter.TODAY,
                    onClick = { onFilterChange(QuestFilter.TODAY) }
                )
                QuestFilterChip(
                    label = "Active",
                    isSelected = currentFilter == QuestFilter.ACTIVE,
                    onClick = { onFilterChange(QuestFilter.ACTIVE) }
                )
                QuestFilterChip(
                    label = "Done",
                    isSelected = currentFilter == QuestFilter.COMPLETED,
                    onClick = { onFilterChange(QuestFilter.COMPLETED) }
                )
                QuestFilterChip(
                    label = "Archived",
                    isSelected = currentFilter == QuestFilter.ARCHIVED,
                    onClick = { onFilterChange(QuestFilter.ARCHIVED) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quests List
            if (filteredQuests.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No quests in this sphere.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = AstralTextSecondary
                        )
                        Text(
                            text = "Forge a new quest to anchor your daily rhythm.",
                            fontSize = 12.sp,
                            color = AstralTextMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredQuests, key = { it.id }) { quest ->
                        val isDone = todayCompletedQuestIds.contains(quest.id)
                        QuestItemCard(
                            quest = quest,
                            isDone = isDone,
                            onComplete = { onCompleteQuest(quest.id) },
                            onArchive = { onArchiveQuest(quest.id, !quest.isArchived) },
                            onDelete = { onDeleteQuest(quest.id) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Forge Quest Dialog
        if (showCreateDialog) {
            CreateQuestDialog(
                onDismiss = { showCreateDialog = false },
                onConfirm = { title, desc, cat, cad, xp, dur ->
                    onCreateQuest(title, desc, cat, cad, xp, dur)
                    showCreateDialog = false
                }
            )
        }
    }
}

@Composable
private fun QuestFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clickable(onClick = onClick)
            .border(
                1.dp,
                if (isSelected) AuroraTeal else AstralDarkSurfaceBorder,
                RoundedCornerShape(10.dp)
            ),
        color = if (isSelected) AstralDarkSurfaceVariant else AstralDarkCard,
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) AuroraTeal else AstralTextSecondary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun QuestItemCard(
    quest: Quest,
    isDone: Boolean,
    onComplete: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AstralDarkCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                if (isDone) listOf(SuccessGreen.copy(alpha = 0.5f), AstralDarkSurfaceBorder)
                else listOf(AstralDarkSurfaceBorder, AstralDarkSurfaceBorder)
            )
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AstralDarkSurfaceVariant,
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(listOf(AuroraTeal.copy(alpha = 0.4f), AstralDarkSurfaceBorder))
                        )
                    ) {
                        Text(
                            text = quest.category,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AuroraTeal,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AstralDarkSurfaceVariant
                    ) {
                        Text(
                            text = quest.cadence,
                            fontSize = 10.sp,
                            color = CosmicViolet,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (quest.targetDurationMinutes > 0) {
                        Text(
                            text = "${quest.targetDurationMinutes}m",
                            fontSize = 10.sp,
                            color = AstralTextMuted
                        )
                    }
                }

                Text(
                    text = "+${quest.xpReward} XP",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SolarAmber
                )
            }

            Text(
                text = quest.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = AstralTextPrimary
            )

            if (quest.description.isNotBlank()) {
                Text(
                    text = quest.description,
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
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onArchive, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = if (quest.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                            contentDescription = "Archive",
                            tint = AstralTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = AstralTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Button(
                    onClick = onComplete,
                    enabled = !isDone,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDone) SuccessGreen.copy(alpha = 0.2f) else AuroraTeal,
                        contentColor = if (isDone) SuccessGreen else Color(0xFF042F2E)
                    ),
                    modifier = Modifier.testTag("quest_complete_action_${quest.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isDone) "Completed" else "Complete",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateQuestDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, desc: String, category: QuestCategory, cadence: QuestCadence, xp: Int, duration: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(QuestCategory.WORKOUT) }
    var selectedCadence by remember { mutableStateOf(QuestCadence.DAILY) }
    var xpReward by remember { mutableFloatStateOf(selectedCategory.defaultXp.toFloat()) }
    var durationMinutes by remember { mutableStateOf("20") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = AstralDarkCard,
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(AuroraTeal.copy(alpha = 0.5f), AstralDarkSurfaceBorder))
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
                        text = "Forge New Quest",
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
                    label = { Text("Quest Title") },
                    placeholder = { Text("e.g., 5km Run, 30m Calculus, Hydrate") },
                    modifier = Modifier.fillMaxWidth().testTag("quest_title_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AstralTextPrimary,
                        unfocusedTextColor = AstralTextPrimary,
                        focusedBorderColor = AuroraTeal,
                        unfocusedBorderColor = AstralDarkSurfaceBorder
                    )
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Optional Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AstralTextPrimary,
                        unfocusedTextColor = AstralTextPrimary,
                        focusedBorderColor = AuroraTeal,
                        unfocusedBorderColor = AstralDarkSurfaceBorder
                    )
                )

                // Category Selector
                Text(text = "Category", fontSize = 12.sp, color = AstralTextSecondary, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    QuestCategory.entries.forEach { cat ->
                        val isSel = cat == selectedCategory
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) AuroraTeal.copy(alpha = 0.2f) else AstralDarkSurfaceVariant,
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.linearGradient(
                                    if (isSel) listOf(AuroraTeal, AuroraTeal)
                                    else listOf(AstralDarkSurfaceBorder, AstralDarkSurfaceBorder)
                                )
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedCategory = cat
                                    xpReward = cat.defaultXp.toFloat()
                                }
                        ) {
                            Text(
                                text = cat.name.take(4),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) AuroraTeal else AstralTextSecondary,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                // Cadence Selector
                Text(text = "Cadence", fontSize = 12.sp, color = AstralTextSecondary, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuestCadence.entries.forEach { cad ->
                        val isSel = cad == selectedCadence
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) CosmicViolet.copy(alpha = 0.2f) else AstralDarkSurfaceVariant,
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.linearGradient(
                                    if (isSel) listOf(CosmicViolet, CosmicViolet)
                                    else listOf(AstralDarkSurfaceBorder, AstralDarkSurfaceBorder)
                                )
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedCadence = cad }
                        ) {
                            Text(
                                text = cad.label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) CosmicViolet else AstralTextSecondary,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                // XP Slider: 5 to 75
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "XP Reward: ${xpReward.toInt()} XP", fontSize = 12.sp, color = SolarAmber, fontWeight = FontWeight.Bold)
                    Text(text = "Range: 5–75 XP", fontSize = 11.sp, color = AstralTextMuted)
                }
                Slider(
                    value = xpReward,
                    onValueChange = { xpReward = it },
                    valueRange = 5f..75f,
                    steps = 13,
                    colors = SliderDefaults.colors(
                        thumbColor = SolarAmber,
                        activeTrackColor = SolarAmber,
                        inactiveTrackColor = AstralDarkSurfaceVariant
                    )
                )

                Button(
                    onClick = {
                        val dur = durationMinutes.toIntOrNull() ?: 0
                        onConfirm(title.ifBlank { "Untitled Quest" }, desc, selectedCategory, selectedCadence, xpReward.toInt(), dur)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_quest_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AuroraTeal,
                        contentColor = Color(0xFF042F2E)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Seal Quest", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
