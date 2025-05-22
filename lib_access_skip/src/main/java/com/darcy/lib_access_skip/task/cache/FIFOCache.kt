package com.darcy.lib_access_skip.task.cache

/**
 * 先进先出缓存
 * [capacity] 缓存容量
 */
class FIFOCache<T>(private val capacity: Int) {
    private val deque = ArrayDeque<T>()

    fun add(item: T) {
        if (deque.size >= capacity) {
            deque.removeFirst()
        }
        deque.addLast(item)
    }

    fun first(): T? {
        return deque.removeFirstOrNull()
    }

    fun last(): T? {
        return deque.removeLastOrNull()
    }

    fun contains(item: T): Boolean {
        return deque.contains(item)
    }

    fun getItems(): List<T> = deque.toList()

    fun clear() {
        deque.clear()
    }

}