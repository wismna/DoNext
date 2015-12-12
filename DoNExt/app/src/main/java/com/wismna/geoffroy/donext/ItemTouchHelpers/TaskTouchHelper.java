package com.wismna.geoffroy.donext.ItemTouchHelpers;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.wismna.geoffroy.donext.activities.MainActivity;
import com.wismna.geoffroy.donext.adapters.TaskAdapter;
import com.wismna.geoffroy.donext.database.TaskDataAccess;
import com.wismna.geoffroy.donext.fragments.ConfirmDialogFragment;

/**
 * Created by geoffroy on 15-12-04.
 */
public class TaskTouchHelper extends ItemTouchHelper.SimpleCallback {
    private TaskAdapter taskAdapter;
    private TaskDataAccess taskDataAccess;
    private FragmentManager fragmentManager;
    private RecyclerView recyclerView;

    public TaskTouchHelper(TaskAdapter taskAdapter, TaskDataAccess taskDataAccess,
                           FragmentManager fragmentManager, RecyclerView recyclerView){
        // No drag moves, only swipes
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.taskAdapter = taskAdapter;
        this.taskDataAccess = taskDataAccess;
        this.fragmentManager = fragmentManager;
        this.recyclerView = recyclerView;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        //TODO: Not implemented here
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(viewHolder.itemView.getContext());
        int itemPosition = viewHolder.getAdapterPosition();
        String title = "Confirm";
        boolean showDialog = false;

        switch (direction)
        {
            // Mark item as Done
            case ItemTouchHelper.LEFT:
                title = "Done";
                showDialog = sharedPref.getBoolean("pref_conf_done", true);
                break;
            // Increase task cycle count
            case ItemTouchHelper.RIGHT:
                title = "Next";
                showDialog = sharedPref.getBoolean("pref_conf_next", true);
                break;
        }
        if (showDialog) {
            ConfirmDialogFragment confirmDialogFragment =
                    ConfirmDialogFragment.newInstance(taskAdapter, title, recyclerView);
            Bundle args = new Bundle();
            args.putInt("ItemPosition", itemPosition);
            args.putInt("Direction", direction);
            confirmDialogFragment.setArguments(args);
            confirmDialogFragment.show(fragmentManager, title);
        }
        else MainActivity.PerformSwipeAction(taskDataAccess, taskAdapter, itemPosition, direction, recyclerView);
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
}
