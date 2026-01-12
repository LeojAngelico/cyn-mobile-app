package cyn.mobile.app.utils.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.CountDownTimer
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.isVisible
import cyn.mobile.app.R

fun showErrorDialog(
    context: Context,
    message: String,
    showVerifyButton: Boolean = false,
    onVerifyPhone: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null
) {
    val dialog = Dialog(context)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    val view = LayoutInflater.from(context).inflate(R.layout.dialog_error_message, null, false)
    dialog.setContentView(view)

    dialog.setCancelable(true)
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    val textMessage = view.findViewById<TextView>(R.id.textMessage)
    val btnVerify = view.findViewById<Button>(R.id.btnVerifyPhone)
    val btnClose = view.findViewById<Button>(R.id.btnClose)

    textMessage.text = message
    btnVerify.isVisible = showVerifyButton

    btnVerify.setOnClickListener {
        onVerifyPhone?.invoke()
        dialog.dismiss()
    }
    btnClose.setOnClickListener {
        dialog.dismiss()
    }

    dialog.setOnDismissListener { onDismiss?.invoke() }

    dialog.show()
    dialog.window?.setLayout(
        (context.resources.displayMetrics.widthPixels * 0.90f).toInt(),
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
}

fun showOtpDialog(
    context: Context,
    title: String = "Enter OTP",
    message: String = "We sent a 6-digit code to your phone.",
    otpLength: Int = 6,
    resendCooldownSeconds: Int = 120,
    onVerifyOtp: (otp: String) -> Unit,
    onResendOtp: () -> Unit,
    onCancel: (() -> Unit)? = null
) {
    val dialog = Dialog(context)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    val view = LayoutInflater.from(context).inflate(R.layout.dialog_otp, null, false)
    dialog.setContentView(view)

    dialog.setCancelable(true)
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    val textTitle = view.findViewById<TextView>(R.id.textOtpTitle)
    val textHint = view.findViewById<TextView>(R.id.textOtpHint)
    val inputOtp = view.findViewById<EditText>(R.id.inputOtp)
    val textError = view.findViewById<TextView>(R.id.textOtpError)
    val btnCancel = view.findViewById<Button>(R.id.btnCancelOtp)
    val btnVerify = view.findViewById<Button>(R.id.btnVerifyOtp)
    val textTimer = view.findViewById<TextView>(R.id.textOtpTimer)
    val btnResend = view.findViewById<Button>(R.id.btnResendOtp)

    textTitle.text = title
    textHint.text = message

    inputOtp.filters = arrayOf(InputFilter.LengthFilter(otpLength))

    var countDownTimer: CountDownTimer? = null

    fun formatSeconds(sec: Int): String {
        val m = sec / 60
        val s = sec % 60
        return String.format("%02d:%02d", m, s)
    }

    fun startResendCountdown() {
        btnResend.isEnabled = false
        textTimer.text = formatSeconds(resendCooldownSeconds)

        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(resendCooldownSeconds * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = (millisUntilFinished / 1000L).toInt()
                textTimer.text = formatSeconds(secondsLeft)
            }

            override fun onFinish() {
                textTimer.text = "00:00"
                btnResend.isEnabled = true
            }
        }.start()
    }

    fun validateAndSubmit() {
        val otp = inputOtp.text?.toString()?.trim().orEmpty()
        if (otp.length != otpLength) {
            textError.text = "Please enter a $otpLength-digit code."
            textError.isVisible = true
            return
        }
        textError.isVisible = false
        onVerifyOtp(otp)
        countDownTimer?.cancel()
        dialog.dismiss()
    }

    btnVerify.setOnClickListener { validateAndSubmit() }

    btnCancel.setOnClickListener {
        dialog.dismiss()
        onCancel?.invoke()
    }

    btnResend.setOnClickListener {
        // Restart timer, clear current input, hide error and trigger resend action
        startResendCountdown()
        inputOtp.text?.clear()
        textError.isVisible = false
        onResendOtp()
    }

    inputOtp.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            textError.isVisible = false
        }
        override fun afterTextChanged(s: Editable?) {}
    })

    // Ensure timer is cancelled if the dialog is dismissed externally
    dialog.setOnDismissListener {
        countDownTimer?.cancel()
        countDownTimer = null
    }

    dialog.show()
    dialog.window?.setLayout(
        (context.resources.displayMetrics.widthPixels * 0.90f).toInt(),
        ViewGroup.LayoutParams.WRAP_CONTENT
    )

    // Start the initial countdown
    startResendCountdown()
}

fun showPhoneNumberDialog(
    context: Context,
    prefillDigitsAfter639: String? = null, // optional: 9 digits after +639
    onConfirm: (phoneE164: String) -> Unit,
    onCancel: (() -> Unit)? = null
) {
    val dialog = Dialog(context)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    val view = LayoutInflater.from(context).inflate(R.layout.dialog_phone_number, null, false)
    dialog.setContentView(view)

    dialog.setCancelable(true)
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    val inputDigits = view.findViewById<EditText>(R.id.inputPhoneDigits)
    val textError = view.findViewById<TextView>(R.id.textPhoneError)
    val btnCancel = view.findViewById<Button>(R.id.btnCancelPhone)
    val btnConfirm = view.findViewById<Button>(R.id.btnConfirmPhone)

    // Allow only digits and cap to 9
    inputDigits.keyListener = DigitsKeyListener.getInstance("0123456789")
    inputDigits.filters = arrayOf(InputFilter.LengthFilter(9))

    // Prefill if provided (sanitized to digits and trimmed to 9)
    prefillDigitsAfter639?.let {
        val sanitized = it.filter(Char::isDigit).take(9)
        inputDigits.setText(sanitized)
        inputDigits.setSelection(sanitized.length)
        btnConfirm.isEnabled = sanitized.length == 9
    }

    fun validate(): Boolean {
        val digits = inputDigits.text?.toString().orEmpty()
        val isValid = digits.length == 9
        textError.isVisible = !isValid
        if (!isValid) {
            textError.text = "Enter 9 digits after +639."
        }
        return isValid
    }

    fun submit() {
        if (!validate()) return
        val full = "+639" + inputDigits.text.toString()
        onConfirm(full)
        dialog.dismiss()
    }

    inputDigits.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            textError.isVisible = false
            btnConfirm.isEnabled = (inputDigits.text?.length == 9)
        }
        override fun afterTextChanged(s: Editable?) {}
    })

    btnConfirm.setOnClickListener { submit() }
    btnCancel.setOnClickListener {
        dialog.dismiss()
        onCancel?.invoke()
    }

    dialog.show()
    dialog.window?.setLayout(
        (context.resources.displayMetrics.widthPixels * 0.90f).toInt(),
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
}
