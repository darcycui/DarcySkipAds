package com.darcy.skipads

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
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
            if (overlayService == null) {
                connectOverlayService { service ->
                    service?.let {
                        if (it.isShowingOverlay()) {
                            it.hideOverlayView()
                            toasts("已关闭护眼")
                        } else {
                            it.showOverlayView()
                            it.setBackground(binding.seekbar.progress)
                            toasts("已开启护眼")
                        }
                        setupOverlayStatus()
                    }
                }
            } else {
                overlayService?.let {
                    if (it.isShowingOverlay()) {
                        it.hideOverlayView()
                        toasts("已关闭护眼")
                        connection?.let {
                            unbindService(it)
                            connection = null
                            overlayService = null
                        }
                    } else {
                        it.showOverlayView()
                        it.setBackground(binding.seekbar.progress)
                        toasts("已开启护眼")
                    }
                }
                setupOverlayStatus()
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
        setupOverlayStatus()
    }

    private fun setupOverlayStatus() {
        overlayService?.let {
            if (it.isShowingOverlay()) {
                binding.tvSwitcher.text = "已开启"
                binding.tvSwitcher.setTextColor(Color.GREEN)
            } else {
                binding.tvSwitcher.text = "已关闭"
                binding.tvSwitcher.setTextColor(Color.BLACK)
            }
        } ?: run {
            binding.tvSwitcher.text = "已关闭"
            binding.tvSwitcher.setTextColor(Color.BLACK)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun connectOverlayService(callback: (service: OverlayService?) -> Unit) {
        if (overlayService != null && connection != null) {
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

