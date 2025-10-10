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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wismna.geoffroy.donext.domain.model.AppDestination
import com.wismna.geoffroy.donext.presentation.ui.events.UiEvent
import com.wismna.geoffroy.donext.presentation.viewmodel.MainViewModel
import com.wismna.geoffroy.donext.presentation.viewmodel.TaskListViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    if (viewModel.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (viewModel.showTaskSheet) {
        TaskBottomSheet { viewModel.onDismissTaskSheet() }
    }
    if (viewModel.showAddListSheet) {
        AddListBottomSheet { viewModel.showAddListSheet = false }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    viewModel.setCurrentDestination(navBackStackEntry)

    ModalNavigationDrawer(
        drawerContent = {
            MenuScreen (currentDestination = viewModel.currentDestination)
        },
        drawerState = drawerState
    ) {
        AppContent(viewModel = viewModel, navController = navController, scope = scope, drawerState = drawerState)
    }
}

@Composable
fun AppContent(
    modifier : Modifier = Modifier,
    viewModel: MainViewModel,
    navController: NavHostController,
    scope: CoroutineScope,
    drawerState: DrawerState
) {
    LaunchedEffect(Unit) {
        viewModel.uiEventBus.events.collectLatest { event ->
            when (event) {
                is UiEvent.Navigate -> {
                    drawerState.close()
                    navController.navigate(event.route)
                }
                is UiEvent.NavigateBack -> navController.popBackStack()
                else -> {}
            }
        }
    }
    Scaffold(
        modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(viewModel.currentDestination.title) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onPrimaryContainer) {
                        if (viewModel.currentDestination.showBackButton) {
                            IconButton(onClick = { viewModel.navigateBack() }) {
                                Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
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
                actions = {
                    when (viewModel.currentDestination) {
                        is AppDestination.ManageLists -> {
                            IconButton(onClick = { viewModel.showAddListSheet = true }) {
                                Icon(Icons.Default.Add, contentDescription = "Add List")
                            }
                        }
                        is AppDestination.RecycleBin -> {
                            EmptyRecycleBinAction()
                        }
                        else -> null
                    }
                }
            )
        },
        floatingActionButton = {
            when (val dest = viewModel.currentDestination) {
                is AppDestination.TaskList -> {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.onNewTaskButtonClicked(dest.taskListId) },
                        icon = { Icon(Icons.Filled.Add, "Create a task.") },
                        text = { Text("Create a task") },
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
                startDestination = viewModel.startDestination.route,
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
                composable(
                    route = "taskList/{taskListId}",
                    arguments = listOf(navArgument("taskListId") {
                        type = NavType.LongType
                    })
                ) { navBackStackEntry ->
                    val taskListId = navBackStackEntry.arguments?.getLong("taskListId") ?: return@composable
                    val listExists by remember(taskListId, viewModel.destinations) {
                        derivedStateOf { viewModel.doesListExist(taskListId) }
                    }
                    LaunchedEffect(listExists) {
                        if (!viewModel.doesListExist(taskListId)) {
                            viewModel.navigateBack()
                        }
                    }

                    val taskListViewModel: TaskListViewModel = hiltViewModel(navBackStackEntry)
                    TaskListScreen(
                        viewModel = taskListViewModel,
                        onTaskClick = { task -> viewModel.onTaskClicked(task) }
                    )
                }

                composable(AppDestination.ManageLists.route) {
                    ManageListsScreen(
                        modifier = Modifier,
                        showAddListSheet = {viewModel.showAddListSheet = true}
                    )
                }
                composable(AppDestination.DueTodayList.route) {
                    DueTodayTasksScreen (
                        modifier = Modifier,
                        onTaskClick = { task -> viewModel.onTaskClicked(task) }
                    )
                }
                composable(AppDestination.RecycleBin.route) {
                    RecycleBinScreen(
                        modifier = Modifier,
                        onTaskClick = { task -> viewModel.onTaskClicked(task) }
                    )
                }
            }
        }
    }
}