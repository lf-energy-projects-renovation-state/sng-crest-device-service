// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.avro.specific.SpecificRecordBase
import org.gxf.crestdeviceservice.config.KafkaProducerProperties
import org.gxf.crestdeviceservice.firmware.mapper.FirmwareMapper
import org.gxf.crestdeviceservice.firmware.service.FirmwareService
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class FirmwareProducerService(
    private val kafkaTemplate: KafkaTemplate<String, SpecificRecordBase>,
    private val kafkaProducerProperties: KafkaProducerProperties,
    private val firmwareService: FirmwareService,
    private val firmwareMapper: FirmwareMapper,
) {
    private val logger = KotlinLogging.logger {}

    fun sendAllFirmwares() {
        val allFirmwares = firmwareService.findAllFirmwares()
        val avroFirmwares = firmwareMapper.mapEntitiesToFirmwares(allFirmwares)
        logger.info { "Sending firmwares to Maki" }
        kafkaTemplate.send(kafkaProducerProperties.firmware.topic, kafkaProducerProperties.firmware.key, avroFirmwares)
    }
}
