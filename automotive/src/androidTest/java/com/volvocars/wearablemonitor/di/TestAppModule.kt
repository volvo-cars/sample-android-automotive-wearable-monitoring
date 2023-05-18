package com.volvocars.wearablemonitor.di

import android.content.Context
import android.content.SharedPreferences
import com.volvocars.wearablemonitor.data.repository.FakeDiabetesRepository
import com.volvocars.wearablemonitor.data.repository.FakePreferenceRepository
import com.volvocars.wearablemonitor.data.storage.SharedPreferenceStorage
import com.volvocars.wearablemonitor.domain.repository.DiabetesRepository
import com.volvocars.wearablemonitor.domain.repository.PreferenceRepository
import com.volvocars.wearablemonitor.domain.storage.Storage
import com.volvocars.wearablemonitor.domain.usecase.DeleteCachedGlucoseValues
import com.volvocars.wearablemonitor.domain.usecase.FetchGlucoseValues
import com.volvocars.wearablemonitor.domain.usecase.FetchServerStatus
import com.volvocars.wearablemonitor.domain.usecase.ObserveCachedGlucoseValues
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AppModule::class]
)
object TestAppModule {

    @Singleton
    @Provides
    fun provideDiabetesRepository(): DiabetesRepository = FakeDiabetesRepository()

    @Provides
    @Singleton
    fun providePreferenceRepository(): PreferenceRepository = FakePreferenceRepository()

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
            "TEST",
            Context.MODE_PRIVATE
        )

    @Provides
    @Singleton
    fun provideSharedPreferenceStorage(sharedPreferences: SharedPreferences): Storage =
        SharedPreferenceStorage(sharedPreferences)

}