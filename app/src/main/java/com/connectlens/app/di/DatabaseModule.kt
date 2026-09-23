package com.connectlens.app.di

import android.content.Context
import androidx.room.Room
import com.connectlens.app.data.local.db.AppDatabase
import com.connectlens.app.data.local.db.CallRecordDao
import com.connectlens.app.data.local.db.ContactCacheDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "connectlens.db"
    )
        // Version 1 is the only version; no migrations exist yet.
        // If the schema changes in future, add Migration objects before removing this.
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    fun provideCallRecordDao(db: AppDatabase): CallRecordDao =
        db.callRecordDao()

    @Provides
    fun provideContactCacheDao(db: AppDatabase): ContactCacheDao =
        db.contactCacheDao()
}
