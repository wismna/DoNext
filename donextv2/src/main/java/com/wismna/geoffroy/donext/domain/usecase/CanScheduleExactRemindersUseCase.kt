package com.wismna.geoffroy.donext.domain.usecase

import com.wismna.geoffroy.donext.domain.repository.ReminderScheduler
import javax.inject.Inject

class CanScheduleExactRemindersUseCase @Inject constructor(
    private val reminderScheduler: ReminderScheduler
) {
    operator fun invoke(): Boolean = reminderScheduler.canScheduleExactReminders()
}
