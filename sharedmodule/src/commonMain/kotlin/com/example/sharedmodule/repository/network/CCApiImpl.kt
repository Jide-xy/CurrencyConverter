package com.example.sharedmodule.repository.network

import com.example.sharedmodule.repository.network.model.RateResponse
import com.example.sharedmodule.util.DispatcherProvider
import com.example.sharedmodule.util.NetworkStatus
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CCApiImpl(
    private val url: String,
//    private val httpClient: HttpClient,
    private val dispatcherProvider: DispatcherProvider
) : CCApi {

    private val httpClient = HttpClient {
        install(JsonFeature) {
            val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            serializer = KotlinxSerializer(json)
        }
    }

    val URL = "http://data.fixer.io/api/"
    val API_KEY = "fef36f32590bfa159328a2ddf302cfc6"

    override fun fetchRates(date: String): Flow<NetworkStatus<RateResponse>> {
        return flow {
            emit(NetworkStatus.Loading())
            emit(fetchRatesNoFlow(date))
        }
    }

    override suspend fun fetchRatesNoFlow(date: String): NetworkStatus<RateResponse> {
        return try {
//            withContext(dispatcherProvider.io()) {
            val rateResponse: RateResponse = httpClient.get {
                pathBuilder(date, queries = mapOf("access_key" to listOf(API_KEY)))
            }
            NetworkStatus.Success(rateResponse)
//            }

        } catch (e: Exception) {
            NetworkStatus.Error<RateResponse>(e.message!!)
        }
    }

    private fun HttpRequestBuilder.pathBuilder(
        vararg path: String = emptyArray(),
        queries: Map<String, List<String>> = emptyMap()
    ) {
        url {
            this.takeFrom(this@CCApiImpl.url)
            path(path.toList())
            this.parameters.appendAll(valuesOf(queries))
        }
    }
}