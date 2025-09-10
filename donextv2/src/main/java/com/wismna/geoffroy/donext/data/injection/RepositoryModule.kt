package com.wismna.geoffroy.donext.data.injection

import com.wismna.geoffroy.donext.data.local.repository.TaskRepositoryImpl
import com.wismna.geoffroy.donext.domain.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindTaskRepository(
        impl: TaskRepositoryImpl
    ): TaskRepository
}