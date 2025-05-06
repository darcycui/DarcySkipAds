package com.darcy.lib_access_skip.task

import android.accessibilityservice.AccessibilityService
import com.darcy.lib_access_skip.task.bean.ITask
import com.darcy.lib_access_skip.task.consumer.TaskConsumer
import com.darcy.lib_access_skip.task.producer.TaskProducer
import kotlinx.coroutines.channels.Channel

object TaskManager {
    private val channel = Channel<ITask>(20)
    private val producer = TaskProducer(channel)
    private val consumer = TaskConsumer(channel)

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
     * 清空生产者缓存
     */
    fun clearProducedCache() {
        producer.clearProducedCache()
    }

    /**
     * 停止
     */
    fun stop() {
        producer.close()
        consumer.close()
    }
}