package com.wismna.geoffroy.donext.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.wismna.geoffroy.donext.presentation.viewmodel.ManageListsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageListsScreen(
    modifier: Modifier,
    viewModel: ManageListsViewModel = hiltViewModel(),
    showAddListSheet: () -> Unit
) {
    val lists = viewModel.taskLists

    LazyColumn(modifier = modifier.fillMaxWidth().padding()) {
        itemsIndexed(lists, key = { _, list -> list.id!! }) { index, list ->

            var isInEditMode by remember { mutableStateOf(false) }
            var editedName by remember { mutableStateOf(list.name) }
            ListItem(
                modifier = Modifier.animateItem(),
                headlineContent = {
                    if (isInEditMode) {
                        OutlinedTextField(
                            value = editedName,
                            onValueChange = { editedName = it },
                            singleLine = true
                        )
                    } else {
                        Text(list.name)
                    }
                },
                trailingContent = {
                    if (isInEditMode) {
                        Row {
                            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onPrimaryContainer) {
                                IconButton(onClick = { isInEditMode = false }) {
                                    Icon(Icons.Default.Close, contentDescription = "Cancel")
                                }
                                IconButton(onClick = {
                                    viewModel.updateTaskListName(list.copy(name = editedName))
                                    isInEditMode = false
                                }) {
                                    Icon(Icons.Default.Check, contentDescription = "Save")
                                }
                            }
                        }
                    } else {
                        Row {
                            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onPrimaryContainer) {
                                IconButton(onClick = { isInEditMode = true }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                }
                                IconButton(onClick = { viewModel.deleteTaskList(list.id!!) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                    }
                }
            )
            HorizontalDivider()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddListBottomSheet(
    viewModel: ManageListsViewModel = hiltViewModel(),
    onDismiss: () -> Unit
) {
    val titleFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        titleFocusRequester.requestFocus()
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        var name by remember { mutableStateOf("") }
        //var type by remember { mutableStateOf(ListType.Default) }
        //var description by remember { mutableStateOf("") }

        Column(modifier = Modifier.padding(16.dp)) {
            Text("Create New List", style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(8.dp))
            /*TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("List Name") },
                singleLine = true
            )*/
            OutlinedTextField(
                value = name,
                singleLine = true,
                onValueChange = { name = it },
                label = { Text("Title") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(titleFocusRequester),
                isError = name.isEmpty(),
            )

            Spacer(Modifier.height(8.dp))
            //DropdownSelector(selected = type, onSelect = { type = it })

            /*Spacer(Modifier.height(8.dp))
            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                maxLines = 3
            )*/

            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                //TextButton(onClick = onDismiss) { Text("Cancel") }
                //Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    viewModel.createTaskList(name/*, type, description*/, 1)
                    onDismiss()
                }) {
                    Text("Add")
                }
            }
        }
    }
}