package com.mariolopez.daybyday.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mariolopez.daybyday.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE currentDate = :date ORDER BY currentTime ASC")
    fun observeTasksForDate(date: LocalDate): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE currentDate BETWEEN :start AND :end")
    fun observeTasksBetween(start: LocalDate, end: LocalDate): Flow<List<TaskEntity>>

    @Query("SELECT DISTINCT currentDate FROM tasks WHERE currentDate BETWEEN :start AND :end")
    fun observeDatesWithTasks(start: LocalDate, end: LocalDate): Flow<List<LocalDate>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Long): TaskEntity?

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun observeById(id: Long): Flow<TaskEntity?>

    @Query("SELECT * FROM tasks WHERE status = 'PENDING'")
    suspend fun getAllPending(): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)
}
