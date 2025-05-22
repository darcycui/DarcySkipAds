package com.darcy.lib_access_skip.utils

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Activity
import android.content.Context
import android.view.accessibility.AccessibilityManager
import android.widget.TextView
import com.darcy.lib_access_skip.exts.setTextViewColorGreen
import com.darcy.lib_access_skip.exts.setTextViewColorRed
import com.darcy.lib_access_skip.service.accessibility.AutoSkipAccessibilityService
import com.darcy.lib_access_skip.service.accessibility.dialog.AccessDialogs

object AccessCheckUtil {

    fun checkAccessibilityWithTextView(textView: TextView?, needRequestPermission: Boolean) {
        if (textView == null) return
        // 检查无障碍权限
        if (!isAccessibilityServiceEnabled(
                textView.context,
                AutoSkipAccessibilityService::class.java.simpleName
            )
        ) {
            // 跳转到无障碍设置页面
            textView.text = "无障碍权限未启用"
            textView.setTextViewColorRed()
            if (needRequestPermission && textView.context is Activity) {
                AccessDialogs.showPermissionDialog(textView.context as Activity)
            }
        } else {
            textView.text = "无障碍权限已启用"
            textView.setTextViewColorGreen()
        }
    }

    /**
     * 检查无障碍服务是否已启用
     *
     * @param context 上下文
     * @param serviceClassName 无障碍服务的完整类名（如 "com.example.MyAccessibilityService"）
     * @return true 表示已启用，false 表示未启用
     */
    fun isAccessibilityServiceEnabled(context: Context, serviceClassName: String): Boolean {
        val accessibilityManager =
            context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager?
        if (accessibilityManager != null) {
            val enabledServices =
                accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            for (service in enabledServices) {
                if (service.id.contains(serviceClassName)) {
                    return true
                }
            }
        }
        return false
    }
}