package cyn.mobile.app.ui.auth.activity

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import cyn.mobile.app.databinding.ActivityLoginBinding
import android.content.Intent
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import cyn.mobile.app.R
import cyn.mobile.app.data.model.ErrorsData
import cyn.mobile.app.ui.auth.viewmodel.LoginViewModel
import cyn.mobile.app.ui.auth.viewmodel.LoginViewState
import cyn.mobile.app.ui.main.activity.MainActivity
import cyn.mobile.app.utils.dialog.CommonDialog
import cyn.mobile.app.utils.setOnSingleClickListener
import cyn.mobile.app.utils.showPopupError
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var loadingDialog: CommonDialog? = null
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // keeps layout under system bars nicely
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUi()
        observeLogin()
    }

    private fun observeLogin(){
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
                val intent = MainActivity.getIntent(this@LoginActivity)
                startActivity(intent)
                this@LoginActivity.finishAffinity()
                Toast.makeText(this, viewState.message, Toast.LENGTH_SHORT).show()
            }
            is LoginViewState.PopupError -> {
                hideLoadingDialog()
                showPopupError(this@LoginActivity, supportFragmentManager, viewState.errorCode, viewState.message)
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
        if (errorsData.password?.get(0)?.isNotEmpty() == true) binding.passwordEditText.error = errorsData.password?.get(0)
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

    private fun initUi() = with(binding) {
        passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                signInMaterialButton.performClick()
                true
            } else {
                false
            }
        }

        signInMaterialButton.setOnSingleClickListener {
            val username = usernameEditText.text?.toString()?.trim().orEmpty()
            val password = passwordEditText.text?.toString().orEmpty()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    this@LoginActivity,
                    "Please enter username and password",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnSingleClickListener
            }else{
                viewModel.doLoginAccount(username, password)
            }
        }

        signUpTextView.setOnSingleClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        }
    }
    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, LoginActivity::class.java)
        }
    }
}
