package com.wismna.geoffroy.donext.helpers;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

/**
 * Created by geoffroy on 15-12-04.
 * Helper class that handles all swipe events on a Task
 */
public class TaskTouchHelper extends ItemTouchHelper.SimpleCallback {
    public interface TaskTouchHelperAdapter {
        void onItemSwiped(int position, int direction);
    }

    private TaskTouchHelperAdapter mAdapter;

    public TaskTouchHelper(TaskTouchHelperAdapter adapter){
        // No drag moves, only left swipes (except for 1st element, see getSwipeDirs method)
        super(0, ItemTouchHelper.LEFT);
        this.mAdapter = adapter;
    }

    @Override
    public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        // Allow both directions swiping on first item, only left on the others
        if (viewHolder.getAdapterPosition() == 0)
            return ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        else return super.getSwipeDirs(recyclerView, viewHolder);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        mAdapter.onItemSwiped(viewHolder.getAdapterPosition(), direction);
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            // Get RecyclerView item from the ViewHolder
            View itemView = viewHolder.itemView;

            Paint p = new Paint();
            if (dX > 0) {
            /* Set your color for positive displacement */
                p.setARGB(255, 204, 229, 255);

                // Draw Rect with varying right side, equal to displacement dX
                c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX,
                        (float) itemView.getBottom(), p);
            } else {
            /* Set your color for negative displacement */
                p.setARGB(255, 204, 255, 229);

                // Draw Rect with varying left side, equal to the item's right side plus negative displacement dX
                c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                        (float) itemView.getRight(), (float) itemView.getBottom(), p);
            }

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE)
        {
            viewHolder.itemView.setBackgroundColor(Color.LTGRAY);
        }

        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);

        viewHolder.itemView.setAlpha(1.0f);
        viewHolder.itemView.setBackgroundColor(0);
    }
}
