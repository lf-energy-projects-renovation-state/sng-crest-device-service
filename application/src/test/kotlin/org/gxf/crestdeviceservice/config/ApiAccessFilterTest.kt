// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class ApiAccessFilterTest {
    private val serverProperties = mock<ServerProperties>()
    private val filter = ApiAccessFilter(serverProperties)
    private val proxyPort = 9000

    @BeforeEach
    fun setup() {
        whenever(serverProperties.port).thenReturn(proxyPort)
    }

    @ParameterizedTest
    @CsvSource(
        "9000, /psk, 200",
        "9000, /sng, 200",
        "9000, /error, 200",
        "9000, /web/firmware, 404",
        "9000, /web/firmware/api, 404",
        "9000, /wbe/firmware/api, 404",
        "9001, /psk, 404",
        "9001, /sng, 404",
        "9001, /error, 200",
        "9001, /web/firmware, 200",
        "9001, /web/firmware/api, 200",
        "9001, /wbe/firmware/api, 404",
        "8080, /psk, 404",
    )
    fun shouldReturn404ForProxyRequestsOnWebPort(port: Int, uri: String, expectedHttpCode: Int) {
        val chain = MockFilterChain()
        val request = MockHttpServletRequest()
        request.serverPort = port
        request.requestURI = uri
        val response = MockHttpServletResponse()

        filter.doFilter(request, response, chain)
        assertThat(response.status).isEqualTo(expectedHttpCode)
    }
}
