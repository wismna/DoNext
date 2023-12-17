package com.wismna.geoffroy.donext.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

/**
 * Created by geoffroy on 15-12-04.
 * Custom ViewPager to forbid vertical swiping between tabs
 */
class NonSwipeableViewPager : ViewPager {
    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        // Never allow swiping to switch between pages
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Never allow swiping to switch between pages
        performClick()
        return false
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }
}
