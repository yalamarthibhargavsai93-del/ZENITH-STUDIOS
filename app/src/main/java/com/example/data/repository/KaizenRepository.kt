package com.example.data.repository

import com.example.data.KaizenDatabase
import com.example.data.dao.KaizenDao
import com.example.data.engine.KaizenProgressionEngine
import com.example.data.model.ActiveFocusSession
import com.example.data.model.ChallengeRecord
import com.example.data.model.ChallengeType
import com.example.data.model.FocusSessionRecord
import com.example.data.model.Quest
import com.example.data.model.QuestCadence
import com.example.data.model.QuestCategory
import com.example.data.model.QuestCompletion
import com.example.data.model.RewardItem
import com.example.data.model.RewardRedemption
import com.example.data.model.StatType
import com.example.data.model.StatValue
import com.example.data.model.UserProfile
import com.example.data.model.XpEvent
import kotlinx.coroutines.flow.Flow
import java.util.UUID

sealed class QuestCompletionResult {
    data class Success(
        val quest: Quest,
        val xpAwarded: Int,
        val leveledUp: Boolean,
        val newLevel: Int,
        val statAwarded: String
    ) : QuestCompletionResult()
    data class AlreadyCompleted(val message: String) : QuestCompletionResult()
    data class Error(val message: String) : QuestCompletionResult()
}

sealed class FocusCompletionResult {
    data class Completed(
        val xpAwarded: Int,
        val sparksAwarded: Int,
        val leveledUp: Boolean,
        val newLevel: Int
    ) : FocusCompletionResult()
    object Cancelled : FocusCompletionResult()
}

sealed class RewardRedeemResult {
    data class Success(val reward: RewardItem, val remainingSparks: Int) : RewardRedeemResult()
    data class InsufficientSparks(val current: Int, val required: Int) : RewardRedeemResult()
    data class Error(val message: String) : RewardRedeemResult()
}

class KaizenRepository(
    private val database: KaizenDatabase
) {
    private val dao: KaizenDao = database.kaizenDao()

    fun getProfileFlow(userId: String): Flow<UserProfile?> = dao.getProfileFlow(userId)
    fun getAllProfilesFlow(): Flow<List<UserProfile>> = dao.getAllProfilesFlow()
    fun getStatsFlow(userId: String): Flow<List<StatValue>> = dao.getStatsFlow(userId)
    fun getQuestsFlow(userId: String): Flow<List<Quest>> = dao.getQuestsFlow(userId)
    fun getCompletionsFlow(userId: String): Flow<List<QuestCompletion>> = dao.getCompletionsFlow(userId)
    fun getXpEventsFlow(userId: String): Flow<List<XpEvent>> = dao.getXpEventsFlow(userId)
    fun getFocusSessionsFlow(userId: String): Flow<List<FocusSessionRecord>> = dao.getFocusSessionsFlow(userId)
    fun getActiveFocusSessionFlow(userId: String): Flow<ActiveFocusSession?> = dao.getActiveFocusSessionFlow(userId)
    fun getChallengesFlow(userId: String): Flow<List<ChallengeRecord>> = dao.getChallengesFlow(userId)
    fun getRewardsFlow(userId: String): Flow<List<RewardItem>> = dao.getRewardsFlow(userId)
    fun getRedemptionsFlow(userId: String): Flow<List<RewardRedemption>> = dao.getRedemptionsFlow(userId)

    suspend fun getProfile(userId: String): UserProfile? = dao.getProfile(userId)

    suspend fun initializeUserIfAbsent(userId: String): UserProfile {
        val existing = dao.getProfile(userId)
        if (existing != null) {
            ensureChallenges(userId, existing.timezone)
            return existing
        }

        val newProfile = UserProfile(
            id = userId,
            displayName = "Starlight Wayfinder",
            email = "$userId@kaizen.local",
            timezone = java.time.ZoneId.systemDefault().id,
            mainGoal = "Cultivate daily clarity and physical resilience",
            totalXp = 0,
            level = 1,
            xpInCurrentLevel = 0,
            sparks = 5, // Welcoming starter sparks
            currentRhythm = 0,
            longestRhythm = 0,
            isOnboarded = false
        )
        dao.upsertProfile(newProfile)

        // Initialize 4 stats
        for (stat in StatType.entries) {
            dao.upsertStat(StatValue(userId = userId, statName = stat.name, value = 0))
        }

        ensureChallenges(userId, newProfile.timezone)
        return newProfile
    }

    suspend fun completeOnboarding(
        userId: String,
        displayName: String,
        timezone: String,
        mainGoal: String,
        starterQuests: List<Quest>
    ) {
        val profile = dao.getProfile(userId) ?: UserProfile(id = userId)
        val updated = profile.copy(
            displayName = displayName.ifBlank { "Seeker" },
            timezone = timezone,
            mainGoal = mainGoal.ifBlank { "Steady personal mastery" },
            isOnboarded = true
        )
        dao.upsertProfile(updated)

        for (q in starterQuests) {
            dao.upsertQuest(q.copy(userId = userId))
        }

        // Add 2 default rewards to Vault for inspiration
        val defaultRewards = listOf(
            RewardItem(
                id = "rew_${userId}_1",
                userId = userId,
                title = "1 Hour Gaming or Favorite Show",
                description = "Guilt-free relaxation after earning today's rhythm.",
                sparkCost = 3
            ),
            RewardItem(
                id = "rew_${userId}_2",
                userId = userId,
                title = "Special Coffee or Treat",
                description = "Savor an artisanal espresso or treat.",
                sparkCost = 5
            )
        )
        for (rew in defaultRewards) {
            if (dao.getReward(rew.id) == null) {
                dao.upsertReward(rew)
            }
        }

        ensureChallenges(userId, timezone)
    }

    suspend fun createOrUpdateQuest(quest: Quest) {
        dao.upsertQuest(quest)
    }

    suspend fun archiveQuest(questId: String, isArchived: Boolean) {
        val quest = dao.getQuest(questId) ?: return
        dao.updateQuest(quest.copy(isArchived = isArchived))
    }

    suspend fun deleteQuest(questId: String) {
        dao.deleteQuest(questId)
    }

    suspend fun completeQuest(userId: String, questId: String): QuestCompletionResult {
        val profile = dao.getProfile(userId) ?: return QuestCompletionResult.Error("User not found")
        val quest = dao.getQuest(questId) ?: return QuestCompletionResult.Error("Quest not found")

        val cadence = try { QuestCadence.valueOf(quest.cadence) } catch (_: Exception) { QuestCadence.DAILY }
        val category = try { QuestCategory.valueOf(quest.category) } catch (_: Exception) { QuestCategory.LIFE }
        val localDate = KaizenProgressionEngine.getLocalDate(profile.timezone)
        val periodKey = KaizenProgressionEngine.getPeriodKey(cadence, profile.timezone)
        val completionId = "qc_${userId}_${questId}_${periodKey}"

        // Idempotency check:
        if (dao.getCompletion(completionId) != null) {
            return QuestCompletionResult.AlreadyCompleted("Quest already completed for this $cadence period.")
        }

        // Calculate Daily XP Cap (300 cap for quest + focus)
        val todayEvents = dao.getDailyQuestAndFocusEvents(userId, localDate)
        val todayXp = todayEvents.sumOf { it.xpDelta }
        val xpToAward = KaizenProgressionEngine.calculateCappedXpAward(quest.xpReward, todayXp)

        // Insert Completion record
        val completion = QuestCompletion(
            id = completionId,
            userId = userId,
            questId = questId,
            localDate = localDate,
            periodKey = periodKey,
            xpAwarded = xpToAward
        )
        val inserted = dao.insertCompletion(completion)
        if (inserted == -1L) {
            return QuestCompletionResult.AlreadyCompleted("Quest completion was already recorded.")
        }

        // Insert XP Event
        val xpEvent = XpEvent(
            id = "xpe_$completionId",
            userId = userId,
            eventType = "QUEST_COMPLETION",
            xpDelta = xpToAward,
            sparkDelta = 0,
            reason = "Completed ${quest.cadence.lowercase()} quest: ${quest.title}",
            localDate = localDate
        )
        dao.insertXpEvent(xpEvent)

        // Stat award
        val (statType, statDelta) = KaizenProgressionEngine.statAwardForCategory(category)
        val currentStat = dao.getStat(userId, statType.name)?.value ?: 0
        dao.upsertStat(StatValue(userId = userId, statName = statType.name, value = currentStat + statDelta))

        // Level & Rhythm calculation
        val oldLevel = profile.level
        val newTotalXp = profile.totalXp + xpToAward
        val (newLevel, newXpInLevel) = KaizenProgressionEngine.calculateLevelState(newTotalXp)
        val leveledUp = newLevel > oldLevel

        // Rhythm
        val allCompletions = dao.getAllCompletions(userId)
        val allActivityDates = allCompletions.map { it.localDate }.toMutableSet()
        allActivityDates.add(localDate)
        val (currentRhythm, longestRhythm) = KaizenProgressionEngine.calculateRhythm(allActivityDates, localDate)

        val updatedProfile = profile.copy(
            totalXp = newTotalXp,
            level = newLevel,
            xpInCurrentLevel = newXpInLevel,
            currentRhythm = currentRhythm,
            longestRhythm = maxOf(longestRhythm, profile.longestRhythm),
            lastActiveDate = localDate
        )
        dao.upsertProfile(updatedProfile)

        // Advance challenges
        recordActionForChallenges(userId, profile.timezone, category, quest.targetDurationMinutes)

        return QuestCompletionResult.Success(
            quest = quest,
            xpAwarded = xpToAward,
            leveledUp = leveledUp,
            newLevel = newLevel,
            statAwarded = "+$statDelta ${statType.label}"
        )
    }

    suspend fun startFocusSession(userId: String, durationSeconds: Int = 1500, isBreak: Boolean = false) {
        val session = ActiveFocusSession(
            userId = userId,
            startedAt = System.currentTimeMillis(),
            durationSeconds = durationSeconds,
            isBreak = isBreak,
            isPaused = false
        )
        dao.saveActiveFocusSession(session)
    }

    suspend fun pauseFocusSession(userId: String, isPaused: Boolean) {
        val current = dao.getActiveFocusSession(userId) ?: return
        val now = System.currentTimeMillis()
        val updated = if (isPaused) {
            current.copy(isPaused = true, pauseStartTime = now)
        } else {
            val pausedMs = if (current.pauseStartTime != null) now - current.pauseStartTime else 0L
            current.copy(
                isPaused = false,
                pauseStartTime = null,
                totalPausedDurationMs = current.totalPausedDurationMs + pausedMs
            )
        }
        dao.saveActiveFocusSession(updated)
    }

    suspend fun finishFocusSession(userId: String, completedSuccessfully: Boolean): FocusCompletionResult {
        val active = dao.getActiveFocusSession(userId)
        dao.clearActiveFocusSession(userId)

        if (active == null || !completedSuccessfully || active.isBreak) {
            // Cancelled or break session -> No awards
            if (active != null && !active.isBreak) {
                val profile = dao.getProfile(userId)
                val localDate = KaizenProgressionEngine.getLocalDate(profile?.timezone ?: "UTC")
                val record = FocusSessionRecord(
                    id = "foc_${UUID.randomUUID()}",
                    userId = userId,
                    startedAt = active.startedAt,
                    endedAt = System.currentTimeMillis(),
                    plannedDurationSeconds = active.durationSeconds,
                    actualDurationSeconds = ((System.currentTimeMillis() - active.startedAt) / 1000).toInt(),
                    status = "CANCELLED",
                    xpAwarded = 0,
                    sparksAwarded = 0,
                    localDate = localDate
                )
                dao.insertFocusSession(record)
            }
            return FocusCompletionResult.Cancelled
        }

        val profile = dao.getProfile(userId) ?: return FocusCompletionResult.Cancelled
        val localDate = KaizenProgressionEngine.getLocalDate(profile.timezone)

        // Award 25 XP, +2 Focus, 1 Spark
        val todayEvents = dao.getDailyQuestAndFocusEvents(userId, localDate)
        val todayXp = todayEvents.sumOf { it.xpDelta }
        val xpToAward = KaizenProgressionEngine.calculateCappedXpAward(KaizenProgressionEngine.FOCUS_SESSION_XP, todayXp)
        val sparksToAward = KaizenProgressionEngine.FOCUS_SESSION_SPARKS

        val sessionId = "foc_${UUID.randomUUID()}"
        val record = FocusSessionRecord(
            id = sessionId,
            userId = userId,
            startedAt = active.startedAt,
            endedAt = System.currentTimeMillis(),
            plannedDurationSeconds = active.durationSeconds,
            actualDurationSeconds = active.durationSeconds,
            status = "COMPLETED",
            xpAwarded = xpToAward,
            sparksAwarded = sparksToAward,
            localDate = localDate
        )
        dao.insertFocusSession(record)

        val xpEvent = XpEvent(
            id = "xpe_$sessionId",
            userId = userId,
            eventType = "FOCUS_SESSION",
            xpDelta = xpToAward,
            sparkDelta = sparksToAward,
            reason = "Completed 25-minute Astral Focus session",
            localDate = localDate
        )
        dao.insertXpEvent(xpEvent)

        // +2 Focus stat
        val currentFocus = dao.getStat(userId, StatType.FOCUS.name)?.value ?: 0
        dao.upsertStat(StatValue(userId = userId, statName = StatType.FOCUS.name, value = currentFocus + 2))

        val oldLevel = profile.level
        val newTotalXp = profile.totalXp + xpToAward
        val (newLevel, newXpInLevel) = KaizenProgressionEngine.calculateLevelState(newTotalXp)
        val leveledUp = newLevel > oldLevel

        val allCompletions = dao.getAllCompletions(userId)
        val allActivityDates = allCompletions.map { it.localDate }.toMutableSet()
        allActivityDates.add(localDate)
        val (currentRhythm, longestRhythm) = KaizenProgressionEngine.calculateRhythm(allActivityDates, localDate)

        val updatedProfile = profile.copy(
            totalXp = newTotalXp,
            level = newLevel,
            xpInCurrentLevel = newXpInLevel,
            sparks = profile.sparks + sparksToAward,
            currentRhythm = currentRhythm,
            longestRhythm = maxOf(longestRhythm, profile.longestRhythm),
            lastActiveDate = localDate
        )
        dao.upsertProfile(updatedProfile)

        // Challenge advancement
        recordActionForChallenges(userId, profile.timezone, QuestCategory.FOCUS, 25)

        return FocusCompletionResult.Completed(
            xpAwarded = xpToAward,
            sparksAwarded = sparksToAward,
            leveledUp = leveledUp,
            newLevel = newLevel
        )
    }

    suspend fun ensureChallenges(userId: String, timezone: String) {
        val localDate = KaizenProgressionEngine.getLocalDate(timezone)
        val localWeek = KaizenProgressionEngine.getLocalWeekKey(timezone)

        // 1. Daily Challenge: Complete 2 qualifying actions today (40 XP, 2 Sparks)
        val dailyId = "ch_${userId}_${ChallengeType.DAILY_ACTIONS.name}_$localDate"
        if (dao.getChallenge(dailyId) == null) {
            dao.upsertChallenge(
                ChallengeRecord(
                    id = dailyId,
                    userId = userId,
                    challengeType = ChallengeType.DAILY_ACTIONS.name,
                    title = "Daily Concord",
                    description = "Complete 2 qualifying actions (quests or focus sessions) today.",
                    targetValue = 2,
                    currentValue = 0,
                    periodKey = localDate,
                    xpReward = KaizenProgressionEngine.DAILY_CHALLENGE_XP,
                    sparkReward = KaizenProgressionEngine.DAILY_CHALLENGE_SPARKS
                )
            )
        }

        // 2. Weekly Challenges (80 XP, 5 Sparks each)
        // 2a. Complete 3 Vitality actions
        val wVitalityId = "ch_${userId}_${ChallengeType.WEEKLY_VITALITY.name}_$localWeek"
        if (dao.getChallenge(wVitalityId) == null) {
            dao.upsertChallenge(
                ChallengeRecord(
                    id = wVitalityId,
                    userId = userId,
                    challengeType = ChallengeType.WEEKLY_VITALITY.name,
                    title = "Vitality Surge",
                    description = "Complete 3 movement or workout quests this week.",
                    targetValue = 3,
                    currentValue = 0,
                    periodKey = localWeek,
                    xpReward = KaizenProgressionEngine.WEEKLY_CHALLENGE_XP,
                    sparkReward = KaizenProgressionEngine.WEEKLY_CHALLENGE_SPARKS
                )
            )
        }

        // 2b. Complete 120 minutes of study/focus
        val wFocusId = "ch_${userId}_${ChallengeType.WEEKLY_FOCUS_DURATION.name}_$localWeek"
        if (dao.getChallenge(wFocusId) == null) {
            dao.upsertChallenge(
                ChallengeRecord(
                    id = wFocusId,
                    userId = userId,
                    challengeType = ChallengeType.WEEKLY_FOCUS_DURATION.name,
                    title = "Deep Resonance",
                    description = "Accumulate 120 minutes of study or focus work this week.",
                    targetValue = 120,
                    currentValue = 0,
                    periodKey = localWeek,
                    xpReward = KaizenProgressionEngine.WEEKLY_CHALLENGE_XP,
                    sparkReward = KaizenProgressionEngine.WEEKLY_CHALLENGE_SPARKS
                )
            )
        }

        // 2c. Active on 5 different days
        val wActiveDaysId = "ch_${userId}_${ChallengeType.WEEKLY_ACTIVE_DAYS.name}_$localWeek"
        if (dao.getChallenge(wActiveDaysId) == null) {
            dao.upsertChallenge(
                ChallengeRecord(
                    id = wActiveDaysId,
                    userId = userId,
                    challengeType = ChallengeType.WEEKLY_ACTIVE_DAYS.name,
                    title = "Constellation Stride",
                    description = "Log activity on 5 different days during this week.",
                    targetValue = 5,
                    currentValue = 0,
                    periodKey = localWeek,
                    xpReward = KaizenProgressionEngine.WEEKLY_CHALLENGE_XP,
                    sparkReward = KaizenProgressionEngine.WEEKLY_CHALLENGE_SPARKS
                )
            )
        }
    }

    private suspend fun recordActionForChallenges(
        userId: String,
        timezone: String,
        category: QuestCategory,
        durationMinutes: Int
    ) {
        val localDate = KaizenProgressionEngine.getLocalDate(timezone)
        val localWeek = KaizenProgressionEngine.getLocalWeekKey(timezone)

        // 1. Advance daily challenge
        val dailyId = "ch_${userId}_${ChallengeType.DAILY_ACTIONS.name}_$localDate"
        dao.getChallenge(dailyId)?.let { ch ->
            if (!ch.isCompleted) {
                val newCount = ch.currentValue + 1
                val completed = newCount >= ch.targetValue
                dao.upsertChallenge(ch.copy(
                    currentValue = newCount,
                    isCompleted = completed,
                    completedAt = if (completed) System.currentTimeMillis() else null
                ))
                if (completed) {
                    awardChallengeBonus(userId, timezone, ch)
                }
            }
        }

        // 2. Weekly Vitality
        if (category == QuestCategory.WORKOUT) {
            val wVitalityId = "ch_${userId}_${ChallengeType.WEEKLY_VITALITY.name}_$localWeek"
            dao.getChallenge(wVitalityId)?.let { ch ->
                if (!ch.isCompleted) {
                    val newCount = ch.currentValue + 1
                    val completed = newCount >= ch.targetValue
                    dao.upsertChallenge(ch.copy(
                        currentValue = newCount,
                        isCompleted = completed,
                        completedAt = if (completed) System.currentTimeMillis() else null
                    ))
                    if (completed) {
                        awardChallengeBonus(userId, timezone, ch)
                    }
                }
            }
        }

        // 3. Weekly Focus/Study duration
        if (category == QuestCategory.FOCUS || category == QuestCategory.STUDY) {
            val minutes = if (durationMinutes > 0) durationMinutes else 25
            val wFocusId = "ch_${userId}_${ChallengeType.WEEKLY_FOCUS_DURATION.name}_$localWeek"
            dao.getChallenge(wFocusId)?.let { ch ->
                if (!ch.isCompleted) {
                    val newTotal = ch.currentValue + minutes
                    val completed = newTotal >= ch.targetValue
                    dao.upsertChallenge(ch.copy(
                        currentValue = newTotal,
                        isCompleted = completed,
                        completedAt = if (completed) System.currentTimeMillis() else null
                    ))
                    if (completed) {
                        awardChallengeBonus(userId, timezone, ch)
                    }
                }
            }
        }

        // 4. Weekly active days
        val wActiveDaysId = "ch_${userId}_${ChallengeType.WEEKLY_ACTIVE_DAYS.name}_$localWeek"
        dao.getChallenge(wActiveDaysId)?.let { ch ->
            if (!ch.isCompleted) {
                // Count distinct days active in this week
                val allCompletions = dao.getAllCompletions(userId)
                val thisWeekDays = allCompletions
                    .map { it.localDate }
                    .filter { KaizenProgressionEngine.getLocalWeekKey(timezone, parseDateEpoch(it)) == localWeek }
                    .distinct()
                    .count()
                val completed = thisWeekDays >= ch.targetValue
                dao.upsertChallenge(ch.copy(
                    currentValue = thisWeekDays,
                    isCompleted = completed,
                    completedAt = if (completed) System.currentTimeMillis() else null
                ))
                if (completed) {
                    awardChallengeBonus(userId, timezone, ch)
                }
            }
        }
    }

    private fun parseDateEpoch(dateStr: String): Long {
        return try {
            java.time.LocalDate.parse(dateStr).atStartOfDay(java.time.ZoneId.of("UTC")).toInstant().toEpochMilli()
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }

    private suspend fun awardChallengeBonus(userId: String, timezone: String, challenge: ChallengeRecord) {
        val profile = dao.getProfile(userId) ?: return
        val localDate = KaizenProgressionEngine.getLocalDate(timezone)
        val eventId = "xpe_challenge_${challenge.id}"

        // Challenges are exempt from daily cap!
        val inserted = dao.insertXpEvent(
            XpEvent(
                id = eventId,
                userId = userId,
                eventType = "CHALLENGE_REWARD",
                xpDelta = challenge.xpReward,
                sparkDelta = challenge.sparkReward,
                reason = "Challenge Completed: ${challenge.title}",
                localDate = localDate
            )
        )
        if (inserted != -1L) {
            val newTotalXp = profile.totalXp + challenge.xpReward
            val (newLevel, newXpInLevel) = KaizenProgressionEngine.calculateLevelState(newTotalXp)
            val updated = profile.copy(
                totalXp = newTotalXp,
                level = newLevel,
                xpInCurrentLevel = newXpInLevel,
                sparks = profile.sparks + challenge.sparkReward
            )
            dao.upsertProfile(updated)
        }
    }

    suspend fun createReward(reward: RewardItem) {
        dao.upsertReward(reward)
    }

    suspend fun archiveReward(rewardId: String, isArchived: Boolean) {
        val r = dao.getReward(rewardId) ?: return
        dao.upsertReward(r.copy(isArchived = isArchived))
    }

    suspend fun redeemReward(userId: String, rewardId: String): RewardRedeemResult {
        val profile = dao.getProfile(userId) ?: return RewardRedeemResult.Error("User profile not found")
        val reward = dao.getReward(rewardId) ?: return RewardRedeemResult.Error("Reward item not found")

        if (profile.sparks < reward.sparkCost) {
            return RewardRedeemResult.InsufficientSparks(current = profile.sparks, required = reward.sparkCost)
        }

        val remainingSparks = profile.sparks - reward.sparkCost
        val updatedProfile = profile.copy(sparks = remainingSparks)
        dao.upsertProfile(updatedProfile)

        val redemption = RewardRedemption(
            id = "red_${UUID.randomUUID()}",
            userId = userId,
            rewardId = reward.id,
            rewardTitle = reward.title,
            sparkCost = reward.sparkCost,
            redeemedAt = System.currentTimeMillis()
        )
        dao.insertRedemption(redemption)

        return RewardRedeemResult.Success(reward = reward, remainingSparks = remainingSparks)
    }

    suspend fun updateSettings(
        userId: String,
        displayName: String,
        timezone: String,
        reducedMotion: Boolean
    ) {
        val profile = dao.getProfile(userId) ?: return
        dao.upsertProfile(
            profile.copy(
                displayName = displayName.ifBlank { profile.displayName },
                timezone = timezone,
                reducedMotion = reducedMotion
            )
        )
    }

    suspend fun deleteUserAccount(userId: String) {
        dao.clearUserStats(userId)
        dao.clearUserQuests(userId)
        dao.clearUserCompletions(userId)
        dao.clearUserEvents(userId)
        dao.clearUserFocus(userId)
        dao.clearUserChallenges(userId)
        dao.clearUserRewards(userId)
        dao.clearUserRedemptions(userId)
        dao.clearActiveFocusSession(userId)
        dao.deleteProfile(userId)
    }

    suspend fun exportUserDataJson(userId: String): String {
        val profile = dao.getProfile(userId) ?: return "{}"
        val stats = dao.getStats(userId)
        val completions = dao.getAllCompletions(userId)
        val events = dao.getXpEvents(userId)
        return """
            {
              "exportedAt": ${System.currentTimeMillis()},
              "profile": {
                "displayName": "${profile.displayName}",
                "level": ${profile.level},
                "totalXp": ${profile.totalXp},
                "sparks": ${profile.sparks},
                "currentRhythm": ${profile.currentRhythm},
                "longestRhythm": ${profile.longestRhythm},
                "timezone": "${profile.timezone}"
              },
              "stats": [
                ${stats.joinToString(",") { """{"stat":"${it.statName}","value":${it.value}}""" }}
              ],
              "completionsCount": ${completions.size},
              "totalXpEventsCount": ${events.size}
            }
        """.trimIndent()
    }
}
