package com.darcy.lib_access_skip.exts

import android.widget.TextView
import androidx.core.content.ContextCompat
import com.darcy.lib_access_skip.R

fun TextView.setTextViewColorRed() {
    this.setTextColor(ContextCompat.getColor(this.context, R.color.red))
}

fun TextView.setTextViewColorGreen() {
    this.setTextColor(ContextCompat.getColor(this.context, R.color.green))
}