package com.wismna.geoffroy.donext.data.injection

import com.wismna.geoffroy.donext.data.reminder.AlarmReminderScheduler
import com.wismna.geoffroy.donext.domain.repository.ReminderScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ReminderModule {

    @Binds
    abstract fun bindReminderScheduler(
        impl: AlarmReminderScheduler
    ): ReminderScheduler
}
