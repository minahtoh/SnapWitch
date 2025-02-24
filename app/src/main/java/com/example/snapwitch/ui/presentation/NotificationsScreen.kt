package com.example.snapwitch.ui.presentation

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissState
import androidx.compose.material.DismissValue
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.modifier.modifierLocalMapOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.snapwitch.R
import com.example.snapwitch.notifications.NotificationData
import com.example.snapwitch.viewmodel.SnapWitchRepository
import com.example.snapwitch.viewmodel.SnapWitchViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// @Preview(widthDp = 320)
@Composable
fun NotificationsScreen(
    modifier: Modifier = Modifier,
    onHomeIconClick : () -> Unit = {},
    onMenuIconClick : () -> Unit = {},
    onNotificationsIconClick : () -> Unit = {},
    onDeleteNotification : (NotificationData) -> Unit = {},
    onBackIconClick: ()->Unit = {},
    onDeleteIconClicked: () -> Unit = {},
    viewModel: SnapWitchViewModel
){
    val notificationsList by remember {
        viewModel.snapWitchNotifications
    }.collectAsState(initial = emptyList())

    val isListEmpty by remember {
       derivedStateOf{ notificationsList.isEmpty() }
    }
    
    DisposableEffect(Unit){
        onDispose {
            viewModel.newNotificationAdded.value = false
        }
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

    Surface(color = MaterialTheme.colorScheme.surface) {
        val statusBarController = rememberStatusBarController()
        val statusBarColor = MaterialTheme.colorScheme.surfaceTint
        var deleteInProgress by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        // Track deletion state for each item
        val notificationStates = remember(notificationsList) {
            notificationsList.map { mutableStateOf(false) }.toMutableList()
        }


        LaunchedEffect(Unit){
            statusBarController.updateStatusBar(
                color = statusBarColor,
                darkIcons = true
            )
        }
        Column(
            modifier = modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = MaterialTheme.colorScheme.onSurface
                    )
            ) {
                NotificationsTopBar(
                    onDeleteIconClicked = {
                        if (!deleteInProgress) {
                            deleteInProgress = true
                            scope.launch {
                                // Trigger cascading deletion animation
                                notificationStates.forEachIndexed { index, state ->
                                    delay(5L * index) // Stagger the animations
                                    state.value = true
                                }
                                // Wait for last animation to complete
                                delay(100)
                                onDeleteIconClicked()
                                deleteInProgress = false
                            }
                        }
                    },
                    onBackIconClick = onBackIconClick,
                    isListEmpty = isListEmpty
                )
                Divider(
                    modifier = Modifier
                        .height(2.dp))
                Column {
                    if (notificationsList.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.baseline_notifications_none_24),
                                contentDescription = "No Notifications",
                                modifier = Modifier.size(120.dp),
                                colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.inversePrimary)
                            )
                            Text(
                                text = "No new notifications! \uD83C\uDF89",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        }
                    } else{
                        LazyColumn(
                            //modifier = Modifier.padding(top = 2.dp),
                            verticalArrangement = Arrangement.spacedBy(15.dp),
                            contentPadding = PaddingValues(
                                start = 25.dp,
                                top = 10.dp,
                                end = 25.dp,
                                bottom = 10.dp
                            )
                        ) {
                            itemsIndexed(
                                items = notificationsList,
                                key = { index, item -> System.identityHashCode(item) }
                            ) { index, _ ->
                                val notification = notificationsList[index]
                                var isVisible by remember { mutableStateOf(true) }

                                val isDeleting by notificationStates[index]

                                // Animate both slide and fade
                                val offsetTransition = updateTransition(
                                    targetState = isDeleting,
                                    label = "offset"
                                )

                                val offsetX by offsetTransition.animateFloat(
                                    label = "offsetX",
                                    transitionSpec = {
                                        tween(
                                            durationMillis = 300,
                                            easing = FastOutSlowInEasing
                                        )
                                    }
                                ) { deleting ->
                                    if (deleting) 1000f else 0f
                                }

                                val alpha by offsetTransition.animateFloat(
                                    label = "alpha",
                                    transitionSpec = {
                                        tween(
                                            durationMillis = 300,
                                            easing = LinearEasing
                                        )
                                    }
                                ) { deleting ->
                                    if (deleting) 0f else 1f
                                }
                                SwipeToDeleteNotificationContainer(
                                    notification = notification,
                                    onDelete = {
                                        isVisible = false
                                        viewModel.deleteNotification(it)
                                    }
                                ) {
                                    val dateList = notification.repeatDays.map {
                                        val dayInMillis = getNextDayInMillis(it, 12, 0)
                                        SimpleDateFormat("EEEE (MMMM dd)", Locale.getDefault()).format(
                                            dayInMillis
                                        )
                                    }.toSet()

                                    NotificationsCard(
                                        modifier = Modifier
                                            .offset(x = offsetX.dp)
                                            .alpha(alpha),
                                        notificationTitle = notification.title,
                                        notificationMessage = notification.message,
                                        timestamp = notification.time,
                                        notificationIcon = when (notification.icon) {
                                            "warning" -> Icons.Default.Warning
                                            "turn On" -> Icons.Default.CheckCircle
                                            "turn Off" -> Icons.Default.Close
                                            else -> Icons.Default.Info
                                        },
                                        repeatDays = dateList.toList()
                                    )
                                }
                            }
                        }
                    }
                }
                }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.onSurface)
                    .padding(bottom = 20.dp, top = 15.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                SnapWitchBottomBar(
                    onHomeIconClick = onHomeIconClick,
                    onMenuIconClick = onMenuIconClick,
                    onNotificationsIconClick = onNotificationsIconClick
                )
            }
        }

        }
    }



//@Preview(widthDp = 320)
@Composable
fun NotificationsTopBar(
    onDeleteIconClicked: ()-> Unit = {},
    onBackIconClick : () -> Unit = {},
    isListEmpty : Boolean
){
    Surface(
        color = MaterialTheme.colorScheme.surfaceTint,
        shadowElevation = 10.dp,
        tonalElevation = 10.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 25.dp, top = 15.dp, end = 15.dp, bottom = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SnapWitchArrowHead(
                modifier = Modifier.rotate(180f),
                contentColor = MaterialTheme.colorScheme.inversePrimary,
                backgroundColor = MaterialTheme.colorScheme.surfaceTint,
                onClick = onBackIconClick,
                size = 35.dp
            )
            Text(
                modifier = Modifier,
                text = "Notifications",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimary
            )
            Image(
                painter = painterResource(id = R.drawable.baseline_delete_forever_24),
                contentDescription = null,
                colorFilter = ColorFilter.tint(
                    color = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .clickable {
                        onDeleteIconClicked()
                    }
                    .graphicsLayer {
                        alpha = if (isListEmpty) {
                            0.3f
                        } else {
                            1.0f
                        }
                    }
            )
        }
    }
}

@Preview(widthDp = 320)
@Composable
fun NotificationsCard(
    modifier: Modifier = Modifier,
    notificationTitle : String = "",
    notificationMessage: String = "",
    notificationIcon : ImageVector = Icons.Default.Info,
    repeatDays: List<String> = emptyList(),
    timestamp: Long = System.currentTimeMillis(),
){
    var isExpanded by remember {
        mutableStateOf(false)
    }

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(5.dp),
        shadowElevation = 7.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !repeatDays.isEmpty()) {
                    isExpanded = !isExpanded
                }
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(15.dp),
            ) {
                Icon(
                    imageVector = notificationIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Column() {
                    Text(
                        text = notificationTitle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Text(
                        text = notificationMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.inversePrimary
                    )
                    AnimatedVisibility(visible = isExpanded) {
                        Spacer(modifier = Modifier.height(7.dp))
                        Text(
                            text = "Schedule will repeat on ${repeatDays.joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.inversePrimary,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

            }
            Text(
                text = timeAgo(timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onError
            )
        }
    }
}

fun timeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        timestamp == 0L -> " "
        diff < 60_000 -> "Just now" // Less than 1 minute
        diff < 3_600_000 -> "${diff / 60_000} minutes ago" // Less than 1 hour
        diff < 86_400_000 -> "${diff / 3_600_000} hours ago" // Less than 1 day
        diff < 7 * 86_400_000 -> "${diff / 86_400_000} days ago" // Less than a week
        else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp)) // Show full date
    }
}



@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeToDeleteNotificationContainer(
    notification : NotificationData,
    onDelete : (NotificationData) -> Unit,
    animationDuration: Int = 500,
    content : @Composable () -> Unit
){
    var isDeleted by remember { mutableStateOf(false) }
    val state = rememberDismissState(
        confirmStateChange = { state ->
            if (state == DismissValue.DismissedToStart){
                isDeleted = true
                true
            } else false
        }
    )

    LaunchedEffect(key1 = isDeleted){
        if (isDeleted){
            delay(animationDuration.toLong())
            onDelete(notification)
        }
    }

    AnimatedVisibility(
        visible = !isDeleted,
        exit = shrinkVertically(
            animationSpec = tween(durationMillis = animationDuration),
            shrinkTowards = Alignment.Top
        ) + fadeOut()
    ) {
        SwipeToDismiss(
            state = state,
            directions = setOf(DismissDirection.EndToStart),
            background = {
                deleteBackground(swipeDismissState = state)
            }) {
            content()
        }
    }

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun deleteBackground(
    swipeDismissState : DismissState
){
    val color = if (swipeDismissState.dismissDirection == DismissDirection.EndToStart){
        MaterialTheme.colorScheme.error
    } else { Color.Transparent }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(16.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}