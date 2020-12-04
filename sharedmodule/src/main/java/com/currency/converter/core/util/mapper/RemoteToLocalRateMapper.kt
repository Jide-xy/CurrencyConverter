package com.currency.converter.core.util.mapper

import com.currency.converter.core.repository.db.model.RateEntity
import com.currency.converter.core.repository.network.model.RateResponse
import com.currency.converter.core.util.Mapper
import javax.inject.Inject

class RemoteToLocalRateMapper @Inject constructor() : Mapper<RateResponse, List<RateEntity>> {
    override suspend fun map(from: RateResponse): List<RateEntity> {
        return with(from) {
            rates.map {
                RateEntity(it.key, base, it.value)
            }
        }
    }

    override suspend fun mapInverse(from: List<RateEntity>): RateResponse {
        return with(from) {
            RateResponse(
                false,
                "",
                0L,
                firstOrNull()?.base_currency_code.orEmpty(),
                false,
                this.associate { it.currency_code to it.rate }
            )
        }
    }
}