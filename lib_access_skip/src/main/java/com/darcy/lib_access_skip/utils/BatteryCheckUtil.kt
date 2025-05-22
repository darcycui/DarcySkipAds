package com.darcy.lib_access_skip.utils

import android.content.Context
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.startActivity
import androidx.core.net.toUri
import com.darcy.lib_access_skip.exts.setTextViewColorGreen
import com.darcy.lib_access_skip.exts.setTextViewColorRed
import com.darcy.lib_access_skip.exts.toasts

object BatteryCheckUtil {
    fun checkBatteryOptimizationWithTextView(textView: TextView?, needRequestPermission: Boolean) {
        if (textView == null) return
        val context = textView.context
        val powerManager =
            context.applicationContext.getSystemService(POWER_SERVICE) as PowerManager
        if (!powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
            textView.text = "电池优化未忽略"
            textView.setTextViewColorRed()
            if (needRequestPermission) {
                requestIgnoreBatteryOptimizations(context)
            }
        } else {
            textView.text = "电池优化已忽略"
            textView.setTextViewColorGreen()
        }
    }

    fun requestIgnoreBatteryOptimizations(context: Context) {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        intent.data = "package:${context.packageName}".toUri()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}