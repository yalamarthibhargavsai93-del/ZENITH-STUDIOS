package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.LevelUpCelebrationModal
import com.example.ui.screens.ChallengesScreen
import com.example.ui.screens.CommandDeckScreen
import com.example.ui.screens.FocusRoomScreen
import com.example.ui.screens.JourneyScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.QuestsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VaultScreen
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

@Composable
fun KaizenApp(
    viewModel: KaizenViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val allProfiles by viewModel.allProfiles.collectAsStateWithLifecycle()
    val currentUserId by viewModel.currentUserId.collectAsStateWithLifecycle()
    val statValues by viewModel.statValues.collectAsStateWithLifecycle()
    val quests by viewModel.quests.collectAsStateWithLifecycle()
    val completions by viewModel.completions.collectAsStateWithLifecycle()
    val xpEvents by viewModel.xpEvents.collectAsStateWithLifecycle()
    val focusSessions by viewModel.focusSessions.collectAsStateWithLifecycle()
    val activeFocusSession by viewModel.activeFocusSession.collectAsStateWithLifecycle()
    val focusRemaining by viewModel.focusRemainingSeconds.collectAsStateWithLifecycle()
    val challenges by viewModel.challenges.collectAsStateWithLifecycle()
    val rewards by viewModel.rewards.collectAsStateWithLifecycle()
    val redemptions by viewModel.redemptions.collectAsStateWithLifecycle()
    val questFilter by viewModel.questFilter.collectAsStateWithLifecycle()
    val levelUpState by viewModel.levelUpState.collectAsStateWithLifecycle()
    val snackMessage by viewModel.snackMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackMessage) {
        snackMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnack()
        }
    }

    // Handle back button
    BackHandler(enabled = currentScreen != KaizenScreen.COMMAND_DECK && currentScreen != KaizenScreen.ONBOARDING) {
        viewModel.navigateTo(KaizenScreen.COMMAND_DECK)
    }

    // Determine if onboarding is needed
    val showOnboarding = userProfile != null && !userProfile!!.isOnboarded

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AstralDarkBg,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AstralDarkCard,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Brush.linearGradient(listOf(AuroraTeal.copy(alpha = 0.6f), AstralDarkSurfaceBorder))
                    ),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = data.visuals.message,
                        color = AstralTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }
        },
        bottomBar = {
            if (!showOnboarding && currentScreen != KaizenScreen.ONBOARDING) {
                KaizenBottomNavBar(
                    currentScreen = currentScreen,
                    onScreenSelected = { viewModel.navigateTo(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                showOnboarding || currentScreen == KaizenScreen.ONBOARDING -> {
                    OnboardingScreen(
                        onComplete = { name, tz, goal, starters ->
                            viewModel.finishOnboarding(name, tz, goal, starters)
                        }
                    )
                }
                currentScreen == KaizenScreen.COMMAND_DECK -> {
                    CommandDeckScreen(
                        profile = userProfile,
                        stats = statValues,
                        quests = quests,
                        completions = completions,
                        challenges = challenges,
                        recentEvents = xpEvents,
                        onCompleteQuest = { viewModel.completeQuest(it) },
                        onNavigate = { viewModel.navigateTo(it) },
                        onStartFocus = { viewModel.navigateTo(KaizenScreen.FOCUS_ROOM) }
                    )
                }
                currentScreen == KaizenScreen.QUESTS -> {
                    QuestsScreen(
                        quests = quests,
                        completions = completions,
                        currentFilter = questFilter,
                        userTimezone = userProfile?.timezone ?: "UTC",
                        onFilterChange = { viewModel.setQuestFilter(it) },
                        onCompleteQuest = { viewModel.completeQuest(it) },
                        onCreateQuest = { t, d, cat, cad, xp, dur ->
                            viewModel.createQuest(t, d, cat, cad, xp, dur)
                        },
                        onArchiveQuest = { id, arc -> viewModel.archiveQuest(id, arc) },
                        onDeleteQuest = { id -> viewModel.deleteQuest(id) }
                    )
                }
                currentScreen == KaizenScreen.FOCUS_ROOM -> {
                    FocusRoomScreen(
                        activeSession = activeFocusSession,
                        remainingSeconds = focusRemaining,
                        history = focusSessions,
                        onStartWork = { viewModel.startFocusSession(25, isBreak = false) },
                        onStartBreak = { viewModel.startFocusSession(5, isBreak = true) },
                        onTogglePause = { viewModel.togglePauseFocusSession() },
                        onCancel = { viewModel.cancelFocusSession() },
                        onManualComplete = { viewModel.completeFocusSession() }
                    )
                }
                currentScreen == KaizenScreen.CHALLENGES -> {
                    ChallengesScreen(
                        challenges = challenges,
                        userTimezone = userProfile?.timezone ?: "UTC"
                    )
                }
                currentScreen == KaizenScreen.JOURNEY -> {
                    JourneyScreen(
                        profile = userProfile,
                        stats = statValues,
                        xpEvents = xpEvents,
                        completions = completions
                    )
                }
                currentScreen == KaizenScreen.VAULT -> {
                    VaultScreen(
                        profile = userProfile,
                        rewards = rewards,
                        redemptions = redemptions,
                        onCreateReward = { t, d, c -> viewModel.createReward(t, d, c) },
                        onArchiveReward = { id, arc -> viewModel.archiveReward(id, arc) },
                        onRedeemReward = { id -> viewModel.redeemReward(id) }
                    )
                }
                currentScreen == KaizenScreen.SETTINGS -> {
                    SettingsScreen(
                        profile = userProfile,
                        allProfiles = allProfiles,
                        currentUserId = currentUserId,
                        onSwitchUser = { viewModel.switchUser(it) },
                        onUpdateSettings = { n, tz, rm -> viewModel.updateSettings(n, tz, rm) },
                        onDeleteAccount = { viewModel.deleteAccount() },
                        onExportData = {
                            // Fetch JSON string
                            "{\"user\": \"$currentUserId\", \"level\": ${userProfile?.level ?: 1}, \"totalXp\": ${userProfile?.totalXp ?: 0}}"
                        }
                    )
                }
            }

            // Level Up Celebration Modal
            if (levelUpState.isVisible) {
                LevelUpCelebrationModal(
                    level = levelUpState.level,
                    rank = levelUpState.rank,
                    isRankPromotion = levelUpState.isRankPromotion,
                    onDismiss = { viewModel.dismissLevelUp() }
                )
            }
        }
    }
}

@Composable
private fun KaizenBottomNavBar(
    currentScreen: KaizenScreen,
    onScreenSelected: (KaizenScreen) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = AstralDarkCard,
        tonalElevation = 8.dp
    ) {
        val navItems = listOf(
            Triple(KaizenScreen.COMMAND_DECK, "Deck", Icons.Default.Dashboard),
            Triple(KaizenScreen.QUESTS, "Quests", Icons.Default.Assignment),
            Triple(KaizenScreen.FOCUS_ROOM, "Focus", Icons.Default.Timer),
            Triple(KaizenScreen.CHALLENGES, "Trials", Icons.Default.Stars),
            Triple(KaizenScreen.JOURNEY, "Journey", Icons.Default.Timeline),
            Triple(KaizenScreen.VAULT, "Vault", Icons.Default.Lock),
            Triple(KaizenScreen.SETTINGS, "Settings", Icons.Default.Settings)
        )

        navItems.forEach { (screen, label, icon) ->
            val isSelected = currentScreen == screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onScreenSelected(screen) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AuroraTeal,
                    selectedTextColor = AuroraTeal,
                    unselectedIconColor = AstralTextMuted,
                    unselectedTextColor = AstralTextMuted,
                    indicatorColor = AstralDarkSurfaceVariant
                ),
                modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
            )
        }
    }
}
