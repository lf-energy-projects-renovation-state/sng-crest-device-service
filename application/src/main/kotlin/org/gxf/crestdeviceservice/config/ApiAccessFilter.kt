// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.config

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import org.apache.catalina.connector.RequestFacade
import org.apache.catalina.connector.ResponseFacade
import org.springframework.stereotype.Component

@Component
class ApiAccessFilter : Filter {
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val requestUri = (request as RequestFacade).requestURI
        val isProxyService = !requestUri.startsWith("/web")
        val correctPortForProxyService = request.serverPort == 9000

        if (isProxyService && !correctPortForProxyService) {
            (response as ResponseFacade).sendError(404)
            return
        }

        chain.doFilter(request, response)
    }
}
