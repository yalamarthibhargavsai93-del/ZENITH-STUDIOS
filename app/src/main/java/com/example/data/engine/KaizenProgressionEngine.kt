package com.example.data.engine

import com.example.data.model.QuestCadence
import com.example.data.model.QuestCategory
import com.example.data.model.Rank
import com.example.data.model.StatType
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.IsoFields
import kotlin.math.max

object KaizenProgressionEngine {

    const val DAILY_QUEST_FOCUS_XP_CAP = 300
    const val FOCUS_SESSION_MINUTES = 25
    const val FOCUS_SESSION_XP = 25
    const val FOCUS_SESSION_SPARKS = 1
    const val DAILY_CHALLENGE_XP = 40
    const val DAILY_CHALLENGE_SPARKS = 2
    const val WEEKLY_CHALLENGE_XP = 80
    const val WEEKLY_CHALLENGE_SPARKS = 5

    /**
     * XP required to advance from [level] to [level + 1].
     * Formula: 100 + 25 * (level - 1)
     */
    fun xpNeededForLevel(level: Int): Int {
        val safeLevel = max(1, level)
        return 100 + 25 * (safeLevel - 1)
    }

    /**
     * Calculates level and remaining XP in the current level from [totalXp].
     * Can level up repeatedly if totalXp crosses multiple level boundaries.
     */
    fun calculateLevelState(totalXp: Int): Pair<Int, Int> {
        var level = 1
        var remainingXp = max(0, totalXp)

        while (true) {
            val needed = xpNeededForLevel(level)
            if (remainingXp < needed) {
                return Pair(level, remainingXp)
            }
            remainingXp -= needed
            level++
        }
    }

    /**
     * Returns the rank corresponding to the given level.
     */
    fun rankForLevel(level: Int): Rank = Rank.forLevel(level)

    /**
     * Returns the stat type and amount awarded for completing a quest category.
     * workout: +2 Vitality
     * study: +2 Knowledge
     * habit / life: +1 Resolve
     * focus: +2 Focus
     */
    fun statAwardForCategory(category: QuestCategory): Pair<StatType, Int> {
        return when (category) {
            QuestCategory.WORKOUT -> Pair(StatType.VITALITY, 2)
            QuestCategory.STUDY -> Pair(StatType.KNOWLEDGE, 2)
            QuestCategory.HABIT -> Pair(StatType.RESOLVE, 1)
            QuestCategory.LIFE -> Pair(StatType.RESOLVE, 1)
            QuestCategory.FOCUS -> Pair(StatType.FOCUS, 2)
        }
    }

    /**
     * Formats current local date for the given timezone.
     */
    fun getLocalDate(timezone: String, epochMillis: Long = System.currentTimeMillis()): String {
        val zone = safeZoneId(timezone)
        val zonedDateTime = java.time.Instant.ofEpochMilli(epochMillis).atZone(zone)
        return zonedDateTime.toLocalDate().toString() // YYYY-MM-DD
    }

    /**
     * Formats current ISO week key for the given timezone (e.g. 2026-W43).
     */
    fun getLocalWeekKey(timezone: String, epochMillis: Long = System.currentTimeMillis()): String {
        val zone = safeZoneId(timezone)
        val localDate = java.time.Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()
        val year = localDate.get(IsoFields.WEEK_BASED_YEAR)
        val week = localDate.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
        return String.format("%04d-W%02d", year, week)
    }

    fun getPeriodKey(cadence: QuestCadence, timezone: String, epochMillis: Long = System.currentTimeMillis()): String {
        return when (cadence) {
            QuestCadence.DAILY -> getLocalDate(timezone, epochMillis)
            QuestCadence.WEEKLY -> getLocalWeekKey(timezone, epochMillis)
            QuestCadence.ONCE -> "once"
        }
    }

    fun safeZoneId(timezone: String): ZoneId {
        return try {
            ZoneId.of(timezone)
        } catch (_: Exception) {
            ZoneId.systemDefault()
        }
    }

    /**
     * Calculates the Rhythm count (consecutive active days) up to today.
     * [activityDates] is a set/list of distinct dates in "YYYY-MM-DD" where the user had qualifying actions.
     */
    fun calculateRhythm(activityDates: Collection<String>, todayStr: String): Pair<Int, Int> {
        if (activityDates.isEmpty()) return Pair(0, 0)

        val parsedDates = activityDates.mapNotNull {
            try { LocalDate.parse(it) } catch (_: Exception) { null }
        }.distinct().sorted()

        if (parsedDates.isEmpty()) return Pair(0, 0)

        val today = try { LocalDate.parse(todayStr) } catch (_: Exception) { LocalDate.now() }
        val yesterday = today.minusDays(1)

        val dateSet = parsedDates.toSet()

        // Current rhythm calculation:
        var currentRhythm = 0
        var checkDate = if (dateSet.contains(today)) today else if (dateSet.contains(yesterday)) yesterday else null

        if (checkDate != null) {
            var dateCursor: LocalDate = checkDate
            while (dateSet.contains(dateCursor)) {
                currentRhythm++
                dateCursor = dateCursor.minusDays(1)
            }
        }

        // Longest rhythm calculation across all history:
        var maxStreak = 0
        var currentRun = 0
        var prevDate: LocalDate? = null

        for (d in parsedDates) {
            if (prevDate == null) {
                currentRun = 1
            } else {
                val diff = ChronoUnit.DAYS.between(prevDate, d)
                if (diff == 1L) {
                    currentRun++
                } else if (diff > 1L) {
                    currentRun = 1
                }
            }
            if (currentRun > maxStreak) {
                maxStreak = currentRun
            }
            prevDate = d
        }

        return Pair(currentRhythm, maxStreak)
    }

    /**
     * Calculates allowable XP to grant considering the daily 300 cap.
     */
    fun calculateCappedXpAward(rawXp: Int, currentTodayXp: Int): Int {
        val remaining = max(0, DAILY_QUEST_FOCUS_XP_CAP - currentTodayXp)
        return kotlin.math.min(rawXp, remaining)
    }
}
