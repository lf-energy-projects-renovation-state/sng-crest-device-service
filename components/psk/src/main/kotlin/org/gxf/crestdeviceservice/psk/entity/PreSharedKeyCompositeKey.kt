// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice.psk.entity

import java.io.Serializable

data class PreSharedKeyCompositeKey(val identity: String?, val revision: Int?) : Serializable {
    constructor() : this(null, null)
}
