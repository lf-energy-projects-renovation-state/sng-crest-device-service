// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class WebSecurityConfiguration {
    @Bean
    fun filterChain(http: HttpSecurity, webServerProperties: WebServerProperties): SecurityFilterChain {
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
            csrf {
                disable()
            }
        }
        return http.build()
    }

    @Autowired
    fun configure(auth: AuthenticationManagerBuilder) {
        auth.ldapAuthentication()
            .userDnPatterns("uid={0},ou=OTHUB,dc=gxf,dc=org")
            .groupSearchBase("ou=groups,dc=gxf,dc=org")
            .groupSearchFilter("member={0}")
            .contextSource().apply {
                root("dc=gxf,dc=org")
                url("ldap://localhost:389")
                managerDn("cn=admin,dc=gxf,dc=org")
                managerPassword("admin")
            }
            .and()
            .passwordCompare().apply {
//                passwordEncoder(BCryptPasswordEncoder())
                passwordAttribute("userPassword")
            }
    }
}
