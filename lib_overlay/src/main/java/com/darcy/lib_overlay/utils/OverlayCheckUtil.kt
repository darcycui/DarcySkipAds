package com.darcy.lib_overlay.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.provider.Settings
import android.widget.TextView
import androidx.core.net.toUri
import com.darcy.lib_overlay.OverlayViewManager

object OverlayCheckUtil {

    // 请求权限
    fun checkOverlayPermissionWithTextView(textView: TextView, needRequestPermission: Boolean) {
        val activity = textView.context as Activity
        if (canDrawOverlays(activity)) {
            textView.text = "悬浮窗权限已授予"
            textView.setTextColor(Color.GREEN)
            return
        }
        textView.text = "悬浮窗权限未授予"
        textView.setTextColor(Color.RED)
        if (needRequestPermission) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                "package:${activity.packageName}".toUri()
            )
            activity.startActivityForResult(intent,  OverlayViewManager.OVERLAY_PERMISSION_REQUEST_CODE)
        }
    }

    // 检查是否已授予权限
    fun canDrawOverlays(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

}