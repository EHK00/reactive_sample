package com.ekh.reactivesample.domain.base

sealed class Resource<out R> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val exception: Throwable) : Resource<Nothing>()
    object Loading : Resource<Nothing>()

    override fun toString(): String = when (this) {
        is Success -> "SUCCESS ${this.data}"
        is Error -> "ERROR ${this.exception}"
        Loading -> "LOADING"
    }
}
