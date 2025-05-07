package com.darcy.lib_overlay.utils

import android.R.attr.text
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.provider.Settings
import android.widget.TextView
import androidx.core.net.toUri

object OverlayCheckUtil {

    // 请求权限
    fun checkOverlayPermissionWithTextView(textView: TextView, requestCode: Int) {
        val activity = textView.context as Activity
        if (canDrawOverlays(activity)) {
            textView.text = "悬浮窗权限已授予"
            textView.setTextColor(Color.GREEN)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                "package:${activity.packageName}".toUri()
            )
            activity.startActivityForResult(intent, requestCode)
        }
    }

    // 检查是否已授予权限
    fun canDrawOverlays(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true // Android 6.0 以下默认允许
        }
    }

}