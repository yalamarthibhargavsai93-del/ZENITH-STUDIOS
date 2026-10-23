package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.engine.KaizenProgressionEngine
import com.example.data.model.ActiveFocusSession
import com.example.data.model.ChallengeRecord
import com.example.data.model.FocusSessionRecord
import com.example.data.model.Quest
import com.example.data.model.QuestCadence
import com.example.data.model.QuestCategory
import com.example.data.model.QuestCompletion
import com.example.data.model.Rank
import com.example.data.model.RewardItem
import com.example.data.model.RewardRedemption
import com.example.data.model.StatType
import com.example.data.model.StatValue
import com.example.data.model.UserProfile
import com.example.data.model.XpEvent
import com.example.data.repository.FocusCompletionResult
import com.example.data.repository.KaizenRepository
import com.example.data.repository.QuestCompletionResult
import com.example.data.repository.RewardRedeemResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class KaizenScreen(val label: String) {
    COMMAND_DECK("Deck"),
    QUESTS("Quests"),
    FOCUS_ROOM("Focus"),
    CHALLENGES("Trials"),
    JOURNEY("Journey"),
    VAULT("Vault"),
    SETTINGS("Settings"),
    ONBOARDING("Arrival")
}

enum class QuestFilter {
    TODAY,
    ACTIVE,
    COMPLETED,
    ARCHIVED
}

data class LevelUpState(
    val isVisible: Boolean = false,
    val level: Int = 1,
    val rank: Rank = Rank.SEED,
    val isRankPromotion: Boolean = false
)

class KaizenViewModel(
    private val repository: KaizenRepository
) : ViewModel() {

    private val _currentUserId = MutableStateFlow("user_default")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    private val _currentScreen = MutableStateFlow(KaizenScreen.COMMAND_DECK)
    val currentScreen: StateFlow<KaizenScreen> = _currentScreen.asStateFlow()

    private val _questFilter = MutableStateFlow(QuestFilter.TODAY)
    val questFilter: StateFlow<QuestFilter> = _questFilter.asStateFlow()

    private val _snackMessage = MutableStateFlow<String?>(null)
    val snackMessage: StateFlow<String?> = _snackMessage.asStateFlow()

    private val _levelUpState = MutableStateFlow(LevelUpState())
    val levelUpState: StateFlow<LevelUpState> = _levelUpState.asStateFlow()

    // Focus room live countdown
    private val _focusRemainingSeconds = MutableStateFlow(1500)
    val focusRemainingSeconds: StateFlow<Int> = _focusRemainingSeconds.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            repository.initializeUserIfAbsent(_currentUserId.value)
        }
        startTimerTicker()
    }

    val userProfile: StateFlow<UserProfile?> = _currentUserId.flatMapLatest { id ->
        repository.getProfileFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allProfiles: StateFlow<List<UserProfile>> = repository.getAllProfilesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val statValues: StateFlow<List<StatValue>> = _currentUserId.flatMapLatest { id ->
        repository.getStatsFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quests: StateFlow<List<Quest>> = _currentUserId.flatMapLatest { id ->
        repository.getQuestsFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completions: StateFlow<List<QuestCompletion>> = _currentUserId.flatMapLatest { id ->
        repository.getCompletionsFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val xpEvents: StateFlow<List<XpEvent>> = _currentUserId.flatMapLatest { id ->
        repository.getXpEventsFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val focusSessions: StateFlow<List<FocusSessionRecord>> = _currentUserId.flatMapLatest { id ->
        repository.getFocusSessionsFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeFocusSession: StateFlow<ActiveFocusSession?> = _currentUserId.flatMapLatest { id ->
        repository.getActiveFocusSessionFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val challenges: StateFlow<List<ChallengeRecord>> = _currentUserId.flatMapLatest { id ->
        repository.getChallengesFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rewards: StateFlow<List<RewardItem>> = _currentUserId.flatMapLatest { id ->
        repository.getRewardsFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val redemptions: StateFlow<List<RewardRedemption>> = _currentUserId.flatMapLatest { id ->
        repository.getRedemptionsFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun navigateTo(screen: KaizenScreen) {
        _currentScreen.value = screen
    }

    fun setQuestFilter(filter: QuestFilter) {
        _questFilter.value = filter
    }

    fun clearSnack() {
        _snackMessage.value = null
    }

    fun dismissLevelUp() {
        _levelUpState.value = _levelUpState.value.copy(isVisible = false)
    }

    fun switchUser(newUserId: String) {
        viewModelScope.launch {
            _currentUserId.value = newUserId
            val profile = repository.initializeUserIfAbsent(newUserId)
            if (!profile.isOnboarded) {
                _currentScreen.value = KaizenScreen.ONBOARDING
            } else {
                _currentScreen.value = KaizenScreen.COMMAND_DECK
            }
            _snackMessage.value = "Switched to account: ${profile.displayName}"
        }
    }

    fun createQuest(
        title: String,
        description: String,
        category: QuestCategory,
        cadence: QuestCadence,
        xpReward: Int,
        targetDurationMinutes: Int
    ) {
        viewModelScope.launch {
            val q = Quest(
                id = "qst_${UUID.randomUUID()}",
                userId = _currentUserId.value,
                title = title.trim(),
                description = description.trim(),
                category = category.name,
                cadence = cadence.name,
                xpReward = xpReward.coerceIn(5, 75),
                targetDurationMinutes = targetDurationMinutes
            )
            repository.createOrUpdateQuest(q)
            _snackMessage.value = "Quest forged: ${q.title}"
        }
    }

    fun completeQuest(questId: String) {
        viewModelScope.launch {
            val oldLevel = userProfile.value?.level ?: 1
            val result = repository.completeQuest(_currentUserId.value, questId)
            when (result) {
                is QuestCompletionResult.Success -> {
                    val msg = "Quest completed! +${result.xpAwarded} Momentum XP (${result.statAwarded})"
                    _snackMessage.value = msg

                    if (result.leveledUp) {
                        val newRank = KaizenProgressionEngine.rankForLevel(result.newLevel)
                        val oldRank = KaizenProgressionEngine.rankForLevel(oldLevel)
                        _levelUpState.value = LevelUpState(
                            isVisible = true,
                            level = result.newLevel,
                            rank = newRank,
                            isRankPromotion = newRank != oldRank
                        )
                    }
                }
                is QuestCompletionResult.AlreadyCompleted -> {
                    _snackMessage.value = result.message
                }
                is QuestCompletionResult.Error -> {
                    _snackMessage.value = "Error: ${result.message}"
                }
            }
        }
    }

    fun archiveQuest(questId: String, isArchived: Boolean) {
        viewModelScope.launch {
            repository.archiveQuest(questId, isArchived)
            _snackMessage.value = if (isArchived) "Quest archived." else "Quest restored."
        }
    }

    fun deleteQuest(questId: String) {
        viewModelScope.launch {
            repository.deleteQuest(questId)
            _snackMessage.value = "Quest deleted."
        }
    }

    // Focus Room Controls
    fun startFocusSession(durationMinutes: Int = 25, isBreak: Boolean = false) {
        viewModelScope.launch {
            val durationSeconds = durationMinutes * 60
            repository.startFocusSession(_currentUserId.value, durationSeconds, isBreak)
            _focusRemainingSeconds.value = durationSeconds
            _snackMessage.value = if (isBreak) "Break session started." else "Astral Focus session started (25m)."
        }
    }

    fun togglePauseFocusSession() {
        viewModelScope.launch {
            val active = activeFocusSession.value ?: return@launch
            val nextPaused = !active.isPaused
            repository.pauseFocusSession(_currentUserId.value, nextPaused)
            _snackMessage.value = if (nextPaused) "Focus session paused." else "Focus session resumed."
        }
    }

    fun cancelFocusSession() {
        viewModelScope.launch {
            repository.finishFocusSession(_currentUserId.value, completedSuccessfully = false)
            _snackMessage.value = "Focus session cancelled. No XP awarded."
        }
    }

    fun completeFocusSession() {
        viewModelScope.launch {
            val oldLevel = userProfile.value?.level ?: 1
            val result = repository.finishFocusSession(_currentUserId.value, completedSuccessfully = true)
            when (result) {
                is FocusCompletionResult.Completed -> {
                    _snackMessage.value = "Focus session completed! +${result.xpAwarded} XP, +2 Focus, +${result.sparksAwarded} Spark"
                    if (result.leveledUp) {
                        val newRank = KaizenProgressionEngine.rankForLevel(result.newLevel)
                        val oldRank = KaizenProgressionEngine.rankForLevel(oldLevel)
                        _levelUpState.value = LevelUpState(
                            isVisible = true,
                            level = result.newLevel,
                            rank = newRank,
                            isRankPromotion = newRank != oldRank
                        )
                    }
                }
                FocusCompletionResult.Cancelled -> {
                    _snackMessage.value = "Focus session ended."
                }
            }
        }
    }

    private fun startTimerTicker() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val active = activeFocusSession.value
                if (active != null) {
                    val now = System.currentTimeMillis()
                    val pausedDuration = active.totalPausedDurationMs + (if (active.isPaused && active.pauseStartTime != null) now - active.pauseStartTime else 0L)
                    val effectiveElapsedMs = (now - active.startedAt) - pausedDuration
                    val elapsedSeconds = (effectiveElapsedMs / 1000).toInt()
                    val remaining = maxOf(0, active.durationSeconds - elapsedSeconds)
                    _focusRemainingSeconds.value = remaining

                    if (remaining <= 0 && !active.isPaused) {
                        completeFocusSession()
                    }
                } else {
                    _focusRemainingSeconds.value = 1500
                }
            }
        }
    }

    // Vault
    fun createReward(title: String, description: String, sparkCost: Int) {
        viewModelScope.launch {
            val reward = RewardItem(
                id = "rew_${UUID.randomUUID()}",
                userId = _currentUserId.value,
                title = title.trim(),
                description = description.trim(),
                sparkCost = sparkCost.coerceAtLeast(1)
            )
            repository.createReward(reward)
            _snackMessage.value = "Reward added to Vault: ${reward.title}"
        }
    }

    fun archiveReward(rewardId: String, isArchived: Boolean) {
        viewModelScope.launch {
            repository.archiveReward(rewardId, isArchived)
            _snackMessage.value = if (isArchived) "Reward archived." else "Reward unarchived."
        }
    }

    fun redeemReward(rewardId: String) {
        viewModelScope.launch {
            when (val res = repository.redeemReward(_currentUserId.value, rewardId)) {
                is RewardRedeemResult.Success -> {
                    _snackMessage.value = "Redeemed: ${res.reward.title}! Spent ${res.reward.sparkCost} Sparks."
                }
                is RewardRedeemResult.InsufficientSparks -> {
                    _snackMessage.value = "Insufficient Sparks. Needed: ${res.required}, available: ${res.current}"
                }
                is RewardRedeemResult.Error -> {
                    _snackMessage.value = res.message
                }
            }
        }
    }

    // Onboarding
    fun finishOnboarding(
        displayName: String,
        timezone: String,
        mainGoal: String,
        starterQuests: List<Quest>
    ) {
        viewModelScope.launch {
            repository.completeOnboarding(
                userId = _currentUserId.value,
                displayName = displayName,
                timezone = timezone,
                mainGoal = mainGoal,
                starterQuests = starterQuests
            )
            _currentScreen.value = KaizenScreen.COMMAND_DECK
            _snackMessage.value = "Welcome to Kaizen, $displayName. Your path is open."
        }
    }

    fun updateSettings(displayName: String, timezone: String, reducedMotion: Boolean) {
        viewModelScope.launch {
            repository.updateSettings(_currentUserId.value, displayName, timezone, reducedMotion)
            _snackMessage.value = "Settings updated."
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            val id = _currentUserId.value
            repository.deleteUserAccount(id)
            repository.initializeUserIfAbsent(id)
            _currentScreen.value = KaizenScreen.ONBOARDING
            _snackMessage.value = "Account reset successfully."
        }
    }
}
