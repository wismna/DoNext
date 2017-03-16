package com.wismna.geoffroy.donext.fragments;

import android.app.Dialog;
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
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.wismna.geoffroy.donext.R;
import com.wismna.geoffroy.donext.dao.Task;
import com.wismna.geoffroy.donext.dao.TaskList;

import org.joda.time.LocalDate;

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
    interface NewTaskListener {
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_form, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.new_task_toolbar);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel);
        }
        setHasOptionsMenu(true);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        /*builder.setView(view)
            // Add action buttons
            .setPositiveButton(R.string.new_task_save, null)
            .setNegativeButton(R.string.new_task_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // Send the negative button event back to the host activity
                    // Canceled creation, nothing to do
                    TaskDialogFragment.this.getDialog().cancel();
                }
            });*/

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
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Set due date
                boolean isRestricted = taskLists.get(position).getName()
                        .equals(getString(R.string.task_list_today));
                dueDatePicker.setEnabled(!isRestricted);
                if (isRestricted) {
                    LocalDate today = LocalDate.now();
                    dueDatePicker.updateDate(today.getYear(), today.getMonthOfYear() - 1, today.getDayOfMonth());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Auto set list value to current tab
        Bundle args = getArguments();
        int id = args.getInt("list");
        spinner.setSelection(id);

        // Set other properties if they exist
        if (task != null) {
            toolbar.setTitle(R.string.action_edit_task);

            EditText titleText = (EditText) view.findViewById(R.id.new_task_name);
            titleText.setText(task.getName());
            EditText descText = (EditText) view.findViewById(R.id.new_task_description);
            descText.setText(task.getDescription());
            SeekBar seekBar = (SeekBar) view.findViewById(R.id.new_task_priority);
            seekBar.setProgress(task.getPriority());

            // Set Due Date
            LocalDate dueDate = task.getDueDate();
            dueDatePicker.updateDate(dueDate.getYear(), dueDate.getMonthOfYear() - 1, dueDate.getDayOfMonth());

            // Add the Delete button
/*            builder.setNeutralButton(R.string.new_task_delete, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mListener.onNewTaskDialogNeutralClick(TaskDialogFragment.this);
                }
            });*/
        }
        else {
            toolbar.setTitle(R.string.action_new_task);
            // Disallow past dates on new tasks
            dueDatePicker.setMinDate(LocalDate.now().toDate().getTime());
        }
        return view;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        final Dialog d = (Dialog) getDialog();
        if(d != null)
        {
            //d.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            /*Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
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
            });*/
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        getActivity().getMenuInflater().inflate(R.menu.menu_new_task, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (task == null) {
            menu.removeItem(R.id.menu_new_task_delete);
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_new_task_save) {
            EditText titleText = (EditText) getDialog().findViewById(R.id.new_task_name);
            // handle confirmation button click hereEditText titleText = (EditText) d.findViewById(R.id.new_task_name);
            if (titleText.getText().toString().matches(""))
                titleText.setError(getResources().getString(R.string.new_task_name_error));
            else
            {
                // Send the positive button event back to the host activity
                mListener.onNewTaskDialogPositiveClick(TaskDialogFragment.this);
                dismiss();
            }
            return true;
        }
        else if (id == R.id.menu_new_task_delete) {
                // handle confirmation button click here
                mListener.onNewTaskDialogNeutralClick(TaskDialogFragment.this);
                dismiss();
                return true;
            }
        else if (id == android.R.id.home) {
            // handle close button click here
            dismiss();
            return true;
        }
        dismiss();

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
}
