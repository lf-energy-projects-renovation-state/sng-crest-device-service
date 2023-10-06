package org.gxf.crestdeviceservice.service

import com.fasterxml.jackson.databind.JsonNode
import org.gxf.crestdeviceservice.kafka.port.MessageProducer
import org.gxf.crestdeviceservice.port.MessagePort
import org.springframework.stereotype.Service

@Service
class MessageService(private val messageProducer: MessageProducer<JsonNode>) : MessagePort<JsonNode> {
    override fun handleMessage(message: JsonNode) {
        messageProducer.produceMessage(message)
    }
}