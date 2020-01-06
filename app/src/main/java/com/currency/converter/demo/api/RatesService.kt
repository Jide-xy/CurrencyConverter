package com.currency.converter.demo.api

import io.reactivex.Observable
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RatesService {

    @GET("{date}")
    fun fetchRates(@Path("date") date: String, @Query("access_key") query: String): Call<RatesResult>

    @GET("{date}")
    fun fetchRxRates(@Path("date") date: String, @Query("access_key") query: String): Observable<RatesResult>
}