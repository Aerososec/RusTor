package com.rustor.core.common.error

import org.junit.Assert.assertEquals
import org.junit.Test

class UiErrorTest {

    @Test
    fun `connection problems become no connection`() {
        val noInternet = AppException.NoInternet().toUiError()
        val timeout = AppException.Timeout().toUiError()

        assertEquals(UiError.NoConnection, noInternet)
        assertEquals(UiError.NoConnection, timeout)
    }

    @Test
    fun `parsing becomes service unavailable`() {
        val result = AppException.Parsing().toUiError()

        assertEquals(UiError.ServiceUnavailable, result)
    }

    @Test
    fun `unknown becomes unknown`() {
        val result = AppException.Unknown().toUiError()

        assertEquals(UiError.Unknown, result)
    }
}
