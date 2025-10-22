package com.example.notificationlistener

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.notificationlistener.data.NotificationEvent
import com.example.notificationlistener.data.NotificationRepository
import com.example.notificationlistener.ui.theme.NotificationListenerTheme
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

/**
 * 主活动类 - 应用程序的入口点
 * 负责设置 Compose UI 并显示通知监听器界面
 */

class MainActivity : ComponentActivity() {
    /**
     * 活动创建时调用
     * 设置内容视图并应用主题
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 应用自定义主题
            NotificationListenerTheme {
                // 显示主应用界面
                NotificationListenerApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationListenerApp() {
    // 获取当前上下文和生命周期所有者
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 从仓库收集服务连接状态和通知事件流
    val serviceConnected by NotificationRepository.serviceConnected.collectAsStateWithLifecycle()
    val events by NotificationRepository.events.collectAsStateWithLifecycle()

    // 记住通知访问权限状态
    var hasAccess by remember { mutableStateOf(hasNotificationAccess(context)) }

    // 在生命周期恢复时检查通知访问权限
    DisposableLifecycleObserver(lifecycleOwner = lifecycleOwner) { event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            hasAccess = hasNotificationAccess(context)
        }
    }

    // 创建带顶部应用栏的脚手架布局
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = context.getString(R.string.app_name)) }
            )
        }
    ) { padding ->
        // 主列布局，包含所有UI组件
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 通知访问权限卡片
            NotificationAccessCard(
                hasAccess = hasAccess,
                serviceConnected = serviceConnected,
                onOpenSettings = { openNotificationAccessSettings(context) }
            )

            // 通知列表卡片
            NotificationListCard(
                events = events,
                onClear = { NotificationRepository.clear() }
            )
        }
    }
}

@Composable
private fun NotificationAccessCard(
    hasAccess: Boolean, // 是否已授予通知访问权限
    serviceConnected: Boolean, // 服务是否已连接
    onOpenSettings: () -> Unit // 打开设置的回调函数
) {
    // 创建一个凸起的卡片来显示通知访问状态
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 显示通知访问权限状态（启用/禁用）
            Text(
                text = if (hasAccess) {
                    stringResource(R.string.notification_access_enabled)
                } else {
                    stringResource(R.string.notification_access_disabled)
                },
                style = MaterialTheme.typography.titleMedium,
                // 根据权限状态设置颜色：启用为primary色，禁用为error色
                color = if (hasAccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )

            // 显示服务连接状态
            Text(
                text = if (serviceConnected) {
                    stringResource(R.string.service_connected)
                } else {
                    stringResource(R.string.service_not_connected)
                },
                style = MaterialTheme.typography.bodyMedium
            )

            // 打开通知访问设置的按钮
            Button(onClick = onOpenSettings) {
                Text(text = stringResource(R.string.open_settings))
            }
        }
    }
}

@Composable
private fun NotificationListCard(
    events: List<NotificationEvent>, // 通知事件列表
    onClear: () -> Unit // 清除通知的回调函数
) {
    // 创建一个凸起的卡片来显示捕获的通知列表
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 顶部行：标题和清除按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 标题文本
                Text(
                    text = stringResource(R.string.captured_notifications),
                    style = MaterialTheme.typography.titleMedium
                )
                // 清除按钮（仅在有通知时启用）
                TextButton(onClick = onClear, enabled = events.isNotEmpty()) {
                    Text(text = stringResource(R.string.clear_notifications))
                }
            }

            // 如果没有通知，显示提示文本
            if (events.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_notifications),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                // 使用LazyColumn高效显示通知列表
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(events, key = { it.key }) { event ->
                        // 为每个通知事件显示一行
                        NotificationRow(event = event)
                        // 在每个通知之间添加分隔线
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(event: NotificationEvent) {
    // 为单个通知事件创建列布局
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // 通知标题行：包含标题和时间
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 通知标题（如果为空则显示包名）
            Text(
                text = event.title.orEmpty().ifBlank { event.packageName },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            // 通知发布时间
            Text(
                text = event.postedAt.formatAsTime(),
                style = MaterialTheme.typography.bodySmall
            )
        }
        // 通知正文（如果存在且不为空）
        if (!event.text.isNullOrBlank()) {
            Text(
                text = event.text,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
        // 包名标签
        Text(
            text = stringResource(R.string.package_label, event.packageName),
            style = MaterialTheme.typography.bodySmall
        )
        // 如果是持续通知，显示特殊标记
        if (event.isOngoing) {
            Text(
                text = stringResource(R.string.ongoing_notification),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

private fun hasNotificationAccess(context: Context): Boolean {
    // 获取已启用通知监听器的包名列表
    val enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(context)
    // 检查当前应用是否在列表中
    return enabledPackages.contains(context.packageName)
}

private fun openNotificationAccessSettings(context: Context) {
    // 打开通知监听器设置页面
    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    })
}

@Composable
private fun DisposableLifecycleObserver(
    lifecycleOwner: androidx.lifecycle.LifecycleOwner, // 生命周期所有者
    onEvent: (Lifecycle.Event) -> Unit // 生命周期事件回调
) {
    // 创建一个可处理的生命周期观察器
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            onEvent(event)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

@Composable
private fun <T> StateFlow<T>.collectAsStateWithLifecycle(): State<T> {
    // 将StateFlow转换为与生命周期感知的状态
    val lifecycleOwner = LocalLifecycleOwner.current
    val stateFlow = this
    val initialValue = stateFlow.value
    val state = remember { mutableStateOf(initialValue) }
    DisposableEffect(lifecycleOwner) {
        val job = lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                stateFlow.collect { value -> state.value = value }
            }
        }
        onDispose { job.cancel() }
    }
    return state
}

private fun Long.formatAsTime(): String =
    // 将时间戳格式化为短日期时间格式
    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(this))
