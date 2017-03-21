package com.wismna.geoffroy.donext.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.wismna.geoffroy.donext.R;
import com.wismna.geoffroy.donext.dao.Task;

import java.util.List;

/**
 * Created by bg45 on 2017-03-21.
 * This is the Today Form dynamic dialog fragment
 */

public class TodayFormDialogFragment extends DynamicDialogFragment {
    /** The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface TodayTaskListener {
        void onTodayTaskDialogPositiveClick(DialogFragment dialog, View dialogView);
    }

    private TodayFormDialogFragment.TodayTaskListener mListener;
    private List<Task> tasks;

    public static TodayFormDialogFragment newInstance(List<Task> tasks, TodayTaskListener todayTaskListener) {
        TodayFormDialogFragment fragment = new TodayFormDialogFragment();
        fragment.tasks = tasks;
        fragment.mListener = todayTaskListener;
        fragment.setRetainInstance(true);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContentFragment = new TodayFormContentFragment();
        Bundle args = getArguments();
        if (args != null) {
            mIsLargeLayout = args.getBoolean("layout");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        setLayoutValues(getView());
    }

    private void setLayoutValues(View view) {
        EditText editText = (EditText) view.findViewById(R.id.today_search);
        ListView listView = (ListView) view.findViewById(R.id.today_tasks);
        final ArrayAdapter<Task> adapter = new ArrayAdapter<>(getActivity(), R.layout.list_task_item, tasks);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Set as selected
                Task task = tasks.get(position);
                view.setSelected(!view.isSelected());
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    protected void onPositiveButtonClick(View view) {
        mListener.onTodayTaskDialogPositiveClick(TodayFormDialogFragment.this, view);
    }

    @Override
    protected void onNeutralButtonClick(View view) {

    }

    @Override
    protected void onNegativeButtonClick() {
        dismiss();
    }
}
