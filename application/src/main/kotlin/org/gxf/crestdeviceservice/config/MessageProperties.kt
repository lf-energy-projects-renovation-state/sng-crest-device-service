package org.gxf.crestdeviceservice.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("message")
class MessageProperties(
    val maxBytes: Int
)