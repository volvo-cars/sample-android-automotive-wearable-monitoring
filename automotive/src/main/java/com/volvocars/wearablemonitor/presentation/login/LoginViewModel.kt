package com.volvocars.wearablemonitor.presentation.login

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.car.ui.core.CarUi
import com.android.car.ui.toolbar.ToolbarController
import com.volvocars.wearablemonitor.R
import com.volvocars.wearablemonitor.domain.model.ServerStatus
import com.volvocars.wearablemonitor.domain.usecase.FetchServerStatus
import com.volvocars.wearablemonitor.domain.usecase.IsUserSignedIn
import com.volvocars.wearablemonitor.domain.usecase.SetBaseUrl
import com.volvocars.wearablemonitor.domain.usecase.SetPreferenceFromServerStatus
import com.volvocars.wearablemonitor.domain.usecase.SetUserSignedIn
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
    private val isUserSignedIn: IsUserSignedIn,
    private val setBaseUrl: SetBaseUrl,
    private val setUserSignedIn: SetUserSignedIn,
    private val setPreferenceFromServerStatus: SetPreferenceFromServerStatus
) : ViewModel() {
    private lateinit var toolbarController: ToolbarController

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

    fun isUserSignedIn() = isUserSignedIn.invoke()

    fun saveServerInformation(url: String, serverStatus: ServerStatus) {
        setBaseUrl(url)
        setUserSignedIn(true)
        setPreferenceFromServerStatus(serverStatus)
    }

    fun initToolbar(activity: Activity) {
        toolbarController = CarUi.requireToolbar(activity)
        toolbarController.setLogo(R.mipmap.wearable_monitor_ic_launcher_round)
        toolbarController.setTitle(R.string.app_name)
    }

    companion object {
        val TAG = LoginViewModel::class.simpleName
    }
}