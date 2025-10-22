package com.example.notificationlistener.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class NotificationEvent(
    val key: String,
    val packageName: String,
    val title: String?,
    val text: String?,
    val postedAt: Long,
    val isOngoing: Boolean
)

/**
 * In-memory store for notification events and service state.
 * For an MVP we use a singleton object; refactor to a proper repository when adding persistence.
 */
object NotificationRepository {

    private val _serviceConnected = MutableStateFlow(false)
    val serviceConnected: StateFlow<Boolean> = _serviceConnected.asStateFlow()

    private val _events = MutableStateFlow<List<NotificationEvent>>(emptyList())
    val events: StateFlow<List<NotificationEvent>> = _events.asStateFlow()

    fun setServiceConnected(isConnected: Boolean) {
        _serviceConnected.value = isConnected
    }

    fun addOrReplace(event: NotificationEvent) {
        _events.update { current ->
            (current.filterNot { it.key == event.key } + event)
                .sortedByDescending { it.postedAt }
        }
    }

    fun removeByKey(key: String) {
        _events.update { current ->
            current.filterNot { it.key == key }
        }
    }

    fun clear() {
        _events.value = emptyList()
    }
}
