package com.darcy.lib_access_skip.task.cache

import com.darcy.lib_access_skip.exts.logI
import java.util.concurrent.ConcurrentLinkedDeque

/**
 * 先进先出缓存
 * [capacity] 缓存容量
 */
class FIFOCache<T>(private val capacity: Int) {
    // 缓存队列 线程安全 ConcurrentLinkedDeque (线程不安全可以使用 ArrayDeque)
    private val deque = ConcurrentLinkedDeque<T>()

    fun add(item: T) {
        if (deque.size >= capacity) {
            remove()
        }
        deque.addLast(item)
    }

    fun remove(): T? {
        return deque.peekFirst()?.let {
            logI("remove item: $it")
            deque.pollFirst()
        }
    }

    fun contains(item: T): Boolean {
        deque.forEach {
            logI("contains item: $it")
        }
        return deque.contains(item)
    }

    fun getItems(): List<T> = deque.toList()

    fun clear() {
        deque.clear()
    }

}