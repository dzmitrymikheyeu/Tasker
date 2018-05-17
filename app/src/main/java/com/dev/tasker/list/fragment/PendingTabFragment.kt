package com.dev.tasker.list.fragment

import android.os.Bundle
import android.view.View
import com.dev.tasker.commons.TaskDH

class PendingTabFragment : BaseTabFragment() {

    private val component by lazy { TaskDH.listComponent() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getTasks(false, false)
        initiateDataListener(viewModel.pendingTasksOutcome)
    }

    override fun onSwipeLeft(from: Int) {
       viewModel.startTask(adapter.data[from])
    }

    override fun onSwipeRight(from: Int) {
        viewModel.startTaskPostpone(adapter.data[from])
        configureAlarm(adapter.data[from])
    }

    override fun isDoneTab(): Boolean = false

    override fun enableItemsSwipe(): Boolean = true
}
