package com.volvocars.wearable_monitor.di

import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import com.volvocars.wearable_monitor.service.GlucoseFetchWorker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkModule {

    @Provides
    @Singleton
    fun provideConstraints(): Constraints =
        Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

    @Provides
    @Singleton
    fun provideFetchDataWorker(constraints: Constraints): PeriodicWorkRequest =
        PeriodicWorkRequestBuilder<GlucoseFetchWorker>(15, TimeUnit.MINUTES).setConstraints(
            constraints
        ).build()
}