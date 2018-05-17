package com.dev.tasker.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.arch.lifecycle.LifecycleService
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import com.dev.tasker.R
import com.dev.tasker.commons.TaskDH
import com.dev.tasker.commons.data.local.Task
import com.dev.tasker.core.extensions.toLiveData
import com.dev.tasker.core.networking.Outcome
import com.dev.tasker.create.CreateTaskActivity
import com.dev.tasker.list.MainActivity
import com.dev.tasker.service.model.ServiceDataContract
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class TaskerService : LifecycleService() {

    companion object {
        private const val PACKAGE = "com.tasker."
        const val CHANNEL_ID = PACKAGE + "TASKS_CHANNEL"
        const val ACTION_DEFAULT = PACKAGE + "ACTION_DEFAULT"
        const val ACTION_CANCEL = PACKAGE + "ACTION_CANCEL"
        const val ACTION_FINISH = PACKAGE + "ACTION_FINISH"
        const val ACTION_CREATE = PACKAGE + "ACTION_CREATE"
        const val ARG_TASK = PACKAGE + "ARG_TASK"
        const val ARG_TASK_ID = PACKAGE + "ARG_TASK_ID"
    }

    private val component by lazy { TaskDH.serviceComponent() }
    @Inject
    lateinit var repo: ServiceDataContract.Repository
    @Inject
    lateinit var compositeDisposable: CompositeDisposable

    private val notificationManager: NotificationManager
            by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    override fun onCreate() {
        super.onCreate()
        component.inject(this)

        repo.tasksFetchOutcome.toLiveData(compositeDisposable)
                .observe(this, Observer<Outcome<List<Task>>> { outcome ->
                    when (outcome) {
                        is Outcome.Success -> {
                            outcome.data.forEach {
                                if (it.started) {
                                    prepareNotification(it)
                                } else {
                                    removeNotification(it.taskId.toInt())
                                }
                            }
                        }

                        is Outcome.Failure -> {
                            outcome.e.printStackTrace()
                        }

                    }
                })
        repo.fetchTasks()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) handleIntent(intent)
        return super.onStartCommand(intent, flags, startId)
    }

    private fun handleIntent(intent: Intent) {
        with(intent) {
            if (intent.hasExtra(ARG_TASK)) {
                val task = getParcelableExtra(ARG_TASK) as Task
                when (action) {
                    ACTION_CANCEL -> cancelTask(task)
                    ACTION_FINISH -> finishTask(task)
                }
            } else if (intent.hasExtra(ARG_TASK_ID)) {
                startTask(intent.getLongExtra(ARG_TASK_ID, 0))
            }
        }
    }

    private fun startTask(taskId: Long) {
        repo.startTask(taskId)
    }

    private fun cancelTask(task: Task) {
        repo.stopTask(task)
        notificationManager.cancel(task.taskId.toInt())
    }

    private fun finishTask(task: Task) {
        repo.finishTask(task)
        notificationManager.cancel(task.taskId.toInt())
    }

    private fun prepareNotification(task: Task) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O &&
                notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            val channelName = getString(R.string.notification_channel_name)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, channelName, importance)
            channel.enableVibration(false)
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            channel.enableLights(true)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.action = ACTION_DEFAULT
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        val defaultPendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val cancelIntent = Intent(this, TaskerService::class.java)
        cancelIntent.putExtra(ARG_TASK, task)
        cancelIntent.action = ACTION_CANCEL
        val cancelPendingIntent = PendingIntent.getService(this, task.taskId.toInt(),
                cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val finishIntent = Intent(this, TaskerService::class.java)
        finishIntent.putExtra(ARG_TASK, task)
        finishIntent.action = ACTION_FINISH
        val finishPendingIntent = PendingIntent.getService(this, task.taskId.toInt(),
                finishIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val createIntent = Intent(this, CreateTaskActivity::class.java)
        createIntent.action = ACTION_CREATE
        createIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        val createPendingIntent = PendingIntent.getActivities(this, 0,
                arrayOf(notificationIntent, createIntent), PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationBuilder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationCompat.Builder(this, CHANNEL_ID)
        } else NotificationCompat.Builder(this)

        notificationBuilder
                .setSmallIcon(R.mipmap.ic_launcher)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setContentIntent(defaultPendingIntent)
                .setContentTitle(task.name)
                .setContentText(task.description)
                .setSubText(getString(R.string.in_progress))
                .addAction(R.drawable.ic_add_circle, getString(R.string.action_add), createPendingIntent)
                .addAction(R.drawable.ic_remove_circle, getString(R.string.action_stop), cancelPendingIntent)
                .addAction(R.drawable.ic_check_circle, getString(R.string.action_done), finishPendingIntent)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC)
        }

        val notification = notificationBuilder.build()
        notificationManager.notify(task.taskId.toInt(), notification)
    }

    private fun removeNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
        TaskDH.destroyServiceComponent()
    }
}