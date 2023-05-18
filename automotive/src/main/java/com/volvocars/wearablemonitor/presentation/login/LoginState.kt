package com.volvocars.wearablemonitor.presentation.login

import com.volvocars.wearablemonitor.domain.model.ServerStatus

data class LoginState(
    val serverStatus: List<ServerStatus?> = emptyList(),
    val isSignedIn: Boolean = false,
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null
)
