// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.config

import io.github.oshai.kotlinlogging.KotlinLogging
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
    private val logger = KotlinLogging.logger {}

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        if (isAllowedCombination(request as HttpServletRequest)) {
            chain.doFilter(request, response)
        } else {
            (response as HttpServletResponse).sendError(404)
        }
    }

    private fun isAllowedCombination(request: HttpServletRequest): Boolean {
        logger.debug { "Filtering request for ${request.requestURL}" }
        val requestUri = request.requestURI

        return isErrorEndpoint(requestUri) ||
            if (isProxyPort(request)) {
                isProxyEndpoint(requestUri)
            } else {
                isWebEndpoint(requestUri)
            }
    }

    private fun isErrorEndpoint(requestUri: String) =
        requestUri.startsWith("/error").also { logger.debug { "isErrorEndpoint: $it" } }

    private fun isProxyEndpoint(requestUri: String) =
        (requestUri.startsWith("/sng") || requestUri.startsWith("/psk")).also {
            logger.debug { "isProxyEndpoint: $it" }
        }

    private fun isWebEndpoint(requestUri: String) =
        (requestUri.startsWith("/web") || requestUri.startsWith("/test")).also { logger.debug { "isWebEndpoint: $it" } }

    private fun isProxyPort(request: ServletRequest) =
        (request.serverPort == serverProperties.port).also { logger.debug { "isProxyPort: $it" } }
}
