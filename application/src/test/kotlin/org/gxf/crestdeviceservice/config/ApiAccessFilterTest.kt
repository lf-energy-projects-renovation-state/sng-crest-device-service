// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class ApiAccessFilterTest {
    private val portProxy = 9000

    private val serverProperties = ServerProperties(portProxy)
    private val filter = ApiAccessFilter(serverProperties)

    @ParameterizedTest
    @CsvSource(
        "9000, /psk, 200",
        "9000, /sng, 200",
        "9000, /error, 200",
        "9000, /web/firmware, 200",
        "9000, /web/api/firmware, 200",
        "9001, /psk, 404",
        "9001, /sng, 404",
        "9001, /error, 200",
        "9001, /web/firmware, 200",
        "9001, /web/api/firmware, 200")
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
