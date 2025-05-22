package com.darcy.lib_access_skip.utils

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityNodeInfo
import com.darcy.lib_access_skip.exts.logV
import com.darcy.lib_access_skip.task.cache.FIFOCache
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object ViewUtil {

    /**
     * 打印 View 树
     */
    fun printViewTree(
        node: AccessibilityNodeInfo?,
        depth: Int = 0,
        builder: StringBuilder = StringBuilder()
    ) {
        if (node == null) return

        // 生成当前节点的缩进
        val indent = "  ".repeat(depth)

        // 收集节点信息
        builder.append("$indent├─ ")
            .append("Class: ${node.className}\n")
            .append("$indent│  Text: ${node.text}\n")
            .append("$indent│  ResId: ${node.viewIdResourceName}\n")
            .append("$indent│  Clickable: ${node.isClickable}\n")
            .append("$indent│  Visible: ${node.isVisibleToUser}\n")

        // 递归处理子节点
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            printViewTree(child, depth + 1, builder)
        }

        // 如果是根节点，输出最终结果
        if (depth == 0) {
            logV("View Tree:\n$builder")
        }
    }

    /**
     * 根据文本 查找目标 View
     */
    fun findTargetView(
        targetStr: String?,
        service: AccessibilityService?
    ): List<AccessibilityNodeInfo?>? {
        if (service == null) return null
        if (service.rootInActiveWindow == null || targetStr.isNullOrEmpty()) {
            return null
        }
        return service.rootInActiveWindow.findAccessibilityNodeInfosByText(targetStr)
    }

    // 使用 FIFO 缓存记录最近点击过的元素（防止重复点击）
    private val clickedCache = FIFOCache<String>(20) // 缓存最近 20 个点击元素
    fun clearClickedCache() {
        clickedCache.clear()
    }

    /**
     * 点击目标 View
     */
    fun filterAndClickTargetView(
        infos: List<AccessibilityNodeInfo?>?,
        widgetList: List<String>?,
        targetStrLengthLimit: Int,
        service: AccessibilityService?,
    ) {
        if (service == null) return
        if (infos.isNullOrEmpty() || widgetList.isNullOrEmpty()) return
        ScopeUtil.getMainScope().launch {
            infos.filterNotNull()
                .filter { BlackListUtil.isInBlackList(it.packageName).not() }
                .filter { StringUtil.isTextValid(it.text, targetStrLengthLimit) }
                .forEach { aInfo ->
                    widgetList.forEach { widgetName ->
                        // 点击按钮
                        if (aInfo.className == widgetName && aInfo.isEnabled) {
                            // 生成唯一标识：使用关键属性组合（防止节点回收导致的误判）
                            val uniqueKey = "${aInfo.viewIdResourceName}:${aInfo.text}"
                            // 检查是否已点击过（通过缓存）
                            if (!clickedCache.getItems().contains(uniqueKey)) {
                                clickedCache.add(uniqueKey) // 记录到缓存
                                if (aInfo.isClickable) {
                                    delay(1_00)
                                    // 执行点击操作
                                    PerformActionUtil.performClickAction(aInfo, service)
                                } else {
                                    // 执行手势点击
                                    GestureUtil.clickByCoordinates(aInfo, service)
                                }
                            } else {
                                logV("重复点击：$uniqueKey")
                            }
                        }
                    }
                }
        }
    }

    /**
     * 过滤需要点击的 View
     */
    fun filterTargetView(
        infos: List<AccessibilityNodeInfo?>?,
        widgetList: List<String>?,
        targetStrLengthLimit: Int,
        service: AccessibilityService?,
    ) {
        if (service == null) return
        if (infos.isNullOrEmpty() || widgetList.isNullOrEmpty()) return
        ScopeUtil.getMainScope().launch {
            infos.filterNotNull()
                .filter { BlackListUtil.isInBlackList(it.packageName).not() }
                .filter { StringUtil.isTextValid(it.text, targetStrLengthLimit) }
                .forEach { aInfo ->
                    widgetList.forEach { widgetName ->
                        // 需要点击的按钮
                        if (aInfo.className == widgetName && aInfo.isEnabled) {

                        }
                    }
                }
        }
    }
}