// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdeviceservice

import org.gxf.crestdeviceservice.kafka.configuration.KafkaProducerProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableConfigurationProperties(KafkaProducerProperties::class)
@EnableScheduling
@SpringBootApplication
class CrestDeviceServiceApplication

fun main(args: Array<String>) {
    runApplication<CrestDeviceServiceApplication>(*args)
}

