package com.wismna.geoffroy.donext.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wismna.geoffroy.donext.domain.model.Priority
import com.wismna.geoffroy.donext.domain.model.QuickAddResult
import com.wismna.geoffroy.donext.domain.model.Task
import com.wismna.geoffroy.donext.domain.model.TaskList
import com.wismna.geoffroy.donext.domain.usecase.AddTaskUseCase
import com.wismna.geoffroy.donext.domain.usecase.GetTaskListsUseCase
import com.wismna.geoffroy.donext.domain.usecase.ParseQuickAddUseCase
import com.wismna.geoffroy.donext.domain.usecase.UpdateTaskUseCase
import com.wismna.geoffroy.donext.presentation.ui.events.UiEvent
import com.wismna.geoffroy.donext.presentation.ui.events.UiEventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val createTaskUseCase: AddTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val parseQuickAddUseCase: ParseQuickAddUseCase,
    getTaskListsUseCase: GetTaskListsUseCase,
    private val uiEventBus: UiEventBus,
    private val clock: Clock
) : ViewModel() {

    var title by mutableStateOf("")
        private set
    var description by mutableStateOf("")
        private set
    private val priorityField = ShortcutBackedValue(Priority.NORMAL)
    private val dueDateField = ShortcutBackedValue<LocalDate?>(null)
    private val dueTimeField = ShortcutBackedValue<LocalTime?>(null)

    val priority: Priority get() = priorityField.value
    val dueDate: LocalDate? get() = dueDateField.value
    val dueTime: LocalTime? get() = dueTimeField.value
    var isDone by mutableStateOf(false)
        private set
    var isDeleted by mutableStateOf(false)
        private set
    var taskListId by mutableStateOf<Long?>(null)
        private set
    var editingTaskId by mutableStateOf<Long?>(null)
        private set

    /** Shortcuts recognized in the title of a new task ("tomorrow 9am !high #Work"); null when there are none. */
    var quickAddPreview by mutableStateOf<QuickAddResult?>(null)
        private set

    private var taskLists: List<TaskList> = emptyList()

    val quickAddListName: String?
        get() = quickAddPreview?.taskListId?.let { id -> taskLists.firstOrNull { it.id == id }?.name }

    val canSave: Boolean
        get() = effectiveTitle.isNotBlank() && !isDeleted

    private val effectiveTitle: String
        get() = quickAddPreview?.title ?: title

    init {
        viewModelScope.launch {
            getTaskListsUseCase().collect { lists ->
                taskLists = lists
                refreshQuickAddPreview()
            }
        }
        viewModelScope.launch {
            uiEventBus.stickyEvents.collect { event ->
                when (event) {
                    is UiEvent.CreateNewTask -> startNewTask(event.taskListId)
                    is UiEvent.EditTask -> startEditTask(event.task)
                    is UiEvent.CloseTask -> reset()
                    else -> {}
                }
            }
        }
    }

    fun isEditing(): Boolean = editingTaskId != null
    fun onTitleChanged(value: String) {
        title = value
        refreshQuickAddPreview()
    }
    fun onDescriptionChanged(value: String) { description = value }
    fun onPriorityChanged(value: Priority) = priorityField.setManually(value)
    fun onDueTimeChanged(value: LocalTime?) {
        if (value == null || isDueTimeAllowed(value)) dueTimeField.setManually(value)
    }

    /** A time that already passed today would never notify, so it cannot be picked for a task due today. */
    fun isDueTimeAllowed(time: LocalTime): Boolean =
        dueDate != LocalDate.now(clock) || time.isAfter(LocalTime.now(clock))

    fun suggestedDueTime(): LocalTime {
        dueTime?.let { return it }
        if (dueDate != LocalDate.now(clock)) return DEFAULT_DUE_TIME
        val nextFullHour = LocalTime.now(clock).plusHours(1).withMinute(0).withSecond(0).withNano(0)
        return if (isDueTimeAllowed(nextFullHour)) nextFullHour else LocalTime.MAX.withSecond(0).withNano(0)
    }

    /** [utcMillis] is the date picker's selection: midnight UTC of the picked day. */
    fun onDueDateChanged(utcMillis: Long?) {
        dueDateField.setManually(utcMillis?.let { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() })
        val keptTime = dueTime?.takeIf { utcMillis != null && isDueTimeAllowed(it) }
        if (keptTime != dueTime) dueTimeField.setManually(keptTime)
    }

    fun save(onDone: (() -> Unit)? = null) {
        if (!canSave) return
        val listId = quickAddPreview?.taskListId ?: taskListId ?: return
        val saveDueDate = dueDate
        val saveDueTime = dueTime.takeIf { saveDueDate != null }
        val savePriority = priority
        val saveTitle = effectiveTitle

        viewModelScope.launch {
            val currentEditingTaskId = editingTaskId
            if (currentEditingTaskId != null) {
                updateTaskUseCase(currentEditingTaskId, listId, saveTitle, description, savePriority, saveDueDate, saveDueTime)
            } else {
                createTaskUseCase(listId, saveTitle, description, savePriority, saveDueDate, saveDueTime)
            }
            onDone?.invoke()
        }
    }

    /**
     * Shortcuts are only read while creating a task: an existing title like "Monday notes" is left alone.
     * Recognized values fill in the fields live, next to the chips that show them.
     */
    private fun refreshQuickAddPreview() {
        quickAddPreview = if (isEditing() || title.isBlank()) {
            null
        } else {
            parseQuickAddUseCase(title, taskLists).takeIf { it.hasShortcuts }
        }
        if (!isEditing()) applyShortcutsToFields()
    }

    private fun applyShortcutsToFields() {
        priorityField.applyShortcut(quickAddPreview?.priority)
        dueDateField.applyShortcut(quickAddPreview?.dueDate)
        dueTimeField.applyShortcut(quickAddPreview?.dueTime)
    }

    private fun resetFields(priority: Priority, dueDate: LocalDate?, dueTime: LocalTime?) {
        priorityField.reset(priority)
        dueDateField.reset(dueDate)
        dueTimeField.reset(dueTime)
    }

    private fun startNewTask(selectedListId: Long) {
        editingTaskId = null
        taskListId = selectedListId
        title = ""
        description = ""
        resetFields(Priority.NORMAL, null, null)
        isDeleted = false
        quickAddPreview = null
    }

    private fun startEditTask(task: Task) {
        editingTaskId = task.id
        taskListId = task.taskListId
        title = task.name
        description = task.description ?: ""
        resetFields(task.priority, task.dueDate, task.dueTime)
        isDone = task.isDone
        isDeleted = task.isDeleted
        quickAddPreview = null
    }

    private fun reset() {
        editingTaskId = null
        taskListId = null
        title = ""
        description = ""
        resetFields(Priority.NORMAL, null, null)
        isDone = false
        isDeleted = false
        quickAddPreview = null
    }

    private companion object {
        val DEFAULT_DUE_TIME: LocalTime = LocalTime.of(9, 0)
    }
}