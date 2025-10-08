package cyn.mobile.app.ui.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import cyn.mobile.app.R
import cyn.mobile.app.data.model.ErrorsData
import cyn.mobile.app.data.repositories.auth.response.UserData
import cyn.mobile.app.data.repositories.profile.request.UpdateProfileRequest
import cyn.mobile.app.databinding.FragmentProfileBinding
import cyn.mobile.app.ui.main.activity.MainActivity
import cyn.mobile.app.ui.main.viewmodel.ProfileViewModel
import cyn.mobile.app.ui.main.viewmodel.ProfileViewState
import cyn.mobile.app.utils.dialog.CommonsErrorDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import cyn.mobile.app.security.AuthStorage

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()
    private val activity get() = requireActivity() as MainActivity
    @Inject lateinit var authStorage: AuthStorage

    // Keep original values to allow cancel
    private var originalFirstName: String = ""
    private var originalMiddleName: String = ""
    private var originalLastName: String = ""
    private var originalAddress: String = ""
    private var originalUsername: String = ""
    private var originalEmail: String = ""
    private var originalPhone: String = ""

    private var isEditing: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeProfile()

        setEditing(false)

        // Header actions
        binding.editImageButton.setOnClickListener {
            setEditing(true)
        }
        binding.saveImageButton.setOnClickListener {
            val first = binding.firstNameEditText.text?.toString().orEmpty().trim()
            val middle = binding.middleNameEditText.text?.toString().orEmpty().trim()
            val last = binding.lastNameEditText.text?.toString().orEmpty().trim()
            val username = binding.usernameEditText.text?.toString().orEmpty().trim()
            val email = binding.emailEditText.text?.toString().orEmpty().trim()
            val phone = binding.phoneEditText.text?.toString().orEmpty().trim()
            val address = binding.addressEditText.text?.toString().orEmpty().trim()

            // Build request and trigger update
            val request = UpdateProfileRequest(
                firstname = first,
                middlename = middle,
                lastname = last,
                username = username,
                email = email,
                contact_number = phone,
                address = address
            )
            viewModel.doUpdateProfile(request)
        }
        binding.closeImageButton.setOnClickListener {
            // Revert to original from cached originals (sourced from AuthStorage)
            restoreOriginalValues()
            setEditing(false)
        }

        // Optional actions
        binding.changePasswordButton.setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.profile_change_password_click), Toast.LENGTH_SHORT).show()
        }
        binding.apiSettingsButton.setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.profile_api_settings_click), Toast.LENGTH_SHORT).show()
        }
        binding.deleteAccountButton.setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.profile_delete_click), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        viewLifecycleOwner.lifecycleScope.launch {
            val data = authStorage.getUserBasicInfo()
            setupView(data)
        }
    }

    private fun observeProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state -> handleViewState(state) }
            }
        }
    }

    private fun handleViewState(viewState: ProfileViewState) {
        when (viewState) {
            is ProfileViewState.Loading -> {
                activity.showLoadingDialog(R.string.loading)
            }
            is ProfileViewState.ProfileLoaded -> {
                val data = viewState.data
                // Map safely from your UserData model
                setupView(data?: UserData())

                activity.hideLoadingDialog()
            }
            is ProfileViewState.ProfileUpdated -> {
                activity.hideLoadingDialog()
                Toast.makeText(requireContext(), viewState.message ?: getString(R.string.profile_saved_toast), Toast.LENGTH_SHORT).show()
                setEditing(false)
                viewModel.getProfileInfo()
            }
            is ProfileViewState.PopupError -> {
                activity.hideLoadingDialog()
                CommonsErrorDialog.openDialog(childFragmentManager, message = viewState.message)
            }
            is ProfileViewState.InputError -> {
                activity.hideLoadingDialog()
                handleInputError(viewState.errorData?: ErrorsData())
            }
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
    }

    private fun setEditing(edit: Boolean) {
        isEditing = edit
        // Toggle fields
        binding.firstNameEditText.isEnabled = edit
        binding.middleNameEditText.isEnabled = edit
        binding.lastNameEditText.isEnabled = edit
        binding.addressEditText.isEnabled = edit
        binding.usernameEditText.isEnabled = edit
        binding.emailEditText.isEnabled = edit
        binding.phoneEditText.isEnabled = edit

        // Toggle header actions visibility
        binding.editImageButton.isVisible = !edit
        binding.saveImageButton.isVisible = edit
        binding.closeImageButton.isVisible = edit
    }

    // Load the "original" values directly from AuthStorage (persistent source of truth)
    private fun setupView(data: UserData) {
        val first = data.firstname.orEmpty()
        val middle = data.middlename.orEmpty()
        val last = data.lastname.orEmpty()
        val username = data.username.orEmpty()
        val email = data.email.orEmpty()
        val phone = data.contact_number.orEmpty()
        val address = data.address.orEmpty()

        binding.firstNameEditText.setText(first)
        binding.middleNameEditText.setText(middle)
        binding.lastNameEditText.setText(last)
        binding.usernameEditText.setText(username)
        binding.emailEditText.setText(email)
        binding.phoneEditText.setText(phone)
        binding.addressEditText.setText(address)

        // Update header display (initials + full display name + handle)
        val initials = buildString {
            if (first.isNotBlank()) append(first.first().uppercase())
            if (last.isNotBlank()) append(last.first().uppercase())
        }.ifBlank { "NA" }
        binding.initialsTextView.text = initials

        val displayName = listOf(first, middle, last).filter { it.isNotBlank() }.joinToString(" ")
        binding.displayNameTextView.text = displayName.ifBlank { getString(R.string.profile_title) }

        binding.handleTextView.text = if (username.isNotBlank()) "@$username" else getString(R.string.profile_username_handle_sample)
    }

    private fun restoreOriginalValues() {
        binding.firstNameEditText.setText(originalFirstName)
        binding.middleNameEditText.setText(originalMiddleName)
        binding.lastNameEditText.setText(originalLastName)
        binding.addressEditText.setText(originalAddress)
        binding.usernameEditText.setText(originalUsername)
        binding.emailEditText.setText(originalEmail)
        binding.phoneEditText.setText(originalPhone)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
