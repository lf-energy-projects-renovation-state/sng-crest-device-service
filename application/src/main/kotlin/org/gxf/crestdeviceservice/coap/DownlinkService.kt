package org.gxf.crestdeviceservice.coap

import mu.KotlinLogging
import org.gxf.crestdeviceservice.psk.PskService
import org.springframework.stereotype.Service

@Service
class DownlinkService(private val pskService: PskService) {

    private val logger = KotlinLogging.logger {}

    fun getDownlinkForIdentity(identity: String): String {

        if (pskService.hasDefaultKey(identity)) {
            logger.info { "Device $identity has default key creating new key " }

            val newKey = pskService.generateAndSetNewKeyForIdentity(identity)

            return constructSetPskCommand(newKey)
        }

        return "0"
    }

    private fun constructSetPskCommand(key: String): String {
        return "PSK:${key};PSK:${key}SET"
    }
}
