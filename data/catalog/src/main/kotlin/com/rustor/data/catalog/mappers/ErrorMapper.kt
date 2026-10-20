package com.rustor.data.catalog.mappers

import com.rustor.core.common.error.AppException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import java.io.IOException
import java.net.SocketTimeoutException

@Suppress("TooGenericExceptionCaught")
internal suspend fun <T> mapErrors(block: suspend () -> T): T = try {
    block()
} catch (e: CancellationException) {
    throw e
} catch (e: AppException) {
    throw e
} catch (e: SocketTimeoutException) {
    throw AppException.Timeout(e)
} catch (e: IOException) {
    throw AppException.NoInternet(e)
} catch (e: SerializationException) {
    throw AppException.Parsing(e)
} catch (e: Exception) {
    throw AppException.Unknown(e)
}
