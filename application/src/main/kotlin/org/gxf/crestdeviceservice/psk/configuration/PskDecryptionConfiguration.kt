package org.gxf.crestdeviceservice.psk.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

@ConfigurationProperties("crest-device-service.psk.decryption")
class PskDecryptionConfiguration(val privateKey: Map<String, RSAPrivateKey>, val publicKey: Map<String, RSAPublicKey>)
