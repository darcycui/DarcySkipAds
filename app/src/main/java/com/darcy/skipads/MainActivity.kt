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
import android.provider.Settings.canDrawOverlays
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
import com.darcy.lib_overlay.OverlayViewManager
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
            onGranted = {
            },
            onDenied = {
                // 打开设置-通知页面
                val intent = Intent().apply {
                    // Android 8.0 跳转方式
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)

                    // 兼容低版本
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", packageName, null)
                    }
                }
                startActivity(intent)
            }
        )
    }
    private var overlayService: OverlayService? = null

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
                needDialog = true
            )
        }
        binding.btnNotification.setOnClickListener {
            notificationPermissionRequester.requestNotificationPermissionWithTextView()
        }
        binding.btnBattery.setOnClickListener {
            BatteryCheckUtil.checkBatteryOptimizationWithTextView(binding.tvInfoBattery)
        }

        binding.tvSwitcher.setOnClickListener {
            overlayService?.let {
                if (it.isShowingOverlay()) {
                    it.hideOverlayView()
                    toasts("已关闭护眼")
                } else {
                    it.showOverlayView()
                    it.setBackground(binding.seekbar.progress)
                    toasts("已开启护眼")
                }
            }
            checkOverlayStatus()
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
            needDialog = false
        )
        // 检查通知权限
        notificationPermissionRequester.requestNotificationPermissionWithTextView()
        // 检查电池优化
        BatteryCheckUtil.checkBatteryOptimizationWithTextView(binding.tvInfoBattery)
        // 检查悬浮窗权限
        checkOverlayPermission(needRequestPermission = true)
        checkOverlayStatus()
    }

    private fun checkOverlayStatus() {
        overlayService?.let {
            if (it.isShowingOverlay()) {
                binding.tvSwitcher.text = "已开启"
                binding.tvSwitcher.setTextColor(Color.GREEN)
            } else {
                binding.tvSwitcher.text = "已关闭"
                binding.tvSwitcher.setTextColor(Color.BLACK)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OverlayViewManager.OVERLAY_PERMISSION_REQUEST_CODE) {
            checkOverlayPermission(needRequestPermission = false)
        }
    }

    private fun checkOverlayPermission(needRequestPermission: Boolean = false) {
        if (canDrawOverlays(this)) {
            // 权限已授予 显示悬浮窗
            binding.tvInfoOverlay.text = "悬浮窗权限已授予"
            binding.tvInfoOverlay.setTextColor(Color.GREEN)
            connectOverlayService()
        } else {
            // 用户拒绝 提示引导
            binding.tvInfoOverlay.text = "悬浮窗权限未授予"
            binding.tvInfoOverlay.setTextColor(Color.RED)
            if (needRequestPermission) {
                OverlayCheckUtil.checkOverlayPermissionWithTextView(
                    binding.tvInfoOverlay,
                    OverlayViewManager.OVERLAY_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun connectOverlayService() {
//        OverlayService.startService(this)
        bindService(Intent(this, OverlayService::class.java), object : ServiceConnection {
            override fun onServiceConnected(
                name: ComponentName?,
                service: IBinder?
            ) {
                val binder = service as? OverlayService.OverlayBinder
                overlayService = binder?.getService()
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                overlayService = null
            }

        }, BIND_AUTO_CREATE)
    }
}

