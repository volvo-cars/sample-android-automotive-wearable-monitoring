package com.volvocars.diabetesmonitor.feature_glucose.presentation.login

import com.volvocars.diabetesmonitor.feature_glucose.domain.model.ServerStatus

data class LoginState(
    val serverStatus: List<ServerStatus?> = emptyList(),
    val isSignedIn: Boolean = false,
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null
)
