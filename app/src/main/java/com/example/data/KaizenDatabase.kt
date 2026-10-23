package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.KaizenDao
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

@Database(
    entities = [
        UserProfile::class,
        StatValue::class,
        Quest::class,
        QuestCompletion::class,
        XpEvent::class,
        FocusSessionRecord::class,
        ActiveFocusSession::class,
        ChallengeRecord::class,
        RewardItem::class,
        RewardRedemption::class
    ],
    version = 1,
    exportSchema = false
)
abstract class KaizenDatabase : RoomDatabase() {
    abstract fun kaizenDao(): KaizenDao

    companion object {
        @Volatile
        private var INSTANCE: KaizenDatabase? = null

        fun getDatabase(context: Context): KaizenDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KaizenDatabase::class.java,
                    "kaizen_astral.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
