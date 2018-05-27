package com.dev.tasker.create.model

import com.dev.tasker.commons.data.local.Task
import com.dev.tasker.core.networking.Outcome
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject

interface TaskDataContract {
    interface Repository {
        val saveTaskOutcome: PublishSubject<Outcome<Long>>
        fun createTask(name: String, description: String, imagePath: String, keywords: String)
        fun updateTask(taskId: Long, name: String, description: String, imagePath: String, keywords: String)
        fun handleError(error: Throwable)
        fun setTags(tags: ArrayList<String>)
        fun getTags(): ArrayList<String>
    }

    interface Local {
        fun getTask(taskId: Long): Single<Task>
        fun saveTask(task: Task)
        fun updateTask(taskId: Long, name: String, description: String, imagePath: String, keywords: String)
        fun setTags(tags: ArrayList<String>)
        fun getTags(): ArrayList<String>
    }
}