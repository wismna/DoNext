package com.wismna.geoffroy.donext.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.wismna.geoffroy.donext.R;

/**
 * Created by gbe on 17-12-19.
 * History Activity class
 */
public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        ActionBar toolbar = getSupportActionBar();

        // Show back button on toolbar
        assert toolbar != null;
        toolbar.setDisplayHomeAsUpEnabled(true);
        toolbar.setDisplayShowHomeEnabled(true);
    }
}
