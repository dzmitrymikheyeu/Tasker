package com.dev.tasker.list.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import com.dev.tasker.R

class ItemTaskTouchHelperCallback(private val adapter: ItemTouchHelperAdapter,
                                  private val isDone: Boolean,
                                  private val isCurrent: Boolean) : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN,
        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    private var isFinish = false
    private var isRemove = false
    private var isRevert = false
    private var isStart = false
    private var isPostpone = false
    private val background = ColorDrawable()
    private val textPaint = Paint()

    init {
        textPaint.isAntiAlias = true
        textPaint.color = Color.WHITE
    }

    override fun onMove(recyclerView: RecyclerView, holder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder): Boolean {
        return true
    }

    override fun onSwiped(holder: RecyclerView.ViewHolder, direction: Int) {
        val fromPos = holder.adapterPosition
        if (direction == ItemTouchHelper.LEFT) {
            adapter.onRightSwipe(fromPos)
        } else {
            adapter.onLeftSwipe(fromPos)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onChildDraw(canvas: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                             dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val context = recyclerView.context

            setMode(dX)

            val itemView = viewHolder.itemView
            val itemHeight = itemView.bottom - itemView.top

            // Fade animation
            val alpha = 1.0f - Math.abs(dX) / itemView.width
            viewHolder.itemView.alpha = alpha
            viewHolder.itemView.translationX = dX

            // Draw the background
            background.color = getColor(context)
            background.setBounds(
                    if (dX < 0) itemView.right + dX.toInt() else itemView.left,
                    itemView.top,
                    if (dX < 0) itemView.right else (dX.toInt() + itemView.left),
                    itemView.bottom
            )
            background.draw(canvas)

            // Calculate position of icon
            val icon = ContextCompat.getDrawable(context, getDrawableRes())
            val intrinsicWidth = icon?.intrinsicWidth
            val intrinsicHeight = icon?.intrinsicHeight
            val iconTop = itemView.top + (itemHeight - intrinsicHeight!!) / 2
            val iconMargin = (itemHeight - intrinsicHeight) / 2
            val iconLeft = if (dX < 0) itemView.right - iconMargin - intrinsicWidth!! else {
                itemView.left + iconMargin
            }
            val iconRight = if (dX < 0) itemView.right - iconMargin else {
                itemView.left + iconMargin + intrinsicWidth!!
            }
            val iconBottom = iconTop + intrinsicHeight

            // Draw the icon
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            icon.draw(canvas)
        }

        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private fun setMode(dX: Float) {
        clearFlags()
        if (dX > 0 && isCurrent) isFinish = true
        else if (dX < 0 && isCurrent) isRevert = true
        else if (dX > 0 && isDone) isRemove = true
        else if (dX < 0 && isDone) isRevert = true
        else if (dX > 0) isStart = true
        else if (dX < 0) isPostpone = true
    }

    private fun clearFlags() {
        isPostpone = false
        isRemove = false
        isRevert = false
        isStart = false
        isFinish = false
    }

    private fun getColor(context: Context): Int {
        val colorRes = when {
            isRemove -> R.color.red_color
            isPostpone -> R.color.colorPrimaryDark
            isRevert -> R.color.colorAccent
            isFinish -> R.color.green_color
            else -> R.color.colorPrimary
        }
        return ContextCompat.getColor(context, colorRes)
    }

    private fun getDrawableRes(): Int {
        return when {
            isRemove -> R.drawable.ic_delete
            isPostpone -> R.drawable.ic_notifications_paused
            isRevert -> R.drawable.ic_replay
            isFinish -> R.drawable.ic_done_task
            else -> R.drawable.ic_play
        }
    }
}