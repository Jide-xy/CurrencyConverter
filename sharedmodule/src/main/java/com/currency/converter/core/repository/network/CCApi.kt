package com.currency.converter.core.repository.network

import com.currency.converter.core.repository.network.model.RateResponse
import com.currency.converter.core.util.NetworkStatus
import kotlinx.coroutines.flow.Flow

interface CCApi {

    fun fetchRates(date: String): Flow<NetworkStatus<RateResponse>>
    suspend fun fetchRatesNoFlow(date: String): NetworkStatus<RateResponse>
}