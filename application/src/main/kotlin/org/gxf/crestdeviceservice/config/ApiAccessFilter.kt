// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.config

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.stereotype.Component

@Component
class ApiAccessFilter(private val serverProperties: ServerProperties) : Filter {
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        if (isAllowedCombination(request)) {
            chain.doFilter(request, response)
        } else {
            (response as HttpServletResponse).sendError(404)
        }
    }

    private fun isAllowedCombination(request: ServletRequest): Boolean {
        val requestUri = (request as HttpServletRequest).requestURI

        return isErrorEndpoint(requestUri) ||
            (isWebEndpoint(requestUri) && !isProxyPort(request)) ||
            (isProxyEndpoint(requestUri) && isProxyPort(request))
    }

    private fun isErrorEndpoint(requestUri: String) = requestUri.startsWith("/error")

    private fun isProxyEndpoint(requestUri: String) = requestUri.startsWith("/sng") || requestUri.startsWith("/psk")

    private fun isWebEndpoint(requestUri: String) = requestUri.startsWith("/web") || requestUri.startsWith("/test")

    private fun isProxyPort(request: ServletRequest) = request.serverPort == serverProperties.port
}
