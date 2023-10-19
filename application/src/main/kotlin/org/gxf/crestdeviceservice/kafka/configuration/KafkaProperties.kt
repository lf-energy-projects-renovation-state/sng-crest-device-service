package org.gxf.crestdeviceservice.kafka.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("crest-device-service.kafka")
class KafkaProperties(val id: String, val topicName: String)
