package com.wismna.geoffroy.donext.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.RelativeLayout

/**
 * Created by GBE on 22/12/2017.
 * This class extends RelativeLayout to intercept touch event (and disable them)
 */
class InterceptTouchRelativeLayout(context: Context?, attrs: AttributeSet?) : RelativeLayout(context, attrs) {
    private var interceptTouchEvents = false
    fun setInterceptTouchEvents(interceptTouchEvents: Boolean) {
        this.interceptTouchEvents = interceptTouchEvents
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return interceptTouchEvents || super.onInterceptTouchEvent(ev)
    }
}
