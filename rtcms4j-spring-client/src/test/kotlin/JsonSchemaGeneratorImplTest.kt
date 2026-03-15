import com.fasterxml.jackson.databind.ObjectMapper
import dto.EndpointProperties
import ru.enzhine.rtcms4j.spring.client.json.JsonSchemaGeneratorImpl
import kotlin.test.Test

class JsonSchemaGeneratorImplTest {
    val objectMapper = ObjectMapper()
    val jsonSchemaGenerator = JsonSchemaGeneratorImpl(objectMapper)

    @Test
    fun test() {
        val schema =
            jsonSchemaGenerator.generateSchema(EndpointProperties::class.java)
        println(schema)
    }
}
