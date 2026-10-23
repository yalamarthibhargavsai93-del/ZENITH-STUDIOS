package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ActiveFocusSession
import com.example.data.model.ChallengeRecord
import com.example.data.model.FocusSessionRecord
import com.example.data.model.Quest
import com.example.data.model.QuestCompletion
import com.example.data.model.RewardItem
import com.example.data.model.RewardRedemption
import com.example.data.model.StatValue
import com.example.data.model.UserProfile
import com.example.data.model.XpEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface KaizenDao {

    // Profiles
    @Query("SELECT * FROM profiles WHERE id = :userId")
    fun getProfileFlow(userId: String): Flow<UserProfile?>

    @Query("SELECT * FROM profiles WHERE id = :userId")
    suspend fun getProfile(userId: String): UserProfile?

    @Query("SELECT * FROM profiles ORDER BY createdAt ASC")
    fun getAllProfilesFlow(): Flow<List<UserProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: UserProfile)

    @Query("DELETE FROM profiles WHERE id = :userId")
    suspend fun deleteProfile(userId: String)

    // Stat Values
    @Query("SELECT * FROM stat_values WHERE userId = :userId")
    fun getStatsFlow(userId: String): Flow<List<StatValue>>

    @Query("SELECT * FROM stat_values WHERE userId = :userId")
    suspend fun getStats(userId: String): List<StatValue>

    @Query("SELECT * FROM stat_values WHERE userId = :userId AND statName = :statName")
    suspend fun getStat(userId: String, statName: String): StatValue?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStat(stat: StatValue)

    // Quests
    @Query("SELECT * FROM quests WHERE userId = :userId ORDER BY createdAt DESC")
    fun getQuestsFlow(userId: String): Flow<List<Quest>>

    @Query("SELECT * FROM quests WHERE id = :questId")
    suspend fun getQuest(questId: String): Quest?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertQuest(quest: Quest)

    @Update
    suspend fun updateQuest(quest: Quest)

    @Query("DELETE FROM quests WHERE id = :questId")
    suspend fun deleteQuest(questId: String)

    // Quest Completions
    @Query("SELECT * FROM quest_completions WHERE userId = :userId ORDER BY completedAt DESC")
    fun getCompletionsFlow(userId: String): Flow<List<QuestCompletion>>

    @Query("SELECT * FROM quest_completions WHERE userId = :userId")
    suspend fun getAllCompletions(userId: String): List<QuestCompletion>

    @Query("SELECT * FROM quest_completions WHERE id = :id")
    suspend fun getCompletion(id: String): QuestCompletion?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCompletion(completion: QuestCompletion): Long

    // XP Events
    @Query("SELECT * FROM xp_events WHERE userId = :userId ORDER BY timestamp DESC")
    fun getXpEventsFlow(userId: String): Flow<List<XpEvent>>

    @Query("SELECT * FROM xp_events WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getXpEvents(userId: String): List<XpEvent>

    @Query("SELECT * FROM xp_events WHERE userId = :userId AND localDate = :localDate AND eventType IN ('QUEST_COMPLETION', 'FOCUS_SESSION')")
    suspend fun getDailyQuestAndFocusEvents(userId: String, localDate: String): List<XpEvent>

    @Query("SELECT * FROM xp_events WHERE id = :id")
    suspend fun getXpEvent(id: String): XpEvent?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertXpEvent(event: XpEvent): Long

    // Focus Sessions
    @Query("SELECT * FROM focus_sessions WHERE userId = :userId ORDER BY startedAt DESC")
    fun getFocusSessionsFlow(userId: String): Flow<List<FocusSessionRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusSession(session: FocusSessionRecord)

    // Active Focus Session (survives app kill / refresh)
    @Query("SELECT * FROM active_focus_session WHERE userId = :userId")
    fun getActiveFocusSessionFlow(userId: String): Flow<ActiveFocusSession?>

    @Query("SELECT * FROM active_focus_session WHERE userId = :userId")
    suspend fun getActiveFocusSession(userId: String): ActiveFocusSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveActiveFocusSession(session: ActiveFocusSession)

    @Query("DELETE FROM active_focus_session WHERE userId = :userId")
    suspend fun clearActiveFocusSession(userId: String)

    // Challenges
    @Query("SELECT * FROM challenges WHERE userId = :userId ORDER BY id ASC")
    fun getChallengesFlow(userId: String): Flow<List<ChallengeRecord>>

    @Query("SELECT * FROM challenges WHERE id = :id")
    suspend fun getChallenge(id: String): ChallengeRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertChallenge(challenge: ChallengeRecord)

    // Rewards & Redemptions
    @Query("SELECT * FROM rewards WHERE userId = :userId ORDER BY createdAt DESC")
    fun getRewardsFlow(userId: String): Flow<List<RewardItem>>

    @Query("SELECT * FROM rewards WHERE id = :id")
    suspend fun getReward(id: String): RewardItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReward(reward: RewardItem)

    @Query("DELETE FROM rewards WHERE id = :id")
    suspend fun deleteReward(id: String)

    @Query("SELECT * FROM reward_redemptions WHERE userId = :userId ORDER BY redeemedAt DESC")
    fun getRedemptionsFlow(userId: String): Flow<List<RewardRedemption>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRedemption(redemption: RewardRedemption)

    // Clear all for user (Account deletion)
    @Query("DELETE FROM stat_values WHERE userId = :userId")
    suspend fun clearUserStats(userId: String)

    @Query("DELETE FROM quests WHERE userId = :userId")
    suspend fun clearUserQuests(userId: String)

    @Query("DELETE FROM quest_completions WHERE userId = :userId")
    suspend fun clearUserCompletions(userId: String)

    @Query("DELETE FROM xp_events WHERE userId = :userId")
    suspend fun clearUserEvents(userId: String)

    @Query("DELETE FROM focus_sessions WHERE userId = :userId")
    suspend fun clearUserFocus(userId: String)

    @Query("DELETE FROM challenges WHERE userId = :userId")
    suspend fun clearUserChallenges(userId: String)

    @Query("DELETE FROM rewards WHERE userId = :userId")
    suspend fun clearUserRewards(userId: String)

    @Query("DELETE FROM reward_redemptions WHERE userId = :userId")
    suspend fun clearUserRedemptions(userId: String)
}
