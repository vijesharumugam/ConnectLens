package com.connectlens.app.di

import com.connectlens.app.data.repository.CallLogRepositoryImpl
import com.connectlens.app.data.repository.ContactRepositoryImpl
import com.connectlens.app.domain.repository.CallLogRepository
import com.connectlens.app.domain.repository.ContactRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds repository interfaces to their concrete implementations.
 * Using @Binds is more efficient than @Provides for interface → impl wiring.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCallLogRepository(
        impl: CallLogRepositoryImpl
    ): CallLogRepository

    @Binds
    @Singleton
    abstract fun bindContactRepository(
        impl: ContactRepositoryImpl
    ): ContactRepository
}
