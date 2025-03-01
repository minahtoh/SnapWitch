package com.example.snapwitch.ui.presentation

import android.content.Context
import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.snapwitch.db.SnapWitchFeatureDatabase
import com.example.snapwitch.ui.models.FeatureUsage
import com.example.snapwitch.ui.utils.PopupSnackbarHost
import com.example.snapwitch.ui.utils.rememberSnackbarState
import com.example.snapwitch.viewmodel.SnapWitchViewModel
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
//@Preview(widthDp = 320, heightDp = 840)
@Composable
fun SchedulerScreen(
    modifier: Modifier = Modifier,
    schedulerType : String = "",
    snapWitchViewModel: SnapWitchViewModel,
    onBackArrowClick: () -> Unit = {},
    onCancelButtonClick : () -> Unit = {},
    saveNotification : (String) -> Unit = {},
    saveNotificationOnRepeat: (String, List<String>) -> Unit
){
    val context = LocalContext.current
    var startTime by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }
    var endTime by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }
    val snackBarState = rememberSnackbarState()
    val scope = rememberCoroutineScope()

    fun timeStateToMillis(timeState: TimePickerState): Long {
        val calendar = Calendar.getInstance()

        calendar.set(Calendar.HOUR_OF_DAY, timeState.hour)
        calendar.set(Calendar.MINUTE, timeState.minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // If the calculated time is in the past, move to the next day
        if (calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return calendar.timeInMillis
    }

    fun getDayOfWeek(): String{
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    fun getNextDayInMillis(targetDay : String, hour : Int, minute: Int): Long {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        val daysOfWeek = mapOf(
            "sunday" to Calendar.SUNDAY,
            "monday" to Calendar.MONDAY,
            "tuesday" to Calendar.TUESDAY,
            "wednesday" to Calendar.WEDNESDAY,
            "thursday" to Calendar.THURSDAY,
            "friday" to Calendar.FRIDAY,
            "saturday" to Calendar.SATURDAY,
        )
        val targetDayLower = daysOfWeek[targetDay.lowercase(Locale.getDefault())]

        while (calendar.get(Calendar.DAY_OF_WEEK) != targetDayLower){
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

        calendar.add(Calendar.DAY_OF_YEAR, 1)
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)

        return calendar.timeInMillis
    }

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.inversePrimary
    ) {
        val statusBarController = rememberStatusBarController()
        val statusBarColor = MaterialTheme.colorScheme.primaryContainer
        var repeatDays by remember {
            mutableStateOf<List<String>>(emptyList())
        }
        LaunchedEffect(Unit){
            statusBarController.updateStatusBar(
                color = statusBarColor,
                darkIcons = true
            )
        }

        Column {
            SchedulerTopBar(
                schedulerType = schedulerType,
                onBackArrowClick = onBackArrowClick
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {

                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    ScheduleStarter(
                        endTime = {
                            endTime = timeStateToMillis(it)
                        },
                        startTime = {
                            startTime = timeStateToMillis(it)
                        },
                        schedulerType = schedulerType
                    )
                }
                Column(
                    modifier = Modifier
                        .padding(15.dp)
                ) {
                    Text(
                        text = "Repeat Schedule on",
                        modifier = Modifier.padding(start = 10.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    RepeatSection(
                        todaysDay = getDayOfWeek()
                    ) {
                        repeatDays = it
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 40.dp, bottom = 20.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Button(
                        onClick = {
                            onCancelButtonClick()
                        },
                        modifier = Modifier,
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonColors(
                            containerColor = MaterialTheme.colorScheme.onError,
                            contentColor = MaterialTheme.colorScheme.surfaceTint,
                            disabledContainerColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            disabledContentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        elevation = ButtonDefaults.buttonElevation(2.dp)
                    ) {
                        SnapWitchFailIcon(
                            backgroundColor = MaterialTheme.colorScheme.onError
                        )
                    }

                    Button(
                        onClick = {
                            val actionTypeExtra =
                                when (schedulerType) {
                                    "Network" -> "TOGGLE_DATA_NOTIFICATION"
                                    "Wifi" ->"TOGGLE_WIFI_NOTIFICATION"
                                    else -> "TOGGLE_BLUETOOTH_NOTIFICATION"
                                }
                            snapWitchViewModel.scheduleAction(
                                context,
                                actionTypeExtra,
                                startTime,
                                endTime
                            )

                            scope.launch {
                                val additionalTimeHour = Calendar.getInstance().apply { timeInMillis = endTime }.get(Calendar.HOUR_OF_DAY)
                                val additionalTimeMinute = Calendar.getInstance().apply { timeInMillis = endTime }.get(Calendar.MINUTE)
                                delay(2000)
                                snackBarState.showSnackbar(
                                    message = "$schedulerType Schedule Saved and Started!",
                                )
                                if (repeatDays.isEmpty()){
                                    saveNotification(schedulerType)
                                   // logFeatureUsage(context,schedulerType)
                                    logToFirebase(schedulerType,"setWithNoRepeat")

                                }else{
                                    repeatDays.forEach {
                                        snapWitchViewModel.scheduleActionOnRepeatDays(
                                            context,
                                            actionTypeExtra,
                                            nextDayInMillis = getNextDayInMillis(it,additionalTimeHour,additionalTimeMinute)
                                        )
                                    }
                                    saveNotificationOnRepeat(schedulerType,repeatDays)
                                    logToFirebase(schedulerType,"setWithRepeatOn${repeatDays}")
                                }

                                delay(500)
                                onBackArrowClick()
                            }

                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.inversePrimary,
                        ),
                        shape = RoundedCornerShape(25.dp),
                        elevation = ButtonDefaults.buttonElevation(2.dp)
                    ) {
                        SnapWitchSuccessIcon(
                            backgroundColor = MaterialTheme.colorScheme.tertiary,
                            onClick = {
                                val actionTypeExtra =
                                    when (schedulerType) {
                                        "Network" -> "TOGGLE_DATA_NOTIFICATION"
                                        "Wifi" ->"TOGGLE_WIFI_NOTIFICATION"
                                        else -> "TOGGLE_BLUETOOTH_NOTIFICATION"
                                    }
                                snapWitchViewModel.scheduleAction(
                                    context,
                                    actionTypeExtra,
                                    startTime,
                                    endTime
                                )
                                scope.launch {
                                    val additionalTimeHour = Calendar.getInstance().apply { timeInMillis = endTime }.get(Calendar.HOUR_OF_DAY)
                                    val additionalTimeMinute = Calendar.getInstance().apply { timeInMillis = endTime }.get(Calendar.MINUTE)
                                    delay(2000)
                                    snackBarState.showSnackbar(
                                        message = "$schedulerType Schedule Saved and Started!",
                                    )

                                    if (repeatDays.isEmpty()){
                                        saveNotification(schedulerType)
                                     //   logFeatureUsage(context,schedulerType)
                                        logToFirebase(schedulerType,"setWithNoRepeat")
                                    }else{
                                        repeatDays.forEach {
                                            snapWitchViewModel.scheduleActionOnRepeatDays(
                                                context,
                                                actionTypeExtra,
                                                nextDayInMillis = getNextDayInMillis(it,additionalTimeHour,additionalTimeMinute)
                                            )
                                        }
                                        saveNotificationOnRepeat(schedulerType,repeatDays)
                                        logToFirebase(schedulerType,"setWithRepeatOn${repeatDays}")
                                    }
                                    delay(500)
                                    onCancelButtonClick()
                                }
                            }
                        )
                    }
                }
            }
        }
            PopupSnackbarHost(
                snackbarState = snackBarState,
                modifier = Modifier.padding(bottom = 70.dp, start = 30.dp, end = 30.dp)
            )
    }
}


@Preview(widthDp = 320)
@Composable
fun SchedulerTopBar(
    modifier: Modifier = Modifier,
    schedulerType: String = "",
    onBackArrowClick : ()-> Unit = {}
){
    Surface(
        modifier = modifier,
        shadowElevation = 10.dp,
        tonalElevation = 10.dp,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiary
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SnapWitchArrowHead(
                modifier = Modifier.rotate(180f),
                contentColor = MaterialTheme.colorScheme.inverseSurface,
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                onClick = onBackArrowClick
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "$schedulerType Scheduler",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun ScheduleStarter(
    startTime : (TimePickerState) -> Unit = {},
    endTime : (TimePickerState) -> Unit = {},
    schedulerType: String = ""
){
    Column(
        modifier = Modifier.padding(15.dp)
    ) {
        Text(
            text = "$schedulerType Schedule",
            style = MaterialTheme.typography.bodyLarge,
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(15.dp),
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.inversePrimary)
                .padding(start = 60.dp, end = 60.dp, top = 15.dp)
        ) {
            TimeCard(
                identifier = "Start",
                selectedTime = startTime,
            )
            Spacer(modifier = Modifier.height(10.dp))
            TimeCard(
                identifier = "End",
                selectedTime = endTime
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview(widthDp = 320)
@Composable
fun TimeCard(
    modifier: Modifier = Modifier,
    identifier: String = "",
    selectedTime: (TimePickerState) -> Unit = {},
){
    var timeText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    var showTimePicker by remember { mutableStateOf(false) }
    var placeHolder by remember{ mutableStateOf("Enter time") }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(15.dp),
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        shadowElevation = 7.dp
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = identifier + " Time",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                    TextField(
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        value = timeText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showTimePicker = true
                            },
                        onValueChange = { },
                        readOnly = true,
                        enabled = false,
                        placeholder = {
                            Text(
                                text = placeHolder,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.surfaceTint
                            )
                        },
                    )
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }

                if (showTimePicker){
                    SnapWitchTimePickerDialog(
                        onConfirm = {
                            selectedTime(it)
                            showTimePicker = false
                            placeHolder = formatTime(it)
                                    },
                        onDismiss = {showTimePicker = false}
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
fun formatTime(timePickerState: TimePickerState): String {
    val hour = timePickerState.hour
    val minute = timePickerState.minute

    val amPm = if (hour < 12) "am" else "pm"
    val formattedHour = if (hour % 12 == 0) 12 else hour % 12
    val formattedMinute = String.format(Locale.getDefault(), "%02d", minute)

    return "$formattedHour:$formattedMinute $amPm"
}



@Preview(widthDp = 320)
@Composable
fun RepeatSection(
    todaysDay : String = "Monday",
    selectedDays : (List<String>) -> Unit = {}
){
        val daysOfTheWeek = listOf(
            "Sunday",
            "Monday",
            "Tuesday",
            "Wednesday",
            "Thursday",
            "Friday",
            "Saturday"
        )
        Column(
            modifier = Modifier
                .padding(start = 35.dp, end = 35.dp, top = 15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var checkedDays by remember {
                mutableStateOf(List(daysOfTheWeek.size){false})
            }
            val toRepeatDays by derivedStateOf {
                daysOfTheWeek.filterIndexed{index, _-> checkedDays[index] }
            }

            daysOfTheWeek.forEachIndexed{ index,
                day ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp)
                        .shadow(elevation = 2.dp, RoundedCornerShape(5.dp))
                        .clickable(enabled = todaysDay != day) {
                            checkedDays = checkedDays
                                .toMutableList()
                                .also { it[index] = !it[index] }

                            selectedDays(toRepeatDays)
                        },
                    shape = RoundedCornerShape(10.dp),
                    color = if(!toRepeatDays.contains(day))
                        MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,

                        ) {
                        Checkbox(
                            checked = checkedDays[index],
                            onCheckedChange = { isChecked ->
                                checkedDays =
                                    checkedDays.toMutableList().also { it[index] = isChecked }

                            },
                            enabled = todaysDay != day,

                            )
                        Text(
                            text = day,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (todaysDay != day) {
                                MaterialTheme.colorScheme.surfaceVariant
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }
                }
            }

        }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnapWitchTimePickerDialog(
    onConfirm: (TimePickerState) -> Unit,
    onDismiss: () -> Unit
) {
    val currentTime = Calendar.getInstance()

    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = true
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.onTertiary
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        clockDialUnselectedContentColor = MaterialTheme.colorScheme.error
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "Dismiss",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Button(
                        onClick = { onConfirm(timePickerState) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text(
                            text = "Confirm",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

fun logToFirebase(schedulerType: String, actionType: String){
    val analytics = Firebase.analytics
    val bundle = Bundle().apply {
        putString("schedulerTypeSet", schedulerType )
    }
    analytics.logEvent(actionType, bundle)
}

fun logFeatureUsage(context: Context, feature: String) {
    val db = SnapWitchFeatureDatabase.getDatabase(context)
    val dao = db.getDao()

    CoroutineScope(Dispatchers.IO).launch {
        dao.insertUsage(FeatureUsage(settingName = feature, timeStamp = System.currentTimeMillis()))
    }
}


