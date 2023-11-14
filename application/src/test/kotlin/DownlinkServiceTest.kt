import org.gxf.crestdeviceservice.coap.DownlinkService
import org.gxf.crestdeviceservice.psk.PskService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any

@ExtendWith(MockitoExtension::class)
class DownlinkServiceTest {

    @Mock
    private lateinit var pskService: PskService

    @InjectMocks
    private lateinit var downLinkService: DownlinkService

    @Test
    fun shouldReturnNoActionDownlinkWhenThereIsNoNewPsk() {
        `when`(pskService.hasDefaultKey(any())).thenReturn(false)

        val result = downLinkService.getDownlinkForIdentity("identity")

        assertEquals("0", result)
    }

    @Test
    fun shouldReturnPskDownlinkWhenThereIsANewPsk() {
        `when`(pskService.hasDefaultKey(any())).thenReturn(true)
        `when`(pskService.generateAndSetNewKeyForIdentity(any())).thenReturn("key")

        val result = downLinkService.getDownlinkForIdentity("identity")

        // Psk command is formatted as: PSK:[Key];PSK:[Key]SET
        assertEquals("PSK:key;PSK:keySET", result)
    }
}