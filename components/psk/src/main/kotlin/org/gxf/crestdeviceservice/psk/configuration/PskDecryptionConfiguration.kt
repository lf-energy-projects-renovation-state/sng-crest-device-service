// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.psk.configuration

import java.security.interfaces.RSAPrivateKey
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("psk.decryption")
class PskDecryptionConfiguration(val privateKey: Map<String, RSAPrivateKey>, val method: String)
