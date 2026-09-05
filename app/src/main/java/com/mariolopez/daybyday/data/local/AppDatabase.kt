package com.mariolopez.daybyday.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mariolopez.daybyday.data.local.dao.TaskDao
import com.mariolopez.daybyday.data.local.dao.TaskHistoryDao
import com.mariolopez.daybyday.data.local.entity.TaskEntity
import com.mariolopez.daybyday.data.local.entity.TaskHistoryEntity

@Database(
    entities = [TaskEntity::class, TaskHistoryEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun taskHistoryDao(): TaskHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "daybyday.db"
                ).build().also { INSTANCE = it }
            }
    }
}
