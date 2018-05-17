package com.dev.tasker.extensions

import android.support.annotation.StringRes
import android.support.v4.app.FragmentActivity
import android.widget.Toast

fun FragmentActivity.showToast(@StringRes resId: Int) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
}

fun FragmentActivity.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}