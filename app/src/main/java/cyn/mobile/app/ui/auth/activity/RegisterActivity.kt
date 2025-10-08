package cyn.mobile.app.ui.auth.activity

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import cyn.mobile.app.databinding.ActivityRegisterBinding
import android.util.Patterns
import android.view.KeyEvent
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import cyn.mobile.app.R
import cyn.mobile.app.data.model.ErrorsData
import cyn.mobile.app.data.repositories.auth.request.RegisterRequest
import cyn.mobile.app.ui.auth.viewmodel.LoginViewModel
import cyn.mobile.app.ui.auth.viewmodel.LoginViewState
import cyn.mobile.app.utils.dialog.CommonDialog
import cyn.mobile.app.utils.setOnSingleClickListener
import cyn.mobile.app.utils.showPopupError
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private var loadingDialog: CommonDialog? = null
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        observeRegister()
        initUi()
    }

    private fun observeRegister(){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.loginSharedFlow.collect{
                    handleViewState(it)
                }
            }
        }
    }

    private fun handleViewState(viewState: LoginViewState){
        when(viewState){
            is LoginViewState.Loading -> showLoadingDialog(R.string.loading)
            is LoginViewState.Success -> {
                hideLoadingDialog()
                val intent = LoginActivity.getIntent(this@RegisterActivity)
                startActivity(intent)
                this@RegisterActivity.finishAffinity()
                Toast.makeText(this, viewState.message, Toast.LENGTH_SHORT).show()
            }
            is LoginViewState.PopupError -> {
                hideLoadingDialog()
                showPopupError(this@RegisterActivity, supportFragmentManager, viewState.errorCode, viewState.message)
            }
            is LoginViewState.InputError -> {
                hideLoadingDialog()
                handleInputError(viewState.errorData?: ErrorsData())
            }
            else -> Unit
        }
    }

    private fun handleInputError(errorsData: ErrorsData){
        if (errorsData.username?.get(0)?.isNotEmpty() == true) binding.usernameEditText.error = errorsData.username?.get(0)
        if (errorsData.firstname?.get(0)?.isNotEmpty() == true) binding.firstNameEditText.error = errorsData.firstname?.get(0)
        if (errorsData.middlename?.get(0)?.isNotEmpty() == true) binding.middleNameEditText.error = errorsData.middlename?.get(0)
        if (errorsData.lastname?.get(0)?.isNotEmpty() == true) binding.lastNameEditText.error = errorsData.lastname?.get(0)
        if (errorsData.email?.get(0)?.isNotEmpty() == true) binding.emailEditText.error = errorsData.email?.get(0)
        if (errorsData.contact_number?.get(0)?.isNotEmpty() == true) binding.phoneEditText.error = errorsData.contact_number?.get(0)
        if (errorsData.address?.get(0)?.isNotEmpty() == true) binding.addressEditText.error = errorsData.address?.get(0)
        if (errorsData.password?.get(0)?.isNotEmpty() == true) binding.passwordEditText.error = errorsData.password?.get(0)
        if (errorsData.password_confirmation?.get(0)?.isNotEmpty() == true) binding.confirmPasswordEditText.error = errorsData.password_confirmation?.get(0)
    }

    private fun showLoadingDialog(@StringRes strId: Int) {
        if (loadingDialog == null) {
            loadingDialog = CommonDialog.getLoadingDialogInstance(
                message = getString(strId)
            )
        }
        loadingDialog?.show(supportFragmentManager)
    }

    private fun hideLoadingDialog() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }

    override fun onDestroy() {
        super.onDestroy()
        hideLoadingDialog()
    }

    // Add this inside RegisterActivity

    private fun setupPhoneField() = with(binding.phoneEditText) {
        val prefix = "+63"
        // "+63" + 10 digits = 13 chars
        filters = arrayOf(InputFilter.LengthFilter(13))
        inputType = InputType.TYPE_CLASS_PHONE

        // Pre-fill prefix on focus
        setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && (text?.isEmpty() != false)) {
                setText(prefix)
                setSelection(prefix.length)
            }
        }

        // Prevent deleting the prefix
        setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL &&
                event.action == KeyEvent.ACTION_DOWN &&
                selectionStart <= prefix.length
            ) {
                return@setOnKeyListener true
            }
            false
        }

        addTextChangedListener(object : TextWatcher {
            private var selfChange = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (selfChange) return
                selfChange = true

                val current = s?.toString().orEmpty()
                val raw = current.filter { it.isDigit() || it == '+' }

                // Normalize common forms: "0917...", "63917...", "+63917...", "917..."
                val normalized = when {
                    raw.startsWith("+63") -> "+63" + raw.removePrefix("+63").filter { it.isDigit() }
                    raw.startsWith("63") -> "+63" + raw.removePrefix("63").filter { it.isDigit() }
                    raw.startsWith("0") -> "+63" + raw.drop(1).filter { it.isDigit() }
                    raw.startsWith("+") -> "+63" + raw.drop(1).filter { it.isDigit() }
                    raw.isEmpty() -> prefix
                    else -> "+63" + raw.filter { it.isDigit() }
                }

                // Keep at most 10 digits after +63
                val after = normalized.removePrefix("+63").take(10)
                val enforced = prefix + after

                if (enforced != current) {
                    setText(enforced)
                    setSelection(enforced.length)
                }

                selfChange = false
            }
        })
    }

    // Get the normalized value when saving (returns null if not 10 digits after +63)
    private fun normalizedPhNumber(): String? {
        val raw = binding.phoneEditText.text?.toString().orEmpty().replace("\\s".toRegex(), "")
        return if (raw.startsWith("+63") && raw.length == 13) raw else null
    }

    // Call this once (e.g., from onCreate() or your initUi())
    private fun initUi() = /* your existing code */ run {
        setupPhoneField()
        with(binding) {
            confirmPasswordEditText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    createAccountMaterialButton.performClick()
                    true
                } else {
                    false
                }
            }

            createAccountMaterialButton.setOnSingleClickListener {
                val firstName = firstNameEditText.text?.toString()?.trim().orEmpty()
                val lastName = lastNameEditText.text?.toString()?.trim().orEmpty()
                val middleName = middleNameEditText.text?.toString()?.trim().orEmpty()
                val username = usernameEditText.text?.toString()?.trim().orEmpty()
                val email = emailEditText.text?.toString()?.trim().orEmpty()
                val phone = normalizedPhNumber() ?: return@setOnSingleClickListener
                val address = addressEditText.text?.toString()?.trim().orEmpty()
                val password = passwordEditText.text?.toString().orEmpty()
                val confirmPassword = confirmPasswordEditText.text?.toString().orEmpty()

                if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() ||
                    email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()
                ) {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Please fill in all fields",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnSingleClickListener
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Please enter a valid email address",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnSingleClickListener
                }

                if (password.length < 6) {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Password must be at least 6 characters",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnSingleClickListener
                }

                if (password != confirmPassword) {
                    Toast.makeText(this@RegisterActivity, "Passwords do not match", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnSingleClickListener
                }

                viewModel.doRegisterAccount(RegisterRequest(
                    firstname = firstName,
                    middlename = middleName,
                    lastname = lastName,
                    username = username,
                    email = email,
                    contact_number = phone,
                    address = address,
                    password = password,
                    password_confirmation = confirmPassword
                ))

            }

            backToLoginTextView.setOnSingleClickListener {
                finish() // simply go back to Login screen
            }
        }
    }
}