package com.darcy.lib_access_skip.task.producer

import android.accessibilityservice.AccessibilityService
import com.darcy.lib_access_skip.exts.logI
import com.darcy.lib_access_skip.task.bean.ITask
import com.darcy.lib_access_skip.task.bean.SkipTask
import com.darcy.lib_access_skip.task.cache.FIFOCache
import com.darcy.lib_access_skip.utils.BlackListUtil
import com.darcy.lib_access_skip.utils.ScopeUtil
import com.darcy.lib_access_skip.utils.StringUtil
import com.darcy.lib_access_skip.utils.ViewUtil
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext

class TaskProducer(
    private val channel: Channel<ITask>,
    private val producerDispatcher: CoroutineDispatcher = newSingleThreadContext("producerDispatcher")
) {
    companion object {
        private val TAG = TaskProducer::class.java.simpleName
        private const val STRING_SKIP = "跳过"
        private const val STRING_LENGTH_MAX = 10
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

    private val exceptionHandler: CoroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            logI("[生产者] 捕获到异常: $throwable")
        }

    private val scope = CoroutineScope(producerDispatcher + SupervisorJob() + exceptionHandler)

    // 添加 FIFO 缓存记录已生产任务（容量 20）
    private val producedCache = FIFOCache<SkipTask>(20)

    fun produce(service: AccessibilityService?) {
        scope.launch {
            if (service == null) return@launch
            val infos = ViewUtil.findTargetView(STRING_SKIP, service)
            if (infos.isNullOrEmpty() || widgetList.isNullOrEmpty()) return@launch
            infos.filterNotNull()
                .filter { BlackListUtil.isInBlackList(it.packageName).not() }
                .filter { StringUtil.isTextValid(it.text, STRING_LENGTH_MAX) }
                .forEach { aInfo ->
                    widgetList.forEach { widgetName ->
                        // 需要点击的按钮
                        if (aInfo.className == widgetName && aInfo.isEnabled) {
                            val item = SkipTask(aInfo, service)
                            if (producedCache.contains(item)) {
                                logI("[生产者] 跳过重复任务: ${item.getUniqueKey()} | 线程: ${Thread.currentThread().name}")
                            } else {
                                producedCache.add(item)
                                channel.send(item)
                                logI("[生产者] 生产: ${item.getUniqueKey()} | 线程: ${Thread.currentThread().name}")
                            }
                        }
                    }
                }
        }
    }

    fun clearProducedCache() {
        producedCache.clear()
    }

    fun close() {
        scope.cancel()
        channel.close()
    }
}