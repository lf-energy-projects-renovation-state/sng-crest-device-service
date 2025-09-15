// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class WebSecurityConfiguration {
    private val logger = KotlinLogging.logger {}

    @Bean
    fun filterChain(
        http: HttpSecurity,
        webServerProperties: WebServerProperties,
        env: Environment,
    ): SecurityFilterChain {
        http {
            authorizeHttpRequests {
                authorize(
                    "/web/**",
                    hasAnyAuthority(authorities = webServerProperties.authorizedRoles.toTypedArray<String>()),
                )
                authorize(anyRequest, permitAll)
            }
            formLogin {
                defaultSuccessUrl("/web", true)
            }
            logout {
                logoutUrl = "/web/logout"
            }
            if (env.acceptsProfiles(Profiles.of("webtest"))) {
                logger.warn { "Enabling HTTP Basic authentication, this should only be enabled for testing" }
                httpBasic {}
            }
            csrf {
                disable()
            }
        }
        return http.build()
    }
}
