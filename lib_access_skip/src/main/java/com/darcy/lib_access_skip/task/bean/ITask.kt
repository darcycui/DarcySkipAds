package com.darcy.lib_access_skip.task.bean

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityNodeInfo

interface ITask {
    fun getNode(): AccessibilityNodeInfo?
    fun getUniqueKey(): CharSequence
    fun getService(): AccessibilityService?

}