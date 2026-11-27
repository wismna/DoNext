package com.wismna.geoffroy.donext.domain.usecase

import com.wismna.geoffroy.donext.domain.model.QuickAddResult
import com.wismna.geoffroy.donext.domain.model.TaskList
import com.wismna.geoffroy.donext.domain.quickadd.QuickAddParser
import java.time.Clock
import java.time.LocalDateTime
import javax.inject.Inject

class ParseQuickAddUseCase @Inject constructor(
    private val parser: QuickAddParser,
    private val clock: Clock
) {
    operator fun invoke(text: String, taskLists: List<TaskList>): QuickAddResult =
        parser.parse(text, LocalDateTime.now(clock), taskLists)
}
