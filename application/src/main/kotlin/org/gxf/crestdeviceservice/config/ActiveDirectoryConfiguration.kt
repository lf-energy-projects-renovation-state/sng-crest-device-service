// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider

@Configuration
@Profile("!webtest")
class ActiveDirectoryConfiguration {
    @Bean
    fun authenticationProvider(properties: ActiveDirectoryProperties): ActiveDirectoryLdapAuthenticationProvider =
        ActiveDirectoryLdapAuthenticationProvider(properties.domain, properties.url)
}

@ConfigurationProperties(prefix = "config.ad")
class ActiveDirectoryProperties(val domain: String, val url: String)
