package com.wismna.geoffroy.donext.domain.quickadd

import com.wismna.geoffroy.donext.domain.quickadd.QuickAddVocabulary.AT
import java.time.LocalTime

/** Finds the first time of day in the text: "9am", "9:30 pm", "14:30", "9h", "9h30", optionally after "at" / "à". */
internal object TimeMatcher {
    private val TWELVE_HOUR = Regex("^(\\d{1,2})(?::(\\d{2}))?(am|pm)$")
    private val TWENTY_FOUR_HOUR = Regex("^(\\d{1,2}):(\\d{2})$")
    private val FRENCH_HOUR = Regex("^(\\d{1,2})h(\\d{2})?$")
    private val CLOCK_DIGITS = Regex("^(\\d{1,2})(?::(\\d{2}))?$")
    private val MERIDIEMS = setOf("am", "pm")
    private const val HOURS_ON_CLOCK_FACE = 12

    fun find(tokens: QuickAddTokens): QuickAddMatch<LocalTime>? {
        for (index in 0 until tokens.size) {
            matchAt(tokens, index)?.let { return withLeadingAt(tokens, it) }
        }
        return null
    }

    private fun matchAt(tokens: QuickAddTokens, index: Int): QuickAddMatch<LocalTime>? {
        val word = tokens.word(index) ?: return null
        // "9:30 pm" must be read before "9:30" alone, which would otherwise match as a 24-hour time
        separateMeridiemTime(word, tokens.word(index + 1))?.let { return QuickAddMatch(it, index..index + 1) }
        return singleWordTime(word)?.let { QuickAddMatch(it, index..index) }
    }

    private fun separateMeridiemTime(word: String, nextWord: String?): LocalTime? {
        val meridiem = nextWord?.takeIf { it in MERIDIEMS } ?: return null
        val digits = CLOCK_DIGITS.matchEntire(word) ?: return null
        return twelveHour(digits.groupValues[1], digits.groupValues[2], meridiem)
    }

    private fun singleWordTime(word: String): LocalTime? {
        TWELVE_HOUR.matchEntire(word)?.let { return twelveHour(it.groupValues[1], it.groupValues[2], it.groupValues[3]) }
        TWENTY_FOUR_HOUR.matchEntire(word)?.let { return timeOf(it.groupValues[1], it.groupValues[2]) }
        FRENCH_HOUR.matchEntire(word)?.let { return timeOf(it.groupValues[1], it.groupValues[2]) }
        return null
    }

    private fun twelveHour(hour: String, minute: String, meridiem: String): LocalTime? {
        val clockHour = hour.toInt().takeIf { it in 1..HOURS_ON_CLOCK_FACE } ?: return null
        val hourOfDay = clockHour % HOURS_ON_CLOCK_FACE + if (meridiem == "pm") HOURS_ON_CLOCK_FACE else 0
        return timeOf(hourOfDay.toString(), minute)
    }

    private fun timeOf(hour: String, minute: String): LocalTime? =
        runCatching { LocalTime.of(hour.toInt(), minute.ifEmpty { "0" }.toInt()) }.getOrNull()

    private fun withLeadingAt(tokens: QuickAddTokens, match: QuickAddMatch<LocalTime>): QuickAddMatch<LocalTime> {
        val previous = match.range.first - 1
        return if (tokens.word(previous) in AT) match.copy(range = previous..match.range.last) else match
    }
}
