package com.example.sharedmodule.util.mapper

import com.example.sharedmodule.model.CurrencyRate
import com.example.sharedmodule.util.Mapper
import comexamplesharedmodulerepositorydb.RateEntity

class LocalToUiRateMapper : Mapper<RateEntity, CurrencyRate> {
    override suspend fun map(from: RateEntity): CurrencyRate {
        return with(from) {
            CurrencyRate(currency_code, rate, base_currency_code)
        }
    }

    override suspend fun mapInverse(from: CurrencyRate): RateEntity {
        return with(from) {
            RateEntity(currencyCode, baseCurrencyCode, rate)
        }
    }
}