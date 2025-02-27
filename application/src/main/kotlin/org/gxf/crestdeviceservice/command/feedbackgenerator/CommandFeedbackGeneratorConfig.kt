// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.command.feedbackgenerator

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CommandFeedbackGeneratorConfig {
    @Bean
    fun commandFeedbackGeneratorsByType(commandFeedbackGenerators: List<CommandFeedbackGenerator>) =
        commandFeedbackGenerators.associateBy { it.supportedCommandType }
}
