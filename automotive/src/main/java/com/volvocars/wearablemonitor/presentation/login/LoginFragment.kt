package com.volvocars.wearablemonitor.presentation.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.volvocars.wearablemonitor.R
import com.volvocars.wearablemonitor.core.service.WearableMonitorService
import com.volvocars.wearablemonitor.core.util.NotificationConstants.ACTION_REQUIRE_CONFIGURATION
import com.volvocars.wearablemonitor.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
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
        listenToServerState()
        startForegroundService()
        initLoginButton()
        initUrlTextView()

        if (viewModel.isUserSignedIn()) {
            loginSuccess()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.initToolbar(requireActivity())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun listenToServerState() = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.serverStatus.collect { state ->
                binding.login.visibility = state.loginButtonVisibility()
                binding.loading.visibility = state.loadingVisibility()

                if (state.isError) {
                    Toast.makeText(requireContext(), state.errorMessage, LENGTH_LONG).show()
                }

                if (state.isSignedIn) {
                    viewModel.saveServerInformation(
                        binding.url.text.toString(),
                        state.serverStatus.first()
                    )
                    loginSuccess()
                }
            }
        }
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
        findNavController().navigate(R.id.diabetesMonitorFragment)
    }

    private fun startForegroundService() {
        WearableMonitorService.startAsForeground(requireContext(), ACTION_REQUIRE_CONFIGURATION)
    }

    private fun initLoginButton() {
        binding.login.setOnClickListener { tryLoginIn() }
    }

    private fun initUrlTextView() {
        binding.url.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_DONE || action == EditorInfo.IME_ACTION_GO) {
                tryLoginIn()
            }
            false
        }
    }

    companion object {
        val TAG = LoginFragment::class.simpleName
    }
}
