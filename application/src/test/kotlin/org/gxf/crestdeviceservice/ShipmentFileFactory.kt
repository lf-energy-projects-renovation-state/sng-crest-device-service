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
                    "hKizC5YXZMLHRFacZA2YTZdZ7DMJZIGaZTNs1uy2fKZ2VBQq5npWF3iSXly0sVinreKqOUKXrniei37xIUZ7ZnfD0SEbk59MUdPAIruaBZGPAgUV6KP68PuF3Xg8oln9DG59awHq8hZ7BDkXUUPNAFojuoB1wm0soohVP+9kY9H/6f+nEfuYnbOuqrIVqEPFGTR0FdRO9MUizAmp5XA7SVXAH4l182G0hbPJ6RyEXiYTJLAda8D66Bg9LMm/XWTPc35Qp+laprwwJ/drsQ8K4b6/Jq6etLwqQ23FBvz2fl+PbQpmR1FKDzE1P3pxCI8N6TJCoTYGMHNdZ4b3uzpfpjI/v53cdyAR+uz0KzKCPccrbeHDsh+p//iiBw0p/7WE8f6xk9ztv1ECmexmN27hl1M2M2hxSOmOug13mtNwyYzD9mUOTeBR+sXQr3x9PJXb1qtIedCKNW0/MejiIbIa0ATfMZrbAygbKzt6TGpGP5g6Z98j54bUHLrtIBu+bM8ZbQZCceolcMNs30q9t1lk7TKXuJGwLPrzxRm1wJR7So1wuqaYGawkdYt0VsbTQBZqg4OVs9JmHCJml8o/7MJIk+jfoDlE8uOtk2dToUyU10gaQVn2OVNfi1sh/t5VttI0rJhp/dhmCCxiNiUZ1AtAavQLrJLfrbnBbDukZZ2ZIjk=",
                pskChangeSecretEncrypted =
                    "hKizC5YXZMLHRFacZA2YTZdZ7DMJZIGaZTNs1uy2fKZ2VBQq5npWF3iSXly0sVinreKqOUKXrniei37xIUZ7ZnfD0SEbk59MUdPAIruaBZGPAgUV6KP68PuF3Xg8oln9DG59awHq8hZ7BDkXUUPNAFojuoB1wm0soohVP+9kY9H/6f+nEfuYnbOuqrIVqEPFGTR0FdRO9MUizAmp5XA7SVXAH4l182G0hbPJ6RyEXiYTJLAda8D66Bg9LMm/XWTPc35Qp+laprwwJ/drsQ8K4b6/Jq6etLwqQ23FBvz2fl+PbQpmR1FKDzE1P3pxCI8N6TJCoTYGMHNdZ4b3uzpfpjI/v53cdyAR+uz0KzKCPccrbeHDsh+p//iiBw0p/7WE8f6xk9ztv1ECmexmN27hl1M2M2hxSOmOug13mtNwyYzD9mUOTeBR+sXQr3x9PJXb1qtIedCKNW0/MejiIbIa0ATfMZrbAygbKzt6TGpGP5g6Z98j54bUHLrtIBu+bM8ZbQZCceolcMNs30q9t1lk7TKXuJGwLPrzxRm1wJR7So1wuqaYGawkdYt0VsbTQBZqg4OVs9JmHCJml8o/7MJIk+jfoDlE8uOtk2dToUyU10gaQVn2OVNfi1sh/t5VttI0rJhp/dhmCCxiNiUZ1AtAavQLrJLfrbnBbDukZZ2ZIjk=",
                pskEncryptionKeyRef = "1",
            )
        )

    fun testShipmentFileDTO() = ShipmentFileDTO(Shipment(version = "1", shipmentNumber = "12345"), listOf(testDevice()))
}
