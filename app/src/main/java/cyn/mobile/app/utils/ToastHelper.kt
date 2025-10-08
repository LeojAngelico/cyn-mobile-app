package cyn.mobile.app.utils

import android.app.Activity
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.marcoscg.materialtoast.MaterialToast
import cyn.mobile.app.R

/**
 * Show toast info
 *
 * @param context
 * @param title Title of toast, default to info
 * @param description message
 */
fun showToastInfo(context: Activity, title : String = "Info", description : String){
    MaterialToast(context)
        .setMessage(description)
        .setDuration(Toast.LENGTH_LONG)
        .setBackgroundColor(ResourcesCompat.getColor(context.resources, R.color.ongoing, null))
        .show()
}

/**
 * Show toast success
 *
 * @param context
 * @param title Title of toast, default to Success
 * @param description message
 */
fun showToastSuccess(context: Activity, title : String = "Success", description : String){
    MaterialToast(context)
        .setMessage(description)
        .setDuration(Toast.LENGTH_LONG)
        .setBackgroundColor(ResourcesCompat.getColor(context.resources, R.color.color_light_green, null))
        .show()
}

/**
 * Show toast warning
 *
 * @param context
 * @param title Title of toast, default to Warning
 * @param description message
 */
fun showToastWarning(context: Activity, title : String = "Warning", description : String){
    MaterialToast(context)
        .setMessage(description)
        .setDuration(Toast.LENGTH_LONG)
        .setBackgroundColor(ResourcesCompat.getColor(context.resources, R.color.pending, null))
        .show()
}

/**
 * Show toast error
 *
 * @param context
 * @param title Title of toast, default to Error
 * @param description message
 */
fun showToastError(context: Activity, title : String = "Error", description : String){
    MaterialToast(context)
        .setMessage(description)
        .setDuration(Toast.LENGTH_LONG)
        .setBackgroundColor(ResourcesCompat.getColor(context.resources, R.color.color_light_red, null))
        .show()
}