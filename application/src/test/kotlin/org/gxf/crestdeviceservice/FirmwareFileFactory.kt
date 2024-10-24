// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice

import org.springframework.core.io.ClassPathResource
import org.springframework.mock.web.MockMultipartFile

object FirmwareFileFactory {
    fun getFirmwareFile(): MockMultipartFile {
        val fileName = "RTU#FULL#TO#23.10.txt"
        val firmwareFile = ClassPathResource(fileName).file
        return MockMultipartFile("file", firmwareFile.name, "text/plain", firmwareFile.readBytes())
    }
}
