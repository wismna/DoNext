package com.wismna.geoffroy.donext.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.wismna.geoffroy.donext.BuildConfig;
import com.wismna.geoffroy.donext.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView version = (TextView) findViewById(R.id.version);
        version.setText(getResources().getString(R.string.about_version, BuildConfig.VERSION_NAME));
    }
}
