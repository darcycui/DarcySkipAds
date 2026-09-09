# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

DarcySkipAds 是一个 Android 工具 App，提供两个功能：
- **自动跳过开屏广告**：通过无障碍服务（AccessibilityService）监听窗口变化，查找并点击文案包含「跳过」的按钮。
- **护眼模式**：通过全屏悬浮窗覆盖一层半透明黑色遮罩来降低屏幕亮度。

纯 Kotlin，无 Compose（XML + ViewBinding），无网络请求/状态管理/第三方 SDK 依赖。界面文案基本为中文。

## 构建与运行

- Gradle Wrapper **8.13**（`distributionUrl` 指向腾讯镜像），AGP **8.11.0**，Kotlin **2.0.21**，版本目录在 `gradle/libs.versions.toml`。
- SDK 版本：`minSdk 26` / `targetSdk 35` / `compileSdk 35`（`lib_access_skip` 为 34）。需 JDK 17。
- 仓库镜像：`settings.gradle.kts` 强制走阿里云 Maven 镜像（`FAIL_ON_PROJECT_REPOS`），直接访问 mavenCentral/google 可能因网络失败。

常用命令（bash 下用 `./gradlew`）：

```bash
./gradlew :app:assembleDebug     # 构建 debug
./gradlew :app:assembleRelease   # 构建 release（自动重命名并复制到 /release 目录）
./gradlew :app:installDebug      # 安装 debug 到已连接设备
./gradlew :app:lintDebug         # lint
./gradlew test                   # 运行全部单元测试（仅示例测试，无真实用例）
```

- 签名配置当前在 `app/build.gradle.kts` 中被**注释掉**（密钥从 `local.properties` 读取，字段 `keystore_path/key_alias/store_password/key_password`），产物为未签名 APK。
- release 产物命名规则：`DarcySkipAds_V<versionName>_<yyyyMMdd-HH-mm-ss>.apk`，通过 `finalizedBy` 挂接的 `copy<Variant>Apk` 任务复制到根目录 `release/`。
- `app` 通过 `implementation(project(":lib_access_skip"))` 和 `implementation(project(":lib_overlay"))` 消费两个库模块（历史上是先编译成 AAR 再引入，相关 `implementation(files(...))` 为注释保留）。

## 模块结构

三个 Gradle 模块：`:app`（应用入口）、`:lib_access_skip`（无障碍自动跳过库）、`:lib_overlay`（护眼悬浮窗库）。两个库相互独立、均不依赖对方，且设计为可独立复用。

- **`:app`**（`com.darcy.skipads`）— 仅一个 `MainActivity` + `DarcySkipApp`(空 Application)。`MainActivity` 负责各权限状态检查（无障碍/通知/电池优化/悬浮窗，均以 TextView 红/绿文字反馈，onResume 时复查），并用 SeekBar 控制护眼遮罩透明度。通过 `bindService` 绑定 `OverlayService`，经 `OverlayBinder` 调用 `showOverlayView()/hideOverlayView()/setBackground(progress)/isShowingOverlay()`。测试跳广告页是 `lib_access_skip` 的 `TestSkipActivity`。
- **`:lib_access_skip`**（`com.darcy.lib_access_skip`）— 实现自动跳过广告的核心库，见下文「自动跳过架构」。
- **`:lib_overlay`**（`com.darcy.lib_overlay`）— 护眼悬浮窗库，见下文「护眼悬浮窗架构」。

库模块设置了 `resourcePrefix`（`lib_access_skip_` / `lib_overlay_`），新增资源必须带此前缀。`app` 与 `lib_overlay` 开启了 ViewBinding；`lib_access_skip` 未开，使用的是 `R.layout` + `findViewById` 或自定义布局。

## 自动跳过广告架构（lib_access_skip）

核心是**生产者-消费者 + FIFO 去重缓存**的任务流水线，全部由 `kotlinx.coroutines` 驱动：

```
AutoSkipAccessibilityService
   │  监听 TYPE_WINDOW_STATE_CHANGED / TYPE_WINDOW_CONTENT_CHANGED
   ▼
TaskManager (object 单例，持有 channel[20] + taskCache)
   ├─ TaskProducer ──► 扫描节点树，产出 SkipTask ──► (taskCache 去重) ──► Channel
   └─ TaskConsumer ◄── 读取任务，延迟 1s 后点击 ◄──────────────────────┘
```

关键点：
- `TaskManager`（`task/TaskManager.kt`）持有 `Channel<ITask>(20)` 与 `FIFOCache<SkipTask>(20)`，`init{}` 中即启动 consumer，`addTask()` 触发一次生产。
- `TaskProducer`（独立单线程 `producerDispatcher`）：用 `ViewUtil.findTargetView("跳过", service)`（即 `rootInActiveWindow.findAccessibilityNodeInfosByText("跳过")`）找出所有含「跳过」的节点，过滤条件为：不在黑名单、`StringUtil.isTextValid`（trim 后长度 < 5）、`className` 匹配 `TextView/Button` 及其 AppCompat 变体、`isEnabled`。命中后构造 `SkipTask`，先经 `taskCache.contains` 去重再 `channel.send`。
- `TaskConsumer`（独立单线程 `consumerDispatcher` + `MainScope`）：从 channel 取任务后先 `delay(1_000)` 防连点，再到主线程执行：节点 `isClickable` 用 `PerformActionUtil.performClickAction`（`ACTION_CLICK`），否则用 `GestureUtil.clickByCoordinates`（`dispatchGesture` 在 `boundsInScreen` 中心点模拟点击）。点击后 `taskCache.remove()`。
- **去重键**：`SkipTask` 的 `equals/hashCode/getUniqueKey` 基于 `${packageName}:${viewIdResourceName}:${text}`（曾修过"taskCache can not hit"问题——键必须与判断"是否为同一按钮"的语义一致）。
- `ViewUtil` 还保留一套**旧的同步点击实现** `filterAndClickTargetView`（内部自带 20 容量的 `clickedCache`），当前在 Service 里被注释、未启用。
- 监听的服务是 `AutoSkipAccessibilityService`，其配置在 `res/xml/accessibility_service_config.xml`（`typeAllMask`，`canRetrieveWindowContent` + `canPerformGestures`）。切包时清缓存的分支被注释掉，`FIFOCache` 仅在容量满时淘汰。

### 已知坑 / 待办
- `BlackListUtil.isInBlackList()` 是**空实现恒返回 `false`**，生产者里的黑名单过滤逻辑实际不生效。
- 无障碍服务权限由 `AccessCheckUtil.isAccessibilityServiceEnabled()` 按类名简单名匹配判断；请求跳转对话框在 `dialog/AccessDialogs.kt`。

## 护眼悬浮窗架构（lib_overlay）

- `OverlayService`：**启动型前台服务**。App 开启护眼时用 `companion.start()`（`ContextCompat.startForegroundService`）启动，`onCreate` 立即 `startForeground`，`onStartCommand` 返回 `START_STICKY`——因此**退出 App/划掉最近任务后服务与遮罩仍持续**。`MainActivity` 的绑定（`OverlayBinder`）仅用于 App 打开时实时调亮度（seekbar），解绑**不会**停止服务（`onUnbind` 里不做任何清理）。进程级状态标志 `companion.isActive`（`onCreate`=true、`stopOverlay/stop/onDestroy`=false）供 UI 判断开关。manifest `foregroundServiceType="health"` + `stopWithTask="false"`。关闭只能走显式路径：绑定态调 `stopOverlay()`（stopForeground + 撤通知 + stopSelf），未绑定态 `companion.stop()`（stopService）。需要通知权限（Android 13+），无权限时 `start()` 引导去设置并拒绝启动。
- `OverlayViewManager`（object 单例）：真正操作 `WindowManager` 的地方——`addView/removeView`。窗口参数：`TYPE_APPLICATION_OVERLAY`、`MATCH_PARENT`、`Gravity.FILL`、`FLAG_NOT_TOUCHABLE | FLAG_NOT_FOCUSABLE`（遮罩不拦截触摸）、沉浸式 + 刘海屏 `LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS`。
- 透明度映射：`OverlayViewManager.setBackground` 把 SeekBar 的 0–100 进度映射到 11 个预置黑色资源色（`lib_overlay_black_10` ~ `black_98`，见 `getColorId`），10% 一档。
- 通知渠道「Darcy护眼服务」，id 为 `${packageName}:channel_eye_protection`；`OverlayNotificationUtil.init(activityClass)` 由 app 在 `onCreate` 里传入 `MainActivity`，用于点通知跳回主界面。`cancelNotification()` 显式 `cancel(NOTIFICATION_ID)`，作为 `stopForeground` 之外撤通知的兜底（部分系统/ROM 不会自动清 FGS 通知）；`createNotification` 对 `targetPendingIntentClazz` 未 init（如系统冷启进程）做了空安全处理。

## 代码约定与注意事项

- 库内扩展工具统一放在各模块 `exts/` 包，如 `LogExts.kt`（`logV/logD/logW/logE`）、`ToastExts.kt`（`toasts`）、`TextViewExts.kt`（红绿着色）。
- 代码中存在 `darcyRefactor:` 前缀注释，标记作者重构时对某段逻辑的说明，阅读时可留意。
- `lib_access_skip` 代码大量使用 `kotlinx.coroutines`（Channel/Scope/delay），但两个模块的 `build.gradle.kts` 均**未显式声明** coroutines 依赖，目前靠传递依赖编译通过；若编译报缺类需显式添加。
- `MainActivity` 通过 `NotificationPermissionUtil.NotificationPermissionRequester`（基于 ActivityResult API）申请通知权限，必须在 `onCreate` 中初始化。
- 无障碍事件分支以全 `when` 列出 `AccessibilityEvent` 各类型，新增事件类型要加到 `AutoSkipAccessibilityService.dealEvent()`。
