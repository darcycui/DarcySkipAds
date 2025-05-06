package com.darcy.lib_access_skip.utils

object StringUtil {
    fun isTextValid(
        text: CharSequence?,
        targetStrLengthLimit: Int
    ): Boolean {
        return text != null && text.isNotEmpty() && text.trim().length < targetStrLengthLimit
    }
}