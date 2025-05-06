package com.darcy.lib_access_skip.utils

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import com.darcy.lib_access_skip.exts.logD
import com.darcy.lib_access_skip.exts.logE
import com.darcy.lib_access_skip.exts.logW
import com.darcy.lib_access_skip.exts.toasts
import com.darcy.lib_access_skip.utils.ScopeUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object GestureUtil {

    /**
     * 通过手势点击
     */
    fun clickByCoordinates(aInfo: AccessibilityNodeInfo?, service: AccessibilityService?) {
        if (aInfo == null || service == null) {
            logE("手势跳过:  aInfo == null || service == null")
            return
        }
        val rect = Rect()
        aInfo.getBoundsInScreen(rect)
        // 确保控件可见且坐标有效
        if (rect.isEmpty || !aInfo.isVisibleToUser) {
            logE("控件不可见或坐标无效")
            return
        }
        // 构建手势
        val gestureBuilder = GestureDescription.Builder()
        val path = Path().apply {
            moveTo(rect.centerX().toFloat(), rect.centerY().toFloat())
        }

        val gesture = gestureBuilder.addStroke(
            GestureDescription.StrokeDescription(
                path,
                0, // 开始时间
                50 // 持续时间（毫秒）
            )
        ).build()
        // 发送手势
        service.dispatchGesture(gesture, object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                logD("手势完成: (${rect.centerX()}, ${rect.centerY()})")
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                logE("手势被取消")
            }
        }, null)
        // 添加延迟确保手势执行
        ScopeUtil.getMainScope().launch {
            delay(1_00)
        }
        logW("手势跳过: (${rect.centerX()}, ${rect.centerY()}) ${aInfo.packageName} ${aInfo.text}")
        service.toasts("手势跳过")
    }
}