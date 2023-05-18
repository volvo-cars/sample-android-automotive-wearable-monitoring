package com.volvocars.wearablemonitor.di

import android.app.Application
import androidx.room.Room
import com.volvocars.wearablemonitor.data.local.database.GlucoseDatabase
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