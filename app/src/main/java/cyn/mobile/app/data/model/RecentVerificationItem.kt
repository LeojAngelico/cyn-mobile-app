package cyn.mobile.app.data.model

import androidx.annotation.Keep

@Keep
data class RecentVerificationItem(
    var title: String,
    var phoneNumber: String,
    var status: String
)
