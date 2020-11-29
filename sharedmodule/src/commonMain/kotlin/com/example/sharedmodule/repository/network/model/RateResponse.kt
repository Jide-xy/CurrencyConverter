package com.example.sharedmodule.repository.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RateResponse(
    @SerialName("success")
    val success: Boolean,
    @SerialName("date")
    val date: String,
    @SerialName("timestamp")
    val timestamp: Long,
    @SerialName("base")
    val base: String,
    @SerialName("historical")
    val historical: Boolean = false,
    @SerialName("rates")
    val rates: Map<String, Double>
)