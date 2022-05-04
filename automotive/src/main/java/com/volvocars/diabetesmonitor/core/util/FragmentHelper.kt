package com.volvocars.diabetesmonitor.core.util

import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

fun FragmentManager.showFragment(fragment: Fragment, containerView: View) {
    containerView.isVisible = true
    this.beginTransaction()
        .replace(containerView.id, fragment)
        .commit()
}

fun FragmentManager.tryRemoveFragment(containerView: View): Boolean {
    val fragment = this.findFragmentById(containerView.id)
    if (fragment != null) {
        containerView.isVisible = false
        this.beginTransaction()
            .remove(fragment)
            .commit()
        return true
    }
    return false
}
