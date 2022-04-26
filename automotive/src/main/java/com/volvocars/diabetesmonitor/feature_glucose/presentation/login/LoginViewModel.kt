package com.volvocars.diabetesmonitor.feature_glucose.presentation.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.volvocars.diabetesmonitor.core.util.Resource
import com.volvocars.diabetesmonitor.feature_glucose.domain.storage.Storage
import com.volvocars.diabetesmonitor.feature_glucose.domain.use_case.FetchServerStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
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
    ) {
        viewModelScope.launch {
            getServerStatus(url).collect { result ->
                Log.d(TAG, "login: $result ${result.message}")
                when (result) {
                    is Resource.Success -> {
                        _serverStatus.value = serverStatus.value.copy(
                            serverStatus = listOf(result.data),
                            isLoading = false,
                            isSignedIn = true,
                            isError = false,
                        )
                    }
                    is Resource.Error -> {
                        Log.d(TAG, "login: ${result.message}")
                        _serverStatus.value = serverStatus.value.copy(
                            serverStatus = listOf(result.data),
                            isLoading = false,
                            isSignedIn = false,
                            isError = true,
                            errorMessage = result.message
                        )
                    }
                    is Resource.Loading -> {
                        _serverStatus.value = serverStatus.value.copy(
                            serverStatus = listOf(result.data),
                            isLoading = true,
                            isSignedIn = false,
                            isError = false,
                        )
                    }
                }
            }
        }
    }

    companion object {
        val TAG = LoginViewModel::class.simpleName
    }
}