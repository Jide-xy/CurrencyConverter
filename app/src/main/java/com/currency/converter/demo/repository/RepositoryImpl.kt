package com.currency.converter.demo.repository

import androidx.annotation.WorkerThread
import androidx.lifecycle.MutableLiveData
import androidx.work.ListenableWorker.Result
import com.currency.converter.demo.BuildConfig
import com.currency.converter.demo.api.RatesResult
import com.currency.converter.demo.api.RatesService
import com.currency.converter.demo.api.Resource
import com.currency.converter.demo.models.CurrencyRate
import com.currency.converter.demo.models.realm.RatesRealm
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class RepositoryImpl @Inject constructor(val realmDb: Realm, val ratesService: RatesService) : IRepository {


    override fun getRates(ratesLiveData: MutableLiveData<Resource<List<CurrencyRate>>>) {
        val ratesResult = realmDb.where(RatesRealm::class.java).findAll()
        ratesLiveData.value = Resource.loading(ratesResult.map { it.toRate() })
        ratesResult.addChangeListener(RealmChangeListener<RealmResults<RatesRealm>> {
            ratesLiveData.value = Resource.success(ratesResult.map {
                it.toRate()
            })
        })
        ratesService.searchRepos(BuildConfig.API_KEY).enqueue(object : Callback<RatesResult> {
            override fun onFailure(call: Call<RatesResult>, t: Throwable) {
                ratesLiveData.value = Resource.error(t.message ?: "", null)
            }

            override fun onResponse(call: Call<RatesResult>, response: Response<RatesResult>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    if (response.body()?.rates?.isEmpty() == true) {
                        ratesLiveData.value = Resource.error("Empty data", null)
                    } else {
                        val responseBody = response.body() as RatesResult
                        realmDb.executeTransactionAsync { realm ->
                            realm.insertOrUpdate(responseBody.rates.map {
                                val ratesRealm = RatesRealm()
                                ratesRealm.baseCurrencyCode = responseBody.base
                                ratesRealm.currencyCode = it.key
                                ratesRealm.rate = it.value
                                return@map ratesRealm
                            })
                        }
                    }
                }
            }

        })
    }

    //TODO: Use method to fetch rates at interval in background with workmanager
    @WorkerThread
    override fun getRatesInBackground(): Result {
        val response = ratesService.searchRepos(BuildConfig.API_KEY).execute()
        if (response.isSuccessful && response.body()?.success == true) {
            if (response.body()?.rates?.isEmpty() == true) {
                return Result.failure()
            }
            val responseBody = response.body() as RatesResult
            realmDb.beginTransaction()
            realmDb.insertOrUpdate(responseBody.rates.map {
                val ratesRealm = RatesRealm()
                ratesRealm.baseCurrencyCode = responseBody.base
                ratesRealm.currencyCode = it.key
                ratesRealm.rate = it.value
                return@map ratesRealm
            })
            realmDb.commitTransaction()
            return Result.success()
        }
        return Result.retry()
    }
}