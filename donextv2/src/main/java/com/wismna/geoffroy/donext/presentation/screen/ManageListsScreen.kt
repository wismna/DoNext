package com.wismna.geoffroy.donext.presentation.screen

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.wismna.geoffroy.donext.presentation.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageListsScreen(
    modifier: Modifier,
    viewModel: MainViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val lists = viewModel.taskLists

    LazyColumn(modifier = modifier.fillMaxWidth().padding()) {
        itemsIndexed(lists, key = { _, list -> list.id }) { index, list ->
            ListItem(
                headlineContent = { Text(list.name) },
                trailingContent = {
                    Row {
                        IconButton(onClick = { /* TODO: edit list */ }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { /* TODO: delete list */ }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
            HorizontalDivider()
        }
    }
}
