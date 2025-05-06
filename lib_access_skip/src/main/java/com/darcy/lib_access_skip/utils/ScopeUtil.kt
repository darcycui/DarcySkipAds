package com.darcy.lib_access_skip.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

object ScopeUtil {

    private val mainScope = MainScope()

    fun getMainScope(): CoroutineScope {
        return mainScope
    }
}