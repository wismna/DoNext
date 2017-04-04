package com.wismna.geoffroy.donext.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wismna.geoffroy.donext.R;

/**
 * Created by bg45 on 2017-03-21.
 * Content fragment for the Task Form Dialog fragment.
 */

public class TaskFormContentFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_task_form, container, false);
    }
}
