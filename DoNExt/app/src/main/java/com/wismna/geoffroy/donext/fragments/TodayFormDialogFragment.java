package com.wismna.geoffroy.donext.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.wismna.geoffroy.donext.R;
import com.wismna.geoffroy.donext.adapters.TodayArrayAdapter;
import com.wismna.geoffroy.donext.dao.Task;
import com.wismna.geoffroy.donext.database.TaskDataAccess;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

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

    public static TodayFormDialogFragment newInstance(Context context, TodayTaskListener todayTaskListener) {
        TodayFormDialogFragment fragment = new TodayFormDialogFragment();

        fragment.mListener = todayTaskListener;
        fragment.setRetainInstance(true);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContentFragment = new TodayFormContentFragment();
        // Load the tasks asynchronously
        new LoadTasks().execute(getActivity());
        Bundle args = getArguments();
        if (args != null) {
            mIsLargeLayout = args.getBoolean("layout");
        }
    }

    private void setLayoutValues(View view, List<Task> tasks) {
        EditText editText = (EditText) view.findViewById(R.id.today_search);
        final ListView listView = (ListView) view.findViewById(R.id.today_tasks);
        final TodayArrayAdapter adapter = new TodayArrayAdapter(getActivity(), tasks);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Set Today date for the task
                Task task = adapter.getItem(position);
                if (task == null) return;
                task.setTodayDate(task.isToday() ? "" : LocalDate.now().toString());
                // Maintain a list of actually updated tasks to commit to DB
                if (!mUpdatedTasks.contains(task)) mUpdatedTasks.add(task);
                else mUpdatedTasks.remove(task);
                // Refresh the view
                adapter.notifyDataSetChanged();
            }
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
        new UpdateTasks().execute(mUpdatedTasks.toArray(new Task[mUpdatedTasks.size()]));
        dismiss();
    }

    @Override
    protected void onNeutralButtonClick(View view) {

    }

    @Override
    protected void onNegativeButtonClick() {
        dismiss();
    }

    private class LoadTasks extends AsyncTask<Context, Void, List<Task>> {
        @Override
        protected  List<Task> doInBackground(Context... params) {
            try(TaskDataAccess taskDataAccess = new TaskDataAccess(params[0])) {
                return taskDataAccess.getAllTasks();
            }
        }

        @Override
        protected void onPostExecute(List<Task> tasks) {
            super.onPostExecute(tasks);
            setLayoutValues(getView(), tasks);
        }
    }

    private class UpdateTasks extends AsyncTask<Task, Void, Integer> {
        @Override
        protected Integer doInBackground(Task... params) {
            int elementsUpdated = 0;
            try (TaskDataAccess taskDataAccess = new TaskDataAccess(getActivity(), TaskDataAccess.MODE.WRITE)) {
                for (Task task :
                        params) {
                    taskDataAccess.updateTodayTasks(task.getId(), task.isToday());
                    elementsUpdated++;
                }
            }
            return elementsUpdated;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            mListener.onTodayTasksUpdated();
        }
    }
 }
