package com.wismna.geoffroy.donext.activities;

import android.os.Bundle;

import com.wismna.geoffroy.donext.R;

/**
 * Created by gbe on 17-12-19.
 * History Activity class
 */
public class HistoryActivity extends ToolBarActivityBase {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initToolBar();
    }
}
