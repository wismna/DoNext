package com.wismna.geoffroy.donext.domain.quickadd

import com.google.common.truth.Truth.assertThat
import com.wismna.geoffroy.donext.domain.model.Priority
import com.wismna.geoffroy.donext.domain.model.QuickAddResult
import com.wismna.geoffroy.donext.domain.model.TaskList
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class QuickAddParserTest {

    // Friday, 10:00
    private val now = LocalDateTime.of(2026, 11, 27, 10, 0)
    private val today = now.toLocalDate()
    private val parser = QuickAddParser()
    private val lists = listOf(
        TaskList(id = 1L, name = "Personnel", isDeleted = false, order = 1),
        TaskList(id = 2L, name = "Work", isDeleted = false, order = 2),
        TaskList(id = 3L, name = "Shopping", isDeleted = false, order = 3),
        TaskList(id = 4L, name = "Shopping list", isDeleted = false, order = 4),
        TaskList(id = 5L, name = "Café", isDeleted = false, order = 5)
    )

    private fun parse(text: String): QuickAddResult = parser.parse(text, now, lists)

    @Test
    fun `text without shortcuts is kept as the title`() {
        val result = parse("Buy 3 apples")

        assertThat(result).isEqualTo(QuickAddResult(title = "Buy 3 apples"))
        assertThat(result.hasShortcuts).isFalse()
    }

    @Test
    fun `parses a full English quick add`() {
        val result = parse("Call dentist tomorrow 9am !high #Work")

        assertThat(result).isEqualTo(
            QuickAddResult(
                title = "Call dentist",
                dueDate = today.plusDays(1),
                dueTime = LocalTime.of(9, 0),
                priority = Priority.HIGH,
                taskListId = 2L
            )
        )
    }

    @Test
    fun `parses a full French quick add`() {
        val result = parse("Payer loyer vendredi 14h !haute #Personnel")

        assertThat(result).isEqualTo(
            QuickAddResult(
                title = "Payer loyer",
                dueDate = today,
                dueTime = LocalTime.of(14, 0),
                priority = Priority.HIGH,
                taskListId = 1L
            )
        )
    }

    @Test
    fun `relative days in both languages`() {
        assertThat(parse("x today").dueDate).isEqualTo(today)
        assertThat(parse("x aujourd'hui").dueDate).isEqualTo(today)
        assertThat(parse("x tomorrow").dueDate).isEqualTo(today.plusDays(1))
        assertThat(parse("x demain").dueDate).isEqualTo(today.plusDays(1))
        assertThat(parse("x day after tomorrow").dueDate).isEqualTo(today.plusDays(2))
        assertThat(parse("x après-demain").dueDate).isEqualTo(today.plusDays(2))
        assertThat(parse("x apres demain").dueDate).isEqualTo(today.plusDays(2))
    }

    @Test
    fun `durations and next week in both languages`() {
        assertThat(parse("x in 3 days").dueDate).isEqualTo(today.plusDays(3))
        assertThat(parse("x dans 2 semaines").dueDate).isEqualTo(today.plusWeeks(2))
        assertThat(parse("x next week").dueDate).isEqualTo(LocalDate.of(2026, 11, 30))
        assertThat(parse("x semaine prochaine").dueDate).isEqualTo(LocalDate.of(2026, 11, 30))
    }

    @Test
    fun `a weekday is the nearest one, today included`() {
        assertThat(parse("x friday").dueDate).isEqualTo(today)
        assertThat(parse("x monday").dueDate).isEqualTo(LocalDate.of(2026, 11, 30))
        assertThat(parse("x mercredi").dueDate).isEqualTo(LocalDate.of(2026, 12, 2))
    }

    @Test
    fun `next weekday skips today`() {
        assertThat(parse("x next friday").dueDate).isEqualTo(LocalDate.of(2026, 12, 4))
        assertThat(parse("x vendredi prochain").dueDate).isEqualTo(LocalDate.of(2026, 12, 4))
    }

    @Test
    fun `month and day in either order and language`() {
        val december5 = LocalDate.of(2026, 12, 5)
        assertThat(parse("x dec 5").dueDate).isEqualTo(december5)
        assertThat(parse("x December 5th").dueDate).isEqualTo(december5)
        assertThat(parse("x 5 décembre").dueDate).isEqualTo(december5)
        assertThat(parse("x 5 decembre").dueDate).isEqualTo(december5)
    }

    @Test
    fun `a day already passed this year means next year`() {
        assertThat(parse("x nov 20").dueDate).isEqualTo(LocalDate.of(2027, 11, 20))
    }

    @Test
    fun `a leading on or le goes with the date`() {
        assertThat(parse("Party on saturday").title).isEqualTo("Party")
        assertThat(parse("Fête le 5 décembre").title).isEqualTo("Fête")
    }

    @Test
    fun `ISO dates are recognized unless they are in the past`() {
        assertThat(parse("x 2026-12-05").dueDate).isEqualTo(LocalDate.of(2026, 12, 5))

        val past = parse("x 2026-11-01")
        assertThat(past.dueDate).isNull()
        assertThat(past.title).isEqualTo("x 2026-11-01")
    }

    @Test
    fun `a month name alone is not a date`() {
        val result = parse("Ask if we may leave")

        assertThat(result.dueDate).isNull()
        assertThat(result.title).isEqualTo("Ask if we may leave")
    }

    @Test
    fun `time formats in both languages`() {
        assertThat(parse("x 9:30 pm").dueTime).isEqualTo(LocalTime.of(21, 30))
        assertThat(parse("x 9:30pm").dueTime).isEqualTo(LocalTime.of(21, 30))
        assertThat(parse("x 12pm").dueTime).isEqualTo(LocalTime.of(12, 0))
        assertThat(parse("x 14:30").dueTime).isEqualTo(LocalTime.of(14, 30))
        assertThat(parse("x 15h").dueTime).isEqualTo(LocalTime.of(15, 0))
        assertThat(parse("x 15h45").dueTime).isEqualTo(LocalTime.of(15, 45))
    }

    @Test
    fun `at or à before a time is removed from the title`() {
        assertThat(parse("Call Bob at 3pm").title).isEqualTo("Call Bob")
        assertThat(parse("Appeler Bob à 15h").title).isEqualTo("Appeler Bob")
    }

    @Test
    fun `a bare number is not a time`() {
        val result = parse("Read chapter 9")

        assertThat(result.dueTime).isNull()
        assertThat(result.title).isEqualTo("Read chapter 9")
    }

    @Test
    fun `a time alone is today when still ahead, otherwise tomorrow`() {
        assertThat(parse("x 3pm").dueDate).isEqualTo(today)
        assertThat(parse("x 8am").dueDate).isEqualTo(today.plusDays(1))
        assertThat(parse("x 8am").dueTime).isEqualTo(LocalTime.of(8, 0))
    }

    @Test
    fun `a time already past for today stays in the title`() {
        val result = parse("x today 8am")

        assertThat(result.dueDate).isEqualTo(today)
        assertThat(result.dueTime).isNull()
        assertThat(result.title).isEqualTo("x 8am")
    }

    @Test
    fun `priority markers`() {
        assertThat(parse("x !!").priority).isEqualTo(Priority.HIGH)
        assertThat(parse("x !HIGH").priority).isEqualTo(Priority.HIGH)
        assertThat(parse("x !low").priority).isEqualTo(Priority.LOW)
        assertThat(parse("x !basse").priority).isEqualTo(Priority.LOW)
        assertThat(parse("x !normal").priority).isEqualTo(Priority.NORMAL)
    }

    @Test
    fun `an unknown list tag stays in the title`() {
        val result = parse("Fix bug #urgent")

        assertThat(result.taskListId).isNull()
        assertThat(result.title).isEqualTo("Fix bug #urgent")
    }

    @Test
    fun `the longest list name wins and matching ignores case and accents`() {
        assertThat(parse("Milk #shopping list").taskListId).isEqualTo(4L)
        assertThat(parse("Milk #shopping list").title).isEqualTo("Milk")
        assertThat(parse("Milk #Shopping").taskListId).isEqualTo(3L)
        assertThat(parse("Beans #cafe").taskListId).isEqualTo(5L)
    }

    @Test
    fun `only the first shortcut of each kind is used`() {
        val result = parse("x tomorrow friday")

        assertThat(result.dueDate).isEqualTo(today.plusDays(1))
        assertThat(result.title).isEqualTo("x friday")
    }

    @Test
    fun `extra spaces and trailing punctuation are cleaned up`() {
        val result = parse("  Call   Bob   tomorrow,  ")

        assertThat(result.title).isEqualTo("Call Bob")
        assertThat(result.dueDate).isEqualTo(today.plusDays(1))
    }

    @Test
    fun `a title made only of shortcuts is empty`() {
        assertThat(parse("tomorrow !high").title).isEmpty()
    }
}
