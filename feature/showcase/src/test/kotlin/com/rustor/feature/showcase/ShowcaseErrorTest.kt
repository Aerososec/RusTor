package com.rustor.feature.showcase

import com.rustor.core.common.error.AppException
import com.rustor.feature.showcase.error.ShowcaseError
import com.rustor.feature.showcase.uiState.toShowcaseError
import org.junit.Test
import kotlin.test.assertEquals

class ShowcaseErrorTest {

    @Test
    fun `connection problems become no connection`() {
        val noInternet = AppException.NoInternet().toShowcaseError()
        val timeout = AppException.Timeout().toShowcaseError()

        assertEquals(ShowcaseError.NoConnection, noInternet)
        assertEquals(ShowcaseError.NoConnection, timeout)
    }

    @Test
    fun `parsing becomes service unavailable`() {
        val result = AppException.Parsing().toShowcaseError()

        assertEquals(ShowcaseError.ServiceUnavailable, result)
    }

    @Test
    fun `unknown becomes unknown`() {
        val result = AppException.Unknown().toShowcaseError()

        assertEquals(ShowcaseError.Unknown, result)
    }
}
