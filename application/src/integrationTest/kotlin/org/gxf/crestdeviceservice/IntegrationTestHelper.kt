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

    fun getFileContentAsString(path: String): String {
        return ResourceUtils.getFile("classpath:$path").readText()
    }

}
