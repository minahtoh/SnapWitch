package com.example.snapwitch.ui.presentation

import android.app.Activity
import androidx.compose.animation.core.EaseOutQuad
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.snapwitch.R
import com.example.snapwitch.notifications.SnapWitchDataStore
import com.example.snapwitch.ui.theme.SnapWitchTheme
import com.example.snapwitch.viewmodel.SnapWitchRepository
import com.example.snapwitch.viewmodel.SnapWitchViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Calendar
import java.util.Locale

@Composable
fun HomeScreen(
    onHomeIconClick : () -> Unit = {},
    onMenuIconClick : () -> Unit = {},
    onNotificationsIconClick : () -> Unit = {},
    onDatesRowClick: () -> Unit,
    snapWitchViewModel: SnapWitchViewModel,
    navigateToBluetoothScheduler: () -> Unit = {},
    navigateToDataScheduler: () -> Unit = {},
    navigateToDataUsage: () -> Unit = {},
    toggleDarkMode : () -> Unit ={}
){
    val context = LocalContext.current
    val networkStatus by snapWitchViewModel.networkStatus.collectAsState()

    val connectivityStatus = snapWitchViewModel.connectivityStatus.collectAsState()
    val rememberedConnectivityStatus by remember {
        mutableStateOf(connectivityStatus)
    }
    val mobileDataUsage by snapWitchViewModel.dataUsage.collectAsState()

    val isNewNotificationAdded by snapWitchViewModel.newNotificationAdded.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        val statusBarController = rememberStatusBarController()
        val statusBarColor = MaterialTheme.colorScheme.surfaceTint

        LaunchedEffect(Unit){
            statusBarController.updateStatusBar(
                color = statusBarColor,
                darkIcons = true
            )
            snapWitchViewModel.getDataUsage(context)
        }
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            TopBarSection(
                modifier = Modifier,
                onAccountIconClick = toggleDarkMode
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    .padding(7.dp)
                    .weight(1f)
            ) {
                DateSection(
                    modifier = Modifier.fillMaxWidth(),
                    onDatesRowClick = { onDatesRowClick() }
                )
                Spacer(modifier = Modifier.height(5.dp))

                MainSection(
                    modifier = Modifier,
                    onBluetoothButtonClick = {
                        snapWitchViewModel.scheduleActionFromHome(
                            context = context,
                            actionType = "TOGGLE_BLUETOOTH"
                        )
                    } ,
                    onNetworkButtonClick = {
                        snapWitchViewModel.scheduleActionFromHome(
                            context = context,
                            actionType = "TOGGLE_DATA"
                        )
                    },
                    onWiFiButtonClick = {
                        snapWitchViewModel.scheduleActionFromHome(
                            context = context,
                            actionType = "TOGGLE_WIFI"
                        )
                    },
                    networkStatus = networkStatus,
                    connectivityStatus = rememberedConnectivityStatus.value,
                    navigateToBluetoothScheduler = navigateToBluetoothScheduler,
                    navigateToDataScheduler = navigateToDataScheduler,
                    navigateToDataUsage = navigateToDataUsage,
                    mobileDataUsage = mobileDataUsage,
                    lastBluetoothTriggeredTime = timeAgo(snapWitchViewModel.savedTime.value)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.onSurface)
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                SnapWitchBottomBar(
                    onHomeIconClick = onHomeIconClick,
                    onMenuIconClick = onMenuIconClick,
                    onNotificationsIconClick = onNotificationsIconClick,
                    newNotificationAdded = isNewNotificationAdded
                )
            }
        }
    }
}

@Composable
fun TopBarSection(
    modifier: Modifier,
    onAccountIconClick : () -> Unit
){
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.secondary)
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Today",
            color = MaterialTheme.colorScheme.onSecondary,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = getTodayDateFormatted(),
            color = MaterialTheme.colorScheme.onSecondary,
            style = MaterialTheme.typography.bodyLarge
        )
        Icon(
            modifier = Modifier.clickable {
              onAccountIconClick()
            },
            imageVector = Icons.Default.AccountCircle,
            contentDescription = null
        )
    }
}


fun getCurrentWeekFromDate(date: LocalDate): List<LocalDate> {
    val firstDayOfMonth = date.withDayOfMonth(1)
    val lastDayOfMonth = firstDayOfMonth.with(TemporalAdjusters.lastDayOfMonth())
    val firstSunday = firstDayOfMonth.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))

    var currentDay = firstSunday
    val weeks = mutableListOf<List<LocalDate>>()

    while (currentDay <= lastDayOfMonth) {
        val week = (0..6).map { currentDay.plusDays(it.toLong()) }
        weeks.add(week)
        currentDay = currentDay.plusWeeks(1)
    }

    val weekNumber = ((ChronoUnit.DAYS.between(firstSunday, date) / 7) + 1).toInt()
    return weeks[weekNumber - 1] // Adjust for zero-based index
}


@Preview(widthDp = 320)
@Composable
fun DateCirclesRow(
    dates: List<LocalDate> =  getCurrentWeekFromDate(LocalDate.now()),
    currentDate: String = getTodayDate(),
    onDatesRowClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        dates.forEach { date ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .padding(5.dp)
                        .background(
                            if (date.dayOfMonth == currentDate.toInt()) MaterialTheme.colorScheme.onSecondary
                            else MaterialTheme.colorScheme.inversePrimary
                        )
                        .clickable {
                            onDatesRowClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = date.dayOfMonth.toString(),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}



fun getTodayDateFormatted(): String {
    val dateFormat = SimpleDateFormat("MMMM d", Locale.getDefault())
    val today = Calendar.getInstance().time
    return dateFormat.format(today)
}
fun getTodayDate() : String{
    val dateFormat = SimpleDateFormat("d", Locale.getDefault())
    val today = Calendar.getInstance().time
    return dateFormat.format(today)
}


@Composable
fun DateSection(
    modifier: Modifier,
    onDatesRowClick: () -> Unit
){
    val days = listOf("S","M","T","W","T","F","S")
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().clickable { onDatesRowClick() },
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            days.forEach {
                Column(modifier = Modifier
                    .padding(top = 10.dp)
                    .size(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        }
        DateCirclesRow(
            onDatesRowClick = { onDatesRowClick() }
        )
    }
}

@Composable
fun MainSection(
    modifier: Modifier,
    onNetworkButtonClick : () -> Unit,
    onBluetoothButtonClick : () -> Unit,
    onWiFiButtonClick : () -> Unit,
    networkStatus : Boolean,
    connectivityStatus: SnapWitchRepository.ConnectivityStatus,
    navigateToDataScheduler : () -> Unit = {},
    navigateToBluetoothScheduler : () -> Unit = {},
    navigateToDataUsage : () -> Unit = {},
    mobileDataUsage : String = "",
    lastBluetoothTriggeredTime : String = "",
){
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(modifier = modifier.height(3.dp))
        Text(
            text = "Manage Connections",
            modifier = Modifier.padding(start = 10.dp),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.inverseOnSurface
        )
        Spacer(modifier = modifier.height(5.dp))
        Divider(
            modifier = Modifier
                .height(2.dp)
                .padding(start = 10.dp, end = 10.dp),

            )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
            SwitchCard(
                onClick = onNetworkButtonClick,
                cardTitle = if (networkStatus) {
                    "Network status: ON"
                } else {
                    "Network status: OFF"
                },
                cardIcon = {
                    SnapWitchNetworkIcon(
                        contentColor = if (!networkStatus)
                            MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.surfaceTint,
                        backgroundColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        size = 50.dp
                    )
                },
                buttonStatus = !networkStatus
            )
            TaskCard(
                taskTitle = "Schedule Data Connections",
                taskMessage = "Set times for data connection",
                onTaskPressed = navigateToDataScheduler,
                onClick = navigateToDataScheduler
            )
            TaskCard(
                taskTitle = "View Data Usage",
                taskMessage = "$mobileDataUsage of data used today",
                onTaskPressed = navigateToDataUsage,
                onClick = navigateToDataUsage
            )
            SwitchCard(
                cardTitle = if (connectivityStatus.isBluetoothEnabled) {
                    "Bluetooth status: ON"
                } else {
                    "Bluetooth status: OFF"
                },
                cardIcon = {
                    Image(
                        modifier = Modifier.size(30.dp),
                        painter = painterResource(id = R.drawable.baseline_settings_bluetooth_24),
                        contentDescription = null,
                        colorFilter = if (connectivityStatus.isBluetoothEnabled) {
                            ColorFilter.tint(
                                color = MaterialTheme.colorScheme.surfaceTint,
                            )
                        } else {
                            ColorFilter.tint(
                                color = MaterialTheme.colorScheme.tertiary,
                            )
                        }
                    )
                },
                onClick = onBluetoothButtonClick,
                buttonStatus = !connectivityStatus.isBluetoothEnabled
            )
            TaskCard(
                onTaskPressed = navigateToBluetoothScheduler,
                onClick = navigateToBluetoothScheduler,
                taskMessage = "Bluetooth status last changed $lastBluetoothTriggeredTime"
                )
            SwitchCard(
                cardTitle = if (connectivityStatus.isWifiEnabled) {
                    "Wifi status: ON"
                } else {
                    "Wifi status: OFF"
                },
                cardIcon = {
                    Image(
                        modifier = Modifier.size(30.dp),
                        painter = painterResource(id = R.drawable.baseline_wifi_24),
                        colorFilter = if (connectivityStatus.isWifiEnabled) {
                            ColorFilter.tint(
                                color = MaterialTheme.colorScheme.surfaceTint,
                            )
                        } else {
                            ColorFilter.tint(
                                color = MaterialTheme.colorScheme.tertiary,
                            )
                        },
                        contentDescription = null
                    )
                },
                onClick = onWiFiButtonClick,
                buttonStatus = !connectivityStatus.isWifiEnabled
            )
        }
    }
}

@Preview(widthDp = 320)
@Composable
fun TaskCard(
    onClick : ()->Unit = {},
    taskTitle : String = "Schedule Bluetooth Settings",
    taskMessage: String = "Bluetooth on at 12:30pm",
    onTaskPressed : ()->Unit = {}
){
    val mutableInteractionSource = remember {
        MutableInteractionSource()
    }
    val pressed = mutableInteractionSource.collectIsPressedAsState()
    val scope = rememberCoroutineScope()
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = animateDpAsState(
                    targetValue = if (pressed.value) {
                        10.dp
                    } else {
                        3.dp
                    },
                    animationSpec = tween(durationMillis = 500, easing = EaseOutQuad),
                    label = "elevation"
                ).value,
                RoundedCornerShape(10.dp)
            ),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceTint,
        border = BorderStroke( width = 4.dp, color = MaterialTheme.colorScheme.primary),

    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = mutableInteractionSource,
                    indication = null
                ) {
                    scope.launch {
                        delay(200)
                        onTaskPressed()
                    }

                }
                .padding(15.dp),
        ) {
            Column(modifier = Modifier.align(Alignment.TopStart)) {
                Text(
                    text = taskTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.inverseSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    modifier = Modifier.padding(start = 10.dp),
                    text = taskMessage,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.errorContainer

                )
            }
            SnapWitchArrowHead(
                modifier = Modifier.align(Alignment.TopEnd),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = onClick,
                size = 40.dp
            )
        }
    }
}

@Preview
@Composable
fun SwitchCard(
    cardTitle : String = "Data Connection Status : OFF",
    cardIcon : @Composable () -> Unit = {
        SnapWitchNetworkIcon(
            contentColor = MaterialTheme.colorScheme.tertiary,
            backgroundColor = MaterialTheme.colorScheme.onPrimaryContainer,
            size = 50.dp
        )
    },
    onClick: () -> Unit = {},
    buttonStatus : Boolean = false
){
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
            ) {
                Text(
                    text = cardTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primaryContainer
                )
                Spacer(modifier = Modifier.height(7.dp))
                Button(
                    onClick = { onClick() },
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = if(!buttonStatus) MaterialTheme.colorScheme.surfaceTint
                        else MaterialTheme.colorScheme.tertiary
                    )

                ) {
                    Text(
                        text = if(buttonStatus)
                                "Turn ON"
                            else "Turn OFF"
                        ,
                        style = MaterialTheme.typography.bodySmall,
                        color = if(!buttonStatus)
                            MaterialTheme.colorScheme.onErrorContainer
                                else
                            MaterialTheme.colorScheme.inversePrimary
                    )
                }
            }
            Column(
                modifier = Modifier.weight(2f),
                horizontalAlignment = Alignment.End
            )
            {
                cardIcon()
            }
        }
    }
}

@Preview
@Composable
fun SnapWitchBottomBar(
    modifier: Modifier = Modifier,
    onHomeIconClick : () -> Unit = {},
    onMenuIconClick : () -> Unit = {},
    onNotificationsIconClick : () -> Unit = {},
    newNotificationAdded : Boolean = false
){
    Surface(
        modifier = Modifier
            .width(300.dp)
            .shadow(elevation = 2.dp, RoundedCornerShape(30.dp)),
        color = MaterialTheme.colorScheme.tertiary,
        shape = RoundedCornerShape(30.dp),
        //shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .width(300.dp)
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onHomeIconClick
            ) {
                Icon(
                    imageVector = HomeScreen.icon,
                    contentDescription = null
                )
            }
            IconButton(
                onClick = onMenuIconClick
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_alarm_on_24),
                    contentDescription = null
                )
            }

            Box {
                if (newNotificationAdded){
                    Image(
                        painter = painterResource(id = R.drawable.baseline_circle_24),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 4.dp, end = 4.dp),
                        colorFilter = ColorFilter.tint(
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    )
                }
                SnapWitchListIcon(
                    contentColor = MaterialTheme.colorScheme.surface,
                    onClick = onNotificationsIconClick
                )
            }
        }
    }
}


//@Preview( widthDp = 340, heightDp = 620)
@Composable
fun HomeScreenPreview(snapWitchRepository: SnapWitchRepository, dataStore: SnapWitchDataStore){
    SnapWitchTheme {
        HomeScreen(
            snapWitchViewModel = SnapWitchViewModel(snapWitchRepository, dataStore),
            onDatesRowClick = {}
        )
    }
}


interface SnapWitchDestinations {
    val route : String
    val icon : ImageVector
}


object HomeScreen : SnapWitchDestinations{
    override val route: String = "home"
    override val icon: ImageVector = Icons.Default.Home
}
object MenuScreen : SnapWitchDestinations{
    override val route: String = "menu"
    override val icon: ImageVector = Icons.Default.Menu
}
object NotificationsScreen : SnapWitchDestinations{
    override val route: String = "notifications"
    override val icon: ImageVector = Icons.Default.Notifications
}
object SchedulerScreen : SnapWitchDestinations{
    override val route: String = "scheduler"
    override val icon: ImageVector = Icons.Default.DateRange
    const val schedulerTypeArg = "scheduler_type"
    val arguments = listOf(
        navArgument(schedulerTypeArg){type = NavType.StringType}
    )
    val routeWithArgs = "${route}/{${schedulerTypeArg}}"
}
object StatisticsScreen : SnapWitchDestinations {
    override val route: String = "statistics"
    override val icon: ImageVector = Icons.Default.Star
}



private val LocalStatusBarController = staticCompositionLocalOf<StatusBarController?> { null }

class StatusBarController(
    private val activity: Activity
) {
    fun updateStatusBar(
        color: Color,
        darkIcons: Boolean = true,
        isVisible: Boolean = true
    ) {
        val window = activity.window
        val controller = WindowInsetsControllerCompat(window, window.decorView)

        window.statusBarColor = color.toArgb()
        controller.isAppearanceLightStatusBars = darkIcons

        if (isVisible) {
            controller.show(WindowInsetsCompat.Type.statusBars())
        } else {
            controller.hide(WindowInsetsCompat.Type.statusBars())
        }
    }
}

@Composable
fun StatusBarProvider(content: @Composable () -> Unit) {
    val view = LocalView.current
    val controller = remember {
        if (!view.isInEditMode) {
            StatusBarController(view.context as Activity)
        } else null
    }

    CompositionLocalProvider(
        LocalStatusBarController provides controller
    ) {
        content()
    }
}

@Composable
fun rememberStatusBarController(): StatusBarController {
    return LocalStatusBarController.current
        ?: error("No StatusBarController found! Did you forget to wrap your content in StatusBarProvider?")
}
