package com.volvocars.diabetesmonitor.feature_glucose.presentation.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.volvocars.diabetesmonitor.core.util.Constants.ACTION_REQUIRE_CONFIGURATION
import com.volvocars.diabetesmonitor.core.util.Constants.ACTION_SHOW_GLUCOSE_VALUES
import com.volvocars.diabetesmonitor.databinding.FragmentLoginBinding
import com.volvocars.diabetesmonitor.feature_glucose.presentation.diabetes_monitor.DiabetesMonitorFragment
import com.volvocars.diabetesmonitor.service.NotificationService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    // Init the view model
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.serverStatus.collect { state ->
                binding.login.visibility = if (state.isLoading) View.INVISIBLE else View.VISIBLE
                binding.loading.visibility = if (state.isLoading) View.VISIBLE else View.INVISIBLE

                if (state.isError) {
                    Toast.makeText(requireContext(), state.errorMessage, LENGTH_LONG).show()
                }

                if (state.isSignedIn) {
                    viewModel.sharedPreferenceStorage.setBaseUrl(binding.url.text.toString())
                    viewModel.sharedPreferenceStorage.setUserSignedIn(true)
                    viewModel.sharedPreferenceStorage.setPreferenceFromServerStatus(state.serverStatus.first()!!)
                    loginSuccess()
                }
            }
        }

        // Check if a user already is signed in.. If yes, move to monitoring fragment instead
        val isUserSignedIn = viewModel.sharedPreferenceStorage.userSignedIn()
        val baseUrl = viewModel.sharedPreferenceStorage.getBaseUrl()
        Log.d(TAG, "onViewCreated: UserSignedIn[$isUserSignedIn]->[$baseUrl]")

        // If a user already signed in, continue to monitoring view
        if (isUserSignedIn) {
            loginSuccess()
        }
        // If a user isn't signed in, start notification configurations is required
        sendCommandToService(ACTION_REQUIRE_CONFIGURATION, true)

        binding.login.setOnClickListener {
            tryLoginIn()
        }

        binding.url.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_DONE || action == EditorInfo.IME_ACTION_GO) {
                tryLoginIn()
            }
            false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     *  Try to login with the baseurl provided in url edittext
     */
    private fun tryLoginIn() {
        val urlFieldText = binding.url.text.toString()
        viewModel.login(urlFieldText)
    }

    /**
     * When we receive a successful api call and the api is enabled
     */
    private fun loginSuccess() {
        // Remove the the notification that tells that configuration is required.
        sendCommandToService(ACTION_REQUIRE_CONFIGURATION, false)

        // If the glucose notification is enabled, start to show the glucose values as a notification.
        if (viewModel.sharedPreferenceStorage.getGlucoseNotificationEnabled()) {
            sendCommandToService(
                ACTION_SHOW_GLUCOSE_VALUES,
                viewModel.sharedPreferenceStorage.getGlucoseNotificationEnabled()
            )
        }

        // Navigate to the monitoring view.
        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(this.id, DiabetesMonitorFragment())?.commit()

    }

    private fun sendCommandToService(action: String, state: Boolean) {
        Intent(requireContext(), NotificationService::class.java).also {
            it.action = action
            if (state) requireContext().startService(it) else requireContext().stopService(it)
        }
    }

    companion object {
        val TAG = LoginFragment::class.simpleName
    }
}
