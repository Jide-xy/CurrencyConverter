/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.sharedmodule.di

import android.app.Application
import com.example.sharedmodule.android.BuildConfig
import com.example.sharedmodule.model.CurrencyRate
import com.example.sharedmodule.repository.Repository
import com.example.sharedmodule.repository.db.AppDatabase
import com.example.sharedmodule.repository.db.DatabaseDriverFactory
import com.example.sharedmodule.repository.db.LocalDb
import com.example.sharedmodule.repository.db.LocalDbImpl
import com.example.sharedmodule.repository.network.CCApi
import com.example.sharedmodule.repository.network.CCApiImpl
import com.example.sharedmodule.repository.network.model.RateResponse
import com.example.sharedmodule.util.DefaultDispatcherProvider
import com.example.sharedmodule.util.DispatcherProvider
import com.example.sharedmodule.util.Mapper
import com.example.sharedmodule.util.mapper.LocalToUiRateMapper
import com.example.sharedmodule.util.mapper.RemoteToLocalRateMapper
import com.example.sharedmodule.util.mapper.RemoteToUIRateMapper
import comexamplesharedmodulerepositorydb.RateEntity
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class SharedModule {

    @Singleton
    @Provides
    fun provideSharedRepository(
        ccApi: CCApi, localDb: LocalDb,
        localToUIMapper: Mapper<RateEntity, CurrencyRate>,
        remoteToLocalMapper: Mapper<RateResponse, List<RateEntity>>,
        remoteToUIMapper: Mapper<RateResponse, List<CurrencyRate>>
    ): Repository {
        return com.example.sharedmodule.repository.RepositoryImpl(
            ccApi,
            localDb,
            localToUIMapper,
            remoteToLocalMapper,
            remoteToUIMapper
        )
    }

    @Singleton
    @Provides
    fun provideSharedLocalDb(app: Application): LocalDb {
        return LocalDbImpl(AppDatabase(DatabaseDriverFactory(app).createDriver()))
    }

    @Singleton
    @Provides
    fun provideSharedApi(
//        httpClient: HttpClient,
        dispatcherProvider: DispatcherProvider
    ): CCApi {
        return CCApiImpl(BuildConfig.URL, dispatcherProvider)
    }

    @Singleton
    @Provides
    fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()

    @Singleton
    @Provides
    fun provideLocalToUIMapper(): Mapper<RateEntity, CurrencyRate> = LocalToUiRateMapper()

    @Singleton
    @Provides
    fun provideRemoteToLocalMapper(): Mapper<RateResponse, List<RateEntity>> =
        RemoteToLocalRateMapper()

    @Singleton
    @Provides
    fun provideRemoteToUIMapper(): Mapper<RateResponse, List<CurrencyRate>> = RemoteToUIRateMapper()

//    @Singleton
//    @Provides
//    fun provideHttpClient(): HttpClient {
//        return HttpClient(){
//            install(JsonFeature) {
//                val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
//                serializer = KotlinxSerializer(json)
//            }
//        }
//    }
}
