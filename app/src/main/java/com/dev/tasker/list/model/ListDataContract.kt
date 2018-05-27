package com.dev.tasker.list.model

import com.dev.tasker.commons.data.local.Task
import com.dev.tasker.core.networking.Outcome
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject

interface ListDataContract {
    interface Repository {
        val pendingTasksFetchOutcome: PublishSubject<Outcome<List<Task>>>
        val currentTasksFetchOutcome: PublishSubject<Outcome<List<Task>>>
        val finishTasksFetchOutcome: PublishSubject<Outcome<List<Task>>>
        fun fetchTasks(finished: Boolean, started: Boolean)
        fun removeTask(task: Task)
        fun startTaskImmediately(task: Task)
        fun finishTask(task: Task)
        fun stopTask(task: Task)
        fun postponeTask(task: Task)
        fun revertTask(task: Task)
        fun startTask(taskId: Long)
        fun handleError(error: Throwable)
    }

    interface Local {
        fun getTask(taskId: Long): Single<Task>
        fun getTasks(): Flowable<List<Task>>
        fun removeTask(task: Task)
        fun stopTask(task: Task)
        fun revertTask(task: Task)
        fun startTask(task: Task)
        fun startTask(taskId: Long)
        fun finishTask(task: Task)
        fun postponeTask(task: Task)
    }
}