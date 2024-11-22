// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.resulthandler

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CommandResultHandlerConfig {

    @Bean
    fun commandResultHandlersByType(commandResultHandlers: List<CommandResultHandler>) =
        commandResultHandlers.associateBy { it.supportedCommandType }
}
