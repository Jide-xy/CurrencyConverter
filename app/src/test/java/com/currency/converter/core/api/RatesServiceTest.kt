package com.currency.converter.core.api

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.currency.converter.core.BuildConfig
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.buffer
import okio.source
import org.hamcrest.CoreMatchers.*
import org.hamcrest.core.IsNull
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@RunWith(JUnit4::class)
class RatesServiceTest {

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var service: RatesService

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun createService() {
        mockWebServer = MockWebServer()
        service = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RatesService::class.java)
    }

    @After
    fun stopService() {
        mockWebServer.shutdown()
    }

    @Test
    fun fetchRates() {
        enqueueResponse("rates-response.json")
        val rates = service.fetchRates(BuildConfig.API_KEY).execute().body()

        val request = mockWebServer.takeRequest()
        assertThat(request.path, `is`("/latest?access_key=${BuildConfig.API_KEY}"))

        assertThat<RatesResult>(rates, IsNull.notNullValue())
        assertThat(
            rates!!.success,
            `is`(true)
        )
        assertThat(rates.base, `is`("USD"))
        assertThat(rates.date, `is`("2019-08-24"))
        assertThat<Long>(rates.timestamp, `is`(1519296206))
        assertThat(rates.rates, `is`(mapOf(Pair("GBP",0.72007),Pair("JPY",107.346001), Pair("EUR",0.813399))))
    }

    private fun enqueueResponse(fileName: String, headers: Map<String, String> = emptyMap()) {
        val inputStream = javaClass.classLoader
            .getResourceAsStream("api-response/$fileName")
        val source = inputStream.source().buffer()
        val mockResponse = MockResponse()
        for ((key, value) in headers) {
            mockResponse.addHeader(key, value)
        }
        mockWebServer.enqueue(
            mockResponse
                .setBody(source.readString(Charsets.UTF_8))
        )
    }
}