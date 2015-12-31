package com.wismna.geoffroy.donext.ItemTouchHelpers;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * Created by geoffroy on 15-12-30.
 * Helper class that handles all drags events on a TaskList
 */
public class TaskListTouchHelper extends ItemTouchHelper.SimpleCallback  {

    public interface TaskListTouchHelperAdapter {
        boolean onItemMove (int fromPosition, int toPosition);
    }

    private final TaskListTouchHelperAdapter mAdapter;

    public TaskListTouchHelper(TaskListTouchHelperAdapter adapter) {
        super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
        mAdapter = adapter;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        // No swipe moves
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }
}
