//package com.currency.converter.demo.repository
//
//import androidx.arch.core.executor.testing.InstantTaskExecutorRule
//import androidx.lifecycle.MutableLiveData
//import com.currency.converter.demo.BuildConfig
//import com.currency.converter.demo.api.RatesService
//import com.currency.converter.demo.api.Resource
//import com.currency.converter.demo.models.CurrencyRate
//import com.currency.converter.demo.models.realm.RatesRealm
//import io.realm.Realm
//import io.realm.RealmConfiguration
//import io.realm.RealmQuery
//import io.realm.RealmResults
//import io.realm.internal.RealmCore
//import org.junit.Rule
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.junit.runners.JUnit4
//import org.mockito.Mockito
//import io.realm.log.RealmLog
//import org.hamcrest.CoreMatchers.`is`
//import org.hamcrest.MatcherAssert.assertThat
//import org.junit.Before
//import org.powermock.api.mockito.PowerMockito
//import org.powermock.api.mockito.PowerMockito.mockStatic
//import org.powermock.core.classloader.annotations.PowerMockIgnore
//import org.powermock.core.classloader.annotations.PrepareForTest
//import org.powermock.modules.junit4.rule.PowerMockRule
//import org.robolectric.RobolectricTestRunner
//import org.robolectric.annotation.Config
//import org.powermock.api.mockito.PowerMockito.mock
//import org.powermock.api.mockito.PowerMockito.spy
//import org.powermock.api.mockito.PowerMockito.`when`
//
//
//@RunWith(RobolectricTestRunner::class)
//        @PowerMockIgnore("org.mockito.*", "org.robolectric.*", "android.*","androidx.*")
//@PrepareForTest(Realm::class, RealmLog::class)
//class RepositoryImplTest {
//
//    private lateinit var ratesService: RatesService
//    private lateinit var repo: RepositoryImpl
//
//    @Rule
//    @JvmField
//    val instantExecutorRule = InstantTaskExecutorRule()
//
//    @Rule
//    @JvmField
//    var rule = PowerMockRule()
//    var mockRealm: Realm? = null
//
//    @Before
//    fun setup() {
//        mockStatic(RealmLog::class.javaPrimitiveType)
//        mockStatic(Realm::class.javaPrimitiveType)
//        ratesService= mock(RatesService::class.java)
//        val mockRealm = mock(Realm::class.java)
//
//        `when`(Realm.getDefaultInstance()).thenReturn(mockRealm)
//
//        this.mockRealm = mockRealm
//        repo = RepositoryImpl(mockRealm!!, ratesService)
//    }
//
//    @Test
//    fun shouldBeAbleToGetDefaultInstance() {
//        assertThat(Realm.getDefaultInstance(), `is`(mockRealm))
//    }
//
//    @Test
//    fun shouldBeAbleToMockRealmMethods() {
//        `when`(mockRealm!!.isAutoRefresh()).thenReturn(true)
//        assertThat(mockRealm!!.isAutoRefresh(), `is`(true))
//
//        `when`(mockRealm!!.isAutoRefresh()).thenReturn(false)
//        assertThat(mockRealm!!.isAutoRefresh(), `is`(false))
//    }
//
//    @Test
//    fun shouldBeAbleToCreateARealmObject() {
//        val ratesRealm = RatesRealm()
//        `when`(mockRealm!!.createObject(RatesRealm::class.java)).thenReturn(ratesRealm)
//
//        val output = mockRealm!!.createObject(RatesRealm::class.java)
//
//        assertThat(output, `is`(ratesRealm))
//    }
//
//    @Test
//    fun fetchRates() {
//        val liveData = MutableLiveData<Resource<List<CurrencyRate>>>()
//        repo.getRates(liveData)
//        Mockito.verify(mockRealm)!!.where(RatesRealm::class.java)
//    }
//}