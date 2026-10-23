package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class QuestCategory(val label: String, val defaultXp: Int) {
    WORKOUT("Movement / Workout", 30),
    STUDY("Study / Deep Work", 25),
    HABIT("Daily Habit", 15),
    FOCUS("Focus Block", 25),
    LIFE("Life Quest", 10)
}

enum class QuestCadence(val label: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    ONCE("One-Time")
}

enum class StatType(val label: String) {
    VITALITY("Vitality"),
    FOCUS("Focus"),
    KNOWLEDGE("Knowledge"),
    RESOLVE("Resolve")
}

enum class Rank(val title: String, val minLevel: Int, val maxLevel: Int, val description: String) {
    SEED("Seed", 1, 4, "Initial spark of disciplined cultivation."),
    WAYFINDER("Wayfinder", 5, 9, "Charted rhythm through chaotic currents."),
    VANGUARD("Vanguard", 10, 19, "Unyielding presence leading the self forward."),
    LUMINARY("Luminary", 20, 34, "Brilliant clarity across knowledge and resolve."),
    PARAGON("Paragon", 35, 49, "Mastery of steady, earned compounding."),
    APEX("Apex", 50, Int.MAX_VALUE, "Zenith of habitual mastery and quiet strength.");

    companion object {
        fun forLevel(level: Int): Rank =
            entries.firstOrNull { level in it.minLevel..it.maxLevel } ?: APEX
    }
}

enum class ChallengeType {
    DAILY_ACTIONS,
    WEEKLY_VITALITY,
    WEEKLY_FOCUS_DURATION,
    WEEKLY_ACTIVE_DAYS
}

/**
 * 1. User Profile Entity
 * Stores user progression, identity parameters, currency, and rhythm streaks.
 */
@Entity(tableName = "profiles")
data class UserProfile(
    @PrimaryKey val id: String = "user_default",
    val displayName: String = "Starlight Wayfinder",
    val email: String = "seeker@kaizen.local",
    val timezone: String = "UTC",
    val mainGoal: String = "Master focused habits and consistent vitality",
    val totalXp: Int = 0,
    val level: Int = 1,
    val xpInCurrentLevel: Int = 0,
    val sparks: Int = 0,
    val currentRhythm: Int = 0,
    val longestRhythm: Int = 0,
    val lastActiveDate: String? = null,
    val isOnboarded: Boolean = false,
    val reducedMotion: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 2. Stat Values Entity
 * Stores stat points for each discipline: Vitality, Focus, Knowledge, Resolve.
 */
@Entity(
    tableName = "stat_values",
    primaryKeys = ["userId", "statName"],
    indices = [Index(value = ["userId"])]
)
data class StatValue(
    val userId: String,
    val statName: String,
    val value: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 3. Quests Entity
 * Stores custom and starter quests across workouts, study, habits, focus, and life tasks.
 */
@Entity(
    tableName = "quests",
    indices = [Index(value = ["userId"])]
)
data class Quest(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val description: String = "",
    val category: String, // from QuestCategory.name
    val cadence: String, // from QuestCadence.name
    val xpReward: Int = 25,
    val targetDurationMinutes: Int = 0,
    val scheduledDay: String = "ALL",
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 4. Quest Completions Entity
 * Idempotently tracks completions using a composite key: qc_${userId}_${questId}_${periodKey}.
 */
@Entity(
    tableName = "quest_completions",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["userId", "questId", "periodKey"], unique = true),
        Index(value = ["userId", "localDate"])
    ]
)
data class QuestCompletion(
    @PrimaryKey val id: String, // idempotency: qc_${userId}_${questId}_${periodKey}
    val userId: String,
    val questId: String,
    val localDate: String, // YYYY-MM-DD
    val periodKey: String, // YYYY-MM-DD for daily, YYYY-Www for weekly, "once" for once
    val xpAwarded: Int,
    val completedAt: Long = System.currentTimeMillis()
)

/**
 * 5. XP Events Entity
 * Immutable audit ledger of all XP and Spark awards with unique idempotency keys.
 */
@Entity(
    tableName = "xp_events",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["userId", "localDate"])
    ]
)
data class XpEvent(
    @PrimaryKey val id: String, // unique idempotency key
    val userId: String,
    val eventType: String,
    val xpDelta: Int,
    val sparkDelta: Int,
    val reason: String,
    val localDate: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 6. Focus Sessions Entity
 * Historical records of completed and cancelled Pomodoro focus chambers.
 */
@Entity(
    tableName = "focus_sessions",
    indices = [Index(value = ["userId"])]
)
data class FocusSessionRecord(
    @PrimaryKey val id: String,
    val userId: String,
    val startedAt: Long,
    val endedAt: Long?,
    val plannedDurationSeconds: Int = 1500, // 25 min default
    val actualDurationSeconds: Int = 0,
    val status: String, // ACTIVE, COMPLETED, CANCELLED
    val xpAwarded: Int = 0,
    val sparksAwarded: Int = 0,
    val localDate: String
)

/**
 * 7. Active Focus Session Entity
 * Stores active running session state so timer countdown survives app kills / restarts.
 */
@Entity(tableName = "active_focus_session")
data class ActiveFocusSession(
    @PrimaryKey val userId: String,
    val startedAt: Long,
    val durationSeconds: Int = 1500,
    val isBreak: Boolean = false,
    val isPaused: Boolean = false,
    val pauseStartTime: Long? = null,
    val totalPausedDurationMs: Long = 0L,
    val title: String = "Astral Focus Chamber"
)

/**
 * 8. Challenges Entity
 * Stores daily and weekly residency challenges with idempotent period keys.
 */
@Entity(
    tableName = "challenges",
    indices = [Index(value = ["userId", "periodKey"])]
)
data class ChallengeRecord(
    @PrimaryKey val id: String, // idempotency: ch_${userId}_${challengeType}_${periodKey}
    val userId: String,
    val challengeType: String,
    val title: String,
    val description: String,
    val targetValue: Int,
    val currentValue: Int,
    val periodKey: String,
    val xpReward: Int,
    val sparkReward: Int,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null
)

/**
 * 9. Vault Rewards Entity
 * User-created real-life rewards (e.g. cinema night, coffee treat, gaming hour).
 */
@Entity(
    tableName = "rewards",
    indices = [Index(value = ["userId"])]
)
data class RewardItem(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val description: String = "",
    val sparkCost: Int = 5,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 10. Reward Redemptions Entity
 * Records redemptions after verifying sufficient Spark balance.
 */
@Entity(
    tableName = "reward_redemptions",
    indices = [Index(value = ["userId"])]
)
data class RewardRedemption(
    @PrimaryKey val id: String,
    val userId: String,
    val rewardId: String,
    val rewardTitle: String,
    val sparkCost: Int,
    val redeemedAt: Long = System.currentTimeMillis()
)
