package com.dev.tasker.list.fragment

import android.os.Bundle
import android.view.View
import com.dev.tasker.commons.TaskDH
import com.dev.tasker.list.fragment.adapter.ListAdapter

class CurrentTabFragment : BaseTabFragment(), ListAdapter.TaskListener {

    private val component by lazy { TaskDH.listComponent() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getTasks(false, true)
        initiateDataListener(viewModel.currentTasksOutcome)
    }

    override fun onSwipeLeft(from: Int) {
        viewModel.revertTask(adapter.data[from])
    }

    override fun onSwipeRight(from: Int) {
        viewModel.revertTask(adapter.data[from])
    }

    override fun isDoneTab(): Boolean = true

    override fun enableItemsSwipe(): Boolean = false
}
