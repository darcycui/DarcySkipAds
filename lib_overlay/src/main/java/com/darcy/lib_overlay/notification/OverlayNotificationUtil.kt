package com.darcy.lib_overlay.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.darcy.lib_overlay.R

object OverlayNotificationUtil {
    private val TAG = OverlayNotificationUtil::class.java.simpleName
    const val CHANNEL_ID_SUFFIX = "channel_eye_protection"
    const val CHANNEL_NAME = "Darcy护眼服务"
    const val CHANNEL_DESC = "用于持续运行的后台任务"
    const val NOTIFICATION_ID = 101

    var targetPendingIntentClazz: Class<*>? = null

    fun init(targetActivityClazz: Class<*>) {
        targetPendingIntentClazz  = targetActivityClazz
    }

    fun areNotificationsEnabled(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun requestNotificationPermission(
        context: Context,
        activityResultLauncher: ActivityResultLauncher<String>
    ) {
        if (areNotificationsEnabled(context).not()) {
            activityResultLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun goNotificationSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        context.startActivity(intent)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun sendNotification(
        context: Context,
        notification: Notification,
        notificationID: Int = NOTIFICATION_ID
    ) {
        NotificationManagerCompat.from(context).notify(notificationID, notification)
    }

    /**
     * 撤销护眼前台服务通知
     * 用于 stopForeground 的兜底，避免服务停止/销毁后通知栏仍残留 FGS 通知
     */
    fun cancelNotification(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun createNotification(context: Context): Notification {
        initNotificationChannel(context)
        // targetPendingIntentClazz 由 App 在 MainActivity.onCreate 中 init。
        // 进程被系统以 START_STICKY 重启时可能尚未 init，此时不设置点击跳转，避免 null class 崩溃
        val builder =
            NotificationCompat.Builder(context, "${context.packageName}:$CHANNEL_ID_SUFFIX")
                .setContentTitle("护眼服务")
                .setContentText("护眼服务正在运行")
                .setSmallIcon(R.drawable.lib_overlay_dog)
                .setOngoing(true) // 不可滑动删除
                .setAutoCancel(false) // 禁止自动取消
        targetPendingIntentClazz?.let {
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, it),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            builder.setContentIntent(pendingIntent)
        }
        return builder.build()
    }

    /**
     * 创建通知渠道 使用兼容类 NotificationChannelCompat 和 NotificationManagerCompat
     */
    private fun initNotificationChannel(context: Context) {
        val realChannelID = "${context.packageName}:$CHANNEL_ID_SUFFIX"
        if (NotificationManagerCompat.from(context).getNotificationChannel(realChannelID) != null) {
            Log.e(TAG, "initNotificationChannel: $realChannelID 已经存在 无需创建")
            return
        }
        val channel =
            NotificationChannelCompat.Builder(realChannelID, NotificationManager.IMPORTANCE_DEFAULT)
                .setName(CHANNEL_NAME)
                .setDescription(CHANNEL_DESC)
                .setLightsEnabled(false) // 不闪灯
                .setVibrationEnabled(false) // 不震动
                .setSound(null, null) // 静音
                .setShowBadge(false) // 不显示角标
                .build()
        NotificationManagerCompat.from(context).createNotificationChannel(channel)
    }
}