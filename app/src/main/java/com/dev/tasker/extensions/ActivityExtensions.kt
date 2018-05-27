package com.dev.tasker.extensions

import android.content.Context
import android.support.annotation.StringRes
import android.support.v4.app.FragmentActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast

fun FragmentActivity.showToast(@StringRes resId: Int) {
    Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
}

fun FragmentActivity.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun FragmentActivity.showKeyboard(view: View) {
    // Show soft keyboard for the user to enter the value.
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
}