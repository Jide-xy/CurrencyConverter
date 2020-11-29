package com.example.sharedmodule.util.mapper

import com.example.sharedmodule.repository.network.model.RateResponse
import com.example.sharedmodule.util.Mapper
import comexamplesharedmodulerepositorydb.RateEntity

class RemoteToLocalRateMapper : Mapper<RateResponse, List<RateEntity>> {
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