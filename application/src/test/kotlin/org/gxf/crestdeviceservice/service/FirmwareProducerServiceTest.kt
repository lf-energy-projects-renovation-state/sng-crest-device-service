// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.service

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import java.util.UUID
import org.apache.avro.specific.SpecificRecordBase
import org.gxf.crestdeviceservice.FirmwaresFactory
import org.gxf.crestdeviceservice.TestConstants.COMMAND_FEEDBACK_TOPIC
import org.gxf.crestdeviceservice.TestConstants.DEVICE_MESSAGE_TOPIC
import org.gxf.crestdeviceservice.TestConstants.FIRMWARE_KEY
import org.gxf.crestdeviceservice.TestConstants.FIRMWARE_TOPIC
import org.gxf.crestdeviceservice.config.KafkaProducerProperties
import org.gxf.crestdeviceservice.config.KafkaProducerTopicKeyProperties
import org.gxf.crestdeviceservice.config.KafkaProducerTopicProperties
import org.gxf.crestdeviceservice.firmware.entity.Firmware
import org.gxf.crestdeviceservice.firmware.mapper.FirmwareMapper
import org.gxf.crestdeviceservice.firmware.service.FirmwareService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.kafka.core.KafkaTemplate

@ExtendWith(MockKExtension::class)
class FirmwareProducerServiceTest {
    @MockK(relaxed = true) private lateinit var kafkaTemplate: KafkaTemplate<String, SpecificRecordBase>
    private lateinit var kafkaProducerProperties: KafkaProducerProperties
    @MockK private lateinit var firmwareService: FirmwareService
    @MockK private lateinit var firmwareMapper: FirmwareMapper

    private lateinit var service: FirmwareProducerService

    @BeforeEach
    fun setUp() {
        kafkaProducerProperties =
            KafkaProducerProperties(
                KafkaProducerTopicProperties(DEVICE_MESSAGE_TOPIC),
                KafkaProducerTopicProperties(COMMAND_FEEDBACK_TOPIC),
                KafkaProducerTopicKeyProperties(FIRMWARE_TOPIC, FIRMWARE_KEY),
            )

        service = FirmwareProducerService(kafkaTemplate, kafkaProducerProperties, firmwareService, firmwareMapper)
    }

    @Test
    fun shouldCallMessageProducerWithCorrectParams() {
        val firmwareEntityList =
            listOf(Firmware(UUID.randomUUID(), "fw1", "1"), Firmware(UUID.randomUUID(), "fw2", "1"))
        val avroFirmwares = FirmwaresFactory.getFirmwares()

        every { firmwareService.findAllFirmwares() } returns firmwareEntityList
        every { firmwareMapper.mapEntitiesToFirmwares(firmwareEntityList) } returns avroFirmwares

        service.sendAllFirmwares()

        verify { kafkaTemplate.send(FIRMWARE_TOPIC, FIRMWARE_KEY, avroFirmwares) }
    }
}
