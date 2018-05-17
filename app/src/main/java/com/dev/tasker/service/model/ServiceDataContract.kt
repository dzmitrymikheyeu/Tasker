package com.dev.tasker.service.model

import com.dev.tasker.commons.data.local.Task
import com.dev.tasker.core.networking.Outcome
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject

interface ServiceDataContract {
    interface Repository {
        val tasksFetchOutcome: PublishSubject<Outcome<List<Task>>>
        fun fetchTasks()
        fun finishTask(task: Task)
        fun stopTask(task: Task)
        fun startTask(taskId: Long)
        fun handleError(error: Throwable)
    }

    interface Local {
        fun getTasks(): Flowable<List<Task>>
        fun stopTask(task: Task)
        fun startTask(taskId: Long)
        fun finishTask(task: Task)
    }
}