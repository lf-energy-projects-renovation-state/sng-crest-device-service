package org.gxf.crestdeviceservice.port

interface MessagePort<T> {
    fun handleMessage(message: T)
}