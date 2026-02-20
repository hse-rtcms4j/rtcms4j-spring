package ru.enzhine.rtcms4j.spring.client.json

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.victools.jsonschema.generator.Option
import com.github.victools.jsonschema.generator.OptionPreset
import com.github.victools.jsonschema.generator.SchemaGenerator
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder
import com.github.victools.jsonschema.generator.SchemaVersion
import com.github.victools.jsonschema.module.jackson.JacksonModule

class JsonSchemaGeneratorImpl(
    private val objectMapper: ObjectMapper,
) : JsonSchemaGenerator {
    val schemaConfig =
        SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON)
            .with(JacksonModule())
            .with(Option.ADDITIONAL_FIXED_TYPES)
            .without(Option.NULLABLE_FIELDS_BY_DEFAULT)
            .without(Option.NULLABLE_ARRAY_ITEMS_ALLOWED)
            .build()!!

    val schemaGenerator = SchemaGenerator(schemaConfig)

    override fun generateSchema(clazz: Class<*>): String {
        val schemaNode =
            schemaGenerator.generateSchema(clazz)

        addVersionField(schemaNode)
        disallowAdditionalProperties(schemaNode)

        return schemaNode.toPrettyString()
    }

    private fun addVersionField(schemaNode: JsonNode) {
        if (schemaNode is ObjectNode) {
            val properties =
                if (schemaNode.has("properties")) {
                    schemaNode.get("properties") as ObjectNode
                } else {
                    schemaNode.putObject("properties")
                }

            if (!properties.has("version")) {
                val versionSchema =
                    objectMapper.createObjectNode().apply {
                        put("type", "string")
                        put("description", "Commit compatibility version.")
                    }
                properties.set<ObjectNode>("version", versionSchema)
            }
        }
    }

    private fun disallowAdditionalProperties(schemaNode: JsonNode) {
        if (schemaNode is ObjectNode) {
            schemaNode.put("additionalProperties", false)
        }
    }
}
