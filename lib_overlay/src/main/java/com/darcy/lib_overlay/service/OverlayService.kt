package com.darcy.lib_overlay.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import com.darcy.lib_overlay.OverlayViewManager
import com.darcy.lib_overlay.databinding.LibOverlayOverlayLayoutBinding
import com.darcy.lib_overlay.exts.setOnIntervalClickListener
import com.darcy.lib_overlay.notification.OverlayNotificationUtil

class OverlayService : Service() {

    // darcyRefactor: ViewBinding 可以使用 application上下文
    val binding by lazy {
        LibOverlayOverlayLayoutBinding.inflate(LayoutInflater.from(application))
    }

    companion object {
        private val TAG = OverlayService::class.simpleName
        /**
         * 启动服务
         */
        fun startService(context: Context) {
            val intent = Intent(context, OverlayService::class.java)
            context.startService(intent)
        }
    }

    /**
     * binder类
     */
    inner class OverlayBinder : Binder() {
        fun getService(): OverlayService {
            return this@OverlayService
        }
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "onBind: ")
        // 每次绑定时确保前台状态
        doStartForeground()
        return OverlayBinder()
    }

    @SuppressLint("MissingPermission")
    private fun doStartForeground() {
        if (OverlayNotificationUtil.areNotificationsEnabled(this)) {
            startForeground(
                OverlayNotificationUtil.NOTIFICATION_ID,
                OverlayNotificationUtil.createNotification(this)
            )
        } else {
            OverlayNotificationUtil.goNotificationSettings(this)
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.w(TAG, "onUnbind: ")
        // 解绑 停止前台服务
        doStopForeground()
        return super.onUnbind(intent)
    }

    private fun doStopForeground() {
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onCreate() {
        super.onCreate()
        initOverlayManager()
        Log.i(TAG, "onCreate: ")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showOverlayView()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun initOverlayManager() {
        OverlayViewManager.init(this)
    }

    fun showOverlayView() {
        OverlayViewManager.showView(binding.rootLayout)
        binding.btnHide.setOnIntervalClickListener {
            OverlayViewManager.hideView(binding.rootLayout)
        }
    }

    fun hideOverlayView() {
        OverlayViewManager.hideView(binding.rootLayout)
    }

    fun setBackground(progress: Int) {
        OverlayViewManager.setBackground(binding.rootLayout, progress)
    }

    fun isShowingOverlay(): Boolean {
        return OverlayViewManager.isShowing
    }

    override fun onDestroy() {
        super.onDestroy()
        hideOverlayView()
        Log.e(TAG, "onDestroy: ")
    }
}