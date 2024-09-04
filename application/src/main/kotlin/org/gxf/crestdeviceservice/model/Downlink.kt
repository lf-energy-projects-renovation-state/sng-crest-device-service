package org.gxf.crestdeviceservice.model

import io.github.oshai.kotlinlogging.KotlinLogging
import org.gxf.crestdeviceservice.command.entity.Command

class Downlink{
    private val commands = listOf<Command>()
    private var downlink = ""

    private val logger = KotlinLogging.logger {}

}