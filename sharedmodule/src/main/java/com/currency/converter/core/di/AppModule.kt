package com.currency.converter.core.di

import android.content.Context
import com.currency.converter.core.model.CurrencyRate
import com.currency.converter.core.repository.Repository
import com.currency.converter.core.repository.RepositoryImpl
import com.currency.converter.core.repository.db.AppDatabase
import com.currency.converter.core.repository.db.model.RateEntity
import com.currency.converter.core.repository.network.CCApi
import com.currency.converter.core.repository.network.CCApiImpl
import com.currency.converter.core.repository.network.CCService
import com.currency.converter.core.repository.network.model.RateResponse
import com.currency.converter.core.util.DefaultDispatcherProvider
import com.currency.converter.core.util.DispatcherProvider
import com.currency.converter.core.util.Mapper
import com.currency.converter.core.util.mapper.LocalToUiRateMapper
import com.currency.converter.core.util.mapper.RemoteToLocalRateMapper
import com.currency.converter.core.util.mapper.RemoteToUIRateMapper
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideService(): CCService {
        return Retrofit.Builder()
            .baseUrl("http://data.fixer.io/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CCService::class.java)
    }

    @Singleton
    @Provides
    fun provideLocalDb(
        @ApplicationContext context: Context
    ): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Singleton
    @Provides
    fun provideRateDao(db: AppDatabase) = db.rateDao()

    @Singleton
    @Provides
    fun bindRemoteToLocalMapper(
//        mapper: RemoteToLocalRateMapper
    ): Mapper<RateResponse, List<RateEntity>> = RemoteToLocalRateMapper()

    @Singleton
    @Provides
    fun bindRemoteToUIMapper(
//        mapper: RemoteToUIRateMapper
    ): Mapper<RateResponse, List<CurrencyRate>> = RemoteToUIRateMapper()
}

@Module
@InstallIn(ApplicationComponent::class)
abstract class BindingModule {

    @ExperimentalCoroutinesApi
    @Singleton
    @Binds
    abstract fun bindSharedRepository(repositoryImpl: RepositoryImpl): Repository


    @Singleton
    @Binds
    abstract fun provideSharedApi(
        ccApiImpl: CCApiImpl
    ): CCApi

    @Singleton
    @Binds
    abstract fun bindDispatcherProvider(defaultDispatcherProvider: DefaultDispatcherProvider): DispatcherProvider

    @Singleton
    @Binds
    abstract fun bindLocalToUIMapper(mapper: LocalToUiRateMapper): Mapper<RateEntity, CurrencyRate>
}