package com.fitdroid.core.common

sealed interface AppError {
    val message: String
    val cause: Throwable?

    data class Network(
        override val message: String,
        override val cause: Throwable? = null,
    ) : AppError

    data class Unavailable(
        override val message: String,
        override val cause: Throwable? = null,
    ) : AppError

    data class PermissionDenied(
        override val message: String,
        override val cause: Throwable? = null,
    ) : AppError

    data class Unknown(
        override val message: String,
        override val cause: Throwable? = null,
    ) : AppError

    companion object {
        fun from(throwable: Throwable): AppError = when (throwable) {
            is java.io.IOException -> Network(
                message = throwable.message ?: "Network request failed",
                cause = throwable,
            )

            else -> Unknown(
                message = throwable.message ?: "Something went wrong",
                cause = throwable,
            )
        }
    }
}
