import com.fasterxml.jackson.databind.ObjectMapper
import org.gxf.crestdeviceservice.kafka.MeasurementProducer
import org.gxf.crestdeviceservice.kafka.configuration.KafkaProperties
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.kafka.core.KafkaTemplate

@ExtendWith(MockitoExtension::class)
class MeasurementProducerTest {

    @Mock
    private lateinit var mockedKafkaTemplate: KafkaTemplate<String, String>

    @Mock
    private lateinit var mockedKafkaProperties: KafkaProperties

    @InjectMocks
    private lateinit var measurementProducer: MeasurementProducer

    @BeforeEach
    fun setup() {
        `when`(mockedKafkaProperties.topicName).thenReturn("topic")
    }

    @Test
    fun shouldCallMessageProducerWithCorrectParams() {
        val jsonNode = ObjectMapper().readTree("{}")
        measurementProducer.produceMessage(jsonNode)
        Mockito.verify(mockedKafkaTemplate).send("topic", jsonNode.toString())
    }
}
