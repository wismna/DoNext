package com.wismna.geoffroy.donext.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;

import com.wismna.geoffroy.donext.R;
import com.wismna.geoffroy.donext.adapters.TaskAdapter;

public class ConfirmDialogFragment extends DialogFragment {
    public interface ConfirmDialogListener {
        void onConfirmDialogPositiveClick(DialogFragment dialog);
        void onConfirmDialogNeutralClick(DialogFragment dialog);
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
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        // Allows refreshing the first item of the adapter
        Bundle args = getArguments();
        int itemPosition = args.getInt("ItemPosition");

        getTaskAdapter().notifyItemChanged(itemPosition);
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
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
            .setPositiveButton(R.string.task_confirmation_yes_button, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    confirmDialogListener.onConfirmDialogPositiveClick(ConfirmDialogFragment.this);
                }
            })
            .setNegativeButton(R.string.task_confirmation_no_button, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                    ConfirmDialogFragment.this.getDialog().cancel();
                }
            }).setNeutralButton(R.string.task_confirmation_never_button, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    confirmDialogListener.onConfirmDialogNeutralClick(ConfirmDialogFragment.this);
                }
            })
            .setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    //return false;
                    return keyCode != KeyEvent.KEYCODE_BACK;
                }
            });
        // Create the AlertDialog object and return it
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }
}
