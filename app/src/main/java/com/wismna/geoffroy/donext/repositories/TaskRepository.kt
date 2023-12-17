package com.wismna.geoffroy.donext.repositories

import android.app.Application
import android.os.AsyncTask
import androidx.lifecycle.LiveData
import com.wismna.geoffroy.donext.data.AppDatabase.Companion.getDatabase
import com.wismna.geoffroy.donext.data.Task
import com.wismna.geoffroy.donext.data.TaskDao

class TaskRepository internal constructor(application: Application?) {
    private val mTaskDao: TaskDao?

    init {
        val db = getDatabase(application!!)
        mTaskDao = db!!.taskDao()
    }

    fun insert(task: Task?) {
        InsertAsyncTask(mTaskDao).execute(task)
    }

    fun update(task: Task?) {
        UpdateAsyncTask(mTaskDao).execute(task)
    }

    fun getTasksInList(taskId: Long): LiveData<List<Task?>?>? {
        return mTaskDao!!.getAllTasksFromList(taskId)
    }

    val todayTasks: LiveData<List<Task?>?>?
        get() = mTaskDao!!.todayTasks

    // Async tasks
    private class InsertAsyncTask internal constructor(private val mAsyncTaskDao: TaskDao?) : AsyncTask<Task?, Void?, Void?>() {
        @Deprecated("Deprecated in Java")
        protected override fun doInBackground(vararg params: Task?): Void? {
            mAsyncTaskDao!!.createTask(params[0])
            return null
        }
    }

    private class UpdateAsyncTask internal constructor(private val mAsyncTaskDao: TaskDao?) : AsyncTask<Task?, Void?, Void?>() {
        @Deprecated("Deprecated in Java")
        protected override fun doInBackground(vararg params: Task?): Void? {
            mAsyncTaskDao!!.updateTask(params[0])
            return null
        }
    }
}
