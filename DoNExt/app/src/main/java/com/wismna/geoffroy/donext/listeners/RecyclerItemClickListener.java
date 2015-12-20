package com.wismna.geoffroy.donext.listeners;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by geoffroy on 15-12-02.
 * Listener class on RecyclerView to intercept touch events
 * This allows disabling swipe on any other element than the first one
 */
public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    GestureDetector mGestureDetector;

    public RecyclerItemClickListener(Context context, OnItemClickListener listener) {
        mListener = listener;
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
        View childView = view.findChildViewUnder(e.getX(), e.getY());
        int childId = view.getChildAdapterPosition(childView);
        if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
            mListener.onItemClick(childView, view.getChildAdapterPosition(childView));
        }

        // Allows right swipe moves only on first element of the list, left everywhere
        return e.getAction() != MotionEvent.ACTION_MOVE ||
                ((e.getY() - e.getHistoricalY(0) > 0 || (e.getY() - e.getHistoricalY(0)) < 0) ||
                        childId != 0 && e.getX() - e.getHistoricalX(0) > 0);
    }

    @Override
    public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }
}