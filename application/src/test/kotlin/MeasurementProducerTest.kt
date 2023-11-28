import com.fasterxml.jackson.databind.ObjectMapper
import org.gxf.crestdeviceservice.kafka.MeasurementProducer
import org.gxf.crestdeviceservice.kafka.configuration.KafkaProducerProperties
import org.gxf.message.Measurement
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.check
import org.springframework.kafka.core.KafkaTemplate

@ExtendWith(MockitoExtension::class)
class MeasurementProducerTest {

    @Mock
    private lateinit var mockedKafkaTemplate: KafkaTemplate<String, Measurement>

    @Mock
    private lateinit var mockedKafkaProducerProperties: KafkaProducerProperties

    @InjectMocks
    private lateinit var measurementProducer: MeasurementProducer

    @BeforeEach
    fun setup() {
        `when`(mockedKafkaProducerProperties.topicName).thenReturn("topic")
    }

    @Test
    fun shouldCallMessageProducerWithCorrectParams() {
        val jsonNode = ObjectMapper().readTree("""
            {
                "ID":12345
            }
        """)
        measurementProducer.produceMessage(jsonNode)
        verify(mockedKafkaTemplate).send(
                check { assertEquals("topic", it) }, check { assertEquals(jsonNode.toString(), it.payload) })
    }
}
