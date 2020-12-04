package com.currency.converter.core.util

import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object DateFormatter {

    fun String.formatDateString(
        previousFormat: String,
        newFormat: String
    ): String {
        return DateFormat.format(
            newFormat,
            SimpleDateFormat(previousFormat, Locale.getDefault()).parse(this)
        ).toString()
    }
}