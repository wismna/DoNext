package com.wismna.geoffroy.donext.presentation.viewmodel

import com.google.common.truth.Truth.assertThat
import com.wismna.geoffroy.donext.domain.model.Priority
import com.wismna.geoffroy.donext.domain.model.Task
import org.junit.Before
import org.junit.Test
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.*

class TaskItemViewModelTest {

    private val fixedClock: Clock = Clock.fixed(
        LocalDate.of(2025, 1, 10)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant(),
        ZoneId.systemDefault()
    )
    private val today: LocalDate = LocalDate.now(fixedClock)

    private lateinit var baseTask: Task

    @Before
    fun setup() {
        baseTask = Task(
            id = 1L,
            taskListId = 1L,
            name = "Test Task",
            description = "Description",
            priority = Priority.NORMAL,
            isDone = false,
            isDeleted = false,
            dueDate = null
        )
    }

    private fun millisForDaysFromFixedToday(daysOffset: Long): Long {
        val targetDate = today.plusDays(daysOffset)
        return targetDate
            .atStartOfDay(fixedClock.zone)
            .toInstant()
            .toEpochMilli()
    }

    @Test
    fun `initializes fields from Task`() {
        val viewModel = TaskItemViewModel(baseTask)

        assertThat(viewModel.id).isEqualTo(baseTask.id)
        assertThat(viewModel.name).isEqualTo(baseTask.name)
        assertThat(viewModel.description).isEqualTo(baseTask.description)
        assertThat(viewModel.isDone).isFalse()
        assertThat(viewModel.isDeleted).isFalse()
        assertThat(viewModel.priority).isEqualTo(Priority.NORMAL)
    }

    @Test
    fun `isOverdue is true when due date is before today`() {
        val overdueTask = baseTask.copy(dueDate = millisForDaysFromFixedToday(-1))
        val viewModel = TaskItemViewModel(overdueTask)

        assertThat(viewModel.isOverdue).isTrue()
    }

    @Test
    fun `isOverdue is false when due date is today`() {
        val dueToday = baseTask.copy(dueDate = millisForDaysFromFixedToday(0))
        val viewModel = TaskItemViewModel(dueToday, fixedClock)

        assertThat(viewModel.isOverdue).isFalse()
    }

    @Test
    fun `isOverdue is false when due date is null`() {
        val viewModel = TaskItemViewModel(baseTask.copy(dueDate = null))

        assertThat(viewModel.isOverdue).isFalse()
    }

    @Test
    fun `dueDateText is Today when due date is today`() {
        val dueToday = baseTask.copy(dueDate = millisForDaysFromFixedToday(0))
        val viewModel = TaskItemViewModel(dueToday, fixedClock)

        assertThat(viewModel.dueDateText).isEqualTo("Today")
    }

    @Test
    fun `dueDateText is Tomorrow when due date is tomorrow`() {
        val dueTomorrow = baseTask.copy(dueDate = millisForDaysFromFixedToday(1))
        val viewModel = TaskItemViewModel(dueTomorrow, fixedClock)

        assertThat(viewModel.dueDateText).isEqualTo("Tomorrow")
    }

    @Test
    fun `dueDateText is Yesterday when due date was yesterday`() {
        val dueYesterday = baseTask.copy(dueDate = millisForDaysFromFixedToday(-1))
        val viewModel = TaskItemViewModel(dueYesterday, fixedClock)

        assertThat(viewModel.dueDateText).isEqualTo("Yesterday")
    }

    @Test
    fun `dueDateText is day of week when within next 7 days`() {
        val dueIn3Days = baseTask.copy(dueDate = millisForDaysFromFixedToday(3))
        val viewModel = TaskItemViewModel(dueIn3Days, fixedClock)

        val expected = today
            .plusDays(3)
            .dayOfWeek
            .getDisplayName(TextStyle.SHORT, Locale.getDefault())

        assertThat(viewModel.dueDateText).isEqualTo(expected)
    }

    @Test
    fun `dueDateText is formatted date when more than 7 days away`() {
        val dueIn10Days = baseTask.copy(dueDate = millisForDaysFromFixedToday(10))
        val viewModel = TaskItemViewModel(dueIn10Days)

        val expected = today
            .plusDays(10)
            .format(
                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                    .withLocale(Locale.getDefault())
            )

        assertThat(viewModel.dueDateText).isEqualTo(expected)
    }
}
