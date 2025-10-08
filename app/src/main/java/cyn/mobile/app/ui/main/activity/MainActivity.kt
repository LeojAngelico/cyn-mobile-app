package cyn.mobile.app.ui.main.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import cyn.mobile.app.R
import cyn.mobile.app.databinding.ActivityMainBinding
import cyn.mobile.app.ui.auth.activity.LoginActivity
import cyn.mobile.app.ui.auth.viewmodel.LoginViewModel
import cyn.mobile.app.ui.auth.viewmodel.LoginViewState
import cyn.mobile.app.ui.main.fragment.HistoryFragment
import cyn.mobile.app.ui.main.fragment.HomeFragment
import cyn.mobile.app.ui.main.fragment.ProfileFragment
import cyn.mobile.app.ui.main.fragment.TestFragment
import cyn.mobile.app.utils.dialog.CommonDialog
import cyn.mobile.app.utils.showPopupError
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var loadingDialog: CommonDialog? = null
    private val viewModel: LoginViewModel by viewModels()

    companion object {
        private const val TAG_HOME = "tag_home"
        private const val TAG_TEST = "tag_test"
        private const val TAG_HISTORY = "tag_history"
        private const val TAG_PROFILE = "tag_profile"
        private const val STATE_SELECTED_ITEM = "state_selected_item"
        fun getIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sysBars.left, sysBars.top, sysBars.right, sysBars.bottom)
            insets
        }

        initBottomNav(savedInstanceState)
        observeLogout()
    }

    private fun observeLogout(){
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
                val intent = LoginActivity.getIntent(this@MainActivity)
                startActivity(intent)
                this@MainActivity.finishAffinity()
                Toast.makeText(this, viewState.message, Toast.LENGTH_SHORT).show()
            }
            is LoginViewState.PopupError -> {
                hideLoadingDialog()
                showPopupError(this@MainActivity, supportFragmentManager, viewState.errorCode, viewState.message)
            }
            else -> Unit
        }
    }

    fun showLoadingDialog(@StringRes strId: Int) {
        if (loadingDialog == null) {
            loadingDialog = CommonDialog.getLoadingDialogInstance(
                message = getString(strId)
            )
        }
        loadingDialog?.show(supportFragmentManager)
    }

    fun hideLoadingDialog() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }

    override fun onDestroy() {
        super.onDestroy()
        hideLoadingDialog()
    }


    private fun initBottomNav(savedInstanceState: Bundle?) = with(binding) {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    showFragment(TAG_HOME) { HomeFragment() }
                    true
                }
                R.id.menu_test -> {
                    showFragment(TAG_TEST) { TestFragment() }
                    true
                }
                R.id.menu_history -> {
                    showFragment(TAG_HISTORY) { HistoryFragment() }
                    true
                }
                R.id.menu_profile -> {
                    showFragment(TAG_PROFILE) { ProfileFragment() }
                    true
                }
                R.id.menu_logout -> {
                    showLogoutConfirmationDialog()
                    true
                }
                else -> false
            }
        }

        val selected = savedInstanceState?.getInt(STATE_SELECTED_ITEM) ?: R.id.menu_home
        bottomNavigation.selectedItemId = selected
    }

    private inline fun showFragment(tag: String, crossinline create: () -> Fragment) {
        val existing = supportFragmentManager.findFragmentByTag(tag)
        supportFragmentManager.beginTransaction().apply {
            setReorderingAllowed(true)
            supportFragmentManager.fragments.forEach { if (it.isAdded) hide(it) }
            if (existing == null) {
                add(R.id.fragmentContainer, create(), tag)
            } else {
                show(existing)
            }
        }.commit()
    }

    // Expose navigation helpers for other screens
    fun navigateToTest() {
        showFragment(TAG_TEST) { TestFragment() }
        binding.bottomNavigation.selectedItemId = R.id.menu_test
    }

    fun navigateToHistory() {
        showFragment(TAG_HISTORY) { HistoryFragment() }
        binding.bottomNavigation.selectedItemId = R.id.menu_history
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { dialog, _ ->
                viewModel.doLogoutAccount()
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_SELECTED_ITEM, binding.bottomNavigation.selectedItemId)
    }
}