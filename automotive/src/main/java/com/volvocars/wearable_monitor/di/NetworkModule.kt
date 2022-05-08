package com.volvocars.wearable_monitor.di

import com.volvocars.wearable_monitor.core.util.Constants
import com.volvocars.wearable_monitor.feature_glucose.data.remote.NightScoutApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * @return an [GsonConverterFactory] instance
     */
    @Provides
    @Singleton
    fun provideConverterFactory(): Converter.Factory = GsonConverterFactory.create()

    /**
     * @return an [OkHttpClient] instance
     */
    @Singleton
    @Provides
    fun providesOkHttpClient(
    ) = OkHttpClient().apply {
        OkHttpClient.Builder().run {
            callTimeout(20, TimeUnit.SECONDS)
            connectTimeout(20, TimeUnit.SECONDS)
            writeTimeout(20, TimeUnit.SECONDS)
            readTimeout(20, TimeUnit.SECONDS)
            build()
        }
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        gson: Converter.Factory,
        client: OkHttpClient,
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(gson)
            .client(client)
            .build()

    /**
     * @param retrofit [Retrofit] instance
     * @return [NightScoutApi] impl
     */
    @Singleton
    @Provides
    fun providesNightScoutApiService(
        retrofit: Retrofit,
    ): NightScoutApi {
        return retrofit.create(NightScoutApi::class.java)
    }
}