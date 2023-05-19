package com.volvocars.wearablemonitor.presentation.login

import android.view.View

fun LoginState.loginButtonVisibility() = if (isLoading) View.INVISIBLE else View.VISIBLE
fun LoginState.loadingVisibility() = if (isLoading) View.VISIBLE else View.INVISIBLE
