package com.wismna.geoffroy.donext.data

import com.google.common.truth.Truth.assertThat
import com.wismna.geoffroy.donext.domain.model.Priority
import com.wismna.geoffroy.donext.domain.model.Task
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class MappersTest {

    @Test
    fun `due time is stored as minutes of the day`() {
        assertThat(LocalTime.of(0, 0).toMinutesOfDay()).isEqualTo(0)
        assertThat(LocalTime.of(14, 30).toMinutesOfDay()).isEqualTo(870)
        assertThat(LocalTime.of(23, 59).toMinutesOfDay()).isEqualTo(1439)
        assertThat(870.toLocalTime()).isEqualTo(LocalTime.of(14, 30))
    }

    @Test
    fun `task round-trips through the entity with its new fields`() {
        val task = Task(
            id = 3L,
            name = "Call",
            description = "Dentist",
            priority = Priority.HIGH,
            isDone = true,
            isDeleted = false,
            taskListId = 2L,
            dueDate = LocalDate.of(2026, 3, 10),
            dueTime = LocalTime.of(9, 5),
            createdAt = 10L,
            completedAt = 20L,
            isReminderShown = true
        )

        assertThat(task.toEntity().toDomain()).isEqualTo(task)
    }
}
