package com.volvocars.wearable_monitor.di

import android.content.Context
import android.content.SharedPreferences
import com.volvocars.wearable_monitor.R
import com.volvocars.wearable_monitor.core.util.GlucoseUtils
import com.volvocars.wearable_monitor.feature_glucose.domain.storage.Storage
import com.volvocars.wearable_monitor.feature_glucose.data.local.GlucoseDatabase
import com.volvocars.wearable_monitor.feature_glucose.data.remote.NightScoutApi
import com.volvocars.wearable_monitor.feature_glucose.data.repository.DiabetesRepositoryImpl
import com.volvocars.wearable_monitor.feature_glucose.data.repository.PreferenceRepositoryImpl
import com.volvocars.wearable_monitor.feature_glucose.domain.repository.DiabetesRepository
import com.volvocars.wearable_monitor.feature_glucose.data.storage.SharedPreferenceStorage
import com.volvocars.wearable_monitor.feature_glucose.domain.repository.PreferenceRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideDispatcherIO(): CoroutineDispatcher = Dispatchers.IO

    @Singleton
    @Provides
    fun provideDispatcherDefault(): CoroutineDispatcher = Dispatchers.Default

    @Singleton
    @Provides
    fun provideGlucoseColorPoint(
        sharedPreferenceStorage: SharedPreferenceStorage,
        @ApplicationContext context: Context,
    ): GlucoseUtils = GlucoseUtils(sharedPreferenceStorage, context)

    @Singleton
    @Provides
    fun provideDiabetesRepository(
        api: NightScoutApi,
        database: GlucoseDatabase,
        sharedPreferenceStorage: SharedPreferenceStorage
    ): DiabetesRepository = DiabetesRepositoryImpl(
        api,
        database.glucoseDao(),
        sharedPreferenceStorage
    )

    @Provides
    @Singleton
    fun providePreferenceRepository(
        sharedPreferenceStorage: SharedPreferenceStorage
    ): PreferenceRepository = PreferenceRepositoryImpl(sharedPreferenceStorage)

    @Singleton
    @Provides
    fun provideSharedPreference(
        @ApplicationContext context: Context
    ): SharedPreferences = context.getSharedPreferences(
        R.string.pk_instance_setting.toString(),
        Context.MODE_PRIVATE
    )

    @Provides
    @Singleton
    fun provideSharedPreferenceStorage(
        sharedPreferences: SharedPreferences
    ): Storage = SharedPreferenceStorage(sharedPreferences)
}