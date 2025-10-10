package com.wismna.geoffroy.donext.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LineWeight
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Badge
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.wismna.geoffroy.donext.domain.model.AppDestination
import com.wismna.geoffroy.donext.presentation.viewmodel.MenuViewModel

@Composable
fun MenuScreen(
    viewModel: MenuViewModel = hiltViewModel(),
    currentDestination: AppDestination,
) {
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        drawerContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Task Lists",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
                NavigationDrawerItem(
                    label = {
                        Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Due Today")
                            Text(viewModel.dueTodayTasksCount.toString())
                        }
                    },
                    icon = { Icon(Icons.Default.Today, contentDescription = "Due Today") },
                    selected = currentDestination is AppDestination.DueTodayList,
                    onClick = { viewModel.navigateTo(AppDestination.DueTodayList.route) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                HorizontalDivider()
                viewModel.taskLists.forEach { list ->
                    NavigationDrawerItem(
                        label = {
                            Text(
                                text = list.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        icon = { Icon(Icons.Default.LineWeight, contentDescription = list.name) },
                        selected = currentDestination is AppDestination.TaskList &&
                                currentDestination.taskListId == list.id,
                        onClick = { viewModel.navigateTo("taskList/${list.id}") },
                        badge = {
                            if (list.overdueCount > 0) {
                                Badge { Text(list.overdueCount.toString()) }
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }

            Column {
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text("Recycle Bin") },
                    icon = { Icon(Icons.Default.Delete, contentDescription = "Recycle Bin") },
                    selected = currentDestination is AppDestination.RecycleBin,
                    onClick = { viewModel.navigateTo(AppDestination.RecycleBin.route) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Edit Lists") },
                    icon = { Icon(Icons.Default.EditNote, contentDescription = "Edit Lists") },
                    selected = currentDestination is AppDestination.ManageLists,
                    onClick = { viewModel.navigateTo(AppDestination.ManageLists.route) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    }
}