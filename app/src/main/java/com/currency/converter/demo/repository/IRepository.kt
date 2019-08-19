package com.currency.converter.demo.repository

import androidx.lifecycle.MutableLiveData
import androidx.work.ListenableWorker
import com.currency.converter.demo.api.Resource
import com.currency.converter.demo.models.CurrencyRate

interface IRepository {

    fun getRates(ratesLiveData: MutableLiveData<Resource<List<CurrencyRate>>>)
    fun getRatesInBackground(): ListenableWorker.Result
}