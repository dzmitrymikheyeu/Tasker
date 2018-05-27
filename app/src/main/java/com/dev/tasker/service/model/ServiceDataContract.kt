package com.dev.tasker.service.model

import com.dev.tasker.commons.data.local.Task
import com.dev.tasker.core.networking.Outcome
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject

interface ServiceDataContract {
    interface Repository {
        val tasksFetchOutcome: PublishSubject<Outcome<List<Task>>>
        val taskOutcome: PublishSubject<Outcome<Task>>
        fun getTask(taskId: Long)
        fun fetchTasks()
        fun finishTask(task: Task)
        fun stopTask(task: Task)
        fun startTask(taskId: Long)
        fun handleError(error: Throwable)
    }

    interface Local {
        fun getTasks(): Flowable<List<Task>>
        fun getTask(taskId: Long): Single<Task>
        fun stopTask(task: Task)
        fun startTask(taskId: Long)
        fun finishTask(task: Task)
    }
}