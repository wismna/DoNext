@file:OptIn(ExperimentalMaterial3Api::class)

package com.wismna.geoffroy.donext.presentation.screen

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wismna.geoffroy.donext.domain.model.TaskListWithOverdue
import com.wismna.geoffroy.donext.presentation.viewmodel.MainViewModel
import com.wismna.geoffroy.donext.presentation.viewmodel.TaskListViewModel
import com.wismna.geoffroy.donext.presentation.viewmodel.TaskViewModel
import kotlinx.coroutines.launch

sealed class AppDestination(
    val route: String,
    val title: String,
    val showBackButton: Boolean = false,
    val actions: @Composable (() -> Unit)? = null
) {
    data class TaskList(val taskListId: Long, val name: String) : AppDestination(
        route = "taskList/$taskListId",
        title = name,
    )

    object ManageLists : AppDestination(
        route = "manageLists",
        title = "Manage Lists",
        showBackButton = true,
        actions = { ManageListsActions() }
    )
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    var showBottomSheet by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val taskViewModel: TaskViewModel = hiltViewModel()

    if (viewModel.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val firstListId = viewModel.taskLists.firstOrNull()?.id
    if (showBottomSheet) {
        TaskBottomSheet(taskViewModel) { showBottomSheet = false }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = remember(navBackStackEntry, viewModel.taskLists) {
        deriveDestination(navBackStackEntry, viewModel.taskLists)
    }

    ModalNavigationDrawer(
        drawerContent = {
            MenuScreen (
                taskLists = viewModel.taskLists,
                currentDestination = currentDestination,
                onNavigate = { route ->
                    scope.launch { drawerState.close() }
                    navController.navigate(route) {
                        //launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        },
        drawerState = drawerState
    ) {
        Scaffold(
            modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(currentDestination.title) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    navigationIcon = {
                        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onPrimaryContainer) {
                            if (currentDestination.showBackButton) {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                                }
                            } else {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(
                                        Icons.Default.Menu,
                                        contentDescription = "Open navigation drawer"
                                    )
                                }
                            }
                        }
                    },
                    actions = { currentDestination.actions?.invoke() }
                )
            },
            floatingActionButton = {
                when (val dest = currentDestination) {
                    is AppDestination.TaskList -> {
                        TaskListFab(
                            taskListId = dest.taskListId,
                            showBottomSheet = { showBottomSheet = it }
                        )
                    }
                    else -> null
                }
            }
        ) { contentPadding ->
            Surface(
                modifier = Modifier
                    .padding(top = contentPadding.calculateTopPadding())
                    .fillMaxSize(),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                NavHost(
                    navController = navController,
                    startDestination = firstListId?.let { "taskList/$it" }
                        ?: AppDestination.ManageLists.route,
                    enterTransition = {
                        slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }, animationSpec = tween(300))
                    },
                    exitTransition = {
                        slideOutHorizontally(targetOffsetX = { fullWidth -> -fullWidth }, animationSpec = tween(300))
                    },
                    popEnterTransition = {
                        slideInHorizontally(initialOffsetX = { fullWidth -> -fullWidth }, animationSpec = tween(300))
                    },
                    popExitTransition = {
                        slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth }, animationSpec = tween(300))
                    }
                ) {
                    viewModel.taskLists.forEach { list ->
                        composable(
                            route = "taskList/{taskListId}",
                            arguments = listOf(navArgument("taskListId") {
                                type = NavType.LongType
                            })
                        ) { navBackStackEntry ->
                            val viewModel: TaskListViewModel = hiltViewModel(navBackStackEntry)
                            TaskListScreen(
                                viewModel = viewModel,
                                onTaskClick = { task ->
                                    taskViewModel.startEditTask(task)
                                    showBottomSheet = true
                                }
                            )
                        }
                    }

                    composable(AppDestination.ManageLists.route) {
                        ManageListsScreen(modifier = Modifier)
                    }
                }
            }
        }
    }
}

fun deriveDestination(
    navBackStackEntry: NavBackStackEntry?,
    taskLists: List<TaskListWithOverdue>
): AppDestination {
    val route = navBackStackEntry?.destination?.route

    return when {
        route == AppDestination.ManageLists.route -> AppDestination.ManageLists
        route?.startsWith("taskList/") == true || route == "taskList/{taskListId}" -> {
            val idArg = navBackStackEntry.arguments?.getLong("taskListId")
            val taskListId = idArg ?: route.substringAfter("taskList/", "").toLongOrNull()
            val matching = taskLists.find { it.id == taskListId }
            matching?.let { AppDestination.TaskList(it.id, it.name)  }
                ?: taskLists.firstOrNull()?.let { AppDestination.TaskList(it.id, it.name) }
                ?: AppDestination.ManageLists
        }
        else -> {
            taskLists.firstOrNull()?.let { AppDestination.TaskList(it.id, it.name) }
                ?: AppDestination.ManageLists
        }
    }
}