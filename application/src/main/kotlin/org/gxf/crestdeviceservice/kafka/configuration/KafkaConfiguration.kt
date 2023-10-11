// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdeviceservice.kafka.configuration

import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory


@Configuration
class KafkaConfiguration(private val kafkaProperties: KafkaProperties) {

    @Bean
    fun kafkaListenerContainerFactory(consumerFactory: ConsumerFactory<String, String>): ConcurrentKafkaListenerContainerFactory<String, String> =
            ConcurrentKafkaListenerContainerFactory<String, String>()
                    .apply { this.consumerFactory = consumerFactory }
}
