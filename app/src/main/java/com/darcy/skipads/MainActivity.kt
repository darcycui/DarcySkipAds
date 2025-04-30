package com.darcy.skipads

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.darcy.lib_access_skip.ui.TestSkipActivity
import com.darcy.lib_access_skip.utils.AccessCheckUtil
import com.darcy.lib_access_skip.utils.BatteryCheckUtil
import com.darcy.skipads.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
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
        initView()
        // 检查电池优化
        BatteryCheckUtil.checkBatteryOptimizationWithTextView(binding.tvInfoBattery)
    }

    private fun initView() {
        binding.testAccessibility.setOnClickListener {
            startActivity(Intent(this, TestSkipActivity::class.java))
        }
        binding.openAccessibility.setOnClickListener {
            // darcyRefactor: 检查无障碍权限 跳转设置页面
            AccessCheckUtil.checkAccessibilityWithTextView(binding.tvInfo, needDialog = true)
        }
    }

    override fun onResume() {
        super.onResume()
        // 检查无障碍权限
        AccessCheckUtil.checkAccessibilityWithTextView(binding.tvInfo, needDialog = false)
        // 检查电池优化
        BatteryCheckUtil.checkBatteryOptimizationWithTextView(binding.tvInfoBattery)
    }
}