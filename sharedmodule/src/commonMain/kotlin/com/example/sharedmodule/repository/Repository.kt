package com.example.sharedmodule.repository

import com.example.sharedmodule.model.CurrencyRate
import com.example.sharedmodule.util.NetworkStatus
import kotlinx.coroutines.flow.Flow

interface Repository {
    fun fetchRates(date: String = "latest"): Flow<NetworkStatus<List<CurrencyRate>>>
    fun getHistoricalRates(numberOfDays: Int): Flow<NetworkStatus<List<Map<String, List<CurrencyRate>>>>>
}