package com.dev.tasker.list.model

import com.dev.tasker.commons.data.local.Task
import com.dev.tasker.commons.data.local.TaskDb
import com.dev.tasker.core.extensions.performOnBack
import com.dev.tasker.core.extensions.performOnBackOutOnMain
import com.dev.tasker.core.networking.Scheduler
import io.reactivex.Completable
import io.reactivex.Flowable

class ListLocalData(private val taskDb: TaskDb, private val scheduler: Scheduler) : ListDataContract.Local {

    override fun getTask(taskId: Long): Flowable<Task> {
        return taskDb.taskDao().getTask(taskId)
    }

    override fun postponeTask(task: Task) {
        Completable.fromAction({
            taskDb.taskDao().updateTaskStatus(task.taskId, false, false, true)
        })
                .performOnBack(scheduler)
                .subscribe()
    }

    override fun stopTask(task: Task) {
        Completable.fromAction({
            val spendingTime = (System.currentTimeMillis() - task.startedTime) + task.spendingTime
            taskDb.taskDao().updateTaskStatus(task.taskId, false, false, false)
            taskDb.taskDao().updateTaskSpendingTime(task.taskId, spendingTime)
        })
                .performOnBackOutOnMain(scheduler)
                .subscribe()
    }

    override fun revertTask(task: Task) {
        Completable.fromAction({
            taskDb.taskDao().updateTaskStatus(task.taskId, false, false, false)
        })
                .performOnBackOutOnMain(scheduler)
                .subscribe()
    }

    override fun removeTask(task: Task) {
        Completable.fromAction({
            taskDb.taskDao().delete(task.taskId)
        })
                .performOnBack(scheduler)
                .subscribe()
    }

    override fun startTask(task: Task) {
        Completable.fromAction({
            taskDb.taskDao().updateTaskStatus(task.taskId, true, false, false)
            taskDb.taskDao().updateTaskStartTime(task.taskId, System.currentTimeMillis())
        })
                .performOnBack(scheduler)
                .subscribe()
    }

    override fun startTask(taskId: Long) {
        Completable.fromAction({
            taskDb.taskDao().updateTaskStatus(taskId, true, false, false)
            taskDb.taskDao().updateTaskStartTime(taskId, System.currentTimeMillis())
        })
                .performOnBack(scheduler)
                .subscribe()
    }

    override fun finishTask(task: Task) {
        Completable.fromAction({
            val spendingTime = (System.currentTimeMillis() - task.startedTime) + task.spendingTime
            taskDb.taskDao().updateTaskStatus(task.taskId, false, true, false)
            taskDb.taskDao().updateTaskSpendingTime(task.taskId, spendingTime)
        })
                .performOnBackOutOnMain(scheduler)
                .subscribe()
    }

    override fun getTasks(): Flowable<List<Task>> {
        return taskDb.taskDao().getAll()
    }
}