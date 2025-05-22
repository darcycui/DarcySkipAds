package com.darcy.lib_access_skip.utils

import com.darcy.lib_access_skip.exts.logD

object StringUtil {
    fun isTextValid(
        text: CharSequence?,
        targetStrLengthLimit: Int
    ): Boolean {
        logD("文本-->text: $text 长度-->${text?.trim()?.length} 文本长度限制: $targetStrLengthLimit")
        return text != null && text.isNotEmpty() && text.trim().length < targetStrLengthLimit
    }
}