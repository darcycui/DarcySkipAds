package com.darcy.lib_access_skip.task.bean

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityNodeInfo

data class SkipTask(
    private val node: AccessibilityNodeInfo?,
    private val service: AccessibilityService?
) : ITask {
    override fun getNode(): AccessibilityNodeInfo? {
        return node
    }

    override fun getUniqueKey(): CharSequence {
        if (node == null) return ""
        return "${node.packageName}:${node.viewIdResourceName}:${node.text}"
    }

    override fun getService(): AccessibilityService? {
        return service
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (this === other) {
            return true
        }
        if (other is SkipTask) {
            return getUniqueKey() == other.getUniqueKey()
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return getUniqueKey().hashCode()
    }

    override fun toString(): String {
        return "SkipTask(key=${getUniqueKey()})"
    }

}