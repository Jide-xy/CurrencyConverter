package com.example.sharedmodule.util

sealed class NetworkStatus<T> {
    data class Success<T>(val data: T) : NetworkStatus<T>()
    data class Loading<T>(val data: T? = null) : NetworkStatus<T>()
    data class Error<T>(val message: String, val data: T? = null) : NetworkStatus<T>()
}