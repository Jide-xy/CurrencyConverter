package com.currency.converter.core.repository

import com.currency.converter.core.model.CurrencyRate
import com.currency.converter.core.util.NetworkStatus
import kotlinx.coroutines.flow.Flow

interface Repository {
    fun fetchRates(date: String = "latest"): Flow<NetworkStatus<List<CurrencyRate>>>
    fun getHistoricalRates(numberOfDays: Int): Flow<NetworkStatus<List<Map<String, List<CurrencyRate>>>>>
}