package com.example.snapwitch

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.snapwitch.notifications.NotificationData
import com.example.snapwitch.notifications.SnapWitchDataStore
import com.example.snapwitch.ui.presentation.HomeScreen
import com.example.snapwitch.ui.presentation.MenuScreen
import com.example.snapwitch.ui.presentation.NotificationsScreen
import com.example.snapwitch.ui.presentation.SchedulerScreen
import com.example.snapwitch.ui.presentation.StatusBarProvider
import com.example.snapwitch.ui.theme.SnapWitchTheme
import com.example.snapwitch.viewmodel.SnapWitchRepository
import com.example.snapwitch.viewmodel.SnapWitchViewModel

class MainActivity : ComponentActivity() {
    private val REQUEST_NOTIFICATION_PERMISSION = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appViewModel : SnapWitchViewModel by viewModels{
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val repository = SnapWitchRepository(applicationContext)
                    val dataStoreManager = SnapWitchDataStore(applicationContext)

                    return SnapWitchViewModel(repository, dataStoreManager) as T
                }
            }
        }

        //huh?
        actionBar?.hide()

        installSplashScreen().setKeepOnScreenCondition{
           !appViewModel.isReady.value
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkNotificationPermission()
        }


        setContent {
            var isDarkMode by rememberSaveable { mutableStateOf(true) }

            SnapWitchTheme(
                darkTheme = isDarkMode
            ) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                   // color = MaterialTheme.colorScheme.background
                ) {
                    val navHostController = rememberNavController()
                    SnapWitchNavHost(
                        navHostController = navHostController,
                        appViewModel = appViewModel,
                        toggleDarkMode = {
                            isDarkMode = !isDarkMode
                        }
                    )

                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted
            // Proceed with your app logic (e.g., show notifications)
        } else {
            // Permission is not granted, request permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS,),
                REQUEST_NOTIFICATION_PERMISSION
            )
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with sending notifications
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                // Permission denied, show a message or disable notifications
                Toast.makeText(this, "Permission Needed to continue", Toast.LENGTH_SHORT).show()
            }
        }
    }
}



@Composable
fun SnapWitchNavHost(
    navHostController: NavHostController,
    modifier: Modifier = Modifier,
    appViewModel: SnapWitchViewModel,
    toggleDarkMode: () -> Unit
){
    val context = LocalContext.current
    StatusBarProvider {
        NavHost(
            navController = navHostController,
            startDestination = HomeScreen.route,
            modifier = modifier
        )
        {
            composable(route = HomeScreen.route) {
                HomeScreen(
                    onHomeIconClick = {
                        navHostController.navigateSingleTopTo(HomeScreen.route)
                    },
                    onNotificationsIconClick = {
                        navHostController.navigateSingleTopTo(NotificationsScreen.route)
                    },
                    onMenuIconClick = {
                        navHostController.navigateSingleTopTo(MenuScreen.route)
                    },
                    snapWitchViewModel = appViewModel,
                    navigateToDataUsage = {
                        appViewModel.goToDataUsage(context)
                    },
                    navigateToDataScheduler = {
                        navHostController.navigateToSchedulerScreen("Network")
                    },
                    navigateToBluetoothScheduler = {
                        navHostController.navigateToSchedulerScreen("Bluetooth")
                    },
                    toggleDarkMode = toggleDarkMode
                )
            }

            composable(route = MenuScreen.route) {
                MenuScreen(
                    onBluetoothSchedulerClick = {
                        navHostController.navigateToSchedulerScreen("Bluetooth")
                    },
                    onNetworkSchedulerClick = {
                        navHostController.navigateToSchedulerScreen("Network")
                    },
                    onWifiSchedulerClick = {
                        navHostController.navigateToSchedulerScreen("Wifi")
                    }
                )
            }

            composable(
                route = SchedulerScreen.routeWithArgs,
                arguments = SchedulerScreen.arguments
            ) {
                val argument = it.arguments?.getString(SchedulerScreen.schedulerTypeArg)
                SchedulerScreen(
                    schedulerType = argument ?: "",
                    snapWitchViewModel = appViewModel,
                    onBackArrowClick = { navHostController.popBackStack() },
                    saveNotification = {
                        appViewModel.saveNotification(
                            NotificationData(
                                message = "$it Scheduler Saved and Started",
                                title = "$it Scheduler",
                                icon = "warning",
                                time = System.currentTimeMillis()
                            )
                        )
                    },
                    saveNotificationOnRepeat = {schedulerType, list ->
                        appViewModel.saveNotification(
                            NotificationData(
                                message = "$schedulerType Scheduler Saved and Started",
                                title = "$schedulerType Scheduler",
                                icon = "warning",
                                time = System.currentTimeMillis(),
                                repeatDays = list
                            )
                        )
                    },
                    onCancelButtonClick = {
                        navHostController.navigateSingleTopTo(HomeScreen.route, true)
                    }
                )
            }

            composable(route = NotificationsScreen.route) {
                NotificationsScreen(
                    onHomeIconClick = {
                        navHostController.navigateSingleTopTo(HomeScreen.route, true)
                    },
                    onNotificationsIconClick = {
                        navHostController.navigateSingleTopTo(NotificationsScreen.route)
                    },
                    onMenuIconClick = {
                        navHostController.navigateSingleTopTo(MenuScreen.route)
                    },
                    viewModel = appViewModel,
                    onDeleteNotification = {
                        appViewModel.deleteNotification(it)
                    },
                    onBackIconClick = {
                        navHostController.popBackStack()
                    },
                    onDeleteIconClicked = {
                        appViewModel.clearNotification()
                    }
                )
            }
        }
    }
}
fun NavHostController.navigateSingleTopTo(route: String, popBackStack : Boolean = false) =
    this.navigate(route) {
        launchSingleTop = true
        if (popBackStack){popBackStack()}
    }

fun NavHostController.navigateToSchedulerScreen(schedulerType: String) =
    this.navigateSingleTopTo("${SchedulerScreen.route}/$schedulerType")



