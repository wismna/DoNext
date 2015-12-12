package com.wismna.geoffroy.donext.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;

import com.wismna.geoffroy.donext.R;
import com.wismna.geoffroy.donext.adapters.TaskAdapter;

public class ConfirmDialogFragment extends DialogFragment {
    public interface ConfirmDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNeutralClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);
    }

    private ConfirmDialogListener confirmDialogListener;
    private TaskAdapter taskAdapter;
    private RecyclerView recyclerView;
    private String message;

    public static ConfirmDialogFragment newInstance(
            TaskAdapter taskAdapter, String message, RecyclerView recyclerView) {

        Bundle args = new Bundle();

        ConfirmDialogFragment fragment = new ConfirmDialogFragment();
        fragment.taskAdapter = taskAdapter;
        fragment.message = message;
        fragment.recyclerView = recyclerView;
        fragment.setArguments(args);
        return fragment;
    }

    public TaskAdapter getTaskAdapter() {
        return taskAdapter;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            confirmDialogListener = (ConfirmDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NewTaskListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // TODO: Handle on dismiss or similar
        builder.setMessage(getResources().getString(R.string.settings_confirm_message) + " " + message + "?")
                .setPositiveButton(R.string.task_swipe_confirmation_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        confirmDialogListener.onDialogPositiveClick(ConfirmDialogFragment.this);
                    }
            })
            .setNegativeButton(R.string.task_swipe_confirmation_no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                    confirmDialogListener.onDialogNegativeClick(ConfirmDialogFragment.this);
                }
            }).setNeutralButton(R.string.task_swipe_confirmation_never, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    confirmDialogListener.onDialogNeutralClick(ConfirmDialogFragment.this);
                }
            }
        );
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
