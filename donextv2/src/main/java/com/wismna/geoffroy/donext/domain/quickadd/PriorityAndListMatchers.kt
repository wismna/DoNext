package com.wismna.geoffroy.donext.domain.quickadd

import com.wismna.geoffroy.donext.domain.model.Priority
import com.wismna.geoffroy.donext.domain.model.TaskList

/** Finds the first priority marker: "!high", "!haute", "!!", "!normal", "!low", "!basse". */
internal object PriorityMatcher {
    fun find(tokens: QuickAddTokens): QuickAddMatch<Priority>? {
        for (index in 0 until tokens.size) {
            val priority = QuickAddVocabulary.PRIORITIES[tokens.word(index)] ?: continue
            return QuickAddMatch(priority, index..index)
        }
        return null
    }
}

/** Finds "#Name" for an existing list, case and accent-insensitively; the longest matching list name wins. */
internal object ListMatcher {
    private const val LIST_PREFIX = "#"
    private val WHITESPACE = Regex("\\s+")

    fun find(tokens: QuickAddTokens, taskLists: List<TaskList>): QuickAddMatch<Long>? {
        val candidates = listCandidates(taskLists)
        for (index in 0 until tokens.size) {
            val word = tokens.word(index)?.takeIf { it.startsWith(LIST_PREFIX) } ?: continue
            val firstWord = word.removePrefix(LIST_PREFIX)
            candidates.firstOrNull { (_, nameWords) -> tokens.matchesName(index, firstWord, nameWords) }
                ?.let { (listId, nameWords) -> return QuickAddMatch(listId, index until index + nameWords.size) }
        }
        return null
    }

    private fun listCandidates(taskLists: List<TaskList>): List<Pair<Long, List<String>>> =
        taskLists
            .mapNotNull { list -> list.id?.let { id -> id to list.name.trim().split(WHITESPACE).map(QuickAddTokens::normalize) } }
            .sortedByDescending { (_, nameWords) -> nameWords.size }

    private fun QuickAddTokens.matchesName(index: Int, firstWord: String, nameWords: List<String>): Boolean =
        nameWords.first() == firstWord &&
            nameWords.drop(1).withIndex().all { (offset, expected) -> word(index + 1 + offset) == expected }
}
