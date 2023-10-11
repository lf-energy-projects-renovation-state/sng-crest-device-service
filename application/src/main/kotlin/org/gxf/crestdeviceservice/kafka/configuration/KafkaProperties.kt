package org.gxf.crestdeviceservice.kafka.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("kafka")
class KafkaProperties(val id: String, val topicName: String)