package com.dev.tasker.list

import android.support.v7.util.DiffUtil
import com.dev.tasker.commons.data.local.Task

class DiffUtilsCallback(private val oldList: List<Task>?, private val newList: List<Task>?) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return newList?.get(newItemPosition)?.taskId == oldList?.get(oldItemPosition)?.taskId
    }

    override fun getOldListSize(): Int {
        return oldList?.size ?: 0
    }

    override fun getNewListSize(): Int {
        return newList?.size ?: 0
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val first = newList?.get(newItemPosition)
        val second = oldList?.get(oldItemPosition)
        return first?.taskId == second?.taskId
                && first?.description.equals(second?.description)
                && first?.name.equals(second?.name)
                && first?.keywords.equals(second?.keywords)
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        return super.getChangePayload(oldItemPosition, newItemPosition)
    }
}
