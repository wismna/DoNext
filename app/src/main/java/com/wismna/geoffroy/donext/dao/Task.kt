package com.wismna.geoffroy.donext.dao

import org.joda.time.LocalDate

/**
 * Created by geoffroy on 15-11-25.
 * Data access object class that represents a Task
 */
class Task {
    @JvmField
    var id: Long = 0
    @JvmField
    var name: String? = null
    @JvmField
    var description: String? = null
    @JvmField
    var priority = 0
    @JvmField
    var cycle = 0
    @JvmField
    var done = 0
    @JvmField
    var deleted = 0
    @JvmField
    var order = 0
    @JvmField
    var todayOrder = 0
    var taskListId: Long = 0
        private set
    @JvmField
    var taskListName: String? = null
    var dueDate: LocalDate? = null
        private set
    private var todayDate: LocalDate? = null
    fun setTaskList(taskList: Long) {
        taskListId = taskList
    }

    fun setDueDate(dueDate: String?) {
        try {
            this.dueDate = LocalDate.parse(dueDate)
        } catch (e: Exception) {
            this.dueDate = null
        }
    }

    fun setTodayDate(todayDate: String?) {
        try {
            this.todayDate = LocalDate.parse(todayDate)
        } catch (e: Exception) {
            this.todayDate = null
        }
    }

    val isToday: Boolean
        get() = todayDate != null && todayDate!!.isEqual(LocalDate.now())
    val isHistory: Boolean
        get() = done == 1 || deleted == 1

    // Will be used by the ArrayAdapter in the ListView
    override fun toString(): String {
        return name!!
    }
}
