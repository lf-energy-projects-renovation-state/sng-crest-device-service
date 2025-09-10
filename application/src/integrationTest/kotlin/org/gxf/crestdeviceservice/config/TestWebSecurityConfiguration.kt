// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.userdetails.User
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain

@Configuration
class TestWebSecurityConfiguration {
    @Bean()
    fun testFilterChain(http: HttpSecurity, webServerProperties: WebServerProperties): SecurityFilterChain {
        http {
            authorizeHttpRequests {
                authorize(
                    "/web/**",
                    hasAnyRole(
                        roles = webServerProperties.authorizedRoles
                            .map { it.uppercase() }
                            .toTypedArray<String>(),
                    ),
                )
                authorize(anyRequest, permitAll)
            }
            formLogin {
            }
            httpBasic { } // no LDAP server here
            csrf {
                disable()
            }
        }
        return http.build()
    }

    @Bean
    fun userDetailsService(): InMemoryUserDetailsManager {
        val kodUser = User.withUsername("kod")
            .password("{noop}kodpass")
            .roles("KOD")
            .build()
        val flexUser = User.withUsername("flex")
            .password("{noop}flexpass")
            .roles("FLEX")
            .build()
        return InMemoryUserDetailsManager(kodUser, flexUser)
    }
}
