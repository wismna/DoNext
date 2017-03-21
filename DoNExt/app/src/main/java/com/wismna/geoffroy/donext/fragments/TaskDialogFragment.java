package com.wismna.geoffroy.donext.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.wismna.geoffroy.donext.R;
import com.wismna.geoffroy.donext.dao.Task;
import com.wismna.geoffroy.donext.dao.TaskList;

import org.joda.time.LocalDate;

import java.util.List;

/**
 * Created by geoffroy on 15-11-26.
 * Represents a New or Edit Task dialog
 */
@Deprecated
public class TaskDialogFragment extends DialogFragment {

    public Task getTask() {
        return task;
    }

    /** The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    interface NewTaskListener {
        void onNewTaskDialogPositiveClick(DialogFragment dialog, View dialogView);
        void onNewTaskDialogNeutralClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    private NewTaskListener mListener;
    private Task task;
    private List<TaskList> taskLists;

    public static TaskDialogFragment newInstance(Task task, List<TaskList> taskLists, NewTaskListener newTaskListener) {
        TaskDialogFragment fragment = new TaskDialogFragment();
        fragment.task = task;
        fragment.taskLists = taskLists;
        fragment.mListener = newTaskListener;
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // This part is only needed on small layouts (large layouts use onCreateDialog)
        if (!getArguments().getBoolean("layout")) {
            View view = inflater.inflate(R.layout.fragment_task_form, container, false);
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.setSupportActionBar(setToolbarTitle(view));

            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeButtonEnabled(true);
                actionBar.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel);
            }
            setHasOptionsMenu(true);
            setTaskValues(view);
            return view;
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Inflate and set the layout for the dialog
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.fragment_task_form, null);
        setToolbarTitle(view);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.new_task_save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onPositiveButtonClick(view);
                    }
                })
                .setNegativeButton(R.string.new_task_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the negative button event back to the host activity
                        // Canceled creation, nothing to do
                        //dialog.cancel();
                        onNegativeButtonClick();
                    }
                });
        if (task != null) {
            builder.setNeutralButton(R.string.new_task_delete, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    onNeutralButtonClick();
                }
            });
        }
        setTaskValues(view);
        return builder.create();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        getActivity().getMenuInflater().inflate(R.menu.menu_dynamic_fragment, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (task == null) {
            menu.removeItem(R.id.menu_neutral_button);
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Determine which menu item was clicked
        int id = item.getItemId();
        View view = getView();

        // Hide the keyboard if present
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
        if (id == R.id.menu_positive_button) {
            // handle save button click here
            onPositiveButtonClick(view);
            return true;
        }
        else if (id == R.id.menu_neutral_button) {
            // handle delete button click here
            onNeutralButtonClick();
            return true;
        }
        else if (id == android.R.id.home) {
            // handle close button click here
            onNegativeButtonClick();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();
        // Stop the dialog from being dismissed on rotation, due to a bug with the compatibility library
        // https://code.google.com/p/android/issues/detail?id=17423
        if (dialog != null && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }
        super.onDestroyView();
    }

    private void setTaskValues(View view) {
        // Get date picker
        final DatePicker dueDatePicker = (DatePicker) view.findViewById(R.id.new_task_due_date);

        // Populate spinner with task lists
        Spinner spinner = (Spinner) view.findViewById(R.id.new_task_list);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<TaskList> adapter = new ArrayAdapter<>(
                getActivity(), android.R.layout.simple_spinner_item, taskLists);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Auto set list value to current tab
        Bundle args = getArguments();
        int id = args.getInt("list");
        spinner.setSelection(id);

        CheckBox checkBox = (CheckBox) view.findViewById(R.id.new_task_today);
        TextView todayLabel = (TextView) view.findViewById(R.id.new_task_today_label);
        boolean isTodayActive = args.getBoolean("today");
        checkBox.setVisibility(isTodayActive ? View.VISIBLE : View.GONE);
        todayLabel.setVisibility(isTodayActive ? View.VISIBLE : View.GONE);

        // Set other properties if they exist
        if (task != null) {

            EditText titleText = (EditText) view.findViewById(R.id.new_task_name);
            titleText.setText(task.getName());
            EditText descText = (EditText) view.findViewById(R.id.new_task_description);
            descText.setText(task.getDescription());
            SeekBar seekBar = (SeekBar) view.findViewById(R.id.new_task_priority);
            seekBar.setProgress(task.getPriority());

            // Set Due Date
            LocalDate dueDate = task.getDueDate();
            dueDatePicker.updateDate(dueDate.getYear(), dueDate.getMonthOfYear() - 1, dueDate.getDayOfMonth());

            checkBox.setChecked(task.isToday());
        }
        else {
            // Disallow past dates on new tasks
            dueDatePicker.setMinDate(LocalDate.now().toDate().getTime());
        }
    }

    private Toolbar setToolbarTitle(View view) {
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.dialog_toolbar);
        toolbar.setTitle(getTag());
        return toolbar;
    }

    protected void onPositiveButtonClick(View view) {
        if (view == null) return;
        EditText titleText = (EditText) view.findViewById(R.id.new_task_name);
        // handle confirmation button click hereEditText titleText = (EditText) d.findViewById(R.id.new_task_name);
        if (titleText.getText().toString().matches(""))
            titleText.setError(getResources().getString(R.string.new_task_name_error));
        else {
            // Send the positive button event back to the host activity
            mListener.onNewTaskDialogPositiveClick(TaskDialogFragment.this, view);
            dismiss();
        }
    }

    protected /*abstract*/ void onNeutralButtonClick() {
        mListener.onNewTaskDialogNeutralClick(TaskDialogFragment.this);
    }

    protected /*abstract*/ void onNegativeButtonClick() {
        dismiss();
    }
}
