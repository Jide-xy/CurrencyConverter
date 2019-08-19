package com.currency.converter.demo.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RatesService {

    @GET("latest")
    fun searchRepos(@Query("access_key") query: String): Call<RatesResult>
}