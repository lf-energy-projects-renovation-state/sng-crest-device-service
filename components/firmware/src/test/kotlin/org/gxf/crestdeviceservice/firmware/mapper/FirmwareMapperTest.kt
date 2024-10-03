// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.firmware.mapper

import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.gxf.crestdeviceservice.firmware.entity.Firmware
import org.gxf.crestdeviceservice.firmware.entity.FirmwarePacket
import org.junit.jupiter.api.Test

class FirmwareMapperTest {
    val previousVersion = "01.10"
    val version = "01.20"
    val name = "RTU#DELTA#$previousVersion#TO#$version.txt"
    val packetNumber = 4
    val line =
        "OTA00045a_x906|nMQBzY)Lq<nEB4l)I*bwN-0CRR_a&u{KZXzr&EHMoLL7Wii+5l85QBzY)Lq<nEB4cTAXk|1I=;{D+b9HHSWg;vvEHHR5FhNC15a{{<4FDx`CICTHD^W&IP(@BXeGusi03uaJQ#vqTb7gXNWpXZXb9@l#8USk|EHDiKD^W&IP(@BUAONWl=_&w0R4Y+NQbj>TJtcJw06|po5a~JqD^W&6OixoqJtcJw06|nM@et`s08vInMNB;+RYp@fFkf?Ja`6!9S^#!saxQRlYa\$H*Rv-X0FfyqS>1qG~K~yVIMpsfvR7E`{bu1;55b1gVbqxSRPfkunLsS4OQASr%mJsQR07+CuIv@Z~aAj^f06|nMQDhM5ngByjK~PUnMNU01EFvX!E+udf>8b#AE+us?C3PY!C3Ot|Nn~tF5b3%AWiLc&b7OCAZe?S1Wn=(Pmk{a70B~h)X>Ml#LvL<wWn*+{ZW|Ek+5l\$&LvL<wWn*+@WB^2Ib7Qa&>FNM)Zf<2`bZKs906}bbVR>`s5b62=En{qNb7de!Zee0<Wn=(Bz7XmP08}eQM@>>aFf26<04qgDO>+?H8URu{AR<sP08>s0Iv^!;071YI>M8(KD?>+9P7MG_O+`rwIv^!^5b8Pra{xh9D?>?5Neuu=O;bq;FA(ZV06HKga{xh9D^Wv3Nkk0*D{c_#S^!Z)LrFwBAOJ~2LrFvlIv_6)>S_Qba{xh9D??LBO+7R+EFv)X5bAmWF+nsiG%zqRFhMXdFfcGMU=Zqx05vr^F)%SOMn*<PMn*<PIIj@ungBU4FfcGMGB7YA4FD@cQ%QIb>Z\$-uIv@Z+R4YSMNliU5Ff1b75bC-BI59&(FfcViFfbww07XeeC=lw(00}xEC365zWNBn?IsjI0WbX{>+5ll@Z*FBe083C#IsnSd5o`sv%m#t;Y1D#\$sA!kyf_Y+%b;tG3Hn6jnKJaY:DONE"
    val uuid = UUID.randomUUID()

    @Test
    fun mapFirmwareDTOToEntity() {
        // todo
    }

    @Test
    fun getFirmwareVersionFromName() {
        val result = FirmwareMapper.getFirmwareVersionFromName(name)

        assertThat(result).isEqualTo(version)
    }

    @Test
    fun mapLineToPacket() {
        val expected = FirmwarePacket(testFirmware(), packetNumber, line)
        // todo finish
    }

    @Test
    fun getPacketNumberFromLine() {
        val result = FirmwareMapper.getPacketNumberFromLine(line)

        assertThat(result).isEqualTo(packetNumber)
    }

    fun testFirmware() = Firmware(uuid, name, version, uuid, mutableListOf())
}
