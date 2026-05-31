package es.aipp.shiftclock

import androidx.navigation.NavType
import androidx.navigation.navArgument
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.currentBackStackEntryAsState
import es.aipp.shiftclock.ui.settings.SettingsScreen
import es.aipp.shiftclock.ui.main.StopwatchScreen
import es.aipp.shiftclock.ui.main.TimerScreen
import es.aipp.shiftclock.ui.main.NotificationsScreen

import es.aipp.shiftclock.theme.ShiftClockTheme
import es.aipp.shiftclock.ui.main.CreateAlarmScreen
import es.aipp.shiftclock.ui.main.HomeScreen

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      ShiftClockTheme { 
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { 
          ShiftClockApp() 
        } 
      }
    }
  }
}



@Composable
fun ShiftClockApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = listOf("home", "stopwatch", "timer", "notifications")

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomBarRoutes) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Alarm, contentDescription = null) },
                        label = { Text(stringResource(R.string.tab_alarms)) },
                        selected = currentRoute == "home",
                        onClick = {
                            navController.navigate("home") {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Timer, contentDescription = null) },
                        label = { Text(stringResource(R.string.tab_stopwatch)) },
                        selected = currentRoute == "stopwatch",
                        onClick = {
                            navController.navigate("stopwatch") {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.HourglassEmpty, contentDescription = null) },
                        label = { Text(stringResource(R.string.tab_timer)) },
                        selected = currentRoute == "timer",
                        onClick = {
                            navController.navigate("timer") {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Notifications, contentDescription = null) },
                        label = { Text(stringResource(R.string.tab_notifications)) },
                        selected = currentRoute == "notifications",
                        onClick = {
                            navController.navigate("notifications") {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController, 
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    onCreateNewAlarm = { navController.navigate("create") },
                    onEditAlarm = { id -> navController.navigate("edit/$id") },
                    onNavigateToSettings = { navController.navigate("settings") }
                )
            }
            composable("stopwatch") {
                StopwatchScreen(onNavigateToSettings = { navController.navigate("settings") })
            }
            composable("timer") {
                TimerScreen(onNavigateToSettings = { navController.navigate("settings") })
            }
            composable("create") {
                CreateAlarmScreen(
                    alarmId = null,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                "edit/{alarmId}",
                arguments = listOf(navArgument("alarmId") { type = NavType.IntType })
            ) { backStackEntry ->
                val alarmId = backStackEntry.arguments?.getInt("alarmId")
                CreateAlarmScreen(
                    alarmId = alarmId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("settings") {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("notifications") {
                NotificationsScreen(
                    onNavigateToSettings = { navController.navigate("settings") }
                )
            }
        }
    }
}
