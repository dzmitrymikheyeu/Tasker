package com.dev.tasker.list.model

import com.dev.tasker.commons.data.local.Task
import com.dev.tasker.core.extensions.addTo
import com.dev.tasker.core.extensions.failed
import com.dev.tasker.core.extensions.performOnBackOutOnMain
import com.dev.tasker.core.extensions.success
import com.dev.tasker.core.networking.Outcome
import com.dev.tasker.core.networking.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject


class ListRepository(private val local: ListDataContract.Local,
                     private val scheduler: Scheduler,
                     private val compositeDisposable: CompositeDisposable) : ListDataContract.Repository {

    override val currentTasksFetchOutcome: PublishSubject<Outcome<List<Task>>>
            = PublishSubject.create<Outcome<List<Task>>>()
    override val pendingTasksFetchOutcome: PublishSubject<Outcome<List<Task>>>
            = PublishSubject.create<Outcome<List<Task>>>()
    override val finishTasksFetchOutcome: PublishSubject<Outcome<List<Task>>>
            = PublishSubject.create<Outcome<List<Task>>>()

    override fun removeTask(task: Task) {
        local.removeTask(task)
    }

    override fun finishTask(task: Task) {
        local.finishTask(task)
    }

    override fun startTaskImmediately(task: Task) {
        local.startTask(task)
    }

    override fun stopTask(task: Task) {
        local.stopTask(task)
    }

    override fun postponeTask(task: Task) {
        local.postponeTask(task)
    }

    override fun revertTask(task: Task) {
        local.revertTask(task)
    }

    override fun startTask(taskId: Long) {
        local.startTask(taskId)
    }

    override fun fetchTasks(finished: Boolean, started: Boolean) {
        local.getTasks()
                .performOnBackOutOnMain(scheduler)
                .subscribe({ results ->
                    run {
                        if (!finished && !started) {
                            pendingTasksFetchOutcome.success(results.filter {
                                !it.finished && !it.started && !it.postponed })
                        } else if (finished && !started) {
                            finishTasksFetchOutcome.success(results.filter { it.finished })
                        } else if(!finished && started) {
                            currentTasksFetchOutcome.success(results.filter { it.started })
                        }
                    }
                }, { error -> handleError(error) })
                .addTo(compositeDisposable)
    }

    override fun handleError(error: Throwable) {
        pendingTasksFetchOutcome.failed(error)
    }

}