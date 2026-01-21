package cyn.mobile.app.ui.main.fragment

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import cyn.mobile.app.R
import cyn.mobile.app.databinding.FragmentTestBinding
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import cyn.mobile.app.BuildConfig
import cyn.mobile.app.security.AuthStorage
import cyn.mobile.app.ui.main.viewmodel.OAuthViewModel
import cyn.mobile.app.ui.main.viewmodel.OAuthViewState
import generateCynIdTimeOrdered
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import cyn.mobile.app.data.repositories.transaction.request.StoreTestResultRequest
import cyn.mobile.app.ui.main.viewmodel.TransactionViewModel
import cyn.mobile.app.ui.main.viewmodel.TransactionViewState
import android.widget.Toast
import cyn.mobile.app.data.model.RecentVerificationItem
import cyn.mobile.app.ui.main.activity.MainActivity
import cyn.mobile.app.ui.main.viewmodel.DashboardViewModel
import cyn.mobile.app.ui.main.viewmodel.DashboardViewState
import cyn.mobile.app.ui.main.viewmodel.OtpViewModel
import cyn.mobile.app.ui.main.viewmodel.OtpViewState
import cyn.mobile.app.utils.dialog.CommonDialog
import cyn.mobile.app.utils.dialog.CommonsErrorDialog
import cyn.mobile.app.utils.dialog.showErrorDialog
import cyn.mobile.app.utils.dialog.showOtpDialog
import cyn.mobile.app.utils.showToastSuccess
import generateSessionIdTimeOrdered

@AndroidEntryPoint
class TestFragment : Fragment() {

    private var _binding: FragmentTestBinding? = null
    private val binding get() = _binding!!

    private val ui = Handler(Looper.getMainLooper())

    private val oAuthViewModel: OAuthViewModel by viewModels()
    private val otpViewModel: OtpViewModel by viewModels()
    private val transactionViewModel: TransactionViewModel by viewModels()

    @Inject
    lateinit var authStorage: AuthStorage

    private var oauthCollectJob: Job? = null
    private var transactionCollectJob: Job? = null
    private var phase: Phase = Phase.Idle
    private var cynClientId = ""
    private var sessionId: String = ""
    private val activity get() = requireActivity() as MainActivity
    private var phoneNumber = ""


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTestBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resetStatuses()
        setStartButtonState(running = false)

        // Display phone number in testPhoneText
        viewLifecycleOwner.lifecycleScope.launch {
            val phone = authStorage.getUserBasicInfo().contact_number.orEmpty()
            binding.testPhoneText.text = "Test phone number: $phone"
            binding.urlAuthCode.text = "${BuildConfig.BASE_TEST_URL}api/oauth/initiate"
            binding.urlToken.text = "${BuildConfig.BASE_TEST_URL}api/oauth/token"
            binding.urlNVS.text = "${BuildConfig.BASE_TEST_URL}api/oauth/verify"
        }

        binding.startCard.setOnClickListener {
            if (binding.progressContainer.isShown) return@setOnClickListener
            // Start real OAuth flow via ViewModel instead of local demo
            startOAuthFlow()
        }

        observeOAuthState()
        observeTransactionState()
        observeOtp()

        viewLifecycleOwner.lifecycleScope.launch {
            phoneNumber = authStorage.getUserBasicInfo().contact_number.orEmpty()
        }
    }

    private fun observeTransactionState() {
        transactionCollectJob?.cancel()
        transactionCollectJob = viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                transactionViewModel.state.collect { state ->
                    when (state) {
                        is TransactionViewState.Loading -> {
                            // No-op: storing result is background work; keep UI as-is
                        }

                        is TransactionViewState.TestResultStored -> {
                            val msg = state.message
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                        }

                        is TransactionViewState.PopupError -> {
                            val msg = state.message
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun startOAuthFlow() {
        resetStatuses()
        setStartButtonState(running = true)
        binding.progressContainer.isVisible = true
        setProgress(0)
        phase = Phase.Auth

        // Step 1: Initiate OAuth (Authorization Code)
        setStatus(StatusView(binding.iconAuthCode, binding.statusAuthCode), Status.PROCESSING)
        setProgress(20)

        // You can replace the client identifier below with the actual one used by your app
        cynClientId = generateCynIdTimeOrdered()
        sessionId = generateSessionIdTimeOrdered()
        oAuthViewModel.initiateOAuth(clientIdentifier = "CYN-MOBILE-APP-2025")
    }

    private fun observeOAuthState() {
        oauthCollectJob?.cancel()
        oauthCollectJob = viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                oAuthViewModel.state.collect { state ->
                    when (state) {
                        is OAuthViewState.Loading -> {
                            when (phase) {
                                Phase.Auth -> {
                                    setStatus(
                                        StatusView(
                                            binding.iconAuthCode,
                                            binding.statusAuthCode
                                        ), Status.PROCESSING
                                    )
                                    setProgress(33)
                                }

                                Phase.Token -> {
                                    setStatus(
                                        StatusView(binding.iconToken, binding.statusToken),
                                        Status.PROCESSING
                                    )
                                    setProgress(60)
                                }

                                Phase.Verify -> {
                                    setStatus(
                                        StatusView(binding.iconNVS, binding.statusNVS),
                                        Status.PROCESSING
                                    )
                                    setProgress(80)
                                }

                                else -> Unit
                            }
                        }

                        is OAuthViewState.Initiated -> {
                            // Step 1 success
                            setStatus(
                                StatusView(binding.iconAuthCode, binding.statusAuthCode),
                                Status.SUCCESS
                            )
                            setProgress(45)

                            // capture session id for later storage

                            // Move to Step 2: Token exchange
                            phase = Phase.Token
                            setStatus(
                                StatusView(binding.iconToken, binding.statusToken),
                                Status.PROCESSING
                            )
                            setProgress(60)

                            val code = state.code
                            if (sessionId.isNotEmpty() && !code.isNullOrEmpty()) {
                                oAuthViewModel.exchangeToken(code)
                            } else {
                                handlePopupError(state.message)
                            }
                        }

                        is OAuthViewState.TokenExchanged -> {
                            // Step 2 success
                            setStatus(
                                StatusView(binding.iconToken, binding.statusToken),
                                Status.SUCCESS
                            )
                            setProgress(80)

                            // capture/refresh session id if provided
                            state.sessionId?.let { if (it.isNotEmpty()) sessionId = it }

                            // Move to Step 3: Verify phone
                            phase = Phase.Verify
                            setStatus(
                                StatusView(binding.iconNVS, binding.statusNVS),
                                Status.PROCESSING
                            )

                            val accessToken = state.accessToken
                            if (!accessToken.isNullOrEmpty()) {
                                // accessToken is bearer_token_from_step_2
                                oAuthViewModel.verifyPhone(accessToken,)
                            } else {
                                handlePopupError(state.message.orEmpty())
                            }
                        }

                        is OAuthViewState.Verified -> {
                            // Step 3 success
                            setStatus(
                                StatusView(binding.iconNVS, binding.statusNVS),
                                Status.SUCCESS
                            )
                            setProgress(100)
                            callStoreTestResult(1, 1, 1, 1)
                            finishProgress()
                            CommonDialog.openDialog(childFragmentManager, true, "Success")
                        }

                        is OAuthViewState.PopupError -> {
                            handlePopupError(state.message, true)
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun callStoreTestResult(
        authStatus: Int,
        tokenStatus: Int,
        numberVerificationStatus: Int,
        finalStatus: Int,
    ) {
        val request = StoreTestResultRequest(
            transaction_id = cynClientId,              // generated at start
            session_id = sessionId,                    // captured during flow
            phone_number = phoneNumber,
            auth_code = authStatus,                             // success
            token_status = tokenStatus,                          // success
            number_verification = numberVerificationStatus,
            final_status = finalStatus
        )
        transactionViewModel.doStoreTestResult(request)

    }

    private fun handlePopupError(message: String, showVerifyButton: Boolean = false) {
        when (phase) {
            Phase.Auth -> {
                setStatus(StatusView(binding.iconAuthCode, binding.statusAuthCode), Status.FAILED)
                callStoreTestResult(0, 0, 0, 0)
            }

            Phase.Token -> {
                setStatus(StatusView(binding.iconToken, binding.statusToken), Status.FAILED)
                callStoreTestResult(1, 0, 0, 0)
            }

            Phase.Verify -> {
                setStatus(StatusView(binding.iconNVS, binding.statusNVS), Status.FAILED)
                callStoreTestResult(1, 1, 0, 0)
            }

            else -> Unit
        }
        displayErrorDialog(message, showVerifyButton)
        setProgress(100)
        finishProgress()
        //CommonsErrorDialog.openDialog(childFragmentManager, message = "Failed")
        // Optional: surface message to user (Snackbar/Toast/Sheet). Kept silent per request.
    }

    private fun displayErrorDialog(message: String, showVerifyButton: Boolean = false) {
        showErrorDialog(
            requireActivity(), message, showVerifyButton,
            onVerifyPhone = {
                otpViewModel.requestOtp(phoneNumber)
            }
        )
    }

    private fun finishProgress() {
        ui.postDelayed({
            binding.progressContainer.isGone = true
            setStartButtonState(running = false)
            phase = Phase.Idle
        }, 500)
    }

    private fun setStartButtonState(running: Boolean) {
        val container = binding.startContainer
        val icon = binding.startIcon
        val text = binding.startText

        if (running) {
            container.setBackgroundResource(R.drawable.bg_button_running)
            icon.setImageResource(R.drawable.ic_running_progress)
            icon.imageTintList = resources.getColorStateList(android.R.color.white, null)
            text.setText(R.string.test_running_transaction)
            text.setTextColor(resources.getColor(android.R.color.white, null))
            binding.startCard.isClickable = false
            binding.startCard.isFocusable = false
        } else {
            container.setBackgroundResource(R.drawable.bg_button_primary)
            icon.setImageResource(R.drawable.ic_play)
            icon.imageTintList = resources.getColorStateList(android.R.color.white, null)
            text.setText(R.string.test_start_transaction)
            text.setTextColor(resources.getColor(android.R.color.white, null))
            binding.startCard.isClickable = true
            binding.startCard.isFocusable = true
        }
    }

    private fun setProgress(value: Int) {
        binding.progressBar.progress = value
        binding.progressPercent.text = getString(R.string.test_progress_percent, value)
    }

    private fun resetStatuses() {
        setStatus(StatusView(binding.iconAuthCode, binding.statusAuthCode), Status.IDLE)
        setStatus(StatusView(binding.iconToken, binding.statusToken), Status.IDLE)
        setStatus(StatusView(binding.iconNVS, binding.statusNVS), Status.IDLE)
        binding.progressBar.progress = 0
        binding.progressPercent.text = getString(R.string.test_progress_percent, 0)
        binding.progressContainer.isGone = true
    }

    private fun setStatus(target: StatusView, status: Status) {
        val (iconRes, bgRes, textRes) = when (status) {
            Status.IDLE -> Quad(
                R.drawable.ic_idle,
                R.drawable.bg_status_idle,
                R.string.test_status_idle
            )

            Status.PROCESSING -> Quad(
                R.drawable.ic_clock,
                R.drawable.bg_status_processing,
                R.string.test_status_processing
            )

            Status.SUCCESS -> Quad(
                R.drawable.ic_successful,
                R.drawable.bg_status_success,
                R.string.test_status_success
            )

            Status.FAILED -> Quad(
                R.drawable.ic_failed,
                R.drawable.bg_status_failed,
                R.string.test_status_failed
            )
        }

        target.icon.setImageResource(iconRes)
        target.chip.text = getString(textRes)
        target.chip.setBackgroundResource(bgRes)
    }

    private data class StatusView(
        val icon: ImageView,
        val chip: TextView,
    )

    private enum class Status { IDLE, PROCESSING, SUCCESS, FAILED }

    private data class Quad<A, B, C>(val a: A, val b: B, val c: C)

    private enum class Phase { Idle, Auth, Token, Verify }

    private fun observeOtp() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                otpViewModel.state.collect { state ->
                    handleViewState(state)
                }
            }
        }
    }

    private fun handleViewState(viewState: OtpViewState) {
        when (viewState) {
            is OtpViewState.Loading -> {
                activity.showLoadingDialog(R.string.loading)
            }

            is OtpViewState.Requested -> {
                activity.hideLoadingDialog()
                showOtpDialog(
                    requireActivity(), onVerifyOtp = { otp ->
                        otpViewModel.verifyOtp(
                            phoneNumber,
                            otp
                        )
                    }, onResendOtp = {
                        otpViewModel.requestOtp(phoneNumber)
                    }
                )
            }

            is OtpViewState.Verified -> {
                activity.hideLoadingDialog()
                showToastSuccess(requireActivity(), description = viewState.message)
            }

            is OtpViewState.PopupError -> {
                activity.hideLoadingDialog()
                CommonDialog.openDialog(childFragmentManager, true, viewState.message)
            }

            else -> {
                activity.hideLoadingDialog()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        oauthCollectJob?.cancel()
        transactionCollectJob?.cancel()
    }
}
