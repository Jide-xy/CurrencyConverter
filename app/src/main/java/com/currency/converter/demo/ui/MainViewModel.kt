package com.currency.converter.demo.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.currency.converter.demo.api.Resource
import com.currency.converter.demo.models.CurrencyRate
import com.currency.converter.demo.repository.IRepository
import javax.inject.Inject

class MainViewModel @Inject constructor(val repository: IRepository) : ViewModel() {
    private val _ratesLiveData: MutableLiveData<Resource<List<CurrencyRate>>> = MutableLiveData()
    val ratesLiveData: LiveData<Resource<List<CurrencyRate>>>
        get() = _ratesLiveData

    fun getRates() {
        repository.getRates(_ratesLiveData)
    }
}