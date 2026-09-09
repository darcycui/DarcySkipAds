package com.darcy.skipads

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.darcy.lib_access_skip.exts.toasts
import com.darcy.lib_access_skip.permission.NotificationPermissionUtil
import com.darcy.lib_access_skip.ui.TestSkipActivity
import com.darcy.lib_access_skip.utils.AccessCheckUtil
import com.darcy.lib_access_skip.utils.BatteryCheckUtil
import com.darcy.lib_overlay.notification.OverlayNotificationUtil
import com.darcy.lib_overlay.service.OverlayService
import com.darcy.lib_overlay.utils.OverlayCheckUtil
import com.darcy.skipads.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val notificationPermissionRequester: NotificationPermissionUtil.NotificationPermissionRequester by lazy {
        NotificationPermissionUtil.NotificationPermissionRequester(
            this,
            binding.tvInfoNotification,
            onGranted = {},
            onDenied = {}
        )
    }
    private var overlayService: OverlayService? = null
    private var connection: ServiceConnection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        setContentView(R.layout.activity_main)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // darcyRefactor: ActivityResultLauncher请求通知权限 必须在onCreate中初始化
        notificationPermissionRequester.init()
        OverlayNotificationUtil.init(this.javaClass)
        initView()
    }

    private fun initView() {
        binding.testAccessibility.setOnClickListener {
            startActivity(Intent(this, TestSkipActivity::class.java))
        }
        binding.openAccessibility.setOnClickListener {
            // darcyRefactor: 检查无障碍权限 跳转设置页面
            AccessCheckUtil.checkAccessibilityWithTextView(
                binding.tvInfoAccessibility,
                needRequestPermission = true
            )
        }
        binding.btnNotification.setOnClickListener {
            notificationPermissionRequester.requestNotificationPermissionWithTextView(
                needRequestPermission = true
            )
        }
        binding.btnBattery.setOnClickListener {
            BatteryCheckUtil.checkBatteryOptimizationWithTextView(
                binding.tvInfoBattery,
                needRequestPermission = true
            )
        }
        binding.btnOverlay.setOnClickListener {
            OverlayCheckUtil.checkOverlayPermissionWithTextView(
                binding.tvInfoOverlay,
                needRequestPermission = true
            )
        }

        binding.tvSwitcher.setOnClickListener {
            // 以服务进程级状态为准：后台前台服务仍在运行时，重进 App 开关也应显示"已开启"
            if (OverlayService.isActive) {
                turnOffOverlay()
            } else {
                turnOnOverlay()
            }
        }
        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                binding.seekbarProgress.text = "$progress%"
                overlayService?.setBackground(progress)
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.seekbarProgress.text = binding.seekbar.progress.toString() + "%"
    }

    /**
     * 开启护眼：以前台服务方式启动，退出 App 后仍持续运行；绑定服务以便实时调节亮度。
     */
    private fun turnOnOverlay() {
        val started = OverlayService.start(this)
        if (!started) {
            // 通知权限未开启，start() 已引导去设置
            toasts("护眼服务需要通知权限，请先开启")
            return
        }
        connectOverlayService { service ->
            service?.let {
                // onStartCommand 已负责展示遮罩；此处兜底确保显示并同步当前亮度
                if (!it.isShowingOverlay()) {
                    it.startOverlay()
                }
                it.setBackground(binding.seekbar.progress)
            }
            setupOverlayStatus()
        }
        toasts("已开启护眼")
    }

    /**
     * 关闭护眼：彻底停止服务并撤销前台通知。
     */
    private fun turnOffOverlay() {
        if (overlayService != null) {
            // 已绑定：直接命令服务停止（内部 stopForeground + 撤通知 + stopSelf）
            overlayService?.stopOverlay()
        } else {
            // 服务在后台运行但页面未绑定：用 stopService 停止
            OverlayService.stop(this)
        }
        unbindOverlayService()
        toasts("已关闭护眼")
        setupOverlayStatus()
    }

    override fun onResume() {
        super.onResume()
        // 检查无障碍权限
        AccessCheckUtil.checkAccessibilityWithTextView(
            binding.tvInfoAccessibility,
            needRequestPermission = false
        )
        // 检查通知权限
        notificationPermissionRequester.requestNotificationPermissionWithTextView(
            needRequestPermission = false
        )
        // 检查电池优化
        BatteryCheckUtil.checkBatteryOptimizationWithTextView(
            binding.tvInfoBattery, needRequestPermission = false
        )
        // 检查悬浮窗权限
        OverlayCheckUtil.checkOverlayPermissionWithTextView(
            binding.tvInfoOverlay, needRequestPermission = false
        )
        refreshOverlayControl()
    }

    /**
     * 护眼服务可能在后台持续运行：回到页面时若已运行但未绑定，补一个绑定，
     * 使开关状态与亮度调节可用（此解绑不会停止服务）。
     */
    private fun refreshOverlayControl() {
        if (!OverlayService.isActive) {
            setupOverlayStatus()
            return
        }
        if (overlayService == null || connection == null) {
            connectOverlayService { service ->
                service?.let { it.setBackground(binding.seekbar.progress) }
                setupOverlayStatus()
            }
        } else {
            setupOverlayStatus()
        }
    }

    private fun setupOverlayStatus() {
        // 以前台服务的进程级运行状态为准
        val active = OverlayService.isActive
        binding.tvSwitcher.text = if (active) "已开启" else "已关闭"
        binding.tvSwitcher.setTextColor(if (active) Color.GREEN else Color.BLACK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * 解绑并清空服务引用
     */
    private fun unbindOverlayService() {
        connection?.let {
            runCatching { unbindService(it) }
            connection = null
        }
        overlayService = null
    }

    private fun connectOverlayService(callback: (service: OverlayService?) -> Unit) {
        if (overlayService != null && connection != null) {
            // 已连接：直接回调当前服务，避免调用方因守卫静默返回而漏刷新 UI
            callback.invoke(overlayService)
            return
        }
        connection = object : ServiceConnection {
            override fun onServiceConnected(
                name: ComponentName?,
                service: IBinder?
            ) {
                val binder = service as? OverlayService.OverlayBinder
                overlayService = binder?.getService()
                callback.invoke(binder?.getService())
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                overlayService = null
            }

        }
        bindService(Intent(this, OverlayService::class.java), connection!!, BIND_AUTO_CREATE)
    }
}

