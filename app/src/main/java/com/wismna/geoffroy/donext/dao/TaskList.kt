package com.wismna.geoffroy.donext.dao

/**
 * Created by geoffroy on 15-11-25.
 * Data access object class that represents a Task List
 */
class TaskList {
    @JvmField
    var id: Long = 0
    @JvmField
    var name: String? = null
    @JvmField
    var taskCount: Long = 0
    override fun toString(): String {
        return name!!
    }
}
