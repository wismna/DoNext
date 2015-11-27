package com.wismna.geoffroy.donext.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.wismna.geoffroy.donext.R;
import com.wismna.geoffroy.donext.dao.TaskList;
import com.wismna.geoffroy.donext.database.TasksDataAccess;

import java.util.List;

/**
 * Created by geoffroy on 15-11-26.
 */
public class NewTaskFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.new_task, null);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
            // Add action buttons
            .setPositiveButton(R.string.new_task_save, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // TODO: create the task in DB
                }
            })
            .setNegativeButton(R.string.new_task_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    NewTaskFragment.this.getDialog().cancel();
                }
            });

        // Access database to retrieve task lists
        TasksDataAccess dataAccess = new TasksDataAccess(getActivity());
        dataAccess.open();

        // Populate spinner with task lists
        Spinner spinner = (Spinner) view.findViewById(R.id.new_task_list);
        // Create an ArrayAdapter using the string array and a default spinner layout
        List<TaskList> taskLists = dataAccess.getAllTaskLists();
        ArrayAdapter<TaskList> adapter = new ArrayAdapter<>(
                getActivity(), android.R.layout.simple_spinner_item, taskLists);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Auto set list value to current tab
        Bundle args = getArguments();
        int id = args.getInt("list");
        spinner.setSelection(id);

        return builder.create();
    }
}
