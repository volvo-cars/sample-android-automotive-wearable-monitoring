package com.volvocars.diabetesmonitor.di

import android.content.Context
import android.content.SharedPreferences
import com.volvocars.diabetesmonitor.R
import com.volvocars.diabetesmonitor.core.util.GlucoseUtils
import com.volvocars.diabetesmonitor.feature_glucose.domain.storage.Storage
import com.volvocars.diabetesmonitor.feature_glucose.data.remote.NightScoutApi
import com.volvocars.diabetesmonitor.feature_glucose.data.local.GlucoseDatabase
import com.volvocars.diabetesmonitor.feature_glucose.data.repository.DiabetesRepositoryImpl
import com.volvocars.diabetesmonitor.feature_glucose.domain.repository.DiabetesRepository
import com.volvocars.diabetesmonitor.feature_glucose.domain.use_case.DeleteCachedGlucoseValues
import com.volvocars.diabetesmonitor.feature_glucose.domain.use_case.FetchGlucoseValues
import com.volvocars.diabetesmonitor.feature_glucose.domain.use_case.FetchServerStatus
import com.volvocars.diabetesmonitor.feature_glucose.domain.use_case.ObserveCachedGlucoseValues
import com.volvocars.diabetesmonitor.feature_glucose.data.storage.SharedPreferenceStorage
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
    ): DiabetesRepository =
        DiabetesRepositoryImpl(api, database.glucoseDao(), sharedPreferenceStorage)

    @Provides
    @Singleton
    fun provideDeletedCachedGlucoseValues(repository: DiabetesRepository): DeleteCachedGlucoseValues =
        DeleteCachedGlucoseValues(repository)

    @Provides
    @Singleton
    fun provideGetGlucoseValues(repository: DiabetesRepository): FetchGlucoseValues =
        FetchGlucoseValues(repository)

    @Provides
    @Singleton
    fun provideGetServerStatus(repository: DiabetesRepository): FetchServerStatus =
        FetchServerStatus(repository)

    @Singleton
    @Provides
    fun provideObserveCachedGlucoseValues(repository: DiabetesRepository): ObserveCachedGlucoseValues =
        ObserveCachedGlucoseValues(repository)

    @Singleton
    @Provides
    fun provideSharedPreference(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences(
            R.string.pk_instance_setting.toString(),
            Context.MODE_PRIVATE
        )

    @Provides
    @Singleton
    fun provideSharedPreferenceStorage(sharedPreferences: SharedPreferences): Storage =
        SharedPreferenceStorage(sharedPreferences)
}