package com.example.sharedmodule.repository.network

import com.example.sharedmodule.repository.network.model.RateResponse
import com.example.sharedmodule.util.NetworkStatus
import kotlinx.coroutines.flow.Flow

interface CCApi {

    fun fetchRates(date: String): Flow<NetworkStatus<RateResponse>>
    suspend fun fetchRatesNoFlow(date: String): NetworkStatus<RateResponse>
}