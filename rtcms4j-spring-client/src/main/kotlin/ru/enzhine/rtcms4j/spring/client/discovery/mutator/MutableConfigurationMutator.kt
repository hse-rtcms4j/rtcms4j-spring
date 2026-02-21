package ru.enzhine.rtcms4j.spring.client.discovery.mutator

import com.fasterxml.jackson.databind.ObjectMapper
import ru.enzhine.rtcms4j.spring.client.json.JsonSchemaGenerator
import ru.enzhine.rtcms4j.spring.client.json.JsonSchemaGeneratorImpl
import ru.enzhine.rtcms4j.spring.client.json.JsonValuesHelper
import ru.enzhine.rtcms4j.spring.client.json.JsonValuesHelperImpl

class MutableConfigurationMutator(
    objectMapper: ObjectMapper,
    private val mutableObject: Any,
    private val mutableObjectClass: Class<*>,
) : ConfigurationMutator {
    private val jsonSchema: String

    init {
        val jsonSchemaGenerator: JsonSchemaGenerator =
            JsonSchemaGeneratorImpl(objectMapper)

        jsonSchema = jsonSchemaGenerator.generateSchema(mutableObjectClass)
    }

    private val jsonValuesHelper: JsonValuesHelper =
        JsonValuesHelperImpl(objectMapper)
    private val objectReader =
        objectMapper
            .readerForUpdating(mutableObject)
            .withView(mutableObjectClass)

    override fun getTarget(): Any = mutableObject

    override fun getJsonSchema(): String = jsonSchema

    override fun getJsonValuesWithVersion(version: String) = jsonValuesHelper.generateValues(getTarget(), version)

    override fun updateValues(jsonValues: String) {
        val (_, src) = jsonValuesHelper.extractVersionFromValuesString(jsonValues)

        objectReader.readValue(src, mutableObjectClass)
    }
}
