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
 * Contains the Today Form contents.
 */

public class TodayFormContentFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_today_form, container, false);
    }
}
