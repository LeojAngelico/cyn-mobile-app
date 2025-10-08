package cyn.mobile.app.utils.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import cyn.mobile.app.databinding.CommonDialogBinding

class CommonDialog private constructor(
    private val dismissListener: (() -> Unit)? = null
) : DialogFragment() {

    private var _binding: CommonDialogBinding? = null
    private val binding get() = _binding!!

    private val message by lazy { arguments?.getString(EXTRA_MESSAGE).orEmpty() }
    private val isLoading by lazy { arguments?.getBoolean(EXTRA_IS_LOADING, false) ?: false }
    private val isCancellable by lazy { arguments?.getBoolean(EXTRA_IS_CANCELABLE, false) ?: false }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CommonDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.messageTextView.text = message

        binding.iconImageView.isGone = isLoading
        binding.progressBar.isVisible = isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCancelable(isCancellable)
        dialog.setCanceledOnTouchOutside(isCancellable)
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setGravity(Gravity.CENTER)
        }
        return dialog
    }

    fun show(fragmentManager: FragmentManager) {
        show(fragmentManager, TAG)
    }

    override fun onDismiss(dialog: DialogInterface) {
        dismissListener?.invoke()
        super.onDismiss(dialog)
    }

    companion object {
        private val TAG = CommonDialog::class.java.simpleName
        private const val EXTRA_MESSAGE = "EXTRA_MESSAGE"
        private const val EXTRA_IS_LOADING = "EXTRA_IS_LOADING"
        private const val EXTRA_IS_CANCELABLE = "EXTRA_IS_CANCELABLE"

        fun openDialog(
            fragmentManager: FragmentManager,
            isCancelable: Boolean = false,
            message: String = "",
            dismissListener: (() -> Unit)? = null
        ) {
            CommonDialog(dismissListener).apply {
                arguments = bundleOf(
                    EXTRA_MESSAGE to message,
                    EXTRA_IS_CANCELABLE to isCancelable
                )
            }.show(fragmentManager, TAG)
        }

        fun newDialogInstance(
            isCancelable: Boolean = false,
            message: String = "",
            dismissListener: (() -> Unit)? = null
        ) = CommonDialog(dismissListener).apply {
            arguments = bundleOf(
                EXTRA_MESSAGE to message,
                EXTRA_IS_LOADING to true,
                EXTRA_IS_CANCELABLE to isCancelable
            )
        }

        fun getLoadingDialogInstance(
            isCancelable: Boolean = false,
            message: String = "",
            dismissListener: (() -> Unit)? = null
        ) = CommonDialog(dismissListener).apply {
            arguments = bundleOf(
                EXTRA_MESSAGE to message,
                EXTRA_IS_LOADING to true,
                EXTRA_IS_CANCELABLE to isCancelable
            )
        }
    }
}