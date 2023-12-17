package com.wismna.geoffroy.donext.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.wismna.geoffroy.donext.R
import java.util.Objects

/**
 * Created by wismna on 2017-03-21.
 * Sub-class this class to create a dynamic fragment that will act as a Dialog in large layouts and
 * a full screen fragment in smaller layouts.
 */
abstract class DynamicDialogFragment : DialogFragment() {
    var mButtonCount = 2
    var mPositiveButtonString = ""
    var mNeutralButtonString = ""
    var mNegativeButtonString = ""
    var mContentLayoutId = 0
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // This part is only needed on small layouts (large layouts use onCreateDialog)
        if (!showsDialog) {
            val view = inflater.inflate(R.layout.fragment_dynamic_dialog, container, false)
            val activity = requireActivity() as AppCompatActivity
            //activity.setSupportActionBar(setToolbarTitle(view));
            val actionBar = activity.supportActionBar
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true)
                actionBar.setHomeButtonEnabled(true)
                actionBar.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel)
            }
            setHasOptionsMenu(true)
            insertContentView(view, inflater)
            return view
        }
        // This basically returns null
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Inflate and set the layout for the dialog
        val activity: Activity = requireActivity()
        val inflater = activity.layoutInflater
        // As it is a Dialog, the root ViewGroup can be null without issues
        val view = inflater.inflate(R.layout.fragment_dynamic_dialog, null)
        val builder = AlertDialog.Builder(activity)
        // Add action buttons
        builder.setView(view)
                .setNegativeButton(mNegativeButtonString) { dialog: DialogInterface?, id: Int ->
                    // Send the negative button event back to the host activity
                    // Canceled creation, nothing to do
                    onNegativeButtonClick()
                }
        if (mButtonCount >= 2) {
            builder.setPositiveButton(mPositiveButtonString) { dialog: DialogInterface?, id: Int -> onPositiveButtonClick(view) }
        }
        if (mButtonCount == 3) {
            builder.setNeutralButton(mNeutralButtonString) { dialog: DialogInterface?, which: Int -> onNeutralButtonClick(view) }
        }
        setToolbarTitle(view)
        insertContentView(view, inflater)
        return builder.create()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        requireActivity().menuInflater.inflate(R.menu.menu_dynamic_fragment, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        /*switch (mButtonCount) {
            case 1:
                menu.removeItem(R.id.menu_positive_button);
                menu.removeItem(R.id.menu_neutral_button);
                break;
            case 2:
                menu.removeItem(R.id.menu_neutral_button);
                menu.findItem(R.id.menu_positive_button).setTitle(args.getString("button_positive"));
                break;
            case 3:
                menu.findItem(R.id.menu_neutral_button).setTitle(args.getString("button_neutral"));
                menu.findItem(R.id.menu_positive_button).setTitle(args.getString("button_positive"));
                break;
        }*/

        // Hide buttons depending on count
        when (mButtonCount) {
            1 -> {
                menu.removeItem(R.id.menu_positive_button)
                menu.removeItem(R.id.menu_neutral_button)
            }

            2 -> menu.removeItem(R.id.menu_neutral_button)
        }
        when (mButtonCount) {
            3 -> {
                menu.findItem(R.id.menu_neutral_button).setTitle(mNeutralButtonString)
                menu.findItem(R.id.menu_positive_button).setTitle(mPositiveButtonString)
            }

            2 -> menu.findItem(R.id.menu_positive_button).setTitle(mPositiveButtonString)
        }
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Determine which menu item was clicked
        val id = item.itemId
        val view = view

        // Hide the keyboard if present
        if (view != null) {
            val imm = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)
        }
        when (id) {
            R.id.menu_positive_button -> {
                // Handle positive button click here
                onPositiveButtonClick(view)
                return true
            }
            R.id.menu_neutral_button -> {
                // Handle neutral button click here
                onNeutralButtonClick(view)
                return true
            }
            android.R.id.home -> {
                // Handle negative button click here
                onNegativeButtonClick()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
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

    fun showFragment(fragmentManager: FragmentManager, title: String?, isLargeLayout: Boolean) {
        if (isLargeLayout) show(fragmentManager, title) else {
            // The device is smaller, so show the fragment fullscreen
            val transaction = fragmentManager.beginTransaction()
            // For a little polish, specify a transition animation
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            // To make it fullscreen, use the 'content' root view as the container
            // for the fragment, which is always the root view for the activity
            transaction.add(android.R.id.content, this, title)
                    .addToBackStack(null).commit()
        }
    }

    /** Helper function to get a View, without having to worry about the fact that is a Dialog or not */
    protected fun <T : View?> findViewById(id: Int): T {
        return if (showsDialog) requireDialog().findViewById(id) else requireView().findViewById(id)
    }

    /** Helper method to clear focus by giving it to the parent layout  */
    protected fun clearFocus() {
        val view = view
        if (view != null) {
            view.requestFocus()

            // Hide keyboard
            val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            Objects.requireNonNull(inputMethodManager).hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    /** Sets the title of the Fragment from the Tag  */
    private fun setToolbarTitle(view: View): Toolbar {
        val toolbar = view.findViewById<Toolbar>(R.id.dialog_toolbar)
        toolbar.title = tag
        return toolbar
    }

    /** Inserts the actual contents in the content Frame Layout  */
    private fun insertContentView(view: View, inflater: LayoutInflater) {
        // Ensure that the content view is set
        if (mContentLayoutId == 0) return
        // Insert the content view
        val content = view.findViewById<FrameLayout>(R.id.dynamic_fragment_content)
        content.addView(inflater.inflate(mContentLayoutId, view.parent as ViewGroup))
    }

    protected abstract fun onPositiveButtonClick(view: View?)
    protected abstract fun onNeutralButtonClick(view: View?)
    protected abstract fun onNegativeButtonClick()
}
