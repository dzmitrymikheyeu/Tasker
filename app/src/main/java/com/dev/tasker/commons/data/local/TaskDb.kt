package com.dev.tasker.commons.data.local

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

@Database(entities = [Task::class], version = 1, exportSchema = false)
abstract class TaskDb : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}
