package com.ossadkowski.crm.mobile.data

sealed class NetworkResult<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : NetworkResult<T>(data)
    open class Error<T>(message: String, data: T? = null) : NetworkResult<T>(data, message)
    class Loading<T> : NetworkResult<T>()
    /**
     * Subclass of Error so existing `is NetworkResult.Error` branches across the
     * codebase continue to match. Auth flow checks `is HttpError` first to read
     * the structured fields (code, deviceTrusted, deviceIsNew).
     */
    class HttpError<T>(
        val code: Int,
        message: String,
        val deviceTrusted: Boolean? = null,
        val deviceIsNew: Boolean? = null,
        data: T? = null
    ) : Error<T>(message, data)
}
