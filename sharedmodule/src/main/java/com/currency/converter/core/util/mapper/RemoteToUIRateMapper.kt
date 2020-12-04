package com.currency.converter.core.util.mapper

import com.currency.converter.core.model.CurrencyRate
import com.currency.converter.core.repository.network.model.RateResponse
import com.currency.converter.core.util.Mapper
import javax.inject.Inject

class RemoteToUIRateMapper @Inject constructor() : Mapper<RateResponse, List<CurrencyRate>> {
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