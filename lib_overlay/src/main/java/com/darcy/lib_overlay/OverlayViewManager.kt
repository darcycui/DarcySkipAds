package com.darcy.lib_overlay

import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.darcy.lib_overlay.utils.OverlayCheckUtil

object OverlayViewManager {
    const val OVERLAY_PERMISSION_REQUEST_CODE = 101
    lateinit var windowManager: WindowManager
    var isShowing = false

    fun init(context: Context) {
        windowManager = context.applicationContext.getSystemService(WINDOW_SERVICE) as WindowManager
    }

    fun showView(rootView: View) {
        if (isShowing) return
        windowManager.addView(rootView, getWindowLayoutParams())
        isShowing = true
    }

    fun hideView(rootView: View) {
        if (!isShowing) return
        windowManager.removeView(rootView)
        isShowing = false
    }

    fun setBackground(rootView: View, progress: Int) {
        val colorId = getColorId(progress)
        rootView.setBackgroundColor(rootView.context.getColor(colorId))
    }

    private fun getColorId(progress: Int): Int {
        when (progress) {
            in 0..10 -> {
                return R.color.lib_overlay_black_10
            }

            in 11..20 -> {
                return R.color.lib_overlay_black_20
            }

            in 21..30 -> {
                return R.color.lib_overlay_black_30
            }

            in 31..40 -> {
                return R.color.lib_overlay_black_40
            }

            in 41..50 -> {
                return R.color.lib_overlay_black_50
            }

            in 51..60 -> {
                return R.color.lib_overlay_black_60
            }

            in 61..70 -> {
                return R.color.lib_overlay_black_70
            }

            in 71..80 -> {
                return R.color.lib_overlay_black_80
            }

            in 81..90 -> {
                return R.color.lib_overlay_black_90
            }

            in 91..95 -> {
                return R.color.lib_overlay_black_95
            }

            in 96..100 -> {
                return R.color.lib_overlay_black_98
            }

            else -> {
                return R.color.lib_overlay_black_20
            }
        }
    }

    fun getWindowLayoutParams(): WindowManager.LayoutParams {
        val layoutParams = WindowManager.LayoutParams()
        //设置宽高
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        //设置显示位置
        layoutParams.gravity = Gravity.FILL
        layoutParams.format = PixelFormat.RGBA_8888
        //设置状态栏与导航栏效果
        layoutParams.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_IMMERSIVE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        //设置悬浮窗效果
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                layoutParams.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            }
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or // 禁止触摸事件
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or  // 禁止获取焦点
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or // 允许内容延伸至系统栏
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
        }
        return layoutParams
    }
}