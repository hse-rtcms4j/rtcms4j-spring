import com.fasterxml.jackson.databind.ObjectMapper
import dto.EndpointProperties
import ru.enzhine.rtcms4j.spring.client.json.JsonValuesHelperImpl
import kotlin.test.Test

class JsonValuesHelperImplTest {
    val objectMapper = ObjectMapper()
    val jsonValuesHelperImpl = JsonValuesHelperImpl(objectMapper)

    @Test
    fun test() {
        val endpointProperties = EndpointProperties()
        val values =
            jsonValuesHelperImpl.generateValues(endpointProperties, "1.0.0")
        println(values)
    }
}
