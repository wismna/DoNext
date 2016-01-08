package com.wismna.geoffroy.donext.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;

import com.wismna.geoffroy.donext.R;

public class ConfirmDialogFragment extends DialogFragment {
    public interface ConfirmDialogListener {
        void onConfirmDialogClick(DialogFragment dialog, ButtonEvent event);
    }

    public enum ButtonEvent{
        YES,
        NO
    }
    private ConfirmDialogListener confirmDialogListener;
    private String message;

    public static ConfirmDialogFragment newInstance(String message, ConfirmDialogListener confirmDialogListener) {
        ConfirmDialogFragment fragment = new ConfirmDialogFragment();
        fragment.message = message;
        fragment.confirmDialogListener = confirmDialogListener;
        return fragment;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        // Allows refreshing the first item of the adapter
        confirmDialogListener.onConfirmDialogClick(this, ButtonEvent.NO);
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_task_confirmation, null);
        builder.setView(view).setMessage(message)
            .setPositiveButton(R.string.task_confirmation_yes_button, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    confirmDialogListener.onConfirmDialogClick(ConfirmDialogFragment.this, ButtonEvent.YES);
                }
            })
            .setNegativeButton(R.string.task_confirmation_no_button, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                    ConfirmDialogFragment.this.getDialog().cancel();
                }
            })
            .setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    return keyCode != KeyEvent.KEYCODE_BACK;
                }
            });
        // Create the AlertDialog object and return it
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }
}
