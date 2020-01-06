package com.currency.converter.demo.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.currency.converter.demo.repository.RepositoryImpl
import com.currency.converter.demo.ui.MainViewModel
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito
import org.mockito.Mockito.*

@RunWith(JUnit4::class)
class MainViewModelTest {

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private val repository = mock(RepositoryImpl::class.java)
    private var repoViewModel = MainViewModel(repository)

    @Test
    fun testNull() {
        assertThat(repoViewModel.repository, notNullValue())
        assertThat(repoViewModel.ratesLiveData, notNullValue())
        //verify(repoViewModel, never()).getRates()
//        verify(repository, never())
//            .getRates(any())
    }
}