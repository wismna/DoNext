package com.wismna.geoffroy.donext.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
 * Created by bg45 on 2017-03-21.
 * This is Task Form dynamic dialog fragment
 */

public class TaskFormDialogFragment extends DynamicDialogFragment {
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

    private TaskFormDialogFragment.NewTaskListener mListener;
    private Task task;
    private List<TaskList> taskLists;

    public static TaskFormDialogFragment newInstance(Task task, List<TaskList> taskLists, NewTaskListener newTaskListener) {
        TaskFormDialogFragment fragment = new TaskFormDialogFragment();
        fragment.task = task;
        fragment.taskLists = taskLists;
        fragment.mListener = newTaskListener;
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContentLayoutId = R.layout.content_task_form;
        Bundle args = getArguments();
        if (args != null) {
            mHasNeutralButton = args.getBoolean("neutral");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Set Task Form specific information at that point because we are sure that the view is
        // entirely inflated (with the content fragment)
        setTaskValues();
    }

    @Override
    protected void onPositiveButtonClick(View view) {
        if (view == null) return;
        EditText titleText = (EditText) view.findViewById(R.id.new_task_name);
        // handle confirmation button click hereEditText titleText = (EditText) d.findViewById(R.id.new_task_name);
        if (titleText.getText().toString().matches(""))
            titleText.setError(getResources().getString(R.string.new_task_name_error));
        else {
            // Send the positive button event back to the host activity
            mListener.onNewTaskDialogPositiveClick(TaskFormDialogFragment.this, view);
            dismiss();
        }
    }

    @Override
    protected void onNeutralButtonClick(View view) {
        mListener.onNewTaskDialogNeutralClick(TaskFormDialogFragment.this);
    }

    @Override
    protected void onNegativeButtonClick() {
        dismiss();
    }

    private void setTaskValues() {
        // Populate spinner with task lists
        Spinner spinner = (Spinner) findViewById(R.id.new_task_list);
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

        CheckBox checkBox = (CheckBox) findViewById(R.id.new_task_today);
        TextView todayLabel = (TextView) findViewById(R.id.new_task_today_label);
        boolean isTodayActive = args.getBoolean("today");
        checkBox.setVisibility(isTodayActive ? View.VISIBLE : View.GONE);
        todayLabel.setVisibility(isTodayActive ? View.VISIBLE : View.GONE);

        // Get date picker
        final DatePicker dueDatePicker = (DatePicker) findViewById(R.id.new_task_due_date);
        // Handle due date spinner depending on check box
        CheckBox setDueDate = (CheckBox) findViewById(R.id.new_task_due_date_set);
        setDueDate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                dueDatePicker.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });

        // Handle priority changes
        final TextView tooltip = (TextView) findViewById(R.id.new_task_priority_tooltip);
        SeekBar seekBar = (SeekBar) findViewById(R.id.new_task_priority);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tooltip.setText(getResources().getStringArray(R.array.task_priority)[progress]);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                tooltip.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                tooltip.setVisibility(View.GONE);
            }
        });
        tooltip.setText(getResources().getStringArray(R.array.task_priority)[seekBar.getProgress()]);
        // Set other properties if they exist
        if (task != null) {

            EditText titleText = (EditText) findViewById(R.id.new_task_name);
            titleText.setText(task.getName());
            EditText descText = (EditText) findViewById(R.id.new_task_description);
            descText.setText(task.getDescription());

            seekBar.setProgress(task.getPriority());

            // Set Due Date
            LocalDate dueDate = task.getDueDate();
            if (dueDate != null) {
                setDueDate.setChecked(true);
                dueDatePicker.updateDate(dueDate.getYear(), dueDate.getMonthOfYear() - 1, dueDate.getDayOfMonth());
            }

            checkBox.setChecked(task.isToday());
        }
        else {
            // Disallow past dates on new tasks
            dueDatePicker.setMinDate(LocalDate.now().toDate().getTime());
        }
    }
}
