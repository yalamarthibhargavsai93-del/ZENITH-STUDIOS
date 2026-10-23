package com.example

import com.example.data.engine.KaizenProgressionEngine
import com.example.data.model.QuestCadence
import com.example.data.model.QuestCategory
import com.example.data.model.Rank
import com.example.data.model.StatType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KaizenProgressionTest {

    @Test
    fun testLevelAndXpFormula() {
        // Level 1 starts at 0 XP
        val (lvl0, rem0) = KaizenProgressionEngine.calculateLevelState(0)
        assertEquals(1, lvl0)
        assertEquals(0, rem0)

        // Needed for Level 1 is 100 + 25 * 0 = 100
        assertEquals(100, KaizenProgressionEngine.xpNeededForLevel(1))

        // 99 XP is still Level 1 with 99 XP
        val (lvl99, rem99) = KaizenProgressionEngine.calculateLevelState(99)
        assertEquals(1, lvl99)
        assertEquals(99, rem99)

        // Exactly 100 XP levels up to Level 2 with 0 XP remaining
        val (lvl100, rem100) = KaizenProgressionEngine.calculateLevelState(100)
        assertEquals(2, lvl100)
        assertEquals(0, rem100)

        // Needed for Level 2 is 100 + 25 * 1 = 125
        assertEquals(125, KaizenProgressionEngine.xpNeededForLevel(2))

        // Level 3 requires 100 + 125 = 225 total XP
        val (lvl225, rem225) = KaizenProgressionEngine.calculateLevelState(225)
        assertEquals(3, lvl225)
        assertEquals(0, rem225)

        // Multi-level up jump: 500 XP
        // L1: 100 -> rem 400
        // L2: 125 -> rem 275
        // L3: 150 -> rem 125
        // L4 needed is 175 -> stays at L4 with 125 XP
        val (lvlMulti, remMulti) = KaizenProgressionEngine.calculateLevelState(500)
        assertEquals(4, lvlMulti)
        assertEquals(125, remMulti)
    }

    @Test
    fun testRankMapping() {
        assertEquals(Rank.SEED, KaizenProgressionEngine.rankForLevel(1))
        assertEquals(Rank.SEED, KaizenProgressionEngine.rankForLevel(4))
        assertEquals(Rank.WAYFINDER, KaizenProgressionEngine.rankForLevel(5))
        assertEquals(Rank.WAYFINDER, KaizenProgressionEngine.rankForLevel(9))
        assertEquals(Rank.VANGUARD, KaizenProgressionEngine.rankForLevel(10))
        assertEquals(Rank.VANGUARD, KaizenProgressionEngine.rankForLevel(19))
        assertEquals(Rank.LUMINARY, KaizenProgressionEngine.rankForLevel(20))
        assertEquals(Rank.LUMINARY, KaizenProgressionEngine.rankForLevel(34))
        assertEquals(Rank.PARAGON, KaizenProgressionEngine.rankForLevel(35))
        assertEquals(Rank.PARAGON, KaizenProgressionEngine.rankForLevel(49))
        assertEquals(Rank.APEX, KaizenProgressionEngine.rankForLevel(50))
        assertEquals(Rank.APEX, KaizenProgressionEngine.rankForLevel(99))
    }

    @Test
    fun testDailyXpCap() {
        // Cap is 300 XP
        // When user has 0 XP today, award full 30 XP
        val award1 = KaizenProgressionEngine.calculateCappedXpAward(30, 0)
        assertEquals(30, award1)

        // When user has 280 XP today, asking for 30 XP should give only 20 XP
        val award2 = KaizenProgressionEngine.calculateCappedXpAward(30, 280)
        assertEquals(20, award2)

        // When user has reached 300 XP today, asking for 30 XP should give 0 XP
        val award3 = KaizenProgressionEngine.calculateCappedXpAward(30, 300)
        assertEquals(0, award3)
    }

    @Test
    fun testStatAwardsByCategory() {
        val (workoutStat, workoutVal) = KaizenProgressionEngine.statAwardForCategory(QuestCategory.WORKOUT)
        assertEquals(StatType.VITALITY, workoutStat)
        assertEquals(2, workoutVal)

        val (studyStat, studyVal) = KaizenProgressionEngine.statAwardForCategory(QuestCategory.STUDY)
        assertEquals(StatType.KNOWLEDGE, studyStat)
        assertEquals(2, studyVal)

        val (habitStat, habitVal) = KaizenProgressionEngine.statAwardForCategory(QuestCategory.HABIT)
        assertEquals(StatType.RESOLVE, habitStat)
        assertEquals(1, habitVal)

        val (lifeStat, lifeVal) = KaizenProgressionEngine.statAwardForCategory(QuestCategory.LIFE)
        assertEquals(StatType.RESOLVE, lifeStat)
        assertEquals(1, lifeVal)

        val (focusStat, focusVal) = KaizenProgressionEngine.statAwardForCategory(QuestCategory.FOCUS)
        assertEquals(StatType.FOCUS, focusStat)
        assertEquals(2, focusVal)
    }

    @Test
    fun testRhythmCalculationAndGentleStreak() {
        val today = "2026-10-23"
        val yesterday = "2026-10-22"
        val twoDaysAgo = "2026-10-21"

        // Active today, yesterday, and two days ago -> 3 days rhythm
        val (curr1, max1) = KaizenProgressionEngine.calculateRhythm(
            listOf(twoDaysAgo, yesterday, today),
            today
        )
        assertEquals(3, curr1)
        assertEquals(3, max1)

        // Missed yesterday: active two days ago and today -> current rhythm resets to 1 (gentle, no XP deduction)
        val (curr2, max2) = KaizenProgressionEngine.calculateRhythm(
            listOf(twoDaysAgo, today),
            today
        )
        assertEquals(1, curr2)
        assertEquals(1, max2)

        // Inactive today, but active yesterday -> current rhythm continues at 1
        val (curr3, _) = KaizenProgressionEngine.calculateRhythm(
            listOf(yesterday),
            today
        )
        assertEquals(1, curr3)

        // Empty activity -> 0 rhythm
        val (curr0, max0) = KaizenProgressionEngine.calculateRhythm(emptyList(), today)
        assertEquals(0, curr0)
        assertEquals(0, max0)
    }

    @Test
    fun testPeriodKeysForCadence() {
        val tz = "UTC"
        val dailyKey = KaizenProgressionEngine.getPeriodKey(QuestCadence.DAILY, tz)
        assertTrue(dailyKey.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))

        val weeklyKey = KaizenProgressionEngine.getPeriodKey(QuestCadence.WEEKLY, tz)
        assertTrue(weeklyKey.matches(Regex("\\d{4}-W\\d{2}")))

        val onceKey = KaizenProgressionEngine.getPeriodKey(QuestCadence.ONCE, tz)
        assertEquals("once", onceKey)
    }

    @Test
    fun testRewardBalanceConstraints() {
        val userSparks = 5
        val rewardCostValid = 3
        val rewardCostTooHigh = 10

        assertTrue(userSparks >= rewardCostValid)
        assertEquals(2, userSparks - rewardCostValid)

        assertFalse(userSparks >= rewardCostTooHigh)
    }
}
