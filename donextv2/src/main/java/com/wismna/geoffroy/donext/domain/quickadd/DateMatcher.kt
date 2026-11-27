package com.wismna.geoffroy.donext.domain.quickadd

import com.wismna.geoffroy.donext.domain.quickadd.QuickAddVocabulary.AFTER_TOMORROW_PHRASES
import com.wismna.geoffroy.donext.domain.quickadd.QuickAddVocabulary.AFTER_TOMORROW_SINGLE
import com.wismna.geoffroy.donext.domain.quickadd.QuickAddVocabulary.DATE_PREPOSITIONS
import com.wismna.geoffroy.donext.domain.quickadd.QuickAddVocabulary.DAY_UNITS
import com.wismna.geoffroy.donext.domain.quickadd.QuickAddVocabulary.IN
import com.wismna.geoffroy.donext.domain.quickadd.QuickAddVocabulary.MONTHS
import com.wismna.geoffroy.donext.domain.quickadd.QuickAddVocabulary.NEXT_EN
import com.wismna.geoffroy.donext.domain.quickadd.QuickAddVocabulary.NEXT_FR
import com.wismna.geoffroy.donext.domain.quickadd.QuickAddVocabulary.NEXT_WEEK_PHRASES
import com.wismna.geoffroy.donext.domain.quickadd.QuickAddVocabulary.TODAY
import com.wismna.geoffroy.donext.domain.quickadd.QuickAddVocabulary.TOMORROW
import com.wismna.geoffroy.donext.domain.quickadd.QuickAddVocabulary.WEEKDAYS
import com.wismna.geoffroy.donext.domain.quickadd.QuickAddVocabulary.WEEK_UNITS
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.temporal.TemporalAdjusters

/** Finds the first due date in the text. Never returns a date before today. */
internal object DateMatcher {
    private val ISO_DATE = Regex("^(\\d{4})-(\\d{2})-(\\d{2})$")
    private val DAY_NUMBER = Regex("^(\\d{1,2})(st|nd|rd|th|er)?$")
    private val COUNT = Regex("^\\d{1,3}$")

    fun find(tokens: QuickAddTokens, today: LocalDate): QuickAddMatch<LocalDate>? {
        for (index in 0 until tokens.size) {
            matchAt(tokens, index, today)?.let { return withLeadingPreposition(tokens, it) }
        }
        return null
    }

    /** "on friday" / "le 5 décembre": the preposition goes with the date, not the title. */
    private fun withLeadingPreposition(tokens: QuickAddTokens, match: QuickAddMatch<LocalDate>): QuickAddMatch<LocalDate> {
        val previous = match.range.first - 1
        return if (tokens.word(previous) in DATE_PREPOSITIONS) match.copy(range = previous..match.range.last) else match
    }

    private fun matchAt(tokens: QuickAddTokens, index: Int, today: LocalDate): QuickAddMatch<LocalDate>? =
        relativeDay(tokens, index, today)
            ?: inDuration(tokens, index, today)
            ?: nextWeek(tokens, index, today)
            ?: weekday(tokens, index, today)
            ?: monthDay(tokens, index, today)
            ?: isoDate(tokens, index, today)

    private fun relativeDay(tokens: QuickAddTokens, index: Int, today: LocalDate): QuickAddMatch<LocalDate>? {
        AFTER_TOMORROW_PHRASES.firstOrNull { tokens.hasPhrase(index, it) }?.let { phrase ->
            return QuickAddMatch(today.plusDays(2), index until index + phrase.size)
        }
        val word = tokens.word(index) ?: return null
        val date = when (word) {
            in AFTER_TOMORROW_SINGLE -> today.plusDays(2)
            in TODAY -> today
            in TOMORROW -> today.plusDays(1)
            else -> return null
        }
        return QuickAddMatch(date, index..index)
    }

    private fun inDuration(tokens: QuickAddTokens, index: Int, today: LocalDate): QuickAddMatch<LocalDate>? {
        if (tokens.word(index) !in IN) return null
        val count = tokens.word(index + 1)?.takeIf { COUNT.matches(it) }?.toLong() ?: return null
        val date = when (tokens.word(index + 2)) {
            in DAY_UNITS -> today.plusDays(count)
            in WEEK_UNITS -> today.plusWeeks(count)
            else -> return null
        }
        return QuickAddMatch(date, index..index + 2)
    }

    private fun nextWeek(tokens: QuickAddTokens, index: Int, today: LocalDate): QuickAddMatch<LocalDate>? {
        val phrase = NEXT_WEEK_PHRASES.firstOrNull { tokens.hasPhrase(index, it) } ?: return null
        return QuickAddMatch(today.with(TemporalAdjusters.next(DayOfWeek.MONDAY)), index until index + phrase.size)
    }

    /** "friday" is the nearest Friday, today included; "next friday" / "vendredi prochain" skips today. */
    private fun weekday(tokens: QuickAddTokens, index: Int, today: LocalDate): QuickAddMatch<LocalDate>? {
        if (tokens.word(index) == NEXT_EN) {
            val day = WEEKDAYS[tokens.word(index + 1)] ?: return null
            return QuickAddMatch(today.with(TemporalAdjusters.next(day)), index..index + 1)
        }
        val day = WEEKDAYS[tokens.word(index)] ?: return null
        if (tokens.word(index + 1) in NEXT_FR) {
            return QuickAddMatch(today.with(TemporalAdjusters.next(day)), index..index + 1)
        }
        return QuickAddMatch(today.with(TemporalAdjusters.nextOrSame(day)), index..index)
    }

    /** "dec 5", "december 5th", "5 dec", "5 décembre": a month name is only a date next to a day number. */
    private fun monthDay(tokens: QuickAddTokens, index: Int, today: LocalDate): QuickAddMatch<LocalDate>? {
        val first = tokens.word(index) ?: return null
        val second = tokens.word(index + 1) ?: return null
        val (month, day) = MONTHS[first]?.let { it to dayNumber(second) }
            ?: MONTHS[second]?.let { it to dayNumber(first) }
            ?: return null
        val date = upcoming(month, day ?: return null, today) ?: return null
        return QuickAddMatch(date, index..index + 1)
    }

    private fun isoDate(tokens: QuickAddTokens, index: Int, today: LocalDate): QuickAddMatch<LocalDate>? {
        val match = ISO_DATE.matchEntire(tokens.word(index) ?: return null) ?: return null
        val (year, month, day) = match.destructured
        val date = runCatching { LocalDate.of(year.toInt(), month.toInt(), day.toInt()) }.getOrNull()
        return date?.takeIf { !it.isBefore(today) }?.let { QuickAddMatch(it, index..index) }
    }

    private fun dayNumber(word: String): Int? = DAY_NUMBER.matchEntire(word)?.groupValues?.get(1)?.toInt()

    /** This year's date if it is today or later, otherwise next year's; null if the day does not exist. */
    private fun upcoming(month: Month, day: Int, today: LocalDate): LocalDate? {
        val thisYear = runCatching { LocalDate.of(today.year, month, day) }.getOrNull()
        if (thisYear != null && !thisYear.isBefore(today)) return thisYear
        return runCatching { LocalDate.of(today.year + 1, month, day) }.getOrNull()
    }

    private fun QuickAddTokens.hasPhrase(index: Int, phrase: List<String>): Boolean =
        phrase.withIndex().all { (offset, expected) -> word(index + offset) == expected }
}
