package com.dev.tasker.commons.data.local

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import io.reactivex.Flowable


@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(task: Task)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(tasks: List<Task>)

    @Query("DELETE FROM task WHERE taskId = :taskId")
    fun delete(taskId: Long)

    @Query("SELECT * FROM task")
    fun getAll(): Flowable<List<Task>>

    @Query("SELECT * FROM task WHERE task.taskId LIKE :taskId")
    fun getTask(taskId: Long): Flowable<Task>

    @Query("UPDATE task SET started = :start, finished = :finish, postponed = :postpone WHERE taskId = :taskId")
    fun updateTaskStatus(taskId: Long, start: Boolean, finish: Boolean, postpone: Boolean)

    @Query("UPDATE task SET spendingTime = :spendTime WHERE taskId = :taskId")
    fun updateTaskSpendingTime(taskId: Long, spendTime: Long)

    @Query("UPDATE task SET startedTime = :startTime WHERE taskId = :taskId")
    fun updateTaskStartTime(taskId: Long, startTime: Long)

    @Query("UPDATE task SET name = :name, description = :description, filePath = :filePath, keywords= :keywords WHERE taskId = :taskId")
    fun updateTask(taskId: Long, name: String, description: String, filePath: String, keywords: String)

}