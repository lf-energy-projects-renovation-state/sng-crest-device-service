package org.gxf.crestdeviceservice.psk.configuration

import org.springframework.boot.context.properties.ConfigurationPropertiesBinding
import org.springframework.core.convert.converter.Converter
import org.springframework.security.converter.RsaKeyConverters
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.security.interfaces.RSAPublicKey

/**
 * Decodes a x509 RSA Public key string to an object
 */
@Component
@ConfigurationPropertiesBinding
class RSAPublicKeyConverter : Converter<String, RSAPublicKey> {
    override fun convert(from: String): RSAPublicKey =
        RsaKeyConverters.x509()
            .convert(ByteArrayInputStream(from.toByteArray()))!!

}
