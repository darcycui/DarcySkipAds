package com.darcy.lib_overlay.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import com.darcy.lib_overlay.OverlayViewManager
import com.darcy.lib_overlay.databinding.LibOverlayOverlayLayoutBinding
import com.darcy.lib_overlay.exts.setOnIntervalClickListener
import com.darcy.lib_overlay.notification.OverlayNotificationUtil

/**
 * 护眼前台服务。
 *
 * 以「启动型前台服务」方式运行：App 开启后即使退出（Home/最近任务划掉），
 * 服务仍会作为前台服务在后台持续运行，护眼遮罩与通知不消失；
 * 需要关闭时显式调用 [stopOverlay] 或 [stop]。
 */
class OverlayService : Service() {

    // darcyRefactor: ViewBinding 可以使用 application上下文
    val binding by lazy {
        LibOverlayOverlayLayoutBinding.inflate(LayoutInflater.from(application))
    }

    companion object {
        private val TAG = OverlayService::class.simpleName

        /**
         * 护眼服务当前是否在运行（进程级标志，供 UI 判断开关状态）。
         * 由于前台服务会撑住进程，退出 App 重进后该值仍为 true。
         */
        @Volatile
        var isActive = false
            private set

        /**
         * 以「前台服务」方式开启护眼。
         * 前台通知在 [OverlayService.onCreate] 中即刻 startForeground。
         *
         * @return true 表示已发起启动；false 表示缺少通知权限，已引导去设置，未启动
         */
        fun start(context: Context): Boolean {
            if (!OverlayNotificationUtil.areNotificationsEnabled(context)) {
                OverlayNotificationUtil.goNotificationSettings(context)
                return false
            }
            ContextCompat.startForegroundService(
                context,
                Intent(context, OverlayService::class.java)
            )
            return true
        }

        /**
         * 停止护眼服务（适用于当前未绑定的场景，直接 stopService）。
         */
        fun stop(context: Context) {
            isActive = false
            context.stopService(Intent(context, OverlayService::class.java))
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

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate: ")
        initOverlayManager()
        isActive = true
        // 启动即进入前台：既满足 startForegroundService 的时限要求，也让服务能长期后台运行
        doStartForeground()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand: ")
        // 前台启动后默认直接展示遮罩
        showOverlayView()
        // 进程被系统回收时以 START_STICKY 重启，确保护眼持续
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "onBind: ")
        // 每次绑定时确保处于前台（已在前台时是幂等的）
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
        // 服务以启动型前台服务运行：解绑不代表停止，护眼需在退出 App 后继续。
        // 显式关闭请调用 stopOverlay() 或 companion.stop()
        return super.onUnbind(intent)
    }

    private fun doStopForeground() {
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onDestroy() {
        // 兜底：确保前台通知被撤销，防止服务销毁后通知残留
        doStopForeground()
        OverlayNotificationUtil.cancelNotification(this)
        isActive = false
        super.onDestroy()
        hideOverlayView()
        Log.e(TAG, "onDestroy: ")
    }

    private fun initOverlayManager() {
        OverlayViewManager.init(this)
    }

    fun startOverlay() {
        showOverlayView()
    }

    private fun showOverlayView() {
        OverlayViewManager.showView(binding.rootLayout)
        binding.btnHide.setOnIntervalClickListener {
            OverlayViewManager.hideView(binding.rootLayout)
        }
    }

    /**
     * 彻底停止护眼：隐藏遮罩、退出前台、撤销通知并停止服务。
     * 由 App 在关闭护眼时调用，避免解绑路径不触发 stopForeground 导致通知残留。
     */
    fun stopOverlay() {
        isActive = false
        hideOverlayView()
        doStopForeground()
        OverlayNotificationUtil.cancelNotification(this)
        stopSelf()
    }

    private fun hideOverlayView() {
        OverlayViewManager.hideView(binding.rootLayout)
    }

    fun setBackground(progress: Int) {
        OverlayViewManager.setBackground(binding.rootLayout, progress)
    }

    fun isShowingOverlay(): Boolean {
        return OverlayViewManager.isShowing
    }
}
