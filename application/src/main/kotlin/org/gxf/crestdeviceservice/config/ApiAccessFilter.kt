// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.config

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component

@Component
class ApiAccessFilter : Filter {
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val requestUri = (request as HttpServletRequest).requestURI
        val isProxyService = !requestUri.startsWith("/web")
        val correctPortForProxyService = request.serverPort == 9000

        if (isProxyService && !correctPortForProxyService) {
            (response as HttpServletResponse).sendError(404)
            return
        }

        chain.doFilter(request, response)
    }
}
