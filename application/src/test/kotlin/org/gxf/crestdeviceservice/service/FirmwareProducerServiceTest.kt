// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.service

import org.apache.avro.specific.SpecificRecordBase
import org.gxf.crestdeviceservice.FirmwaresFactory
import org.gxf.crestdeviceservice.TestConstants.COMMAND_FEEDBACK_TOPIC
import org.gxf.crestdeviceservice.TestConstants.DEVICE_MESSAGE_TOPIC
import org.gxf.crestdeviceservice.TestConstants.FIRMWARE_KEY
import org.gxf.crestdeviceservice.TestConstants.FIRMWARE_TOPIC
import org.gxf.crestdeviceservice.config.KafkaProducerProperties
import org.gxf.crestdeviceservice.config.KafkaProducerTopicKeyProperties
import org.gxf.crestdeviceservice.config.KafkaProducerTopicProperties
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.kafka.core.KafkaTemplate

@ExtendWith(MockitoExtension::class)
class FirmwareProducerServiceTest {
    @Mock private lateinit var mockedKafkaTemplate: KafkaTemplate<String, SpecificRecordBase>

    private val kafkaProducerProperties =
        KafkaProducerProperties(
            KafkaProducerTopicProperties(DEVICE_MESSAGE_TOPIC),
            KafkaProducerTopicProperties(COMMAND_FEEDBACK_TOPIC),
            KafkaProducerTopicKeyProperties(FIRMWARE_TOPIC, FIRMWARE_KEY)
        )

    @Test
    fun shouldCallMessageProducerWithCorrectParams() {
        val firmwareProducerService = FirmwareProducerService(mockedKafkaTemplate, kafkaProducerProperties)
        val firmwares = FirmwaresFactory.getFirmwares()

        firmwareProducerService.send(firmwares)
        verify(mockedKafkaTemplate).send(FIRMWARE_TOPIC, FIRMWARE_KEY, firmwares)
    }
}
