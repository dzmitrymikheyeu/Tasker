package com.dev.tasker.create.model

import android.text.TextUtils
import com.dev.tasker.commons.data.local.Task
import com.dev.tasker.core.extensions.failed
import com.dev.tasker.core.extensions.success
import com.dev.tasker.core.networking.Outcome
import com.dev.tasker.create.exceptions.TaskCreateException
import io.reactivex.subjects.PublishSubject
import java.util.*

class TaskRepository(
        private val local: TaskDataContract.Local)
    : TaskDataContract.Repository {

    override val saveTaskOutcome: PublishSubject<Outcome<Long>> =
            PublishSubject.create<Outcome<Long>>()

    override fun createTask(name: String, description: String,
                            imagePath: String,
                            keywords: String) {
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(description)) {
            saveTaskOutcome.failed(TaskCreateException.EmptyFieldsException())
        } else {
            val date = Date()
            val task = Task(
                    name = name,
                    description = description,
                    imagePath = imagePath,
                    keywords = keywords,
                    taskId = Date().time)

            local.saveTask(task)
            saveTaskOutcome.success(date.time)
        }
    }

    override fun updateTask(taskId: Long, name: String, description: String, imagePath: String, keywords: String) {
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(description)) {
            saveTaskOutcome.failed(TaskCreateException.EmptyFieldsException())
        } else {
            local.updateTask(taskId, name, description, imagePath, keywords)

            saveTaskOutcome.success(taskId)
        }
    }

    override fun setTags(tags: ArrayList<String>) {
        local.setTags(tags)
    }

    override fun getTags(): ArrayList<String> {
        return local.getTags()
    }

    override fun handleError(error: Throwable) {
        saveTaskOutcome.failed(error)
    }
}