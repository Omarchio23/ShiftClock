package es.aipp.shiftclock.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import es.aipp.shiftclock.R
import es.aipp.shiftclock.data.AlarmEntity
import es.aipp.shiftclock.data.PatternType
import es.aipp.shiftclock.logic.ShiftPatternData
import es.aipp.shiftclock.logic.WeeklyPatternData
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAlarmScreen(
    alarmId: Int?,
    viewModel: MainScreenViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val defaultTitle = stringResource(R.string.default_alarm_title)
    var title by remember { mutableStateOf(defaultTitle) }
    
    // States for Shift Pattern
    var daysOn by remember { mutableStateOf("4") }
    var daysOff by remember { mutableStateOf("2") }
    var startDateMillis by remember { mutableStateOf(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()) }
    
    // States for Weekly Pattern
    var selectedDays by remember { mutableStateOf(setOf<Int>()) } // 1..7
    
    var selectedType by remember { mutableStateOf(PatternType.WEEKLY) }

    var showDatePicker by remember { mutableStateOf(false) }
    var existingAlarm by remember { mutableStateOf<AlarmEntity?>(null) }
    var isLoading by remember { mutableStateOf(alarmId != null) }

    var isSnoozeEnabled by remember { mutableStateOf(true) }
    var snoozeDurationMinutes by remember { mutableStateOf("10") }
    var isVibrateEnabled by remember { mutableStateOf(true) }

    val timePickerState = key(existingAlarm?.id) {
        rememberTimePickerState(
            initialHour = existingAlarm?.hour ?: 6,
            initialMinute = existingAlarm?.minute ?: 0,
            is24Hour = true
        )
    }

    LaunchedEffect(alarmId) {
        if (alarmId != null) {
            val alarm = viewModel.getAlarmById(alarmId)
            if (alarm != null) {
                existingAlarm = alarm
                title = alarm.title
                selectedType = alarm.type
                
                if (alarm.type == PatternType.SHIFT_CYCLIC) {
                    val data = Gson().fromJson(alarm.patternData, ShiftPatternData::class.java)
                    daysOn = data.daysOn.toString()
                    daysOff = data.daysOff.toString()
                    startDateMillis = data.startDateMillis
                } else if (alarm.type == PatternType.WEEKLY) {
                    val data = Gson().fromJson(alarm.patternData, WeeklyPatternData::class.java)
                    selectedDays = data.activeDays.toSet()
                }
                
                isSnoozeEnabled = alarm.isSnoozeEnabled
                snoozeDurationMinutes = alarm.snoozeDurationMinutes.toString()
                isVibrateEnabled = alarm.isVibrateEnabled
            }
            isLoading = false
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { startDateMillis = it }
                    showDatePicker = false
                }) { Text(stringResource(R.string.accept)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.cancel)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (alarmId == null) stringResource(R.string.new_alarm) else stringResource(R.string.edit_alarm)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.alarm_title_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                PrimaryTabRow(
                    selectedTabIndex = if (selectedType == PatternType.WEEKLY) 0 else 1,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedType == PatternType.WEEKLY,
                        onClick = { selectedType = PatternType.WEEKLY },
                        text = { Text(stringResource(R.string.tab_by_days)) }
                    )
                    Tab(
                        selected = selectedType == PatternType.SHIFT_CYCLIC,
                        onClick = { selectedType = PatternType.SHIFT_CYCLIC },
                        text = { Text(stringResource(R.string.tab_by_pattern)) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Text(stringResource(R.string.select_time_label), style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                TimeInput(state = timePickerState)
                
                Spacer(modifier = Modifier.height(16.dp))

                if (selectedType == PatternType.WEEKLY) {
                    Text(stringResource(R.string.repeat_days), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val daysLabels = listOf(
                            stringResource(R.string.day_mon), 
                            stringResource(R.string.day_tue), 
                            stringResource(R.string.day_wed), 
                            stringResource(R.string.day_thu), 
                            stringResource(R.string.day_fri), 
                            stringResource(R.string.day_sat), 
                            stringResource(R.string.day_sun)
                        )
                        for (i in 1..7) {
                            val isSelected = selectedDays.contains(i)
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        if (isSelected) selectedDays = selectedDays - i
                                        else selectedDays = selectedDays + i
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = daysLabels[i - 1],
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                } else {
                    Text(stringResource(R.string.pattern_config), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedTextField(
                            value = daysOn,
                            onValueChange = { daysOn = it },
                            label = { Text(stringResource(R.string.days_on)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        )
                        OutlinedTextField(
                            value = daysOff,
                            onValueChange = { daysOff = it },
                            label = { Text(stringResource(R.string.days_off)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).padding(start = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val dateFormatted = Instant.ofEpochMilli(startDateMillis).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        Text(stringResource(R.string.start_date, dateFormatted))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Text(stringResource(R.string.snooze_config), style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.snooze_label), style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = isSnoozeEnabled,
                        onCheckedChange = { isSnoozeEnabled = it }
                    )
                }
                if (isSnoozeEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = snoozeDurationMinutes,
                        onValueChange = { snoozeDurationMinutes = it },
                        label = { Text(stringResource(R.string.snooze_duration_label)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.vibrate_label), style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = isVibrateEnabled,
                        onCheckedChange = { isVibrateEnabled = it }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        val jsonPattern = if (selectedType == PatternType.WEEKLY) {
                            Gson().toJson(WeeklyPatternData(selectedDays.toList()))
                        } else {
                            val dOn = daysOn.toIntOrNull() ?: 0
                            val dOff = daysOff.toIntOrNull() ?: 0
                            Gson().toJson(ShiftPatternData(dOn, dOff, startDateMillis))
                        }

                        val newAlarm = AlarmEntity(
                            id = existingAlarm?.id ?: 0,
                            title = title,
                            type = selectedType,
                            hour = timePickerState.hour,
                            minute = timePickerState.minute,
                            patternData = jsonPattern,
                            isActive = true,
                            isSnoozeEnabled = isSnoozeEnabled,
                            snoozeDurationMinutes = snoozeDurationMinutes.toIntOrNull() ?: 10,
                            isVibrateEnabled = isVibrateEnabled
                        )
                        if (existingAlarm == null) {
                            viewModel.insertAndScheduleAlarm(newAlarm)
                        } else {
                            viewModel.updateAndScheduleAlarm(newAlarm)
                        }
                        onNavigateBack()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text(if (alarmId == null) stringResource(R.string.create_alarm_btn) else stringResource(R.string.save_changes_btn), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
