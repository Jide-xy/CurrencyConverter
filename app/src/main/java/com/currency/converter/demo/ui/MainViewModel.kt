package com.currency.converter.demo.ui

import androidx.lifecycle.*
import com.example.sharedmodule.model.CurrencyRate
import com.example.sharedmodule.repository.Repository
import com.example.sharedmodule.util.NetworkStatus
import javax.inject.Inject

class MainViewModel @Inject constructor(private val sharedRepository: Repository) : ViewModel() {

    private val _ratesLiveData: MutableLiveData<Unit> = MutableLiveData()
    val ratesLiveData: LiveData<NetworkStatus<List<CurrencyRate>>> =
        Transformations.switchMap(_ratesLiveData) {
            sharedRepository.fetchRates().asLiveData()
        }

    private val _thirtyDaysLiveData: MutableLiveData<Int> = MutableLiveData()
    private val thirtyDaysLiveData = _thirtyDaysLiveData.switchMap {
        sharedRepository.getHistoricalRates(it).asLiveData()
    }

    private val _ninetyDaysLiveData: MutableLiveData<Int> = MutableLiveData()
    private val ninetyDaysLiveData = _ninetyDaysLiveData.switchMap {
        sharedRepository.getHistoricalRates(it).asLiveData()
    }

    private val _historicalRatesLiveData: MediatorLiveData<Result> =
        MediatorLiveData<Result>().apply {
            addSource(thirtyDaysLiveData) {
                value = Result(30, it)
            }
            addSource(ninetyDaysLiveData) {
                value = Result(90, it)
            }
        }
    val historicalRatesLiveData: LiveData<Result>
        get() = _historicalRatesLiveData

    init {
        getHistoricalRates(30)
        getHistoricalRates(90)
    }

    fun getRates() {
        _ratesLiveData.value = Unit
    }

    fun getHistoricalRates(numberOfDays: Int) {
        if (numberOfDays == 30) {
            _thirtyDaysLiveData.value = numberOfDays
        } else _ninetyDaysLiveData.value = numberOfDays
    }
}

data class Result(
    val numberOfDays: Int,
    val data: NetworkStatus<List<Map<String, List<CurrencyRate>>>>
)