package ru.enzhine.rtcms4j.spring.client.discovery.mutator

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.aop.target.HotSwappableTargetSource
import ru.enzhine.rtcms4j.spring.client.json.JsonSchemaGenerator
import ru.enzhine.rtcms4j.spring.client.json.JsonSchemaGeneratorImpl
import ru.enzhine.rtcms4j.spring.client.json.JsonValuesHelper
import ru.enzhine.rtcms4j.spring.client.json.JsonValuesHelperImpl

class ImmutableConfigurationMutator(
    private val objectMapper: ObjectMapper,
    private val swappableTargetSource: HotSwappableTargetSource,
) : ConfigurationMutator {
    private val jsonSchema: String

    init {
        val jsonSchemaGenerator: JsonSchemaGenerator =
            JsonSchemaGeneratorImpl(objectMapper)
        jsonSchema = jsonSchemaGenerator.generateSchema(swappableTargetSource.targetClass)
    }

    private val jsonValuesHelper: JsonValuesHelper =
        JsonValuesHelperImpl(objectMapper)

    override fun getTarget(): Any = swappableTargetSource.target

    override fun getJsonSchema(): String = jsonSchema

    override fun getJsonValuesWithVersion(version: String): String = jsonValuesHelper.generateValues(getTarget(), version)

    override fun updateValues(jsonValues: String) {
        val (_, content) = jsonValuesHelper.extractVersionFromValuesString(jsonValues)

        val newTarget = objectMapper.readValue(content, swappableTargetSource.targetClass)
        swappableTargetSource.swap(newTarget)
    }
}
