package com.wismna.geoffroy.donext.helpers;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;

import com.wismna.geoffroy.donext.R;

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
        // No drag moves, no swipes (except for 1st element, see getSwipeDirs method)
        super(0, 0);
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
        // Get RecyclerView item from the ViewHolder
        View itemView = viewHolder.itemView;
        //View backgroundView = recyclerView.getRootView().findViewById(R.id.task_list_background);
        //View textView;
        /*if (dX > 0) {
            textView = recyclerView.getRootView().findViewById(R.id.task_background_next);
        } else {
            textView = recyclerView.getRootView().findViewById(R.id.task_background_done);
        }
        //backgroundView.setY(itemView.getTop());

        if (isCurrentlyActive) {
            backgroundView.setVisibility(View.VISIBLE);
            textView.setVisibility(View.VISIBLE);
        } else {
            backgroundView.setVisibility(View.GONE);
            textView.setVisibility(View.GONE);
        }*/
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            Paint background = new Paint();
            background.setARGB(255, 222, 222, 222);

            TextPaint textPaint = new TextPaint();
            textPaint.setAntiAlias(true);
            textPaint.setTextSize(25 * itemView.getResources().getDisplayMetrics().density);
            textPaint.setColor(Color.WHITE);

            int heightOffset = itemView.getHeight() / 2 - (int)textPaint.getTextSize() / 2;
            int widthOffset = 30;
            // Set your color for positive displacement
            if (dX > 0) {
                //p.setARGB(255, 204, 229, 255);
                // Draw Rect with varying right side, equal to displacement dX
                Rect rect = new Rect(itemView.getLeft(), itemView.getTop(), (int) dX,
                        itemView.getBottom());
                c.drawRect(rect, background);

                // Draw text in the rectangle
                String text = itemView.getResources().getString(R.string.task_confirmation_next_button).toUpperCase();
                int width = (int) textPaint.measureText(text);
                float textXCoordinate = rect.left + widthOffset;
                c.translate(textXCoordinate, dY + heightOffset);
                StaticLayout staticLayout = new StaticLayout(text, textPaint, width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0, false);
                staticLayout.draw(c);
                //textView = recyclerView.getRootView().findViewById(R.id.task_background_next);

            } else {
                // Set your color for negative displacement
                //p.setARGB(255, 204, 255, 229);
                // Draw Rect with varying left side, equal to the item's right side plus negative displacement dX
                Rect rect = new Rect(itemView.getRight() + (int)dX, itemView.getTop(),
                        itemView.getRight(), itemView.getBottom());
                c.drawRect(rect, background);

                // Draw text in the rectangle
                String text = itemView.getResources().getString(R.string.task_confirmation_done_button).toUpperCase();
                int width = (int) textPaint.measureText(text);
                float textXCoordinate = rect.right - width - widthOffset;
                c.translate(textXCoordinate, dY + heightOffset);
                StaticLayout staticLayout = new StaticLayout(text, textPaint, width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0, false);
                staticLayout.draw(c);
                //textView = recyclerView.getRootView().findViewById(R.id.task_background_done);
            }
            //textView.draw(c);
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    /*@Override
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
    }*/
}
