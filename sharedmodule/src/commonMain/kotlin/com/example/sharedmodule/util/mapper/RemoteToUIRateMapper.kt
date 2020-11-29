package com.example.sharedmodule.util.mapper

import com.example.sharedmodule.model.CurrencyRate
import com.example.sharedmodule.repository.network.model.RateResponse
import com.example.sharedmodule.util.Mapper

class RemoteToUIRateMapper : Mapper<RateResponse, List<CurrencyRate>> {
    override suspend fun map(from: RateResponse): List<CurrencyRate> {
        return with(from) {
            rates.map {
                CurrencyRate(it.key, it.value, base)
            }
        }
    }

    override suspend fun mapInverse(from: List<CurrencyRate>): RateResponse {
        return with(from) {
            RateResponse(
                false,
                "",
                0L,
                firstOrNull()?.baseCurrencyCode.orEmpty(),
                false,
                this.associate { it.currencyCode to it.rate }
            )
        }
    }
}