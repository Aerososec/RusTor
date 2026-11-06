package com.rustor.feature.showcase

import com.rustor.core.common.error.AppException
import com.rustor.feature.showcase.error.UiError
import com.rustor.feature.showcase.uiState.toShowcaseError
import org.junit.Test
import kotlin.test.assertEquals

class UiErrorTest {

    @Test
    fun `connection problems become no connection`() {
        val noInternet = AppException.NoInternet().toShowcaseError()
        val timeout = AppException.Timeout().toShowcaseError()

        assertEquals(UiError.NoConnection, noInternet)
        assertEquals(UiError.NoConnection, timeout)
    }

    @Test
    fun `parsing becomes service unavailable`() {
        val result = AppException.Parsing().toShowcaseError()

        assertEquals(UiError.ServiceUnavailable, result)
    }

    @Test
    fun `unknown becomes unknown`() {
        val result = AppException.Unknown().toShowcaseError()

        assertEquals(UiError.Unknown, result)
    }
}
