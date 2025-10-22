package com.example.notificationlistener.listener

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.notificationlistener.data.NotificationEvent
import com.example.notificationlistener.data.NotificationRepository

/**
 * 应用通知监听服务
 * 继承自NotificationListenerService，用于监听系统通知的发布和移除
 */
class AppNotificationListenerService : NotificationListenerService() {

    /**
     * 当通知监听器连接成功时调用
     * 更新仓库中的服务连接状态为true，并记录日志
     */
    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Notification listener connected")
        NotificationRepository.setServiceConnected(true)
    }

    /**
     * 当通知监听器断开连接时调用
     * 更新仓库中的服务连接状态为false，并记录日志
     */
    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "Notification listener disconnected")
        NotificationRepository.setServiceConnected(false)
    }

    /**
     * 当有新通知发布时调用
     * 将StatusBarNotification转换为NotificationEvent，添加到仓库中，并记录日志
     * @param sbn 状态栏通知对象
     */
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val event = sbn.toNotificationEvent() ?: return
        NotificationRepository.addOrReplace(event)
        Log.d(
            TAG,
            "Posted: package=${event.packageName}, title=${event.title}, text=${event.text}"
        )
    }

    /**
     * 当通知被移除时调用
     * 从仓库中移除对应的通知，并记录日志
     * @param sbn 被移除的状态栏通知对象（可能为null）
     */
    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        val key = sbn?.key ?: return
        NotificationRepository.removeByKey(key)
        Log.d(TAG, "Removed notification with key=$key")
    }

    /**
     * 将StatusBarNotification转换为NotificationEvent的扩展函数
     * 从通知中提取标题、正文等信息，创建NotificationEvent对象
     * @return 转换后的NotificationEvent对象，如果转换失败则返回null
     */
    private fun StatusBarNotification.toNotificationEvent(): NotificationEvent? {
        val notification = notification ?: return null
        val extras = notification.extras ?: return null

        // 尝试从不同字段获取通知标题
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_TITLE_BIG)?.toString()
        // 尝试从不同字段获取通知正文
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()

        return NotificationEvent(
            key = key, // 使用系统提供的唯一键
            packageName = packageName, // 发送通知的应用包名
            title = title, // 通知标题
            text = text, // 通知正文
            postedAt = postTime, // 通知发布时间
            isOngoing = notification.flags and Notification.FLAG_ONGOING_EVENT != 0 // 是否为持续通知
        )
    }

    companion object {
        private const val TAG = "NotificationListener" // 日志标签
    }
}
