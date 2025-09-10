package com.wismna.geoffroy.donext.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by GBE on 27/12/2017.
 * Defines a template for sub activities
 */

public abstract class ToolBarActivityBase extends AppCompatActivity {
    protected void initToolBar() {
        ActionBar toolbar = getSupportActionBar();

        // Show back button on toolbar
        assert toolbar != null;
        toolbar.setDisplayHomeAsUpEnabled(true);
        toolbar.setDisplayShowHomeEnabled(true);
    }
}
