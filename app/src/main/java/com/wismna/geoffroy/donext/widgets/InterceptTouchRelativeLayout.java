package com.wismna.geoffroy.donext.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * Created by GBE on 22/12/2017.
 * This class extends RelativeLayout to intercept touch event (and disable them)
 */

public class InterceptTouchRelativeLayout extends RelativeLayout {
    private boolean interceptTouchEvents = false;

    public InterceptTouchRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setInterceptTouchEvents(boolean interceptTouchEvents) {
        this.interceptTouchEvents = interceptTouchEvents;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return interceptTouchEvents || super.onInterceptTouchEvent(ev);
    }
}
