package com.volvocars.wearable_monitor.di

import android.app.Application
import androidx.room.Room
import com.volvocars.wearable_monitor.feature_glucose.data.local.database.GlucoseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application): GlucoseDatabase =
        Room.databaseBuilder(
            app,
            GlucoseDatabase::class.java,
            "diabetes_database.db"
        ).fallbackToDestructiveMigration().build()
}