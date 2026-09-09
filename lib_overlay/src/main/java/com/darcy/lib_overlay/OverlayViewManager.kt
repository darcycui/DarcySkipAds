package com.darcy.lib_overlay

import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
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
        // 尺寸用真实屏幕像素铺满（含状态栏/导航栏区域）。
        // 对 TYPE_APPLICATION_OVERLAY 用 MATCH_PARENT 时，部分设备/ROM 会按“去掉系统栏后的可用区”计算，
        // 导致顶部状态栏无法被遮罩覆盖。
        @Suppress("DEPRECATION")
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(metrics)
        layoutParams.width = metrics.widthPixels
        layoutParams.height = metrics.heightPixels
        // 以屏幕左上角为原点铺开
        layoutParams.gravity = Gravity.TOP or Gravity.START
        layoutParams.x = 0
        layoutParams.y = 0
        layoutParams.format = PixelFormat.RGBA_8888
        // 悬浮窗类型
        layoutParams.type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }
        // 允许内容延伸进系统栏区域并铺满全屏
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or // 禁止触摸事件
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or // 禁止获取焦点
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or // 内容可延伸至系统栏
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        // 全屏效果：尽力隐藏状态栏与导航栏（对非聚焦悬浮窗，部分系统会忽略）
        layoutParams.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        // 刘海屏/挖孔屏区域也允许绘制
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            layoutParams.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            layoutParams.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        return layoutParams
    }
}