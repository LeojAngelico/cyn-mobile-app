package cyn.mobile.app.utils

import android.os.SystemClock
import android.view.View

abstract class OnSingleClickListener : View.OnClickListener {

    companion object {
        const val MIN_CLICK_INTERVAL = 600
    }

    private var lastClickTime = 0L

    override fun onClick(view: View) {
        if (SystemClock.elapsedRealtime() - lastClickTime < MIN_CLICK_INTERVAL) {
            return
        }
        lastClickTime = SystemClock.elapsedRealtime()

        onSingleClick(view)
    }

    abstract fun onSingleClick(view: View)
}

fun View.setOnSingleClickListener(block: () -> Unit) {
    setOnClickListener(object : OnSingleClickListener() {
        override fun onSingleClick(view: View) {
            block()
        }
    })
}