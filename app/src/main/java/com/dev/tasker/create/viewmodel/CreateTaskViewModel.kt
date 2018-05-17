package com.dev.tasker.create.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import com.dev.tasker.commons.TaskDH
import com.dev.tasker.commons.data.local.Task
import com.dev.tasker.core.extensions.toLiveData
import com.dev.tasker.core.networking.Outcome
import com.dev.tasker.create.model.TaskDataContract
import io.reactivex.disposables.CompositeDisposable

class CreateTaskViewModel(private val repo: TaskDataContract.Repository,
                          private val compositeDisposable: CompositeDisposable) : ViewModel() {

    var editTask: Task? = null
    var uploadFilePath: String? = null

    val createTaskOutcome: LiveData<Outcome<Long>> by lazy {
        repo.saveTaskOutcome.toLiveData(compositeDisposable)
    }

    fun isEditMode(): Boolean {
        return editTask != null
    }

    fun saveTask(name: String, description: String, keywords: String) {
        val imagePath = uploadFilePath
        if (isEditMode()) {
            updateTask(editTask?.taskId!!, name, description, imagePath ?: "", keywords)
        } else {
            repo.createTask(name, description, imagePath ?: "", keywords)
        }
    }

    private fun updateTask(taskId: Long, name: String, description: String, imagePath: String, keywords: String) {
        repo.updateTask(taskId, name, description, imagePath, keywords)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
        TaskDH.destroyDetailsComponent()
    }
}