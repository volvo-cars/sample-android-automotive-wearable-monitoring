package com.volvocars.wearable_monitor.feature_glucose.presentation.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.volvocars.wearable_monitor.feature_glucose.domain.storage.Storage
import com.volvocars.wearable_monitor.feature_glucose.domain.use_case.FetchServerStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val getServerStatus: FetchServerStatus,
    val sharedPreferenceStorage: Storage,
) : ViewModel() {
    private val _serverStatus = MutableStateFlow(LoginState())
    val serverStatus = _serverStatus.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    /**
     * Try to login to the server.
     *
     * @param url - URL of the device.
     */
    fun login(
        url: String
    ) = viewModelScope.launch {
        _serverStatus.value = serverStatus.value.copy(isLoading = true)
        getServerStatus(url).collect { result ->
            Log.d(TAG, "login: $result")
            result.onSuccess {
                _serverStatus.value = serverStatus.value.copy(
                    serverStatus = listOf(it),
                    isLoading = false,
                    isSignedIn = true,
                    isError = false,
                )
            }.onFailure {
                _serverStatus.value = serverStatus.value.copy(
                    serverStatus = listOf(),
                    isLoading = false,
                    isSignedIn = false,
                    isError = true,
                    errorMessage = it.message
                )
            }
        }
    }

    companion object {
        val TAG = LoginViewModel::class.simpleName
    }
}