package com.talkingkotlin.model.network

/**
 * Network State Machine for Retrofit requests
 * @author Alexander Gherschon
 */

sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    class NetworkError<out T>(val data: T? = null) : Resource<T>()
    class Loading<out T> : Resource<T>()
}

