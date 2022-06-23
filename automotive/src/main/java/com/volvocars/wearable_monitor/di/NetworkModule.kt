package com.volvocars.wearable_monitor.di

import android.util.Log
import com.volvocars.wearable_monitor.feature_glucose.data.remote.NightScoutApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.features.observer.*
import io.ktor.client.request.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun provideNightScoutApiV2(client: HttpClient): NightScoutApi {
        return NightScoutApi(client)
    }

    @Singleton
    @Provides
    fun provideKtorClient(): HttpClient {
        return HttpClient(Android) {
            install(JsonFeature) {
                serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })

                engine {
                    connectTimeout = 60_000
                    socketTimeout = 60_000
                }
            }

            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.v("Logger Ktor => ", message)
                    }
                }
                level = LogLevel.ALL
            }

            install(ResponseObserver) {
                onResponse {
                    Log.d("HTTP status: ", "${it.status.value}")
                }
            }

            install(DefaultRequest) {
                header(
                    io.ktor.http.HttpHeaders.ContentType,
                    io.ktor.http.ContentType.Application.Json
                )
            }
        }
    }
}