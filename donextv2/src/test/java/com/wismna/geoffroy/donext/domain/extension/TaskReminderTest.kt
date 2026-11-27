package com.wismna.geoffroy.donext.domain.extension

import com.google.common.truth.Truth.assertThat
import com.wismna.geoffroy.donext.domain.model.Priority
import com.wismna.geoffroy.donext.domain.model.Task
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class TaskReminderTest {

    private val zone = ZoneId.of("Europe/Paris")
    private val day = LocalDate.of(2026, 7, 16)
    private val clock = Clock.fixed(day.atStartOfDay(zone).toInstant(), zone)

    private val timedTask = Task(
        id = 1L,
        name = "Call",
        description = null,
        priority = Priority.NORMAL,
        isDone = false,
        isDeleted = false,
        taskListId = 1L,
        dueDate = day,
        dueTime = LocalTime.of(14, 30)
    )

    @Test
    fun `combines due date and time in the clock zone`() {
        assertThat(timedTask.reminderInstant(clock))
            .isEqualTo(day.atTime(14, 30).atZone(zone).toInstant())
    }

    @Test
    fun `no reminder without a due time`() {
        assertThat(timedTask.copy(dueTime = null).reminderInstant(clock)).isNull()
    }

    @Test
    fun `no reminder without a due date`() {
        assertThat(timedTask.copy(dueDate = null).reminderInstant(clock)).isNull()
    }

    @Test
    fun `no reminder for done or deleted tasks`() {
        assertThat(timedTask.copy(isDone = true).reminderInstant(clock)).isNull()
        assertThat(timedTask.copy(isDeleted = true).reminderInstant(clock)).isNull()
    }

    @Test
    fun `resetting a future reminder marks it not shown`() {
        val earlyClock = Clock.fixed(day.atTime(9, 0).atZone(zone).toInstant(), zone)

        assertThat(timedTask.copy(isReminderShown = true).withReminderReset(earlyClock).isReminderShown).isFalse()
    }

    @Test
    fun `resetting a reminder whose time already passed marks it shown`() {
        val lateClock = Clock.fixed(day.atTime(15, 0).atZone(zone).toInstant(), zone)

        assertThat(timedTask.withReminderReset(lateClock).isReminderShown).isTrue()
    }

    @Test
    fun `due date keeps its calendar day after a time zone change`() {
        val newYorkClock = Clock.fixed(day.atStartOfDay(zone).toInstant(), ZoneId.of("America/New_York"))

        assertThat(timedTask.reminderInstant(newYorkClock))
            .isEqualTo(day.atTime(14, 30).atZone(ZoneId.of("America/New_York")).toInstant())
    }
}
