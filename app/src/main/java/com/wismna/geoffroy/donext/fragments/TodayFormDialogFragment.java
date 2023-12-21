package com.wismna.geoffroy.donext.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.Nullable;

import com.wismna.geoffroy.donext.R;
import com.wismna.geoffroy.donext.adapters.TodayArrayAdapter;
import com.wismna.geoffroy.donext.dao.Task;
import com.wismna.geoffroy.donext.database.TaskDataAccess;
import com.wismna.geoffroy.donext.utils.TaskRunner;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by bg45 on 2017-03-21.
 * This is the Today Form dynamic dialog fragment
 */

public class TodayFormDialogFragment extends DynamicDialogFragment {
    /** The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface TodayTaskListener {
        void onTodayTaskDialogPositiveClick(View dialogView);
        void onTodayTasksUpdated();
    }
    private TodayFormDialogFragment.TodayTaskListener mListener;
    private final List<Task> mUpdatedTasks = new ArrayList<>();

    public static TodayFormDialogFragment newInstance(TodayTaskListener todayTaskListener) {
        TodayFormDialogFragment fragment = new TodayFormDialogFragment();

        fragment.mListener = todayTaskListener;
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPositiveButtonString = getString(R.string.new_task_save);
        mNegativeButtonString = getString(R.string.new_task_cancel);
        mContentLayoutId = R.layout.content_today_form;
        // Load the tasks asynchronously
        //new LoadTasks(this).execute(getActivity());
        TaskRunner taskRunner = new TaskRunner();
        taskRunner.executeAsync(new LoadTasks(getContext()), this::setLayoutValues);
    }

    private void setLayoutValues(List<Task> tasks) {
        EditText editText = findViewById(R.id.today_search);
        final ListView listView = findViewById(R.id.today_tasks);
        final TodayArrayAdapter adapter = new TodayArrayAdapter(requireActivity(), tasks);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            // Set Today date for the task
            Task task = adapter.getItem(position);
            if (task == null) return;
            task.setTodayDate(task.isToday() ? "" : LocalDate.now().toString());
            // Maintain a list of actually updated tasks to commit to DB
            if (!mUpdatedTasks.contains(task)) mUpdatedTasks.add(task);
            else mUpdatedTasks.remove(task);
            // Refresh the view
            adapter.notifyDataSetChanged();
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    protected void onPositiveButtonClick(View view) {
        mListener.onTodayTaskDialogPositiveClick(view);
        // Only commit the updated tasks to DB
        TaskRunner taskRunner = new TaskRunner();
        taskRunner.executeAsync(new UpdateTasks(getContext(), mUpdatedTasks.toArray(new Task[0])), (position) -> mListener.onTodayTasksUpdated());
        dismiss();
    }

    @Override
    protected void onNeutralButtonClick(View view) {

    }

    @Override
    protected void onNegativeButtonClick() {
        dismiss();
    }

    static class LoadTasks implements Callable<List<Task>> {
        private final Context context;

        public LoadTasks(Context context) {
            this.context = context;
        }

        @Override
        public List<Task> call() {
            try(TaskDataAccess taskDataAccess = new TaskDataAccess(context)) {
                return taskDataAccess.getAllTasks();
            }
        }
    }

    static class UpdateTasks implements Callable<Integer> {
        private final Context context;
        private final Task[] tasks;

        public UpdateTasks(Context context, Task[] tasks) {
            this.context = context;
            this.tasks = tasks;
        }

        @Override
        public Integer call() {
            int position;
            try (TaskDataAccess taskDataAccess = new TaskDataAccess(context, TaskDataAccess.MODE.WRITE)) {
                for (position = 0; position < tasks.length; position ++) {
                    Task task = tasks[position];
                    taskDataAccess.updateTodayTasks(task.getId(), task.isToday(), position);
                }
            }
            return position;
        }
    }
 }
