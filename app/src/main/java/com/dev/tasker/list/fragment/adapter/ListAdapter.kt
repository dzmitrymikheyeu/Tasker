package com.dev.tasker.list.fragment.adapter

import android.support.v4.content.ContextCompat
import android.support.v7.util.DiffUtil
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dev.tasker.R
import com.dev.tasker.commons.data.local.Task
import com.dev.tasker.list.DiffUtilsCallback
import kotlinx.android.synthetic.main.item_task.view.*

class ListAdapter : RecyclerView.Adapter<ListAdapter.ListViewHolder>() {

    interface TaskListener {
        fun itemClicked(task: Task)
        fun itemEdit(task: Task)
        fun itemStart(task: Task)
        fun itemPostpone(task: Task)
        fun itemStop(task: Task)
        fun itemFinish(task: Task)
        fun itemRevert(task: Task)
        fun itemRemove(task: Task)
        fun itemOpenFile(task: Task)
    }

    var data: List<Task> = emptyList()
        set(value) {
            val diffResult = DiffUtil.calculateDiff(DiffUtilsCallback(data, value))
            field = value
            diffResult.dispatchUpdatesTo(this)
        }

    var listener: TaskListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        return ListViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false))
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        var task = data[position]
        with(holder.itemView) {
            holder.bind(task)
            if (this@with != null) {
                setOnClickListener {
                    listener?.itemClicked(task)
                }
                more.setOnClickListener {
                    showPopupMenu(task, holder.itemView.more)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class ListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(task: Task) {
            with(task) {
                itemView.tvTitle.text = name
                itemView.tvBody.text = description
                if (!task.keywords.isNullOrEmpty()) {
                    itemView.tagsContainer.setTags(task.keywords.split(","))
                }
                if (finished) {
                    itemView.time.visibility = View.VISIBLE
                    itemView.time.text = DateUtils.formatElapsedTime(spendingTime / DateUtils.SECOND_IN_MILLIS)
                } else if (started) {
                    itemView.tvTitle.setTextColor(ContextCompat.getColor(itemView.context, R.color.colorPrimaryDark))
                    itemView.time.visibility = View.VISIBLE
                    itemView.time.text = itemView.context.getString(R.string.in_progress)
                }
            }
        }
    }

    private fun showPopupMenu(task: Task, view: View) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.inflate(R.menu.item_menu)
        when {
            task.finished -> {
                popupMenu.menu.removeItem(R.id.menuStart)
                popupMenu.menu.removeItem(R.id.menuStop)
                popupMenu.menu.removeItem(R.id.menuPostpone)
                popupMenu.menu.removeItem(R.id.menuFinish)
            }
            task.started -> {
                popupMenu.menu.removeItem(R.id.menuStart)
                popupMenu.menu.removeItem(R.id.menuPostpone)
                popupMenu.menu.removeItem(R.id.menuRevert)
                popupMenu.menu.removeItem(R.id.menuRemove)
            }
            else -> {
                popupMenu.menu.removeItem(R.id.menuFinish)
                popupMenu.menu.removeItem(R.id.menuStop)
                popupMenu.menu.removeItem(R.id.menuRevert)
            }
        }
        if (task.filePath.isNullOrEmpty()) {
            popupMenu.menu.removeItem(R.id.menuFile)
        }
        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {
            when (it.itemId) {
                R.id.menuStart -> listener?.itemStart(task)
                R.id.menuStop -> listener?.itemStop(task)
                R.id.menuFinish -> listener?.itemFinish(task)
                R.id.menuRevert -> listener?.itemRevert(task)
                R.id.menuPostpone -> listener?.itemPostpone(task)
                R.id.menuEdit -> listener?.itemEdit(task)
                R.id.menuRemove -> listener?.itemRemove(task)
                R.id.menuFile -> listener?.itemOpenFile(task)
            }
            return@OnMenuItemClickListener true
        })
        popupMenu.show()
    }
}