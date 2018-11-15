package com.wismna.geoffroy.donext.repositories;

import android.app.Application;
import android.os.AsyncTask;

import com.wismna.geoffroy.donext.data.AppDatabase;
import com.wismna.geoffroy.donext.data.Task;
import com.wismna.geoffroy.donext.data.TaskDao;

import java.util.List;

import androidx.lifecycle.LiveData;

public class TaskRepository {
    private TaskDao mTaskDao;

    TaskRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mTaskDao = db.taskDao();
    }

    public void insert(Task task) {
        new insertAsyncTask(mTaskDao).execute(task);
    }

    public void update(Task task) {
        new updateAsyncTask(mTaskDao).execute(task);
    }

    public LiveData<List<Task>> getTasksInList(long taskId) {
        return mTaskDao.getAllTasksFromList(taskId);
    }

    public LiveData<List<Task>> getTodayTasks() {
        return mTaskDao.getTodayTasks();
    }

    // Async tasks
    private static class insertAsyncTask extends AsyncTask<Task, Void, Void> {

        private TaskDao mAsyncTaskDao;

        insertAsyncTask(TaskDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Task... params) {
            mAsyncTaskDao.createTask(params[0]);
            return null;
        }
    }

    private static class updateAsyncTask extends AsyncTask<Task, Void, Void> {

        private TaskDao mAsyncTaskDao;

        updateAsyncTask(TaskDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Task... params) {
            mAsyncTaskDao.updateTask(params[0]);
            return null;
        }
    }
}
