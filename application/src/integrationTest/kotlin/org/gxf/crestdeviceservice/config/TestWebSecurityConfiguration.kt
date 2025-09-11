// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.provisioning.InMemoryUserDetailsManager

@Configuration
@EnableWebSecurity
class TestWebSecurityConfiguration {
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
