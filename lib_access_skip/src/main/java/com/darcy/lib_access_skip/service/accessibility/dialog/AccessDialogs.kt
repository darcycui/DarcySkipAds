package com.darcy.lib_access_skip.service.accessibility.dialog

import android.app.Activity
import android.content.Intent
import android.provider.Settings
import androidx.appcompat.app.AlertDialog

object AccessDialogs {
    fun showPermissionDialog(activity: Activity) {
        AlertDialog.Builder(activity)
            .setTitle("提示")
            .setMessage("请开启无障碍服务以便使用跳过广告功能")
            .setPositiveButton("去设置") { _, _ ->
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                activity.startActivity(intent)
            }
            .setNegativeButton("取消", null)
            .show()
    }
}