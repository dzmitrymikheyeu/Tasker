package com.dev.tasker.service.model

import com.dev.tasker.commons.data.local.Task
import com.dev.tasker.core.extensions.addTo
import com.dev.tasker.core.extensions.failed
import com.dev.tasker.core.extensions.performOnBackOutOnMain
import com.dev.tasker.core.extensions.success
import com.dev.tasker.core.networking.Outcome
import com.dev.tasker.core.networking.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject


class ServiceRepository(private val local: ServiceDataContract.Local,
                        private val scheduler: Scheduler,
                        private val compositeDisposable: CompositeDisposable) : ServiceDataContract.Repository {

    override val tasksFetchOutcome: PublishSubject<Outcome<List<Task>>> = PublishSubject.create<Outcome<List<Task>>>()

    override fun finishTask(task: Task) {
        local.finishTask(task)
    }

    override fun stopTask(task: Task) {
        local.stopTask(task)
    }

    override fun startTask(taskId: Long) {
        local.startTask(taskId)
    }

    override fun fetchTasks() {
        local.getTasks()
                .performOnBackOutOnMain(scheduler)
                .subscribe({ results ->
                    run {
                        tasksFetchOutcome.success(results)
                    }
                }, { error -> handleError(error) })
                .addTo(compositeDisposable)
    }

    override fun handleError(error: Throwable) {
        tasksFetchOutcome.failed(error)
    }

}