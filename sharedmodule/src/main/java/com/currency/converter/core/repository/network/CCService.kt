package com.currency.converter.core.repository.network

import com.currency.converter.core.repository.network.model.RateResponse

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CCService {

    @GET("{date}")
    suspend fun fetchRates(
        @Path("date") date: String = "latest",
        @Query("access_key") apiKey: String
    ): Response<RateResponse>
}
