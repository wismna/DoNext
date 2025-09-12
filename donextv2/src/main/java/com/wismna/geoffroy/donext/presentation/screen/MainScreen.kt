@file:OptIn(ExperimentalMaterial3Api::class)

package com.wismna.geoffroy.donext.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wismna.geoffroy.donext.domain.model.Priority
import com.wismna.geoffroy.donext.domain.model.TaskList
import com.wismna.geoffroy.donext.presentation.viewmodel.MainViewModel
import com.wismna.geoffroy.donext.presentation.viewmodel.TaskListViewModel

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
        val startDestination = viewModel.taskLists[0]
        // TODO: get last opened tab from saved settings
        var selectedDestination by rememberSaveable { mutableIntStateOf(0) }

        if (showBottomSheet) {
            AddTaskBottomSheet(viewModel, selectedDestination, { showBottomSheet = false })
        }

        Scaffold(
            modifier = modifier,
            floatingActionButton = {
                AddNewTaskButton {
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
                AppNavHost(navController, startDestination, viewModel)
            }
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: TaskList,
    viewModel: MainViewModel
) {
    NavHost(
        navController,
        startDestination = "taskList/${startDestination.id}"
    ) {
        viewModel.taskLists.forEach { destination ->
            composable(
                route = "taskList/{taskListId}",
                arguments = listOf(navArgument("taskListId") { type = NavType.LongType })) {
                val viewModel: TaskListViewModel = hiltViewModel<TaskListViewModel>()
                TaskListScreen(viewModel)
            }
        }
    }
}

@Composable
fun AddTaskBottomSheet(
    viewModel: MainViewModel,
    selectedListIndex: Int,
    onDismiss: () -> Unit
) {
    val titleFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        titleFocusRequester.requestFocus()
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "New Task",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(8.dp))

            // --- Title ---
            OutlinedTextField(
                value = viewModel.title,
                singleLine = true,
                onValueChange = { viewModel.onTitleChanged(it) },
                label = { Text("Title") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(titleFocusRequester),
                isError = !viewModel.isTitleValid && viewModel.title.isNotEmpty(),
            )
            Spacer(Modifier.height(8.dp))

            // --- Description ---
            OutlinedTextField(
                value = viewModel.description,
                onValueChange = { viewModel.onDescriptionChanged(it) },
                label = { Text("Description") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            // --- Priority ---
            Text("Priority", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            SingleChoiceSegmentedButton(
                value = viewModel.priority,
                onValueChange = { viewModel.onPriorityChanged(it) }
            )
            Spacer(Modifier.height(16.dp))

            // --- Add Button ---
            Button(
                onClick = {
                    val currentListId = viewModel.taskLists[selectedListIndex].id
                    viewModel.createTask(currentListId)
                    onDismiss()
                    viewModel.resetTaskForm()
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Add")
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