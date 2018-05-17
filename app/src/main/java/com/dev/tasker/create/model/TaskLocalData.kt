package com.dev.tasker.create.model

import PreferenceHelper
import android.content.SharedPreferences
import com.dev.tasker.commons.data.local.Task
import com.dev.tasker.commons.data.local.TaskDb
import com.dev.tasker.core.extensions.performOnBack
import com.dev.tasker.core.networking.Scheduler
import get
import io.reactivex.Completable
import io.reactivex.Flowable
import set

class TaskLocalData(private val preferences: SharedPreferences,
                    private val taskDb: TaskDb,
                    private val scheduler: Scheduler) : TaskDataContract.Local {
    override fun getTask(taskId: Long) : Flowable<Task> {
       return taskDb.taskDao().getTask(taskId)
    }

    override fun updateTask(taskId: Long, name: String, description: String,
                            imagePath: String, keywords: String) {
        Completable.fromAction({
            taskDb.taskDao().updateTask(taskId, name, description, imagePath, keywords)
        })
                .performOnBack(scheduler)
                .subscribe()
    }

    override fun saveTask(task: Task) {
        Completable.fromAction({
            taskDb.taskDao().upsert(task)
        })
                .performOnBack(scheduler)
                .subscribe()
    }

    override fun getTags(): ArrayList<String> {
        val tagsAsString: String? = preferences[PreferenceHelper.PREFS_TAGS, ""]
        return ArrayList(tagsAsString?.split(","))
    }

    override fun setTags(tags: ArrayList<String>) {
        preferences[PreferenceHelper.PREFS_TAGS] = tags.joinToString()
    }
}


