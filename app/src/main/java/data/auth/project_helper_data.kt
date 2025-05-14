package data.auth

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.PropertyName
import java.util.Date

data class UserData(
    var username: String? = null,
    var email: String? = null,
    var phone: String? = null,
    var avatarUrl: String? = null,

    @get:PropertyName("registration_date")
    @set:PropertyName("registration_date")
    var registrationDate: Date? = null
) {
    constructor() : this(null, null, null, null, null)
}

fun Date?.formatToString(): String {
    if (this == null) return "Дата недоступна"
    val day = this.date
    val month = this.month + 1
    val year = this.year + 1900
    return String.format("%02d.%02d.%04d", day, month, year)
}