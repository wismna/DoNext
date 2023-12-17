package com.wismna.geoffroy.donext.widgets

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager

/**
 * Created by geoffroy on 15-12-21.
 * Custom Layout Manager that disables vertical scrolling for the RecyclerView
 */
class NoScrollingLayoutManager(context: Context?) : LinearLayoutManager(context) {
    override fun canScrollVertically(): Boolean {
        return false
    }
}
