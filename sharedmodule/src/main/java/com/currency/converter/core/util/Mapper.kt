package com.currency.converter.core.util

interface Mapper<F, T> {

    suspend fun map(from: F): T
    suspend fun mapList(from: List<F>): List<T> = from.map { map(it) }
    suspend fun mapInverse(from: T): F
    suspend fun mapListInverse(from: List<T>): List<F> = from.map { mapInverse(it) }
}