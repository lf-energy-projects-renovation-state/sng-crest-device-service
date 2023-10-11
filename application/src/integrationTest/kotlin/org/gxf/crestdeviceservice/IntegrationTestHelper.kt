package org.gxf.crestdeviceservice

import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.util.ResourceUtils

object IntegrationTestHelper {

    fun createKafkaConsumer(embeddedKafkaBroker: EmbeddedKafkaBroker, topic: String): Consumer<String, String> {
        val testProperties =
                KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafkaBroker)
        val consumerFactory =
                DefaultKafkaConsumerFactory(
                        testProperties,
                        StringDeserializer(),
                        StringDeserializer()
                )
        val consumer = consumerFactory.createConsumer()
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, topic)
        return consumer
    }

    /**
     * Copy of the kafka test util producer props with different serializers
     * @see KafkaTestUtils.producerProps
     */
    private fun producerProps(brokers: String): Map<String, Any> {
        val props: MutableMap<String, Any> = java.util.HashMap()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = brokers
        props[ProducerConfig.BATCH_SIZE_CONFIG] = "16384"
        props[ProducerConfig.LINGER_MS_CONFIG] = 1
        props[ProducerConfig.BUFFER_MEMORY_CONFIG] = "33554432"
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        return props
    }

    fun getFileContentAsString(path: String): String {
        return ResourceUtils.getFile("classpath:$path").readText()
    }

}
