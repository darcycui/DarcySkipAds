package com.darcy.lib_access_skip.task.producer

import android.accessibilityservice.AccessibilityService
import com.darcy.lib_access_skip.exts.logD
import com.darcy.lib_access_skip.exts.logE
import com.darcy.lib_access_skip.exts.logI
import com.darcy.lib_access_skip.exts.logW
import com.darcy.lib_access_skip.task.bean.ITask
import com.darcy.lib_access_skip.task.bean.SkipTask
import com.darcy.lib_access_skip.task.cache.FIFOCache
import com.darcy.lib_access_skip.utils.BlackListUtil
import com.darcy.lib_access_skip.utils.StringUtil
import com.darcy.lib_access_skip.utils.ViewUtil
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext

class TaskProducer(
    private val channel: Channel<ITask>,
    private val taskCache: FIFOCache<SkipTask>,
    private val producerDispatcher: CoroutineDispatcher = newSingleThreadContext("producerDispatcher")
) {
    companion object {
        private val TAG = TaskProducer::class.java.simpleName
        private const val STRING_SKIP = "跳过"
        private const val STRING_LENGTH_MAX = 5
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


    fun produce(service: AccessibilityService?) {
        scope.launch {
            if (service == null) return@launch
            val infos = ViewUtil.findTargetView(STRING_SKIP, service)
            if (infos.isNullOrEmpty() || widgetList.isNullOrEmpty()) return@launch
            infos.filterNotNull()
                .filter { BlackListUtil.isInBlackList(it.packageName).not() }
                .filter { StringUtil.isTextValid(it.text, STRING_LENGTH_MAX) }
                .forEachIndexed { infoIndex: Int, aInfo ->
                    logW("infoIndex=$infoIndex")
                    widgetList.forEachIndexed { widgetIndex: Int, widgetName ->
                        // 需要点击的按钮
                        if (aInfo.className == widgetName && aInfo.isEnabled) {
                            logD("widgetIndex=$widgetIndex")
                            val item = SkipTask(aInfo, service)
                            if (taskCache.contains(item)) {
                                logI("[生产者] 跳过重复任务: ${item.getUniqueKey()} | 线程: ${Thread.currentThread().name}")
                            } else {
                                // 添加到缓存
                                taskCache.add(item)
                                channel.send(item)
                                logI("[生产者] 生产: ${item.getUniqueKey()} | 线程: ${Thread.currentThread().name}")
                            }
                            return@forEachIndexed
                        }
                    }
                }
        }
    }

    fun clearProducedCache() {
        logE("[生产者] 清空已生产任务缓存")
        taskCache.clear()
    }

    fun close() {
        scope.cancel()
        channel.close()
    }
}