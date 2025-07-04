// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice

import org.gxf.crestdeviceservice.model.ShipmentFileDTO
import org.gxf.crestdeviceservice.model.ShipmentFileDTO.Device
import org.gxf.crestdeviceservice.model.ShipmentFileDTO.Device.Rtu
import org.gxf.crestdeviceservice.model.ShipmentFileDTO.Shipment

object ShipmentFileFactory {
    fun testDevice() =
        Device(
            Rtu(
                rtuId = "869777040008792",
                pskEncrypted =
                    "OqixTadPZgHzUJx3azoqjBbkUtpHGcpBbA5alyjW4BGi8I45i5Cv2mcdXZXIr4d9qHaBy5H1GWkpCs0gTJp3I+RpmnaO3uYc+kwUPo43XRq+xQjtO12oykzCTnF93NAgjXpwR6hpFkYN02Gmb9VgRNKgnT4EnTS8O5qU3yK7jzyAE73WfIh8DcHxKjCaVgFDcwCYABCPInKTxLrK4zw5S/LZK/2TvROvDPYHcfZMxjil6bXgPE8TJDaCCYDHUh7BcvykRFR1X5ojqWJ65Zl3iEBAdN6NYZZVfV6JNk0tCO5HPg3AYG2J1FvSxqwW6OEWS6eKovv9cygX71XeBLHyIg==",
                pskChangeSecretEncrypted =
                    "dFLqPcPAF5ko8xRamCNmRK5nSbtdoCOpVWGK2phfYXuqBpUzX1XhC5tWm8AMQGM27XYt14KETqMACznt0jyw067O9YpCSVxdIyFxptcRQCu4oTXvqGHu2zW1MbMY1mU2JLOOf+r5k1twEn4VU7MCP89XrDDqee6fjy56yDA+H8Uhd5Y3yOPuqDLzF2x2N5E92MIcIFz6fnsIFE5ACXxBJz96aSAsElx1OJyX/OE0F7z8OtjT6Ix8KPVPPS8U/m0avaAro3oOaa0+hSUR4Ik8AoWWWD5lQBcsuWgZFO5Ce4uxhUo+dzAQCklFqnLlfCnXq855gXWpPTDhc8sbGvdTYw==",
                pskEncryptionKeyRef = "1",
            )
        )

    fun testShipmentFileDTO() = ShipmentFileDTO(Shipment(version = "1", shipmentNumber = "12345"), listOf(testDevice()))
}
