import com.fasterxml.jackson.databind.ObjectMapper
import org.gxf.crestdeviceservice.kafka.KafkaProducer
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
class KafkaProducerTest {

    @Mock
    private lateinit var mockedKafkaTemplate: KafkaTemplate<String, String>

    @Mock
    private lateinit var mockedKafkaProperties: KafkaProperties

    @InjectMocks
    private lateinit var kafkaProducer: KafkaProducer

    @BeforeEach
    fun setup() {
        `when`(mockedKafkaProperties.topicName).thenReturn("topic")
    }

    @Test
    fun shouldCallMessageProducerWithCorrectParams() {
        val jsonNode = ObjectMapper().readTree("{}")
        kafkaProducer.produceMessage(jsonNode)
        Mockito.verify(mockedKafkaTemplate).send("topic", jsonNode.toString())
    }
}
