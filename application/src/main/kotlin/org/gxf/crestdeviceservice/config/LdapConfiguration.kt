// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder

@Configuration
@Profile("!webtest")
class LdapConfiguration {
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
    }
}
