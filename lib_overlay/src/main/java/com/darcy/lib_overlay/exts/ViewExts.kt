package com.darcy.lib_overlay.exts

import android.view.View

// 自定义扩展函数示例（可能存在于项目中的某个位置）
fun View.setOnIntervalClickListener(
    interval: Long = 500, // 默认间隔 500ms
    action: (View) -> Unit
) {
    var lastClickTime = 0L
    setOnClickListener { view ->
        val now = System.currentTimeMillis()
        if (now - lastClickTime >= interval) {
            lastClickTime = now
            action(view)
        }
    }
}
