package com.wismna.geoffroy.donext.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.wismna.geoffroy.donext.ItemTouchHelpers.TaskListTouchHelper;
import com.wismna.geoffroy.donext.R;
import com.wismna.geoffroy.donext.adapters.TaskListRecyclerViewAdapter;
import com.wismna.geoffroy.donext.dao.TaskList;
import com.wismna.geoffroy.donext.database.TaskListDataAccess;

import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class TaskListsFragment extends Fragment implements TaskListRecyclerViewAdapter.TaskListRecyclerViewAdapterListener {
    private TaskListRecyclerViewAdapter taskListRecyclerViewAdapter;
    private TaskListDataAccess taskListDataAccess;
    private View mView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TaskListsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        taskListDataAccess = new TaskListDataAccess(getContext());
        new GetTaskListsTask().execute(taskListDataAccess);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_tasklists, container, false);

        Button createTaskListButton = (Button) mView.findViewById(R.id.new_task_list_button);
        createTaskListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText) mView.findViewById(R.id.new_task_list_name);
                String text = editText.getText().toString();
                if (text.matches("")) return;
                int position = taskListRecyclerViewAdapter.getItemCount();

                taskListDataAccess.open();
                TaskList taskList = taskListDataAccess.createTaskList(text, position);
                taskListDataAccess.close();
                taskListRecyclerViewAdapter.add(taskList, position);

                editText.setText("");
                toggleVisibleCreateNewTaskListLayout(mView);
            }
        });

        return mView;
    }

    private void toggleVisibleCreateNewTaskListLayout(View view) {
        LinearLayout layout = (LinearLayout) view.findViewById(R.id.new_task_list_layout);
        int taskListCount = taskListRecyclerViewAdapter.getItemCount();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String maxTaskListsString = sharedPref.getString("pref_conf_max_lists", "3");
        int maxTaskLists = Integer.valueOf(maxTaskListsString);
        if (taskListCount >= maxTaskLists) layout.setVisibility(View.GONE);
        else layout.setVisibility(View.VISIBLE);
    }

    @Override
    public void notifyOnDeleteButtonClicked() {
        toggleVisibleCreateNewTaskListLayout(mView);
    }

    @Override
    public void onPause() {
        // TODO: persist changes in DB here

        super.onPause();
    }

    public class GetTaskListsTask extends AsyncTask<TaskListDataAccess, Void, List<TaskList>> {
        @Override
        protected List<TaskList> doInBackground(TaskListDataAccess... params) {
            TaskListDataAccess taskListDataAccess = params[0];
            taskListDataAccess.open();
            List<TaskList> taskLists = taskListDataAccess.getAllTaskLists();
            taskListDataAccess.close();
            return taskLists;
        }

        @Override
        protected void onPostExecute(List<TaskList> taskLists) {
            super.onPostExecute(taskLists);
            taskListRecyclerViewAdapter =
                    new TaskListRecyclerViewAdapter(taskLists, getContext(), TaskListsFragment.this);

            // Set the adapter
            Context context = mView.getContext();
            RecyclerView recyclerView = (RecyclerView) mView.findViewById(R.id.task_lists_view);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(taskListRecyclerViewAdapter);

            // Set the Touch Helper
            ItemTouchHelper.Callback callback = new TaskListTouchHelper(taskListRecyclerViewAdapter);
            ItemTouchHelper helper = new ItemTouchHelper(callback);
            helper.attachToRecyclerView(recyclerView);

            toggleVisibleCreateNewTaskListLayout(mView);
        }
    }
}
