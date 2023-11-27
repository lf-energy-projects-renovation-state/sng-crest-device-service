package org.gxf.crestdeviceservice.data.entity

import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import org.gxf.crestdeviceservice.data.convertors.DatabaseFieldEncryptor
import java.time.Instant

@Entity
@IdClass(PreSharedKeyCompositeKey::class)
class PreSharedKey(@Id val identity: String,
                   @Id val revisionTime: Instant,
                   @Convert(converter = DatabaseFieldEncryptor::class) val preSharedKey: String
)
