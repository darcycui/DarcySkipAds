package com.darcy.skipads

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.darcy.lib_access_skip.exts.toasts
import com.darcy.lib_access_skip.permission.NotificationPermissionUtil
import com.darcy.lib_access_skip.ui.TestSkipActivity
import com.darcy.lib_access_skip.utils.AccessCheckUtil
import com.darcy.lib_access_skip.utils.BatteryCheckUtil
import com.darcy.skipads.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
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
            AccessCheckUtil.checkAccessibilityWithTextView(binding.tvInfo, needDialog = true)
        }
        binding.btnNotification.setOnClickListener {
            notificationPermissionRequester.requestNotificationPermissionWithTextView()
        }
        binding.btnBattery.setOnClickListener {
            BatteryCheckUtil.checkBatteryOptimizationWithTextView(binding.tvInfoBattery)
        }
    }

    override fun onResume() {
        super.onResume()
        // 检查无障碍权限
        AccessCheckUtil.checkAccessibilityWithTextView(binding.tvInfo, needDialog = false)
        // 检查通知权限
        notificationPermissionRequester.requestNotificationPermissionWithTextView()
        // 检查电池优化
        BatteryCheckUtil.checkBatteryOptimizationWithTextView(binding.tvInfoBattery)
    }
}