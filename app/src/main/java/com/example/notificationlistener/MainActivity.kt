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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotificationListenerTheme {
                NotificationListenerApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationListenerApp() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val serviceConnected by NotificationRepository.serviceConnected.collectAsStateWithLifecycle()
    val events by NotificationRepository.events.collectAsStateWithLifecycle()

    var hasAccess by remember { mutableStateOf(hasNotificationAccess(context)) }

    DisposableLifecycleObserver(lifecycleOwner = lifecycleOwner) { event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            hasAccess = hasNotificationAccess(context)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = context.getString(R.string.app_name)) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            NotificationAccessCard(
                hasAccess = hasAccess,
                serviceConnected = serviceConnected,
                onOpenSettings = { openNotificationAccessSettings(context) }
            )

            NotificationListCard(
                events = events,
                onClear = { NotificationRepository.clear() }
            )
        }
    }
}

@Composable
private fun NotificationAccessCard(
    hasAccess: Boolean,
    serviceConnected: Boolean,
    onOpenSettings: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (hasAccess) {
                    stringResource(R.string.notification_access_enabled)
                } else {
                    stringResource(R.string.notification_access_disabled)
                },
                style = MaterialTheme.typography.titleMedium,
                color = if (hasAccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )

            Text(
                text = if (serviceConnected) {
                    stringResource(R.string.service_connected)
                } else {
                    stringResource(R.string.service_not_connected)
                },
                style = MaterialTheme.typography.bodyMedium
            )

            Button(onClick = onOpenSettings) {
                Text(text = stringResource(R.string.open_settings))
            }
        }
    }
}

@Composable
private fun NotificationListCard(
    events: List<NotificationEvent>,
    onClear: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.captured_notifications),
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(onClick = onClear, enabled = events.isNotEmpty()) {
                    Text(text = stringResource(R.string.clear_notifications))
                }
            }

            if (events.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_notifications),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(events, key = { it.key }) { event ->
                        NotificationRow(event = event)
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(event: NotificationEvent) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = event.title.orEmpty().ifBlank { event.packageName },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = event.postedAt.formatAsTime(),
                style = MaterialTheme.typography.bodySmall
            )
        }
        if (!event.text.isNullOrBlank()) {
            Text(
                text = event.text,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = stringResource(R.string.package_label, event.packageName),
            style = MaterialTheme.typography.bodySmall
        )
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
    val enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(context)
    return enabledPackages.contains(context.packageName)
}

private fun openNotificationAccessSettings(context: Context) {
    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    })
}

@Composable
private fun DisposableLifecycleObserver(
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    onEvent: (Lifecycle.Event) -> Unit
) {
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
    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(this))
