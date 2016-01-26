package com.wismna.geoffroy.donext.activities;

import android.os.Build;
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

        TextView versionDonext = (TextView) findViewById(R.id.version_donext);
        versionDonext.setText(getResources().getString(R.string.about_version_donext, BuildConfig.VERSION_NAME));

        TextView versionAndroid = (TextView) findViewById(R.id.version_android);
        versionAndroid.setText(getResources().getString(R.string.about_version_android, Build.VERSION.SDK_INT));

    }
}
