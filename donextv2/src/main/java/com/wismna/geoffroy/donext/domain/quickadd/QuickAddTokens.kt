package com.wismna.geoffroy.donext.domain.quickadd

import java.text.Normalizer
import java.util.Locale

/** The words of a quick-add text, with the ones already recognized as shortcuts set aside. */
internal class QuickAddTokens(text: String) {
    private val raw: List<String> = text.trim().split(WHITESPACE).filter { it.isNotEmpty() }
    private val normalized: List<String> = raw.map { normalize(it) }
    private val consumed = BooleanArray(raw.size)

    val size: Int get() = raw.size

    /** The normalized word at [index], or null if it is out of range or already used by a shortcut. */
    fun word(index: Int): String? = normalized.getOrNull(index)?.takeIf { !consumed[index] }

    fun consume(range: IntRange) = range.forEach { consumed[it] = true }

    fun release(range: IntRange) = range.forEach { consumed[it] = false }

    fun remainingText(): String = raw.filterIndexed { index, _ -> !consumed[index] }.joinToString(" ")

    companion object {
        private val WHITESPACE = Regex("\\s+")
        private val DIACRITICS = Regex("\\p{Mn}+")
        private val TRAILING_PUNCTUATION = Regex("[,;.]+$")

        /** Lowercase, accent-free form so "Décembre", "decembre" and "DECEMBRE" compare equal. */
        fun normalize(word: String): String {
            val withoutAccents = Normalizer.normalize(word, Normalizer.Form.NFD).replace(DIACRITICS, "")
            return withoutAccents.lowercase(Locale.ROOT).replace(TRAILING_PUNCTUATION, "")
        }
    }
}

/** A shortcut found in the text: its value and the words it covers. */
internal data class QuickAddMatch<T>(val value: T, val range: IntRange)
