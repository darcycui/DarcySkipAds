package com.darcy.lib_access_skip.permission

import android.content.Context
import android.os.Build
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.darcy.lib_access_skip.exts.logD
import com.darcy.lib_access_skip.exts.logW
import com.darcy.lib_access_skip.exts.setTextViewColorGreen
import com.darcy.lib_access_skip.exts.setTextViewColorRed

object NotificationPermissionUtil {

    // 检查通知权限是否已授予
    fun isNotificationPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        } else {
            true // 低版本无需权限
        }
    }

    // 注册权限请求回调 (在 Activity 的 onCreate 中初始化)
    class NotificationPermissionRequester(
        val activity: AppCompatActivity,
        val textView: TextView,
        val onGranted: () -> Unit,
        val onDenied: () -> Unit
    ) {
        companion object {
            private val TAG = NotificationPermissionRequester::class.java.simpleName
        }

        private val requestPermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            handlePermissionResult(isGranted)
        }

        private fun handlePermissionResult(isGranted: Boolean) {
            if (isGranted) {
                // 权限已授予，执行后续操作
                logD("通知权限已授予", TAG)
                setGrantedUI()
            } else {
                // 引导用户手动开启
                logW("通知权限未授予", TAG)
                setDeniedUI()
                onDenied()
            }
        }

        fun init() {
            logD("初始化权限请求,必须在onResume前调用", TAG)
        }

        fun requestNotificationPermissionWithTextView(needRequestPermission: Boolean) {
            if (isNotificationPermissionGranted(activity)) {
                setGrantedUI()
                onGranted()
                return
            }
            setDeniedUI()
            if (needRequestPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        private fun setGrantedUI() {
            textView.text = "通知权限已授予"
            textView.setTextViewColorGreen()
        }

        private fun setDeniedUI() {
            textView.text = "通知权限未授予"
            textView.setTextViewColorRed()
        }
    }
}
