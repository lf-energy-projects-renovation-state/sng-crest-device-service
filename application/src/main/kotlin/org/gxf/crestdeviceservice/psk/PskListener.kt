package org.gxf.crestdeviceservice.psk

import com.alliander.sng.DeviceCredentials
import org.gxf.crestdeviceservice.psk.decryption.PskDecryptor
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class PskListener(val pskService: PskService, val pskDecryptor: PskDecryptor) {

    @KafkaListener(
        topics = ["\${crest-device-service.kafka.pre-shared-key-consumer.topic-name}"],
        id = "\${crest-device-service.kafka.pre-shared-key-consumer.id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun handleIncomingPSK(psk: DeviceCredentials) {
        val decryptedPsk = pskDecryptor.decryptSecret(psk.psk)
        val decryptedSecret = pskDecryptor.decryptSecret(psk.secret)
        pskService.setInitialKeyForIdentify(psk.imei, decryptedPsk, decryptedSecret)
    }
}
