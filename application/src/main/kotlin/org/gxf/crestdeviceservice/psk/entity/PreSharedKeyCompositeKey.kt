package org.gxf.crestdeviceservice.psk.entity

import java.io.Serializable
import java.time.Instant

class PreSharedKeyCompositeKey(val identity: String?,
                               val revisionTime: Instant?) : Serializable {
    constructor() : this(null, null)
}
