package cyn.mobile.app.data.repositories.base.response

import androidx.annotation.Keep

@Keep
data class DateBaseResponse(
    val date_db: String? = null,
    val date_only: String? = null,
    val date_only_ph: String? = null,
    val datetime_ph: String? = null,
    val iso_format: String? = null,
    val month_year: String? = null,
    val time_passed: String? = null,
    val timestamp: String? = null
)
