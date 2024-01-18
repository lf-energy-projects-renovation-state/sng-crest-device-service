package org.gxf.crestdeviceservice.psk.decryption

import org.springframework.boot.context.properties.ConfigurationPropertiesBinding
import org.springframework.core.convert.converter.Converter
import org.springframework.security.converter.RsaKeyConverters
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.security.interfaces.RSAPrivateKey


@Component
@ConfigurationPropertiesBinding
class RSAPrivateKeyConverter : Converter<String, RSAPrivateKey> {
    override fun convert(from: String): RSAPrivateKey {
        return RsaKeyConverters.pkcs8().convert(ByteArrayInputStream(from.toByteArray()))!!
        
    }
}
