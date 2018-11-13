package com.wismna.geoffroy.donext.fragments;

import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.wismna.geoffroy.donext.BuildConfig;
import com.wismna.geoffroy.donext.R;

/**
 * Created by GBE on 27/12/2017.
 * Shows the About page
 */

public class AboutDialogFragment extends DynamicDialogFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mButtonCount = 1;
        mNegativeButtonString = getString(R.string.task_list_ok);
        mContentLayoutId = R.layout.content_about;
    }

    @Override
    public void onStart() {
        super.onStart();

        TextView versionDonext = findViewById(R.id.version_donext);
        Resources resources = getResources();
        versionDonext.setText(resources.getString(R.string.about_version_donext, BuildConfig.VERSION_NAME));

        TextView versionAndroid = findViewById(R.id.version_android);
        versionAndroid.setText(resources.getString(R.string.about_version_android, Build.VERSION.SDK_INT));
    }

    @Override
    protected void onPositiveButtonClick(View view) {
        // Not implemented
    }

    @Override
    protected void onNeutralButtonClick(View view) {
        // Not implemented
    }

    @Override
    protected void onNegativeButtonClick() {
        dismiss();
    }
}
