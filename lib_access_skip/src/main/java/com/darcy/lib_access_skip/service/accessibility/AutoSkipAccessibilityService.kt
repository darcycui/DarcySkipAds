package com.darcy.lib_access_skip.service.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.darcy.lib_access_skip.exts.logE
import com.darcy.lib_access_skip.exts.logV
import com.darcy.lib_access_skip.exts.logW
import com.darcy.lib_access_skip.task.TaskManager

/**
 * 无障碍服务 Service
 */
class AutoSkipAccessibilityService : AccessibilityService() {

    companion object {
        private val TAG = AutoSkipAccessibilityService::class.java.simpleName
        private const val WIDGET_TEXTVIEW = "android.widget.TextView"
        private const val WIDGET_APP_COMPAT_TEXTVIEW = "androidx.appcompat.widget.AppCompatTextView"
        private const val WIDGET_BUTTON = "android.widget.Button"
        private const val WIDGET_APP_COMPAT_BUTTON = "androidx.appcompat.widget.AppCompatButton"
        private val widgetList = listOf<String>(
            WIDGET_TEXTVIEW,
            WIDGET_APP_COMPAT_TEXTVIEW,
            WIDGET_BUTTON,
            WIDGET_APP_COMPAT_BUTTON
        )
    }

    private var lastApp: CharSequence = ""

    override fun onServiceConnected() {
        super.onServiceConnected()
        logV("$TAG onServiceConnected")
//        val newServiceInfo = AccessibilityServiceInfo().apply {
//            //  设置无障碍服务, 已经在@xml/accessibility_service_config.xml中配置
//        }
//        setServiceInfo(newServiceInfo)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
//        logD("$TAG onAccessibilityEvent: event-->$event")
        runCatching {
            clearCacheIfNeeded(event)
            dealEvent(event)
        }.onFailure {
            logE("$TAG onAccessibilityEvent: error-->$it")
        }.onSuccess {
        }
    }

    private fun clearCacheIfNeeded(event: AccessibilityEvent?) {
        if (event == null) return
        val currentApp = event.packageName ?: ""
//        logV("$TAG onAccessibilityEvent: currentApp=$currentApp lastApp=$lastApp")
//        if (currentApp != lastApp) {
//            TaskManager.clearTaskCache()
//        }
        lastApp = currentApp
    }

    private fun dealEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val eventType = event.eventType
        when (eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                // 打印视图树
//                rootInActiveWindow?.let {
//                    if (event.packageName == "tv.danmaku.bili") {
//                        ViewUtil.printViewTree(it)
//                    } else if (event.packageName == "com.sina.weibo") {
//                        ViewUtil.printViewTree(it)
//                    }
//                }
                // 跳过广告
//                ViewUtil.filterAndClickTargetView(
//                    ViewUtil.findTargetView(STRING_SKIP, this),
//                    widgetList,
//                    STRING_LENGTH_MAX,
//                    this
//                )
                // 跳过广告 使用异步任务
                TaskManager.addTask(this)
            }

            AccessibilityEvent.TYPE_WINDOWS_CHANGED -> {}

            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {}

            AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED -> {}

            AccessibilityEvent.TYPE_GESTURE_DETECTION_START -> {}

            AccessibilityEvent.TYPE_GESTURE_DETECTION_END -> {}

            AccessibilityEvent.TYPE_VIEW_CLICKED -> {}

            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {}

            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {}

            AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED -> {}

            AccessibilityEvent.TYPE_ANNOUNCEMENT -> {}

            AccessibilityEvent.TYPE_ASSIST_READING_CONTEXT -> {}

            AccessibilityEvent.TYPE_SPEECH_STATE_CHANGE -> {}

            AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END -> {}

            AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START -> {}

            AccessibilityEvent.TYPE_TOUCH_INTERACTION_END -> {}

            AccessibilityEvent.TYPE_TOUCH_INTERACTION_START -> {}

            AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED -> {}

            AccessibilityEvent.TYPE_VIEW_CONTEXT_CLICKED -> {}

            AccessibilityEvent.TYPE_VIEW_FOCUSED -> {}

            AccessibilityEvent.TYPE_VIEW_HOVER_ENTER -> {}

            AccessibilityEvent.TYPE_VIEW_HOVER_EXIT -> {}

            AccessibilityEvent.TYPE_VIEW_LONG_CLICKED -> {}

            AccessibilityEvent.TYPE_VIEW_SELECTED -> {}

            AccessibilityEvent.TYPE_VIEW_TARGETED_BY_SCROLL -> {}

            AccessibilityEvent.TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY -> {}
        }
    }


    override fun onInterrupt() {
        logW("$TAG onInterrupt")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        logE("$TAG onUnbind")
        return super.onUnbind(intent)
    }
}