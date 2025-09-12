@file:OptIn(ExperimentalMaterial3Api::class)

package com.wismna.geoffroy.donext.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wismna.geoffroy.donext.domain.model.Priority
import com.wismna.geoffroy.donext.presentation.viewmodel.MainViewModel
import com.wismna.geoffroy.donext.presentation.viewmodel.TaskViewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    var showBottomSheet by remember { mutableStateOf(false) }

    if (viewModel.isLoading) {
        // Show loading or empty state
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        val taskViewModel: TaskViewModel = hiltViewModel()
        val startDestination = viewModel.taskLists[0]
        // TODO: get last opened tab from saved settings
        var selectedDestination by rememberSaveable { mutableIntStateOf(0) }

        if (showBottomSheet) {
            TaskBottomSheet(taskViewModel, { showBottomSheet = false })
        }

        Scaffold(
            modifier = modifier,
            floatingActionButton = {
                AddNewTaskButton {
                    val currentListId = viewModel.taskLists[selectedDestination].id
                    taskViewModel.startNewTask(currentListId)
                    showBottomSheet = true
                }
            }, topBar = {
                PrimaryTabRow(selectedTabIndex = selectedDestination) {
                    viewModel.taskLists.forEachIndexed { index, destination ->
                        Tab(
                            selected = selectedDestination == index,
                            onClick = {
                                navController.navigate(route = "taskList/${destination.id}")
                                selectedDestination = index
                            },
                            text = {
                                Text(
                                    text = destination.name,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }
            }) { contentPadding ->
            Box(modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
            ) {
                NavHost(
                    navController,
                    startDestination = "taskList/${startDestination.id}"
                ) {
                    viewModel.taskLists.forEach { destination ->
                        composable(
                            route = "taskList/{taskListId}",
                            arguments = listOf(navArgument("taskListId") {
                                type = NavType.LongType
                            })
                        ) {
                            TaskListScreen(
                                onTaskClick = { task ->
                                    taskViewModel.startEditTask(task)
                                    showBottomSheet = true
                                })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddNewTaskButton(onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = { Icon(Icons.Filled.Add, "Create a task.") },
        text = { Text(text = "Create a task") },
    )
}

@Composable
fun SingleChoiceSegmentedButton(
    value: Priority,
    onValueChange: (Priority) -> Unit) {
    val options = listOf(Priority.LOW.label, Priority.NORMAL.label, Priority.HIGH.label)

    SingleChoiceSegmentedButtonRow {
        options.forEachIndexed { index, label ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size
                ),
                onClick = { onValueChange(Priority.fromValue(index)) },
                selected = index == value.value,
                label = { Text(label) }
            )
        }
    }
}