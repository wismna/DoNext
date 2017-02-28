package com.wismna.geoffroy.donext.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.wismna.geoffroy.donext.R;
import com.wismna.geoffroy.donext.dao.Task;
import com.wismna.geoffroy.donext.dao.TaskList;

import java.util.List;

/**
 * Created by geoffroy on 15-11-26.
 * Represents a New or Edit Task dialog
 */
public class TaskDialogFragment extends DialogFragment {

    public Task getTask() {
        return task;
    }

    /** The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NewTaskListener {
        void onNewTaskDialogPositiveClick(DialogFragment dialog);
        void onNewTaskDialogNeutralClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    private NewTaskListener mListener;
    private Task task;
    private List<TaskList> taskLists;

    public static TaskDialogFragment newInstance(Task task, List<TaskList> taskLists, NewTaskListener newTaskListener) {

        Bundle args = new Bundle();
        TaskDialogFragment fragment = new TaskDialogFragment();
        fragment.setArguments(args);
        fragment.task = task;
        fragment.taskLists = taskLists;
        fragment.mListener = newTaskListener;
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_task_form, null);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
            // Add action buttons
            .setPositiveButton(R.string.new_task_save, null)
            .setNegativeButton(R.string.new_task_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // Send the negative button event back to the host activity
                    // Canceled creation, nothing to do
                    TaskDialogFragment.this.getDialog().cancel();
                }
            });

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

        // Set other properties if they exist
        if (task != null) {
            EditText titleText = (EditText) view.findViewById(R.id.new_task_name);
            titleText.setText(task.getName());
            EditText descText = (EditText) view.findViewById(R.id.new_task_description);
            descText.setText(task.getDescription());
            RadioGroup priorityGroup = (RadioGroup) view.findViewById(R.id.new_task_priority);
            switch (task.getPriority()) {
                case 0:
                    priorityGroup.check(R.id.new_task_priority_low);
                    break;
                case 1:
                    priorityGroup.check(R.id.new_task_priority_normal);
                    break;
                case 2:
                    priorityGroup.check(R.id.new_task_priority_high);
                    break;
            }

            builder.setNeutralButton(R.string.new_task_delete, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mListener.onNewTaskDialogNeutralClick(TaskDialogFragment.this);
                }
            });
            setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light);
        }
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        final AlertDialog d = (AlertDialog) getDialog();
        if(d != null)
        {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    EditText titleText = (EditText) d.findViewById(R.id.new_task_name);
                    if (titleText.getText().toString().matches(""))
                        titleText.setError(getResources().getString(R.string.new_task_name_error));
                    else
                    {
                        // Send the positive button event back to the host activity
                        mListener.onNewTaskDialogPositiveClick(TaskDialogFragment.this);
                        dismiss();
                    }
                }
            });
        }
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
}
