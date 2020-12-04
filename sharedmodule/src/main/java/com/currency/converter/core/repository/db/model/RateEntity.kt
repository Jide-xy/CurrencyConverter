package com.currency.converter.core.repository.db.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RateEntity(
    @PrimaryKey
    val currency_code: String,
    val base_currency_code: String,
    val rate: Double
)
