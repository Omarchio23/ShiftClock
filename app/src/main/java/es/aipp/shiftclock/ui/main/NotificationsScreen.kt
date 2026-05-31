package es.aipp.shiftclock.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.aipp.shiftclock.R
import es.aipp.shiftclock.data.AppDatabase
import es.aipp.shiftclock.data.NotificationEntity
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val notifications by database.notificationDao().getAllNotifications().collectAsState(initial = emptyList())

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.filter_all),
        stringResource(R.string.filter_stopped),
        stringResource(R.string.filter_snoozed)
    )

    val filteredNotifications = when (selectedTabIndex) {
        1 -> notifications.filter { it.eventType == "STOPPED" }
        2 -> notifications.filter { it.eventType == "SNOOZED" }
        else -> notifications
    }

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            text = stringResource(R.string.tab_notifications),
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                        ) 
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    )
                )
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (filteredNotifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_notifications), style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredNotifications) { notification ->
                    NotificationCard(notification = notification)
                }
            }
        }
    }
}

@Composable
fun NotificationCard(notification: NotificationEntity) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    val timeString = Instant.ofEpochMilli(notification.timestampMillis).atZone(ZoneId.systemDefault()).format(formatter)
    
    val (icon, titleRes, color) = when (notification.eventType) {
        "SNOOZED" -> Triple(Icons.Filled.Snooze, R.string.event_snoozed, MaterialTheme.colorScheme.tertiary)
        "STOPPED" -> Triple(Icons.Filled.Alarm, R.string.event_stopped, MaterialTheme.colorScheme.error)
        else -> Triple(Icons.Filled.Notifications, R.string.event_rang, MaterialTheme.colorScheme.primary)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = timeString,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${stringResource(titleRes)}: ${notification.alarmTitle}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (notification.eventType in listOf("STOPPED", "SNOOZED")) {
                    val statusStr = if (notification.eventType == "STOPPED") {
                        stringResource(R.string.history_stopped_by_user)
                    } else {
                        stringResource(R.string.history_snoozed_by_user)
                    }
                    val durationStr = stringResource(R.string.history_duration, notification.durationSeconds)
                    val vibratedStr = if (notification.vibrated) stringResource(R.string.history_vibration_on) else stringResource(R.string.history_vibration_off)
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = statusStr,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = durationStr,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = vibratedStr,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
