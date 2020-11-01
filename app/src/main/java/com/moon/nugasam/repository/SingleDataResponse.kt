package com.moon.nugasam.repository

import com.moon.nugasam.repository.SingleDataStatus.*

enum class SingleDataStatus {
    SUCCESS,
    ERROR,
    LOADING,
    INIT
}

data class SingleDataResponse<out T>(
    val status: SingleDataStatus,
    val data: T? = null,
    val error: Throwable? = null
) {
    companion object {
        fun <T> success(data: T?): SingleDataResponse<T> {
            return SingleDataResponse(
                status = SUCCESS,
                data = data
            )
        }

        fun <T> error(error: Throwable?): SingleDataResponse<T> {
            return SingleDataResponse(
                status = ERROR,
                error = error
            )
        }

        fun <T> loading(): SingleDataResponse<T> {
            return SingleDataResponse(status = LOADING)
        }

        fun <T> init(): SingleDataResponse<T> {
            return SingleDataResponse(status = INIT)
        }
    }
}