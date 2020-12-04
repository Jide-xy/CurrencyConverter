package com.currency.converter.core.repository.network

import com.currency.converter.core.repository.network.model.RateResponse
import com.currency.converter.core.util.DispatcherProvider
import com.currency.converter.core.util.NetworkStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CCApiImpl @Inject constructor(
    private val httpClient: CCService,
    private val dispatcherProvider: DispatcherProvider
) : CCApi {

    val API_KEY = "fef36f32590bfa159328a2ddf302cfc6"

    override fun fetchRates(date: String): Flow<NetworkStatus<RateResponse>> {
        return flow {
            emit(NetworkStatus.Loading())
            emit(fetchRatesNoFlow(date))
        }
    }

    override suspend fun fetchRatesNoFlow(date: String): NetworkStatus<RateResponse> {
        return try {
            withContext(dispatcherProvider.io()) {
                httpClient.fetchRates(date, API_KEY).takeIf { it.isSuccessful }?.let {
                    NetworkStatus.Success(it.body()!!)
                } ?: NetworkStatus.Error<RateResponse>("An error occured")
            }

        } catch (e: Exception) {
            NetworkStatus.Error<RateResponse>(e.message!!)
        }
    }
}