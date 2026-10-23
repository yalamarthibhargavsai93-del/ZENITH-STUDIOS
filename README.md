# Kaizen — Astral Field Journal of Earned Growth

**Kaizen** is an original self-improvement progression application. Users complete real-life quests for workouts, studying, habits, life tasks, and focus sessions; gain Momentum XP; cultivate core stats; advance through astral ranks; complete concord trials; and redeem self-chosen rewards from their Vault.

---

## 🌌 Astral Field Journal Aesthetics
- **Dark Space Canvas**: `#090D14` and `#111724` obsidian surfaces with starlight field canvas and constellation connections.
- **Accents**:
  - **Aurora Teal** (`#2DD4BF`): Primary / Vitality / Momentum
  - **Astral Indigo** (`#818CF8`): Secondary / Focus / Deep Space
  - **Cosmic Violet** (`#C084FC`): Tertiary / Knowledge / Rank Ascension
  - **Solar Amber** (`#FBBF24`): Currency / Sparks / Resolve
- **Philosophy**: Momentum XP and Astral Ranks represent progress inside Kaizen, never personal human worth. Missed days are never penalized.

---

## 📈 Progression Engine & Core Rules
1. **Level Formula**:
   - Level 1 starts at 0 XP.
   - XP required for the next level: `100 + 25 * (current_level - 1)`.
   - Multi-level thresholds are resolved atomically within a single transaction.
2. **Astral Ranks**:
   - **Seed**: Levels 1–4
   - **Wayfinder**: Levels 5–9
   - **Vanguard**: Levels 10–19
   - **Luminary**: Levels 20–34
   - **Paragon**: Levels 35–49
   - **Apex**: Level 50+
3. **Core Disciplines (Stats)**:
   - **Workout**: +2 Vitality
   - **Study**: +2 Knowledge
   - **Habit / Life**: +1 Resolve
   - **Focus**: +2 Focus
4. **Daily Momentum XP Cap**:
   - Quests and focus sessions cap at **300 XP per local day**.
   - Challenge bonuses bypass this cap.
5. **Rhythm (Consecutive Active Days)**:
   - A day is active if ≥ 1 qualifying quest or 25m focus session is completed.
   - Missed days reset current rhythm without deducting XP, ranks, or stats.
6. **Sparks & The Vault**:
   - Sparks are earned via full 25m focus blocks (+1) and challenges (+2 to +5).
   - Vault rewards deduct Sparks atomically and reject any attempt at negative balances.
   - Absolutely zero gambling, loot boxes, or real-money transactions.

---

## 📱 App Navigation & Screens
1. **Arrival / Onboarding (`OnboardingScreen`)**:
   - Display name, timezone configuration, personal focus goal.
   - Generates 3 starter keystones (Movement, Study, Habit).
   - Philosophical grounding note.
2. **Command Deck (`CommandDeckScreen`)**:
   - Greeting, Rank badge, Level, Momentum XP progress bar, Sparks, Rhythm count.
   - Today's Quests with one-tap completion controls.
   - Daily Concord trial card with live progress.
   - One-tap "Start Focus Chamber" action.
   - 4 compact discipline cards (Vitality, Focus, Knowledge, Resolve).
   - Recent Field Journal audit feed.
3. **Quests (`QuestsScreen`)**:
   - Filters: Today, Active, Completed, Archived.
   - Quest forger dialog: Title, description, category, cadence, XP slider (5–75 XP), target duration.
   - Cadence enforcement: Daily (once per local day), Weekly (once per ISO week), Once.
4. **Focus Room (`FocusRoomScreen`)**:
   - Calm Pomodoro interface: 25m work session, 5m restorative break.
   - Circular glowing arc timer canvas.
   - Timer persistence in local database: app backgrounding or restart retains state.
   - Awards 25 XP, +2 Focus, and 1 Spark only on full completion.
5. **Trials (`ChallengesScreen`)**:
   - Daily Concord: Complete 2 qualifying actions today (40 XP, 2 Sparks).
   - Weekly Residency:
     - 3 Vitality actions (80 XP, 5 Sparks)
     - 120 minutes of study/focus (80 XP, 5 Sparks)
     - Active on 5 different days (80 XP, 5 Sparks)
6. **Journey (`JourneyScreen`)**:
   - Accessible 7-day velocity chart drawn via Compose Canvas.
   - Stat constellation breakdown and percentage meters.
   - Astral milestone achievements.
7. **Vault (`VaultScreen`)**:
   - Available Sparks balance header.
   - Custom real-life reward forge ("Watch a movie", "Buy an espresso", "1h Gaming").
   - Atomic redemption with transaction log.
8. **Settings & System (`SettingsScreen`)**:
   - Display name, timezone, reduced-motion switch.
   - Multi-user account switcher to verify strict data isolation between profiles.
   - Full JSON data export.
   - Clean slate account reset.
   - Explicitly labeled "Planned" integration cards (Health Connect, Strava, Notion, Friends, Leaderboards).

---

## 🧪 Testing Instructions
Run local JVM unit and Robolectric tests via Gradle:
```bash
gradle :app:testDebugUnitTest
```
Tests cover:
- Level & XP progression formulas across multiple thresholds.
- Rank classification boundaries.
- Daily 300 XP cap enforcement.
- Stat award mappings.
- Rhythm calculation and non-punitive missed-day behavior.
- Period key generation per cadence.
- Reward balance atomic validation.

---

## 🚀 Phase 2 Integration-Ready Plan
- **Health Connect**: Read exercise sessions to automatically complete movement quests.
- **Strava**: Webhook / OAuth integration for outdoor runs and cycling.
- **Notion**: Direct sync of daily field journal logs to user Notion databases.
- **Astral Cohorts**: Cloud-synced opt-in accountability circles with end-to-end privacy.
