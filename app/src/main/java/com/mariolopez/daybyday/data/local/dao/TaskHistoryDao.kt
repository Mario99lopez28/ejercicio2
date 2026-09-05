package com.mariolopez.daybyday.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.mariolopez.daybyday.data.local.entity.TaskHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskHistoryDao {

    @Query("SELECT * FROM task_history WHERE taskId = :taskId ORDER BY timestamp ASC")
    fun observeHistoryForTask(taskId: Long): Flow<List<TaskHistoryEntity>>

    @Query("SELECT * FROM task_history ORDER BY timestamp DESC")
    fun observeAllHistory(): Flow<List<TaskHistoryEntity>>

    @Insert
    suspend fun insert(entry: TaskHistoryEntity)
}
