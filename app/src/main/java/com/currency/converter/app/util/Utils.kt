package com.currency.converter.app.util

import java.util.*

object Utils {
    fun getCurrencyCodeToCountryCodeMap(): MutableMap<String, String>{
        val countriesMap: MutableMap<String, String> = mutableMapOf()
        Locale.getISOCountries().forEach {
            try {
                countriesMap[Currency.getInstance(Locale("", it)).currencyCode] = it
            } catch (e: Exception) {
                when (e) {
                    is IllegalStateException, is NullPointerException -> e.printStackTrace()
                    else -> throw e
                }
            }
        }
        return countriesMap
    }
}