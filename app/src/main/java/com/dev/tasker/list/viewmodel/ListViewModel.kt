package com.dev.tasker.list.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import com.dev.tasker.commons.TaskDH
import com.dev.tasker.commons.data.local.Task
import com.dev.tasker.core.extensions.toLiveData
import com.dev.tasker.core.networking.Outcome
import com.dev.tasker.list.model.ListDataContract
import io.reactivex.disposables.CompositeDisposable

class ListViewModel(private val repo: ListDataContract.Repository,
                    private val compositeDisposable: CompositeDisposable) : ViewModel() {

    val pendingTasksOutcome: LiveData<Outcome<List<Task>>> by lazy {
        repo.pendingTasksFetchOutcome.toLiveData(compositeDisposable)
    }

    val finishTasksOutcome: LiveData<Outcome<List<Task>>> by lazy {
        repo.finishTasksFetchOutcome.toLiveData(compositeDisposable)
    }

    val currentTasksOutcome: LiveData<Outcome<List<Task>>> by lazy {
        repo.currentTasksFetchOutcome.toLiveData(compositeDisposable)
    }

    fun getTasks(finished: Boolean, started: Boolean) {
        repo.fetchTasks(finished, started)
    }

    fun startTask(task: Task) {
        repo.startTaskImmediately(task)
    }

    fun startTaskPostpone(task: Task) {
        repo.postponeTask(task)
    }

    fun revertTask(task: Task) {
        repo.revertTask(task)
    }

    fun stopTask(task: Task) {
        repo.stopTask(task)
    }

    fun finishTask(task: Task) {
        repo.finishTask(task)
    }

    fun removeTask(task: Task) {
        repo.removeTask(task)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
        TaskDH.destroyListComponent()
    }
}