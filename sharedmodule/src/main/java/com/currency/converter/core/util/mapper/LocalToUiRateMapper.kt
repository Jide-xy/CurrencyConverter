package com.currency.converter.core.util.mapper

import com.currency.converter.core.model.CurrencyRate
import com.currency.converter.core.repository.db.model.RateEntity
import com.currency.converter.core.util.Mapper
import javax.inject.Inject

class LocalToUiRateMapper @Inject constructor() : Mapper<RateEntity, CurrencyRate> {
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