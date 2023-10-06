package org.gxf.crestdeviceservice.kafka.port

interface MessageProducer<T> {
    fun produceMessage(message: T)
}