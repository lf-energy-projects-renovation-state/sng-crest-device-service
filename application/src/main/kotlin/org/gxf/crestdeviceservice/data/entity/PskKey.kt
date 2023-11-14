package org.gxf.crestdeviceservice.data.entity

import java.io.Serializable
import java.time.Instant

class PskKey(val identity: String?,
             val revisionTimeStamp: Instant?) : Serializable {
    constructor() : this(null, null)
}
