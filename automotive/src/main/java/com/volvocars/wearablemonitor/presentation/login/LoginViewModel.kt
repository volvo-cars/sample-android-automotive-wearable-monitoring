package com.volvocars.wearablemonitor.presentation.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.volvocars.wearablemonitor.domain.model.ServerStatus
import com.volvocars.wearablemonitor.domain.usecase.FetchServerStatus
import com.volvocars.wearablemonitor.domain.usecase.IsUserSignedIn
import com.volvocars.wearablemonitor.domain.usecase.SetBaseUrl
import com.volvocars.wearablemonitor.domain.usecase.SetPreferenceFromServerStatus
import com.volvocars.wearablemonitor.domain.usecase.SetUserSignedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.features.HttpRequestTimeoutException
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val getServerStatus: FetchServerStatus,
    private val isUserSignedIn: IsUserSignedIn,
    private val setBaseUrl: SetBaseUrl,
    private val setUserSignedIn: SetUserSignedIn,
    private val setPreferenceFromServerStatus: SetPreferenceFromServerStatus,
) : ViewModel() {

    private val _serverStatus = MutableStateFlow(LoginState())
    val serverStatus = _serverStatus.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    /**
     * Validate and attempt to login to the server.
     *
     * Emits [LoginState.urlError] for field-level problems (empty / missing https)
     * without hitting the network. Network timeouts are caught and surfaced
     * through [LoginState.errorMessage].
     *
     * @param url - URL of the NightScout device.
     */
    fun login(url: String) = viewModelScope.launch {
        // --- client-side validation ---
        val trimmed = url.trim()
        when {
            trimmed.isBlank() -> {
                _serverStatus.value = serverStatus.value.copy(
                    urlError = ERROR_URL_EMPTY,
                    isError = false,
                    isLoading = false,
                )
                return@launch
            }

            !trimmed.startsWith("https://") -> {
                _serverStatus.value = serverStatus.value.copy(
                    urlError = ERROR_URL_HTTPS,
                    isError = false,
                    isLoading = false,
                )
                return@launch
            }
        }

        // Clear any previous field error and start loading.
        _serverStatus.value = serverStatus.value.copy(
            urlError = null,
            isError = false,
            isLoading = true,
        )

        // --- network call ---
        try {
            getServerStatus(trimmed).collect { result ->
                Log.d(TAG, "login: $result")
                result.onSuccess {
                    _serverStatus.value = serverStatus.value.copy(
                        serverStatus = listOf(it),
                        isLoading = false,
                        isSignedIn = true,
                        isError = false,
                    )
                }.onFailure { cause ->
                    val message = if (cause is HttpRequestTimeoutException) {
                        ERROR_TIMEOUT
                    } else {
                        cause.message ?: ERROR_GENERIC
                    }
                    _serverStatus.value = serverStatus.value.copy(
                        serverStatus = emptyList(),
                        isLoading = false,
                        isSignedIn = false,
                        isError = true,
                        errorMessage = message,
                    )
                }
            }
        } catch (e: HttpRequestTimeoutException) {
            _serverStatus.value = serverStatus.value.copy(
                isLoading = false,
                isSignedIn = false,
                isError = true,
                errorMessage = ERROR_TIMEOUT,
            )
        }
    }

    fun isUserSignedIn() = isUserSignedIn.invoke()

    fun saveServerInformation(url: String, serverStatus: ServerStatus) {
        setBaseUrl(url)
        setUserSignedIn(true)
        setPreferenceFromServerStatus(serverStatus)
    }

    companion object {
        val TAG = LoginViewModel::class.simpleName

        const val ERROR_URL_EMPTY = "URL field cannot be empty"
        const val ERROR_URL_HTTPS = "URL must start with https://"
        const val ERROR_TIMEOUT = "Server did not respond in time. Check the URL and your network connection."
        const val ERROR_GENERIC = "An unexpected error occurred"
    }
}
