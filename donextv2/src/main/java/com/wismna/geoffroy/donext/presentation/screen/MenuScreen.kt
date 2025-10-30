package com.wismna.geoffroy.donext.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.wismna.geoffroy.donext.R
import com.wismna.geoffroy.donext.domain.model.AppDestination
import com.wismna.geoffroy.donext.presentation.viewmodel.MenuViewModel

@Composable
fun MenuScreen(
    modifier: Modifier = Modifier,
    viewModel: MenuViewModel = hiltViewModel(),
    currentDestination: AppDestination,
) {
    ModalDrawerSheet(
        modifier = modifier,
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
                    text = stringResource(R.string.navigation_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
                NavigationDrawerItem(
                    label = {
                        Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(R.string.navigation_due_today))
                            Text(viewModel.dueTodayTasksCount.toString())
                        }
                    },
                    icon = { Icon(Icons.Default.Today, contentDescription = stringResource(R.string.navigation_due_today)) },
                    selected = currentDestination is AppDestination.DueTodayList,
                    onClick = { viewModel.navigateTo(AppDestination.DueTodayList.route, currentDestination.route) },
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
                        icon = { Icon(Icons.Default.Checklist, contentDescription = list.name) },
                        selected = currentDestination is AppDestination.TaskList &&
                                currentDestination.taskListId == list.id,
                        onClick = { viewModel.navigateTo("taskList/${list.id}", currentDestination.route) },
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
                    label = { Text(stringResource(R.string.navigation_edit_lists)) },
                    icon = { Icon(Icons.Default.EditNote, contentDescription = stringResource(R.string.navigation_edit_lists)) },
                    selected = currentDestination is AppDestination.ManageLists,
                    onClick = { viewModel.navigateTo(AppDestination.ManageLists.route, currentDestination.route) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.navigation_recycle_bin)) },
                    icon = { Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.navigation_recycle_bin)) },
                    selected = currentDestination is AppDestination.RecycleBin,
                    onClick = { viewModel.navigateTo(AppDestination.RecycleBin.route, currentDestination.route) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    }
}