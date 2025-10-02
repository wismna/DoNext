package com.wismna.geoffroy.donext.presentation.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.wismna.geoffroy.donext.presentation.viewmodel.ManageListsViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageListsScreen(
    modifier: Modifier,
    viewModel: ManageListsViewModel = hiltViewModel(),
    showAddListSheet: () -> Unit
) {
    var lists = viewModel.taskLists.toMutableList()
    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { from, to ->
            viewModel.moveTaskList(from.index, to.index)
        }
    )

    LazyColumn(
        modifier = modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp),
        state = lazyListState
    ) {
        itemsIndexed(lists, key = { _, list -> list.id!! }) { index, list ->

            var isInEditMode by remember { mutableStateOf(false) }
            var editedName by remember { mutableStateOf(list.name) }
            ReorderableItem(
                state =  reorderState,
                key = list.id!!
            ) {
                val interactionSource = remember { MutableInteractionSource() }
                Card(
                    onClick = {},
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 5.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                    modifier = Modifier.draggableHandle(
                        onDragStopped = {
                            viewModel.commitTaskListOrder()
                        },
                        interactionSource = interactionSource,
                    )
                        .clearAndSetSemantics {
                            customActions = listOf(
                                CustomAccessibilityAction(
                                    label = "Move Up",
                                    action = {
                                        if (index > 0) {
                                            lists = lists.toMutableList().apply {
                                                add(index - 1, removeAt(index))
                                            }
                                            true
                                        } else {
                                            false
                                        }
                                    }
                                ),
                                CustomAccessibilityAction(
                                    label = "Move Down",
                                    action = {
                                        if (index < lists.size - 1) {
                                            lists = lists.toMutableList().apply {
                                                add(index + 1, removeAt(index))
                                            }
                                            true
                                        } else {
                                            false
                                        }
                                    }
                                ),
                            )
                        },
                    interactionSource = interactionSource,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DragHandle, contentDescription = "Edit")
                        AnimatedContent(
                            targetState = isInEditMode,
                            modifier = Modifier.weight(1f),
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "Headline transition"
                        ) { isEditing ->
                            if (isEditing) {
                                OutlinedTextField(
                                    value = editedName,
                                    onValueChange = { editedName = it },
                                    singleLine = true
                                )
                            } else {
                                Text(
                                    modifier = Modifier.padding(start = 8.dp),
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1,
                                    text = list.name
                                )
                            }
                        }
                        AnimatedContent(
                            targetState = isInEditMode,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "Trailing transition"
                        ) { editing ->
                            if (editing) {
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
                                        IconButton(onClick = { viewModel.deleteTaskList(list.id) }) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete"
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
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
            Text("New List", style = MaterialTheme.typography.titleMedium)

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
                    .focusRequester(titleFocusRequester)
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
                Button(
                    onClick = {
                        viewModel.createTaskList(name/*, type, description*/, viewModel.taskCount + 1)
                        onDismiss()
                    },
                    enabled = name.isNotBlank()
                ) {
                    Text("Create")
                }
            }
        }
    }
}