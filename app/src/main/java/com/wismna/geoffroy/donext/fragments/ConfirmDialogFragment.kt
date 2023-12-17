package com.wismna.geoffroy.donext.fragments

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.wismna.geoffroy.donext.R

class ConfirmDialogFragment : DialogFragment() {
    interface ConfirmDialogListener {
        fun onConfirmDialogClick(dialog: DialogFragment, event: ButtonEvent)
    }

    enum class ButtonEvent {
        YES,
        NO
    }

    private var confirmDialogListener: ConfirmDialogListener? = null
    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)

        // Allows refreshing the first item of the adapter
        confirmDialogListener!!.onConfirmDialogClick(this, ButtonEvent.NO)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val args = requireArguments()
        val inflater = requireActivity().layoutInflater
        // No need for a parent in a Dialog Fragment
        val view = inflater.inflate(R.layout.fragment_task_confirmation, null)
        builder.setView(view).setMessage(args.getString("message"))
                .setPositiveButton(args.getInt("button")) { dialog: DialogInterface?, id: Int -> confirmDialogListener!!.onConfirmDialogClick(this@ConfirmDialogFragment, ButtonEvent.YES) }
                .setNegativeButton(R.string.task_confirmation_no_button) { dialog: DialogInterface?, id: Int ->
                    // User cancelled the dialog
                    requireDialog().cancel()
                }
                .setOnKeyListener { dialog: DialogInterface?, keyCode: Int, event: KeyEvent? -> keyCode != KeyEvent.KEYCODE_BACK }
        // Create the AlertDialog object and return it
        val dialog: Dialog = builder.create()
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    override fun onDestroyView() {
        val dialog = dialog
        // Stop the dialog from being dismissed on rotation, due to a bug with the compatibility library
        // https://code.google.com/p/android/issues/detail?id=17423
        if (dialog != null && retainInstance) {
            dialog.setDismissMessage(null)
        }
        super.onDestroyView()
    }

    companion object {
        fun newInstance(confirmDialogListener: ConfirmDialogListener?): ConfirmDialogFragment {
            val fragment = ConfirmDialogFragment()
            fragment.confirmDialogListener = confirmDialogListener
            fragment.retainInstance = true
            return fragment
        }
    }
}
