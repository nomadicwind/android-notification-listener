package com.example.notificationlistener.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * 通知事件数据类
 * 包含捕获的通知的所有相关信息
 */
data class NotificationEvent(
    val key: String, // 通知的唯一标识符
    val packageName: String, // 发送通知的应用包名
    val title: String?, // 通知标题（可能为空）
    val text: String?, // 通知正文（可能为空）
    val postedAt: Long, // 通知发布时间（时间戳）
    val isOngoing: Boolean // 是否为持续通知
)

/**
 * 通知仓库 - 用于存储通知事件和服务状态的内存存储
 * 使用单例对象实现，未来添加持久化功能时可重构为正式的仓库模式
 */
object NotificationRepository {

    // 私有可变状态流，用于跟踪服务连接状态
    private val _serviceConnected = MutableStateFlow(false)
    // 公开的只读状态流，供UI观察服务连接状态
    val serviceConnected: StateFlow<Boolean> = _serviceConnected.asStateFlow()

    // 私有可变状态流，用于存储通知事件列表
    private val _events = MutableStateFlow<List<NotificationEvent>>(emptyList())
    // 公开的只读状态流，供UI观察通知事件列表
    val events: StateFlow<List<NotificationEvent>> = _events.asStateFlow()

    /**
     * 设置服务连接状态
     * @param isConnected 服务是否已连接
     */
    fun setServiceConnected(isConnected: Boolean) {
        _serviceConnected.value = isConnected
    }

    /**
     * 添加或替换通知事件
     * 如果已存在相同key的通知，则替换它；否则添加新通知
     * 所有通知按发布时间降序排列（最新在前）
     * @param event 要添加或替换的通知事件
     */
    fun addOrReplace(event: NotificationEvent) {
        _events.update { current ->
            (current.filterNot { it.key == event.key } + event)
                .sortedByDescending { it.postedAt }
        }
    }

    /**
     * 根据key移除通知事件
     * @param key 要移除的通知的唯一标识符
     */
    fun removeByKey(key: String) {
        _events.update { current ->
            current.filterNot { it.key == key }
        }
    }

    /**
     * 清空所有通知事件
     */
    fun clear() {
        _events.value = emptyList()
    }
}
