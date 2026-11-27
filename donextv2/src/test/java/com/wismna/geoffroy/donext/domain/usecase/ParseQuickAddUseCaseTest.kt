package com.wismna.geoffroy.donext.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.wismna.geoffroy.donext.domain.model.TaskList
import com.wismna.geoffroy.donext.domain.quickadd.QuickAddParser
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class ParseQuickAddUseCaseTest {

    @Test
    fun `parses relative to the clock and resolves lists`() {
        val zone = ZoneId.of("Europe/Paris")
        val today = LocalDate.of(2026, 11, 27)
        val clock = Clock.fixed(today.atTime(10, 0).atZone(zone).toInstant(), zone)
        val lists = listOf(TaskList(id = 9L, name = "Work", isDeleted = false, order = 0))

        val result = ParseQuickAddUseCase(QuickAddParser(), clock)("Report tomorrow 9am #work", lists)

        assertThat(result.title).isEqualTo("Report")
        assertThat(result.dueDate).isEqualTo(today.plusDays(1))
        assertThat(result.dueTime).isEqualTo(LocalTime.of(9, 0))
        assertThat(result.taskListId).isEqualTo(9L)
    }
}
