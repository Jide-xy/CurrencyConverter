package com.example.sharedmodule.util

expect object DateFormatter {

    fun String.formatDateString(previousFormat: String, newFormat: String): String
}