package org.gxf.crestdeviceservice.psk.decryption

import org.springframework.boot.context.properties.ConfigurationProperties
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

@ConfigurationProperties("crest-device-service.psk.decryption")
class PskDecryptionConfiguration(val privateKey: RSAPrivateKey, val publicKey: RSAPublicKey)

