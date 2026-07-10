package com.example.soavertriggertracker.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BindingModule {
    @Binds
    @Singleton
    abstract fun bindLogRepository(
        logRepositoryImpl: LogRepositoryImpl
    ): LogRepository

    @Binds
    @Singleton
    abstract fun bindLogSupabaseLink(
        logSupabaseLinkImpl: LogSupabaseLinkImpl
    ): LogSupabaseLink

    @Binds
    @Singleton
    abstract fun bindTriggerSupabaseLink(
        triggerSupabaseLinkImpl: TriggerSupabaseLinkImpl
    ): TriggerSupabaseLink
}