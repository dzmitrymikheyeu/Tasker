package com.dev.tasker.list.fragment

import android.app.AlarmManager
import android.app.PendingIntent
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dev.tasker.R
import com.dev.tasker.commons.data.local.Task
import com.dev.tasker.core.networking.Outcome
import com.dev.tasker.create.CreateTaskActivity
import com.dev.tasker.extensions.showToast
import com.dev.tasker.list.fragment.adapter.ListAdapter
import com.dev.tasker.list.view.ItemTaskTouchHelperCallback
import com.dev.tasker.list.view.ItemTouchHelperAdapter
import com.dev.tasker.list.viewmodel.ListViewModel
import com.dev.tasker.list.viewmodel.ListViewModelFactory
import com.dev.tasker.service.TaskerService
import com.dev.tasker.upload.FileUploadService
import kotlinx.android.synthetic.main.fragment_tasks.*
import javax.inject.Inject

abstract class BaseTabFragment : Fragment(), ListAdapter.TaskListener {

    @Inject
    lateinit var viewModelFactory: ListViewModelFactory
    protected val viewModel: ListViewModel by lazy {
        ViewModelProviders.of(this,
                viewModelFactory).get(ListViewModel::class.java)
    }

    protected val adapter: ListAdapter by lazy { ListAdapter() }

    protected abstract fun onSwipeLeft(from: Int)
    protected abstract fun onSwipeRight(from: Int)
    protected abstract fun isDoneTab(): Boolean
    protected abstract fun enableItemsSwipe(): Boolean

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.fragment_tasks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        rv_tasks.adapter = adapter
        adapter.listener = this
        if (enableItemsSwipe()) {
            val itemTaskTouchHelperCallback = ItemTaskTouchHelperCallback(
                    object : ItemTouchHelperAdapter {
                override fun onLeftSwipe(from: Int) {
                    onSwipeLeft(from)
                }

                override fun onRightSwipe(from: Int) {
                    onSwipeRight(from)
                }
            }, isDoneTab())
            val itemTouchHelper = ItemTouchHelper(itemTaskTouchHelperCallback)
            itemTouchHelper.attachToRecyclerView(rv_tasks)
        }
    }

    protected fun initiateDataListener(liveData: LiveData<Outcome<List<Task>>>) {
        liveData.observe(this, Observer<Outcome<List<Task>>> { outcome ->
            when (outcome) {
                is Outcome.Success -> {
                    with(outcome) {
                        img_empty.visibility =
                                if (data.isEmpty()) View.VISIBLE else View.GONE
                        adapter.data = data
                    }
                }

                is Outcome.Failure -> {
                    activity?.showToast(outcome.e.localizedMessage)
                }
            }
        })
    }

    override fun itemClicked(task: Task) {
    }

    override fun itemEdit(task: Task) {
        startActivity(Intent(activity, CreateTaskActivity::class.java)
                .putExtra(CreateTaskActivity.ARG_TASK, task))
    }

    override fun itemStart(task: Task) {
        viewModel.startTask(task)
    }

    override fun itemPostpone(task: Task) {
        viewModel.startTaskPostpone(task)
        configureAlarm(task)
    }

    override fun itemStop(task: Task) {
        viewModel.stopTask(task)
    }

    override fun itemFinish(task: Task) {
        viewModel.finishTask(task)
    }

    override fun itemRevert(task: Task) {
        viewModel.revertTask(task)
    }

    override fun itemRemove(task: Task) {
        viewModel.removeTask(task)
    }

    override fun itemOpenFile(task: Task) {
        val intent = Intent(activity, FileUploadService::class.java)
        intent.putExtra(FileUploadService.ARG_FILE_PATH, task.filePath)
        intent.putExtra(FileUploadService.ARG_UPLOAD_IMAGE, false)
        activity?.startService(intent)
    }

    protected fun configureAlarm(task: Task) {
        val intent = Intent(activity, TaskerService::class.java)
        intent.putExtra(TaskerService.ARG_TASK_ID, task.taskId)
        val alarm = activity?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmTime = System.currentTimeMillis() + DateUtils.MINUTE_IN_MILLIS
        val pendingIntent = PendingIntent.getService(context, alarmTime.toInt(),
                intent, PendingIntent.FLAG_CANCEL_CURRENT)
        alarm.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent)
        activity?.showToast(String.format(getString(R.string.message_task_postponed), task.name))
    }

}