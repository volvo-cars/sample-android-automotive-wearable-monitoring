package com.volvocars.wearable_monitor.di

import android.app.Application
import androidx.room.Room
import com.volvocars.wearable_monitor.feature_glucose.data.local.GlucoseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class]
)
object TestDatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application): GlucoseDatabase =
        Room.inMemoryDatabaseBuilder(
            app,
            GlucoseDatabase::class.java,
        ).fallbackToDestructiveMigration().build()
}