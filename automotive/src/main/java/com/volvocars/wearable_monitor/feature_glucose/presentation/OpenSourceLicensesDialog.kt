package com.volvocars.wearable_monitor.feature_glucose.presentation

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.webkit.WebView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity

class OpenSourceLicensesDialog : DialogFragment() {

    fun showLicenses(activity: FragmentActivity) {
        val fragmentManager = activity.supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        val previousFragment = fragmentManager.findFragmentByTag("dialog_licenses")
        if (previousFragment != null) {
            fragmentTransaction.remove(previousFragment)
        }
        fragmentTransaction.addToBackStack(null)

        show(fragmentManager, "dialog_licenses")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val webView = WebView(requireActivity())
        webView.loadUrl("file:///android_asset/open_source_licenses.html")

        return AlertDialog.Builder(requireActivity())
            .setTitle("Open Source Licenses")
            .setView(webView)
            .setPositiveButton(
                "OK"
            ) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
            .create()
    }
}