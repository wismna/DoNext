package com.wismna.geoffroy.donext.fragments

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.wismna.geoffroy.donext.R

/**
 * Created by GBE on 27/12/2017.
 * Shows the About page
 */
class AboutDialogFragment : DynamicDialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mButtonCount = 1
        mNegativeButtonString = getString(R.string.task_list_ok)
        mContentLayoutId = R.layout.content_about
    }

    override fun onStart() {
        super.onStart()
        val resources = resources
        try {
            val context = context
            val pInfo = context!!.packageManager.getPackageInfo(context.packageName, 0)
            val versionDonext = findViewById<TextView>(R.id.version_donext)
            versionDonext.text = resources.getString(R.string.about_version_donext, pInfo.versionName)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        val versionAndroid = findViewById<TextView>(R.id.version_android)
        versionAndroid.text = resources.getString(R.string.about_version_android, Build.VERSION.SDK_INT)
    }

    override fun onPositiveButtonClick(view: View?) {
        // Not implemented
    }

    override fun onNeutralButtonClick(view: View?) {
        // Not implemented
    }

    override fun onNegativeButtonClick() {
        dismiss()
    }
}
