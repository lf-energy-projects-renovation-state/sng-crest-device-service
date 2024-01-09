// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdeviceservice.kafka.configuration

import com.gxf.utilities.kafka.avro.AvroDeserializer
import com.gxf.utilities.kafka.avro.AvroSerializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.gxf.sng.avro.DeviceMessage
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.boot.ssl.SslBundles
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*


@Configuration
class KafkaConfiguration(private val kafkaProperties: KafkaProperties, private val sslBundles: SslBundles) {

    @Bean
    fun kafkaListenerContainerFactory(consumerFactory: ConsumerFactory<String, DeviceMessage>): ConcurrentKafkaListenerContainerFactory<String, DeviceMessage> =
            ConcurrentKafkaListenerContainerFactory<String, DeviceMessage>()
                    .apply { this.consumerFactory = consumerFactory }

    @Bean
    fun consumerFactory(): ConsumerFactory<String, DeviceMessage> =
            DefaultKafkaConsumerFactory(
                    kafkaProperties.buildConsumerProperties(sslBundles),
                    StringDeserializer(),
                    AvroDeserializer(DeviceMessage.getDecoder())
            )

    @Bean
    fun kafkaTemplate(producerFactory: ProducerFactory<String, DeviceMessage>) =
            KafkaTemplate(producerFactory)

    @Bean
    fun producerFactory(): ProducerFactory<String, DeviceMessage> =
            DefaultKafkaProducerFactory(
                    kafkaProperties.buildProducerProperties(sslBundles),
                    StringSerializer(),
                    AvroSerializer(DeviceMessage.getEncoder())
            )
}
