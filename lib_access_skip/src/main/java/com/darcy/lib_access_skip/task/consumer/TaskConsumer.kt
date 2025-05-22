package com.darcy.lib_access_skip.task.consumer

import com.darcy.lib_access_skip.exts.logV
import com.darcy.lib_access_skip.task.bean.ITask
import com.darcy.lib_access_skip.task.bean.SkipTask
import com.darcy.lib_access_skip.task.cache.FIFOCache
import com.darcy.lib_access_skip.utils.GestureUtil
import com.darcy.lib_access_skip.utils.PerformActionUtil
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext

class TaskConsumer(
    private val channel: Channel<ITask>,
    private val taskCache: FIFOCache<SkipTask>,
    private val consumerDispatcher: CoroutineDispatcher = newSingleThreadContext("consumerDispatcher")
) {
    companion object {
        private val TAG = TaskConsumer::class.java.simpleName
        private const val CONSUME_DELAY_MM = 1_000L
    }
    private val exceptionHandler: CoroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logV("[消费者] 捕获到异常: ${throwable.message}")
    }

    // darcyRefactor: 协程调度器 子job单独失败 异常处理
    private val scopeConsumer = CoroutineScope(consumerDispatcher + SupervisorJob() + exceptionHandler)
    private val scopeMain = MainScope()

    fun consume() {
        scopeConsumer.launch {
            for (item in channel) {
                // darcyRefactor: 延迟执行 避免频繁点击
                delay(CONSUME_DELAY_MM)
                logV(("[消费者] 处理: ${item.getUniqueKey()} | 线程: ${Thread.currentThread().name}"))
                scopeMain.launch {
                    item.getNode()?.let { aInfo ->
                        if (aInfo.isClickable) {
                            // 执行点击操作
                            PerformActionUtil.performClickAction(item.getNode(), item.getService())
                        } else {
                            // 执行手势点击
                            GestureUtil.clickByCoordinates(item.getNode(), item.getService())
                        }
                        // 移除缓存
                        taskCache.remove()
                    }
                }
            }
        }
    }

    fun close() {
        scopeConsumer.cancel()
        scopeMain.cancel()
        channel.close()
    }
}