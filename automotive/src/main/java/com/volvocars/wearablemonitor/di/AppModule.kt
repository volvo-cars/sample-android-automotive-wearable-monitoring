package com.volvocars.wearablemonitor.di

import android.content.Context
import android.content.SharedPreferences
import com.volvocars.wearablemonitor.R
import com.volvocars.wearablemonitor.data.local.database.GlucoseDatabase
import com.volvocars.wearablemonitor.data.remote.NightScoutApi
import com.volvocars.wearablemonitor.data.repository.DiabetesRepositoryImpl
import com.volvocars.wearablemonitor.data.repository.PreferenceRepositoryImpl
import com.volvocars.wearablemonitor.data.storage.SharedPreferenceStorage
import com.volvocars.wearablemonitor.domain.repository.DiabetesRepository
import com.volvocars.wearablemonitor.domain.repository.PreferenceRepository
import com.volvocars.wearablemonitor.domain.storage.Storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
    fun provideDiabetesRepository(
        api: NightScoutApi,
        database: GlucoseDatabase,
    ): DiabetesRepository = DiabetesRepositoryImpl(
        api,
        database.glucoseDao(),
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