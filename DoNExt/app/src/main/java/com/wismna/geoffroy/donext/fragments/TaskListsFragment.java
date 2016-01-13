package com.wismna.geoffroy.donext.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.wismna.geoffroy.donext.R;
import com.wismna.geoffroy.donext.adapters.TaskListRecyclerViewAdapter;
import com.wismna.geoffroy.donext.dao.TaskList;
import com.wismna.geoffroy.donext.database.TaskListDataAccess;
import com.wismna.geoffroy.donext.helpers.TaskListTouchHelper;

import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class TaskListsFragment extends Fragment implements
        TaskListRecyclerViewAdapter.TaskListRecyclerViewAdapterListener,
        ConfirmDialogFragment.ConfirmDialogListener {
    private TaskListRecyclerViewAdapter taskListRecyclerViewAdapter;
    private TaskListDataAccess taskListDataAccess;
    private View mView;
    private ItemTouchHelper mItemTouchHelper;

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
        taskListDataAccess.open();
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
                if (text.matches("")) {
                    editText.setError(getResources().getString(R.string.task_list_new_list_error));
                    return;
                }
                int position = taskListRecyclerViewAdapter.getItemCount();

                TaskList taskList = taskListDataAccess.createTaskList(text, position);
                taskListRecyclerViewAdapter.add(taskList, position);

                editText.setText("");
                toggleVisibleCreateNewTaskListLayout(mView);
            }
        });

        return mView;
    }

    @Override
    public void onPause() {
        clearFocus();
        super.onPause();
        taskListDataAccess.close();
    }

    @Override
    public void onResume() {
        super.onResume();
        clearFocus();
        taskListDataAccess.open();
    }

    private void toggleVisibleCreateNewTaskListLayout(View view) {
        LinearLayout layout = (LinearLayout) view.findViewById(R.id.new_task_list_layout);
        int taskListCount = taskListRecyclerViewAdapter.getItemCount();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String maxTaskListsString = sharedPref.getString("pref_conf_max_lists", "3");
        int maxTaskLists = Integer.valueOf(maxTaskListsString);
        if (taskListCount >= maxTaskLists) layout.setVisibility(View.GONE);
        else layout.setVisibility(View.VISIBLE);
        clearFocus();
    }

    @Override
    public void onEditTextLoseFocus(TaskList taskList) {
        taskListDataAccess.updateName(taskList.getId(), taskList.getName());
    }

    @Override
    public void onClickDeleteButton(int position, long id) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());

        if(sharedPref.getBoolean("pref_conf_tasklist_del", true)) {
            String title = getResources().getString(R.string.task_list_confirmation_delete);
            ConfirmDialogFragment confirmDialogFragment =
                    ConfirmDialogFragment.newInstance(title, this);
            Bundle args = new Bundle();
            args.putInt("ItemPosition", position);
            args.putLong("ItemId", id);
            confirmDialogFragment.setArguments(args);
            confirmDialogFragment.show(getFragmentManager(), title);
        }
        else deleteTaskList(position, id);
    }

    @Override
    public void onConfirmDialogClick(DialogFragment dialog, ConfirmDialogFragment.ButtonEvent event) {
        // Handle never ask again checkbox
        CheckBox neverAskAgainCheckBox = (CheckBox) dialog.getDialog().findViewById(R.id.task_confirmation_never);
        if (neverAskAgainCheckBox.isChecked()) {

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor editor = sharedPref.edit();

            editor.putBoolean("pref_conf_tasklist_del", false);
            editor.apply();
        }

        if (event == ConfirmDialogFragment.ButtonEvent.NO) return;

        Bundle args = dialog.getArguments();
        deleteTaskList(args.getInt("ItemPosition"), args.getLong("ItemId"));
    }

    @Override
    public void onItemMove(long fromTaskId, long toTaskId, int fromPosition, int toPosition) {
        taskListDataAccess.updateOrder(fromTaskId, toPosition);
        taskListDataAccess.updateOrder(toTaskId, fromPosition);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    private void deleteTaskList(int position, long id)
    {
        taskListRecyclerViewAdapter.remove(position);
        taskListDataAccess.deleteTaskList(id);
        toggleVisibleCreateNewTaskListLayout(mView);
    }

    /** Helper method to clear focus by giving it to the parent layout */
    private void clearFocus() {
        View view = getView();
        if (view != null) {
            view.requestFocus();

            // Hide keyboard
            InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public class GetTaskListsTask extends AsyncTask<TaskListDataAccess, Void, List<TaskList>> {
        @Override
        protected List<TaskList> doInBackground(TaskListDataAccess... params) {
            TaskListDataAccess taskListDataAccess = params[0];
            return taskListDataAccess.getAllTaskLists();
        }

        @Override
        protected void onPostExecute(List<TaskList> taskLists) {
            super.onPostExecute(taskLists);
            taskListRecyclerViewAdapter =
                    new TaskListRecyclerViewAdapter(taskLists, TaskListsFragment.this);

            // Set the adapter
            Context context = mView.getContext();
            RecyclerView recyclerView = (RecyclerView) mView.findViewById(R.id.task_lists_view);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(taskListRecyclerViewAdapter);

            // Set the Touch Helper
            ItemTouchHelper.Callback callback = new TaskListTouchHelper(taskListRecyclerViewAdapter);
            mItemTouchHelper = new ItemTouchHelper(callback);
            mItemTouchHelper.attachToRecyclerView(recyclerView);

            toggleVisibleCreateNewTaskListLayout(mView);
        }
    }
}
