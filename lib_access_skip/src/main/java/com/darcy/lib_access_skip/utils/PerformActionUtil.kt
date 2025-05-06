package com.darcy.lib_access_skip.utils

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityNodeInfo
import com.darcy.lib_access_skip.exts.logW
import com.darcy.lib_access_skip.exts.toasts

object PerformActionUtil {
    /**
     * 执行点击操作
     */
    fun performClickAction(aInfo: AccessibilityNodeInfo?, service: AccessibilityService?) {
        if (aInfo == null || service == null) {
            logW("点击跳过:  aInfo == null || service == null")
            return
        }
        performActionInternal(aInfo, AccessibilityNodeInfo.ACTION_CLICK)
        logW("点击跳过:  ${aInfo.packageName} ${aInfo.text}")
        service.toasts("点击跳过")
    }

    /**
     * 执行操作 action
     */
    private fun performActionInternal(aInfo: AccessibilityNodeInfo, action: Int) {
        aInfo.performAction(action)
    }

    /**
     * 执行全局操作 返回
     */
    fun performGlobalBackAction(
        action: Int,
        service: AccessibilityService
    ) {
        performGlobalActionInternal(AccessibilityService.GLOBAL_ACTION_BACK, service)
    }

    /**
     * 执行全局操作 Home
     */
    fun performGlobalHomeAction(
        action: Int,
        service: AccessibilityService
    ) {
        performGlobalActionInternal(AccessibilityService.GLOBAL_ACTION_HOME, service)
    }

    /**
     * 执行全局操作 action
     */
    private fun performGlobalActionInternal(
        action: Int,
        service: AccessibilityService
    ) {
        service.performGlobalAction(action)
    }
}