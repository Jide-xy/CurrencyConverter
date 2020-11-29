package com.example.sharedmodule.util

import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.*

actual object DateFormatter {

    actual fun String.formatDateString(
        previousFormat: String,
        newFormat: String
    ): String {
        return DateFormat.format(
            newFormat,
            SimpleDateFormat(previousFormat, Locale.getDefault()).parse(this)
        ).toString()
    }


}