package com.example.notificationlistener.listener

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.notificationlistener.data.NotificationEvent
import com.example.notificationlistener.data.NotificationRepository

class AppNotificationListenerService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Notification listener connected")
        NotificationRepository.setServiceConnected(true)
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "Notification listener disconnected")
        NotificationRepository.setServiceConnected(false)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val event = sbn.toNotificationEvent() ?: return
        NotificationRepository.addOrReplace(event)
        Log.d(
            TAG,
            "Posted: package=${event.packageName}, title=${event.title}, text=${event.text}"
        )
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        val key = sbn?.key ?: return
        NotificationRepository.removeByKey(key)
        Log.d(TAG, "Removed notification with key=$key")
    }

    private fun StatusBarNotification.toNotificationEvent(): NotificationEvent? {
        val notification = notification ?: return null
        val extras = notification.extras ?: return null

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_TITLE_BIG)?.toString()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()

        return NotificationEvent(
            key = key,
            packageName = packageName,
            title = title,
            text = text,
            postedAt = postTime,
            isOngoing = notification.flags and Notification.FLAG_ONGOING_EVENT != 0
        )
    }

    companion object {
        private const val TAG = "NotificationListener"
    }
}
