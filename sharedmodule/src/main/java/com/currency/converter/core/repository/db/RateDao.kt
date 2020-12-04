package com.currency.converter.core.repository.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.currency.converter.core.repository.db.model.RateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RateDao {

    @Query("Select * from RateEntity")
    fun fetchRates(): Flow<List<RateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRates(rates: List<RateEntity>)
}