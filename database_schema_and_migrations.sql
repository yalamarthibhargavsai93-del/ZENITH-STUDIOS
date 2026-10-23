-- Kaizen Database Schema, RLS Policies, and Atomic Server-Side Functions
-- Compatible with Postgres / Supabase & Room Local Store

-- 1. Profiles Table
CREATE TABLE IF NOT EXISTS public.profiles (
    id TEXT PRIMARY KEY,
    display_name TEXT NOT NULL DEFAULT 'Wayfinder',
    email TEXT,
    timezone TEXT NOT NULL DEFAULT 'UTC',
    main_goal TEXT NOT NULL DEFAULT 'Cultivate daily clarity and physical resilience',
    total_xp INT NOT NULL DEFAULT 0,
    level INT NOT NULL DEFAULT 1,
    xp_in_current_level INT NOT NULL DEFAULT 0,
    sparks INT NOT NULL DEFAULT 0,
    current_rhythm INT NOT NULL DEFAULT 0,
    longest_rhythm INT NOT NULL DEFAULT 0,
    last_active_date TEXT,
    is_onboarded BOOLEAN NOT NULL DEFAULT FALSE,
    reduced_motion BOOLEAN NOT NULL DEFAULT FALSE,
    created_at BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000)::BIGINT
);

ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can only read their own profile"
    ON public.profiles FOR SELECT
    USING (auth.uid()::text = id);

CREATE POLICY "Users can only update their own profile"
    ON public.profiles FOR UPDATE
    USING (auth.uid()::text = id);

-- 2. Stat Values Table
CREATE TABLE IF NOT EXISTS public.stat_values (
    user_id TEXT NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    stat_name TEXT NOT NULL,
    value INT NOT NULL DEFAULT 0,
    updated_at BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000)::BIGINT,
    PRIMARY KEY (user_id, stat_name)
);

ALTER TABLE public.stat_values ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users access own stats"
    ON public.stat_values FOR ALL
    USING (auth.uid()::text = user_id);

-- 3. Quests Table
CREATE TABLE IF NOT EXISTS public.quests (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    description TEXT DEFAULT '',
    category TEXT NOT NULL CHECK (category IN ('WORKOUT', 'STUDY', 'HABIT', 'FOCUS', 'LIFE')),
    cadence TEXT NOT NULL CHECK (cadence IN ('DAILY', 'WEEKLY', 'ONCE')),
    xp_reward INT NOT NULL CHECK (xp_reward BETWEEN 5 AND 75),
    target_duration_minutes INT NOT NULL DEFAULT 0,
    scheduled_day TEXT NOT NULL DEFAULT 'ALL',
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_at BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000)::BIGINT
);

ALTER TABLE public.quests ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users manage own quests"
    ON public.quests FOR ALL
    USING (auth.uid()::text = user_id);

-- 4. Quest Completions (Idempotency Keyed)
CREATE TABLE IF NOT EXISTS public.quest_completions (
    id TEXT PRIMARY KEY, -- qc_${user_id}_${quest_id}_${period_key}
    user_id TEXT NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    quest_id TEXT NOT NULL REFERENCES public.quests(id) ON DELETE CASCADE,
    local_date TEXT NOT NULL,
    period_key TEXT NOT NULL,
    xp_awarded INT NOT NULL,
    completed_at BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000)::BIGINT,
    CONSTRAINT uq_completion_period UNIQUE (user_id, quest_id, period_key)
);

ALTER TABLE public.quest_completions ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users read and insert own completions"
    ON public.quest_completions FOR ALL
    USING (auth.uid()::text = user_id);

-- 5. XP Events (Immutable Audit Log)
CREATE TABLE IF NOT EXISTS public.xp_events (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    event_type TEXT NOT NULL,
    xp_delta INT NOT NULL,
    spark_delta INT NOT NULL DEFAULT 0,
    reason TEXT NOT NULL,
    local_date TEXT NOT NULL,
    timestamp BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000)::BIGINT
);

ALTER TABLE public.xp_events ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users read own xp events"
    ON public.xp_events FOR SELECT
    USING (auth.uid()::text = user_id);

-- 6. Focus Sessions Table
CREATE TABLE IF NOT EXISTS public.focus_sessions (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    started_at BIGINT NOT NULL,
    ended_at BIGINT,
    planned_duration_seconds INT NOT NULL DEFAULT 1500,
    actual_duration_seconds INT NOT NULL DEFAULT 0,
    status TEXT NOT NULL CHECK (status IN ('ACTIVE', 'COMPLETED', 'CANCELLED')),
    xp_awarded INT NOT NULL DEFAULT 0,
    sparks_awarded INT NOT NULL DEFAULT 0,
    local_date TEXT NOT NULL
);

ALTER TABLE public.focus_sessions ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users manage own focus sessions"
    ON public.focus_sessions FOR ALL
    USING (auth.uid()::text = user_id);

-- 7. Challenges Table
CREATE TABLE IF NOT EXISTS public.challenges (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    challenge_type TEXT NOT NULL,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    target_value INT NOT NULL,
    current_value INT NOT NULL DEFAULT 0,
    period_key TEXT NOT NULL,
    xp_reward INT NOT NULL,
    spark_reward INT NOT NULL,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at BIGINT
);

ALTER TABLE public.challenges ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users access own challenges"
    ON public.challenges FOR ALL
    USING (auth.uid()::text = user_id);

-- 8. Vault Rewards Table
CREATE TABLE IF NOT EXISTS public.rewards (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    description TEXT DEFAULT '',
    spark_cost INT NOT NULL CHECK (spark_cost >= 1),
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_at BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000)::BIGINT
);

ALTER TABLE public.rewards ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users manage own rewards"
    ON public.rewards FOR ALL
    USING (auth.uid()::text = user_id);

-- 9. Reward Redemptions Table
CREATE TABLE IF NOT EXISTS public.reward_redemptions (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    reward_id TEXT NOT NULL REFERENCES public.rewards(id) ON DELETE CASCADE,
    reward_title TEXT NOT NULL,
    spark_cost INT NOT NULL,
    redeemed_at BIGINT NOT NULL DEFAULT (extract(epoch from now()) * 1000)::BIGINT
);

ALTER TABLE public.reward_redemptions ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users view own redemptions"
    ON public.reward_redemptions FOR ALL
    USING (auth.uid()::text = user_id);

-- Atomic Server-Side Function: Complete Quest
CREATE OR REPLACE FUNCTION complete_quest_atomic(
    p_user_id TEXT,
    p_quest_id TEXT,
    p_period_key TEXT,
    p_local_date TEXT
) RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_quest RECORD;
    v_profile RECORD;
    v_today_xp INT;
    v_xp_to_award INT;
    v_completion_id TEXT;
    v_new_total_xp INT;
    v_level INT := 1;
    v_rem_xp INT;
    v_needed INT;
    v_stat_name TEXT;
    v_stat_delta INT;
BEGIN
    SELECT * INTO v_quest FROM public.quests WHERE id = p_quest_id AND user_id = p_user_id;
    IF NOT FOUND THEN
        RETURN jsonb_build_object('success', false, 'error', 'Quest not found');
    END IF;

    v_completion_id := 'qc_' || p_user_id || '_' || p_quest_id || '_' || p_period_key;
    IF EXISTS (SELECT 1 FROM public.quest_completions WHERE id = v_completion_id) THEN
        RETURN jsonb_build_object('success', false, 'error', 'Already completed in this period');
    END IF;

    -- Daily 300 XP cap for quest & focus events
    SELECT COALESCE(SUM(xp_delta), 0) INTO v_today_xp
    FROM public.xp_events
    WHERE user_id = p_user_id AND local_date = p_local_date AND event_type IN ('QUEST_COMPLETION', 'FOCUS_SESSION');

    v_xp_to_award := LEAST(v_quest.xp_reward, GREATEST(0, 300 - v_today_xp));

    INSERT INTO public.quest_completions (id, user_id, quest_id, local_date, period_key, xp_awarded)
    VALUES (v_completion_id, p_user_id, p_quest_id, p_local_date, p_period_key, v_xp_to_award);

    INSERT INTO public.xp_events (id, user_id, event_type, xp_delta, spark_delta, reason, local_date)
    VALUES ('xpe_' || v_completion_id, p_user_id, 'QUEST_COMPLETION', v_xp_to_award, 0, 'Completed quest: ' || v_quest.title, p_local_date);

    -- Stat bonus
    IF v_quest.category = 'WORKOUT' THEN v_stat_name := 'VITALITY'; v_stat_delta := 2;
    ELSIF v_quest.category = 'STUDY' THEN v_stat_name := 'KNOWLEDGE'; v_stat_delta := 2;
    ELSIF v_quest.category = 'FOCUS' THEN v_stat_name := 'FOCUS'; v_stat_delta := 2;
    ELSE v_stat_name := 'RESOLVE'; v_stat_delta := 1;
    END IF;

    INSERT INTO public.stat_values (user_id, stat_name, value)
    VALUES (p_user_id, v_stat_name, v_stat_delta)
    ON CONFLICT (user_id, stat_name) DO UPDATE
    SET value = public.stat_values.value + v_stat_delta, updated_at = (extract(epoch from now()) * 1000)::BIGINT;

    -- Level calculation
    SELECT * INTO v_profile FROM public.profiles WHERE id = p_user_id;
    v_new_total_xp := v_profile.total_xp + v_xp_to_award;
    v_rem_xp := v_new_total_xp;
    LOOP
        v_needed := 100 + 25 * (v_level - 1);
        EXIT WHEN v_rem_xp < v_needed;
        v_rem_xp := v_rem_xp - v_needed;
        v_level := v_level + 1;
    END LOOP;

    UPDATE public.profiles
    SET total_xp = v_new_total_xp,
        level = v_level,
        xp_in_current_level = v_rem_xp,
        last_active_date = p_local_date
    WHERE id = p_user_id;

    RETURN jsonb_build_object(
        'success', true,
        'xp_awarded', v_xp_to_award,
        'new_level', v_level,
        'stat_awarded', v_stat_name
    );
END;
$$;
