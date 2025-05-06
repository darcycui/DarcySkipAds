package com.darcy.lib_access_skip.utils

object BlackListUtil {

    private val blackList = mutableListOf<CharSequence>().apply {
        add("com.samsung.accessibility")
    }

    fun addToBlackList(appName: CharSequence) {
        blackList.add(appName)
    }

    fun removeFromBlackList(appName: CharSequence) {
        blackList.remove(appName)
    }

    fun isInBlackList(appName: CharSequence): Boolean {
        return false
    }
}