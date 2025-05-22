package com.darcy.lib_access_skip.task

import android.accessibilityservice.AccessibilityService
import com.darcy.lib_access_skip.task.bean.ITask
import com.darcy.lib_access_skip.task.bean.SkipTask
import com.darcy.lib_access_skip.task.cache.FIFOCache
import com.darcy.lib_access_skip.task.consumer.TaskConsumer
import com.darcy.lib_access_skip.task.producer.TaskProducer
import kotlinx.coroutines.channels.Channel

object TaskManager {
    // 添加 FIFO 缓存记录已生产任务
    private val taskCache = FIFOCache<SkipTask>(20)
    private val channel = Channel<ITask>(20)

    private val producer = TaskProducer(channel, taskCache)
    private val consumer = TaskConsumer(channel, taskCache)


    init {
        start()
    }

    /**
     * 开始
     */
    fun start() {
        consumer.consume()
    }

    /**
     * 添加任务
     */
    fun addTask(service: AccessibilityService?) {
        producer.produce(service)
    }

    /**
     * 清空缓存
     */
    fun clearTaskCache() {
        taskCache.clear()
    }

    /**
     * 停止
     */
    fun stop() {
        producer.close()
        consumer.close()
    }
}