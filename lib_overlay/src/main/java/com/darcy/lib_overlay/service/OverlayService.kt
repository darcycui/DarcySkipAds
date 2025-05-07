package com.darcy.lib_overlay.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.view.LayoutInflater
import com.darcy.lib_overlay.OverlayViewManager
import com.darcy.lib_overlay.databinding.LibOverlayOverlayLayoutBinding
import com.darcy.lib_overlay.exts.setOnIntervalClickListener
import kotlin.jvm.java

class OverlayService : Service() {

    // darcyRefactor: ViewBinding 可以使用 application上下文
    val binding by lazy {
        LibOverlayOverlayLayoutBinding.inflate(LayoutInflater.from(application))
    }

    companion object {
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
        return OverlayBinder()
    }

    override fun onCreate() {
        super.onCreate()
        initOverlayManager()
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
        OverlayViewManager.hideView(binding.rootLayout)
    }
}