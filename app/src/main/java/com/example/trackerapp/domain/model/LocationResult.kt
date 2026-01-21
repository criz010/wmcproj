package com.example.trackerapp.domain.model

/**
 * Sealed class representing the result of a location operation
 *
 * This is a type-safe way to handle success and error states
 * following Railway-Oriented Programming principles.
 *
 * Usage:
 * ```
 * when (val result = getLocation()) {
 *     is LocationResult.Success -> handleLocation(result.location)
 *     is LocationResult.Error -> handleError(result.error)
 * }
 * ```
 */
sealed class LocationResult {
    /**
     * Successful location result
     */
    data class Success(val location: Location) : LocationResult()

    /**
     * Error result with detailed error information
     */
    data class Error(val error: LocationError) : LocationResult()
}

/**
 * Sealed class representing different types of location errors
 */
sealed class LocationError(open val message: String) {
    /**
     * Location permission not granted
     */
    data class PermissionDenied(
        override val message: String = "Location permission is required"
    ) : LocationError(message)

    /**
     * Location services are disabled on the device
     */
    data class ServicesDisabled(
        override val message: String = "Location services are disabled"
    ) : LocationError(message)

    /**
     * Location request timed out
     */
    data class Timeout(
        override val message: String = "Location request timed out"
    ) : LocationError(message)

    /**
     * Network/connectivity issue
     */
    data class NetworkError(
        override val message: String = "Network error occurred"
    ) : LocationError(message)

    /**
     * General/unknown error
     */
    data class Unknown(
        override val message: String = "An unknown error occurred",
        val exception: Throwable? = null
    ) : LocationError(message)

    /**
     * Location provider unavailable
     */
    data class ProviderUnavailable(
        override val message: String = "Location provider is unavailable"
    ) : LocationError(message)
}

/**
 * Generic Result type for operations that can succeed or fail
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val error: Throwable, val message: String? = null) : Result<Nothing>()
    data object Loading : Result<Nothing>()

    /**
     * Returns true if this is a Success result
     */
    fun isSuccess(): Boolean = this is Success

    /**
     * Returns true if this is an Error result
     */
    fun isError(): Boolean = this is Error

    /**
     * Returns true if this is a Loading result
     */
    fun isLoading(): Boolean = this is Loading

    /**
     * Returns the data if Success, null otherwise
     */
    fun getOrNull(): T? = if (this is Success) data else null

    /**
     * Maps the success value with the given transform function
     */
    inline fun <R> map(transform: (T) -> R): Result<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> this
            is Loading -> this
        }
    }

    /**
     * Executes the given action if this is an Error
     */
    inline fun onError(action: (Throwable, String?) -> Unit): Result<T> {
        if (this is Error) action(error, message)
        return this
    }

    /**
     * Executes the given action if this is Loading
     */
    inline fun onLoading(action: () -> Unit): Result<T> {
        if (this is Loading) action()
        return this
    }
}

fun <T> Result<T>.getOrDefault(defaultValue: T): T {
    return when (this) {
        is Result.Success -> data
        else -> defaultValue
    }
}

inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}
