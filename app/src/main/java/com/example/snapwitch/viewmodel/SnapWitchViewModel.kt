package com.example.snapwitch.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snapwitch.notifications.NotificationData
import com.example.snapwitch.notifications.SnapWitchDataStore
import com.example.snapwitch.receivers.SnapWitchReceiver
import com.example.snapwitch.ui.utils.PhoneDataUsageManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.log


class SnapWitchViewModel(
    private val snapWitchRepository: SnapWitchRepository,
    private val dataStoreManager: SnapWitchDataStore
): ViewModel() {
    private var _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()
    private var _networkStatus = MutableStateFlow(false)
    val networkStatus = _networkStatus.asStateFlow()
    private var _connectivityStatus = MutableStateFlow(
        SnapWitchRepository.ConnectivityStatus()
    )
    val connectivityStatus = _connectivityStatus.asStateFlow()
    private val _notificationsList = MutableStateFlow<List<NotificationData>>(emptyList())
    val snapWitchNotifications = _notificationsList.asStateFlow()
    private val _dataUsage = MutableStateFlow("")
    val dataUsage = _dataUsage.asStateFlow()
    private val _savedTime = MutableStateFlow(0L)
    val savedTime = _savedTime.asStateFlow()
    val newNotificationAdded = MutableStateFlow(false)


    init {
        viewModelScope.launch {
            delay(1300)
            _isReady.value = true
            checkNetworkStatus(snapWitchRepository)
            checkBluetoothAndWifiStatus(snapWitchRepository)

            dataStoreManager.notificationsFlow.collect { notificationsSet ->
                val oldList = _notificationsList.value.size
                val newList = notificationsSet.toList().reversed()

                _notificationsList.value = newList

                if (newList.size > oldList){
                    newNotificationAdded.value = true
                }
            }

        }
    }

     fun getDataUsage(context: Context) {
        viewModelScope.launch {
            val dataUsage = PhoneDataUsageManager(context).getTodayUsage()
            val mobileUsage = PhoneDataUsageManager(context).formatDataSize(dataUsage.mobileTotalBytes)
            val wifiUsage = PhoneDataUsageManager(context).formatDataSize(dataUsage.wifiTotalBytes)
            _dataUsage.value = mobileUsage

        }
    }


    private var dataFirstEmission = true

    private fun checkNetworkStatus(networkRepository: SnapWitchRepository){
        viewModelScope.launch {
            networkRepository.observeNetworkStatus()
                .collect {  isNetworkAvailable ->
                    // Ignore first emission to prevent unnecessary notifications
                    if (dataFirstEmission) {
                        dataFirstEmission = false
                        _networkStatus.emit(isNetworkAvailable)
                        return@collect
                    }
                    _networkStatus.emit(isNetworkAvailable)

                    if (isNetworkAvailable == networkStatus.value){
                        _savedTime.value = System.currentTimeMillis()
                        saveNotification(
                            NotificationData(
                                title = "Data Status",
                                message = if (isNetworkAvailable) "Mobile Data turned on" else "Mobile Data turned off",
                                time = _savedTime.value,
                                icon = if(isNetworkAvailable)"turn On" else "turn Off"
                            )
                        )
                    }
                }
        }
    }


    private var isFirstEmission = true

    private fun checkBluetoothAndWifiStatus(
        networkRepository: SnapWitchRepository
    ){
        viewModelScope.launch {
            networkRepository.observeConnectivityStatus().collect { status ->

                // Ignore first emission to prevent unnecessary notifications
                if (isFirstEmission) {
                    isFirstEmission = false
                    _connectivityStatus.value = status
                    return@collect
                }

                // Avoid duplicate notifications by checking if the status has actually changed
                if (status.isBluetoothEnabled != _connectivityStatus.value.isBluetoothEnabled) {
                    _savedTime.value = System.currentTimeMillis()
                    saveNotification(
                        NotificationData(
                            title = "Bluetooth Status",
                            message = if (status.isBluetoothEnabled) "Bluetooth turned on" else "Bluetooth turned off",
                            time = _savedTime.value,
                            icon = if(status.isBluetoothEnabled)"turn On" else "turn Off"
                        )
                    )
                }

                if (status.isWifiEnabled != _connectivityStatus.value.isWifiEnabled) {
                    saveNotification(
                        NotificationData(
                            title = "WiFi Status",
                            message = if (status.isWifiEnabled) "WiFi turned on" else "WiFi turned off",
                            time = System.currentTimeMillis() + 100L,
                            icon = if(status.isWifiEnabled)"turn On" else "turn Off"
                        )
                    )
                }
                    _connectivityStatus.value = status
                }
        }
    }


    fun scheduleActionFromHome(
        context: Context,
        actionType : String
    ){
        val intent = Intent(context, SnapWitchReceiver::class.java).apply {
            putExtra("ACTION_TYPE", actionType)
        }
        context.sendBroadcast(intent)
    }

    fun scheduleActionOnRepeatDays(
        context: Context,
        actionType: String,
        nextDayInMillis:Long
    ){
        val requestCode = (nextDayInMillis % Int.MAX_VALUE).toInt() // ✅ Unique per day

        val intent = Intent(context, SnapWitchReceiver::class.java).apply {
            putExtra("ACTION_TYPE", actionType)
        }
        val endPendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextDayInMillis,
                endPendingIntent
            )

        }catch (e: PendingIntent.CanceledException) {
            Log.e("PendingIntent", "PendingIntent was canceled: ${e.message}")
        }

    }

    fun scheduleAction(
        context: Context,
        actionType:String,
        startTime:Long ,
        endTime: Long
    ) {
        val intent = Intent(context, SnapWitchReceiver::class.java).apply {
            putExtra("ACTION_TYPE", actionType)
        }
        val endPendingIntent = PendingIntent.getBroadcast(
            context,
            actionType.hashCode(), // Unique request code per action
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val startPendingIntent = PendingIntent.getBroadcast(
            context,
            actionType.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        try {
            alarmManager.apply {
                setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    startTime,
                    startPendingIntent
                )
                setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    endTime,
                    endPendingIntent
                )
                val alarmSet = alarmManager.nextAlarmClock
            }

        } catch (e: PendingIntent.CanceledException) {
            Log.e("PendingIntent", "PendingIntent was canceled: ${e.message}")
        }
    }

    fun saveNotification(notification:NotificationData){
        viewModelScope.launch {
            dataStoreManager.saveNotification(notification)
        }
    }

    fun deleteNotification(notification:NotificationData){
        viewModelScope.launch {
            dataStoreManager.deleteNotification(notification)
        }
    }

    fun clearNotification(){
        viewModelScope.launch {
            dataStoreManager.clearNotifications()
        }
    }

    fun goToDataUsage(context: Context){
        val intent = Intent(Settings.ACTION_DATA_USAGE_SETTINGS)
        context.startActivity(intent)
    }


    override fun onCleared() {
        super.onCleared()

    }
}
