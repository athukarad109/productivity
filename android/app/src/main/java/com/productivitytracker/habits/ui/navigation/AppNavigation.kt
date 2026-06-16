package com.productivitytracker.habits.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.productivitytracker.habits.ui.screens.HabitDetailScreen
import com.productivitytracker.habits.ui.screens.HabitFormScreen
import com.productivitytracker.habits.ui.screens.HabitsScreen
import com.productivitytracker.habits.ui.screens.StatsScreen
import com.productivitytracker.habits.ui.screens.TodayScreen
import com.productivitytracker.habits.ui.screens.planner.CompareScreen
import com.productivitytracker.habits.ui.screens.planner.LogScreen
import com.productivitytracker.habits.ui.screens.planner.PlanScreen
import com.productivitytracker.habits.ui.theme.AppColors

sealed class Screen(val route: String) {
    data object Today : Screen("today")
    data object Plan : Screen("plan")
    data object Log : Screen("log")
    data object Compare : Screen("compare")
    data object Stats : Screen("stats")
    data object Habits : Screen("habits")
    data object HabitForm : Screen("habit_form?habitId={habitId}") {
        fun createRoute(habitId: Long? = null) =
            if (habitId == null) "habit_form?habitId=-1" else "habit_form?habitId=$habitId"
    }
    data object HabitDetail : Screen("habit_detail/{habitId}") {
        fun createRoute(habitId: Long) = "habit_detail/$habitId"
    }
}

private data class BottomTab(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
)

private val bottomTabs = listOf(
    BottomTab(Screen.Today, "Today", Icons.Default.CheckCircle),
    BottomTab(Screen.Plan, "Plan", Icons.Default.EventNote),
    BottomTab(Screen.Log, "Log", Icons.Default.EditNote),
    BottomTab(Screen.Compare, "Compare", Icons.Default.CompareArrows),
    BottomTab(Screen.Stats, "Stats", Icons.Default.BarChart),
)

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = currentRoute in bottomTabs.map { it.screen.route }

    Scaffold(
        containerColor = AppColors.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = AppColors.surface) {
                    bottomTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.screen.route,
                            onClick = {
                                navController.navigate(tab.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Today.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(Screen.Today.route) {
                TodayScreen(
                    onHabitClick = { navController.navigate(Screen.HabitDetail.createRoute(it)) },
                    onManageHabits = { navController.navigate(Screen.Habits.route) },
                )
            }
            composable(Screen.Plan.route) {
                PlanScreen()
            }
            composable(Screen.Log.route) {
                LogScreen()
            }
            composable(Screen.Compare.route) {
                CompareScreen()
            }
            composable(Screen.Stats.route) {
                StatsScreen(
                    onHabitClick = { navController.navigate(Screen.HabitDetail.createRoute(it)) },
                    onManageHabits = { navController.navigate(Screen.Habits.route) },
                )
            }
            composable(Screen.Habits.route) {
                HabitsScreen(
                    onAddHabit = { navController.navigate(Screen.HabitForm.createRoute()) },
                    onHabitClick = { navController.navigate(Screen.HabitDetail.createRoute(it)) },
                )
            }
            composable(
                route = Screen.HabitForm.route,
                arguments = listOf(
                    navArgument("habitId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                ),
            ) { entry ->
                val habitId = entry.arguments?.getLong("habitId")?.takeIf { it > 0 }
                HabitFormScreen(
                    habitId = habitId,
                    onBack = { navController.popBackStack() },
                    onSaved = { id ->
                        navController.popBackStack()
                        navController.navigate(Screen.HabitDetail.createRoute(id))
                    },
                )
            }
            composable(
                route = Screen.HabitDetail.route,
                arguments = listOf(navArgument("habitId") { type = NavType.LongType }),
            ) { entry ->
                val habitId = entry.arguments?.getLong("habitId") ?: return@composable
                HabitDetailScreen(
                    habitId = habitId,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Screen.HabitForm.createRoute(it)) },
                )
            }
        }
    }
}
