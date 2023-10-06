package org.gxf.crestdeviceservice.kafka.port

interface MessageConsumer<T> {
    fun consumeMessage(message: T)
}