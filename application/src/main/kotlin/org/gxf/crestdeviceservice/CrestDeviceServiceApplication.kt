// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@ConfigurationPropertiesScan
@SpringBootApplication(
    // Prevent default user from being created by Spring Security
    exclude = [UserDetailsServiceAutoConfiguration::class],
)
class CrestDeviceServiceApplication

fun main(args: Array<String>) {
    runApplication<CrestDeviceServiceApplication>(*args)
}
