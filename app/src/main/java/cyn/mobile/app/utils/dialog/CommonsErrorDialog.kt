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
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import cyn.mobile.app.R
import cyn.mobile.app.databinding.CommonsDialogErrorBinding

class CommonsErrorDialog private constructor(
    private val dismissListener: (() -> Unit)? = null
) : DialogFragment() {

    private var binding: CommonsDialogErrorBinding? = null
    private val message by lazy { arguments?.getString(EXTRA_MESSAGE).orEmpty() }
    private val title by lazy { arguments?.getString(EXTRA_TITLE).orEmpty() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = CommonsDialogErrorBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            titleTextView.isGone = title.isEmpty()
            titleTextView.text = title

            messageTextView.text = message.ifEmpty {
                getString(R.string.commons_error_message)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
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
        private val TAG = CommonsErrorDialog::class.java.simpleName
        private const val EXTRA_MESSAGE = "EXTRA_MESSAGE"
        private const val EXTRA_TITLE = "EXTRA_TITLE"


        fun openDialog(
            fragmentManager: FragmentManager,
            title: String = "",
            message: String = "",
            dismissListener: (() -> Unit)? = null
        ) {
            getLoadingDialogInstance(title, message, dismissListener)
                .show(fragmentManager, TAG)
        }

        fun getLoadingDialogInstance(
            title: String = "",
            message: String = "",
            dismissListener: (() -> Unit)? = null
        ) = CommonsErrorDialog(dismissListener).apply {
            arguments = bundleOf(
                EXTRA_TITLE to title,
                EXTRA_MESSAGE to message
            )
        }
    }
}