# Android 通知监听器

一个Android示例应用，通过`NotificationListenerService`捕获状态栏通知（例如微信），在Compose UI中显示，并记录元数据用于调试。

## 核心概念：NotificationListenerService

`NotificationListenerService`是Android系统提供的一个特殊服务，允许应用监听设备上所有通知的发布和移除事件。这是实现通知监听功能的核心机制。

**工作原理：**
1. 应用需要声明一个继承自`NotificationListenerService`的服务类
2. 用户必须在系统设置中明确授予该应用通知访问权限
3. 一旦授权，系统会在有新通知发布或现有通知被移除时回调服务的方法
4. 服务可以提取通知的详细信息（如标题、正文、包名、时间戳等）
5. 这些信息可以被存储、显示或用于其他业务逻辑

**主要优势：**
- **系统级访问**：能够捕获设备上所有应用的通知（用户授权后）
- **实时性**：通知事件几乎是实时传递的
- **丰富信息**：可以获取通知的完整元数据
- **无root需求**：不需要设备root权限，只需用户授权

**注意事项：**
- 需要用户手动在系统设置中授予权限
- 受到Android系统的隐私保护限制
- 在某些定制ROM或安全软件中可能被限制
- 应用被强制停止或系统重启后需要重新连接

## 前提条件
- 已安装macOS 12+、Android Studio（最新稳定版）和Android SDK API 34。
- Java 17（随Android Studio捆绑）。
- 可选：已启用USB调试的物理Android设备，或通过Android Studio配置的模拟器。
- 在设备/模拟器上安装了微信（或任何目标应用）用于测试。

请参阅`plan.md`获取完整的环境准备指南，包括模拟器创建和APK侧载提示。

## 开始使用
1. **克隆仓库**
   ```bash
   git clone https://github.com/nomadicwind/android-notification-listener.git
   cd android-notification-listener
   ```
2. **在Android Studio中打开**
   - 选择*打开现有项目*并选择此目录。
   - 让Gradle同步；根据提示安装任何缺失的SDK组件。
3. **构建**
   - 选择已连接的设备或模拟器。
   - 在Android Studio中按下**同步项目与Gradle文件**，然后**运行▶**，或从终端执行`./gradlew installDebug`。
4. **启动应用**
   - 从Android Studio使用**运行▶**，或在设备上从启动器打开新安装的*通知监听器*应用。
   - 如果使用命令行，请在`installDebug`完成后手动启动应用。

## 授予通知访问权限
1. 在您的设备/模拟器上启动应用。
2. 点击应用中的**打开设置**，或导航到**设置▸通知▸通知访问**。
3. 为*通知监听器*启用访问权限。
4. 触发一个通知（例如，发送一条微信消息）并验证它是否出现在应用列表和Logcat中。

## 项目结构
```
app/
 ├── src/main/java/com/example/notificationlistener/
 │    ├── data/NotificationRepository.kt        # 捕获通知的内存存储
 │    ├── listener/AppNotificationListenerService.kt
 │    ├── ui/theme/                             # Compose Material 3主题设置
 │    └── MainActivity.kt                       # Compose UI和权限流程
 ├── src/main/res/                              # 资源（字符串、图标、主题）
 └── build.gradle.kts                           # 启用Compose的模块配置
```

## 测试与工具
- 运行单元测试：`./gradlew test`
- 运行仪器测试（需要模拟器/设备）：`./gradlew connectedAndroidTest`
- 静态分析/代码检查：`./gradlew lint`

## 下一步与增强功能
- 持久化捕获的通知（Room, DataStore）。
- 添加过滤器（例如仅微信切换）和搜索功能。
- 导出或同步通知到其他服务。
- 加强隐私保护：删除敏感数据，为持久化/导出添加选择加入功能。

## 故障排除
- **服务未接收通知：** 撤销并重新启用通知访问权限；确保服务在设置中显示为已启用。
- **模拟器上的微信：** 使用Google Play系统镜像或通过`adb install`侧载官方APK。
- **Gradle同步问题：** 验证您安装了匹配的Android Gradle插件和SDK版本（AGP 8.2.2与Gradle 8.2）。
