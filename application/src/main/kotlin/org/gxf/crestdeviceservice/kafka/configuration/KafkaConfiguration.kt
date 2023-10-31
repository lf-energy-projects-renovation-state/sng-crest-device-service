// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.gxf.crestdeviceservice.kafka.configuration

import com.gxf.utilities.kafka.avro.AvroDeserializer
import com.gxf.utilities.kafka.avro.AvroSerializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.gxf.crestdeviceservice.Measurement
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*


@Configuration
class KafkaConfiguration(private val kafkaProperties: KafkaProperties) {

    @Bean
    fun kafkaListenerContainerFactory(consumerFactory: ConsumerFactory<String, Measurement>): ConcurrentKafkaListenerContainerFactory<String, Measurement> =
            ConcurrentKafkaListenerContainerFactory<String, Measurement>()
                    .apply { this.consumerFactory = consumerFactory }

    @Bean
    fun consumerFactory(): ConsumerFactory<String, Measurement> =
            DefaultKafkaConsumerFactory(
                    kafkaProperties.buildConsumerProperties(),
                    StringDeserializer(),
                    AvroDeserializer(Measurement.getDecoder())
            )
    
    @Bean
    fun kafkaTemplate(producerFactory: ProducerFactory<String, Measurement>) =
            KafkaTemplate(producerFactory)

    @Bean
    fun producerFactory(): ProducerFactory<String, Measurement> =
            DefaultKafkaProducerFactory(
                    kafkaProperties.buildProducerProperties(),
                    StringSerializer(),
                    AvroSerializer(Measurement.getEncoder())
            )
}
