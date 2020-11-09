package com.example.sharedmodule.repository

data class CurrencyRate(
    val currencyCode: String,
    val rate: Double,
    val baseCurrencyCode: String,
    val date: String = "latest"
)