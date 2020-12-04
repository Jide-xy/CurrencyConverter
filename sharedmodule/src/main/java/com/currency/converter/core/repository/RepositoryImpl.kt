package com.currency.converter.core.repository

import com.currency.converter.core.model.CurrencyRate
import com.currency.converter.core.repository.db.RateDao
import com.currency.converter.core.repository.db.model.RateEntity
import com.currency.converter.core.repository.network.CCApi
import com.currency.converter.core.repository.network.model.RateResponse
import com.currency.converter.core.util.DateFormatter.formatDateString
import com.currency.converter.core.util.DispatcherProvider
import com.currency.converter.core.util.Mapper
import com.currency.converter.core.util.NetworkStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.*
import kotlinx.datetime.*
import javax.inject.Inject

const val DATE_FORMAT = "yyyy-MM-dd"

@ExperimentalCoroutinesApi
class RepositoryImpl @Inject constructor(
    private val ccApi: CCApi,
    private val localDb: RateDao,
    private val localToUIMapper: Mapper<RateEntity, CurrencyRate>,
    private val remoteToLocalMapper: Mapper<RateResponse, List<RateEntity>>,
    private val remoteToUIMapper: Mapper<RateResponse, List<CurrencyRate>>,
    private val dispatcherProvider: DispatcherProvider
) : Repository {

    override fun fetchRates(date: String): Flow<NetworkStatus<List<CurrencyRate>>> =
        ccApi.fetchRates(date).filterNot {
            it is NetworkStatus.Loading
        }
            .flatMapLatest { response ->
                return@flatMapLatest when (response) {
                    is NetworkStatus.Success -> {
                        localDb.saveRates(remoteToLocalMapper.map(response.data))
                        localDb.fetchRates()
                            .map { NetworkStatus.Success(localToUIMapper.mapList(it)) }
                    }
                    is NetworkStatus.Loading -> emptyFlow()
                    is NetworkStatus.Error -> localDb.fetchRates()
                        .map { NetworkStatus.Error(response.message, localToUIMapper.mapList(it)) }
                }
            }
            .flowOn(dispatcherProvider.io())
            .onStart {
                emit(
                    localDb.fetchRates()
                        .map { NetworkStatus.Loading(localToUIMapper.mapList(it)) }.first()
                )
            }
                as Flow<NetworkStatus<List<CurrencyRate>>>

    override fun getHistoricalRates(numberOfDays: Int): Flow<NetworkStatus<List<Map<String, List<CurrencyRate>>>>> {
        return flow<NetworkStatus<List<Map<String, List<CurrencyRate>>>>> {
            val datesFromNow = mutableListOf<String>()
            var today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            datesFromNow.add(today.toString())
            for (i in 1..4) {
                today = today.plus((numberOfDays / 5) * -1, DateTimeUnit.DAY)
                datesFromNow.add(today.toString())
            }
            datesFromNow.reverse()
            val calls = datesFromNow.map {
                MainScope().async { ccApi.fetchRatesNoFlow(it) }
            }
            val result = calls.awaitAll()
            result.firstOrNull { it is NetworkStatus.Error }?.let {
                emit(NetworkStatus.Error((it as NetworkStatus.Error).message))
            } ?: kotlin.run {
                val list = result.map {
                    val response = (it as NetworkStatus.Success).data
                    mapOf(
                        response.date.formatDateString(
                            DATE_FORMAT,
                            "dd MMM"
                        ) to remoteToUIMapper.map(response)
                    )
                }
                emit(NetworkStatus.Success(list))
            }
        }

    }
}