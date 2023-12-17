package com.wismna.geoffroy.donext.helpers

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.wismna.geoffroy.donext.R
import java.util.Locale

/**
 * Created by geoffroy on 15-12-04.
 * Helper class that handles all swipe events on a Task
 */
class TaskTouchHelper // No drag moves, no swipes (except for 1st element, see getSwipeDirs method)
(private val mAdapter: TaskTouchHelperAdapter, private val colorRight: Int, private val colorLeft: Int) : ItemTouchHelper.SimpleCallback(0, 0) {
    interface TaskTouchHelperAdapter {
        fun onItemSwiped(position: Int, direction: Int)
    }

    override fun getSwipeDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        // Allow both directions swiping on first item, only left on the others
        return if (viewHolder.absoluteAdapterPosition == 0) ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT else super.getSwipeDirs(recyclerView, viewHolder)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        mAdapter.onItemSwiped(viewHolder.absoluteAdapterPosition, direction)
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                             dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        // Get RecyclerView item from the ViewHolder
        val itemView = viewHolder.itemView
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            if (dX > 0) {
                val rect = Rect(itemView.left, itemView.top, dX.toInt(),
                        itemView.bottom)
                setBackground(c, itemView, rect, dX, dY,
                        colorLeft, R.string.task_confirmation_next_button)
            } else {
                // Draw Rect with varying left side, equal to the item's right side plus negative displacement dX
                val rect = Rect(itemView.right + dX.toInt(), itemView.top,
                        itemView.right, itemView.bottom)
                setBackground(c, itemView, rect, dX, dY,
                        colorRight, R.string.task_confirmation_done_button)
            }
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private fun setBackground(c: Canvas, itemView: View,
                              rect: Rect, dX: Float, dY: Float, color: Int, textId: Int) {
        val textPaint = TextPaint()
        textPaint.isAntiAlias = true
        textPaint.textSize = 25 * itemView.resources.displayMetrics.density
        textPaint.color = Color.WHITE
        val heightOffset = itemView.height / 2 - textPaint.textSize.toInt() / 2
        val widthOffset = 30f
        val background = Paint()
        // Set your color for negative displacement
        background.color = color
        // Draw Rect with varying left side, equal to the item's right side plus negative displacement dX
        c.drawRect(rect, background)

        // Draw text in the rectangle
        val text = itemView.resources.getString(textId).uppercase(Locale.getDefault())
        val width = textPaint.measureText(text)
        val textXCoordinate: Float = if (dX > 0) rect.left + widthOffset else rect.right - width - widthOffset
        c.translate(textXCoordinate, dY + heightOffset)
        val staticLayout = StaticLayout(text, textPaint, width.toInt(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0f, false)
        staticLayout.draw(c)
    }
}
