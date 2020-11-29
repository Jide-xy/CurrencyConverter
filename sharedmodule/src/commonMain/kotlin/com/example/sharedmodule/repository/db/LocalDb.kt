package com.example.sharedmodule.repository.db

import comexamplesharedmodulerepositorydb.RateEntity
import kotlinx.coroutines.flow.Flow

interface LocalDb {
    fun fetchRates(): Flow<List<RateEntity>>
    fun saveRates(rates: List<RateEntity>)
}