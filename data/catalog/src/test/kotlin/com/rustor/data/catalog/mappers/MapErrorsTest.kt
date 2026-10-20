package com.rustor.data.catalog.mappers

import com.rustor.core.common.error.AppException
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class MapErrorsTest {

    @Test
    fun `successful result passes through unchanged`() = runTest {
        val result = mapErrors { 42 }

        assertEquals(42, result)
    }

    @Test
    fun `io exception becomes no internet with cause`() = runTest {
        val original = IOException("boom")

        val error = assertFailsWith<AppException.NoInternet> {
            mapErrors { throw original }
        }

        assertSame(original, error.cause)
    }

    @Test
    fun `socket timeout exception becomes timeout with cause`() = runTest {
        val original = SocketTimeoutException("timeout error")

        val error = assertFailsWith<AppException.Timeout> {
            mapErrors { throw original }
        }

        assertSame(original, error.cause)
    }

    @Test
    fun `serialization exception becomes parsing with cause`() = runTest {
        val original = SerializationException("bad json")

        val error = assertFailsWith<AppException.Parsing> {
            mapErrors { throw original }
        }

        assertSame(original, error.cause)
    }

    @Test
    fun `unexpected exception becomes unknown with cause`() = runTest {
        val original = IllegalStateException("weird")

        val error = assertFailsWith<AppException.Unknown> {
            mapErrors { throw original }
        }

        assertSame(original, error.cause)
    }

    @Test
    fun `app exception is not rewrapped`() = runTest {
        val original = AppException.NoInternet()

        val error = assertFailsWith<AppException.NoInternet> {
            mapErrors { throw original }
        }

        assertSame(original, error)
    }

    @Test
    fun `cancellation is rethrown as is`() = runTest {
        val original = CancellationException("cancelled")

        val error = assertFailsWith<CancellationException> {
            mapErrors { throw original }
        }

        assertSame(original, error)
    }
}
