package com.example.sharedmodule.repository.db

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import comexamplesharedmodulerepositorydb.RateEntity
import kotlinx.coroutines.flow.Flow

class LocalDbImpl(
    private val appDatabase: AppDatabase
) : LocalDb {
    override fun fetchRates(): Flow<List<RateEntity>> {
        return appDatabase.appDatabaseQueries.getAllRates().asFlow().mapToList()
    }

    override fun saveRates(rates: List<RateEntity>) {
        appDatabase.appDatabaseQueries.transaction {
            rates.forEach {
                appDatabase.appDatabaseQueries.insertRate(it)
            }
        }
    }
}