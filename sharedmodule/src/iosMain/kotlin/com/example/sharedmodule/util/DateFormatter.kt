package com.example.sharedmodule.util

import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale

actual object DateFormatter {

    actual fun String.formatDateString(
        previousFormat: String,
        newFormat: String
    ): String {
        val uiFormatter = NSDateFormatter()
        uiFormatter.locale = NSLocale("en_US")
        uiFormatter.dateFormat = previousFormat
        val date = uiFormatter.dateFromString(this)
        uiFormatter.dateFormat = newFormat
        return uiFormatter.stringFromDate(date!!)
    }

}