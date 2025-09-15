// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider

@Configuration
@Profile("!webtest")
class ActiveDirectoryConfiguration {
    @Bean
    fun authencationProvider(): ActiveDirectoryLdapAuthenticationProvider =
        ActiveDirectoryLdapAuthenticationProvider("testing.gxf.org", "ldap://localhost:389")
}
